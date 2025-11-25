(ns code.test.compile.snippet-test
  (:require [code.test.compile.snippet :refer :all :exclude [=> *last*]]
            [code.test.base.process :as process]
            [code.test.base.context :as ctx]
            [code.test :refer [fact fact:global contains-in]]))

(fact:global
 {:component {*serv* {:create (list :database)
                      :setup  'start}}})

(fact "fact-use function"
  (fact-use '*serv* [:setup])
  => '((start *serv*)))

(fact "fact-wrap-bindings with use"
  (fact-wrap-bindings '{:use [*serv*]
                        :let [a 1]})
  => '(clojure.core/fn [thunk]
        (clojure.core/fn []
          (clojure.core/binding [*serv* (start (list :database))
                                 a 1]
            (thunk)))))

(fact "fact-wrap-check function"
  (let [f (eval (fact-wrap-check {:check {:before '(1) :after '(2)}}))
        thunk (fn [] "hello")]
    (ctx/with-context {:results (atom [])}
      ((f thunk)) => "hello")))

(fact "fact-slim function"
  (let [f (eval (fact-slim '[(+ 1 1)]))]
    (f) => 2))

^{:refer code.test.compile.snippet/vecify :added "3.0"}
(fact "puts the item in a vector if not already"

  (vecify 1)
  => [1])

^{:refer code.test.compile.snippet/replace-bindings :added "3.0"}
(fact "replaces values of current bindings"

  (replace-bindings '[a 1 b 2 c 3]
                    '[b 4])
  => '[a 1 b 4 c 3])

^{:refer code.test.compile.snippet/wrap-bindings :added "3.0"}
(fact "wraps the form in bindings"

  (wrap-bindings '(+ a b)
                 '[a 1 b (+ a 1)])
  => '(clojure.core/binding [a 1]
        (clojure.core/binding [b (+ a 1)]
          (+ a b))))

^{:refer code.test.compile.snippet/fact-use-setup-eval :added "3.0"}
(fact "evaluates the setup form for a `fact:use` component"

  (fact-use-setup-eval '*serv*)
  => '(list :database))

^{:refer code.test.compile.snippet/fact-use-setup :added "3.0"}
(fact "prepares the setup form for a `fact:use` component, including any necessary initialization"

  (fact-use-setup '*serv*)
  => '(start (list :database)))

^{:refer code.test.compile.snippet/fact-use-teardown :added "3.0"}
(fact "teardown form for 'use'"

  (fact-use-teardown '*serv*)
  => '(clojure.core/identity *serv*))

^{:refer code.test.compile.snippet/fact-let-defs :added "3.0"}
(fact "create def forms for symbols"

  (fact-let-defs '{:let [a 1 b 2]})
  => '[(def a 1) (def b 2)])

^{:refer code.test.compile.snippet/fact-let-declare :added "3.0"}
(fact "creates let declarations"

  (fact-let-declare '{:let [a 1 b 2 c 3]
                      :use [*serv*]})
  => '[(def a nil) (def b nil) (def c nil) (def *serv* nil)])

^{:refer code.test.compile.snippet/fact-declare :added "3.0"}
(fact "creates a declare hook" ^:hidden

  (fact-declare '{:let [a 1 b 2]
                  :use [*serv*]})
  => '(clojure.core/fn [] (def a nil) (def b nil) (def *serv* nil)))

^{:refer code.test.compile.snippet/fact-setup :added "3.0"}
(fact "creates a setup hook" ^:hidden

  (fact-setup '{:let [a 1 b 2]
                :setup (prn (+ a b))})
  => '(clojure.core/fn [] (def a 1) (def b 2) (prn (+ a b))))

^{:refer code.test.compile.snippet/fact-teardown :added "3.0"}
(fact "creates a teardown hook" ^:hidden

  (fact-teardown '{:teardown [(prn "goodbye")]})
  => '(clojure.core/fn [] (prn "goodbye")))

^{:refer code.test.compile.snippet/fact-wrap-replace :added "3.0"}
(fact "creates a replace wrapper"

  (fact-wrap-replace '{:use [*serv*]
                       :let [a 1 b 2]
                       :replace {+ -}})
  ^:hidden
  => '(clojure.core/fn [thunk]
        (clojure.core/fn []
          (clojure.core/binding [code.test.base.runtime/*eval-replace*
                                 (quote {+ -, a 1, b 2})]
            (thunk)))))

^{:refer code.test.compile.snippet/fact-wrap-ceremony :added "3.0"}
(fact "creates the setup/teardown wrapper"

  (fact-wrap-ceremony '{:setup [(prn 1 2 3)]
                        :teardown (prn "goodbye")}) ^:hidden
  => (clojure.core/fn [thunk]
       (clojure.core/fn []
         (clojure.core/let [_ [(prn 1 2 3)]
                            out (thunk)
                            _ (prn "goodbye")]
           out))))
