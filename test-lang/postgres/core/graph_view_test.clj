(ns postgres.core.graph-view-test
  (:require [lang.runtime.postgres.base.application :as app]
            [postgres.core.graph-view :as view]
            [postgres.core.graph-query :as query]
            [postgres.core.impl-base :as impl]
            [postgres.gen.bind-macro :as gen]
            [postgres.sample.scratch-v1 :as scratch]
            [lang.core :as l]
            [std.lib.schema :as schema])
  (:use code.test))

(l/script- :postgres
  {:require [[postgres.sample.scratch-v1 :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

^{:refer postgres.core.graph-view/make-view-prep :added "4.0"}
(fact "preps view access")

^{:refer postgres.core.graph-view/primary-key :added "4.0"}
(fact "gets the primary key of a schema"

  (view/primary-key 'scratch/Task)
  => :uuid

  (view/primary-key 'scratch/TaskCache)
  => :uuid)

^{:refer postgres.core.graph-view/lead-symbol :added "4.0"}
(fact "gets the lead symbol"

  (view/lead-symbol '[:uuid i-account-id])
  => 'i-account-id)

^{:refer postgres.core.graph-view/defsel-fn :added "4.0"}
(fact "the defsel generator function")

^{:refer postgres.core.graph-view/defsel.pg :added "4.0"}
(fact "creates a select function"

  ;;
  ;; GENERAL-ACCESS
  ;;

  (view/defsel.pg ^{:- [scratch/Task]
                    :scope #{:public}
                    :args [:name i-name]}
    task-by-name
    {:name i-name})
  => #'postgres.core.graph-view-test/task-by-name

  (:static/view @postgres.core.graph-view-test/task-by-name)
  => '{:args [:name i-name],
      :table postgres.sample.scratch-v1/Task,
      :key :Task,
      :type :select,
      :scope #{:public},
      :guards nil,
      :query-base {:name i-name},
      :tag "by-name",
      :query {"name" [:eq i-name]},
      :autos nil})

^{:refer postgres.core.graph-view/defret-fn :added "4.0"}
(fact "the defret generator function derives composite physical keys"
  (let [captured (atom nil)
        sym      (with-meta 'task-composite-ret
                   {:- '[scratch/Task]})]
    (with-redefs [impl/prep-table
                  (fn [_ _ _]
                    [{:static/schema-primary [{:id :id :type :uuid}
                                              {:id :class-table :type :enum}]}
                     {:id         [{:type :uuid :primary "default"}]
                      :class-table [{:type :enum}]}
                     {}])
                  query/query-fn
                  (fn [_ params]
                    (reset! captured params)
                    :query)]
      (view/defret-fn nil
                      sym
                      [:uuid 'i-id :text 'i-class-table]
                      #{:*/data})
      @captured)
    => {:where {:id 'i-id
                :class-table 'i-class-table}
        :returning #{:*/data}
        :single true}))

^{:refer postgres.core.graph-view/defret-fn :added "4.0"
  :id explicit-identity}
(fact "the defret generator accepts an explicit logical identity"
  (let [captured (atom nil)
        sym      (with-meta 'task-logical-ret
                   {:- '[scratch/Task]
                    :identity [:class-table :class-ref]})]
    (with-redefs [impl/prep-table
                  (fn [_ _ _]
                    [{:static/schema-primary {:id :id :type :uuid}}
                     {:class-table [{:type :enum}]
                      :class-ref   [{:type :uuid}]}
                     {}])
                  query/query-fn
                  (fn [_ params]
                    (reset! captured params)
                    :query)]
      (view/defret-fn nil
                      sym
                      [:text 'i-class-table :uuid 'i-class-ref]
                      #{:*/data})
      @captured)
    => {:where {:class-table 'i-class-table
                :class-ref 'i-class-ref}
        :returning #{:*/data}
        :single true}))

^{:refer postgres.core.graph-view/defret-fn :added "4.0"
  :id invalid-identity-arity}
(fact "the defret generator rejects incomplete identities"
  (let [sym (with-meta 'task-invalid-ret
              {:- '[scratch/Task]})
        result (try
                 (with-redefs [impl/prep-table
                               (fn [_ _ _]
                                 [{:static/schema-primary [{:id :id :type :uuid}
                                                           {:id :class-table :type :enum}]}
                                  {:id         [{:type :uuid}]
                                   :class-table [{:type :enum}]}
                                  {}])]
                   (view/defret-fn nil
                                   sym
                                   [:uuid 'i-id]
                                   #{:*/data}))
                 :ok
                 (catch clojure.lang.ExceptionInfo ex
                   (select-keys (ex-data ex) [:expected :actual :identity])))]
    result
    => {:expected 2
        :actual 1
        :identity [:id :class-table]}))

^{:refer postgres.core.graph-view/defret.pg :added "4.0"}
(fact "creates a returns function"

  (view/defret.pg ^{:- [scratch/Task]}
    task-basic
    [:uuid i-task-id]
    #{:*/data})
  #'postgres.core.graph-view-test/task-basic

  (:static/view @postgres.core.graph-view-test/task-basic)
  => '{:args [:uuid i-task-id],
       :table postgres.sample.scratch-v1/Task,
       :key :Task,
       :type :return,
       :scope nil,
       :guards nil,
       :tag "basic",
       :query #{:*/data},
       :autos nil})

^{:refer postgres.core.graph-view/defret.pg :added "4.0"
  :id explicit-view}
(fact "binds an explicit identity into the return view"
  (view/defret.pg ^{:- [scratch/Task]
                    :identity [:id]}
    task-basic-explicit-id
    [:uuid i-task-id]
    #{:*/data})

  (gen/bind-view task-basic-explicit-id)
  => {:input [{:symbol "i_task_id" :type "uuid"}]
      :return "jsonb"
      :schema "scratch"
      :id "task_basic_explicit_id"
      :flags {}
      :view {:table "Task"
             :type "return"
             :tag "basic_explicit_id"
             :query ["*/data"]
             :identity ["id"]}})

^{:refer postgres.core.graph-view/view-fn :added "4.0"}
(fact "constructs a view function"

  (view/view-fn '[-/task-basic]
                '[-/task-by-name "hello"]
                {:limit 10})
  => '[postgres.sample.scratch-v1/Task
       {:where {"name" [:eq "hello"]},
        :returning #{:*/data},
        :limit 10}])

^{:refer postgres.core.graph-view/view :added "4.0"}
(fact "view macro"

  (macroexpand-1
   '(view/view
     [-/task-basic]
     [-/task-by-name "hello"]
     {:limit 10}))
  => '[:with j-ret :as [:select (--- [#{"id"} #{"status"} #{"name"} #{"time_created"} #{"time_updated"}])
                        :from postgres.sample.scratch-v1/Task \\ :where {"name" [:eq "hello"]}
                        \\ :limit 10]
       \\ :select (jsonb-agg j-ret) :from j-ret])

(comment
  (./import))
