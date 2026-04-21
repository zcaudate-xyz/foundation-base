(ns rt.postgres.runtime.graph-test
  (:require [rt.postgres.base.application :as app]
            [rt.postgres.runtime.graph :refer :all]
            [rt.postgres.runtime.graph-view :as view]
            [rt.postgres.runtime.impl-base :as impl]
            [rt.postgres.test.scratch-v1 :as scratch]
            [std.lang :as l]
            [std.lib.foundation :as f]
            [std.lib.schema :as schema])
  (:use code.test))

(l/script- :postgres
  {:require [[rt.postgres.test.scratch-v1 :as scratch]
             [rt.postgres :as pg]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

^{:refer rt.postgres.runtime.graph/g:id :added "4.0"}
(fact "gets only id"

  (pg/g:id scratch/Task
    {:where {}})
  => string?)

^{:refer rt.postgres.runtime.graph/g:count :added "4.0"}
(fact "gets only count"

  (pg/g:count scratch/Task)
  => string?)

^{:refer rt.postgres.runtime.graph/g:select :added "4.0"}
(fact "returns matching entries"

  (pg/g:select scratch/Task)
  => string?)

^{:refer rt.postgres.runtime.graph/g:get :added "4.0"}
(fact "gets a single entry"

  (pg/g:get scratch/Task
    {:where {}})
  => string?)

^{:refer rt.postgres.runtime.graph/g:update :added "4.0"}
(fact "constructs the update form"

  (pg/g:update scratch/Task
    {:set {:name "name"}
     :where {:id (str (f/uuid-nil))}
     :track 'o-op})
  => string?)

^{:refer rt.postgres.runtime.graph/g:modify :added "4.0"}
(fact  "constructs the modify form"

  (binding [rt.postgres.base.grammar.form-let/*input-syms* (volatile! #{'o-op})]
    (pg/g:modify scratch/Task
      {:set {:name "name"}
       :where {:id (str (f/uuid-nil))}
       :track 'o-op}))
  => string?)

^{:refer rt.postgres.runtime.graph/g:delete :added "4.0"}
(fact  "constructs the delete form"

  (pg/g:delete scratch/Task)
  => string?)

^{:refer rt.postgres.runtime.graph/g:insert :added "4.0"}
(fact "constructs an insert form"

  (binding [rt.postgres.base.grammar.form-let/*input-syms* (volatile! #{'o-op})]
    (pg/g:insert scratch/Task
      {:name "name"
       :status "pending"
       :cache (str (f/uuid-nil))}
      {:track 'o-op}))
  => string?)

^{:refer rt.postgres.runtime.graph/q :added "4.0"}
(fact "constructs a query form"

  (pg/q scratch/Task)
  => string?)

^{:refer rt.postgres.runtime.graph/q:get :added "4.0"}
(fact "constructs a single query form"

  (pg/q:get scratch/Task)
  => string?)

^{:refer rt.postgres.runtime.graph/view :added "4.0"}
(fact "constructs a view form"

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


^{:refer rt.postgres.runtime.graph/g:where :added "4.0"}
(fact "constructs the where clause"
  (pg/g:where scratch/Task {:name "foo"})
  => string?)

^{:refer rt.postgres.runtime.graph/g:exists :added "4.0"}
(fact "checks for existence"
  (pg/g:exists scratch/Task {:where {:name "foo"}})
  => string?)
