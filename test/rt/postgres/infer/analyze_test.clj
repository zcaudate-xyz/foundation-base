(ns rt.postgres.infer.analyze-test
  "Tests for rt.postgres.infer.analyze namespace.
   Provides expression and function body analysis."
  (:use code.test)
  (:require [rt.postgres.infer.analyze :as analyze]
            [rt.postgres.infer.types :as types]))

;; -----------------------------------------------------------------------------
;; Expression Analysis Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.analyze/analyze-expr :added "0.1"}
(fact "analyze-expr handles literals"
  (let [ctx (types/make-context)]
    (analyze/analyze-expr nil ctx) => (contains {:kind :literal :type :null})
    (analyze/analyze-expr true ctx) => (contains {:kind :literal :type :boolean :value true})
    (analyze/analyze-expr "hello" ctx) => (contains {:kind :literal :type :text :value "hello"})
    (analyze/analyze-expr 42 ctx) => (contains {:kind :literal :type :integer :value 42})
    (analyze/analyze-expr 3.14 ctx) => (contains {:kind :literal :type :numeric :value 3.14})))

^{:refer rt.postgres.infer.analyze/analyze-expr :added "0.1"}
(fact "analyze-expr handles symbols"
  (let [ctx (-> (types/make-context)
                (types/add-binding 'x {:type :uuid}))]
    (analyze/analyze-expr 'x ctx) => (contains {:type :uuid})
    (analyze/analyze-expr 'unknown ctx) => (contains {:kind :unknown :name 'unknown})))

^{:refer rt.postgres.infer.analyze/analyze-expr :added "0.1"}
(fact "analyze-expr handles map literals"
  (let [ctx (types/make-context)
        result (analyze/analyze-expr {:id 'x :name "test"} ctx)]
    (:kind result) => :map
    (contains? result :entries) => true
    (contains? result :shape) => true))

^{:refer rt.postgres.infer.analyze/analyze-expr :added "0.1"}
(fact "analyze-expr handles control flow"
  (let [ctx (types/make-context)]
    ;; Return statement
    (let [result (analyze/analyze-expr '(return {:id 1}) ctx)]
      (:kind result) => :return)
    ;; If branch
    (let [result (analyze/analyze-expr '(if true {:a 1} {:b 2}) ctx)]
      (:kind result) => :branch)
    ;; When branch
    (let [result (analyze/analyze-expr '(when true {:a 1}) ctx)]
      (:kind result) => :branch)))

^{:refer rt.postgres.infer.analyze/analyze-expr :added "0.1"}
(fact "analyze-expr handles operators"
  (let [ctx (types/make-context)]
    ;; Comparison
    (let [result (analyze/analyze-expr '(== x y) ctx)]
      (:kind result) => :primitive
      (:type result) => :boolean)
    ;; Math
    (let [result (analyze/analyze-expr '(+ x y) ctx)]
      (:kind result) => :primitive
      (:type result) => :numeric)
    ;; JSONB merge
    (let [result (analyze/analyze-expr '(|| a b) ctx)]
      (:kind result) => :shaped
      (:op result) => :merge)))

;; -----------------------------------------------------------------------------
;; Function Analysis Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.analyze/analyze-function-body :added "0.1"}
(fact "analyze-function-body analyzes simple function"
  (let [fn-def (types/make-fn-def "test" "simple-fn"
                                  [(types/make-fn-arg 'i-id :uuid {})]
                                  [:jsonb]
                                  {:raw-body ['(return {:id i-id})]})]
    (let [result (analyze/analyze-function-body fn-def)]
      result =not=> nil
      (:fn result) => "simple-fn"
      (:ns result) => "test"
      (seq (:body-analysis result)) => true)))

^{:refer rt.postgres.infer.analyze/analyze-function-body :added "0.1"}
(fact "analyze-function-body handles let bindings"
  (let [fn-def (types/make-fn-def "test" "let-fn"
                                  [(types/make-fn-arg 'i-data :jsonb {})]
                                  [:jsonb]
                                  {:raw-body ['(let [x i-data]
                                                (return x))]})]
    (let [result (analyze/analyze-function-body fn-def)]
      (not (nil? result)) => true
      (not (empty? (:body-analysis result))) => true)))

;; -----------------------------------------------------------------------------
;; Return Type Inference Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.analyze/infer-return-type :added "0.1"}
(fact "infer-return-type returns void for empty body"
  (let [fn-def (types/make-fn-def "test" "empty-fn" [] [:void]
                                  {:raw-body []})]
    (let [result (analyze/infer-return-type fn-def)]
      (:kind result) => :void)))

^{:refer rt.postgres.infer.analyze/infer-return-type :added "0.1"}
(fact "infer-return-type infers from return statement"
  (let [fn-def (types/make-fn-def "test" "return-fn"
                                  [(types/make-fn-arg 'i-id :uuid {})]
                                  [:jsonb]
                                  {:raw-body ['(return {:id i-id})]})]
    (let [result (analyze/infer-return-type fn-def)]
      (not (nil? (:kind result))) => true)))

;; -----------------------------------------------------------------------------
;; Cache Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.analyze/reset-cache! :added "0.1"}
(fact "reset-cache! clears the inference cache"
  (analyze/reset-cache!)
  ;; Cache should be empty after reset
  (count @analyze/*infer-cache*) => 0)

^{:refer rt.postgres.infer.analyze/cached-infer :added "0.1"}
(fact "cached-infer caches inference results"
  (analyze/reset-cache!)
  (let [fn-def (types/make-fn-def "test" "cached-fn"
                                  [(types/make-fn-arg 'i-id :uuid {})]
                                  [:jsonb]
                                  {:raw-body ['(return {:id i-id})]})]
    ;; First call should compute and cache
    (let [result1 (analyze/cached-infer fn-def)]
      (not (nil? result1)) => true)
    ;; Second call should return cached result
    (let [result2 (analyze/cached-infer fn-def)]
      (not (nil? result2)) => true)))

;; -----------------------------------------------------------------------------
;; PG Operations Tests
;; -----------------------------------------------------------------------------

^{:refer rt.postgres.infer.analyze/get-op-info :added "0.1"}
(fact "get-op-info returns operation signatures"
  (analyze/get-op-info 'pg/t:insert) => (contains {:op :insert :returns :table-instance})
  (analyze/get-op-info 'pg/t:get) => (contains {:op :get :returns :table-instance})
  (analyze/get-op-info 'pg/t:select) => (contains {:op :select :returns :array})
  (analyze/get-op-info 'pg/id) => (contains {:returns :uuid})
  (analyze/get-op-info 'pg/coalesce) => (contains {:returns :coalesce})
  (analyze/get-op-info 'unknown-op) => nil)
