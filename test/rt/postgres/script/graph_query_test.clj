(ns rt.postgres.script.graph-query-test
  (:use code.test)
  (:require [rt.postgres.script.graph-query :as q]
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

(def -sch- (get-in (app/app "scratch")
                   [:schema
                    :tree]))

^{:refer rt.postgres.script.graph-query/table-col-token :added "4.0"}
(fact "constructs a table ref token"
  ^:hidden
  
  (q/table-col-token `scratch/Task 'cache)
  => '(. rt.postgres.script.scratch/Task #{"cache_id"}))

^{:refer rt.postgres.script.graph-query/table-id-token :added "4.0"}
(fact "constructs a table id token"
  ^:hidden
  
  (q/table-id-token `scratch/Task)
  => '(. rt.postgres.script.scratch/Task #{"id"}))

^{:refer rt.postgres.script.graph-query/returning-block :added "4.0"}
(fact "constructs a returning block"
  ^:hidden
  
  (q/returning-block @scratch/TaskCache
                     (first (get-in -sch- [:TaskCache :tasks]))
                     {}
                     [:id]
                     {}
                     q/query-raw-fn
                     (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => '(% [[:with j-ret :as [:select (--- [#{"id"}]) :from rt.postgres.script.scratch/Task
                            \\ :where #{["cache_id" [:eq (. rt.postgres.script.scratch/TaskCache #{"id"})]]}]
           \\ :select (jsonb-agg j-ret) :from j-ret] :as #{"tasks"}]))

^{:refer rt.postgres.script.graph-query/returning-map-markers :added "4.0"}
(fact "prepares the map markers"
  ^:hidden
  
  (q/returning-map-markers @scratch/TaskCache
                           (get -sch- :TaskCache)
                           [[:tasks]]
                           q/query-raw-fn
                           (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => vector?

  (q/returning-map-markers @scratch/TaskCache
                           (get -sch- :TaskCache)
                           [[:id]]
                           q/query-raw-fn
                           (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => (throws))

^{:refer rt.postgres.script.graph-query/returning-all-markers :added "4.0"}
(fact "returns all markers for return"
  ^:hidden
  
  (q/returning-all-markers @scratch/Task
                           (get -sch- :Task)
                           [:id]
                           q/query-raw-fn
                           (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => #{:id}

  (q/returning-all-markers @scratch/Task
                           (get -sch- :Task)
                           [[:id]]
                           q/query-raw-fn
                           (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => (throws)

  (q/returning-all-markers @scratch/Task
                           (get -sch- :Task)
                           [[:cache]]
                           q/query-raw-fn
                           (last (impl/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => '#{{:expr (% [[:with j-ret :as [:select (--- [#{"id"}])
                                     :from rt.postgres.script.scratch/TaskCache
                                     \\ :where #{["id" [:eq (. rt.postgres.script.scratch/Task #{"cache_id"})]]}
                                     \\ :limit 1]
                 \\ :select (to-jsonb j-ret) :from j-ret] :as #{"cache"}])}}
  

  (q/returning-all-markers @scratch/TaskCache
                           (get -sch- :TaskCache)
                           [[:tasks]]
                           q/query-raw-fn
                           (last (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))))
  => '#{{:expr (% [[:with j-ret
                    :as [:select (--- [#{"id"} #{"status"}])
                         :from rt.postgres.script.scratch/Task \\
                         :where #{["cache_id" [:eq (. rt.postgres.script.scratch/TaskCache #{"id"})]]}]
                    \\ :select (jsonb-agg j-ret) :from j-ret] :as #{"tasks"}])}})

^{:refer rt.postgres.script.graph-query/query-raw-fn :added "4.0"}
(fact "constructs a query form with prep"
  ^:hidden
  
  (q/query-raw-fn (impl/prep-table 'scratch/TaskCache true (l/rt:macro-opts :postgres))
                  {:returning #{:tasks}})
  => vector?)

^{:refer rt.postgres.script.graph-query/query-fn :added "4.0"}
(fact "constructs a query form"
  ^:hidden

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
      (q/query-fn 'scratch/Task
                  {}))
  => '[:with j-ret :as
       [:select
        (--- [#{"id"}
               #{"status"}
               #{"name"}
               #{"cache_id"}
               #{"time_created"}
               #{"time_updated"}])
        :from rt.postgres.script.scratch/Task]
       \\ :select (jsonb-agg j-ret) :from j-ret]

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (q/query-fn 'scratch/Task
                {:returning '#{cache-id}
                 :as :raw}))
  => '[:select (--- [#{"cache_id"}]) :from rt.postgres.script.scratch/Task]
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (q/query-fn 'scratch/Task
                {:returning #{:id}
                 :as :raw}))
  => '[:select (--- [#{"id"}]) :from rt.postgres.script.scratch/Task]

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (q/query-fn 'scratch/Task
                {:returning '#{{:expr (count *)
                                :as len}}
                 :as :raw}))
  => '[:select (--- [[(count *) :as len]]) :from rt.postgres.script.scratch/Task]


  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (q/query-fn 'scratch/Task
                {:returning '#{[:cache :*/data]}
                 :as :raw}))
  => '[:select (--- [(% [(quote ([:with j-ret
                                   :as [:select (--- [#{"id"} #{"time_created"} #{"time_updated"}])
                                        :from rt.postgres.script.scratch/TaskCache
                                        \\ :where #{["id" [:eq (. rt.postgres.script.scratch/Task #{"cache_id"})]]}
                                        \\ :limit 1]
                                   \\ :select (to-jsonb j-ret) :from j-ret])) :as #{"cache"}])])
       :from rt.postgres.script.scratch/Task]
  
  
  
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (q/query-fn 'scratch/Task
                {:returning '#{:*/default
                               [:cache
                                {:id "cache-id"}]}
                 :as :raw}))
  => '[:select
          (--- [#{"id"} #{"status"} #{"name"}
                #{"time_created"} #{"time_updated"}
                (% [(quote ([:with j-ret
                             :as [:select (--- [#{"id"}])
                                  :from rt.postgres.script.scratch/TaskCache
                                  \\ :where #{["id" [:eq (. rt.postgres.script.scratch/Task #{"cache_id"})]
                                               :and "id" [:eq "cache-id"]]}
                                  \\ :limit 1]
                             \\ :select (to-jsonb j-ret) :from j-ret])) :as #{"cache"}])])
          :from rt.postgres.script.scratch/Task]
  
  
  
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (q/query-fn 'scratch/TaskCache
                {:returning '#{:*/default
                               [:tasks]}
                 :as :raw}))
  => '[:select (--- [#{"id"} #{"time_created"} #{"time_updated"}
                      (% [(quote ([:with j-ret
                                   :as [:select (--- [#{"id"} #{"status"}])
                                        :from rt.postgres.script.scratch/Task
                                        \\ :where #{["cache_id" [:eq (. rt.postgres.script.scratch/TaskCache #{"id"})]]}]
                                   \\ :select (jsonb-agg j-ret) :from j-ret])) :as #{"tasks"}])])
       :from rt.postgres.script.scratch/TaskCache])
