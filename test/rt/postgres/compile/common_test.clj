(ns rt.postgres.compile.common-test
  (:require [rt.postgres.compile.common :as compile.common]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.grammar.typed-parse :as parse])
  (:use code.test))

;; -----------------------------------------------------------------------------
;; Shared Compile Helpers
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.compile.common/infer-jsonb-arg-shape :added "0.1"}
(fact "infer-jsonb-arg-shape traces jsonb args to target table shapes"
  ;; Clear and setup
  (types/clear-registry!)
  (let [task-columns [(types/make-column-def :id
                                             (types/make-type-ref :primitive nil :uuid)
                                             {:required true :primary true})
                      (types/make-column-def :status
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})
                      (types/make-column-def :name
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})]
        task-table (types/make-table-def "test.ns" "Task" task-columns :id)]
    (types/register-type! 'test.ns/Task task-table)
    ;; Parse function
    (let [form '(defn.pg ^{:%% :sql :- Task}
                  insert-task-raw
                  "inserts a task"
                  [:jsonb m :jsonb o-op]
                  (let [o-out (pg/t:insert Task m {:track o-op})]
                    (return o-out)))
          fn-def (parse/parse-defn form "test.ns" nil)
          _ (types/register-type! (symbol "test.ns" "insert-task-raw") fn-def)
          inferred (compile.common/infer-jsonb-arg-shape 'm fn-def)]
      (:source-table inferred) => "Task"
      (-> inferred :fields keys set) => #{:id :status :name})))

^{:refer rt.postgres.compile.common/infer-jsonb-arg-shape :added "4.1"}
(fact "infer-jsonb-arg-shape follows derived payload vars through helper calls"
  (types/clear-registry!)
  (let [task-columns [(types/make-column-def :id
                                             (types/make-type-ref :primitive nil :uuid)
                                             {:required true :primary true})
                      (types/make-column-def :status
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})
                      (types/make-column-def :name
                                             (types/make-type-ref :primitive nil :text)
                                             {:required true})]
        task-table (types/make-table-def "test.ns" "Task" task-columns :id)
        prep-form '(defn.pg
                     prepare-task
                     "prepares a task payload"
                     [:jsonb m]
                     (return (merge m {:status "pending"})))
        insert-form '(defn.pg ^{:%% :sql :- Task}
                       insert-task
                       "inserts a prepared task"
                       [:jsonb m]
                       (let [v-prep (prepare-task m)
                             v-input (merge v-prep {:name "demo"})]
                         (pg/t:insert Task v-input)))
        prep-def (parse/parse-defn prep-form "test.ns" nil)
        insert-def (parse/parse-defn insert-form "test.ns" nil)]
    (types/register-type! 'test.ns/Task task-table)
    (types/register-type! 'test.ns/prepare-task prep-def)
    (types/register-type! 'test.ns/insert-task insert-def)
    (let [inferred (compile.common/infer-jsonb-arg-shape 'm insert-def)]
      (:source-table inferred) => "Task"
      (-> inferred :fields keys set) => #{:id :status :name})))
