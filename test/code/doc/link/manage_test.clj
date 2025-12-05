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
  => (throws clojure.lang.ExceptionInfo "Task not found: invalid-task"))

(fact "link-manage"
  (let [interim {:articles {:test {:elements [{:type :manage
                                               :task 'missing
                                               :args [['code.doc] {:print {:result false :summary false} :return :summary}]}]}}}
        result (link-manage interim :test)
        element (-> result :articles :test :elements first)]

    (:type element) => :block
    (:code element) => string?
    (:code element) => ""))

(fact "link-manage with formatter"
  (let [interim {:articles {:test {:elements [{:type :manage
                                               :task 'missing
                                               :args [['code.doc] {:return :summary}]
                                               :formatter :edn}]}}}
        result (link-manage interim :test)
        element (-> result :articles :test :elements first)]

    (:type element) => :block
    (:code element) => string?
    (:code element) => #"\{:errors 0,"))


^{:refer code.doc.link.manage/format-manage-output :added "4.1"}
(fact "TODO")

^{:refer code.doc.link.manage/run-manage-task :added "4.1"}
(fact "TODO")

^{:refer code.doc.link.manage/link-manage :added "4.1"}
(fact "TODO")