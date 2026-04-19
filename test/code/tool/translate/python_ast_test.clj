(ns code.tool.translate.python-ast-test
  (:require [code.tool.translate.python-ast :refer :all]
            [std.lib.env :as env]
            [std.lib.os :as os]
            [std.make :as make])
  (:use code.test))

^{:refer code.tool.translate.python-ast/initialise :added "4.1"}
(fact "builds the helper script and installs ast2json"
  (with-redefs [make/build-all (fn [target] target)
                os/sh identity
                env/p identity]
    (let [result (initialise)]
      [(:root result)
       (string? (get-in result [:env "PATH"]))
       (:args result)]))
  => [".build/code.tool.python-ast"
      true
      ["pip3" "install" "ast2json" "--target" "."]])

^{:refer code.tool.translate.python-ast/translate-ast :added "4.1"}
(fact "builds the helper script and shells out to python"
  (with-redefs [make/build-all (fn [target] target)
                os/sh identity]
    (let [result (translate-ast "example.py" "example.json")]
      [(:root result)
       (get-in result [:env "PYTHONPATH"])
       (string? (get-in result [:env "PATH"]))
       (:args result)]))
  => [".build/code.tool.python-ast"
      "."
      true
      ["python3" "index.py" "example.py" "example.json"]])
