(ns postgres.core.graph-walk-test
  (:require [hara.runtime.postgres.base.application :as app]
            [postgres.core.graph-walk :as walk]
            [postgres.core.impl-base :as impl]
            [postgres.sample.scratch-v1 :as scratch]
            [hara.lang :as l]
            [std.lib.schema :as schema])
  (:use code.test))

(l/script- :postgres
  {:require [[postgres.sample.scratch-v1 :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

^{:refer postgres.core.graph-walk/wrap-seed-id :added "4.0"}
(fact "seeds ids for missing primary keys in tree"
  ;; Tested via link-data
  )

^{:refer postgres.core.graph-walk/wrap-sym-id :added "4.0"}
(fact "allow strings and symbols in primary key"
  ;; Tested via link-data
  )

^{:refer postgres.core.graph-walk/wrap-link-attr :added "4.0"}
(fact "adds link information to tree"
  ;; Tested via link-data
  )

^{:refer postgres.core.graph-walk/link-data :added "4.0"}
(fact"adds missing ids to tree"

  (walk/link-data
   '{:TaskCache
     {:id "hello",
      :tasks
      [{:name "task1", :status "pending", :cache "hello"}
       {:name "task2",
        :status "pending",
        :cache "hello"}]}}
   (app/app-schema "scratch"))
  => (contains-in {:TaskCache {:id "hello"
                               :tasks [(contains {:name "task1" :id symbol?})
                                       (contains {:name "task2" :id symbol?})]} }))

^{:refer postgres.core.graph-walk/wrap-output :added "4.0"}
(fact "adds the flattened data to output"
  ;; Tested via flatten-data
  )

^{:refer postgres.core.graph-walk/flatten-data :added "4.0"}
(fact "converts tree to flattened data by table"

  (walk/flatten-data
   {:TaskCache
     {:id "hello",
      :tasks
      [{:name "task1", :status "pending", :cache "hello"}
       {:name "task2",
        :status "pending",
        :cache "hello"}]}}
   (app/app-schema "scratch"))
  => (contains {:TaskCache [{:id "hello"}],
                :Task (contains [(contains {:name "task1", :cache "hello", :status "pending"})
                                 (contains {:name "task2", :cache "hello", :status "pending"})] :in-any-order)}))


(comment
  (./import))
