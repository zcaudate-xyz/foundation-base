(ns rt.postgres.script.impl-base-test
  (:use code.test)
  (:require [rt.postgres.script.impl-base :refer :all]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres]
            [std.lang :as l]
            [std.lang.base.book :as book]
            [rt.postgres.script.scratch :as scratch]))

(l/script- :postgres
  {:require [[rt.postgres.script.scratch :as scratch]]
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

(deftype.pg Hello
  [:id   {:type :text :primary true}
   :task {:type :ref :ref {:ns scratch/Task}
          :profiles {:web  {:editable #{:create
                                        :modify}}}}])

^{:refer rt.postgres.script.impl-base/prep-entry :added "4.0"
  :setup [@Hello]}
(fact "prepares data given an entry sym"
  ^:hidden
  
  (second (prep-entry '-/Hello (l/rt:macro-opts :postgres)))
  => book/book-entry?)

^{:refer rt.postgres.script.impl-base/prep-table :added "4.0"}
(fact "prepares data related to the table sym"
  ^:hidden
  
  (prep-table '-/Hello false (l/rt:macro-opts :postgres))
  => vector?)

(def -tsch- (get-in (app/app "scratch")
                    [:schema
                     :tree
                     :Task]))

^{:refer rt.postgres.script.impl-base/t-input-check :guard true :added "4.0"}
(fact "passes the input if check is ok"
  ^:hidden
  
  (t-input-check (get-in (app/app "scratch")
                         [:schema
                          :tree
                          :Hello])
                 {:id "a"})
  => {:id "a"}

  (t-input-check (get-in (app/app "scratch")
                         [:schema
                          :tree
                          :Hello])
                 {})
  => (throws)

  (t-input-check (get-in (app/app "scratch")
                         [:schema
                          :tree
                          :Task])
                 '{:status a
                   :name "hello"
                   :cache hello})
  => map?)

^{:refer rt.postgres.script.impl-base/t-input-collect :added "4.0"}
(fact "adds schema info to keys of map"
  ^:hidden
  
  (t-input-collect -tsch-
                   '{:status a})
  => (contains {:status (contains ['a map?])}))

^{:refer rt.postgres.script.impl-base/t-val-process :added "4.0"}
(fact "allows additional filters to be added"
  ^:hidden

  (t-val-process [['add 5]
                  ['sub 5]]
                 'i)
  => '(sub (add i 5) 5))

^{:refer rt.postgres.script.impl-base/t-val-fn :added "4.0"}
(fact "builds a js val given input"
  ^:hidden
  
  (t-val-fn -tsch-
            :status 'a {} {})
  => '(++ a rt.postgres.script.scratch/EnumStatus)
  
  (t-val-fn -tsch-
            :cache 'a {}
            (last (prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => '(:uuid a))

^{:refer rt.postgres.script.impl-base/t-key-attrs-fn :added "4.0"}
(fact "builds a js key"
  ^:hidden
  
  (t-key-attrs-fn [:cache  (get -tsch- :cache)])
  => "cache_id"

  (t-key-attrs-fn [:id  (get -tsch- :id)])
  => "id")

^{:refer rt.postgres.script.impl-base/t-key-fn :added "4.0"}
(fact "builds a js key"
  ^:hidden
  
  (t-key-fn -tsch-
            :cache)
  => "cache_id"

  (t-key-fn -tsch-
            :id)
  => "id")

^{:refer rt.postgres.script.impl-base/t-sym-fn :added "4.0"}
(fact "builds a json access form"
  ^:hidden
  
  (t-sym-fn -tsch-
            :cache
            'e)
  => '(:uuid (coalesce (:->> e "cache_id") (:->> (:-> e "cache") "id")))
  
  (t-sym-fn -tsch-
            :id
            'e)
  => '(:uuid (:->> e "id")))

^{:refer rt.postgres.script.impl-base/t-build-js-map :added "4.0"}
(fact "builds a js map"
  ^:hidden
  
  (t-build-js-map -tsch-
                  (t-input-collect -tsch-
                                   '{:status a
                                     :name "hello"
                                     :cache hello})
                  {:coalesce true}
                  (last (prep-table 'scratch/Task true (l/rt:macro-opts :postgres))))
  => '(jsonb-build-object "id" (rt.postgres/uuid-generate-v4)
                          "status" (++ a rt.postgres.script.scratch/EnumStatus)
                          "name" (:text "hello") "cache_id" (:uuid hello)
                          "op_created" nil
                          "op_updated" nil
                          "time_created" nil
                          "time_updated" nil
                          "__deleted__" false))

^{:refer rt.postgres.script.impl-base/t-create-fn :added "4.0"}
(fact "the flat create-fn"
  ^:hidden
  
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (t-create-fn 'scratch/Task '{:status a
                                 :name "hello"
                                 :cache hello}
                 {:coalesce true}))
  => '(jsonb-build-object "id" (rt.postgres/uuid-generate-v4)
                          "status" (++ a rt.postgres.script.scratch/EnumStatus)
                          "name" (:text "hello") "cache_id" (:uuid hello)
                          "op_created" nil
                          "op_updated" nil
                          "time_created" nil
                          "time_updated" nil
                          "__deleted__" false))

^{:refer rt.postgres.script.impl-base/t-returning-cols :added "4.0"}
(fact "formats returning cols given"
  ^:hidden
  
  (t-returning-cols -tsch- 
                    [:*/data]
                    t-key-attrs-fn)
  => [#{"id"} #{"status"} #{"name"} #{"time_created"} #{"time_updated"}]

  (t-returning-cols -tsch- 
                    [:-/id]
                    t-key-attrs-fn)
  => [#{"id"}]

  (t-returning-cols -tsch- 
                    [:name :time-created]
                    t-key-attrs-fn)
  => [#{"name"} #{"time_created"}]

  (t-returning-cols -tsch- 
                    '[hello-id]
                    t-key-attrs-fn)
  => [#{"hello_id"}])

^{:refer rt.postgres.script.impl-base/t-returning :added "4.0"}
(fact "formats the returning expression"
  ^:hidden
  
  (t-returning -tsch- 
               #{:*/data})
  => '(--- [#{"id"} #{"status"} #{"name"} #{"time_created"} #{"time_updated"}])

  (t-returning -tsch- 
               :*/data)
  => '(--- [#{"id"} #{"status"} #{"name"} #{"time_created"} #{"time_updated"}])

  (t-returning -tsch- '*)
  => '*

  
  (t-returning -tsch- #{{:expr '(count *)}})
  => '(--- [(count *)])

  (t-returning -tsch- '#{:id {:expr (concat #{id} "-" #{time})
                              :as hello}})
  => '(--- [#{"id"} [(concat #{id} "-" #{time}) :as hello]]))

^{:refer rt.postgres.script.impl-base/t-where-hashvec-transform :added "4.0"}
(fact "transforms entries"
  ^:hidden
  
  (t-where-hashvec-transform [[:id 1] [:cache 1]] identity)
  => #{[:id 1 :or :cache 1]})

^{:refer rt.postgres.script.impl-base/t-where-hashvec :added "4.0"}
(fact "function for where transform"
  ^:hidden
  
  (t-where-hashvec #{[:simple-id [:eq 1]]}
                   identity)
  => #{[:simple-id [:eq 1]]}

  (t-where-hashvec #{[:simple 1
                      :hello 1
                      [:or]
                      :id 1]}
                   identity)
  => #{[:simple 1 :and :hello 1 :or :id 1]})

^{:refer rt.postgres.script.impl-base/t-where-transform :added "4.0"}
(fact "creates a where transform"
  ^:hidden
  
  (t-where-transform -tsch- {:id 1} {})
  => {"id" [:eq 1]}

  (t-where-transform -tsch- {:cache 1} {})
  => {"cache_id" [:eq 1]}

  (t-where-transform -tsch- #{[:id 1
                               :or
                               :cache 1]} {})
  => #{["id" [:eq 1] :or "cache_id" [:eq 1]]})

^{:refer rt.postgres.script.impl-base/t-wrap-json :added "4.0"}
(fact "wraps a json return to the statement"
  ^:hidden

  (t-wrap-json '<FORM> :json 'js-agg 'output nil)
  => '[:with j-ret :as <FORM> \\ :select (js-agg j-ret) :from j-ret]
  
  (t-wrap-json '<FORM> :json 'js-agg 'output :id)
  => '[:with j-ret :as <FORM> \\ :select (js-agg (. j-ret #{:id})) :from j-ret]

  (t-wrap-json '<FORM> :record 'js-agg 'output nil)
  => '[:with j-ret :as <FORM> \\ :select (js-agg j-ret) :from j-ret])

^{:refer rt.postgres.script.impl-base/t-wrap-where :added "4.0"}
(fact "adds a `where` clause"
  ^:hidden
  
  (t-wrap-where []
                {:id 1}
                -tsch-
                {}
                {})
  => '[:where {"id" [:eq 1]}])

^{:refer rt.postgres.script.impl-base/t-wrap-order-by :added "4.0"}
(fact "adds an `order-by` clause"
  ^:hidden
  
  (t-wrap-order-by []
                   [:id :status]
                   -tsch-
                   {})
  => '[:order-by (quote [#{"id"} #{"status"}])])

^{:refer rt.postgres.script.impl-base/t-wrap-order-sort :added "4.0"}
(fact "determines asc or desc key"
  ^:hidden
  
  (t-wrap-order-sort []
                     :asc
                     -tsch-
                     {})
  => '[:asc])

^{:refer rt.postgres.script.impl-base/t-wrap-limit :added "4.0"}
(fact "adds a `limit` clause"
  ^:hidden
  
  (t-wrap-limit [] 10 {})
  => [:limit 10])

^{:refer rt.postgres.script.impl-base/t-wrap-offset :added "4.0"}
(fact "adds a `offset` clause"
  ^:hidden
  
  (t-wrap-offset [] 10 {})
  => [:offset 10])

^{:refer rt.postgres.script.impl-base/t-wrap-into :added "4.0"}
(fact "adds `into` clause"
  ^:hidden
  
  (t-wrap-into [] 'hello {})
  => '[:into hello])

^{:refer rt.postgres.script.impl-base/t-wrap-returning :added "4.0"}
(fact "adds `returning` clause"
  ^:hidden
  
  (t-wrap-returning [] [#{"id"} #{"status"}] {})
  => [:returning [#{"id"} #{"status"}]])

^{:refer rt.postgres.script.impl-base/t-wrap-group-by :added "4.0"}
(fact "adds `group-by` clause"
  ^:hidden
  
  (t-wrap-group-by [] [#{"id"} #{"status"}] {})
  => [:group-by [#{"id"} #{"status"}]])

^{:refer rt.postgres.script.impl-base/t-wrap-args :added "4.0"}
(fact "adds `additional` args"
  (t-wrap-args [] [:a] {})
  => [:a])

^{:refer rt.postgres.script.impl-base/t-returning-cols-default :added "4.0"}
(fact "formats returning cols default"
  (t-returning-cols-default [:id] t-key-attrs-fn)
  => vector?)
