(ns rt.postgres.infer.generate-test
  "Tests for rt.postgres.infer.generate namespace.
   Provides OpenAPI, JSON Schema, and TypeScript generation."
  (:use code.test)
  (:require [rt.postgres.infer.generate :as generate]
            [rt.postgres.infer.parse :as parse]
            [rt.postgres.infer.types :as types]
            [rt.postgres.infer.shape :as shape]))

;; -----------------------------------------------------------------------------
;; OpenAPI Schema Generation Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.generate/shape->openapi :added "0.1"}
(fact "shape->openapi converts JsonbShape to OpenAPI schema"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}
                                        :name {:type :text :nullable? true}}
                                        :User)
        result (generate/shape->openapi shape)]
    (:type result) => "object"
    (contains? (:properties result) "id") => true
    (contains? (:properties result) "name") => true
    (:required result) => ["id"]))

^{:refer rt.postgres.infer.generate/shape->openapi :added "0.1"}
(fact "shape->openapi handles various primitive types"
  (let [shape (types/make-jsonb-shape {:s {:type :text}
                                        :n {:type :integer}
                                        :f {:type :numeric}
                                        :b {:type :boolean}
                                        :j {:type :jsonb}}
                                        :Test)
        result (generate/shape->openapi shape)]
    (get-in result [:properties "s" :type]) => "string"
    (get-in result [:properties "n" :type]) => "integer"
    (get-in result [:properties "f" :type]) => "number"
    (get-in result [:properties "b" :type]) => "boolean"
    (get-in result [:properties "j" :type]) => "object"))

^{:refer rt.postgres.infer.generate/fn->openapi :added "0.1"}
(fact "fn->openapi generates OpenAPI operation from FnDef"
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
        task-table (types/make-table-def "test.ns" "Task" task-columns :id nil nil)]
    (types/register-type! 'Task task-table)
    (types/register-type! '-/Task task-table)
    ;; Parse function
    (let [form '(defn.pg ^{:%% :sql :- Task}
                  insert-task-raw
                  "inserts a task"
                  [:jsonb m :jsonb o-op]
                  (let [o-out (pg/t:insert Task m {:track o-op})]
                    (return o-out)))
          fn-def (parse/parse-defn form "test.ns" nil)
          _ (types/register-type! (symbol "test.ns" "insert-task-raw") fn-def)
          openapi (generate/fn->openapi fn-def)
          request-schema (get-in openapi [:requestBody :content "application/json" :schema])
          m-schema (get-in request-schema [:properties "m"])]
      ;; Verify m has Task shape
      (:type m-schema) => "object"
      (contains? (:properties m-schema) "id") => true
      (contains? (:properties m-schema) "status") => true
      (contains? (:properties m-schema) "name") => true)))

^{:refer rt.postgres.infer.generate/fn->openapi :added "0.1"}
(fact "fn->openapi uses output field for response schema - TABLE REF"
  (let [form '(defn.pg ^{:%% :sql :- Entry}
                insert-entry
                "inserts an entry"
                [:text i-name :jsonb i-tags]
                (pg/t:insert Entry {:name i-name :tags i-tags}))
        fn-def (parse/parse-defn form "test.ns" nil)
        openapi (generate/fn->openapi fn-def)
        response-schema (get-in openapi [:responses "200" :content "application/json" :schema])]
    ;; Returns reference to Entry schema
    response-schema => {:$ref "#/components/schemas/Entry"}))

^{:refer rt.postgres.infer.generate/fn->openapi :added "0.1"}
(fact "fn->openapi filters out o-op from request body"
  (let [form '(defn.pg ^{:%% :sql :- Task}
                insert-task
                "inserts a task"
                [:text i-name :jsonb o-op]
                (let [o-out (pg/t:insert Task {:name i-name} {:track o-op})]
                  (return o-op)))
        fn-def (parse/parse-defn form "test.ns" nil)
        openapi (generate/fn->openapi fn-def)
        request-schema (get-in openapi [:requestBody :content "application/json" :schema])]
    ;; o-op should NOT appear in inputs (stripped prefix becomes "op")
    (contains? (:properties request-schema) "op") => false))

