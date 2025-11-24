(ns code.doc.link.chapter-test
  (:use code.test)
  (:require [code.doc.link.chapter :refer :all]))

^{:refer code.doc.link.chapter/link-chapters :added "3.0"}
(fact "links each chapter to each of the elements"

  (-> (link-chapters {:articles {"doc" {:elements [{:type :api :namespace 'code.core :table {:a 1}}
                                                   {:type :chapter :link 'code.core}]}}}
                     "doc")
      (get-in [:articles "doc" :elements]))
  => [{:type :api, :namespace 'code.core, :table {:a 1}}
      {:type :chapter, :link 'code.core, :table {:a 1}}])
