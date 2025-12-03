(ns rt.jep.bootstrap
  (:require [std.fs :as fs]
            [std.concurrent :as cc]
            [std.string :as str]
            [std.lib :as h])
  (:import jep.MainInterpreter
           jep.Interpreter
           jep.SharedInterpreter))

(def ^:dynamic *python* (or (System/getenv "JEP_PYTHON") "python3"))

(def ^:dynamic *pip* (or (System/getenv "JEP_PIP") "pip3"))

(defn bootstrap-code
  "creates the bootstrap code"
  {:added "3.0"}
  ([]
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
    "def check_jep ():"
    "  return glob.glob(os.path.join(site.getsitepackages()[0], \"jep/libjep.*\"))"
    ""
    "jep_out = check_jep()"
    ""
    "if len(jep_out) == 0:"
    "  print(\"Jep not present, Installing...\")"
    (format "  subprocess.call(['%s', 'install', 'jep'])" *pip*)
    "  jep_out = check_jep()"
    ""
    "print(jep_out[0])"]))

(defn ^String jep-bootstrap
  "returns the jep runtime
 
   (jep-bootstrap)
   => (any string?
           throws)"
  {:added "3.0"}
  ([]
   (let [path (fs/create-tmpfile (str/join "\n" (bootstrap-code)))
         process  (h/sh *python* (str path) {:wait true :output false :inherit false})
         {:keys [exit out err]} (h/sh-output process)]
     (if (zero? exit)
       (last (str/split-lines (str/trim out)))
       (throw (ex-info out {:status :failed :message out}))))))

(defn init-paths
  "sets the path of the jep interpreter"
  {:added "3.0"}
  ([]
   (let [jep  (jep-bootstrap)
         root (str/replace jep #"/jep/libjep.*" "")]
     (MainInterpreter/setJepLibraryPath jep)
     (SharedInterpreter/setConfig
      (-> (jep.JepConfig.)
          (.addIncludePaths (into-array [root])))))))

(defonce +init+ (delay (init-paths)))
