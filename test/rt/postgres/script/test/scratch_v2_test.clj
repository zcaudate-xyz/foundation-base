(ns rt.postgres.script.test.scratch-v2-test
  (:require [rt.postgres.grammar.typed-parse :as parse]
            [rt.postgres.script.test.scratch-v2 :refer :all])
  (:use code.test))

(defn scratch-analysis
  []
  (parse/analyze-namespace 'rt.postgres.script.test.scratch-v2))

^{:refer rt.postgres.script.test.scratch-v2/as-array :added "4.1"}
(fact "as-array is parsed as a jsonb helper function"
  (let [fn-def (some #(when (= "as-array" (:name %)) %)
                     (:functions (scratch-analysis)))]
    [(:name fn-def) (:output fn-def)])
  => ["as-array" [:jsonb]])

^{:refer rt.postgres.script.test.scratch-v2/TaskCache :added "4.1"}
(fact "TaskCache is parsed as a table with an id primary key"
  (let [table (some #(when (= "TaskCache" (:name %)) %)
                    (:tables (scratch-analysis)))]
    [(:name table) (:primary-key table)])
  => ["TaskCache" :id])

^{:refer rt.postgres.script.test.scratch-v2/Task :added "4.1"}
(fact "Task is parsed with its expected columns"
  (let [table (some #(when (= "Task" (:name %)) %)
                    (:tables (scratch-analysis)))]
    (mapv :name (:columns table)))
  => [:status :name :cache :detail])

^{:refer rt.postgres.script.test.scratch-v2/Entry :added "4.1"}
(fact "Entry is parsed with name and tags columns"
  (let [table (some #(when (= "Entry" (:name %)) %)
                    (:tables (scratch-analysis)))]
    (mapv :name (:columns table)))
  => [:name :tags])

^{:refer rt.postgres.script.test.scratch-v2/insert-entry :added "4.1"}
(fact "insert-entry keeps its jsonb output declaration"
  (let [fn-def (some #(when (= "insert-entry" (:name %)) %)
                     (:functions (scratch-analysis)))]
    [(:name fn-def) (:output fn-def)])
  => ["insert-entry" [:jsonb]])

^{:refer rt.postgres.script.test.scratch-v2/update-entry-tags :added "4.1"}
(fact "update-entry-tags is present in the parsed function list"
  (some #(when (= "update-entry-tags" (:name %)) (:name %))
        (:functions (scratch-analysis)))
  => "update-entry-tags")

^{:refer rt.postgres.script.test.scratch-v2/insert-task-fields :added "4.1"}
(fact "insert-task-fields is present in the parsed function list"
  (some #(when (= "insert-task-fields" (:name %)) (:name %))
        (:functions (scratch-analysis)))
  => "insert-task-fields")

^{:refer rt.postgres.script.test.scratch-v2/insert-task-raw :added "4.1"}
(fact "insert-task-raw is present in the parsed function list"
  (some #(when (= "insert-task-raw" (:name %)) (:name %))
        (:functions (scratch-analysis)))
  => "insert-task-raw")

^{:refer rt.postgres.script.test.scratch-v2/insert-task-wrapped :added "4.1"}
(fact "insert-task-wrapped is present in the parsed function list"
  (some #(when (= "insert-task-wrapped" (:name %)) (:name %))
        (:functions (scratch-analysis)))
  => "insert-task-wrapped")
