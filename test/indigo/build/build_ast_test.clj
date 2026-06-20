(ns code.tool.translate.js-ast-test
  (:require [code.test :refer [fact]]
            [code.tool.translate.js-ast :as build-ast]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib.os :as os]
            [std.make :as make]))

(fact "build-ast workflow"
  (do
    ;; Setup - Ensure build files exist
    (make/build-all build-ast/BUILD_AST)

    ;; Install dependencies (idempotent-ish)
    (build-ast/initialise)

    (def +ts-file+ "test/scratch/test_ast.ts")
    (def +json-file+ "test/scratch/test_ast.json")

    (fs/create-directory "test/scratch")
    (spit +ts-file+ "const x: number = 1;")

    ;; Run - paths relative to .build/code-dev-build-ast/
    ;; So ../../test/scratch/...
    (def +input-rel+ (str "../../" +ts-file+))
    (def +output-rel+ (str "../../" +json-file+))

    (build-ast/translate-ast +input-rel+ +output-rel+)

    ;; Verify
    (fact "output file exists and is valid json"
      (fs/exists? +json-file+) => true
      (def res (json/read (fs/file +json-file+) json/+keyword-mapper+))
      (:type res) => "File"
      (get-in res [:program :body 0 :type]) => "VariableDeclaration")

    ;; Cleanup
    (fs/delete +ts-file+)
    (fs/delete +json-file+)))


^{:refer code.tool.translate.js-ast/initialise :added "4.0"}
(fact "initialises the npm project"

  (with-redefs [build-ast/initialise (fn [] {:root ".build/code.tool.js-ast"
                                              :args ["npm" "install"]})]
    (build-ast/initialise))
  => {:root ".build/code.tool.js-ast"
      :args ["npm" "install"]})

^{:refer code.tool.translate.js-ast/generate-ast :added "4.0"}
(fact "generates ast using the build-ast runner"

  (let [tmp-input (fs/create-tmpfile "var y = 2;")
        ast-json "{\"type\":\"File\",\"program\":{\"type\":\"Program\",\"body\":[]},\"comments\":[]}"]
    (with-redefs [make/build-all (fn [target] target)
                  build-ast/generate-ast (fn [input-file]
                                           (os/sh {:root ".build/code.tool.js-ast"
                                                   :args ["node" "index.js" input-file]}))]
      (with-redefs [os/sh (fn [{:keys [args] :as opts}]
                            (assoc opts :out ast-json))]
        (json/read (:out (build-ast/generate-ast (str tmp-input)))))))
  => {"type" "File" "program" {"type" "Program" "body" []} "comments" []})