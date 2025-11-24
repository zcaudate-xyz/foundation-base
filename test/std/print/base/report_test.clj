(ns std.print.base.report-test
  (:use code.test)
  (:require [std.print.base.report :refer :all]
            [std.concurrent.print :as print]
            [std.string :as str]
            [std.lib :as h]
            [std.lib.result :as res]))

^{:refer std.print.base.report/print-header :added "3.0"}
(fact "prints a header for the row"

  (-> (print-header [:id :name :value]
                    {:padding 0
                     :spacing 1
                     :columns [{:align :right :length 10}
                               {:align :center :length 10}
                               {:align :left :length 10}]})
      (h/with-out-str)) ^:hidden
  => "[1m        id   name   value     [0m\n\n")

^{:refer std.print.base.report/print-title :added "3.0" :tags #{:print}}
(fact "prints the title"

  (-> (print-title "Hello World")
      (h/with-out-str)) ^:hidden
  => "[1m\n-----------\nHello World\n-----------[0m\n")

^{:refer std.print.base.report/print-subtitle :added "3.0"}
(fact "prints the subtitle"

  (-> (print-subtitle "Hello Again")
      (h/with-out-str)) ^:hidden
  => "[1mHello Again[0m\n")

^{:refer std.print.base.report/print-row :added "3.0" :tags #{:print}}
(fact "prints a row to output"
  ^:hidden

  (-> (print-row ["hello" :world (res/result {:data [:a :b :c :d :e :f]
                                              :status :info})]
                 {:padding 0
                  :spacing 1
                  :columns [{:align :right :length 10}
                            {:align :center :length 10}
                            {:align :left :length 10}]})
      (h/with-out-str))
  => "     hello   :world   [34m[:a :b :c [0m\n                      [34m :d :e :f][0m\n")

^{:refer std.print.base.report/print-column :added "3.0"}
(fact "prints the column"

  (-> (print-column [[:id.a {:data 100}] [:id.b {:data 200}]]
                    :data
                    #{})
      (h/with-out-str)) ^:hidden
  => string?)

^{:refer std.print.base.report/print-summary :added "3.0" :tags #{:print}}
(fact "outputs the summary of results"

  (-> (print-summary {:count 6 :files 2})
      (h/with-out-str)) ^:hidden
  ;; SUMMARY {:count 6, :files 2}
  => "[1mSUMMARY {:count 6, :files 2}[0m\n")

^{:refer std.print.base.report/print-tree-graph :added "3.0"}
(fact "outputs the result of `format-tree`"

  (-> (print-tree-graph '[{a "1.1"}
                          [{b "1.2"}
                           [{c "1.3"}
                            {d "1.4"}]]])
      (h/with-out-str))
  => string?)

(comment
  (./code:scaffold)
  (./code:import)
  (./code:arrange)
  (./code:scaffold))
