(ns xtbench.dart.substrate.e2e-transport-memory-test
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

^{:refer xt.substrate.transport-memory/text-endpoint :added "4.1"}
(fact "two nodes talk to each other over the memory transport"

  (notify/wait-on :dart
    (var wire (transport-memory/memory-pair {"left_id" "client"
                                             "right_id" "server"}))
    (var client (event-node/node-create {"id" "client"}))
    (var server (event-node/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo" {"fn" (fn [space args request server-node]
                                       (return {"space" (. space ["id"])
                                                "action" (. request ["action"])
                                                "args" args
                                                "server" (. server-node ["id"])}))
                                "meta" {"kind" "request"}}}}))
    (-> (promise/x:promise-all
         [(event-node/attach-transport
           client
           "server"
           (transport-memory/text-endpoint (. wire ["left"])))
          (event-node/attach-transport
           server
           "client"
           (transport-memory/text-endpoint (. wire ["right"])))])
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request
             client
             "room/a"
             "demo/echo"
             ["ping"]
             nil))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"space" "room/a"
      "action" "demo/echo"
      "args" ["ping"]
      "server" "server"})
