(ns rt.postgres.runtime.graph-base-test
  (:require [rt.postgres.base.application :as app]
            [rt.postgres.runtime.graph-base :as base]
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

(def -sch- (get-in (app/app "scratch")
                   [:schema
                    :tree]))

^{:refer rt.postgres.runtime.graph-base/where-pair-ref :added "4.0"}
(fact "constructs the where ref pair"

  ;; FORWARD
  (base/where-pair-ref [:cache "cache-001"]
                       base/where-fn
                       (first (get-in -sch- [:Task :cache]))
                       (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => [:cache [:eq "cache-001"]]

  ;; REVERSE
  (base/where-pair-ref [:tasks "tasks-001"]
                       base/where-fn
                       (first (get-in -sch- [:TaskCache :tasks]))
                       (last (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))))
  => '[:id [:in [:select (--- [#{"cache_id"}])
                 :from rt.postgres.test.scratch-v1/Task
                 \\ :where {"id" [:eq "tasks-001"]}]]])

^{:refer rt.postgres.runtime.graph-base/where-pair-fn :added "4.0"}
(fact "constructs the where pair"

  (base/where-pair-fn [:id "hello"]
                      base/where-fn
                      (get-in -sch- [:Task])
                      (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => [:id [:eq "hello"]]

  (base/where-pair-fn [:cache "cache-001"]
                      base/where-fn
                      (get-in -sch- [:Task])
                      (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => [:cache [:eq "cache-001"]]

  (base/where-pair-fn [:tasks "tasks-001"]
                      base/where-fn
                      (get-in -sch- [:TaskCache])
                      (last (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))))
  => '[:id [:in [:select (--- [#{"cache_id"}])
                 :from rt.postgres.test.scratch-v1/Task
                 \\ :where {"id" [:eq "tasks-001"]}]]])

^{:refer rt.postgres.runtime.graph-base/where-fn :added "4.0"}
(fact "constructs where clause"

  (base/where-fn (get-in -sch- [:Task])
                 {:cache "cache-001"}
                 (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => '{:cache [:eq "cache-001"]}


  (base/where-fn (get-in -sch- [:TaskCache])
                 {:tasks "tasks-001"}
                 (last (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))))
  => '{:id [:in [:select (--- [#{"cache_id"}]) :from rt.postgres.test.scratch-v1/Task
                 \\ :where {"id" [:eq "tasks-001"]}]]}

  (base/where-fn (get-in -sch- [:TaskCache])
                 #{[:tasks "tasks-001"]}
                 (last (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))))
  => '#{[:id [:in [:select (--- [#{"cache_id"}]) :from rt.postgres.test.scratch-v1/Task
                   \\ :where {"id" [:eq "tasks-001"]}]]]}

  (base/where-fn (get-in -sch- [:TaskCache])
                 {:tasks "tasks-001"
                  :id "hello"}
                 (last (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))))
  => '#{[:id [:in [:select (--- [#{"cache_id"}]) :from rt.postgres.test.scratch-v1/Task
                  \\ :where {"id" [:eq "tasks-001"]}]]
         :id [:eq "hello"]]}

  (base/where-fn (get-in -sch- [:TaskCache])
                 #{[:tasks "tasks-001"
                    :id "hello"]}
                 (last (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))))
  => '#{[:id [:in [:select (--- [#{"cache_id"}]) :from rt.postgres.test.scratch-v1/Task
                   \\ :where {"id" [:eq "tasks-001"]}]]
         :and
         :id [:eq "hello"]]}

  (base/where-fn (get-in -sch- [:Task])
                 {:cache "cache-001"}
                 (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => {:cache [:eq "cache-001"]}

  (base/where-fn (get-in -sch- [:Task])
                 {:cache {:tasks {:cache "cache-001"}}}
                 (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => '{:cache [:in [:select (--- [#{"id"}]) :from rt.postgres.test.scratch-v1/TaskCache
                   \\ :where {"id" [:in [:select (--- [#{"cache_id"}])
                                         :from rt.postgres.test.scratch-v1/Task
                                         \\ :where {"cache_id" [:eq "cache-001"]}]]}]]})

^{:refer rt.postgres.runtime.graph-base/id-where-fn :added "4.0"}
(fact "constructs id-fn"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/id-where-fn 'scratch/TaskCache
                {:where {:tasks "tasks-001"}}))
  => '[{:id [:in [:select (--- [#{"cache_id"}]) :from rt.postgres.test.scratch-v1/Task \\ :where {"id" [:eq "tasks-001"]}]]}]

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/id-where-fn 'scratch/Task
                      {:where {:cache "tasks-001"}}))
  => [{:cache_id [:eq "tasks-001"]}])

