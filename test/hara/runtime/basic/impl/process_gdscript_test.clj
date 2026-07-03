(ns hara.runtime.basic.impl.process-gdscript-test
  (:require [hara.lang :as l]
            [hara.lang.impl :as impl]
            [hara.model.spec-gdscript]
            [hara.runtime.basic.impl.process-gdscript :as gd]
            [std.fs :as fs]
            [std.lib.env :as env]
            [std.lib.os :as os])
  (:use code.test))

(fact:global
 {:skip (not (or (env/program-exists? "godot")
                 (env/program-exists? "godot-4")))})

^{:refer hara.runtime.basic.impl.process-gdscript/default-body-transform :added "4.1"}
(fact "wraps body forms for return"
  (impl/emit-as :gdscript [(gd/default-body-transform '[(:= a 1) (+ a 2)] {:bulk true})])
  => "a = 1\nreturn a + 2")

^{:refer hara.runtime.basic.impl.process-gdscript/default-oneshot-wrap :added "4.1"}
(fact "wraps emitted GDScript in a SceneTree script"
  (let [body (impl/emit-as :gdscript [(gd/default-body-transform '[(:= a 1) (+ a 2)] {:bulk true})])]
    (gd/default-oneshot-wrap body "/tmp/out.json"))
  => (str "extends SceneTree\n\n"
          "func OUT_FN():\n"
          "  a = 1\n"
          "  return a + 2\n\n"
          "func _init():\n"
          "  var __result__ = OUT_FN()\n"
          "  var __json__ = JSON.stringify({\"type\": \"data\", \"value\": __result__})\n"
          "  var __file__ = FileAccess.open(\"/tmp/out.json\", FileAccess.WRITE)\n"
          "  __file__.store_string(__json__)\n"
          "  __file__.close()\n"
          "  print(__json__)\n"
          "  quit()\n"))

^{:refer hara.runtime.basic.impl.process-gdscript/sh-exec-gdscript :added "4.1"}
(fact "flattens nested process options before invoking Godot"
  (let [calls (atom [])]
    (with-redefs [os/sh (fn [opts]
                          (swap! calls conj opts)
                          :proc)
                  os/sh-write (fn [& _] nil)
                  os/sh-close (fn [& _] nil)
                  os/sh-wait (fn [& _] nil)
                  os/sh-output (fn [_] {:exit 0 :out "ok\n" :err ""})]
      [(gd/sh-exec-gdscript ["godot" "--headless" "--script"] "file.gd"
                             {:process {:root "/tmp/rt" :pipe false}})
       (:root (first @calls))
       (:args (first @calls))]))
  => ["ok" "/tmp/rt" ["godot" "--headless" "--script" "file.gd"]])

^{:refer hara.runtime.basic.impl.process-gdscript/ensure-project! :added "4.1"}
(fact "creates a minimal Godot project in the runtime dir"
  (let [tmp (str (System/getProperty "user.home") "/gw_gd_ensure_" (System/currentTimeMillis))]
    (try
      (with-redefs [gd/+gdscript-runtime-dir+ tmp]
        (gd/ensure-project!)
        [(.. (java.io.File. tmp "project.godot") exists)
         (slurp (java.io.File. tmp "project.godot"))])
      => [true (str "[application]\n"
                    "config/name=\"hara_gdscript_runtime\"\n"
                    "config/features=PackedStringArray(\"4.2\")\n\n"
                    "[rendering]\n"
                    "renderer/rendering_method=\"mobile\"\n")]
      (finally
        (fs/delete tmp)))))

^{:refer hara.runtime.basic.impl.process-gdscript/wrap-godot-eval :added "4.1"}
(fact "wraps emitted GDScript in a Node eval script"
  (let [body (impl/emit-as :gdscript [(gd/default-body-transform '[(:= a 1) (+ a 2)] {:bulk true})])]
    (gd/wrap-godot-eval body))
  => (str "extends Node\n\n"
          "\n\n"
          "func eval():\n"
          "  a = 1\n"
          "  return a + 2\n"))

^{:refer hara.runtime.basic.impl.process-gdscript/transform-form-verify :added "4.1"}
(fact "wraps emitted GDScript in a SceneTree script for syntax checking"
  (gd/transform-form-verify '[(:= a 1)] {})
  => '(:- "extends SceneTree\n\n\n\nfunc _init():\n  a = 1"))

^{:refer hara.runtime.basic.impl.process-gdscript/default-oneshot-in :added "4.1"}
(fact "writes generated script to runtime project and returns filename"
  (let [tmp (str (System/getProperty "user.home") "/gw_gd_oneshot_" (System/currentTimeMillis))
        body (impl/emit-as :gdscript [(gd/default-body-transform '[(:= a 1) (+ a 2)] {:bulk true})])]
    (try
      (with-redefs-fn {(var gd/+gdscript-runtime-dir+) tmp
                       (var gd/+current-output-file+) (atom nil)}
        #(let [filename (gd/default-oneshot-in body)]
           [(.startsWith filename "__eval_")
            (.endsWith filename ".gd")
            (.exists (java.io.File. tmp filename))
            (some? @@(var gd/+current-output-file+))]))
      => [true true true true]
      (finally
        (fs/delete tmp)))))

^{:refer hara.runtime.basic.impl.process-gdscript/default-oneshot-out :added "4.1"}
(fact "polls output file and deletes it after reading"
  (let [tmp (java.io.File. (System/getProperty "user.home") (str "gw_gd_out_" (System/currentTimeMillis)))]
    (try
      (spit tmp "{\"type\": \"data\", \"value\": 42}")
      (with-redefs-fn {(var gd/+current-output-file+) (atom tmp)}
        #(vector (gd/default-oneshot-out "header\n\nstdout line")
                 (.exists tmp)))
      => ["{\"type\": \"data\", \"value\": 42}" false]
      (finally
        (when (.exists tmp) (.delete tmp))))))

^{:refer hara.runtime.basic.impl.process-gdscript/default-oneshot-out :added "4.1"}
(fact "falls back to the last non-blank stdout line"
  (with-redefs-fn {(var gd/+current-output-file+) (atom (java.io.File. "/nonexistent/path"))}
    #(gd/default-oneshot-out "Godot Engine header\n\n  \nfinal line\n"))
  => "final line")

^{:refer hara.runtime.basic.impl.process-gdscript/verify-exec-gdscript :added "4.1"}
(fact "writes GDScript to runtime project and runs Godot syntax check"
  (let [tmp (str (System/getProperty "user.home") "/gw_gd_verify_" (System/currentTimeMillis))
        body "extends SceneTree\n\nfunc _init():\n  pass\n  quit()\n"]
    (try
      (with-redefs [gd/+gdscript-runtime-dir+ tmp]
        (gd/verify-exec-gdscript ["godot-4" "--headless" "--script"] body {:extension "gd" :root tmp}))
      => body
      (finally
        (fs/delete tmp)))))
