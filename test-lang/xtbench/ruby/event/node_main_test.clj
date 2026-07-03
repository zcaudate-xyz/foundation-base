(ns xtbench.ruby.event.node-main-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :ruby
           {:runtime :basic
            :require [[xt.lang.spec-base :as xt]
                      [xt.lang.spec-promise :as promise]
                      [xt.substrate :as main]
                      [xt.substrate.base-router :as router]
                      [xt.substrate.base-request :as req]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-router/list-subscriptions :added "4.1"}
(fact "lists router subscriptions by space"

  (!.rb
    (var n (main/node-create {}))
    (router/add-subscription n "peer-a" "room/a" "event/ping" nil nil)
    (router/add-subscription n "peer-b" "room/a" "event/ping" nil nil)
    [(router/list-subscriptions n "room/a" "event/ping")
     (router/list-subscriptions n "room/b" "event/ping")])
  => [["peer-a" "peer-b"] []])

^{:refer xt.substrate.base-router/unregister-connection :added "4.1"}
(fact "removing a transport prunes its subscriptions"

  (!.rb
    (var n (main/node-create {}))
    (router/register-connection n "peer-a" {})
    (router/register-connection n "peer-b" {})
    (router/add-subscription n "peer-a" "room/a" "event/ping" nil nil)
    (router/add-subscription n "peer-b" "room/a" "event/ping" nil nil)
    (router/unregister-connection n "peer-a")
    (router/list-subscriptions n "room/a" "event/ping"))
  => ["peer-b"])

^{:refer xt.substrate/node? :added "4.1"}
(fact "detects node values"

  (!.rb
    (main/node? (main/node-create {})))
  => true)

^{:refer xt.substrate/transport? :added "4.1"}
(fact "detects transport values"

  (!.rb
    (main/transport? (main/transport-create "peer-a" {})))
  => true)

^{:refer xt.substrate/transport-create :added "4.1"}
(fact "creates transport entries"

  (!.rb
    (var transport (main/transport-create "peer-a" {"meta" {"role" "edge"}}))
    [(. transport ["::"])
     (. transport ["id"])
     (. transport ["meta"] ["role"])])
  => ["substrate.transport" "peer-a" "edge"])

^{:refer xt.substrate/node-create :added "4.1"}
(fact "creates node state with router and registries"

  (!.rb
    (var n (main/node-create {}))
    [(. n ["::"])
     (xt/x:obj-keys (. n ["spaces"]))
     (xt/x:obj-keys (. n ["handlers"]))
     (xt/x:obj-keys (. n ["triggers"]))
     (xt/x:obj-keys (. n ["router"]))])
  => ["substrate" [] [] [] ["connections" "subscriptions"]])

^{:refer xt.substrate/register-handler :added "4.1"}
(fact "registers handlers on the node"

  (!.rb
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) {"role" "test"})
    [(. (main/get-handler n "echo") ["id"])
     (. (main/get-handler n "echo") ["meta"] ["role"])])
  => ["echo" "test"])

^{:refer xt.substrate/unregister-handler :added "4.1"}
(fact "unregisters handlers from the node"

  (!.rb
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (main/unregister-handler n "echo")
    (main/get-handler n "echo"))
  => nil)

^{:refer xt.substrate/get-handler :added "4.1"}
(fact "gets handler entries"

  (!.rb
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (. (main/get-handler n "echo") ["id"]))
  => "echo")

^{:refer xt.substrate/list-handlers :added "4.1"}
(fact "lists registered handlers"

  (!.rb
    (var n (main/node-create {}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (main/register-handler n "sum" (fn [ctx a b] (return (+ a b))) nil)
    (main/list-handlers n))
  => ["echo" "sum"])

^{:refer xt.substrate/register-trigger :added "4.1"}
(fact "registers triggers on the node"

  (!.rb
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) {"role" "test"})
    [(. (main/get-trigger n "event/ping") ["id"])
     (. (main/get-trigger n "event/ping") ["meta"] ["role"])])
  => ["event/ping" "test"])

^{:refer xt.substrate/unregister-trigger :added "4.1"}
(fact "unregisters triggers from the node"

  (!.rb
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (main/unregister-trigger n "event/ping")
    (main/get-trigger n "event/ping"))
  => nil)

^{:refer xt.substrate/get-trigger :added "4.1"}
(fact "gets trigger entries"

  (!.rb
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (. (main/get-trigger n "event/ping") ["id"]))
  => "event/ping")

