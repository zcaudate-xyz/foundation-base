(ns code.test.compile-test
  (:require [code.test.compile :as compile :refer :all :exclude [=> *last*]]
            [code.test.base.process :as process]
            [code.test.base.runtime :as rt]
            [code.test.base.context :as ctx]
            [std.lib :as h]
            [code.test :refer [contains-in contains]]))

^{:refer code.test.compile/arrow? :added "3.0"}
(fact "checks if form is an arrow"
  (arrow? '=>) => true
  (arrow? '+) => false)

^{:refer code.test.compile/fact-skip? :added "3.0"}
(fact "checks if form should be skipped"

  (fact-skip? '(fact:component))
  => true)

^{:refer code.test.compile/rewrite-top-level :added "3.0"}
(fact "creates a sequence of pairs from a loose sequence"
  (rewrite-top-level '[(def a 1)
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
(fact "prepares fact for a core form"
  (first (fact-prepare-core "hello" '(1 => 1) {}))
  => (contains {:id 'test-hello :desc "hello"}))

^{:refer code.test.compile/fact-thunk :added "3.0"}
(fact "creates a thunk form"
  (fact-thunk {:full [] :ns 'my.ns})
  => seq?)

^{:refer code.test.compile/create-fact :added "3.0"}
(fact "creates a fact given meta and body"
  (ctx/with-new-context {}
    (create-fact {:ns 'my.ns :id 'test-fact} '[(+ 1 1) => 2]))
  => map?)

^{:refer code.test.compile/install-fact :added "3.0"}
(fact "installs the current fact"
  (ctx/with-new-context {}
    (install-fact {:ns 'my.ns :id 'test-fact} '[(+ 1 1) => 2])
    (rt/get-fact 'my.ns 'test-fact))
  => map?)

^{:refer code.test.compile/fact:compile :added "3.0"}
(fact "recompiles fact with a different global"
  (ctx/with-new-context {}
    (let [fact (install-fact {:ns 'my.ns :id 'test-fact} '[(+ 1 1) => 2])]
      (fact:compile fact {:a 1})))
  => map?)

^{:refer code.test.compile/fact-eval :added "3.0"}
(fact "creates the forms in eval mode"
  (fact-eval {:ns 'my.ns :id 'test-fact})
  => seq?)

^{:refer code.test.compile/fact :added "3.0"
  :style/indent 1}
(fact "top level macro for test definitions")

^{:refer code.test.compile/fact:template :added "3.0"
  :style/indent 1}
(fact "adds a template to the file")

^{:refer code.test.compile/fact:purge :added "3.0"
  :style/indent 1}
(fact "purges all facts in namespace"
  (ctx/with-new-context {:registry (atom {'my.ns {:facts {:a 1}}})}
    (binding [*ns* (create-ns 'my.ns)]
      (fact:purge)
      (rt/all-facts 'my.ns)))
  => nil)

^{:refer code.test.compile/fact:list :added "3.0"
  :style/indent 1}
(fact "lists all facts in namespace"
  ^:hidden
  
  (ctx/with-new-context {:registry (atom {'my.ns {:facts {'a {:id 'a :line 1}}}})}
    (binding [*ns* (create-ns 'my.ns)]
      (fact:list)))
  => '(a))

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
