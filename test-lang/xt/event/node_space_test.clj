(ns xt.event.node-space-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node :as node]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.event.node/create-space :added "4.1"}
(fact "manages per-space state independently from node-level handlers"

  (!.js
    (var n (node/node-create {}))
    (node/create-space n "alpha" {:state {:count 1}})
    (node/ensure-space n "beta" nil)
    (node/set-space-state n "beta" {:count 2})
    (node/update-space-state
     n
     "beta"
     (fn [state entry node]
       (return (xt/x:obj-assign state {:count 3
                                       :id (. entry ["id"])}))))
    [(xt/x:len (node/list-spaces n))
     (. (node/get-space n "alpha") ["id"])
     (. (node/get-space-state n "alpha") ["count"])
     (. (node/get-space-state n "beta") ["count"])
     (. (node/get-space-state n "beta") ["id"])
     (. (node/remove-space n "alpha") ["id"])
     (node/list-spaces n)])
  => [2 "alpha" 1 3 "beta" "alpha" ["beta"]])

^{:refer xt.event.node-space/space :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-space/get-space :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-space/ensure-space :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-space/remove-space :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-space/list-spaces :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-space/get-space-state :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-space/set-space-state :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-space/update-space-state :added "4.1"}
(fact "TODO")
