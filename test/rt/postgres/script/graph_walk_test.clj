(ns rt.postgres.script.graph-walk-test
  (:use code.test)
  (:require [rt.postgres.script.graph-walk :as walk]
            [rt.postgres.script.impl-base :as impl]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.script.scratch :as scratch]
            [std.lib.schema :as schema]
            [std.lang :as l]
            [std.lib :as h]))

(l/script- :postgres
  {:require [[rt.postgres.script.scratch :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

^{:refer rt.postgres.script.graph-walk/wrap-seed-id :added "4.0"}
(fact "seeds ids for missing primary keys in tree"
  ;; Tested via link-data
  )

^{:refer rt.postgres.script.graph-walk/wrap-sym-id :added "4.0"}
(fact "allow strings and symbols in primary key"
  ;; Tested via link-data
  )

^{:refer rt.postgres.script.graph-walk/wrap-link-attr :added "4.0"}
(fact "adds link information to tree"
  ;; Tested via link-data
  )

^{:refer rt.postgres.script.graph-walk/link-data :added "4.0"}
(fact"adds missing ids to tree"
  ^:hidden
  
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

^{:refer rt.postgres.script.graph-walk/wrap-output :added "4.0"}
(fact "adds the flattened data to output"
  ;; Tested via flatten-data
  )

^{:refer rt.postgres.script.graph-walk/flatten-data :added "4.0"}
(fact "converts tree to flattened data by table"
  ^:hidden
  
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