^{:refer rt.postgres.infer.generate/generate-openapi :added "0.1"}
(fact "generate-openapi creates full OpenAPI spec from namespace"
  (let [spec (generate/generate-openapi 'rt.postgres.infer.types-test (constantly true))]
    (contains? spec :openapi) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true))

;; -----------------------------------------------------------------------------
;; JSON Schema Generation Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.generate/shape->jschema :added "0.1"}
(fact "shape->jschema generates JSON Schema from shape"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid}
                                        :name {:type :text}}
                                        :User)
        result (generate/shape->jschema shape)]
    (:type result) => "object"
    (contains? (:properties result) "id") => true
    (contains? (:properties result) "name") => true))

^{:refer rt.postgres.infer.generate/generate-jschema :added "0.1"}
(fact "generate-jschema creates JSON Schema for all types"
  (let [schemas (generate/generate-jschema)]
    (map? schemas) => true))

;; -----------------------------------------------------------------------------
;; TypeScript Generation Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.generate/shape->ts-interface :added "0.1"}
(fact "shape->ts-interface generates TypeScript interface"
  (let [shape (types/make-jsonb-shape {:id {:type :uuid :nullable? false}
                                        :name {:type :text :nullable? true}}
                                        :User)
        result (generate/shape->ts-interface shape "IUser")]
    (clojure.string/includes? result "interface IUser") => true
    (clojure.string/includes? result "id: string") => true
    (clojure.string/includes? result "name: string | null") => true))

^{:refer rt.postgres.infer.generate/shape->ts-interface :added "0.1"}
(fact "shape->ts-interface handles various types"
  (let [shape (types/make-jsonb-shape {:active {:type :boolean}
                                        :count {:type :integer}
                                        :amount {:type :numeric}
                                        :data {:type :jsonb}}
                                        :Test)
        result (generate/shape->ts-interface shape "ITest")]
    (clojure.string/includes? result "active: boolean") => true
    (clojure.string/includes? result "count: number") => true
    (clojure.string/includes? result "amount: number") => true
    (clojure.string/includes? result "data: Record<string, unknown>") => true))

^{:refer rt.postgres.infer.generate/shape->ts-interface :added "0.1"}
(fact "shape->ts-interface uses snake_case keys"
  (let [shape (types/make-jsonb-shape {:time-created {:type :bigint :nullable? true}
                                        :op-created {:type :uuid :nullable? true}}
                                        :User)
        result (generate/shape->ts-interface shape "IUser")]
    (clojure.string/includes? result "time_created") => true
    (clojure.string/includes? result "op_created") => true
    ;; Should NOT have kebab-case
    (clojure.string/includes? result "time-created") => false))

^{:refer rt.postgres.infer.generate/generate-typescript :added "0.1"}
(fact "generate-typescript creates TypeScript interfaces for all types"
  (let [ts-code (generate/generate-typescript)]
    (string? ts-code) => true))

;; -----------------------------------------------------------------------------
;; Emit Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.generate/emit :added "0.1"}
(fact "emit generates output in specified format"
  (let [openapi (generate/emit 'rt.postgres.infer.types-test :openapi {})]
    (contains? openapi :openapi) => true)
  (let [jschema (generate/emit 'rt.postgres.infer.types-test :jschema {})]
    (map? jschema) => true)
  (let [typescript (generate/emit 'rt.postgres.infer.types-test :typescript {})]
    (string? typescript) => true))

^{:refer rt.postgres.infer.generate/emit :added "0.1"}
(fact "emit throws for unknown format"
  (generate/emit 'rt.postgres.infer.types-test :unknown {}) 
  => (throws Exception))
