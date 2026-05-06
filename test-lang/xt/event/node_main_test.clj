(ns xt.event.node-main-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
           {:runtime :basic
            :require [[xt.lang.spec-base :as xt]
                      [xt.lang.spec-promise :as promise]
                      [xt.event.node :as node]
                      [xt.event.node-main :as main]
                      [xt.event.node-router :as router]
                      [xt.event.node-request :as req]]})

(l/script- :lua
           {:runtime :basic
            :require [[xt.lang.spec-base :as xt]
                      [xt.lang.spec-promise :as promise]
                      [xt.event.node :as node]
                      [xt.event.node-main :as main]
                      [xt.event.node-router :as router]
                      [xt.event.node-request :as req]]})

(l/script- :python
           {:runtime :basic
            :require [[xt.lang.spec-base :as xt]
                      [xt.lang.spec-promise :as promise]
                      [xt.event.node :as node]
                      [xt.event.node-main :as main]
                      [xt.event.node-router :as router]
                      [xt.event.node-request :as req]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.event.node-router/list-subscriptions :added "4.1"
  :setup [(def +out+
            (just-in
             [(just ["peer-a" "peer-b"] :in-any-order)
              empty?]))]}
(fact "lists router subscriptions by space"

  (!.js
    (var n (main/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" nil nil)
    (router/add-subscription n "peer-b" "room/a" "event/ping" nil nil)
    [(router/list-subscriptions n "room/a" "event/ping")
     (router/list-subscriptions n "room/b" "event/ping")])
  => +out+

  (!.lua
    (var n (main/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" nil nil)
    (router/add-subscription n "peer-b" "room/a" "event/ping" nil nil)
    [(router/list-subscriptions n "room/a" "event/ping")
     (router/list-subscriptions n "room/b" "event/ping")])
  => +out+

  (!.py
    (var n (main/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" nil nil)
    (router/add-subscription n "peer-b" "room/a" "event/ping" nil nil)
    [(router/list-subscriptions n "room/a" "event/ping")
     (router/list-subscriptions n "room/b" "event/ping")])
  => +out+)

^{:refer xt.event.node-router/unregister-connection :added "4.1"}
(fact "removing a transport prunes its subscriptions"

  (!.js
    (var n (main/node-create {}))
    (router/register-connection n "peer-a" {})
    (router/register-connection n "peer-b" {})
    (router/add-subscription n "peer-a" "room/a" "event/ping" nil nil)
    (router/add-subscription n "peer-b" "room/a" "event/ping" nil nil)
    (router/unregister-connection n "peer-a")
    (router/list-subscriptions n "room/a" "event/ping"))
  => ["peer-b"]

  (!.lua
    (var n (main/node-create {}))
    (router/register-connection n "peer-a" {})
    (router/register-connection n "peer-b" {})
    (router/add-subscription n "peer-a" "room/a" "event/ping" nil nil)
    (router/add-subscription n "peer-b" "room/a" "event/ping" nil nil)
    (router/unregister-connection n "peer-a")
    (router/list-subscriptions n "room/a" "event/ping"))
  => ["peer-b"]

  (!.py
    (var n (main/node-create {}))
    (router/register-connection n "peer-a" {})
    (router/register-connection n "peer-b" {})
    (router/add-subscription n "peer-a" "room/a" "event/ping" nil nil)
    (router/add-subscription n "peer-b" "room/a" "event/ping" nil nil)
    (router/unregister-connection n "peer-a")
    (router/list-subscriptions n "room/a" "event/ping"))
  => ["peer-b"])

^{:refer xt.event.node-main/node? :added "4.1"}
(fact "detects node values"

  (!.js
    (main/node? (main/node-create {})))
  => true

  (!.lua
    (main/node? (main/node-create {})))
  => true

  (!.py
    (main/node? (main/node-create {})))
  => true)

^{:refer xt.event.node-main/transport? :added "4.1"}
(fact "detects transport values"

  (!.js
    (main/transport? (main/transport-create "peer-a" {})))
  => true

  (!.lua
    (main/transport? (main/transport-create "peer-a" {})))
  => true

  (!.py
    (main/transport? (main/transport-create "peer-a" {})))
  => true)

^{:refer xt.event.node-main/transport-create :added "4.1"}
(fact "creates transport entries"

  (!.js
    (var transport (main/transport-create "peer-a" {"meta" {"role" "edge"}}))
    [(. transport ["::"])
     (. transport ["id"])
     (. transport ["meta"] ["role"])])
  => ["event.node.transport" "peer-a" "edge"]

  (!.lua
    (var transport (main/transport-create "peer-a" {"meta" {"role" "edge"}}))
    [(. transport ["::"])
     (. transport ["id"])
     (. transport ["meta"] ["role"])])
  => ["event.node.transport" "peer-a" "edge"]

  (!.py
    (var transport (main/transport-create "peer-a" {"meta" {"role" "edge"}}))
    [(. transport ["::"])
     (. transport ["id"])
     (. transport ["meta"] ["role"])])
  => ["event.node.transport" "peer-a" "edge"])

^{:refer xt.event.node-main/node-create :added "4.1"
  :setup [(def +out+
            (just-in
             ["event.node"
              empty?
              empty?
              empty?
              (just ["connections" "subscriptions"] :in-any-order)]))]}
(fact "creates node state with router and registries"

  (!.js
    (var n (main/node-create {}))
    [(. n ["::"])
     (xt/x:obj-keys (. n ["spaces"]))
     (xt/x:obj-keys (. n ["handlers"]))
     (xt/x:obj-keys (. n ["triggers"]))
     (xt/x:obj-keys (. n ["router"]))])
  => +out+

  (!.lua
    (var n (main/node-create {}))
    [(. n ["::"])
     (xt/x:obj-keys (. n ["spaces"]))
     (xt/x:obj-keys (. n ["handlers"]))
     (xt/x:obj-keys (. n ["triggers"]))
     (xt/x:obj-keys (. n ["router"]))])
  => +out+

  (!.py
    (var n (main/node-create {}))
    [(. n ["::"])
     (xt/x:obj-keys (. n ["spaces"]))
     (xt/x:obj-keys (. n ["handlers"]))
     (xt/x:obj-keys (. n ["triggers"]))
     (xt/x:obj-keys (. n ["router"]))])
  => +out+)

^{:refer xt.event.node-main/register-handler :added "4.1"}
(fact "registers handlers on the node"

  (!.js
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) {"role" "test"})
    [(. (main/get-handler n "echo") ["id"])
     (. (main/get-handler n "echo") ["meta"] ["role"])])
  => ["echo" "test"]

  (!.lua
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) {"role" "test"})
    [(. (main/get-handler n "echo") ["id"])
     (. (main/get-handler n "echo") ["meta"] ["role"])])
  => ["echo" "test"]

  (!.py
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) {"role" "test"})
    [(. (main/get-handler n "echo") ["id"])
     (. (main/get-handler n "echo") ["meta"] ["role"])])
  => ["echo" "test"])

^{:refer xt.event.node-main/unregister-handler :added "4.1"}
(fact "unregisters handlers from the node"

  (!.js
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (main/unregister-handler n "echo")
    (main/get-handler n "echo"))
  => nil

  (!.lua
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (main/unregister-handler n "echo")
    (main/get-handler n "echo"))
  => nil

  (!.py
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (main/unregister-handler n "echo")
    (main/get-handler n "echo"))
  => nil)

^{:refer xt.event.node-main/get-handler :added "4.1"}
(fact "gets handler entries"

  (!.js
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (. (main/get-handler n "echo") ["id"]))
  => "echo"

  (!.lua
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (. (main/get-handler n "echo") ["id"]))
  => "echo"

  (!.py
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (. (main/get-handler n "echo") ["id"]))
  => "echo")

^{:refer xt.event.node-main/list-handlers :added "4.1"}
(fact "lists registered handlers"

  (!.js
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (main/register-handler n "sum" (fn [ctx a b] (return (+ a b))) nil)
    (main/list-handlers n))
  => ["echo" "sum"]

  (!.lua
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (main/register-handler n "sum" (fn [ctx a b] (return (+ a b))) nil)
    (main/list-handlers n))
  => ["echo" "sum"]

  (!.py
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (main/register-handler n "sum" (fn [ctx a b] (return (+ a b))) nil)
    (main/list-handlers n))
  => ["echo" "sum"])

^{:refer xt.event.node-main/register-trigger :added "4.1"}
(fact "registers triggers on the node"

  (!.js
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) {"role" "test"})
    [(. (main/get-trigger n "event/ping") ["id"])
     (. (main/get-trigger n "event/ping") ["meta"] ["role"])])
  => ["event/ping" "test"]

  (!.lua
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) {"role" "test"})
    [(. (main/get-trigger n "event/ping") ["id"])
     (. (main/get-trigger n "event/ping") ["meta"] ["role"])])
  => ["event/ping" "test"]

  (!.py
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) {"role" "test"})
    [(. (main/get-trigger n "event/ping") ["id"])
     (. (main/get-trigger n "event/ping") ["meta"] ["role"])])
  => ["event/ping" "test"])

