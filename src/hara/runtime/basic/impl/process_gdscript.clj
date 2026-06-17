(ns hara.runtime.basic.impl.process-gdscript
  (:require [clojure.string]
            [xt.lang.common-promise]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-twostep :as twostep]
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
   :gdscript {:default {:twostep   :godot-4
                        :interactive :godot-4}
              :env {:godot-4 {:exec  "godot-4"
                              :flags {:twostep     ["--headless" "--script"]
                                      :interactive ["--script"]}}
                    :godot   {:exec  "godot"
                              :flags {:twostep     ["--headless" "--script"]
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

(defn- split-body
  "Splits emitted GDScript into top-level function definitions and the
   remaining executable body. GDScript does not allow nested functions,
   so definitions must be emitted at the top level of the script."
  [body]
  (let [lines (clojure.string/split-lines body)
        part-of-def? (fn [line]
                       (or (clojure.string/blank? line)
                           (clojure.string/starts-with? line "func ")
                           (clojure.string/starts-with? line " ")))
        [defs remaining] (split-with part-of-def? lines)]
    [(clojure.string/join "\n" defs)
     (indent-body (clojure.string/join "\n" remaining))]))

(defn default-oneshot-wrap
  "wraps emitted GDScript body in a SceneTree script, lifting top-level
   function definitions out of OUT_FN so GDScript parses correctly."
  {:added "4.1"}
  [body output-path]
  (let [[defs wrapped-body] (split-body body)]
    (str "extends SceneTree\n\n"
         defs "\n\n"
         "func OUT_FN():\n"
         wrapped-body "\n\n"
         "func _init():\n"
         "  var __result__ = OUT_FN()\n"
         "  var __json__ = JSON.stringify({\"type\": \"data\", \"value\": __result__})\n"
         "  var __file__ = FileAccess.open(\"" output-path "\", FileAccess.WRITE)\n"
         "  __file__.store_string(__json__)\n"
         "  __file__.close()\n"
         "  print(__json__)\n"
         "  quit()\n")))

(defn wrap-godot-eval
  "Takes emitted GDScript body (function defs + executable statements) and
   wraps it in a Node script with a top-level eval() function suitable for
   the persistent :godot runtime."
  {:added "4.1"}
  [body]
  (let [[defs wrapped-body] (split-body body)]
    (str "extends Node\n\n"
         defs "\n\n"
         "func eval():\n"
         wrapped-body "\n")))

(defn- spit-sync
  "writes content to file and forces an fsync so subprocesses see it"
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

(defn- normalize-process
  "flattens a nested :process block into the top-level process map"
  [process]
  (if (map? process)
    (merge (:process process)
           (dissoc process :process))
    process))

(defn sh-exec-gdscript
  "basic function for executing a GDScript process"
  {:added "4.1"}
  [input-args input-body process]
  (let [process (normalize-process process)
        {:keys [pipe
                trim
                stderr
                raw
                root
                shell]
         :or {trim clojure.string/trim-newline}} process]
    (try (let [args (if pipe
                      input-args
                      (conj input-args input-body))
               proc (os/sh (merge shell
                                  {:wait false
                                   :args args
                                   :root root}))
               _    (cond-> proc
                      pipe  (doto (os/sh-write input-body) (os/sh-close))
                      :then (os/sh-wait))
               {:keys [err out exit] :as ret} (os/sh-output proc)]
           (cond raw
                 (let [out-lines (->> (clojure.string/split-lines (trim out))
                                      (remove empty?)
                                      seq)
                       err-lines (->> (clojure.string/split-lines (trim err))
                                      (remove empty?)
                                      seq)]
                   [exit (or out-lines err-lines [])])

                 :else
                 (trim out)))
         (catch Throwable t
           (if stderr
             (trim (.getMessage t))
             (throw t))))))

(def +gdscript-twostep-config+
  (common/set-context-options
   [:gdscript :twostep :default]
   {:main  {:in    #'default-oneshot-in
            :out   #'default-oneshot-out}
    :emit  {:body  {:transform #'default-body-transform}}
    :root  +gdscript-runtime-dir+
    :pipe  false
    :exec-fn #'sh-exec-gdscript
    :json :full}))

(def +gdscript-twostep+
  [(rt/install-type!
    :gdscript :twostep
    {:type :hara/rt.twostep
     :instance {:create #'twostep/rt-twostep:create}})])

