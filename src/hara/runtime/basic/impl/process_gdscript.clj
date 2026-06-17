(ns hara.runtime.basic.impl.process-gdscript
  (:require [clojure.string]
            [xt.lang.common-promise]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.model.spec-gdscript :as spec]
            [std.lib.os :as os]
            [xt.lang.common-lib :as lib]))

;;
;; PROGRAM
;;

(def +program-init+
  (common/put-program-options
   :gdscript {:default {:oneshot   :godot-4
                        :interactive :godot-4}
              :env {:godot-4 {:exec  "godot-4"
                              :flags {:oneshot     ["--headless" "--script"]
                                      :interactive ["--script"]}}
                    :godot   {:exec  "godot"
                              :flags {:oneshot     ["--headless" "--script"]
                                      :interactive ["--script"]}}}}))

;;
;; ONESHOT
;;

(def +gdscript-runtime-dir+
  "Project directory used for Godot oneshot evaluation.

   Must be a non-hidden directory inside the user's home because the
   Godot snap cannot read paths under dot-directories."
  (str (System/getProperty "user.home") "/hara_gdscript_runtime"))

(defn ensure-project!
  "creates a minimal Godot project in the runtime dir if missing"
  {:added "4.1"}
  []
  (let [dir (java.io.File. +gdscript-runtime-dir+)]
    (when-not (.exists dir)
      (.mkdirs dir))
    (let [project (java.io.File. dir "project.godot")]
      (when-not (.exists project)
        (spit project (str "[application]\n"
                           "config/name=\"hara_gdscript_runtime\"\n"
                           "config/features=PackedStringArray(\"4.2\")\n\n"
                           "[rendering]\n"
                           "renderer/rendering_method=\"mobile\"\n"))))))

(def ^:private +current-output-file+
  "Holds the output file for the most recent oneshot eval.

   The Godot snap wrapper exits before the engine has finished writing to
   stdout, so the runtime writes results to a file and polls for it instead."
  (atom nil))

(defn- poll-file
  "waits up to timeout-ms for file to exist and contain data, then returns contents"
  [file timeout-ms]
  (let [start (System/currentTimeMillis)]
    (loop []
      (if (and (.exists ^java.io.File file)
               (pos? (.length ^java.io.File file)))
        (let [contents (slurp file)]
          (.delete ^java.io.File file)
          contents)
        (if (< (- (System/currentTimeMillis) start) timeout-ms)
          (do (Thread/sleep 50)
              (recur))
          nil)))))

(defn default-body-transform
  "normalizes and return-formats a sequence of body forms"
  {:added "4.1"}
  [input mopts]
  (let [forms (rt/normalize-body-forms input mopts)]
    (apply list 'do
           (rt/return-format forms '#{:- := var var* local def defn break throw}))))

(defn- indent-body
  "indents each non-empty line of a GDScript body by two spaces"
  [body]
  (->> (clojure.string/split-lines body)
       (map (fn [line]
              (if (clojure.string/blank? line)
                line
                (str "  " line))))
       (clojure.string/join "\n")))

(defn default-oneshot-wrap
  "wraps emitted GDScript body in a SceneTree script"
  {:added "4.1"}
  [body output-path]
  (str "extends SceneTree\n\n"
       "func OUT_FN():\n"
       (indent-body body) "\n\n"
       "func _init():\n"
       "  var __result__ = OUT_FN()\n"
       "  var __json__ = JSON.stringify({\"type\": \"data\", \"value\": __result__})\n"
       "  var __file__ = FileAccess.open(\"" output-path "\", FileAccess.WRITE)\n"
       "  __file__.store_string(__json__)\n"
       "  __file__.close()\n"
       "  print(__json__)\n"
       "  quit()\n"))

(defn- spit-sync
  "writes content to file and forces an fsync so subprocesses see it immediately"
  [file content]
  (let [file (if (string? file) (java.io.File. file) file)]
    (with-open [out (java.io.FileOutputStream. file)]
      (.write out (.getBytes ^String content "UTF-8"))
      (.flush out)
      (.sync (.getFD out)))))

(defn default-oneshot-in
  "writes the generated script to the runtime project and returns its filename"
  {:added "4.1"}
  [body]
  (ensure-project!)
  (let [ts (System/currentTimeMillis)
        filename (str "__eval_" ts ".gd")
        output-file (str "__eval_" ts ".out")
        file (java.io.File. +gdscript-runtime-dir+ filename)
        out-file (java.io.File. +gdscript-runtime-dir+ output-file)]
    (.delete out-file)
    (spit-sync file (default-oneshot-wrap body (.getAbsolutePath out-file)))
    (reset! +current-output-file+ out-file)
    filename))

(defn default-oneshot-out
  "Godot prints an engine header before script output and the snap
   wrapper may exit before the engine has finished writing to stdout.
   The script therefore also writes the JSON payload to a file; this
   function polls for that file and falls back to stdout parsing."
  {:added "4.1"}
  [out]
  (or (some-> @+current-output-file+
              (poll-file 10000))
      (->> (clojure.string/split-lines out)
           (remove clojure.string/blank?)
           last)))

(def +gdscript-oneshot-config+
  (common/set-context-options
   [:gdscript :oneshot :default]
   {:main  {:in    #'default-oneshot-in
            :out   #'default-oneshot-out}
    :emit  {:body  {:transform #'default-body-transform}}
    :process {:root +gdscript-runtime-dir+
              :pipe false}
    :json :full}))

(def +gdscript-oneshot+
  [(rt/install-type!
    :gdscript :oneshot
    {:type :hara/rt.oneshot
     :instance {:create #'oneshot/rt-oneshot:create}})])

