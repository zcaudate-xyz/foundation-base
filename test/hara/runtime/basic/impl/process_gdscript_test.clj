(ns hara.runtime.basic.impl.process-gdscript-test
  (:require [hara.lang :as l]
            [hara.lang.impl :as impl]
            [hara.model.spec-gdscript]
            [hara.runtime.basic.impl.process-gdscript :as gd]
            [std.lib.os :as os])
  (:use code.test))

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
