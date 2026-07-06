(ns documentation.code-test
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

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.test"}]]
[[:api {:namespace "code.test.task"}]]
[[:api {:namespace "code.test.manage"}]]
