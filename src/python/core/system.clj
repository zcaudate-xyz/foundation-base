(ns python.core.system
  (:require [std.lang :as l]
            [std.lib.template]))

(l/script :python
  python.core
  {})

(def$.py ^{:arglists '([name])} __import__ __import__)

(defmacro.py thread
  "creates a thread"
  {:added "4.0"}
  [f]
  (std.lib.template/$ (. (python.core/pkg "threading")
          (Thread :target ~f))))

(defmacro.py thread:run
  "runs a thread"
  {:added "4.0"}
  [f]
  (std.lib.template/$ (. (python.core/thread ~f)
          (start))))


(defmacro.py pkg-load
  "loads a package using __loader__"
  {:added "4.0"}
  [name]
  (std.lib.template/$ (. (python.core/__import__ ~name)
          __loader__
          (load_module))))

(defmacro.py pkg
  "loads a package using importlib"
  {:added "4.0"}
  [name]
  (std.lib.template/$ (. (__import__ "importlib")
          (import_module ~name))))

(defmacro.py pkg:dir
  "returns a listing of package members"
  {:added "4.0"}
  [name]
  (std.lib.template/$ (dir (python.core/pkg ~name))))

(defmacro.py sys:version-info
  "gets the system version"
  {:added "4.0"}
  []
  (std.lib.template/$ (. (python.core/pkg "sys") version-info)))

(defmacro.py sys:path
  "gets the system path"
  {:added "4.0"}
  []
  (std.lib.template/$ (. (python.core/pkg "sys") path)))

(defmacro.py site:packages
  "list all site packages"
  {:added "4.0"}
  []
  (std.lib.template/$ (-> (map (fn [e] e.project_name)
                (. (__import__ "pkg_resources")
                   working_set))
           (sorted))))

(defmacro.py site:builtins
  "list all builtin modules"
  {:added "4.0"}
  []
  (std.lib.template/$ (. (python.core/pkg "sys") builtin_module_names)))


(defmacro.py site:install
  "installs a package with pip"
  {:added "4.0"}
  [name]
  (std.lib.template/$ (python.core/thread:run
        (fn [] (. (python.core/pkg "pip._internal")
                  (main ["install" ~name]))))))

(defmacro.py site:uninstall
  "uninstalls a package with pip"
  {:added "4.0"}
  [name]
  (std.lib.template/$ (python.core/thread:run
        (fn [] (. (python.core/pkg "pip._internal")
                  (main ["uninstall" ~name]))))))

(defmacro.py site:list-all
  "lists all site packages"
  {:added "4.0"}
  []
  '(sorted (. (python.core/pkg "sys") modules (keys))))

(defmacro.py site:list-toplevel
  "lists alll top level packages"
  {:added "4.0"}
  []
  (std.lib.template/$ (->> (. (python.core/pkg "sys") modules (keys))
            (map (fn [k] (. k (split ".") [0])))
            (set)
            (sorted))))

(defmacro.py fn:signature
  "returns the signature of a function"
  {:added "4.0"}
  [obj]
  (std.lib.template/$ (. (python.core/pkg "inspect")
          (signature ~obj))))

(defmacro.py fn:argspec
  "gets the argspect of a function"
  {:added "4.0"}
  [obj]
  (std.lib.template/$ (. (python.core/pkg "inspect")
          (getfullargspec ~obj))))

(defmacro.py js:dumps
  "converts python to json"
  {:added "4.0"}
  [obj]
  (std.lib.template/$ (. (python.core/pkg "json")
          (dumps ~obj))))

(defmacro.py js:loads
  "loads python from json"
  {:added "4.0"}
  [s]
  (std.lib.template/$ (. (python.core/pkg "json")
          (loads ~s))))

(defmacro.py uuid
  "returns a uuid"
  {:added "4.0"}
  [& [version]]
  (let [version (or (get #{1 4}
                         version)
                    4)
        sym  (symbol (str "uuid" version))]
    (std.lib.template/$ (str (. (python.core/pkg "uuid")
                 (~sym))))))

(defmacro.py g:out
  "gets the current global output"
  {:added "4.0"}
  []
  '(. (globals) ["OUT"]))
