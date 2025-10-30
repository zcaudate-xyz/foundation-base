(ns code.framework.test.fact-test
  (:use code.test)
  (:require [code.framework.test.fact :refer :all]
            [code.framework.docstring :as docstring]
            [code.query.block :as nav]
            [std.lib :as h]))

^{:refer code.framework.test.fact/gather-fact-body :added "3.0"}
(fact "extracts and formats the body of a `fact` form into docstring notation, used as a helper for `gather-fact`"
  (-> "(\n  (+ 1 1) => 2\n  (long? 3) => true)"
      nav/parse-string
      nav/down
      (gather-fact-body)
      (docstring/->docstring))
  => "\n  (+ 1 1) => 2\n  (long? 3) => true")

^{:refer code.framework.test.fact/gather-fact :added "3.0"}
(fact "Make docstring notation out of fact form"
  (-> "^{:refer example/hello-world :added \"0.1\"}
       (fact \"Sample test program\"\n  (+ 1 1) => 2\n  (long? 3) => true)"
      (nav/parse-string)
      nav/down nav/right nav/down nav/right
      (gather-fact)
      (update-in [:test] docstring/->docstring))
  => (just-in {:form  'fact
               :ns    'example,
               :var   'hello-world,
               :refer 'example/hello-world
               :added "0.1",
               :line  {:row 2, :col 8, :end-row 4, :end-col 21}
               :intro "Sample test program",
               :sexp h/form?
               :test  "\n  (+ 1 1) => 2\n  (long? 3) => true"}))
