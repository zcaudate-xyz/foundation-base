(ns rt.postgres.script.impl-main-test
  (:use code.test)
  (:require [rt.postgres.script.impl-base :as base]
            [rt.postgres.script.impl-main :as main]
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

^{:refer rt.postgres.script.impl-main/t-select-raw :added "4.0"}
(fact "contructs an select form with prep"
  ^:hidden
  
  (main/t-select-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                     {})
  => '[:with j-ret :as
       [:select
        (--- [#{"id"}
              #{"status"}
              #{"name"}
              #{"cache_id"}
              #{"time_created"}
              #{"time_updated"}])
        :from
        rt.postgres.script.scratch/Task]
       \\ :select (jsonb-agg j-ret) :from j-ret]

  (main/t-select-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                     {:returning #{{:expr '(count *)}
                                   {:expr '(count abc)}}})
  => '[:with j-ret :as [:select (--- [(count *)
                                      (count abc)])
                        :from rt.postgres.script.scratch/Task]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.script.impl-main/t-select :added "4.0"}
(fact "contructs an select form with prep"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-select 'scratch/Task
                   {:as :raw}))
  => '[:select * :from rt.postgres.script.scratch/Task]
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-select 'scratch/Task
                   {:as :record}))
  => '[:select
       (--- [#{"id"}
             #{"status"}
             #{"name"}
             #{"cache_id"}
             #{"time_created"}
             #{"time_updated"}])
       :from
       rt.postgres.script.scratch/Task])

^{:refer rt.postgres.script.impl-main/t-id-raw :added "4.0"}
(fact  "contructs an id form with prep"
  ^:hidden
  
  (main/t-id-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                 {})
  => '[:select (--- [#{"id"}]) :from rt.postgres.script.scratch/Task
       \\ :limit 1])

^{:refer rt.postgres.script.impl-main/t-id :added "4.0"}
(fact "contructs an id form"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-id 'scratch/Task
               {}))
  => '[:select (--- [#{"id"}]) :from rt.postgres.script.scratch/Task
       \\ :limit 1])

^{:refer rt.postgres.script.impl-main/t-count-raw :added "4.0"}
(fact "constructs a count form with prep"
  ^:hidden
  
  (main/t-count-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                    {})
  => '[:select (count *) :from rt.postgres.script.scratch/Task])

^{:refer rt.postgres.script.impl-main/t-count :added "4.0"}
(fact "create count statement"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-count 'scratch/Task
                  {}))
  => '[:select (count *) :from rt.postgres.script.scratch/Task])

^{:refer rt.postgres.script.impl-main/t-exists-raw :added "4.0"}
(fact "constructs a exists form with prep"
  ^:hidden
  
  (main/t-exists-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                    {})
  => '[:select (exists *) :from rt.postgres.script.scratch/Task])

^{:refer rt.postgres.script.impl-main/t-exists :added "4.0"}
(fact "create exists statement"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-exists 'scratch/Task
                  {}))
  => '[:select (exists *) :from rt.postgres.script.scratch/Task])

^{:refer rt.postgres.script.impl-main/t-delete-raw :added "4.0"}
(fact  "contructs a delete form with prep"
  ^:hidden

  (main/t-delete-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                     {})
  => '[:with j-ret :as
       [:delete :from rt.postgres.script.scratch/Task
        \\ :returning (--- [#{"id"}
                            #{"status"}
                            #{"name"}
                            #{"cache_id"}
                            #{"op_created"}
                            #{"op_updated"}
                            #{"time_created"}
                            #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.script.impl-main/t-delete :added "4.0"}
(fact  "contructs an delete form"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-delete 'scratch/Task
                   {}))
  => '[:with j-ret :as
       [:delete :from rt.postgres.script.scratch/Task
        \\ :returning (--- [#{"id"}
                             #{"status"}
                             #{"name"}
                             #{"cache_id"}
                             #{"op_created"}
                             #{"op_updated"}
                             #{"time_created"}
                             #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret])


^{:refer rt.postgres.script.impl-main/t-fields-raw :added "4.0"}
(fact "returns the raw fields"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-fields 'scratch/Task
                   {}))
  => '(status name cache op_created op_updated time_created time_updated __deleted__))

^{:refer rt.postgres.script.impl-main/t-fields :added "4.0"}
(fact "returns fields"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-fields 'scratch/Task
                   {}))
  => '(status name cache op_created op_updated time_created time_updated __deleted__))
