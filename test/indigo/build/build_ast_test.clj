(ns indigo.build.build-ast-test
  (:require [indigo.build.build-ast :as build-ast]
            [std.lib :as h]
            [std.fs :as fs]
            [std.json :as json]
            [std.make :as make]
            [code.test :refer [fact]]))

(fact "build-ast workflow"
  (do
    ;; Setup - Ensure build files exist
    (make/build-all build-ast/BUILD_AST)

    ;; Install dependencies (idempotent-ish)
    (build-ast/initialise)

    (def +ts-file+ "test/scratch/test_ast.ts")
    (def +json-file+ "test/scratch/test_ast.json")

    (fs/create-directory "test/scratch")
    (fs/write-file +ts-file+ "const x: number = 1;")

    ;; Run - paths relative to .build/code-dev-build-ast/
    ;; So ../../test/scratch/...
    (def +input-rel+ (str "../../" +ts-file+))
    (def +output-rel+ (str "../../" +json-file+))

    (build-ast/generate-ast +input-rel+ +output-rel+)

    ;; Verify
    (fact "output file exists and is valid json"
      (fs/exists? +json-file+) => true
      (def res (json/read +json-file+))
      (:type res) => "File"
      (get-in res [:program :body 0 :type]) => "VariableDeclaration")

    ;; Cleanup
    (fs/delete +ts-file+)
    (fs/delete +json-file+)))


^{:refer indigo.build.build-ast/initialise :added "4.0"}
(fact "TODO")

^{:refer indigo.build.build-ast/generate-ast :added "4.0"}
(fact "TODO")