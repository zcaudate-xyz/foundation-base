(ns code.framework.generate-test
  (:require [code.framework.generate :refer :all]
            [code.test :refer :all]))

(fact "get-template"
  (:params (get-template "(+ 1 ~a)"))
  => '[(unquote a)]

  (:params (get-template "(+ 1 ~@a)"))
  => '[(unquote-splicing a)])

(fact "fill-template"
  (fill-template (get-template "(+ 1 ~a)")
                 {'a 2})
  => "(+ 1 2)"

  (fill-template (get-template "(+ 1 ~@a)")
                 {'a [2 3]})
  => "(+ 1 2 3)")

(def Audit
  '(deftype.pg ^{:track {:key :audit}} Audit
     [:class-table {:type :text}
      :class-context {:type :text}
      :class-ref {:type :uuid}]
     {:partition-by {:strategy :list :columns [:class-table] :default {:in "szn_type_impl"}}}))

(fact "create-insert"
  (create-insert '-/Audit)
  => '(defn.pg ^{:%% :sql} create-audit "Creates a new audit entry to record system events or changes." {:added "0.1"}
        [:text i-class_table :text i-class_context :uuid i-class_ref :jsonb o-op]
        (rt.postgres.script.impl/t:insert -/Audit {:class-table i-class_table :class-context i-class_context :class-ref i-class_ref} {:track o-op})))

(fact "create-update"
  (create-update '-/Audit)
  => '(defn.pg ^{:%% :sql} create-audit-update "Updates the audit entry." {:added "0.1"}
        [:uuid id :text i-class_table :text i-class_context :uuid i-class_ref :jsonb o-op]
        (rt.postgres.script.impl/t:update -/Audit (clojure.core/merge {:set {:class-table i-class_table :class-context i-class_context :class-ref i-class_ref} :where {:id id}} {:track o-op}))))

(fact "create-purge"
  (create-purge '-/Audit)
  => '(defn.pg ^{:%% :sql} create-audit-purge "Purges the audit entry." {:added "0.1"}
        [:uuid id]
        (rt.postgres.script.impl/t:delete -/Audit {:where {:id id}})))