^{:refer xt.event.node-main/unregister-trigger :added "4.1"}
(fact "unregisters triggers from the node"

  (!.js
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (main/unregister-trigger n "event/ping")
    (main/get-trigger n "event/ping"))
  => nil

  (!.lua
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (main/unregister-trigger n "event/ping")
    (main/get-trigger n "event/ping"))
  => nil

  (!.py
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (main/unregister-trigger n "event/ping")
    (main/get-trigger n "event/ping"))
  => nil)

^{:refer xt.event.node-main/get-trigger :added "4.1"}
(fact "gets trigger entries"

  (!.js
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (. (main/get-trigger n "event/ping") ["id"]))
  => "event/ping"

  (!.lua
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (. (main/get-trigger n "event/ping") ["id"]))
  => "event/ping"

  (!.py
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (. (main/get-trigger n "event/ping") ["id"]))
  => "event/ping")

^{:refer xt.event.node-main/list-triggers :added "4.1"}
(fact "lists registered triggers"

  (!.js
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (main/register-trigger n "event/pong" (fn [ctx data] (return data)) nil)
    (main/list-triggers n))
  => ["event/ping" "event/pong"]

  (!.lua
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (main/register-trigger n "event/pong" (fn [ctx data] (return data)) nil)
    (main/list-triggers n))
  => ["event/ping" "event/pong"]

  (!.py
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (main/register-trigger n "event/pong" (fn [ctx data] (return data)) nil)
    (main/list-triggers n))
  => ["event/ping" "event/pong"])

