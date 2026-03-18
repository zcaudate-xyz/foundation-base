(ns rt.postgres.infer-test
  "Integration tests for rt.postgres.infer namespace.
   Tests end-to-end OpenAPI generation from namespace analysis."
  (:use code.test)
  (:require [rt.postgres.infer :as core]
            [rt.postgres.infer.types :as types]
            [rt.postgres.script.test.scratch-v2 :as scratch]))

;; -----------------------------------------------------------------------------
;; OpenAPI Generation Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer/make-openapi :added "0.1"}
(fact "make-openapi generates correct OpenAPI spec for rt.postgres.script.test.scratch-v2"
  (let [spec (core/make-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    ;; Check that paths are generated
    (map? paths) => true
    (not (empty? paths)) => true))

^{:refer rt.postgres.infer/make-openapi :added "0.1"}
(fact "make-openapi generates valid OpenAPI structure"
  (let [spec (core/make-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))]
    ;; Top-level required fields
    (contains? spec :openapi) => true
    (contains? spec :info) => true
    (contains? spec :paths) => true
    (contains? spec :components) => true
    ;; Components structure
    (contains? (:components spec) :schemas) => true
    ;; Paths should be a map
    (map? (:paths spec)) => true))

^{:refer rt.postgres.infer/make-openapi :added "0.1"}
(fact "make-openapi includes schemas in components"
  (let [spec (core/make-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        schemas (get-in spec [:components :schemas])]
    (map? schemas) => true))

^{:refer rt.postgres.infer/make-openapi :added "0.1"}
(fact "make-openapi filters paths based on predicate"
  (let [spec (core/make-openapi 'rt.postgres.script.test.scratch-v2 #(clojure.string/starts-with? % "/rpc/insert"))
        paths (keys (:paths spec))]
    ;; Should only include insert functions
    (every? #(clojure.string/starts-with? % "/rpc/insert") paths) => true))

^{:refer rt.postgres.infer/make-openapi :added "0.1"}
(fact "make-openapi response schemas are valid"
  (let [spec (core/make-openapi 'rt.postgres.script.test.scratch-v2 (constantly true))
        paths (:paths spec)]
    ;; Check that response schemas exist for all paths
    (doseq [[path methods] paths
            [method op] methods]
      (contains? op :responses) => true))))
