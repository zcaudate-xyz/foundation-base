(ns code.test.checker.common-test
  (:use [code.test :exclude [satisfies anything approx throws exactly stores capture]])
  (:require [code.test.checker.common :refer :all]
            [code.test.checker.common :as common]
            [std.lib :as h]
            [std.lib.result :as res]))

^{:refer code.test.checker.common/function-string :added "3.0"}
(fact "returns the string representation of a function"

  (function-string every?) => "every?"

  (function-string reset!) => "reset!")

^{:refer code.test.checker.common/checker :added "3.0"}
(fact "creates a 'code.test.checker.common.Checker' object"

  (checker {:tag :anything :fn (fn [x] true)})
  => code.test.checker.common.Checker)

^{:refer code.test.checker.common/checker? :added "3.0"}
(fact "checks to see if a datastructure is a 'code.test.checker.common.Checker'"

  (checker? (checker {:tag :anything :fn (fn [x] true)}))
  => true)

^{:refer code.test.checker.common/verify :added "3.0"}
(fact "verifies a value with it's associated check"

  (verify (satisfies 2) 1)
  => (contains-in {:status :success
                   :data false
                   :checker {:tag :satisfies
                             :doc string?
                             :expect 2}
                   :actual 1
                   :from :verify})

  (verify (->checker #(/ % 0)) 1)
  => (contains {:status :exception
                :data java.lang.ArithmeticException
                :from :verify}))

^{:refer code.test.checker.common/succeeded? :added "3.0" :class [:test/checker]}
(fact "determines if the results of a check have succeeded"

  (-> (satisfies Long)
      (verify 1)
      succeeded?)
  => true

  (-> (satisfies even?)
      (verify 1)
      succeeded?)
  => false)

^{:refer code.test.checker.common/throws :added "3.0" :class [:test/checker]}
(fact "checker that determines if an exception has been thrown"

  ((throws Exception "Hello There")
   (res/result
    {:status :exception
     :data (Exception. "Hello There")}))
  => true)

^{:refer code.test.checker.common/exactly :added "3.0" :class [:test/checker]}
(fact "checker that allows exact verifications"

  ((exactly 1) 1) => true

  ((exactly Long) 1) => false

  ((exactly number?) 1) => false)

^{:refer code.test.checker.common/approx :added "3.0" :class [:test/checker]}
(fact "checker that allows approximate verifications"

  ((approx 1) 1.000001) => true

  ((approx 1) 1.1) => false

  ((approx 1 0.0000001) 1.001) => false)

^{:refer code.test.checker.common/satisfies :added "3.0" :class [:test/checker]}
(fact "checker that allows loose verifications"

  ((satisfies 1) 1) => true

  ((satisfies Long) 1) => true

  ((satisfies number?) 1) => true

  ((satisfies #{1 2 3}) 1) => true

  ((satisfies [1 2 3]) 1) => false

  ((satisfies number?) "e") => false

  ((satisfies #"hello") #"hello") => true)

^{:refer code.test.checker.common/stores :added "3.0"}
(fact "a checker that looks into a ref object"

  ((stores 1) (volatile! 1)) => true

  ((stores 1) 1) => false)

^{:refer code.test.checker.common/anything :added "3.0" :class [:test/checker]}
(fact "a checker that returns true for any value"

  (anything nil) => true

  (anything [:hello :world]) => true)

^{:refer code.test.checker.common/->checker :added "3.0"}
(fact "creates a 'satisfies' checker if not already a checker"

  ((->checker 1) 1) => true

  ((->checker (exactly 1)) 1) => true)

(fact "capture macro captures a value"
  (let [ns (create-ns 'test.capture)
        checker (binding [*ns* ns]
                  (eval `(capture ~'x)))]
    (checker 10) => true
    @(ns-resolve 'test.capture 'x) => 10
    (remove-ns 'test.capture)))

(comment
  (./arrange)
  (./import))
