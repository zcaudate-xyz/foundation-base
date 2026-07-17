(ns hara.runtime.basic.impl-annex.process-r-test
  (:require [hara.runtime.basic.impl-annex.process-r :refer :all]
            [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(fact:global
 {:skip (not (env/program-exists? "R"))
  :setup    [(l/script- :r {:runtime :oneshot})]
  :teardown []})

^{:refer hara.runtime.basic.impl-annex.process-r/CANARY :adopt true :added "4.0"}
(fact "EVALUATE r code"
  
  (!.R (+ 1 2 3 4))
  => 10

  (!.R [1 2 3 4])
  => [1 2 3 4]

  (!.R (mean [1 2 3 4]))
  => 2.5)

^{:refer hara.runtime.basic.impl-annex.process-r/default-oneshot-wrap  :adopt true :added "4.0"}
(fact "creates the oneshot form"

  (default-oneshot-wrap 1)
  => string?)

^{:refer hara.runtime.basic.impl-annex.process-r/default-basic-client  :adopt true :added "4.0"}
(fact "creates the oneshot form"

  (default-basic-client 19000)
  => string?)

^{:refer hara.runtime.basic.impl-annex.process-r/default-oneshot-trim :added "4.0"}
(fact "trim for oneshot"

  (default-oneshot-trim "{\"type\":\"data\",\"return\":\"number\",\"value\":1}")
  => "{\"type\":\"data\",\"return\":\"number\",\"value\":1}")

^{:refer hara.runtime.basic.impl-annex.process-r/CANARY :adopt true :added "4.1"
  :id test-r-canary-grammar-additions}
(fact "R grammar additions"
  (!.R (df {:a [1 2] :b [3 4]}))
  => [{:a 1, :b 3} {:a 2, :b 4}]

  (!.R (formula mpg cyl))
  => "~ mpg cyl"

  (!.R (|> [1 2 3 4] (mean)))
  => 2.5

  (!.R (%in% 2 [1 2 3]))
  => true

  (!.R [NA NaN Inf])
  => [nil NaN Inf])

^{:refer hara.runtime.basic.impl-annex.process-r/CANARY :adopt true :added "4.1"
  :id test-r-canary-errors}
(fact "R errors are propagated"
  (!.R (throw "boom"))
  => (throws clojure.lang.ExceptionInfo))


^{:refer hara.runtime.basic.impl-annex.process-r/default-body-transform :added "4.0"}
(fact "wraps body forms in an R function with explicit return"
  (default-body-transform '[1 2 3] {})
  => '((fn [] (return [1 2 3])))

  (default-body-transform '(do (defn add-10 [x] (+ x 10))
                               (add-10 5))
                          {})
  => '((fn [] (def add-10 (fn [x] (+ x 10)))
         (return (add-10 5))))

  (default-body-transform '[1] {})
  => '((fn [] (return [1]))))
