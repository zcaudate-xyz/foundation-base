(ns xt.substrate.walkthrough.s04-fanout-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.substrate.walkthrough.s04-fanout-test/demo-001-memory-network}
(fact "a shared memory network supports stream fanout across multiple transports"

  (notify/wait-on :js
    (var network (transport-memory/memory-network
                  {"client-a" ["server-a"]
                   "server-a" ["client-a"]
                   "client-b" ["server-b"]
                   "server-b" ["client-b"]}))
    (var seen [])
    (var client-a (event-node/node-create
                   {"id" "client-a"
                    "triggers"
                    {"event/pinged" {"fn" (fn [space stream node]
                                             (xt/x:arr-push seen
                                                            {"client" "client-a"
                                                             "data" (. stream ["data"])})
                                             (return true))
                                     "meta" {"kind" "stream"}}}}))
    (var client-b (event-node/node-create
                   {"id" "client-b"
                    "triggers"
                    {"event/pinged" {"fn" (fn [space stream node]
                                             (xt/x:arr-push seen
                                                            {"client" "client-b"
                                                             "data" (. stream ["data"])})
                                             (return true))
                                     "meta" {"kind" "stream"}}}}))
    (var server (event-node/node-create {"id" "server"}))
    (-> (promise/x:promise-all
         [(event-node/attach-transport
           client-a
           "server"
           (transport-memory/text-endpoint (. network ["client-a"])))
          (event-node/attach-transport
           client-b
           "server"
           (transport-memory/text-endpoint (. network ["client-b"])))
          (event-node/attach-transport
           server
           "client-a"
           (transport-memory/text-endpoint (. network ["server-a"])))
          (event-node/attach-transport
           server
           "client-b"
           (transport-memory/text-endpoint (. network ["server-b"])))])
        (promise/x:promise-then
         (fn [_]
           (return
            (promise/x:promise-all
             [(event-node/subscribe client-a "room/a" "event/pinged" "sub-a" {"transport_id" "server"})
              (event-node/subscribe client-b "room/a" "event/pinged" "sub-b" {"transport_id" "server"})]))))
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/publish server "room/a" "event/pinged" {"count" 2} nil))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify seen)))))
  => [{"client" "client-a" "data" {"count" 2}}
      {"client" "client-b" "data" {"count" 2}}])
