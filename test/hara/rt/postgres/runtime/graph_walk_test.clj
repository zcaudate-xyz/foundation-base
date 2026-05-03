(ns hara.rt.postgres.runtime.graph-walk-test
  (:require [hara.rt.postgres.base.application :as app]
            [hara.rt.postgres.runtime.graph-walk :as walk]
            [hara.rt.postgres.runtime.impl-base :as impl]
            [hara.rt.postgres.test.scratch-v1 :as scratch]
            [hara.lang :as l]
            [std.lib.schema :as schema])
  (:use code.test))

(l/script- :postgres
  {:require [[hara.rt.postgres.test.scratch-v1 :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

^{:refer hara.rt.postgres.runtime.graph-walk/wrap-seed-id :added "4.0"}
(fact "seeds ids for missing primary keys in tree"
  ;; Tested via link-data
  )

^{:refer hara.rt.postgres.runtime.graph-walk/wrap-sym-id :added "4.0"}
(fact "allow strings and symbols in primary key"
  ;; Tested via link-data
  )

^{:refer hara.rt.postgres.runtime.graph-walk/wrap-link-attr :added "4.0"}
(fact "adds link information to tree"
  ;; Tested via link-data
  )

^{:refer hara.rt.postgres.runtime.graph-walk/link-data :added "4.0"}
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

^{:refer hara.rt.postgres.runtime.graph-walk/wrap-output :added "4.0"}
(fact "adds the flattened data to output"
  ;; Tested via flatten-data
  )

^{:refer hara.rt.postgres.runtime.graph-walk/flatten-data :added "4.0"}
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
