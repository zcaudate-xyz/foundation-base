(ns code.doc.link.manage-test
  (:use code.test)
  (:require [code.doc.link.manage :refer :all]
            [code.manage :as manage]))

(fact "format-manage-output"
  (format-manage-output {:a 1} :edn) => "{:a 1}\n"
  (format-manage-output {:a 1} str) => "{:a 1}"
  (format-manage-output {:a 1} 'str) => "{:a 1}"
  (format-manage-output {:a 1} nil) => "{:a 1}")

(fact "run-manage-task"
  (run-manage-task 'missing [['code.doc] {:return :summary}] :edn)
  => string?

  (run-manage-task 'missing [['code.doc] {:return :summary}] nil)
  => string?

  (run-manage-task 'invalid-task [] nil)
  => "Task not found: invalid-task")

(fact "link-manage"
  (let [interim {:articles {:test {:elements [{:type :manage
                                               :task 'missing
                                               :args [['code.doc] {:print {:result false :summary false} :return :summary}]}]}}}
        result (link-manage interim :test)
        element (-> result :articles :test :elements first)]

    (:type element) => :block
    (:code element) => string?
    (:code element) => (contains "MISSING TESTS")))

(fact "link-manage with formatter"
  (let [interim {:articles {:test {:elements [{:type :manage
                                               :task 'missing
                                               :args [['code.doc] {:return :summary}]
                                               :formatter :edn}]}}}
        result (link-manage interim :test)
        element (-> result :articles :test :elements first)]

    (:type element) => :block
    (:code element) => string?
    (:code element) => (contains "{:errors 0,")))