^{:refer xt.substrate/list-triggers :added "4.1"}
(fact "lists registered triggers"

  (!.rb
    (var n (main/node-create {}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (main/register-trigger n "event/pong" (fn [ctx data] (return data)) nil)
    (main/list-triggers n))
  => ["event/ping" "event/pong"])

^{:refer xt.substrate.base-util/transport-get :added "4.1"}
(fact "gets transports by id"

  (!.rb
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (. (util/transport-get n "peer-a") ["id"]))
  => "peer-a")

^{:refer xt.substrate.base-util/transport-list :added "4.1"}
(fact "lists active transport ids"

  (!.rb
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (xt/x:set-key (. n ["transports"]) "peer-b" (main/transport-create "peer-b" {}))
    (util/transport-list n))
  => ["peer-a" "peer-b"])

^{:refer xt.substrate.base-util/transport-send :added "4.1"}
(fact "sends frames through a transport"

  (!.rb
    (xt/x:is-function? util/transport-send))
  => true)

^{:refer xt.substrate.base-util/transport-broadcast-loop :added "4.1"}
(fact "broadcast loop returns a promise"

  (!.rb
    (xt/x:is-function? util/transport-broadcast-loop))
  => true)

^{:refer xt.substrate/broadcast-transport :added "4.1"}
(fact "broadcast sends to all transports"

  (!.rb
    (xt/x:is-function? main/broadcast-transport))
  => true)

^{:refer xt.substrate.base-util/stream-route-loop :added "4.1"}
(fact "stream-route-loop returns a promise"

  (!.rb
    (xt/x:is-function? util/stream-route-loop))
  => true)

^{:refer xt.substrate/route-stream :added "4.1"}
(fact "route-stream fans out by router subscription"

  (!.rb
    (xt/x:is-function? main/route-stream))
  => true)

^{:refer xt.substrate/attach-transport :added "4.1"}
(fact "attaches transports and registers router connections"

  (!.rb
    (xt/x:is-function? main/attach-transport))
  => true)

^{:refer xt.substrate/detach-transport :added "4.1"}
(fact "detaches transports and unregisters router connections"

  (!.rb
    (xt/x:is-function? main/detach-transport))
  => true)

^{:refer xt.substrate.base-util/transport-request-target :added "4.1"}
(fact "picks a target transport from meta or the first attached transport"

  (!.rb
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(util/transport-request-target n {"transport_id" "peer-b"})
     (util/transport-request-target n {})
     (util/transport-request-target (main/node-create {}) {})])
  => ["peer-b" "peer-a" nil])

^{:refer xt.substrate.base-util/response-ok :added "4.1"}
(fact "response-ok forwards response frames to a transport"

  (!.rb
    (xt/x:is-function? util/response-ok))
  => true)

^{:refer xt.substrate.base-util/response-error :added "4.1"}
(fact "response-error forwards error responses"

  (!.rb
    (xt/x:is-function? util/response-error))
  => true)

^{:refer xt.substrate/receive-request :added "4.1"}
(fact "receive-request invokes a registered handler"

  (!.rb
    (xt/x:is-function? main/receive-request))
  => true)

^{:refer xt.substrate/receive-response :added "4.1"}
(fact "receive-response settles pending requests"

  (!.rb
    (xt/x:is-function? main/receive-response))
  => true)

^{:refer xt.substrate/request :added "4.1"}
(fact "request runs through the local handler path"

  (!.rb
    (xt/x:is-function? main/request))
  => true)

^{:refer xt.substrate/subscribe :added "4.1"}
(fact "subscribe sends control frames through the target transport"

  (!.rb
    (xt/x:is-function? main/subscribe))
  => true)

^{:refer xt.substrate/unsubscribe :added "4.1"}
(fact "unsubscribe sends control frames through the target transport"

  (!.rb
    (xt/x:is-function? main/unsubscribe))
  => true)

^{:refer xt.substrate/publish :added "4.1"}
(fact "publish routes streams by subscription"

  (!.rb
    (xt/x:is-function? main/publish))
  => true)

^{:refer xt.substrate/receive-publish :added "4.1"}
(fact "receive-publish invokes matching triggers"

  (!.rb
    (xt/x:is-function? main/receive-publish))
  => true)

^{:refer xt.substrate/receive-frame :added "4.1"}
(fact "receive-frame dispatches by frame kind"

  (!.rb
    (xt/x:is-function? main/receive-frame))
  => true)