^{:refer rt.postgres.runtime.graph-base/id-fn :added "4.0"}
(fact "constructs id-fn"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/id-fn 'scratch/TaskCache
                {:where {:tasks "tasks-001"}}))
  => '[:select (--- [#{"id"}]) :from rt.postgres.test.scratch-v1/TaskCache
       \\ :where {"id" [:in [:select (--- [#{"cache_id"}]) :from rt.postgres.test.scratch-v1/Task
                             \\ :where {"id" [:eq "tasks-001"]}]]}
       \\ :limit 1])

^{:refer rt.postgres.runtime.graph-base/count-fn :added "4.0"}
(fact "constructs count-fn"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/count-fn 'scratch/TaskCache
                   {:where {:tasks "tasks-001"}}))
  => '[:select (count *) :from
       rt.postgres.test.scratch-v1/TaskCache
       \\ :where {"id" [:in [:select (--- [#{"cache_id"}])
                             :from
                             rt.postgres.test.scratch-v1/Task
                             \\ :where
                             {"id" [:eq "tasks-001"]}]]}])

^{:refer rt.postgres.runtime.graph-base/select-fn-raw :added "4.0"}
(fact "constructs a select fn with prep"

  (base/select-fn-raw
   (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))
   {:where {:tasks "tasks-001"}})
  => '[:with j-ret
       :as [:select (--- [#{"id"} #{"time_created"} #{"time_updated"}])
            :from rt.postgres.test.scratch-v1/TaskCache
            \\ :where {"id" [:in [:select (--- [#{"cache_id"}])
                                  :from rt.postgres.test.scratch-v1/Task
                                  \\ :where {"id" [:eq "tasks-001"]}]]}]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.runtime.graph-base/select-fn :added "4.0"}
(fact "constructs a select fn"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/select-fn 'scratch/TaskCache
                    {:where {:tasks "tasks-001"}}))
  => '[:with j-ret
       :as [:select (--- [#{"id"} #{"time_created"} #{"time_updated"}])
            :from rt.postgres.test.scratch-v1/TaskCache
            \\ :where {"id" [:in [:select (--- [#{"cache_id"}])
                                  :from rt.postgres.test.scratch-v1/Task
                                  \\ :where {"id" [:eq "tasks-001"]}]]}]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.runtime.graph-base/delete-fn :added "4.0"}
(fact "constructs a delete fn"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/delete-fn 'scratch/TaskCache
                    {:where {:tasks "tasks-001"}}))
  => '[:with j-ret :as [:delete :from rt.postgres.test.scratch-v1/TaskCache
                        :where {"id" [:in [:select (--- [#{"cache_id"}])
                                           :from rt.postgres.test.scratch-v1/Task
                                           \\ :where {"id" [:eq "tasks-001"]}]]}
                       \\ :returning (--- [#{"id"} #{"op_created"} #{"op_updated"} #{"time_created"} #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.runtime.graph-base/update-fn :added "4.0"}
(fact "constructs an update fn"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/update-fn 'scratch/TaskCache
                    {:set {:time-created 0}
                     :where {:tasks "tasks-001"}
                     :track 'o-op}))
  => '[:with j-ret :as
       [:update rt.postgres.test.scratch-v1/TaskCache
        :set (--- [(== #{"op_updated"} (:uuid (:->> o-op "id")))
                     (== #{"time_created"} (:bigint 0))
                     (== #{"time_updated"} (:bigint (:->> o-op "time")))])
        :where {"id" [:in [:select (--- [#{"cache_id"}])
                              :from rt.postgres.test.scratch-v1/Task
                              \\ :where {"id" [:eq "tasks-001"]}]]}
        \\ :returning (--- [#{"id"} #{"time_created"} #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.runtime.graph-base/modify-fn :added "4.0"}
(fact "constructs an modify fn"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/modify-fn 'scratch/TaskCache
                    {:set {:time-created 0}
                     :where {:tasks "tasks-001"}
                     :track 'o-op}))
  => '(let [(++ u-ret rt.postgres.test.scratch-v1/TaskCache)
            [:update rt.postgres.test.scratch-v1/TaskCache
             :set (--- [(== #{"op_updated"} (:uuid (:->> o-op "id")))
                          (== #{"time_created"} (:bigint 0))
                          (== #{"time_updated"} (:bigint (:->> o-op "time")))])
             :where {"id" [:in [:select (--- [#{"cache_id"}])
                                   :from rt.postgres.test.scratch-v1/Task
                                \\ :where {"id" [:eq "tasks-001"]}]]}
             \\ :returning * :into u-ret]
            _ (if [:not (exists [:select u-ret])]
                [:raise-exception (% "Record Not Found")])]))

(comment
  (first (get-in -sch- [:TaskCache :tasks])))


^{:refer rt.postgres.runtime.graph-base/exists-fn :added "4.0"}
(fact "constructs exists-fn"
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (base/exists-fn 'scratch/TaskCache
                    {:where {:tasks "tasks-001"}}))
  => vector?)
