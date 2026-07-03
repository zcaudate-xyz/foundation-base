(ns xt.substrate.base-util-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
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
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as main]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-request :as req]
             [xt.substrate.base-util :as util]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

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

^{:refer xt.substrate.base-util/register-handler :added "4.1"}
(fact "registers a shared request handler"

  (!.js
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) {"role" "test"})
    [(. (util/get-handler n "echo") ["id"])
     (. (util/get-handler n "echo") ["meta"] ["role"])])
  => ["echo" "test"]

  (!.lua
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) {"role" "test"})
    [(. (util/get-handler n "echo") ["id"])
     (. (util/get-handler n "echo") ["meta"] ["role"])])
  => ["echo" "test"]

  (!.py
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) {"role" "test"})
    [(. (util/get-handler n "echo") ["id"])
     (. (util/get-handler n "echo") ["meta"] ["role"])])
  => ["echo" "test"])

^{:refer xt.substrate.base-util/unregister-handler :added "4.1"}
(fact "unregisters a shared request handler"

  (!.js
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (util/unregister-handler n "echo")
    (util/get-handler n "echo"))
  => nil

  (!.lua
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (util/unregister-handler n "echo")
    (util/get-handler n "echo"))
  => nil

  (!.py
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (util/unregister-handler n "echo")
    (util/get-handler n "echo"))
  => nil)

^{:refer xt.substrate.base-util/get-handler :added "4.1"}
(fact "gets shared request handler entries"

  (!.js
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (. (util/get-handler n "echo") ["id"]))
  => "echo"

  (!.lua
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (. (util/get-handler n "echo") ["id"]))
  => "echo"

  (!.py
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (. (util/get-handler n "echo") ["id"]))
  => "echo")

^{:refer xt.substrate.base-util/list-handlers :added "4.1"}
(fact "lists shared request handlers"

  (!.js
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (util/register-handler n "sum" (fn [ctx a b] (return (+ a b))) nil)
    (util/list-handlers n))
  => ["echo" "sum"]

  (!.lua
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (util/register-handler n "sum" (fn [ctx a b] (return (+ a b))) nil)
    (util/list-handlers n))
  => ["echo" "sum"]

  (!.py
    (var n (main/node-create {}))
    (util/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (util/register-handler n "sum" (fn [ctx a b] (return (+ a b))) nil)
    (util/list-handlers n))
  => ["echo" "sum"])

^{:refer xt.substrate.base-util/register-trigger :added "4.1"}
(fact "registers a shared stream trigger"

  (!.js
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) {"role" "test"})
    [(. (util/get-trigger n "event/ping") ["id"])
     (. (util/get-trigger n "event/ping") ["meta"] ["role"])])
  => ["event/ping" "test"]

  (!.lua
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) {"role" "test"})
    [(. (util/get-trigger n "event/ping") ["id"])
     (. (util/get-trigger n "event/ping") ["meta"] ["role"])])
  => ["event/ping" "test"]

  (!.py
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) {"role" "test"})
    [(. (util/get-trigger n "event/ping") ["id"])
     (. (util/get-trigger n "event/ping") ["meta"] ["role"])])
  => ["event/ping" "test"])

^{:refer xt.substrate.base-util/unregister-trigger :added "4.1"}
(fact "unregisters a shared stream trigger"

  (!.js
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (util/unregister-trigger n "event/ping")
    (util/get-trigger n "event/ping"))
  => nil

  (!.lua
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (util/unregister-trigger n "event/ping")
    (util/get-trigger n "event/ping"))
  => nil

  (!.py
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (util/unregister-trigger n "event/ping")
    (util/get-trigger n "event/ping"))
  => nil)

^{:refer xt.substrate.base-util/get-trigger :added "4.1"}
(fact "gets shared stream trigger entries"

  (!.js
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (. (util/get-trigger n "event/ping") ["id"]))
  => "event/ping"

  (!.lua
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (. (util/get-trigger n "event/ping") ["id"]))
  => "event/ping"

  (!.py
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (. (util/get-trigger n "event/ping") ["id"]))
  => "event/ping")

^{:refer xt.substrate.base-util/list-triggers :added "4.1"}
(fact "lists shared stream triggers"

  (!.js
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (util/register-trigger n "event/pong" (fn [ctx data] (return data)) nil)
    (util/list-triggers n))
  => ["event/ping" "event/pong"]

  (!.lua
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (util/register-trigger n "event/pong" (fn [ctx data] (return data)) nil)
    (util/list-triggers n))
  => ["event/ping" "event/pong"]

  (!.py
    (var n (main/node-create {}))
    (util/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (util/register-trigger n "event/pong" (fn [ctx data] (return data)) nil)
    (util/list-triggers n))
  => ["event/ping" "event/pong"])

^{:refer xt.substrate.base-util/request :added "4.1"}
(fact "issues a request locally or over an attached transport"

  (!.js
    (xt/x:is-function? util/request))
  => true

  (!.lua
    (xt/x:is-function? util/request))
  => true

  (!.py
    (xt/x:is-function? util/request))
  => true)

^{:refer xt.substrate.base-util/publish :added "4.1"}
(fact "publishes a stream frame through node core and subscribed transports"

  (!.js
    (xt/x:is-function? util/publish))
  => true

  (!.lua
    (xt/x:is-function? util/publish))
  => true

  (!.py
    (xt/x:is-function? util/publish))
  => true)
