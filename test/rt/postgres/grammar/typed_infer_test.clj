(ns rt.postgres.grammar.typed-infer-test
  (:require [rt.postgres.grammar.typed-infer :refer :all]
            [rt.postgres.grammar.typed-common :as types])
  (:use code.test))

(defn- entry-table
  []
  (types/make-table-def
   "demo"
   "Entry"
   [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid))
    (types/make-column-def :tags (types/make-type-ref :primitive nil :text))
    (types/make-column-def :name (types/make-type-ref :primitive nil :text))]
   :id))

^{:refer rt.postgres.grammar.typed-infer/select-shape-columns :added "4.1"}
(fact "select-shape-columns keeps only requested fields"
  (let [shape (types/make-jsonb-shape
               {:id {:type :uuid}
                :name {:type :text}}
               "Entry")]
    (get-in (select-shape-columns shape [:id])
            [:fields]) => {:id {:type :uuid}}
    (select-shape-columns shape nil) => shape))

^{:refer rt.postgres.grammar.typed-infer/resolve-table-def :added "4.1"}
(fact "resolve-table-def resolves table defs from the registry"
  (types/clear-registry!)
  (let [table (entry-table)]
    (types/register-type! 'demo/Entry table)
    (resolve-table-def 'Entry) => table
    (resolve-table-def :Entry) => table))

^{:refer rt.postgres.grammar.typed-infer/form-uses-tracked? :added "4.1"}
(fact "form-uses-tracked? detects tracked symbols anywhere in a form"
  (form-uses-tracked? '(let [x y] {:value x}) #{'y}) => true
  (form-uses-tracked? '(let [x z] {:value x}) #{'y}) => false)

^{:refer rt.postgres.grammar.typed-infer/form-uses-track-param? :added "4.1"}
(fact "form-uses-track-param? detects nested :track maps"
  (form-uses-track-param? '(pg/t:insert -/Entry {:track o-op}) #{'o-op}) => true
  (form-uses-track-param? '(pg/t:insert -/Entry {:set {:name o-op}}) #{'o-op}) => false)

^{:refer rt.postgres.grammar.typed-infer/find-table-op-in-body :added "4.1"}
(fact "find-table-op-in-body returns nil for a plain insert form"
  (find-table-op-in-body
   '((pg/t:insert -/Entry {:name x}))
   '-/Entry)
  => nil)

^{:refer rt.postgres.grammar.typed-infer/find-table-update-spec-in-body :added "4.1"}
(fact "find-table-update-spec-in-body returns the update spec"
  (find-table-update-spec-in-body
   '((pg/t:update -/Entry {:set {:tags x}
                           :columns [:tags]}))
   'x)
  => {:table '-/Entry
      :columns [:tags]
      :set {:tags 'x}
      :op 'pg/t:update})

^{:refer rt.postgres.grammar.typed-infer/find-table-track-spec-in-body :added "4.1"}
(fact "find-table-track-spec-in-body returns the track spec"
  (find-table-track-spec-in-body
   '((pg/t:insert -/Entry {:track o-op}))
   'o-op)
  => {:table '-/Entry
      :track 'o-op
      :op 'pg/t:insert})

^{:refer rt.postgres.grammar.typed-infer/infer-jsonb-arg-access-shape :added "4.1"}
(fact "infer-jsonb-arg-access-shape infers shapes from js-select access"
  (let [fn-def (types/make-fn-def
                "demo"
                "select-entry"
                [{:name 'm :type :jsonb :role :payload}]
                [:jsonb]
                {:raw-body '((js-select m (js ["id" "name"])))}
                nil)
        seed-shape (types/make-jsonb-shape
                    {:id {:type :uuid :nullable? false}
                     :name {:type :text :nullable? true}}
                    "Entry")]
    (get-in (infer-jsonb-arg-access-shape 'm fn-def seed-shape)
            [:fields :id :type]) => :uuid
    (get-in (infer-jsonb-arg-access-shape 'm fn-def seed-shape)
            [:fields :name :type]) => :text))

^{:refer rt.postgres.grammar.typed-infer/infer-jsonb-arg-table-shape* :added "4.1"}
(fact "infer-jsonb-arg-table-shape* projects table updates"
  (types/clear-registry!)
  (let [table (entry-table)
        fn-def (types/make-fn-def
                "demo"
                "update-entry"
                [{:name 'm :type :jsonb :role :payload}]
                [:jsonb]
                {:raw-body '((pg/t:update -/Entry {:set {:tags m}
                                                    :columns [:tags]}))}
                nil)]
    (types/register-type! '-/Entry table)
    (set (keys (:fields (infer-jsonb-arg-table-shape* 'm fn-def #{}))))
    => #{:tags}))

^{:refer rt.postgres.grammar.typed-infer/infer-jsonb-arg-shape :added "4.1"}
(fact "infer-jsonb-arg-shape returns the inferred table shape"
  (types/clear-registry!)
  (let [table (entry-table)
        fn-def (types/make-fn-def
                "demo"
                "update-entry"
                [{:name 'm :type :jsonb :role :payload}]
                [:jsonb]
                {:raw-body '((pg/t:update -/Entry {:set {:tags m}
                                                    :columns [:tags]}))}
                nil)]
    (types/register-type! '-/Entry table)
    (contains? (:fields (infer-jsonb-arg-shape 'm fn-def)) :tags) => true))

^{:refer rt.postgres.grammar.typed-infer/infer-jsonb-arg-shape* :added "4.1"}
(fact "infer-jsonb-arg-shape* respects :track and still returns shapes otherwise"
  (types/clear-registry!)
  (let [table (entry-table)
        fn-def (types/make-fn-def
                "demo"
                "update-entry"
                [{:name 'm :type :jsonb :role :payload}]
                [:jsonb]
                {:raw-body '((pg/t:update -/Entry {:set {:tags m}
                                                    :columns [:tags]}))}
                nil)]
    (types/register-type! '-/Entry table)
    (infer-jsonb-arg-shape* 'm fn-def #{} :track) => nil
    (contains? (:fields (infer-jsonb-arg-shape* 'm fn-def #{}))
               :tags) => true))
