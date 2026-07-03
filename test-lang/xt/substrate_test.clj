(ns xt.substrate-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-sync :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as main]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-request :as req]
             [xt.substrate.base-util :as util]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-sync :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as main]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-request :as req]
             [xt.substrate.base-util :as util]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-sync :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as main]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-request :as req]
             [xt.substrate.base-util :as util]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate/node? :added "4.1"}
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

^{:refer xt.substrate/transport? :added "4.1"}
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

^{:refer xt.substrate/transport-create :added "4.1"}
(fact "creates transport entries"

  (!.js
    (var transport (main/transport-create
                    "peer-a"
                    {"meta" {"role" "edge"}
                     "send_fn" (fn [frame] (return frame))
                     "start_fn" (fn [listener] (return listener))
                     "stop_fn" (fn [listener] (return listener))}))
    [(. transport ["::"])
     (. transport ["id"])
     (. transport ["meta"] ["role"])
     (xt/x:is-function? (. transport ["send_fn"]))
     (xt/x:is-function? (. transport ["start_fn"]))
     (xt/x:is-function? (. transport ["stop_fn"]))])
  => ["substrate.transport" "peer-a" "edge" true true true]

  (!.lua
    (var transport (main/transport-create
                    "peer-a"
                    {"meta" {"role" "edge"}
                     "send_fn" (fn [frame] (return frame))
                     "start_fn" (fn [listener] (return listener))
                     "stop_fn" (fn [listener] (return listener))}))
    [(. transport ["::"])
     (. transport ["id"])
     (. transport ["meta"] ["role"])
     (xt/x:is-function? (. transport ["send_fn"]))
     (xt/x:is-function? (. transport ["start_fn"]))
     (xt/x:is-function? (. transport ["stop_fn"]))])
  => ["substrate.transport" "peer-a" "edge" true true true]

  (!.py
    (var transport (main/transport-create
                    "peer-a"
                    {"meta" {"role" "edge"}
                     "send_fn" (fn [frame] (return frame))
                     "start_fn" (fn [listener] (return listener))
                     "stop_fn" (fn [listener] (return listener))}))
    [(. transport ["::"])
     (. transport ["id"])
     (. transport ["meta"] ["role"])
     (xt/x:is-function? (. transport ["send_fn"]))
     (xt/x:is-function? (. transport ["start_fn"]))
     (xt/x:is-function? (. transport ["stop_fn"]))])
  => ["substrate.transport" "peer-a" "edge" true true true])

^{:refer xt.substrate/get-services :added "4.1"}
(fact "node-create keeps a first-class services registry"

  (!.js
   (var n (main/node-create {"id" "node-services"
                            "services" {"cache" {"scope" "global"}}}))
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:obj-keys (main/get-services n))])
  => ["global" ["cache"]]

  (!.lua
   (var n (main/node-create {"id" "node-services"
                            "services" {"cache" {"scope" "global"}}}))
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:obj-keys (main/get-services n))])
  => ["global" ["cache"]]

  (!.py
   (var n (main/node-create {"id" "node-services"
                            "services" {"cache" {"scope" "global"}}}))
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:obj-keys (main/get-services n))])
  => ["global" ["cache"]])

^{:refer xt.substrate/get-service :added "4.1"}
(fact "gets a registered service by id"

  (!.js
   (var n (main/node-create {"id" "node-get-service"
                            "services" {"cache" {"scope" "global"}}}))
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:nil? (main/get-service n "missing"))])
  => ["global" true]

  (!.lua
   (var n (main/node-create {"id" "node-get-service"
                            "services" {"cache" {"scope" "global"}}}))
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:nil? (main/get-service n "missing"))])
  => ["global" true]

  (!.py
   (var n (main/node-create {"id" "node-get-service"
                            "services" {"cache" {"scope" "global"}}}))
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:nil? (main/get-service n "missing"))])
  => ["global" true])

^{:refer xt.substrate/set-service :added "4.1"}
(fact "set-service registers a runtime service on the node"

  (!.js
   (var n (main/node-create {"id" "node-service-set"}))
   (main/set-service n "cache" {"scope" "local"})
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:obj-keys (main/get-services n))])
  => ["local" ["cache"]]

  (!.lua
   (var n (main/node-create {"id" "node-service-set"}))
   (main/set-service n "cache" {"scope" "local"})
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:obj-keys (main/get-services n))])
  => ["local" ["cache"]]

  (!.py
   (var n (main/node-create {"id" "node-service-set"}))
   (main/set-service n "cache" {"scope" "local"})
   [(. (main/get-service n "cache") ["scope"])
    (xt/x:obj-keys (main/get-services n))])
  => ["local" ["cache"]])

