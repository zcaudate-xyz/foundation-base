(ns xt.substrate.e2e-multiplex-test
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

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-memory :as transport-memory]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.substrate.transport-memory/memory-network :added "4.1"}
(fact "a shared memory network supports multiplexer nodes with multiple upstream transports"

  (notify/wait-on :js
    (var network (transport-memory/memory-network
                  {"client" ["mux-client"]
                   "mux-client" ["client"]
                   "server-a" ["mux-a"]
                   "mux-a" ["server-a"]
                   "server-b" ["mux-b"]
                   "mux-b" ["server-b"]}))
    (var client (event-node/node-create {"id" "client"}))
    (var mux (event-node/node-create
              {"id" "mux"
               "handlers"
               {"demo/proxy"
                {"fn" (fn [space args request mux-node]
                        (return
                         (event-node/request
                          mux-node
                          (. space ["id"])
                          "demo/echo"
                          [(xt/x:get-idx args (xt/x:offset 1))]
                          {"transport_id" (xt/x:get-idx args (xt/x:offset 0))})))
                 "meta" {"kind" "request"}}}}))
    (var server-a (event-node/node-create
                   {"id" "server-a"
                    "handlers"
                    {"demo/echo" {"fn" (fn [space args request server-node]
                                         (return {"server" (. server-node ["id"])
                                                  "value" (xt/x:get-idx args (xt/x:offset 0))}))
                                  "meta" {"kind" "request"}}}}))
    (var server-b (event-node/node-create
                   {"id" "server-b"
                    "handlers"
                    {"demo/echo" {"fn" (fn [space args request server-node]
                                         (return {"server" (. server-node ["id"])
                                                  "value" (xt/x:get-idx args (xt/x:offset 0))}))
                                  "meta" {"kind" "request"}}}}))
    (-> (promise/x:promise-all
         [(event-node/attach-transport
           client
           "mux"
           (transport-memory/text-endpoint (. network ["client"])))
          (event-node/attach-transport
           mux
           "client"
           (transport-memory/text-endpoint (. network ["mux-client"])))
          (event-node/attach-transport
           mux
           "server-a"
           (transport-memory/text-endpoint (. network ["mux-a"])))
          (event-node/attach-transport
           mux
           "server-b"
           (transport-memory/text-endpoint (. network ["mux-b"])))
          (event-node/attach-transport
           server-a
           "mux"
           (transport-memory/text-endpoint (. network ["server-a"])))
          (event-node/attach-transport
           server-b
           "mux"
           (transport-memory/text-endpoint (. network ["server-b"])))])
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request client "room/a" "demo/proxy" ["server-a" "ping-a"] nil))))
        (promise/x:promise-then
         (fn [out-a]
           (return
            (promise/x:promise-then
             (event-node/request client "room/a" "demo/proxy" ["server-b" "ping-b"] nil)
             (fn [out-b]
               (return {"a" out-a
                        "b" out-b}))))))       
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"a" {"server" "server-a" "value" "ping-a"}
      "b" {"server" "server-b" "value" "ping-b"}}

  (notify/wait-on [:lua 10000]
    (var network (transport-memory/memory-network
                  {"client" ["mux-client"]
                   "mux-client" ["client"]
                   "server-a" ["mux-a"]
                   "mux-a" ["server-a"]
                   "server-b" ["mux-b"]
                   "mux-b" ["server-b"]}))
    (var client (event-node/node-create {"id" "client"}))
    (var mux (event-node/node-create
              {"id" "mux"
               "handlers"
               {"demo/proxy"
                {"fn" (fn [space args request mux-node]
                        (return
                         (event-node/request
                          mux-node
                          (. space ["id"])
                          "demo/echo"
                          [(xt/x:get-idx args (xt/x:offset 1))]
                          {"transport_id" (xt/x:get-idx args (xt/x:offset 0))})))
                 "meta" {"kind" "request"}}}}))
    (var server-a (event-node/node-create
                   {"id" "server-a"
                    "handlers"
                    {"demo/echo" {"fn" (fn [space args request server-node]
                                         (return {"server" (. server-node ["id"])
                                                  "value" (xt/x:get-idx args (xt/x:offset 0))}))
                                  "meta" {"kind" "request"}}}}))
    (var server-b (event-node/node-create
                   {"id" "server-b"
                    "handlers"
                    {"demo/echo" {"fn" (fn [space args request server-node]
                                         (return {"server" (. server-node ["id"])
                                                  "value" (xt/x:get-idx args (xt/x:offset 0))}))
                                  "meta" {"kind" "request"}}}}))
    (-> (promise/x:promise-all
         [(event-node/attach-transport
           client
           "mux"
           (transport-memory/text-endpoint (. network ["client"])))
          (event-node/attach-transport
           mux
           "client"
           (transport-memory/text-endpoint (. network ["mux-client"])))
          (event-node/attach-transport
           mux
           "server-a"
           (transport-memory/text-endpoint (. network ["mux-a"])))
          (event-node/attach-transport
           mux
           "server-b"
           (transport-memory/text-endpoint (. network ["mux-b"])))
          (event-node/attach-transport
           server-a
           "mux"
           (transport-memory/text-endpoint (. network ["server-a"])))
          (event-node/attach-transport
           server-b
           "mux"
           (transport-memory/text-endpoint (. network ["server-b"])))])
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request client "room/a" "demo/proxy" ["server-a" "ping-a"] nil))))
        (promise/x:promise-then
         (fn [out-a]
           (return
            (promise/x:promise-then
             (event-node/request client "room/a" "demo/proxy" ["server-b" "ping-b"] nil)
             (fn [out-b]
               (return {"a" out-a
                        "b" out-b}))))))       
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"a" {"server" "server-a" "value" "ping-a"}
      "b" {"server" "server-b" "value" "ping-b"}}

  (notify/wait-on :python
    (var network (transport-memory/memory-network
                  {"client" ["mux-client"]
                   "mux-client" ["client"]
                   "server-a" ["mux-a"]
                   "mux-a" ["server-a"]
                   "server-b" ["mux-b"]
                   "mux-b" ["server-b"]}))
    (var client (event-node/node-create {"id" "client"}))
    (var mux (event-node/node-create
              {"id" "mux"
               "handlers"
               {"demo/proxy"
                {"fn" (fn [space args request mux-node]
                        (return
                         (event-node/request
                          mux-node
                          (. space ["id"])
                          "demo/echo"
                          [(xt/x:get-idx args (xt/x:offset 1))]
                          {"transport_id" (xt/x:get-idx args (xt/x:offset 0))})))
                 "meta" {"kind" "request"}}}}))
    (var server-a (event-node/node-create
                   {"id" "server-a"
                    "handlers"
                    {"demo/echo" {"fn" (fn [space args request server-node]
                                         (return {"server" (. server-node ["id"])
                                                  "value" (xt/x:get-idx args (xt/x:offset 0))}))
                                  "meta" {"kind" "request"}}}}))
    (var server-b (event-node/node-create
                   {"id" "server-b"
                    "handlers"
                    {"demo/echo" {"fn" (fn [space args request server-node]
                                         (return {"server" (. server-node ["id"])
                                                  "value" (xt/x:get-idx args (xt/x:offset 0))}))
                                  "meta" {"kind" "request"}}}}))
    (-> (promise/x:promise-all
         [(event-node/attach-transport
           client
           "mux"
           (transport-memory/text-endpoint (. network ["client"])))
          (event-node/attach-transport
           mux
           "client"
           (transport-memory/text-endpoint (. network ["mux-client"])))
          (event-node/attach-transport
           mux
           "server-a"
           (transport-memory/text-endpoint (. network ["mux-a"])))
          (event-node/attach-transport
           mux
           "server-b"
           (transport-memory/text-endpoint (. network ["mux-b"])))
          (event-node/attach-transport
           server-a
           "mux"
           (transport-memory/text-endpoint (. network ["server-a"])))
          (event-node/attach-transport
           server-b
           "mux"
           (transport-memory/text-endpoint (. network ["server-b"])))])
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request client "room/a" "demo/proxy" ["server-a" "ping-a"] nil))))
        (promise/x:promise-then
         (fn [out-a]
           (return
            (promise/x:promise-then
             (event-node/request client "room/a" "demo/proxy" ["server-b" "ping-b"] nil)
             (fn [out-b]
               (return {"a" out-a
                        "b" out-b}))))))       
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"a" {"server" "server-a" "value" "ping-a"}
      "b" {"server" "server-b" "value" "ping-b"}})
