(ns documentation.code-test
  (:require [code.test :as t]
            [code.test.task :as task])
  (:use code.test))

[[:hero {:title "code.test"
         :subtitle "Fact-based custom testing."
         :lead "`code.test` is the custom test runner used throughout foundation-base. It reads `fact` forms, arrow assertions, namespace metadata, and runner options rather than delegating to standard `clojure.test`."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"The repository uses tests as examples as well as verification. `fact` forms can be imported into docs, linked back to source, and executed with targeted selectors. This is why new docs should prefer runnable `fact` examples when dependencies are local."

[[:chapter {:title "How to use it" :link "usage"}]]

"Run focused namespaces with `lein test :only some.namespace-test` or families with `lein test :with \"[std]\"`. Conditional environments should use `fact:global` skips so missing tools skip cleanly instead of failing setup."

(comment
  lein test :only code.doc-test
  lein test :with "[code]")

[[:chapter {:title "Internal usage" :link "internal"}]]

"`code.test` is used by normal tests under `test/`, language tests under `test-lang/`, and documentation source files under `src-doc/documentation/`. The docs generator can render facts as examples, so a fact can serve both test and explanation."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Writing a fact"}]]

"Tests are written with the `fact` macro and the `=>` arrow. A fact can contain multiple `input => expected` pairs, plain setup forms, and string descriptions."

^{:refer code.test/fact :added "3.0"}
(fact "a basic fact with the arrow assertion"
  (+ 1 2)
  => 3

  (str "hello" " " "world")
  => "hello world")

[[:section {:title "Checkers"}]]

"`code.test` provides matchers that go beyond equality. `throws` expects an exception, `satisfies` accepts a predicate or type, and `contains` checks nested data loosely."

^{:refer code.test.checker.common/throws :added "3.0"}
(fact "throws catches exceptions"
  (/ 1 0)
  => (throws))

^{:refer code.test.checker.common/satisfies :added "3.0"}
(fact "satisfies accepts predicates and classes"
  42
  => (satisfies even?)

  "hello"
  => (satisfies string?))

^{:refer code.test.checker.collection/contains :added "3.0"}
(fact "contains checks a subset of a map"
  {:a 1 :b 2 :c 3}
  => (contains {:a 1 :b even?}))

[[:section {:title "Inspecting facts"}]]

"Facts are registered when the namespace loads. `fact:list` returns the ids in the current namespace, and `fact:all` retrieves facts from any loaded namespace."

^{:refer code.test/fact:list :added "3.0"}
(fact "list ids in the current namespace"
  (set (keys (t/fact:list)))
  => set?)

^{:refer code.test/fact:all :added "3.0"}
(fact "retrieve all facts for a namespace"
  (t/fact:all 'code.project-test)
  => map?)

[[:section {:title "End-to-end: running a focused namespace"}]]

"Use `task/run` to execute a single test namespace, or `run-errored` to retry only the tests that failed in the last run."

^{:refer code.test.task/run :added "3.0"}
(fact "run a focused test namespace"
  (task/run 'code.project-test)
  => map?)

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.test"}]]
[[:api {:namespace "code.test.task"}]]
[[:api {:namespace "code.test.manage"}]]

;; BEGIN merged documentation: guides/code.test.md
;; sha256: c1877b82935ced04097fbbc6ec1a23f5a468d0940d2a5f1d704d23de10f44da7
[[:chapter {:title "code.test Guide" :link "merged-guides-code-test-md"}]]

"`code.test` is a custom testing framework that provides a robust alternative to `clojure.test`. It emphasizes clear facts, rich assertions, and integrated management tools."

[[:section {:title "Core Concepts" :link "merged-guides-code-test-md-core-concepts"}]]

"- **Fact**: The unit of test execution. Defined via `fact`.\n- **Assertion**: Uses the `=>` arrow to compare results.\n- **Checker**: Objects that encapsulate validation logic (e.g., `throws`, `contains`)."

[[:section {:title "Usage" :link "merged-guides-code-test-md-usage"}]]

[[:subsection {:title "Basic Structure" :link "merged-guides-code-test-md-basic-structure"}]]

[[:code {:lang "clojure"} "(ns my.ns-test\n  (:require [code.test :refer [fact =>]]))\n\n(fact \"test description\"\n  (expression) => expected-value)"]]

[[:subsection {:title "Scenarios" :link "merged-guides-code-test-md-scenarios"}]]

[[:subsubsection {:title "1. Testing Exceptions" :link "merged-guides-code-test-md-1-testing-exceptions"}]]

"When testing for exceptions, you often need to verify not just the type, but the message or the data (in `ex-info`)."

[[:code {:lang "clojure"} "(fact \"exception handling\"\n  ;; 1. Check exception type\n  (/ 1 0) => (throws ArithmeticException)\n\n  ;; 2. Check exception type and message\n  (throw (Exception. \"Hello\")) => (throws Exception \"Hello\")\n\n  ;; 3. Check ex-info data using nested checkers\n  ;; Note: The verification of `ex-info` data often requires capturing the exception\n  ;; or using a custom predicate if you need to check the data map deep inside.\n\n  (throw (ex-info \"Error\" {:code 500}))\n  => (throws clojure.lang.ExceptionInfo)\n)"]]

"To strictly check `ex-info` data, you can use a custom predicate or `throws-info` if available (check `coll/throws-info` in `code.test.checker.collection`)."

[[:code {:lang "clojure"} "(require '[code.test.checker.collection :as coll])\n\n(fact \"ex-info check\"\n  (throw (ex-info \"msg\" {:a 1}))\n  => (coll/throws-info {:a 1}))"]]

[[:subsubsection {:title "2. Complex Data Validation" :link "merged-guides-code-test-md-2-complex-data-validation"}]]

"Use `contains`, `contains-in`, and `just` for detailed map/collection verification."

[[:code {:lang "clojure"} "(fact \"complex data\"\n  (def m {:user {:name \"Bob\" :age 30 :roles [:admin]}})\n\n  ;; Partial match on map\n  m => (contains {:user (contains {:name \"Bob\"})})\n\n  ;; Nested match\n  m => (contains-in [:user :roles] [:admin])\n\n  ;; Exact match (ignoring order for sets/maps where applicable)\n  {:a 1 :b 2} => (just {:b 2 :a 1}))"]]

[[:subsubsection {:title "3. Skipping and Focusing Tests" :link "merged-guides-code-test-md-3-skipping-and-focusing-tests"}]]

"You can control test execution using metadata on the `fact` form."

[[:code {:lang "clojure"} ";; Skip this test during normal runs\n(fact \"long running test\"\n  {:tag :integration}\n  (Thread/sleep 1000) => nil)\n\n;; Prevent evaluation at definition time (if configured)\n(fact \"pending test\"\n  {:eval false}\n  (future-implementation) => true)"]]

"To run tagged tests, you would typically filter them via the runner (e.g., `lein test :tag integration`)."

[[:subsubsection {:title "4. Side Effects and Mocking" :link "merged-guides-code-test-md-4-side-effects-and-mocking"}]]

"Use `with-redefs` to mock functions. Since `fact` executes in its own scope, these redefinitions are contained."

[[:code {:lang "clojure"} "(defn external-call [] :real)\n\n(fact \"mocking external calls\"\n  (with-redefs [external-call (constantly :mocked)]\n    (external-call) => :mocked)\n\n  ;; Original is restored\n  (external-call) => :real)"]]

[[:subsubsection {:title "5. Logic Checkers" :link "merged-guides-code-test-md-5-logic-checkers"}]]

"Combine checkers for flexible validation."

[[:code {:lang "clojure"} "(require '[code.test.checker.logic :as logic])\n\n(fact \"logic combinations\"\n  10 => (logic/all number? pos?)\n  10 => (logic/any 10 20 30)\n  10 => (logic/is-not neg?))"]]

[[:subsubsection {:title "6. Capturing Values for Debugging" :link "merged-guides-code-test-md-6-capturing-values-for-debugging"}]]

"You can use the `capture` checker to inspect intermediate values during test development."

[[:code {:lang "clojure"} "(require '[code.test.checker.common :as common])\n\n(fact \"capturing\"\n  (+ 1 2) => (common/capture common/anything my-var))\n\n;; After running, `my-var` will hold the value 3 in the test namespace."]]

[[:subsubsection {:title "7. Attaching Debug Forms to Failing Expressions" :link "merged-guides-code-test-md-7-attaching-debug-forms-to-failing-expressions"}]]

"For expressions that are hard to diagnose when they throw or fail an assertion,\nyou can attach a debug form via metadata. If the expression errors or its `=>`\ncheck fails, the debug form is evaluated and its result is printed in the\nfailure report."

[[:code {:lang "clojure"} "(fact \"debug on throw\"\n  ^{:on-error '(prn :troubleshooting)}\n  (risky-op))\n\n(fact \"debug on failed assertion\"\n  ^{:on-error '(l/with:print (compute-context))}\n  (computed-value) => expected-value)"]]

"**Important:** the value of `:on-error` must be a *quoted* form. Metadata map\nvalues are evaluated when the `fact` is read, so an unquoted form would run\neven when the test passes. The debug form runs only when the main expression\nthrows an exception or when the assertion returns `false`. Its result appears\nin the `THROW` or `FAILED` output under an `On Error:` label."

[[:subsection {:title "Running Tests" :link "merged-guides-code-test-md-running-tests"}]]

"- **All**: `lein test`\n- **Namespace**: `lein test :only my.ns`\n- **Pattern**: `lein test :in my.pkg`\n- **Save REPL rerun helper**: `lein test :only my.ns :save-run true` writes `.hara/runs/run-<timestamp>.run.edn` with a copy-pastable `code.test/run` form.\n- **Re-run failures**: The runner typically outputs instructions or you can use `code.manage` to focus on failures."
;; END merged documentation: guides/code.test.md
