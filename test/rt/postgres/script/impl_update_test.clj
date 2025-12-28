(ns rt.postgres.script.impl-update-test
  (:use code.test)
  (:require [rt.postgres.script.impl-base :as base]
            [rt.postgres.script.impl-update :as update]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres]
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

^{:refer rt.postgres.script.impl-update/t-update-symbol :added "4.0"}
(fact "constructs with symbol"
  ^:hidden
  
  (update/t-update-symbol -tsch-
                          'e
                          (tracker/add-tracker {:set {:status "error"}
                                                :columns [:id :status :name :cache]
                                                :track 'o-op}
                                               (:static/tracker @scratch/Task)
                                               rt.postgres.script.scratch/Task
                                               :insert)
                          (last (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))))
  => '(--- [(== #{"id"} (coalesce (:uuid (:->> e "id")) #{"id"}))
            (== #{"status"} (coalesce (++ (:->> e "status")
                                          rt.postgres.script.scratch/EnumStatus)
                                      #{"status"}))
            (== #{"name"} (coalesce (:text (:->> e "name")) #{"name"}))
            (== #{"cache_id"} (coalesce (:uuid (coalesce (:->> e "cache_id")
                                                         (:->> (:-> e "cache") "id")))
                                        #{"cache_id"}))
            (== #{"op_updated"} (:uuid (:->> o-op "id")))
            (== #{"time_updated"} (:bigint (:->> o-op "time")))])
  
  
  (update/t-update-symbol -tsch-
                          'e
                          (tracker/add-tracker {:set {:status "error"}
                                                :track 'o-op}
                                               (:static/tracker @scratch/Task)
                                               rt.postgres.script.scratch/Task
                                               :insert)
                          (last (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))))
  => '(--- [(== #{"status"} (coalesce (++ (:->> e "status")
                                          rt.postgres.script.scratch/EnumStatus)
                                      #{"status"}))
            (== #{"name"} (coalesce (:text (:->> e "name")) #{"name"}))
            (== #{"cache_id"} (coalesce (:uuid (coalesce (:->> e "cache_id")
                                                         (:->> (:-> e "cache") "id")))
                                        #{"cache_id"}))
            (== #{"op_created"} (coalesce (:uuid (:->> e "op_created")) #{"op_created"}))
            (== #{"op_updated"} (:uuid (:->> o-op "id")))
            (== #{"time_created"} (coalesce (:bigint (:->> e "time_created"))
                                            #{"time_created"}))
            (== #{"time_updated"} (:bigint (:->> o-op "time")))
            (== #{"__deleted__"} (coalesce (:boolean (:->> e "__deleted__")) #{"__deleted__"}))]))


^{:refer rt.postgres.script.impl-update/t-update-map :added "4.0"}
(fact  "constructs with map"
  ^:hidden

  (update/t-update-map -tsch-
                       {:status "error"}
                       (tracker/add-tracker {:track 'o-op}
                                            (:static/tracker @scratch/Task)
                                            rt.postgres.script.scratch/Task
                                            :insert)
                       (last (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))))
  => '(--- [(== #{"status"} (++ "error" rt.postgres.script.scratch/EnumStatus))
             (== #{"op_updated"} (:uuid (:->> o-op "id")))
             (== #{"time_updated"} (:bigint (:->> o-op "time")))])

  (fact "t-update-map handles composite primary key reference"
    (with-redefs [book/get-base-entry (fn [_ _ _ _]
                                        {:static/schema-primary [{:id :id :type :uuid}
                                                                 {:id :prospect_id :type :ref}]})
                  base/prep-table (fn [& _] [nil {:k [{:type :ref :ref {:link {:id :tbl}}}]} {}])
                  base/t-key-fn (fn [_ k] k)]
      (update/t-update-map {:k [{:type :ref :ref {:link {:id :tbl}} :order 1}]}
                           {:k "some-uuid"}
                           {}
                           {:book {}}))
    => (list '--- [(list '== #{:k} '(:uuid "some-uuid"))])))

^{:refer rt.postgres.script.impl-update/t-update-raw :added "4.0"}
(fact "contructs an update form with prep"
  ^:hidden
  
  (update/t-update-raw
   (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
   (tracker/add-tracker {:set {:status "error"}
                         :track 'o-op}
                        (:static/tracker @scratch/Task)
                        rt.postgres.script.scratch/Task
                        :insert))
  => '[:with j-ret
       :as [:update
            rt.postgres.script.scratch/Task
            :set
            (--- [(== #{"status"} (++ "error" rt.postgres.script.scratch/EnumStatus))
                   (== #{"op_updated"} (:uuid (:->> o-op "id")))
                   (== #{"time_updated"} (:bigint (:->> o-op "time")))])
            \\
            :returning
            (--- [#{"id"}
                   #{"status"}
                   #{"name"}
                   #{"cache_id"}
                   #{"time_created"}
                  #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.script.impl-update/t-update :added "4.0"}
(fact "contructs an update form"
  ^:hidden

  ;; MAP
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (update/t-update 'scratch/Task
                     {:set {:status "error"}
                      :track 'o-op}))
  => '[:with j-ret :as
       [:update rt.postgres.script.scratch/Task
        :set
        (--- [(== #{"status"} (++ "error" rt.postgres.script.scratch/EnumStatus))
              (== #{"op_updated"} (:uuid (:->> o-op "id")))
              (== #{"time_updated"} (:bigint (:->> o-op "time")))])
        \\ :returning (--- [#{"id"} #{"status"} #{"name"} #{"cache_id"}
                            #{"time_created"} #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret]

  ;; SYMBOL
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (update/t-update 'scratch/Task
                     {:set 'e
                      :columns [:status :name :cache]
                      :track 'o-op}))

  => '[:with j-ret :as
       [:update rt.postgres.script.scratch/Task
        :set
        (--- [(== #{"status"} (coalesce (++ (:->> e "status")
                                            rt.postgres.script.scratch/EnumStatus)
                                        #{"status"}))
              (== #{"name"} (coalesce (:text (:->> e "name")) #{"name"}))
              (== #{"cache_id"} (coalesce (:uuid (coalesce (:->> e "cache_id")
                                                           (:->> (:-> e "cache") "id")))
                                          #{"cache_id"}))
              (== #{"op_updated"} (:uuid (:->> o-op "id")))
              (== #{"time_updated"} (:bigint (:->> o-op "time")))])
        \\ :returning (--- [#{"id"} #{"status"} #{"name"} #{"cache_id"}
                            #{"time_created"} #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret]

  ;; SYMBOL, COALESCE FALSE
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (update/t-update 'scratch/Task
                     {:set 'e
                      :coalesce false
                      :columns [:status :name :cache]
                      :track 'o-op}))
  => '[:with j-ret :as
       [:update rt.postgres.script.scratch/Task
        :set
        (--- [(== #{"status"} (++ (:->> e "status")
                                  rt.postgres.script.scratch/EnumStatus))
              (== #{"name"} (:text (:->> e "name")))
              (== #{"cache_id"} (:uuid (coalesce (:->> e "cache_id")
                                                 (:->> (:-> e "cache") "id"))))
              (== #{"op_updated"} (:uuid (:->> o-op "id")))
              (== #{"time_updated"} (:bigint (:->> o-op "time")))])
        \\ :returning (--- [#{"id"} #{"status"} #{"name"}
                            #{"cache_id"} #{"time_created"} #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.script.impl-update/t-modify-raw :added "4.0"}
(fact "contructs an upsert form with prep"
  ^:hidden
  
  (update/t-modify-raw
   (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
   (tracker/add-tracker {:set {:status "error"}
                         :where {:id "A"}
                         :track 'o-op}
                        (:static/tracker @scratch/Task)
                        rt.postgres.script.scratch/Task
                        :insert))
  => '(let [(++ u-ret rt.postgres.script.scratch/Task)
            [:update rt.postgres.script.scratch/Task
                                                        :set
             (--- [(==
                    #{"status"}
                    (++ "error" rt.postgres.script.scratch/EnumStatus))
                   (== #{"op_updated"} (:uuid (:->> o-op "id")))
                   (== #{"time_updated"} (:bigint (:->> o-op "time")))])
                                                        :where {"id" [:eq "A"]}
                                                        \\ :returning * :into u-ret]
            _ (if [:not (exists [:select u-ret])]
                [:raise-exception (% "Record Not Found")])]))

^{:refer rt.postgres.script.impl-update/t-modify :added "4.0"}
(fact "constructs a modify form"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (update/t-modify 'scratch/Task
                     {:set {:status "error"}
                      :where {:id "A"}
                      :track 'o-op}))
  => '(let [(++ u-ret rt.postgres.script.scratch/Task)
            [:update rt.postgres.script.scratch/Task
             :set
             
             (---
              [(==
                #{"status"}
                (++ "error" rt.postgres.script.scratch/EnumStatus))
               (== #{"op_updated"} (:uuid (:->> o-op "id")))
               (== #{"time_updated"} (:bigint (:->> o-op "time")))])
             :where {"id" [:eq "A"]}
             \\ :returning * :into u-ret]
            _ (if [:not (exists [:select u-ret])]
                [:raise-exception (% "Record Not Found")])])
  
  ;; assign/fn
  ((-> (l/with:macro-opts [(l/rt:macro-opts :postgres)]
         (update/t-modify 'scratch/Task
                          {:set {:status "error"}
                           :returning #{:status}
                           :where {:id "A"}
                           :track 'o-op}))
       meta
       :assign/fn)
   'o-task)
  => '(let [(++ u-ret rt.postgres.script.scratch/Task)
            [:update rt.postgres.script.scratch/Task
             :set
             (--- [(==
                     #{"status"}
                     (++ "error" rt.postgres.script.scratch/EnumStatus))
                    (== #{"op_updated"} (:uuid (:->> o-op "id")))
                    (== #{"time_updated"} (:bigint (:->> o-op "time")))])
             :where {"id" [:eq "A"]}
             \\ :returning (--- [#{"status"}]) :into u-ret]
            _ (if [:not (exists [:select u-ret])]
                [:raise-exception (% "Record Not Found")])]
        [:select (to-jsonb u-ret) :into o-task]))