^{:refer xt.event.node-main/get-transport :added "4.1"}
(fact "gets transports by id"

  (!.js
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (. (main/get-transport n "peer-a") ["id"]))
  => "peer-a"

  (!.lua
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (. (main/get-transport n "peer-a") ["id"]))
  => "peer-a"

  (!.py
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (. (main/get-transport n "peer-a") ["id"]))
  => "peer-a")

^{:refer xt.event.node-main/list-transports :added "4.1"}
(fact "lists active transport ids"

  (!.js
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (xt/x:set-key (. n ["transports"]) "peer-b" (main/transport-create "peer-b" {}))
    (main/list-transports n))
  => ["peer-a" "peer-b"]

  (!.lua
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (xt/x:set-key (. n ["transports"]) "peer-b" (main/transport-create "peer-b" {}))
    (main/list-transports n))
  => ["peer-a" "peer-b"]

  (!.py
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (xt/x:set-key (. n ["transports"]) "peer-b" (main/transport-create "peer-b" {}))
    (main/list-transports n))
  => ["peer-a" "peer-b"])

^{:refer xt.event.node-main/send-transport :added "4.1"}
(fact "sends frames through a transport"

  (!.js
    (xt/x:is-function? main/send-transport))
  => true

  (!.lua
    (xt/x:is-function? main/send-transport))
  => true

  (!.py
    (xt/x:is-function? main/send-transport))
  => true)

^{:refer xt.event.node-main/broadcast-transport-loop :added "4.1"}
(fact "broadcast loop returns a promise"

  (!.js
    (xt/x:is-function? main/broadcast-transport-loop))
  => true

  (!.lua
    (xt/x:is-function? main/broadcast-transport-loop))
  => true

  (!.py
    (xt/x:is-function? main/broadcast-transport-loop))
  => true)

^{:refer xt.event.node-main/broadcast-transport :added "4.1"}
(fact "broadcast sends to all transports"

  (!.js
    (xt/x:is-function? main/broadcast-transport))
  => true

  (!.lua
    (xt/x:is-function? main/broadcast-transport))
  => true

  (!.py
    (xt/x:is-function? main/broadcast-transport))
  => true)

^{:refer xt.event.node-main/route-stream-loop :added "4.1"}
(fact "route-stream-loop returns a promise"

  (!.js
    (xt/x:is-function? main/route-stream-loop))
  => true

  (!.lua
    (xt/x:is-function? main/route-stream-loop))
  => true

  (!.py
    (xt/x:is-function? main/route-stream-loop))
  => true)

^{:refer xt.event.node-main/route-stream :added "4.1"}
(fact "route-stream fans out by router subscription"

  (!.js
    (xt/x:is-function? main/route-stream))
  => true

  (!.lua
    (xt/x:is-function? main/route-stream))
  => true

  (!.py
    (xt/x:is-function? main/route-stream))
  => true)

^{:refer xt.event.node-main/attach-transport :added "4.1"}
(fact "attaches transports and registers router connections"

  (!.js
    (xt/x:is-function? main/attach-transport))
  => true

  (!.lua
    (xt/x:is-function? main/attach-transport))
  => true

  (!.py
    (xt/x:is-function? main/attach-transport))
  => true)

