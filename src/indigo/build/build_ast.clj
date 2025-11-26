(ns indigo.build.build-ast
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [std.fs :as fs]))

(def INDEXJS
  (l/emit-as
   :js
   '[(def fs (require "fs"))
     (def parser (require "@babel/parser"))

     (defn main []
       (var inputFile (. (. process argv) [2]))
       (var outputFile (. (. process argv) [3]))

       (if (not inputFile)
         (do (console.error "Usage: node index.js <input_file> [output_file]")
             (process.exit 1)))

       (try
         (do
           (var code (. fs (readFileSync inputFile "utf-8")))
           (var ast (. parser (parse code
                                     {:sourceType "module"
                                      :plugins ["typescript" "jsx"]})))
           (if outputFile
             (. fs (writeFileSync outputFile (JSON.stringify ast nil 2)))
             (console.log (JSON.stringify ast nil 2))))
         (catch error
           (do
             (console.error (+ "Error parsing file: " (. error message)))
             (process.exit 1)))))

     (main)]))

(def.make BUILD_AST
  {:tag       "indigo.build-ast"
   :build     ".build/code-dev-build-ast"
   :hooks    {}
   :default  [{:type   :package.json
               :main   {"name" "indigo.build-ast",
                        "version" "1.0.0",
                        "description" "Utility to parse TypeScript files to AST",
                        "main" "index.js",
                        "dependencies" {"@babel/parser" "^7.24.0"}}}
              {:type :raw
               :file "index.js"
               :main [INDEXJS]}]})

(defn initialise
  []
  (h/p (h/sh {:root ".build/code-dev-build-ast"
              :args ["npm" "install"]})))

(defn generate-ast
  ([input-file]
   (generate-ast input-file nil))
  ([input-file output-file]
   (make/build-all BUILD_AST) ;; Ensure build is ready
   (let [args (cond-> ["node" "index.js" input-file]
                output-file (conj output-file))]
     (h/sh {:root ".build/code-dev-build-ast"
            :args args}))))
