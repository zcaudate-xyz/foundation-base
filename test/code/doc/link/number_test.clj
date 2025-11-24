(ns code.doc.link.number-test
  (:use code.test)
  (:require [code.doc.link.number :refer :all]))

^{:refer code.doc.link.number/increment :added "3.0"}
(fact "increment string representation"

  (increment 1)
  => "A"

  (increment "A")
  => "B"

  (increment "1")
  => "2")

^{:refer code.doc.link.number/link-numbers-loop :added "3.0"}
(fact "iterates and assigns sequential numbers to elements, used as a helper for `link-numbers`"

  (link-numbers-loop [{:type :chapter}
                      {:type :section}
                      {:type :code :numbered true :title "Code"}]
                     #{:code})
  => [{:type :chapter :number "1"}
      {:type :section :number "1.1"}
      {:type :code :numbered true :title "Code" :number "1.1"}])

^{:refer code.doc.link.number/link-numbers :added "3.0"}
(fact "creates numbers for each of the elements in the list"

  (-> (link-numbers {:articles {"doc" {:elements [{:type :chapter}]}}}
                    "doc")
      (get-in [:articles "doc" :elements]))
  => [{:type :chapter :number "1"}])
