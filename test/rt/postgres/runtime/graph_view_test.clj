(ns rt.postgres.runtime.graph-view-test
  (:require [rt.postgres.base.application :as app]
            [rt.postgres.runtime.graph-view :as view]
            [rt.postgres.runtime.impl-base :as impl]
            [rt.postgres.test.scratch-v1 :as scratch]
            [std.lang :as l]
            [std.lib.schema :as schema])
  (:use code.test))

(l/script- :postgres
  {:require [[rt.postgres.test.scratch-v1 :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

^{:refer rt.postgres.runtime.graph-view/make-view-prep :added "4.0"}
(fact "preps view access")

^{:refer rt.postgres.runtime.graph-view/primary-key :added "4.0"}
(fact "gets the primary key of a schema"

  (view/primary-key 'scratch/Task)
  => :uuid

  (view/primary-key 'scratch/TaskCache)
  => :uuid)

^{:refer rt.postgres.runtime.graph-view/lead-symbol :added "4.0"}
(fact "gets the lead symbol"

  (view/lead-symbol '[:uuid i-account-id])
  => 'i-account-id)

^{:refer rt.postgres.runtime.graph-view/defsel-fn :added "4.0"}
(fact "the defsel generator function")

^{:refer rt.postgres.runtime.graph-view/defsel.pg :added "4.0"}
(fact "creates a select function"

  ;;
  ;; GENERAL-ACCESS
  ;;

  (view/defsel.pg ^{:- [scratch/Task]
                    :scope #{:public}
                    :args [:name i-name]}
    task-by-name
    {:name i-name})
  => #'rt.postgres.runtime.graph-view-test/task-by-name

  (:static/view @rt.postgres.runtime.graph-view-test/task-by-name)
  => '{:args [:name i-name],
      :table rt.postgres.test.scratch-v1/Task,
      :key :Task,
      :type :select,
      :scope #{:public},
      :guards nil,
      :query-base {:name i-name},
      :tag "by-name",
      :query {"name" [:eq i-name]},
      :autos nil})

^{:refer rt.postgres.runtime.graph-view/defret-fn :added "4.0"}
(fact "the defref generator function")

^{:refer rt.postgres.runtime.graph-view/defret.pg :added "4.0"}
(fact "creates a returns function"

  (view/defret.pg ^{:- [scratch/Task]}
    task-basic
    [:uuid i-task-id]
    #{:*/data})
  #'rt.postgres.runtime.graph-view-test/task-basic

  (:static/view @rt.postgres.runtime.graph-view-test/task-basic)
  => '{:args [:uuid i-task-id],
       :table rt.postgres.test.scratch-v1/Task,
       :key :Task,
       :type :return,
       :scope nil,
       :guards nil,
       :tag "basic",
       :query #{:*/data},
       :autos nil})

^{:refer rt.postgres.runtime.graph-view/view-fn :added "4.0"}
(fact "constructs a view function"

  (view/view-fn '[-/task-basic]
                '[-/task-by-name "hello"]
                {:limit 10})
  => '[rt.postgres.test.scratch-v1/Task
       {:where {"name" [:eq "hello"]},
        :returning #{:*/data},
        :limit 10}])

^{:refer rt.postgres.runtime.graph-view/view :added "4.0"}
(fact "view macro"

  (macroexpand-1
   '(view/view
     [-/task-basic]
     [-/task-by-name "hello"]
     {:limit 10}))
  => '[:with j-ret :as [:select (--- [#{"id"} #{"status"} #{"name"} #{"time_created"} #{"time_updated"}])
                        :from rt.postgres.test.scratch-v1/Task \\ :where {"name" [:eq "hello"]}
                        \\ :limit 10]
       \\ :select (jsonb-agg j-ret) :from j-ret])

(comment
  (./import))
