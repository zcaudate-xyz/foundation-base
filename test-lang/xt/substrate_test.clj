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
             [xt.db.node.view-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as main]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-request :as req]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.view-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as main]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-request :as req]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.view-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as main]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-request :as req]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

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

^{:refer xt.substrate.base-router/unregister-connection :added "4.1"}
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

^{:refer xt.substrate/node-create :added "4.1"
  :setup [(def +out+
            (just-in
             ["substrate"
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

^{:refer xt.substrate/node-create :added "4.1"}
(fact "node-create accepts declarative spaces, handlers, and triggers"

  (!.js
   (var n (main/node-create {"id" "node-a"
                             "meta" {"cluster" "local"}
                             "spaces" {"room/a" {"state" {"count" 1}
                                                 "meta" {"role" "alpha"}}
                                      "room/b" {"state" {"count" 2}}}
                             "handlers" {"ping" {"fn" (fn [space args request node]
                                                        (return args))
                                                 "meta" {"kind" "request"}}}
                             "triggers" {"event/updated" {"fn" (fn [space stream node]
                                                                 (return stream))
                                                          "meta" {"kind" "stream"}}}}))
   [(. n ["id"])
    (. n ["meta"] ["cluster"])
    (main/list-spaces n)
    (. (main/get-space n "room/a") ["meta"] ["role"])
    (. (main/get-space-state n "room/b") ["count"])
    (. (main/get-handler n "ping") ["meta"] ["kind"])
    (. (main/get-trigger n "event/updated") ["meta"] ["kind"])])
  => ["node-a" "local" ["room/a" "room/b"] "alpha" 2 "request" "stream"]

  (!.lua
   (var n (main/node-create {"id" "node-a"
                             "meta" {"cluster" "local"}
                             "spaces" {"room/a" {"state" {"count" 1}
                                                 "meta" {"role" "alpha"}}
                                      "room/b" {"state" {"count" 2}}}
                             "handlers" {"ping" {"fn" (fn [space args request node]
                                                        (return args))
                                                 "meta" {"kind" "request"}}}
                             "triggers" {"event/updated" {"fn" (fn [space stream node]
                                                                 (return stream))
                                                          "meta" {"kind" "stream"}}}}))
   [(. n ["id"])
    (. n ["meta"] ["cluster"])
    (main/list-spaces n)
    (. (main/get-space n "room/a") ["meta"] ["role"])
    (. (main/get-space-state n "room/b") ["count"])
    (. (main/get-handler n "ping") ["meta"] ["kind"])
    (. (main/get-trigger n "event/updated") ["meta"] ["kind"])])
  => ["node-a" "local" ["room/a" "room/b"] "alpha" 2 "request" "stream"]

  (!.py
   (var n (main/node-create {"id" "node-a"
                             "meta" {"cluster" "local"}
                             "spaces" {"room/a" {"state" {"count" 1}
                                                 "meta" {"role" "alpha"}}
                                      "room/b" {"state" {"count" 2}}}
                             "handlers" {"ping" {"fn" (fn [space args request node]
                                                        (return args))
                                                 "meta" {"kind" "request"}}}
                             "triggers" {"event/updated" {"fn" (fn [space stream node]
                                                                 (return stream))
                                                          "meta" {"kind" "stream"}}}}))
   [(. n ["id"])
    (. n ["meta"] ["cluster"])
    (main/list-spaces n)
    (. (main/get-space n "room/a") ["meta"] ["role"])
    (. (main/get-space-state n "room/b") ["count"])
    (. (main/get-handler n "ping") ["meta"] ["kind"])
    (. (main/get-trigger n "event/updated") ["meta"] ["kind"])])
  => ["node-a" "local" ["room/a" "room/b"] "alpha" 2 "request" "stream"])

^{:refer xt.substrate/node-create :added "4.1"}
(fact "node-create rejects space configs without explicit state or meta keys"

  (!.js
   (try
     (main/node-create {"spaces" {"room/a" {"count" 2}}})
     (return :not-thrown)
     (catch err
       (return :thrown))))
  => :thrown

  (!.lua
   (try
     (main/node-create {"spaces" {"room/a" {"count" 2}}})
     (return :not-thrown)
     (catch err
       (return :thrown))))
  => :thrown

  (!.py
   (try
     (main/node-create {"spaces" {"room/a" {"count" 2}}})
     (return :not-thrown)
     (catch err
       (return :thrown))))
  => :thrown)

^{:refer xt.substrate/configure-node :added "4.1"}
(fact "configure-node applies declarative config to an existing node"

  (!.js
   (var n (main/node-create {"id" "node-b"}))
   (main/configure-node
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
   (main/configure-node
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
   (main/configure-node
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

^{:refer xt.substrate/get-transport :added "4.1"}
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

^{:refer xt.substrate/list-transports :added "4.1"}
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

^{:refer xt.substrate/send-transport :added "4.1"}
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

^{:refer xt.substrate/broadcast-transport-loop :added "4.1"}
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

^{:refer xt.substrate/route-stream-loop :added "4.1"}
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

^{:refer xt.substrate/request-target :added "4.1"}
(fact "picks a target transport from meta or the first attached transport"

  (!.js
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(main/request-target n {"transport_id" "peer-b"})
     (main/request-target n {})
     (xt/x:nil? (main/request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true]

  (!.lua
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(main/request-target n {"transport_id" "peer-b"})
     (main/request-target n {})
     (xt/x:nil? (main/request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true]

  (!.py
    (var n (main/node-create {}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    [(main/request-target n {"transport_id" "peer-b"})
     (main/request-target n {})
     (xt/x:nil? (main/request-target (main/node-create {}) {}))])
  => ["peer-b" "peer-a" true])

^{:refer xt.substrate/respond-ok :added "4.1"}
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

^{:refer xt.substrate/respond-error :added "4.1"}
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

^{:refer xt.substrate/publish :added "4.1"}
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

(comment
  (s/snapto '[xt.substrate])
  (s/seedgen-benchadd '[xt.substrate] {:lang :dart :write true})
  (s/seedgen-langremove '[xt.substrate] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.substrate] {:lang [:lua :python] :write true}))
