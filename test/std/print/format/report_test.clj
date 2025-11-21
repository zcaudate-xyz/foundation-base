(ns std.print.format.report-test
  (:use code.test)
  (:require [std.print.format.report :refer :all]
            [std.string :as str]
            [std.lib :as h]
            [std.lib.result :as res]))

^{:refer std.print.format.report/lines:elements :added "3.0"}
(fact "layout an array of elements as a series of rows of a given length"

  (lines:elements ["A" "BC" "DEF" "GHIJ" "KLMNO"]
                  {:align :left
                   :length 9}
                  0
                  1)
  => ["[A BC DEF"
      " GHIJ    "
      " KLMNO]  "])

^{:refer std.print.format.report/lines:row-basic :added "3.0"}
(fact "layout raw elements based on alignment and length properties"

  (lines:row-basic
   ["hello" :world [:a :b :c :d :e :f]]
   {:padding 0
    :spacing 1
    :columns [{:align :right :length 10}
              {:align :center :length 10}
              {:align :left :length 10}]})
  => [["     hello"] ["  :world  "] ["[:a :b :c "
                                     " :d :e :f]"]])

^{:refer std.print.format.report/lines:row :added "3.0"}
(fact "same as row-elements but allows for colors and results"

  (lines:row ["hello" :world [:a :b :c :d]]
             {:padding 0
              :spacing 1
              :columns [{:align :right :length 10}
                        {:align :center :length 10}
                        {:align :left :length 10}]})

  => [["     hello" "          "]
      ["  :world  " "          "]
      ["[:a :b :c " " :d]      "]]
  
  (lines:row ["hello" :world (res/result {:data [:a :b :c :d :e :f]
                                          :status :info})]
             {:padding 0
              :spacing 1
              :columns [{:align :right :length 10}
                        {:align :center :length 10}
                        {:align :left :length 10}]})
  => '[("     hello"
        "          ")
       ("  :world  "
        "          ")
       ("[34m[:a :b :c [0m"
        "[34m :d :e :f][0m")])

^{:refer std.print.format.report/report:header :added "3.0"}
(fact "prints a header for the row"

  (report:header [:id :name :value]
                 {:padding 0
                  :spacing 1
                  :columns [{:align :right :length 10}
                            {:align :center :length 10}
                            {:align :left :length 10}]}) ^:hidden
  => "[1m        id   name   value     [0m")

^{:refer std.print.format.report/report:row :added "3.0" :tags #{:print}}
(fact "prints a row to output"
  
  (report:row ["hello" :world (res/result {:data [:a :b :c :d :e :f]
                                           :status :info})]
              {:padding 0
               :spacing 1
               :columns [{:align :right :length 10}
                         {:align :center :length 10}
                         {:align :left :length 10}]})
  => "     hello   :world   [34m[:a :b :c [0m\n                      [34m :d :e :f][0m")

^{:refer std.print.format.report/report:title :added "3.0" :tags #{:print}}
(fact "prints the title"

  (report:title "Hello World") ^:hidden
  => "[1m\n-----------\nHello World\n-----------[0m")

^{:refer std.print.format.report/report:bold :added "3.0"}
(fact "prints the subtitle"

  (report:bold "Hello Again") ^:hidden
  => "[1mHello Again[0m")

^{:refer std.print.format.report/report:column :added "3.0"}
(fact "prints the column"

  (report:column [[:id.a {:data 100}] [:id.b {:data 200}]]
                 :data
                 #{}) ^:hidden
  => string?)