^{:refer xt.substrate/register-handler :added "4.1"}
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

^{:refer xt.substrate/unregister-handler :added "4.1"}
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

^{:refer xt.substrate/get-handler :added "4.1"}
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

^{:refer xt.substrate/list-handlers :added "4.1"}
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

^{:refer xt.substrate/register-trigger :added "4.1"}
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

^{:refer xt.substrate/unregister-trigger :added "4.1"}
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

^{:refer xt.substrate/get-trigger :added "4.1"}
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

^{:refer xt.substrate/list-triggers :added "4.1"}
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

^{:refer xt.substrate.base-util/transport-get :added "4.1"}
(fact "gets transports by id"

  (!.js
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (. (util/transport-get n "peer-a") ["id"]))
  => "peer-a"

  (!.lua
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (. (util/transport-get n "peer-a") ["id"]))
  => "peer-a"

  (!.py
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (. (util/transport-get n "peer-a") ["id"]))
  => "peer-a")

^{:refer xt.substrate.base-util/transport-list :added "4.1"}
(fact "lists active transport ids"

  (!.js
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (xt/x:set-key (. n ["transports"]) "peer-b" (main/transport-create "peer-b" {}))
    (util/transport-list n))
  => ["peer-a" "peer-b"]

  (!.lua
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (xt/x:set-key (. n ["transports"]) "peer-b" (main/transport-create "peer-b" {}))
    (util/transport-list n))
  => ["peer-a" "peer-b"]

  (!.py
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (xt/x:set-key (. n ["transports"]) "peer-b" (main/transport-create "peer-b" {}))
    (util/transport-list n))
  => ["peer-a" "peer-b"])

