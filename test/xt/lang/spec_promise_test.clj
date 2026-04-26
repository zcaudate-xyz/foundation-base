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

^{:refer xt.lang.spec-base/x:promise :added "4.1"}
(fact "python runtime hardlinks promise helpers"
  (!.py
    (defn.py boom []
      (throw (Exception "boom")))
    (var resolved (pp/promise (fn [] (return 5))))
    (var chained (pp/promise-then resolved (fn [value] (return (+ value 2)))))
    (var rejected (pp/promise boom))
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
    (defn.py ^{:- [:async]} async-add [value]
      (:- :await (. asyncio (sleep 0)))
      (return (+ value 2)))
    (defn.py ^{:- [:async]} async-boom [msg]
      (:- :await (. asyncio (sleep 0)))
      (throw (Exception msg)))
    (defn.py ^{:- [:async]} async-tag [label]
      (:- :await (. asyncio (sleep 0)))
      (return label))
    (var resolved (pp/promise (fn [] (async-add 5))))
    (var chained (pp/promise-then resolved (fn [value] (return (async-add value)))))
    (var rejected (pp/promise (fn [] (async-boom "boom"))))
    (var handled (pp/promise-catch rejected (fn [err] (return (async-tag "handled")))))
    (var finaled (pp/promise-finally chained (fn [] (return (async-tag "cleanup")))))
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
