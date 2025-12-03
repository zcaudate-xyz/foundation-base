(ns rt.postgres.script.impl-insert-test
  (:use code.test)
  (:require [rt.postgres.script.impl-base :as base]
            [rt.postgres.script.impl-insert :as insert]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common-tracker :as tracker]
            [std.lang :as l]
            [std.lang.base.book :as book]))

(l/script- :postgres
  {:require [[rt.postgres.script.scratch :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

(def -tsch- (get-in (app/app "scratch")
                    [:schema
                     :tree
                     :Task]))

^{:refer rt.postgres.script.impl-insert/t-insert-form :added "4.0"}
(fact "insert form"
  ^:hidden
  
  (insert/t-insert-form -tsch-
                        [:id :status]
                        '("A" "B"))
  => '[(>-< [#{"id"} #{"status"}])
       :values (>-< ["A" "B"])])

^{:refer rt.postgres.script.impl-insert/t-insert-symbol :added "4.0"}
(fact  "constructs an insert symbol form"
  ^:hidden

  (insert/t-insert-symbol -tsch-
                          'sym
                          [:name :status :cache]
                          (tracker/add-tracker {:track 'o-op}
                                            (:static/tracker @scratch/Task)
                                            rt.postgres.script.scratch/Task
                                            :insert)
                          (last (base/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => '[(>-< [#{"status"}
              #{"name"}
              #{"cache_id"}
              #{"op_created"}
              #{"op_updated"}
              #{"time_created"}
              #{"time_updated"}])
       :values
       (>-< [(++ (:->> sym "status") rt.postgres.script.scratch/EnumStatus)
              (:text (:->> sym "name"))
              (:uuid (coalesce (:->> sym "cache_id")
                               (:->> (:-> sym "cache") "id")))
             (:uuid (:->> o-op "id"))
             (:uuid (:->> o-op "id"))
             (:bigint (:->> o-op "time"))
             (:bigint (:->> o-op "time"))])])

^{:refer rt.postgres.script.impl-insert/t-insert-map :added "4.0"}
(fact "constructs an insert map form"
  ^:hidden
  
  (insert/t-insert-map -tsch-
                       {:name "hello"
                        :status "pending"
                        :cache "cache-aaa"}
                       (tracker/add-tracker {:track 'o-op}
                                            (:static/tracker @scratch/Task)
                                            rt.postgres.script.scratch/Task
                                            :insert)
                       (last (base/prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => '[(>-< [#{"status"}
              #{"name"}
              #{"cache_id"}
              #{"op_created"}
              #{"op_updated"}
              #{"time_created"}
              #{"time_updated"}])
       :values (>-< [(++ "pending" rt.postgres.script.scratch/EnumStatus)
                      (:text "hello")
                      (:uuid "cache-aaa")
                      (:uuid (:->> o-op "id"))
                      (:uuid (:->> o-op "id"))
                      (:bigint (:->> o-op "time"))
                      (:bigint (:->> o-op "time"))])])

^{:refer rt.postgres.script.impl-insert/t-insert-record :added "4.0"}
(fact "constructs a record insert form"
  ^:hidden
  
  (insert/t-insert-record
   'rt.postgres.script.scratch/Task
   'e
   (tracker/add-tracker {:track 'o-op}
                        
                        (:static/tracker @scratch/Task)
                        rt.postgres.script.scratch/Task
                        :insert))
  => '[:select *
       :from
       (jsonb-populate-record
        (++ nil rt.postgres.script.scratch/Task)
        (|| e
            {:op-created (:->> o-op "id"),
             :op-updated (:->> o-op "id"),
             :time-created (:->> o-op "time"),
             :time-updated (:->> o-op "time")}))])

^{:refer rt.postgres.script.impl-insert/t-insert-raw :added "4.0"}
(fact "contructs an insert form with prep"
  ^:hidden
  
  (insert/t-insert-raw
   (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
   {:name "hello"
    :status "pending"
    :cache "id"}
   (tracker/add-tracker {:track 'o-op}
                        (:static/tracker @scratch/Task)
                        rt.postgres.script.scratch/Task
                        :insert))
  => vector?)

^{:refer rt.postgres.script.impl-insert/t-insert :added "4.0"}
(fact "constructs an insert form"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (insert/t-insert 'scratch/Task
                     {:name "hello"
                      :status "pending"
                      :cache "id"}
                     {:track 'o-op}))
  => '[:with
       j-ret
       :as
       [:insert-into
        rt.postgres.script.scratch/Task
        (>-<
         [#{"status"}
          #{"name"}
          #{"cache_id"}
          #{"op_created"}
          #{"op_updated"}
          #{"time_created"}
          #{"time_updated"}])
        :values
        (>-<
         [(++ "pending" rt.postgres.script.scratch/EnumStatus)
          (:text "hello")
          (:uuid "id")
          (:uuid (:->> o-op "id"))
          (:uuid (:->> o-op "id"))
          (:bigint (:->> o-op "time"))
          (:bigint (:->> o-op "time"))])
        :returning
        (---
         [#{"id"}
          #{"status"}
          #{"name"}
          #{"cache_id"}
          #{"time_created"}
          #{"time_updated"}])]
       \\
       :select
       (to-jsonb j-ret)
       :from
       j-ret])

^{:refer rt.postgres.script.impl-insert/t-upsert-raw :added "4.0"}
(fact "contructs an upsert form with prep"
  ^:hidden
  
  (insert/t-upsert-raw
   (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
   {:name "hello"
    :status "pending"
    :cache "id"}
   (tracker/add-tracker {:track 'o-op}
                        (:static/tracker @scratch/Task)
                        rt.postgres.script.scratch/Task
                        :insert))
  => '[:with j-ret :as [:insert-into rt.postgres.script.scratch/Task (>-< [#{"status"} #{"name"} #{"cache_id"} #{"op_created"} #{"op_updated"} #{"time_created"} #{"time_updated"}]) :values (>-< [(++ "pending" rt.postgres.script.scratch/EnumStatus) (:text "hello") (:uuid "id") (:uuid (:->> o-op "id")) (:uuid (:->> o-op "id")) (:bigint (:->> o-op "time")) (:bigint (:->> o-op "time"))]) :on-conflict (quote (#{"id"})) :do-update :set (quote (#{"status"} #{"name"} #{"cache_id"} #{"op_updated"} #{"time_updated"})) := (row (. (:- "EXCLUDED") #{"status"}) (. (:- "EXCLUDED") #{"name"}) (. (:- "EXCLUDED") #{"cache_id"}) (. (:- "EXCLUDED") #{"op_updated"}) (. (:- "EXCLUDED") #{"time_updated"})) :returning (--- [#{"id"} #{"status"} #{"name"} #{"cache_id"} #{"time_created"} #{"time_updated"}])] \\ :select (to-jsonb j-ret) :from j-ret])

^{:refer rt.postgres.script.impl-insert/t-upsert :added "4.0"}
(fact "constructs an upsert form"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (insert/t-upsert 'scratch/Task
                     {:name "hello"
                      :status "pending"
                      :cache "id"}
                     {:track 'o-op}))
  => '[:with j-ret :as [:insert-into rt.postgres.script.scratch/Task (>-< [#{"status"} #{"name"} #{"cache_id"} #{"op_created"} #{"op_updated"} #{"time_created"} #{"time_updated"}]) :values (>-< [(++ "pending" rt.postgres.script.scratch/EnumStatus) (:text "hello") (:uuid "id") (:uuid (:->> o-op "id")) (:uuid (:->> o-op "id")) (:bigint (:->> o-op "time")) (:bigint (:->> o-op "time"))]) :on-conflict (quote (#{"id"})) :do-update :set (quote (#{"status"} #{"name"} #{"cache_id"} #{"op_updated"} #{"time_updated"})) := (row (. (:- "EXCLUDED") #{"status"}) (. (:- "EXCLUDED") #{"name"}) (. (:- "EXCLUDED") #{"cache_id"}) (. (:- "EXCLUDED") #{"op_updated"}) (. (:- "EXCLUDED") #{"time_updated"})) :returning (--- [#{"id"} #{"status"} #{"name"} #{"cache_id"} #{"time_created"} #{"time_updated"}])] \\ :select (to-jsonb j-ret) :from j-ret])
