(ns xt.substrate.base-pubsub-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate :as node]
             [xt.substrate.base-pubsub :as pubsub]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate :as node]
             [xt.substrate.base-pubsub :as pubsub]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate :as node]
             [xt.substrate.base-pubsub :as pubsub]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-pubsub/subscribe :added "4.1"}
(fact "constructs a subscription frame for the transport"

  (!.js
    (var n (node/node-create {}))
    (var frame (pubsub/subscribe n "room/a" "event/updated" "sub-1" nil))
    [(. frame ["kind"])
     (. frame ["space"])
     (. frame ["signal"])
     (. frame ["id"])])
  => ["subscribe" "room/a" "event/updated" "sub-1"]

  (!.lua
    (var n (node/node-create {}))
    (var frame (pubsub/subscribe n "room/a" "event/updated" "sub-1" nil))
    [(. frame ["kind"])
     (. frame ["space"])
     (. frame ["signal"])
     (. frame ["id"])])
  => ["subscribe" "room/a" "event/updated" "sub-1"]

  (!.py
    (var n (node/node-create {}))
    (var frame (pubsub/subscribe n "room/a" "event/updated" "sub-1" nil))
    [(. frame ["kind"])
     (. frame ["space"])
     (. frame ["signal"])
     (. frame ["id"])])
  => ["subscribe" "room/a" "event/updated" "sub-1"])

^{:refer xt.substrate.base-pubsub/unsubscribe :added "4.1"}
(fact "constructs an unsubscribe frame for the transport"

  (!.js
    (var n (node/node-create {}))
    (var frame (pubsub/unsubscribe n "room/a" "event/updated" "sub-1" nil))
    [(. frame ["kind"])
     (. frame ["space"])
     (. frame ["signal"])
     (. frame ["id"])])
  => ["unsubscribe" "room/a" "event/updated" "sub-1"]

  (!.lua
    (var n (node/node-create {}))
    (var frame (pubsub/unsubscribe n "room/a" "event/updated" "sub-1" nil))
    [(. frame ["kind"])
     (. frame ["space"])
     (. frame ["signal"])
     (. frame ["id"])])
  => ["unsubscribe" "room/a" "event/updated" "sub-1"]

  (!.py
    (var n (node/node-create {}))
    (var frame (pubsub/unsubscribe n "room/a" "event/updated" "sub-1" nil))
    [(. frame ["kind"])
     (. frame ["space"])
     (. frame ["signal"])
     (. frame ["id"])])
  => ["unsubscribe" "room/a" "event/updated" "sub-1"])

^{:refer xt.substrate.base-pubsub/invoke-trigger :added "4.1"}
(fact "invokes a shared trigger against the selected space"

  (!.js
    (var n (node/node-create {}))
    (var calls [])
    (node/register-trigger
     n
     "event/updated"
     (fn [space stream node]
       (xt/x:arr-push calls
                      [(. space ["id"])
                       (. stream ["data"] ["value"])])
       (return {:space (. space ["id"])
                :value (. stream ["data"] ["value"])}))
     nil)
    (pubsub/invoke-trigger
     n
     (frame/stream-frame "room/a"
                         "event/updated"
                         {:value 3}
                         nil
                         nil))
    calls)
  => [["room/a" 3]]

  (!.lua
    (var n (node/node-create {}))
    (var calls [])
    (node/register-trigger
     n
     "event/updated"
     (fn [space stream node]
       (xt/x:arr-push calls
                      [(. space ["id"])
                       (. stream ["data"] ["value"])])
       (return {:space (. space ["id"])
                :value (. stream ["data"] ["value"])}))
     nil)
    (pubsub/invoke-trigger
     n
     (frame/stream-frame "room/a"
                         "event/updated"
                         {:value 3}
                         nil
                         nil))
    calls)
  => [["room/a" 3]]

  (!.py
    (var n (node/node-create {}))
    (var calls [])
    (node/register-trigger
     n
     "event/updated"
     (fn [space stream node]
       (xt/x:arr-push calls
                      [(. space ["id"])
                       (. stream ["data"] ["value"])])
       (return {:space (. space ["id"])
                :value (. stream ["data"] ["value"])}))
     nil)
    (pubsub/invoke-trigger
     n
     (frame/stream-frame "room/a"
                         "event/updated"
                         {:value 3}
                         nil
                         nil))
    calls)
  => [["room/a" 3]])

^{:refer xt.substrate.base-pubsub/receive-publish :added "4.1"}
(fact "dispatches stream frames to shared triggers"

  (!.js
    (var n (node/node-create {}))
    (node/register-trigger
     n
     "event/updated"
     (fn [space stream node]
       (node/set-space-state node
                             (. space ["id"])
                             (. stream ["data"]))
       (return true))
     nil)
    (pubsub/receive-publish
     n
     (frame/stream-frame "room/a"
                         "event/updated"
                         {:value 9}
                         nil
                         nil))
    [(. (node/get-space-state n "room/a") ["value"])])
  => [9]

  (!.lua
    (var n (node/node-create {}))
    (node/register-trigger
     n
     "event/updated"
     (fn [space stream node]
       (node/set-space-state node
                             (. space ["id"])
                             (. stream ["data"]))
       (return true))
     nil)
    (pubsub/receive-publish
     n
     (frame/stream-frame "room/a"
                         "event/updated"
                         {:value 9}
                         nil
                         nil))
    [(. (node/get-space-state n "room/a") ["value"])])
  => [9]

  (!.py
    (var n (node/node-create {}))
    (node/register-trigger
     n
     "event/updated"
     (fn [space stream node]
       (node/set-space-state node
                             (. space ["id"])
                             (. stream ["data"]))
       (return true))
     nil)
    (pubsub/receive-publish
     n
     (frame/stream-frame "room/a"
                         "event/updated"
                         {:value 9}
                         nil
                         nil))
     [(. (node/get-space-state n "room/a") ["value"])])
  => [9])

(comment
  (s/snapto '[xt.substrate.base-pubsub])
  (s/seedgen-langremove '[xt.substrate.base-pubsub] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.substrate.base-pubsub] {:lang [:lua :python] :write true}))
