(ns test.std.block.format-test
  (:use code.test)
  (:require [std.block.format :as format]
            [std.block.base :as base]
            [std.block.construct :as construct]
            [code.query.block :as nav]
            [clojure.string :as str]))

^{:refer std.block.format/rule :added "4.0"}
(fact "creates a formatting rule"
  (let [r (format/rule 'my-pattern {:indent-level 1})]
    (:match r) => 'my-pattern
    (:directives r) => {:indent-level 1}))

^{:refer std.block.format/format-code :added "4.0"}
(fact "formats code string using rules"
  (let [rules [(format/rule 'my-symbol {:indent-level 2 :line-break-after true})]
        code-str "(my-symbol arg1 arg2)"]
    (format/format-code code-str rules))
  => "(  my-symbol\n arg1 arg2)") ; This is a very basic expectation, will refine.

(fact "formats code string with multiple rules"
  (let [rules [(format/rule '(list 'my-symbol & _) {:indent-level 2 :line-break-after true})
               (format/rule 'arg1 {:indent-level 4})]
        code-str "(my-symbol arg1 arg2)"]
    (format/format-code code-str rules))
  => "(  my-symbol\n    arg1 arg2)")
