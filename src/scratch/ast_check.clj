(ns scratch.ast-check
  (:require [indigo.build.build-ast :as build-ast]
            [std.fs :as fs]
            [std.json :as json]
            [std.make :as make]))

(defn check []
  (make/build-all build-ast/BUILD_AST)
  (build-ast/initialise)
  (let [code "function hello(x) { if (x) { return x + 1; } }"
        tmp-in (fs/create-tmpfile code)
        tmp-out (fs/create-tmpfile)]
    (build-ast/generate-ast (str tmp-in) (str tmp-out))
    (let [ast (json/read tmp-out)]
      (println "AST Keys:" (keys ast))
      (println "Program Body:" (first (get-in ast ["program" "body"])))
      ast)))
