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
    (initialise))
  => {:root ".build/code.tool.python-ast"
      :env {"PATH" string?}
      :args ["pip3" "install" "ast2json" "--target" "."]})

^{:refer code.tool.translate.python-ast/translate-ast :added "4.1"}
(fact "builds the helper script and shells out to python"
  (with-redefs [make/build-all (fn [target] target)
                os/sh identity]
    (translate-ast "example.py" "example.json"))
  => {:root ".build/code.tool.python-ast"
      :env {"PYTHONPATH" "."
            "PATH" string?}
      :args ["python3" "index.py" "example.py" "example.json"]})
