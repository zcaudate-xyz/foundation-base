(ns code.tool.translate.python-ast
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [std.fs :as fs]))

(def +root-dir+
  ".build/code.tool.python-ast")

(def INDEXPY
  (l/emit-as
   :python
   '[(:- :import sys)
     (:- :import json)
     (:- :import ast)
     (:- :from ast2json :import ast2json)

     (defn fix-type [node]
       (if (isinstance node dict)
         (do
           (if (. node (__contains__ "_type"))
             (do
               (:= (. node ["type"]) (. node ["_type"]))
               (del (. node ["_type"])))
             (pass))
           (for [key :in (. node (keys))]
             (fix-type (. node [key]))))
         (if (isinstance node list)
           (for [item :in node]
             (fix-type item))
           (pass)))
       (return node))

     (defn main []
       (try
         (:= inputFile (. sys.argv [1]))
         (var outputFile nil)
         (if (> (len sys.argv) 2)
           (:= outputFile (. sys.argv [2]))
           (pass))

         (catch [IndexError]
           (print "Usage: python index.py <input_file> [output_file]")
           (sys.exit 1)))

       (try
         (with [(open inputFile "r") :as f]
           (:= code (. f (read))))

         (:= node (. ast (parse code)))
         (:= json-ast (ast2json node))
         (fix-type json-ast)

         (if outputFile
           (with [(open outputFile "w") :as f]
             (. json (dump json-ast f {:indent 2})))
           (print (. json (dumps json-ast {:indent 2}))))

         (catch [Exception :as e]
           (print (+ "Error parsing file: " (str e)) {:file sys.stderr})
           (sys.exit 1))))

     (if (== __name__ "__main__")
       (main)
       (pass))]))

(def.make PYTHON_AST
  {:tag       "code.tool.python-ast"
   :build     +root-dir+
   :hooks     {}
   :default   [{:type :raw
                :file "index.py"
                :main [INDEXPY]}]})

(defn initialise
  []
  (make/build-all PYTHON_AST)
  (h/p (h/sh {:root +root-dir+
              :env {"PATH" (System/getenv "PATH")}
              :args ["pip3" "install" "ast2json" "--target" "."]})))

(defn translate-ast
  ([input-file]
   (translate-ast input-file nil))
  ([input-file output-file]
   (make/build-all PYTHON_AST) ;; Ensure build is ready
   (let [args (cond-> ["python3" "index.py" input-file]
                output-file (conj output-file))]
     (h/sh {:root +root-dir+
            :env {"PYTHONPATH" "."
                  "PATH" (System/getenv "PATH")}
            :args args}))))
