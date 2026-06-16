(ns code.doc.link.chapter-test
  (:require [code.doc.link.chapter :refer :all])
  (:use code.test))

^{:refer code.doc.link.chapter/link-chapters :added "3.0"}
(fact "links each chapter to the api tables contained within it"

  (-> (link-chapters {:articles {"doc" {:elements [{:type :chapter :link 'code.core}
                                                   {:type :api :namespace 'code.core :table {:a 1}}]}}}
                     "doc")
      (get-in [:articles "doc" :elements]))
  => [{:type :chapter, :link 'code.core, :table {:a 1}}
      {:type :api, :namespace 'code.core, :table {:a 1}}]

  (-> (link-chapters {:articles {"doc" {:elements [{:type :api :namespace 'code.core :table {:a 1}}
                                                   {:type :chapter :link 'code.core}]}}}
                     "doc")
      (get-in [:articles "doc" :elements]))
  => [{:type :api, :namespace 'code.core, :table {:a 1}}
      {:type :chapter, :link 'code.core}])
