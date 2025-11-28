(ns code.test.compile.snippet-test
  (:require [code.test.compile.snippet :refer :all :exclude [=> *last*]]
            [code.test.base.process :as process]
            [code.test :refer [fact fact:global contains-in]]))

^{:refer code.test.compile.snippet/vecify :added "3.0"}
(fact "puts the item in a vector if not already"

  (vecify 1)
  => [1])

^{:refer code.test.compile.snippet/fact-setup :added "3.0"}
(fact "creates a setup hook"
  ^:hidden

  (fact-setup '{:setup (prn (+ a b))})
  => '(clojure.core/fn [] (prn (+ a b))))

^{:refer code.test.compile.snippet/fact-teardown :added "3.0"}
(fact "creates a teardown hook"
  ^:hidden

  (fact-teardown '{:teardown [(prn "goodbye")]})
  => '(clojure.core/fn [] (prn "goodbye")))

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

^{:refer code.test.compile.snippet/fact-wrap-check :added "3.0"}
(fact "creates a wrapper for before and after arrows")

^{:refer code.test.compile.snippet/fact-slim :added "3.0"}
(fact "creates the slim thunk"

  (fact-slim '[(+ a b)]))
