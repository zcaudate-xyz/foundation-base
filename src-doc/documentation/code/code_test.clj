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
