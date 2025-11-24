(ns code.doc.link.anchor-test
  (:use code.test)
  (:require [code.doc.link.anchor :refer :all]))

^{:refer code.doc.link.anchor/link-anchors-lu :added "3.0"}
(fact "adds anchor lookup table by name"

  (link-anchors-lu {:articles {"doc" {:elements [{:tag "a1" :type :chapter :number 1}
                                                 {:tag "a2" :type :section :number "1.1"}]}}}
                   "doc")
  => (contains {:anchors-lu
                {"doc" {:by-tag {"a1" {:type :chapter, :tag "a1", :number 1},
                                 "a2" {:type :section, :tag "a2", :number "1.1"}},
                        :by-number {:chapter {1 {:type :chapter, :tag "a1", :number 1}},
                                    :section {"1.1" {:type :section, :tag "a2", :number "1.1"}}}}}}))

^{:refer code.doc.link.anchor/link-anchors :added "3.0"}
(fact "add anchors to the bundle"

  (-> (link-anchors {:anchors-lu
                     {"code.doc" {:by-tag {:a 1
                                           :b 2}}}}
                    "code.doc")
      :anchors)
  => {"code.doc" {:a 1, :b 2}})
