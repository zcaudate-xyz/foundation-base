(ns xtbench.dart.lang.spec-promise-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-base :as xt])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-promise :as spec-promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-promise/x:promise-then :added "4.1"}
(fact "chains a resolved js promise"

  (notify/wait-on :dart
    (spec-promise/x:promise-then
     (spec-promise/x:promise
      (fn []
        (return 5)))
     (fn [value]
       (repl/notify (+ value 2)))))
  => 7)

^{:refer xt.lang.spec-promise/x:promise-catch :added "4.1"}
(fact "recovers a rejected js promise"

  (notify/wait-on :dart
    (spec-promise/x:promise-catch
     (spec-promise/x:promise
      (fn []
        (throw "boom")))
     (fn [err]
       (repl/notify err)
       (return err))))
  => "boom")

^{:refer xt.lang.spec-promise/x:promise-finally :added "4.1"}
(fact "runs cleanup without changing the resolved value"

  (notify/wait-on :dart
    (var out [])
    (spec-promise/x:promise-then
     (spec-promise/x:promise-finally
      (spec-promise/x:promise-then
       (spec-promise/x:promise
        (fn []
          (return 5)))
       (fn [value]
         (xt/x:arr-push out "then")
         (return (+ value 2))))
      (fn []
        (xt/x:arr-push out "finally")))
     (fn [value]
       (return (repl/notify [out value])))))
  => [["then" "finally"] 7])

^{:refer xt.lang.spec-promise/x:promise-native? :added "4.1"}
(fact "detects native js promises"

  (!.dt
    (var p
         (spec-promise/x:promise
          (fn []
            (return 1))))
    [(spec-promise/x:promise-native? p)
     (spec-promise/x:promise-native? 1)])
  => [true false])

^{:refer xt.lang.spec-promise/x:with-delay :added "4.1"}
(fact "delays asynchronous js computations"

  (notify/wait-on :dart
    (spec-promise/x:with-delay 100
                               (fn []
                                 (repl/notify "OK"))))
  => "OK")

(comment

  (s/seedgen-benchadd '[xt.lang.spec-promise] {:lang [:dart] :write true})
  
  (s/seedgen-langadd 'xt.lang.common-promise {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-promise {:lang [:lua :python] :write true}))

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
    => ["resolved" true 7 42 7]))
