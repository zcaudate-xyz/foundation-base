(ns rt.postgres.script.graph-view-test
  (:use code.test)
  (:require [rt.postgres.script.graph-view :as view]
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

(view/defaccess.pg
  cache-is-member
  {:forward  [scratch/Task
              {:cache <%>}]
   :reverse  [scratch/TaskCache
              {:id <%>}]
   :roles #{:task}})

^{:refer rt.postgres.script.graph-view/create-defaccess-prep :added "4.0"}
(fact "creates a defaccess prep"
  ^:hidden
  
  (view/create-defaccess-prep 'cache-is-member
                              '{:forward  [scratch/Task
                                           {:cache <%>}]
                                :reverse  [scratch/TaskCache
                                           {:id <%>}]
                                :roles #{:task}})
  => '{:symbol rt.postgres.script.graph-view-test/cache-is-member,
       :forward {:table rt.postgres.script.scratch/Task,
                 :clause {:cache <%>},
                 :form [:with j-ret :as
                        [:select (---
                                  [#{"id"}
                                   #{"status"}
                                   #{"name"}
                                   #{"cache_id"}
                                   #{"op_created"}
                                   #{"op_updated"}
                                   #{"time_created"}
                                   #{"time_updated"}])
                         :from rt.postgres.script.scratch/Task
                         \\ :where {"cache_id" [:eq <%>]}]
                        \\ :select (jsonb-agg j-ret) :from j-ret]},
       :reverse    {:table rt.postgres.script.scratch/TaskCache,
                    :clause {:id <%>},
                    :form [:with j-ret :as
                           [:select
                            (--- [#{"id"}
                                  #{"op_created"}
                                  #{"op_updated"}
                                  #{"time_created"}
                                  #{"time_updated"}])
                            :from rt.postgres.script.scratch/TaskCache
                            \\ :where {"id" [:eq <%>]}]
                           \\ :select (jsonb-agg j-ret) :from j-ret]},
       :roles #{:task}})

^{:refer rt.postgres.script.graph-view/defaccess.pg :added "4.0"}
(fact "creates a defaccess macro")

^{:refer rt.postgres.script.graph-view/make-view-access :added "4.0"}
(fact "creates view access")

^{:refer rt.postgres.script.graph-view/make-view-prep :added "4.0"}
(fact "preps view access")

^{:refer rt.postgres.script.graph-view/primary-key :added "4.0"}
(fact "gets the primary key of a schema"
  ^:hidden
  
  (view/primary-key 'scratch/Task)
  => :uuid

  (view/primary-key 'scratch/TaskCache)
  => :uuid)

^{:refer rt.postgres.script.graph-view/lead-symbol :added "4.0"}
(fact "gets the lead symbol"
  ^:hidden
  
  (view/lead-symbol '[:uuid i-account-id])
  => 'i-account-id)

^{:refer rt.postgres.script.graph-view/defsel-fn :added "4.0"}
(fact "the defsel generator function")

^{:refer rt.postgres.script.graph-view/defsel.pg :added "4.0"}
(fact "creates a select function"
  ^:hidden
  
  ;;
  ;; GENERAL-ACCESS
  ;;
  
  (view/defsel.pg ^{:- [scratch/Task]
                    :scope #{:public}
                    :args [:name i-name]}
    task-by-name
    {:name i-name})
  => #'rt.postgres.script.graph-view-test/task-by-name
  
  (:static/view @rt.postgres.script.graph-view-test/task-by-name)
  => '{:args [:name i-name],
      :table rt.postgres.script.scratch/Task,
      :key :Task,
      :type :select,
      :scope #{:public},
      :guards nil,
      :access nil,
      :query-base {:name i-name},
      :tag "by-name",
      :query {"name" [:eq i-name]},
      :autos nil})

^{:refer rt.postgres.script.graph-view/defret-fn :added "4.0"}
(fact "the defref generator function")

^{:refer rt.postgres.script.graph-view/defret.pg :added "4.0"}
(fact "creates a returns function"
  ^:hidden
  
  (view/defret.pg ^{:- [scratch/Task]}
    task-basic
    [:uuid i-task-id]
    #{:*/data})
  #'rt.postgres.script.graph-view-test/task-basic

  (:static/view @rt.postgres.script.graph-view-test/task-basic)
  => '{:args [:uuid i-task-id],
       :table rt.postgres.script.scratch/Task,
       :key :Task,
       :type :return,
       :scope nil,
       :guards nil,
       :access nil,
       :tag "basic",
       :query #{:*/data},
       :autos nil})

^{:refer rt.postgres.script.graph-view/view-fn :added "4.0"}
(fact "constructs a view function"
  ^:hidden

  (view/view-fn '[-/task-basic]
                '[-/task-by-name "hello"]
                {:limit 10})
  => '[rt.postgres.script.scratch/Task
       {:where {"name" [:eq "hello"]},
        :returning #{:*/data},
        :limit 10}])

^{:refer rt.postgres.script.graph-view/view :added "4.0"}
(fact "view macro"
  ^:hidden

  (macroexpand-1
   '(view/view
     [-/task-basic]
     [-/task-by-name "hello"]
     {:limit 10}))
  => '[:with j-ret :as [:select (--- [#{"id"} #{"status"} #{"name"} #{"time_created"} #{"time_updated"}])
                        :from rt.postgres.script.scratch/Task \\ :where {"name" [:eq "hello"]}
                        \\ :limit 10]
       \\ :select (jsonb-agg j-ret) :from j-ret])

(comment
  (./import))
