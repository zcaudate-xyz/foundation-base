(ns rt.postgres.infer-test
  "Integration tests for rt.postgres.infer namespace.
   Tests end-to-end OpenAPI generation from namespace analysis."
  (:use code.test)
  (:require [rt.postgres.infer :as infer]
            [rt.postgres.infer.generate :as generate]
            [rt.postgres.infer.types :as types]
            [rt.postgres.script.test.scratch-v2 :as scratch]))

;; -----------------------------------------------------------------------------
;; OpenAPI Generation Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.generate/generate-openapi :added "0.1"}
(fact "generate-openapi generates correct OpenAPI spec for rt.postgres.script.test.scratch-v2"
  (let [spec (generate/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    ;; Check that paths are generated
    (map? paths) => true
    (not (empty? paths)) => true))

^{:refer rt.postgres.infer.generate/generate-openapi :added "0.1"}
(fact "generate-openapi generates valid OpenAPI structure"
  (let [spec (generate/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))]
    ;; Top-level required fields
    (contains? spec :openapi) => true
    (contains? spec :info) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true
    ;; Components structure
    (contains? (:components spec) :schemas) => true
    ;; Paths should be a map
    (map? (:paths spec)) => true))

^{:refer rt.postgres.infer.generate/generate-openapi :added "0.1"}
(fact "generate-openapi includes schemas in components"
  (let [spec (generate/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        schemas (get-in spec [:components :schemas])]
    (map? schemas) => true))

^{:refer rt.postgres.infer.generate/generate-openapi :added "0.1"}
(fact "generate-openapi filters paths based on predicate"
  (let [spec (generate/generate-openapi 'rt.postgres.script.test.scratch-v2 #(clojure.string/starts-with? % "/rpc/insert"))
        paths (keys (:paths spec))]
    ;; Should only include insert functions
    (every? #(clojure.string/starts-with? % "/rpc/insert") paths) => true))

^{:refer rt.postgres.infer.generate/generate-openapi :added "0.1"}
(fact "generate-openapi response schemas are valid"
  (let [spec (generate/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    ;; Check that response schemas exist for all paths
    (doseq [[path methods] paths
            [method op] methods]
      (contains? op :responses) => true)))

;; -----------------------------------------------------------------------------
;; Public API Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer/clear-registry! :added "4.1"}
(fact "clear-registry! empties the type registry"
  (infer/clear-registry!)
  (infer/register-type! 'test/Type (types/make-type-ref :primitive nil :test))
  (some? (infer/get-type 'test/Type)) => true
  (infer/clear-registry!)
  (infer/get-type 'test/Type) => nil)

^{:refer rt.postgres.infer/register-type! :added "4.1"}
(fact "register-type! adds a type to the registry"
  (infer/clear-registry!)
  (let [type-ref (types/make-type-ref :primitive nil :uuid)]
    (infer/register-type! 'test/Uuid type-ref)
    (infer/get-type 'test/Uuid) => type-ref))

^{:refer rt.postgres.infer/get-type :added "4.1"}
(fact "get-type retrieves a registered type"
  (infer/clear-registry!)
  (infer/get-type 'nonexistent/Type) => nil
  (let [type-ref (types/make-type-ref :primitive nil :text)]
    (infer/register-type! 'test/Text type-ref)
    (infer/get-type 'test/Text) => type-ref))

^{:refer rt.postgres.infer/analyze-file :added "4.1"}
(fact "analyze-file returns structure with tables, enums, and functions"
  (let [result (infer/analyze-file "src/rt/postgres/infer/types.clj")]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.infer/analyze-namespace :added "4.1"}
(fact "analyze-namespace analyzes a namespace and returns type definitions"
  (let [result (infer/analyze-namespace 'rt.postgres.infer.types)]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.infer/analyze-and-register! :added "4.1"}
(fact "analyze-and-register! analyzes and registers types from a namespace"
  (infer/clear-registry!)
  (let [result (infer/analyze-and-register! 'rt.postgres.infer.types)]
    ;; Result is the analysis map with tables, enums, functions
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.infer/make-openapi :added "4.1"}
(fact "make-openapi generates OpenAPI spec for a namespace"
  (let [spec (infer/make-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))]
    (contains? spec :openapi) => true
    (contains? spec :info) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true))

^{:refer rt.postgres.infer/make-json-schema :added "4.1"}
(fact "make-json-schema generates JSON Schema definitions"
  (infer/clear-registry!)
  (-> 'rt.postgres.script.test.scratch-v2 infer/analyze-namespace infer/register-types!)
  (let [schema (infer/make-json-schema)]
    (map? schema) => true))

^{:refer rt.postgres.infer/make-typescript :added "4.1"}
(fact "make-typescript generates TypeScript definitions"
  (infer/clear-registry!)
  (-> 'rt.postgres.script.test.scratch-v2 infer/analyze-namespace infer/register-types!)
  (let [ts (infer/make-typescript)]
    (string? ts) => true
    (clojure.string/includes? ts "interface") => true))

^{:refer rt.postgres.infer/get-table-shape :added "4.1"}
(fact "get-table-shape returns shape for a registered table"
  (infer/clear-registry!)
  (let [table (types/make-table-def "test" "User"
                                    [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid) {:required true})]
                                    :id)]
    (infer/register-type! 'test/User table)
    (let [shape (infer/get-table-shape 'test/User)]
      (types/jsonb-shape? shape) => true
      (contains? (:fields shape) :id) => true)))

^{:refer rt.postgres.infer/list-tables :added "4.1"}
(fact "list-tables returns all registered table definitions"
  (infer/clear-registry!)
  (let [table (types/make-table-def "test" "User" [] :id)]
    (infer/register-type! 'test/User table)
    (let [tables (infer/list-tables)]
      (count tables) => 1
      (:name (first tables)) => "User")))

^{:refer rt.postgres.infer/list-functions :added "4.1"}
(fact "list-functions returns all registered function definitions"
  (infer/clear-registry!)
  (let [fn-def (types/make-fn-def "test" "get-user" [] [:jsonb] {} nil)]
    (infer/register-type! 'test/get-user fn-def)
    (let [fns (infer/list-functions)]
      (count fns) => 1
      (:name (first fns)) => "get-user")))

^{:refer rt.postgres.infer/list-enums :added "4.1"}
(fact "list-enums returns all registered enum definitions"
  (infer/clear-registry!)
  (let [enum (types/make-enum-def "test" "Status" #{:active :inactive} nil)]
    (infer/register-type! 'test/Status enum)
    (let [enums (infer/list-enums)]
      (count enums) => 1
      (:name (first enums)) => "Status")))

^{:refer rt.postgres.infer/load-runtime-tables :added "4.1"}
(fact "load-runtime-tables loads tables from runtime format"
  (let [tables-map {:User [:id {:type :uuid :primary true}
                           :name {:type :text}]
                    :Org [:id {:type :uuid :primary true}
                          :handle {:type :citext}]}
        loaded (infer/load-runtime-tables tables-map)]
    (count loaded) => 2
    (contains? loaded :User) => true
    (contains? loaded :Org) => true))

^{:refer rt.postgres.infer/register-runtime-tables! :added "4.1"}
(fact "register-runtime-tables! registers runtime tables in the registry"
  (infer/clear-registry!)
  (let [tables-map {:TestTable [:id {:type :uuid :primary true}]}
        loaded (infer/load-runtime-tables tables-map)]
    (infer/register-runtime-tables! loaded)
    (some? (infer/get-type :TestTable)) => true))
