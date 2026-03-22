(ns rt.postgres.typed-test
  "Integration tests for rt.postgres.typed namespace.
   Tests end-to-end OpenAPI generation from namespace analysis."
  (:use code.test)
  (:require [rt.postgres.typed :as typed]
            [rt.postgres.compile.json-openapi :as compile.json-openapi]
            [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.script.test.scratch-v2 :as scratch]))

;; -----------------------------------------------------------------------------
;; OpenAPI Generation Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi generates correct OpenAPI spec for rt.postgres.script.test.scratch-v2"
  (let [spec (compile.json-openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    ;; Check that paths are generated
    (map? paths) => true
    (not (empty? paths)) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi generates valid OpenAPI structure"
  (let [spec (compile.json-openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))]
    ;; Top-level required fields
    (contains? spec :openapi) => true
    (contains? spec :info) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true
    ;; Components structure
    (contains? (:components spec) :schemas) => true
    ;; Paths should be a map
    (map? (:paths spec)) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi includes schemas in components"
  (let [spec (compile.json-openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        schemas (get-in spec [:components :schemas])]
    (map? schemas) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi filters paths based on predicate"
  (let [spec (compile.json-openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 #(clojure.string/starts-with? % "/rpc/insert"))
        paths (keys (:paths spec))]
    ;; Should only include insert functions
    (every? #(clojure.string/starts-with? % "/rpc/insert") paths) => true))

^{:refer rt.postgres.compile.json-openapi/generate-openapi :added "0.1"}
(fact "generate-openapi response schemas are valid"
  (let [spec (compile.json-openapi/generate-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    ;; Check that response schemas exist for all paths
    (doseq [[path methods] paths
            [method op] methods]
      (contains? op :responses) => true)))

;; -----------------------------------------------------------------------------
;; Public API Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.typed/clear-registry! :added "4.1"}
(fact "clear-registry! empties the type registry"
  (typed/clear-registry!)
  (typed/register-type! 'test/Type (types/make-type-ref :primitive nil :test))
  (some? (typed/get-type 'test/Type)) => true
  (typed/clear-registry!)
  (typed/get-type 'test/Type) => nil)

^{:refer rt.postgres.typed/register-type! :added "4.1"}
(fact "register-type! adds a type to the registry"
  (typed/clear-registry!)
  (let [type-ref (types/make-type-ref :primitive nil :uuid)]
    (typed/register-type! 'test/Uuid type-ref)
    (typed/get-type 'test/Uuid) => type-ref))

^{:refer rt.postgres.typed/get-type :added "4.1"}
(fact "get-type retrieves a registered type"
  (typed/clear-registry!)
  (typed/get-type 'nonexistent/Type) => nil
  (let [type-ref (types/make-type-ref :primitive nil :text)]
    (typed/register-type! 'test/Text type-ref)
    (typed/get-type 'test/Text) => type-ref))

^{:refer rt.postgres.typed/analyze-file :added "4.1"}
(fact "analyze-file returns structure with tables, enums, and functions"
  (let [result (typed/analyze-file "src/rt/postgres/grammar/typed_common.clj")]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.typed/analyze-namespace :added "4.1"}
(fact "analyze-namespace analyzes a namespace and returns type definitions"
  (let [result (typed/analyze-namespace 'rt.postgres.grammar.typed-common)]
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.typed/analyze-and-register! :added "4.1"}
(fact "analyze-and-register! analyzes and registers types from a namespace"
  (typed/clear-registry!)
  (let [result (typed/analyze-and-register! 'rt.postgres.grammar.typed-common)]
    ;; Result is the analysis map with tables, enums, functions
    (contains? result :tables) => true
    (contains? result :enums) => true
    (contains? result :functions) => true))

^{:refer rt.postgres.typed/make-openapi :added "4.1"}
(fact "make-openapi generates OpenAPI spec for a namespace"
  (let [spec (typed/make-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))]
    (contains? spec :openapi) => true
    (contains? spec :info) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true))

^{:refer rt.postgres.typed/make-json-schema :added "4.1"}
(fact "make-json-schema generates JSON Schema definitions"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'rt.postgres.script.test.scratch-v2)
  (let [schema (typed/make-json-schema)]
    (map? schema) => true))

^{:refer rt.postgres.typed/make-typescript :added "4.1"}
(fact "make-typescript generates TypeScript definitions"
  (typed/clear-registry!)
  (typed/analyze-and-register! 'rt.postgres.script.test.scratch-v2)
  (let [ts (typed/make-typescript)]
    (string? ts) => true
    (clojure.string/includes? ts "interface") => true))

^{:refer rt.postgres.typed/get-table-shape :added "4.1"}
(fact "get-table-shape returns shape for a registered table"
  (typed/clear-registry!)
  (let [table (types/make-table-def "test" "User"
                                    [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid) {:required true})]
                                    :id)]
    (typed/register-type! 'test/User table)
    (let [shape (typed/get-table-shape 'test/User)]
      (types/jsonb-shape? shape) => true
      (contains? (:fields shape) :id) => true)))

^{:refer rt.postgres.typed/list-tables :added "4.1"}
(fact "list-tables returns all registered table definitions"
  (typed/clear-registry!)
  (let [table (types/make-table-def "test" "User" [] :id)]
    (typed/register-type! 'test/User table)
    (let [tables (typed/list-tables)]
      (count tables) => 1
      (:name (first tables)) => "User")))

^{:refer rt.postgres.typed/list-functions :added "4.1"}
(fact "list-functions returns all registered function definitions"
  (typed/clear-registry!)
  (let [fn-def (types/make-fn-def "test" "get-user" [] [:jsonb] {} nil)]
    (typed/register-type! 'test/get-user fn-def)
    (let [fns (typed/list-functions)]
      (count fns) => 1
      (:name (first fns)) => "get-user")))

^{:refer rt.postgres.typed/list-enums :added "4.1"}
(fact "list-enums returns all registered enum definitions"
  (typed/clear-registry!)
  (let [enum (types/make-enum-def "test" "Status" #{:active :inactive} nil)]
    (typed/register-type! 'test/Status enum)
    (let [enums (typed/list-enums)]
      (count enums) => 1
      (:name (first enums)) => "Status")))

^{:refer rt.postgres.typed/load-runtime-tables :added "4.1"}
(fact "load-runtime-tables loads tables from runtime format"
  (let [tables-map {:User [:id {:type :uuid :primary true}
                           :name {:type :text}]
                    :Org [:id {:type :uuid :primary true}
                          :handle {:type :citext}]}
        loaded (typed/load-runtime-tables tables-map)]
    (count loaded) => 2
    (contains? loaded :User) => true
    (contains? loaded :Org) => true))

^{:refer rt.postgres.typed/register-runtime-tables! :added "4.1"}
(fact "register-runtime-tables! registers runtime tables in the registry"
  (typed/clear-registry!)
  (let [tables-map {:TestTable [:id {:type :uuid :primary true}]}
        loaded (typed/load-runtime-tables tables-map)]
    (typed/register-runtime-tables! loaded)
    (some? (typed/get-type :TestTable)) => true))


^{:refer rt.postgres.typed/make-function-report :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.typed/report-json :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.typed/make-function-json :added "4.1"}
(fact "TODO")