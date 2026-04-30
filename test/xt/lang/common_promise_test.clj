(ns xt.lang.common-promise-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-promise :as common-promise]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [lua.core.common-promise]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-promise :as common-promise]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [python.core.common-promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-promise/promise-run :added "4.1"}
(fact "common promise helpers derive chaining from async primitives"
  (notify/wait-on :python
    (common-promise/promise-then
     (common-promise/promise-run 5)
     (repl/>notify)))
  => 5

  (notify/wait-on :lua
    (common-promise/promise-then
     (common-promise/promise-run 5)
     (repl/>notify)))
  => 5)

^{:refer xt.lang.common-promise/promise-finally :added "4.1"}
(fact "common promise helpers preserve errors and cleanup order"
  (notify/wait-on :python
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
  => [["finally"] true 1]

  (notify/wait-on :lua
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
