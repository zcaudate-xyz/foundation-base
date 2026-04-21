(ns rt.postgres.runtime.impl-main-test
  (:require [rt.postgres]
            [rt.postgres.base.application :as app]
            [rt.postgres.base.grammar.common-tracker :as tracker]
            [rt.postgres.runtime.impl-base :as base]
            [rt.postgres.runtime.impl-main :as main]
            [std.lang :as l]
            [std.lang.base.book :as book])
  (:use code.test))

(l/script- :postgres
  {:require [[rt.postgres.test.scratch-v1 :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

(def -tsch- (get-in (app/app "scratch")
                    [:schema
                     :tree
                     :Task]))

^{:refer rt.postgres.runtime.impl-main/t-select-raw :added "4.0"}
(fact "contructs an select form with prep"

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
        rt.postgres.test.scratch-v1/Task]
       \\ :select (jsonb-agg j-ret) :from j-ret]

  (main/t-select-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                     {:returning #{{:expr '(count *)}
                                   {:expr '(count abc)}}})
  => '[:with j-ret :as [:select (--- [(count *)
                                      (count abc)])
                        :from rt.postgres.test.scratch-v1/Task]
       \\ :select (jsonb-agg j-ret) :from j-ret]

  (main/t-select-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                     {:join [[:inner-join 'rt.postgres.test.scratch-v1/Project
                              {:on [:= 'rt.postgres.test.scratch-v1/Task.id 'rt.postgres.test.scratch-v1/Project.id]}]]})
  => '[:with j-ret :as
       [:select
        (--- [#{"id"}
              #{"status"}
              #{"name"}
              #{"cache_id"}
              #{"time_created"}
              #{"time_updated"}])
        :from
        rt.postgres.test.scratch-v1/Task
        \\
        [:inner-join
         rt.postgres.test.scratch-v1/Project
         {:on
          [:=
           rt.postgres.test.scratch-v1/Task.id
           rt.postgres.test.scratch-v1/Project.id]}]]
       \\
       :select
       (jsonb-agg j-ret)
       :from
       j-ret]

  (main/t-select-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                     {:having {:id 1}})
  => '[:with j-ret :as
       [:select
        (--- [#{"id"}
              #{"status"}
              #{"name"}
              #{"cache_id"}
              #{"time_created"}
              #{"time_updated"}])
        :from
        rt.postgres.test.scratch-v1/Task
        \\
        :having
        {"id" [:eq 1]}]
       \\
       :select
       (jsonb-agg j-ret)
       :from
       j-ret])

^{:refer rt.postgres.runtime.impl-main/t-select :added "4.0"}
(fact "contructs an select form with prep"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-select 'scratch/Task
                   {:as :raw}))
  => '[:select * :from rt.postgres.test.scratch-v1/Task]

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
       rt.postgres.test.scratch-v1/Task])

^{:refer rt.postgres.runtime.impl-main/t-id-raw :added "4.0"}
(fact  "contructs an id form with prep"

  (main/t-id-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                 {})
  => '[:select (--- [#{"id"}]) :from rt.postgres.test.scratch-v1/Task
       \\ :limit 1])

^{:refer rt.postgres.runtime.impl-main/t-id :added "4.0"}
(fact "contructs an id form"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-id 'scratch/Task
               {}))
  => '[:select (--- [#{"id"}]) :from rt.postgres.test.scratch-v1/Task
       \\ :limit 1])

^{:refer rt.postgres.runtime.impl-main/t-count-raw :added "4.0"}
(fact "constructs a count form with prep"

  (main/t-count-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                    {})
  => '[:select (count *) :from rt.postgres.test.scratch-v1/Task])

^{:refer rt.postgres.runtime.impl-main/t-count :added "4.0"}
(fact "create count statement"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-count 'scratch/Task
                  {}))
  => '[:select (count *) :from rt.postgres.test.scratch-v1/Task])

^{:refer rt.postgres.runtime.impl-main/t-exists-raw :added "4.0"}
(fact "constructs a exists form with prep"

  (main/t-exists-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                    {})
  => '[:select (exists [:select 1 :from rt.postgres.test.scratch-v1/Task])])

^{:refer rt.postgres.runtime.impl-main/t-exists :added "4.0"}
(fact "create exists statement"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-exists 'scratch/Task
                  {}))
  => '[:select (exists [:select 1 :from rt.postgres.test.scratch-v1/Task])])

^{:refer rt.postgres.runtime.impl-main/t-delete-raw :added "4.0"}
(fact  "contructs a delete form with prep"

  (main/t-delete-raw (base/prep-table 'scratch/Task false (l/rt:macro-opts :postgres))
                     {})
  => '[:with j-ret :as
       [:delete :from rt.postgres.test.scratch-v1/Task
        \\ :returning (--- [#{"id"}
                            #{"status"}
                            #{"name"}
                            #{"cache_id"}
                            #{"op_created"}
                            #{"op_updated"}
                            #{"time_created"}
                            #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret])

^{:refer rt.postgres.runtime.impl-main/t-delete :added "4.0"}
(fact  "contructs an delete form"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-delete 'scratch/Task
                   {}))
  => '[:with j-ret :as
       [:delete :from rt.postgres.test.scratch-v1/Task
        \\ :returning (--- [#{"id"}
                             #{"status"}
                             #{"name"}
                             #{"cache_id"}
                             #{"op_created"}
                             #{"op_updated"}
                             #{"time_created"}
                             #{"time_updated"}])]
       \\ :select (jsonb-agg j-ret) :from j-ret])


^{:refer rt.postgres.runtime.impl-main/t-fields-raw :added "4.0"}
(fact "returns the raw fields"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-fields 'scratch/Task
                   {}))
  => '(status name cache op_created op_updated time_created time_updated __deleted__))

^{:refer rt.postgres.runtime.impl-main/t-fields :added "4.0"}
(fact "returns fields"

  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (main/t-fields 'scratch/Task
                   {}))
  => '(status name cache op_created op_updated time_created time_updated __deleted__))
