(ns xt.lang.spec-promise-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :python
  {:runtime :basic
   :require [[python.core.common-promise :as pp]]})

(l/script- :lua
  {:runtime :basic
   :require [[lua.core.common-promise :as lp]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-promise/x:promise-then :added "4.1"}
(fact "TODO")

^{:refer xt.lang.spec-promise/x:promise-catch :added "4.1"}
(fact "TODO")

^{:refer xt.lang.spec-promise/x:promise-finally :added "4.1"}
(fact "TODO")

^{:refer xt.lang.spec-promise/x:promise-native? :added "4.1"}
(fact "TODO")


^{:refer xt.lang.spec-promise/x:with-delay :added "4.1"}
(fact "delays asynchronous js computations"

  (notify/wait-on :js
                  (spec-promise/x:with-delay 20
                                             (fn []
                                               (repl/notify "OK"))))
  => "LATER"
  
  (notify/wait-on :python
                  (spec-promise/x:with-delay 20
                                             (fn []
                                               (repl/notify "OK"))))
  => "LATER"

  (notify/wait-on :lua
                  (spec-promise/x:with-delay 20
                                             (fn []
                                               (repl/notify "OK"))))
  => "LATER")


(comment
  
^{:refer xt.lang.spec-base/x:promise :added "4.1"}
(fact "python runtime hardlinks promise helpers"
  (!.py
    (var resolved (pp/promise (fn [] (return 5))))
    (var chained (pp/promise-then resolved (fn [value] (return (+ value 2)))))
    (var rejected (pp/promise (fn [] (/ 1 0))))
    (var handled (pp/promise-catch rejected (fn [err] (return 42))))
    (var finaled (pp/promise-finally chained (fn [] (return "ignored"))))
    [(. resolved ["status"])
     (pp/promise-native? resolved)
     (. chained ["value"])
     (. handled ["value"])
     (. finaled ["value"])])
  => ["resolved" true 7 42 7]

  (!.py
    (:- :import asyncio)
    (var resolved (pp/promise (fn [] (. asyncio (sleep 0 :result 7)))))
    (var chained (pp/promise-then resolved
                                  (fn [value]
                                    (return (. asyncio (sleep 0 :result (+ value 2)))))))
    (var rejected (pp/promise (fn [] (/ 1 0))))
    (var handled (pp/promise-catch rejected
                                   (fn [err]
                                     (return (. asyncio (sleep 0 :result "handled"))))))
    (var finaled (pp/promise-finally chained
                                     (fn []
                                       (return (. asyncio (sleep 0 :result "cleanup"))))))
    [(. resolved ["status"])
     (. resolved ["value"])
     (. chained ["value"])
     (. handled ["value"])
     (. finaled ["value"])])
  => ["resolved" 7 9 "handled" 9]

  (!.lua
    (var resolved (lp/promise (fn [] (return 5))))
    (var chained (lp/promise-then resolved (fn [value] (return (+ value 2)))))
    (var rejected (lp/promise (fn [] (error "boom"))))
    (var handled (lp/promise-catch rejected (fn [err] (return 42))))
    (var finaled (lp/promise-finally chained (fn [] (return "ignored"))))
    [(. resolved ["status"])
     (lp/promise-native? resolved)
     (. chained ["value"])
     (. handled ["value"])
     (. finaled ["value"])])
  => ["resolved" true 7 42 7])

  )
