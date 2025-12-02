(ns code.framework.text-test
  (:use code.test)
  (:require [code.framework.text :refer :all]))

^{:refer code.framework.text/summarise-deltas :added "3.0"}
(fact "summary of what changes have been made"
  (summarise-deltas [{:functions #{'a}
                      :original {:count 1}
                      :revised {:count 2}}])
  => {:deletes 1, :inserts 2, :changed ['a]})

^{:refer code.framework.text/deltas :added "3.0"}
(fact "returns a list of changes between the original and revised versions"
  (deltas 'ns {} "a" "b")
  => (contains [{:original {:position 0, :count 1, :lines ["a"]},
                 :revised {:position 0, :count 1, :lines ["b"]},
                 :type "CHANGE",
                 :label nil,
                 :functions #{}}]))

^{:refer code.framework.text/highlight-lines :added "3.0"}
(fact "highlights a segment of code in a different color"
  (highlight-lines ["foo"] {:row 1 :col 1 :end-row 1 :end-col 4} 1
                   {:text {:normal :cyan :highlight :yellow}})
  => ["\u001b[33mfoo\u001b[36m"])

^{:refer code.framework.text/->string :added "3.0"}
(fact "turns a set of results into a output string for printing"
  (->string [{:row 1 :col 1 :end-row 1 :end-col 4}]
            ["foo"]
            (constantly 'a)
            {})
  => string?)

(comment
  (code.manage/import {:write true}))