^{:refer xt.event.node-main/detach-transport :added "4.1"}
(fact "detaches transports and unregisters router connections"

  (!.js
    (xt/x:is-function? main/detach-transport))
  => true

  (!.lua
    (xt/x:is-function? main/detach-transport))
  => true

  (!.py
    (xt/x:is-function? main/detach-transport))
  => true)

^{:refer xt.event.node-main/request-target :added "4.1"}
(fact "picks a target transport from meta or the first attached transport"

  (!.js
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(main/request-target n {"transport-id" "peer-b"})
     (main/request-target n {})
     (xt/x:nil? (main/request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true]

  (!.lua
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(main/request-target n {"transport-id" "peer-b"})
     (main/request-target n {})
     (xt/x:nil? (main/request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true]

  (!.py
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(main/request-target n {"transport-id" "peer-b"})
     (main/request-target n {})
     (xt/x:nil? (main/request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true])

^{:refer xt.event.node-main/respond-ok :added "4.1"}
(fact "respond-ok forwards response frames to a transport"

  (!.js
    (xt/x:is-function? main/respond-ok))
  => true

  (!.lua
    (xt/x:is-function? main/respond-ok))
  => true

  (!.py
    (xt/x:is-function? main/respond-ok))
  => true)

^{:refer xt.event.node-main/respond-error :added "4.1"}
(fact "respond-error forwards error responses"

  (!.js
    (xt/x:is-function? main/respond-error))
  => true

  (!.lua
    (xt/x:is-function? main/respond-error))
  => true

  (!.py
    (xt/x:is-function? main/respond-error))
  => true)

^{:refer xt.event.node-main/receive-request :added "4.1"}
(fact "receive-request invokes a registered handler"

  (!.js
    (xt/x:is-function? main/receive-request))
  => true

  (!.lua
    (xt/x:is-function? main/receive-request))
  => true

  (!.py
    (xt/x:is-function? main/receive-request))
  => true)

^{:refer xt.event.node-main/receive-response :added "4.1"}
(fact "receive-response settles pending requests"

  (!.js
    (xt/x:is-function? main/receive-response))
  => true

  (!.lua
    (xt/x:is-function? main/receive-response))
  => true

  (!.py
    (xt/x:is-function? main/receive-response))
  => true)

^{:refer xt.event.node-main/request :added "4.1"}
(fact "request runs through the local handler path"

  (!.js
    (xt/x:is-function? main/request))
  => true

  (!.lua
    (xt/x:is-function? main/request))
  => true

  (!.py
    (xt/x:is-function? main/request))
  => true)

^{:refer xt.event.node-main/subscribe :added "4.1"}
(fact "subscribe sends control frames through the target transport"

  (!.js
    (xt/x:is-function? main/subscribe))
  => true

  (!.lua
    (xt/x:is-function? main/subscribe))
  => true

  (!.py
    (xt/x:is-function? main/subscribe))
  => true)

^{:refer xt.event.node-main/unsubscribe :added "4.1"}
(fact "unsubscribe sends control frames through the target transport"

  (!.js
    (xt/x:is-function? main/unsubscribe))
  => true

  (!.lua
    (xt/x:is-function? main/unsubscribe))
  => true

  (!.py
    (xt/x:is-function? main/unsubscribe))
  => true)

^{:refer xt.event.node-main/publish :added "4.1"}
(fact "publish routes streams by subscription"

  (!.js
    (xt/x:is-function? main/publish))
  => true

  (!.lua
    (xt/x:is-function? main/publish))
  => true

  (!.py
    (xt/x:is-function? main/publish))
  => true)

^{:refer xt.event.node-main/receive-publish :added "4.1"}
(fact "receive-publish invokes matching triggers"

  (!.js
    (xt/x:is-function? main/receive-publish))
  => true

  (!.lua
    (xt/x:is-function? main/receive-publish))
  => true

  (!.py
    (xt/x:is-function? main/receive-publish))
  => true)

^{:refer xt.event.node-main/receive-frame :added "4.1"}
(fact "receive-frame dispatches by frame kind"

  (!.js
    (xt/x:is-function? main/receive-frame))
  => true

  (!.lua
    (xt/x:is-function? main/receive-frame))
  => true

  (!.py
    (xt/x:is-function? main/receive-frame))
  => true)

(comment
  (s/snapto '[xt.event.node-main])
  (s/seedgen-langremove '[xt.event.node-main] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.event.node-main] {:lang [:lua :python] :write true}))
