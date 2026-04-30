(ns xtbench.dart.lang.common-promise-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-base :as xt])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-promise :as common-promise]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-promise/promise-run :added "4.1"}
(fact "common promise helpers use the :: xt.promise wrapper"

  (!.dt
    (var p (common-promise/promise-run 5))
    [(common-promise/promise-native? p)
     (xt/x:get-key p "::")
     (xt/x:get-key p "status")
     (xt/x:get-key p "value")])
  => [true "xt.promise" "resolved" 5])

^{:refer xt.lang.common-promise/promise-then :added "4.1"}
(fact "common promise helpers resolve delayed async work through the wrapper"

  (notify/wait-on :dart
    (common-promise/promise-then
     (common-promise/promise
      (fn []
        (return 5)))
     (repl/>notify)))
  => 5)

^{:refer xt.lang.common-promise/promise-finally :added "4.1"}
(fact "common promise helpers preserve errors and cleanup order"

  (notify/wait-on :dart
    (var out [])
    (common-promise/promise-catch
     (common-promise/promise-finally
      (common-promise/promise
       (fn []
         (throw (xt/x:ex "boom" {:a 1}))))
      (fn []
        (xt/x:arr-push out "finally")))
     (fn [err]
       (repl/notify [out
                     (xt/x:ex-native? err)
                     (xt/x:get-key (xt/x:ex-data err) "a")]))))
  => [["finally"] true 1])
