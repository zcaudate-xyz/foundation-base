(ns std.print.format.chart-test
  (:require [clojure.string]
            [std.print.format.chart :refer :all]
            [std.string.common :as common])
  (:use code.test))

(defn ascii
  [vs]
  (common/joinl vs "\n"))

^{:refer std.print.format.chart/lines:bar-graph :added "3.0"}
(fact "formats an ascii bar graph for output"

  (lines:bar-graph (range 10) 6)
  => ["    ▟"
      "  ▗██"
      " ▟███"])

^{:refer std.print.format.chart/bar-graph :added "3.0"}
(fact "constructs a bar graph"

  (-> (bar-graph (range 10) 6)
      (clojure.string/split-lines))
  => ["    ▟"
      "  ▗██"
      " ▟███"])

^{:refer std.print.format.chart/sparkline :added "3.0"}
(fact "formats a sparkline"

  (sparkline (range 8))
  => " ▁▂▃▅▆▇█" ^:hidden

  (sparkline [1 3 5 7 3 4 6 8 4 2 4 5 7 10])
  => " ▂▄▆▂▃▅▇▃▁▃▄▆█")

^{:refer std.print.format.chart/tree-graph :added "3.0"}
(fact "returns a string representation of a tree"

  (-> (tree-graph '[{a "1.1"}
                    [{b "1.2"}
                     [{c "1.3"}
                      {d "1.4"}]]])
      (clojure.string/split-lines))
  => ["{a \"1.1\"}"
      " {b \"1.2\"}"
      "  {c \"1.3\"}"
      "  {d \"1.4\"}"
      ""])

^{:refer std.print.format.chart/table-basic:format :added "3.0"}
(fact "generates a table for output"

  (table-basic:format [:id :value]
                      [{:id 1 :value "a"}
                       {:id 2 :value "b"}])

  => (ascii ["| :id | :value |"
             "|-----+--------|"
             "|   1 |    \"a\" |"
             "|   2 |    \"b\" |"]))

^{:refer std.print.format.chart/table-basic:parse :added "3.0"}
(fact "reads a table from a string"

  (table-basic:parse (ascii
                      ["| :id | :value |"
                       "|-----+--------|"
                       "|   1 |    \"a\" |"
                       "|   2 |    \"b\" |"]))
  => {:headers [:id :value]
      :data [{:id 1 :value "a"}
             {:id 2 :value "b"}]})

^{:refer std.print.format.chart/table :added "3.0"}
(fact "generates a single table"

  (table {"a@a.com" {:id 1 :value "a"}
          "b@b.com" {:id 2 :value "b"}}
         {:headers [:id :email :value]
          :sort-key :email
          :id-key :email})
  => (ascii ["| :id |    :email | :value |"
             "|-----+-----------+--------|"
             "|   1 | \"a@a.com\" |    \"a\" |"
             "|   2 | \"b@b.com\" |    \"b\" |"]))

^{:refer std.print.format.chart/table:parse :added "3.0"}
(fact "generates a single table"

  (table:parse
   (ascii ["| :id |    :email | :value |"
           "|-----+-----------+--------|"
           "|   1 | \"a@a.com\" |    \"a\" |"
           "|   2 | \"b@b.com\" |    \"b\" |"])

   {:headers [:id :email :value]
    :sort-key :email
    :id-key :email})
  => {"a@a.com" {:id 1 :value "a"}
      "b@b.com" {:id 2 :value "b"}})
