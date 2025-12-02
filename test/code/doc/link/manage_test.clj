(ns code.doc.link.manage-test
  (:use code.test)
  (:require [code.doc.link.manage :refer :all]
            [code.manage :as manage]))

(fact "link-manage"
  (let [interim {:articles {:test {:elements [{:type :manage
                                               :task 'missing
                                               :args [['code.doc] {:print {:result false :summary false} :return :summary}]}]}}}
        result (link-manage interim :test)
        element (-> result :articles :test :elements first)]

    (:type element) => :block
    (:code element) => string?
    (:code element) => (contains "MISSING TESTS")))