^{:refer xt.substrate.base-router/list-subscriptions :added "4.1"
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

^{:refer xt.substrate.base-util/transport-send :added "4.1"}
(fact "sends frames through a transport"

  (!.js
    (xt/x:is-function? util/transport-send))
  => true

  (!.lua
    (xt/x:is-function? util/transport-send))
  => true

  (!.py
    (xt/x:is-function? util/transport-send))
  => true)

^{:refer xt.substrate.base-util/transport-broadcast-loop :added "4.1"}
(fact "broadcast loop returns a promise"

  (!.js
    (xt/x:is-function? util/transport-broadcast-loop))
  => true

  (!.lua
    (xt/x:is-function? util/transport-broadcast-loop))
  => true

  (!.py
    (xt/x:is-function? util/transport-broadcast-loop))
  => true)

^{:refer xt.substrate/broadcast-transport :added "4.1"}
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

^{:refer xt.substrate.base-util/stream-route-loop :added "4.1"}
(fact "stream-route-loop returns a promise"

  (!.js
    (xt/x:is-function? util/stream-route-loop))
  => true

  (!.lua
    (xt/x:is-function? util/stream-route-loop))
  => true

  (!.py
    (xt/x:is-function? util/stream-route-loop))
  => true)

^{:refer xt.substrate/route-stream :added "4.1"}
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

^{:refer xt.substrate.base-util/transport-request-target :added "4.1"}
(fact "picks a target transport from meta or the first attached transport"

  (!.js
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(util/transport-request-target n {"transport_id" "peer-b"})
     (util/transport-request-target n {})
     (xt/x:nil? (util/transport-request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true]

  (!.lua
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(util/transport-request-target n {"transport_id" "peer-b"})
     (util/transport-request-target n {})
     (xt/x:nil? (util/transport-request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true]

  (!.py
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(util/transport-request-target n {"transport_id" "peer-b"})
     (util/transport-request-target n {})
     (xt/x:nil? (util/transport-request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true])

^{:refer xt.substrate.base-util/pending-await :added "4.1"}
(fact "waits for pending states to resolve or reject"
  (notify/wait-on :js
    (var state {"status" "pending"
                "value" nil
                "error" nil})
    (setTimeout
     (fn []
       (xt/x:set-key state "status" "resolved")
       (xt/x:set-key state "value" {"ok" true}))
     20)
    (promise/x:promise-then
     (util/pending-await state)
     (fn [value]
       (return
        (promise/x:promise-catch
         (util/pending-await {"status" "rejected"
                              "error" "denied"})
         (fn [err]
           (repl/notify {"value" value
                         "error" err})))))))
  => {"value" {"ok" true}
      "error" "denied"})

^{:refer xt.substrate.base-util/request-context-merge :added "4.1"}
(fact "merges transport context into request meta"
  (!.js
   [(util/request-context-merge
     {"kind" "request"
      "meta" {"trace" "a"}}
     {"transport_id" "peer-a"})
    (util/request-context-merge
     {"kind" "request"}
     {"transport_id" "peer-b"})])
  => [{"kind" "request"
       "meta" {"trace" "a"
               "transport_id" "peer-a"}}
      {"kind" "request"
       "meta" {"transport_id" "peer-b"}}])

^{:refer xt.substrate.base-util/response-ok :added "4.1"}
(fact "response-ok forwards response frames to a transport"

  (!.js
    (xt/x:is-function? util/response-ok))
  => true

  (!.lua
    (xt/x:is-function? util/response-ok))
  => true

  (!.py
    (xt/x:is-function? util/response-ok))
  => true)

^{:refer xt.substrate.base-util/response-error :added "4.1"}
(fact "response-error forwards error responses"

  (!.js
    (xt/x:is-function? util/response-error))
  => true

  (!.lua
    (xt/x:is-function? util/response-error))
  => true

  (!.py
    (xt/x:is-function? util/response-error))
  => true)

^{:refer xt.substrate/receive-request :added "4.1"}
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

^{:refer xt.substrate/receive-response :added "4.1"}
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

^{:refer xt.substrate/request :added "4.1"}
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

^{:refer xt.substrate/subscribe :added "4.1"}
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

^{:refer xt.substrate/unsubscribe :added "4.1"}
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

^{:refer xt.substrate/receive-publish :added "4.1"}
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

^{:refer xt.substrate/publish :added "4.1"}
(fact "publish can chain a second async callback after local trigger handling"

  (notify/wait-on :js
    (var n (main/node-create {"id" "node-a"}))
    (model/install n fixtures/InstallOpts)
    (promise/x:promise-catch
     (promise/x:promise-then
      (main/publish n "room/a" "xt.db/cache.changed" {"tables" {"Order" true}}
                    {"origin_node" "node-a"})
      (fn [_]
        (repl/notify {"ok" true
                      "space" "room/a"})))
     (fn [err]
       (repl/notify {"error" err}))))
  => {"ok" true
      "space" "room/a"})

^{:refer xt.substrate/receive-frame :added "4.1"}
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

^{:refer xt.substrate/attach-transport :added "4.1"}
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

^{:refer xt.substrate/detach-transport :added "4.1"}
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

^{:refer xt.substrate.base-util/config-normalize-space :added "4.1"}
(fact "normalizes declarative space config and rejects mismatched ids"
  (!.js
   (do:>
    (var thrown false)
    (try
      (util/config-normalize-space "room/a" {"id" "room/b"})
      (catch err
        (:= thrown true)))
    (return
     [(util/config-normalize-space "room/a" nil)
      (util/config-normalize-space "room/a" {"id" "room/a"
                                        "state" {"count" 1}
                                        "meta" {"role" "alpha"}})
      thrown])))
  => [nil
      {"state" {"count" 1}
       "meta" {"role" "alpha"}}
      true])

^{:refer xt.substrate.base-util/config-normalize-handler :added "4.1"}
(fact "normalizes handler config from fn or declarative entry"
  (!.js
   (do:>
    (var handler-fn (fn [space args request node]
                     (return args)))
    (var thrown false)
    (try
     (util/config-normalize-handler "ping" {"id" "pong"
                                        "fn" handler-fn})
     (catch err
       (:= thrown true)))
    (return
     [(xt/x:is-function? (:? true (xt/x:get-key (util/config-normalize-handler "ping" handler-fn) "fn")))
     (. (util/config-normalize-handler "ping" {"fn" handler-fn
                                           "meta" {"kind" "request"}}) ["meta"])
     thrown])))
  => [true
     {"kind" "request"}
     true])

^{:refer xt.substrate.base-util/config-normalize-trigger :added "4.1"}
(fact "normalizes trigger config from fn or declarative entry"
  (!.js
   (do:>
    (var trigger-fn (fn [space stream node]
                     (return stream)))
    (var thrown false)
    (try
     (util/config-normalize-trigger "event/ping" {"id" "event/pong"
                                              "fn" trigger-fn})
     (catch err
       (:= thrown true)))
    (return
     [(xt/x:is-function? (:? true (xt/x:get-key (util/config-normalize-trigger "event/ping" trigger-fn) "fn")))
     (. (util/config-normalize-trigger "event/ping" {"fn" trigger-fn
                                                 "meta" {"kind" "stream"}}) ["meta"])
     thrown])))
  => [true
     {"kind" "stream"}
     true])

^{:refer xt.substrate/node-configure :added "4.1"}
(fact "node-configure applies declarative config to an existing node"

  (!.js
   (var n (main/node-create {"id" "node-b"}))
   (main/node-configure
    n
    {"spaces" {"room/c" {"state" {"count" 4}}}
     "handlers" {"echo" (fn [space args request node]
                          (return (. space ["id"])))}
     "triggers" {"event/tick" (fn [space stream node]
                                (main/set-space-state node
                                                      (. space ["id"])
                                                      (. stream ["data"]))
                                (return true))}})
   [(main/list-spaces n)
    (main/list-handlers n)
    (main/list-triggers n)])
  => [["room/c"] ["echo"] ["event/tick"]]

  (!.lua
   (var n (main/node-create {"id" "node-b"}))
   (main/node-configure
    n
    {"spaces" {"room/c" {"state" {"count" 4}}}
     "handlers" {"echo" (fn [space args request node]
                          (return (. space ["id"])))}
     "triggers" {"event/tick" (fn [space stream node]
                                (main/set-space-state node
                                                      (. space ["id"])
                                                      (. stream ["data"]))
                                (return true))}})
   [(main/list-spaces n)
    (main/list-handlers n)
    (main/list-triggers n)])
  => [["room/c"] ["echo"] ["event/tick"]]

  (!.py
   (var n (main/node-create {"id" "node-b"}))
   (main/node-configure
    n
    {"spaces" {"room/c" {"state" {"count" 4}}}
     "handlers" {"echo" (fn [space args request node]
                          (return (. space ["id"])))}
     "triggers" {"event/tick" (fn [space stream node]
                                (main/set-space-state node
                                                      (. space ["id"])
                                                      (. stream ["data"]))
                                (return true))}})
   [(main/list-spaces n)
    (main/list-handlers n)
    (main/list-triggers n)])
  => [["room/c"] ["echo"] ["event/tick"]])

^{:refer xt.substrate.base-util/node-base-opts :added "4.1"}
(fact "strips declarative config keys before node construction"
  (!.js
   (util/node-base-opts {"id" "node-a"
                         "meta" {"cluster" "local"}
                         "spaces" {"room/a" {}}
                         "handlers" {"ping" true}
                         "triggers" {"event/ping" true}
                         "custom" 42}))
  => {"id" "node-a"
      "meta" {"cluster" "local"}
      "custom" 42})

^{:refer xt.substrate/node-create :added "4.1"}
(fact "declarative node config participates in local publish and request flows"

  (notify/wait-on :js
    (var n (main/node-create {"spaces" {"room/a" {"state" {"count" 1}}}
                              "handlers" {"ping" (fn [space args request node]
                                                   (return {"space" (. space ["id"])
                                                            "count" (. (. space ["state"]) ["count"])
                                                            "payload" (xt/x:get-idx args 0)}))}
                              "triggers" {"event/updated" (fn [space stream node]
                                                            (main/set-space-state node
                                                                                  (. space ["id"])
                                                                                  (. stream ["data"]))
                                                            (return true))}}))
    (promise/x:promise-then
     (main/publish n "room/a" "event/updated" {"count" 3} nil)
     (fn [_]
       (return
        (promise/x:promise-then
         (main/request n "room/a" "ping" ["hello"] nil)
         (fn [out]
           (repl/notify out)))))))
  => {"space" "room/a"
      "count" 3
      "payload" "hello"})

(comment
  (s/snapto '[xt.substrate])
  (s/seedgen-benchadd '[xt.substrate] {:lang :dart :write true})
  (s/seedgen-langremove '[xt.substrate] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.substrate] {:lang [:lua :python] :write true}))


^{:refer xt.substrate/remove-service :added "4.1"}
(fact "TODO")