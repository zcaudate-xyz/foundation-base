(ns xt.event.node-main-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node :as node]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node/list-subscriptions :added "4.1"}
(fact "tracks router subscriptions by space and signal"

  (!.js
    (var n (node/node-create {}))
    (node/router-register-connection n "peer-a" nil)
    (node/router-register-connection n "peer-b" nil)
    (node/router-add-subscription n "peer-a" "room/a" "event/ping" "sub-a" nil)
    (node/router-add-subscription n "peer-b" "room/b" "event/ping" "sub-b" nil)
    [(node/list-subscriptions n "room/a" "event/ping")
     (node/list-subscriptions n "room/b" "event/ping")
     (xt/x:obj-keys (node/router-connections n))])
  => [["peer-a"]
      ["peer-b"]
      ["peer-a" "peer-b"]])

^{:refer xt.event.node/router-unregister-connection :added "4.1"}
(fact "removes connection subscriptions when a router connection is removed"

  (!.js
    (var n (node/node-create {}))
    (node/router-register-connection n "peer-a" nil)
    (node/router-add-subscription n "peer-a" "room/a" "event/ping" "sub-a" nil)
    (node/router-unregister-connection n "peer-a")
    [(node/list-subscriptions n "room/a" "event/ping")
     (xt/x:obj-keys (node/router-connections n))])
  => [[] []])

^{:refer xt.event.node-main/transport-create :added "4.1"}
(fact "constructs node and transport values"

  (!.js
    (var n (node/node-create {:id "node-a"}))
    (var t (node/transport-create
            "peer-a"
            {"send-fn" (fn [event]
                         (return true))}))
    [(node/node? n)
     (node/transport? t)
     (. n ["id"])
     (. t ["id"])
     (node/request-target n {"transport-id" "peer-a"})])
  => [true true "node-a" "peer-a" "peer-a"])

^{:refer xt.event.node-main/receive-frame :added "4.1"}
(fact "applies subscribe and unsubscribe control frames through receive-frame"

  (!.js
    (var n (node/node-create {}))
    (node/router-register-connection n "peer-a" nil)
    (node/receive-frame
     n
     (node/subscribe-frame "room/a" "event/ping" "sub-a" nil)
     {"transport-id" "peer-a"})
    (var before
      (xt/x:get-key
       (xt/x:get-key
        (xt/x:get-key (node/router-subscriptions n) "room/a")
        "event/ping")
       "peer-a"))
    (node/receive-frame
     n
     (node/unsubscribe-frame "room/a" "event/ping" "sub-a" nil)
     {"transport-id" "peer-a"})
    [(xt/x:get-key before "id")
     (node/list-subscriptions n "room/a" "event/ping")])
  => ["sub-a" []])


^{:refer xt.event.node-main/node? :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/transport? :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/transport-create :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/node-create :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/register-handler :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/unregister-handler :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/get-handler :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/list-handlers :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/register-trigger :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/unregister-trigger :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/get-trigger :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/list-triggers :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/get-transport :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/list-transports :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/send-transport :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/broadcast-transport-loop :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/broadcast-transport :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/route-stream-loop :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/route-stream :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/attach-transport :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/detach-transport :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/request-target :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/respond-ok :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/respond-error :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/receive-request :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/receive-response :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/request :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/subscribe :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/unsubscribe :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/publish :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/receive-publish :added "4.1"}
(fact "TODO")

^{:refer xt.event.node-main/receive-frame :added "4.1"}
(fact "TODO")



(comment
  (s/snapto)
  (s/seedgen-langadd '[xt.event.util-validate]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.node-]  {:lang [:lua :python] :write true}))
