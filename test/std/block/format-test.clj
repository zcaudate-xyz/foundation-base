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
(fact "formats code string using rules - basic indentation and line break"
  (let [rules [(format/rule 'my-symbol {:indent-level 2 :line-break-after true})]
        code-str "(my-symbol arg1 arg2)"]
    (format/format-code code-str rules))
  => "(  my-symbol\n arg1 arg2)")

(fact "formats code string using rules - space before and after"
  (let [rules [(format/rule 'my-symbol {:space-before true :space-after true})]
        code-str "(prefix my-symbol suffix)"]
    (format/format-code code-str rules))
  => "(prefix  my-symbol  suffix)")

(fact "formats code string with multiple rules"
  (let [rules [(format/rule '(list 'my-symbol & _) {:indent-level 2 :line-break-after true})
               (format/rule 'arg1 {:indent-level 4 :space-before true})]
        code-str "(my-symbol arg1 arg2)"]
    (format/format-code code-str rules))
  => "(  my-symbol\n    arg1 arg2)")

(fact "formats code string using rules - line wrapping if long"
  (binding [format/*max-line-length* 20]
    (let [rules [(format/rule '(list 'my-symbol & _) {:line-wrap-strategy :if-long})
                 (format/rule 'arg1 {:space-before true})
                 (format/rule 'arg2 {:space-before true})]
          code-str "(my-symbol arg1 arg2)"]
      (format/format-code code-str rules)))
  => "(my-symbol\n arg1\n arg2)") ; This is a very basic expectation, will refine.

^{:refer std.block.format/format :added "4.0"}
(fact "formats code string using default rules"
  (format/format "(defn my-fn [a b] (+ a b))")
  => "(defn my-fn\n  [a b]\n  (+ a b))")
