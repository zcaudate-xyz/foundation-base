(ns code.doc.link.manage-test
  (:require [code.doc.link.manage :refer :all]
            [code.manage :as manage])
  (:use code.test))

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
(fact "supports custom formatters"
  (format-manage-output {:value 1} (fn [{:keys [value]}] (str "value=" value)))
  => "value=1"

  (format-manage-output {:value 1} 'pr-str)
  => "{:value 1}")

^{:refer code.doc.link.manage/run-manage-task :added "4.1"}
(fact "resolves string tasks and merges formatter print options"
  (with-redefs [manage/missing (fn [& args] (last args))]
    (-> (run-manage-task "missing"
                         [['code.doc] {:return :summary
                                       :print {:summary true}}]
                         'pr-str)
        read-string))
  => {:return :summary
      :print {:summary false
              :result false
              :item false}})

^{:refer code.doc.link.manage/link-manage :added "4.1"}
(fact "replaces only manage elements with rendered blocks"
  (with-redefs [run-manage-task (fn [task args formatter]
                                  (str task "|" formatter "|" (count args)))]
    (-> {:articles {:demo {:elements [{:type :manage
                                       :task 'missing
                                       :args [1 2]
                                       :formatter :edn}
                                      {:type :text
                                       :code "keep"}]}}}
        (link-manage :demo)
        (get-in [:articles :demo :elements])))
  => [{:type :block :code "missing|:edn|2"}
      {:type :text :code "keep"}])
