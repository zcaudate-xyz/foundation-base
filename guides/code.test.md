# `code.test` Guide

`code.test` is a custom testing framework that provides a robust alternative to `clojure.test`. It emphasizes clear facts, rich assertions, and integrated management tools.

## Core Concepts

- **Fact**: The unit of test execution. Defined via `fact`.
- **Assertion**: Uses the `=>` arrow to compare results.
- **Checker**: Objects that encapsulate validation logic (e.g., `throws`, `contains`).

## Usage

### Basic Structure

```clojure
(ns my.ns-test
  (:require [code.test :refer [fact =>]]))

(fact "test description"
  (expression) => expected-value)
```

### Scenarios

#### 1. Testing Exceptions

When testing for exceptions, you often need to verify not just the type, but the message or the data (in `ex-info`).

```clojure
(fact "exception handling"
  ;; 1. Check exception type
  (/ 1 0) => (throws ArithmeticException)

  ;; 2. Check exception type and message
  (throw (Exception. "Hello")) => (throws Exception "Hello")

  ;; 3. Check ex-info data using nested checkers
  ;; Note: The verification of `ex-info` data often requires capturing the exception
  ;; or using a custom predicate if you need to check the data map deep inside.

  (throw (ex-info "Error" {:code 500}))
  => (throws clojure.lang.ExceptionInfo)
)
```

To strictly check `ex-info` data, you can use a custom predicate or `throws-info` if available (check `coll/throws-info` in `code.test.checker.collection`).

```clojure
(require '[code.test.checker.collection :as coll])

(fact "ex-info check"
  (throw (ex-info "msg" {:a 1}))
  => (coll/throws-info {:a 1}))
```

#### 2. Complex Data Validation

Use `contains`, `contains-in`, and `just` for detailed map/collection verification.

```clojure
(fact "complex data"
  (def m {:user {:name "Bob" :age 30 :roles [:admin]}})

  ;; Partial match on map
  m => (contains {:user (contains {:name "Bob"})})

  ;; Nested match
  m => (contains-in [:user :roles] [:admin])

  ;; Exact match (ignoring order for sets/maps where applicable)
  {:a 1 :b 2} => (just {:b 2 :a 1}))
```

#### 3. Skipping and Focusing Tests

You can control test execution using metadata on the `fact` form.

```clojure
;; Skip this test during normal runs
(fact "long running test"
  {:tag :integration}
  (Thread/sleep 1000) => nil)

;; Prevent evaluation at definition time (if configured)
(fact "pending test"
  {:eval false}
  (future-implementation) => true)
```

To run tagged tests, you would typically filter them via the runner (e.g., `lein test :tag integration`).

#### 4. Side Effects and Mocking

Use `with-redefs` to mock functions. Since `fact` executes in its own scope, these redefinitions are contained.

```clojure
(defn external-call [] :real)

(fact "mocking external calls"
  (with-redefs [external-call (constantly :mocked)]
    (external-call) => :mocked)

  ;; Original is restored
  (external-call) => :real)
```

#### 5. Logic Checkers

Combine checkers for flexible validation.

```clojure
(require '[code.test.checker.logic :as logic])

(fact "logic combinations"
  10 => (logic/all number? pos?)
  10 => (logic/any 10 20 30)
  10 => (logic/is-not neg?))
```

#### 6. Capturing Values for Debugging

You can use the `capture` checker to inspect intermediate values during test development.

```clojure
(require '[code.test.checker.common :as common])

(fact "capturing"
  (+ 1 2) => (common/capture common/anything my-var))

;; After running, `my-var` will hold the value 3 in the test namespace.
```

### Running Tests

- **All**: `lein test`
- **Namespace**: `lein test :only my.ns`
- **Pattern**: `lein test :in my.pkg`
- **Re-run failures**: The runner typically outputs instructions or you can use `code.manage` to focus on failures.
