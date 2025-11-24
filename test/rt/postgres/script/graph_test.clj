(ns rt.postgres.script.graph-test
  (:use code.test)
  (:require [rt.postgres.script.graph :refer :all]
            [rt.postgres.script.graph-view :as view]
            [rt.postgres.script.impl-base :as impl]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.script.scratch :as scratch]
            [std.lib.schema :as schema]
            [std.lang :as l]
            [std.lib :as h]))

(l/script- :postgres
  {:require [[rt.postgres.script.scratch :as scratch]
             [rt.postgres :as pg]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

^{:refer rt.postgres.script.graph/g:id :added "4.0"}
(fact "gets only id"
  ^:hidden
  
  (pg/g:id scratch/Task
    {:where {}})
  => string?)

^{:refer rt.postgres.script.graph/g:count :added "4.0"}
(fact "gets only count"
  ^:hidden
  
  (pg/g:count scratch/Task)
  => string?)

^{:refer rt.postgres.script.graph/g:select :added "4.0"}
(fact "returns matching entries"
  ^:hidden
  
  (pg/g:select scratch/Task)
  => string?)

^{:refer rt.postgres.script.graph/g:get :added "4.0"}
(fact "gets a single entry"
  ^:hidden
  
  (pg/g:get scratch/Task
    {:where {}})
  => string?)

^{:refer rt.postgres.script.graph/g:update :added "4.0"}
(fact "constructs the update form"
  ^:hidden
  
  (pg/g:update scratch/Task
    {:set {:name "name"}
     :where {:id (str (h/uuid-nil))}
     :track 'o-op})
  => string?)

^{:refer rt.postgres.script.graph/g:modify :added "4.0"}
(fact  "constructs the modify form"
  ^:hidden

  (binding [rt.postgres.grammar.form-let/*input-syms* (volatile! #{'o-op})]
    (pg/g:modify scratch/Task
      {:set {:name "name"}
       :where {:id (str (h/uuid-nil))}
       :track 'o-op}))
  => string?)

^{:refer rt.postgres.script.graph/g:delete :added "4.0"}
(fact  "constructs the delete form"
  ^:hidden

  (pg/g:delete scratch/Task)
  => string?)

^{:refer rt.postgres.script.graph/g:insert :added "4.0"}
(fact "constructs an insert form"
  ^:hidden
  
  (binding [rt.postgres.grammar.form-let/*input-syms* (volatile! #{'o-op})]
    (pg/g:insert scratch/Task
      {:name "name"
       :status "pending"
       :cache (str (h/uuid-nil))}
      {:track 'o-op}))
  => string?)

^{:refer rt.postgres.script.graph/q :added "4.0"}
(fact "constructs a query form"
  ^:hidden
  
  (pg/q scratch/Task)
  => string?)

^{:refer rt.postgres.script.graph/q:get :added "4.0"}
(fact "constructs a single query form"
  ^:hidden
  
  (pg/q:get scratch/Task)
  => string?)

^{:refer rt.postgres.script.graph/view :added "4.0"}
(fact "constructs a view form"
  ^:hidden

  (view/defret.pg ^{:- [scratch/Task]}
    task-basic
    [:uuid i-task-id]
    #{:*/data})
  
  (view/defsel.pg ^{:- [scratch/Task]
                  :scope #{:public}
                    :args [:name i-name]}
    task-by-name
    {:name i-name})
  
  (pg/view
      [-/task-basic]
      [-/task-by-name "hello"]
    {:limit 10})
  => string?)


^{:refer rt.postgres.script.graph/g:where :added "4.0"}
(fact "constructs the where clause")

^{:refer rt.postgres.script.graph/g:exists :added "4.0"}
(fact "checks for existence")