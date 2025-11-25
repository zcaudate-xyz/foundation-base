(ns code.test.compile-test
  (:require [code.test.compile :refer :all :exclude [=> *last*]]
            [code.test.base.process :as process]
            [code.test :refer [contains-in]]))

^{:refer code.test.compile/arrow? :added "3.0"}
(fact "checks if form is an arrow")

^{:refer code.test.compile/fact-skip? :added "3.0"}
(fact "checks if form should be skipped"

  (fact-skip? '(fact:component))
  => true)

^{:refer code.test.compile/strip :added "3.0"}
(fact "removes all checks in the function"

  (strip '[(def a 1)
           (+ a 3)
           => 5])
  => '[(def a 1) (+ a 3)])

^{:refer code.test.compile/split :added "3.0"}
(fact "creates a sequence of pairs from a loose sequence"
  (split '[(def a 1)
           (+ a 3)
           => 5])
  (contains-in '[{:type :form,
                  :meta {:line 8, :column 12},
                  :form '(def a 1)}
                 {:type :test-equal,
                  :meta {:line 9, :column 12},
                  :input  {:form '(+ a 3)},
                  :output {:form 5}}]))

^{:refer code.test.compile/fact-id :added "3.0"}
(fact "creates an id from fact data"

  (fact-id {} "hello there")
  => 'test-hello-there)

^{:refer code.test.compile/fact-prepare-meta :added "3.0"}
(fact "parses and converts fact to symbols"

  (fact-prepare-meta 'test-hello
                     {}
                     "hello"
                     '(1 => 1)) ^:hidden
  => '[{:path "test/code/test/compile_test.clj",
        :desc "hello",
        :ns code.test.compile-test,
        :id test-hello}
       (1 => 1)])

^{:refer code.test.compile/fact-prepare-core :added "3.0"}
(fact "prepares fact for a core form")

^{:refer code.test.compile/fact-prepare-derived :added "3.0"}
(fact "prepares fact for a derived form")

^{:refer code.test.compile/fact-prepare-link :added "3.0"}
(fact "prepares fact for a linked form")

^{:refer code.test.compile/fact-thunk :added "3.0"}
(fact "creates a thunk form")

^{:refer code.test.compile/create-fact :added "3.0"}
(fact "creates a fact given meta and body")

^{:refer code.test.compile/install-fact :added "3.0"}
(fact "installs the current fact")

^{:refer code.test.compile/fact:compile :added "3.0"}
(fact "recompiles fact with a different global")

^{:refer code.test.compile/fact-eval :added "3.0"}
(fact "creates the forms in eval mode")

^{:refer code.test.compile/fact :added "3.0"
  :style/indent 1}
(fact "top level macro for test definitions")

^{:refer code.test.compile/fact:template :added "3.0"
  :style/indent 1}
(fact "adds a template to the file")

^{:refer code.test.compile/fact:purge :added "3.0"
  :style/indent 1}
(fact "purges all facts in namespace")

^{:refer code.test.compile/fact:list :added "3.0"
  :style/indent 1}
(fact "lists all facts in namespace")

^{:refer code.test.compile/fact:all :added "3.0"
  :style/indent 1}
(fact "returns all facts in namespace")

^{:refer code.test.compile/fact:rerun :added "3.0"}
(fact "reruns all facts along with filter and compile options")

^{:refer code.test.compile/fact:missing :added "3.0"
  :style/indent 1}
(fact "returns all missing facts for a given namespace")

^{:refer code.test.compile/fact:get :added "3.0"
  :style/indent 1}
(fact "gets elements of the current fact")

^{:refer code.test.compile/fact:exec :added "3.0"
  :style/indent 1}
(fact "runs main hook for fact form")

^{:refer code.test.compile/fact:setup :added "3.0"
  :style/indent 1}
(fact "runs setup hook for current fact")

^{:refer code.test.compile/fact:setup? :added "3.0"
  :style/indent 1}
(fact "checks if setup hook has been ran")

^{:refer code.test.compile/fact:teardown :added "3.0"
  :style/indent 1}
(fact "runs teardown hook for current fact")

^{:refer code.test.compile/fact:remove :added "3.0"
  :style/indent 1}
(fact "removes the current fact")

^{:refer code.test.compile/fact:symbol :added "3.0"
  :style/indent 1}
(fact "gets the current fact symbol")

^{:refer code.test.compile/fact-eval-current :added "3.0"}
(fact "evaluates the current fact, typically used within a testing context")

^{:refer code.test.compile/fact:let-install :added "3.0"}
(fact "installer for `fact:let` macro")

^{:refer code.test.compile/fact:let :added "3.0"
  :style/indent 1}
(fact "runs a form that has binding substitutions")

^{:refer code.test.compile/fact:derive-install :added "3.0"}
(fact "installer for `fact:derive` macro")

^{:refer code.test.compile/fact:derive :added "3.0"
  :style/indent 1}
(fact "runs a form derived from a previous test")

^{:refer code.test.compile/fact:table-install :added "3.0"}
(fact "installer for `fact:table` macro")

^{:refer code.test.compile/fact:table :added "3.0"
  :style/indent 1}
(fact "runs a form with tabular value substitutions")

^{:refer code.test.compile/fact:bench-install :added "3.0"}
(fact "installer for `fact:bench` macro")

^{:refer code.test.compile/fact:bench :added "3.0"
  :style/indent 1}
(fact "runs a small micro bench for the current fact")

^{:refer code.test.compile/fact:check-install :added "3.0"}
(fact "installer for `fact:check` macro")

^{:refer code.test.compile/fact:check :added "3.0"
  :style/indent 1}
(fact "runs a check over a range of values")


^{:refer code.test.compile/group-and-rewrite :added "4.0"}
(fact "TODO")

^{:refer code.test.compile/rewrite-nested-checks :added "4.0"}
(fact "TODO")