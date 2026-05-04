(ns hara.runtime.jep.bootstrap
  (:require [clojure.string]
            [std.concurrent :as cc]
            [std.fs :as fs]
            [std.lib.os :as os])
  (:import jep.MainInterpreter jep.Interpreter jep.SharedInterpreter))

(def ^:dynamic *python* (or (System/getenv "JEP_PYTHON") "python3"))

(def ^:dynamic *pip* (or (System/getenv "JEP_PIP") "pip3"))

(defn bootstrap-code
  "creates the bootstrap code"
  {:added "3.0"}
  ([]
   (bootstrap-code {:install true}))
  ([{:keys [install] :or {install true}}]
   (vec
    (concat
     ["import sys"
      "import site "
      "import subprocess"
      "import os"
      "import glob"
      ""
      "if sys.version[0] != '3':"
      "  print(\"Requires Python 3\")"
      "  exit(1)"
      ""
      "def get_sitepackages ():"
      "  out = []"
      "  try:"
      "    out.extend(site.getsitepackages())"
      "  except Exception:"
      "    pass"
      "  try:"
      "    user_site = site.getusersitepackages()"
      "  except Exception:"
      "    user_site = None"
      "  if user_site:"
      "    out.append(user_site)"
      "  return list(dict.fromkeys([path for path in out if path]))"
      ""
      "def check_jep ():"
      "  out = []"
      "  for root in get_sitepackages():"
      "    out.extend(glob.glob(os.path.join(root, \"jep/libjep.*\")))"
      "  return out"
      ""
      "jep_out = check_jep()"
      ""]
     (if install
       ["if len(jep_out) == 0:"
        "  print(\"Jep not present, Installing...\")"
        (format "  subprocess.call(['%s', 'install', 'jep'])" *pip*)
        "  jep_out = check_jep()"
        ""]
       [])
     ["if len(jep_out) == 0:"
      "  print(\"Jep not present\")"
      "  exit(1)"
      ""
      "print(jep_out[0])"]))))

(defn ^String jep-bootstrap
  "returns the jep runtime
 
    (jep-bootstrap)
    => (any string?
            throws)"
  {:added "3.0"}
  ([]
   (jep-bootstrap {:install false}))
  ([opts]
   (let [path (fs/create-tmpfile (clojure.string/join "\n" (bootstrap-code opts)))
           process  (os/sh *python* (str path) {:wait true :output false :inherit false})
           {:keys [exit out err]} (os/sh-output process)]
      (if (zero? exit)
        (last (clojure.string/split-lines (clojure.string/trim out)))
        (let [message (->> [out err]
                           (remove clojure.string/blank?)
                           (clojure.string/join "\n")
                           (clojure.string/trim))]
          (throw (ex-info message
                          {:status :failed
                           :message message
                           :exit exit
                           :out out
                           :err err})))))))

(defn jep-available?
  "checks if the jep python runtime is already available"
  {:added "4.1"}
  ([]
   (try
     (boolean (jep-bootstrap {:install false}))
     (catch Throwable _
       false))))

(defn init-paths
  "sets the path of the jep interpreter"
  {:added "3.0"}
  ([]
    (let [jep  (jep-bootstrap {:install false})
          root (clojure.string/replace jep #"/jep/libjep.*" "")]
      (MainInterpreter/setJepLibraryPath jep)
      (SharedInterpreter/setConfig
       (-> (jep.JepConfig.)
          (.addIncludePaths (into-array [root])))))))

(defonce +init+ (delay (init-paths)))
