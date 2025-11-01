;; test/code/gen/example_test.clj
(ns test.code.gen.example-test
  (:use code.test)
  (:require [code.gen.example :as example] ; Import the example namespace
            [std.string :as str]))

^{:refer example/preprocess-greeter-bindings :added "4.0"}
(fact "correctly preprocesses bindings for greeter function"
  (let [bindings {:args '[first-name last-name]}
          processed (example/preprocess-greeter-bindings bindings)]
    processed => {:args '[first-name last-name]
                  :doc-extra "[Preprocessed args: first-name, last-name]"}))

^{:refer code.gen.example/generated-code-list :added "4.0"}
(fact "generates hello-world code correctly"
  (nth example/generated-code-list 0)
  => (str "(ns my.generated.core\n  (:require\n    [clojure.string :as str]\n    [std.lib :as h]))\n\n;;;\n;;; This file is a template.\n;;; It will be used to generate a function.\n;;;\n\n(defn hello-world\n  \"Greets a person by name. [Preprocessed args: first-name, last-name]\"\n  [first-name\n   last-name]\n  (comment \"This comment from the template is preserved!\")\n\n  ;; The function body will be spliced in below\n  (let [full-name (str first-name \" \" last-name)]\n    (h/pl (str \"Hello, \" full-name \"!\"))))
