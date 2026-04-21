(ns rt.postgres.runtime.graph-insert-test
  (:require [rt.postgres.base.application :as app]
            [rt.postgres.runtime.graph-insert :as insert]
            [rt.postgres.runtime.impl-base :as impl]
            [rt.postgres.test.scratch-v1 :as scratch]
            [std.lang :as l]
            [std.lib.collection :as collection]
            [std.lib.schema :as schema]
            [std.lib.walk :as walk])
  (:use code.test))

(l/script- :postgres
  {:require [[rt.postgres.test.scratch-v1 :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

^{:refer rt.postgres.runtime.graph-insert/insert-walk-ids :added "4.0"}
(fact "inserts walk ids to the entries"

  (let [output (volatile! #{})]
    (walk/postwalk
     (fn [x]
       (if (not-empty (meta x))
         (vswap! output conj (meta x)))
       x)
     (insert/insert-walk-ids
      '{:TaskCache
        {:id "hello",
         :tasks
         [{:name "task1", :status "pending", :cache "hello", :id ?id-00}
          {:name "task2",
           :status "pending",
           :cache "hello",
           :id ?id-01}]}}))
    @output)
  => #{#:walk{:id 2}
       #:walk{:id 4}
       #:walk{:id 1}
       #:walk{:id 0, :data true}
       #:walk{:id 3}})

^{:refer rt.postgres.runtime.graph-insert/insert-generate-graph-tree :added "4.0"}
(fact "generates a graph tree from nodes"

  (insert/insert-generate-graph-tree
   (insert/insert-walk-ids
    '{:TaskCache
      {:id "hello",
       :tasks
       [{:name "task1", :status "pending", :cache "hello", :id ?id-00}
        {:name "task2",
         :status "pending",
         :cache "hello",
         :id ?id-01}]}}))
  => '[{:TaskCache
        {:id "hello",
         :tasks [{:name "task1",
                  :status "pending",
                  :cache "hello",
                  :id ?id-00}
                {:name "task2",
                 :status "pending",
                 :cache "hello",
                 :id ?id-01}]}}
       {3 {}, 4 {}, 2 [3 4], 1 {:tasks 2}}])

^{:refer rt.postgres.runtime.graph-insert/insert-associate-graph-data :added "4.0"}
(fact "associate nodes wit h graph data"

  (apply insert/insert-associate-graph-data
   (insert/insert-generate-graph-tree
    (insert/insert-walk-ids
     '{:TaskCache
       {:id "hello",
        :tasks
        [{:name "task1", :status "pending", :cache "hello", :id ?id-00}
         {:name "task2",
          :status "pending",
          :cache "hello",
          :id ?id-01}]}})))
  => '[(3 4 2 1)
       {3 gid_3, 4 gid_4, 2 gid_2, 1 gid_1}
       {?id-00 3, ?id-01 4, "hello" 1}])

^{:refer rt.postgres.runtime.graph-insert/insert-gen-sql :added "4.0"}
(fact "generates sql given graph"

  (insert/insert-gen-sql
   '{:TaskCache
     {:id "hello",
      :tasks
      [{:name "task1", :status "pending", :cache "hello", :id ?id-00}
       {:name "task2",
        :status "pending",
        :cache "hello",
        :id ?id-01}]}}
   {:track 'o-op}
   (assoc (last (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres)))
          :application (app/app "scratch")))
  => collection/form?)

^{:refer rt.postgres.runtime.graph-insert/insert-fn-raw :added "4.0"}
(fact "constructs insert form with prep"

  (insert/insert-fn-raw
   (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))
   {:id "hello"}
   {:track 'o-op})
  => collection/form?)

^{:refer rt.postgres.runtime.graph-insert/insert-fn :added "4.0"}
(fact "constructs insert form"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (insert/insert-fn 'scratch/TaskCache
                      {:id "hello"
                       :tasks [{:name "task1"
                                :status "pending"}
                               {:name "task2"
                                :status "pending"}]}
                      {:track 'o-op}))
  => collection/form?)


^{:refer rt.postgres.runtime.graph-insert/insert-fn.assign :adopt true :added "4.0"}
(fact "constructs insert form with assignment"

  ((-> (l/with:macro-opts [(l/rt:macro-opts :postgres)]
         (insert/insert-fn 'scratch/TaskCache
                           {:id "hello"
                            :tasks [{:name "task1"
                                     :status "pending"}
                                    {:name "task2"
                                     :status "pending"}]}
                           {:track 'o-op}))
       meta
       :assign/fn)
   'o-output)
  => collection/form?)
