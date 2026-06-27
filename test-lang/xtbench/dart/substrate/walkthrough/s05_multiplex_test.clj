(ns xtbench.dart.substrate.walkthrough.s05-multiplex-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.walkthrough.s05-multiplex-test/demo-001-memory-network}
(fact "a shared memory network supports multiplexer nodes with multiple upstream transports"

  (notify/wait-on :dart
    (var network (transport-memory/memory-network
                  {"client" ["mux-client"]
                   "mux-client" ["client"]
                   "server-a" ["mux-a"]
                   "mux-a" ["server-a"]
                   "server-b" ["mux-b"]
                   "mux-b" ["server-b"]}))
  
    ;;
    ;; Client has no handlers; it only sends requests to the mux.
    ;;
    (var client (event-node/node-create {"id" "client"}))
  
    ;;
    ;; The mux exposes a single "demo/proxy" handler. It receives
    ;; args [target-id payload], then issues a new request to the
    ;; named target transport. This is the multiplexer: one public
    ;; action fans out to many backend nodes.
    ;;
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
  
    ;;
    ;; Two identical backend servers. Each only needs to know the
    ;; "demo/echo" action and returns its own id so the test can
    ;; verify which server actually handled the request.
    ;;
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
  
    ;;
    ;; Attach all six transport endpoints. After this, the mux can
    ;; reach both servers and the client can reach the mux.
    ;;
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
  
        ;;
        ;; Ask the mux to proxy to server-a, then to server-b.
        ;; The responses prove routing went to the correct backend.
        ;;
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
