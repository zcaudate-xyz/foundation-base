(ns xtbench.dart.substrate-transport-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate/CANARY.00-transport-attach :added "4.1" :adopt true}
(fact "create transports between nodes"

  (notify/wait-on :dart
    (var server (event-node/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo" {"fn" (fn [space args request server-node]
                                       (return {"space"   (. space ["id"])
                                                "server"  (. server-node ["id"])}))
                                "meta" {"kind" "request"}}}}))
    (event-node/attach-transport
     server
     "client"
     {"send_fn"
      (fn [frame]
        (return
         (repl/notify frame)))})
    (event-node/request server "room/a" "demo/echo" [{"ping" 1}] nil))
  => (contains-in
      {"space" "room/a"
       "args" [{"ping" 1}]
       "id" string?
       "action" "demo/echo"
       "kind" "request"
       "meta" {}})

  (notify/wait-on [:dart 10000]
    (var server (event-node/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo" {"fn" (fn [space args request server-node]
                                       (return {"space"   (. space ["id"])
                                                "server"  (. server-node ["id"])}))
                                "meta" {"kind" "request"}}}}))
    (event-node/attach-transport
     server
     "client"
     {"send_fn"
      (fn [frame]
        (return
         (repl/notify frame)))})
    (event-node/request server "room/a" "demo/echo" [{"ping" 1}] nil))
  => (contains-in
      {"space" "room/a"
       "args" [{"ping" 1}]
       "id" string?
       "action" "demo/echo"
       "kind" "request"
       "meta" {}})

  (notify/wait-on :dart
    (var server (event-node/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo" {"fn" (fn [space args request server-node]
                                       (return {"space"   (. space ["id"])
                                                "server"  (. server-node ["id"])}))
                                "meta" {"kind" "request"}}}}))
    (event-node/attach-transport
     server
     "client"
     {"send_fn"
      (fn [frame]
        (return
         (repl/notify frame)))})
    (event-node/request server "room/a" "demo/echo" [{"ping" 1}] nil))
  => (contains-in
      {"space" "room/a"
       "args" [{"ping" 1}]
       "id" string?
       "action" "demo/echo"
       "kind" "request"
       "meta" {}}))

^{:refer xt.substrate/CANARY.00-transport-two-way :added "4.1" :adopt true}
(fact "create transports between nodes"

  (notify/wait-on :dart
    (var client (event-node/node-create
                 {"id" "client"}))
    (var server (event-node/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo" {"fn" (fn [space args request server-node]
                                       (return {"space"   (. space ["id"])
                                                "server"  (. server-node ["id"])}))
                                "meta" {"kind" "request"}}}}))
    (-> (promise/x:promise-all
         [(event-node/attach-transport
           client
           "server"
           {"send_fn"
            (fn [frame]
              (return
               (event-node/receive-frame
                server
                frame
                {"transport_id" "client"})))})
          (event-node/attach-transport
           server
           "client"
           {"send_fn"
            (fn [frame]
              (return
               (event-node/receive-frame
                client
                frame
                {"transport_id" "server"})))})])
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request client "room/a" "demo/echo" [{"ping" 1}] nil))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"space" "room/a" "server" "server"}

  (notify/wait-on :dart
    (var client (event-node/node-create
                 {"id" "client"}))
    (var server (event-node/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo" {"fn" (fn [space args request server-node]
                                       (return {"space"   (. space ["id"])
                                                "server"  (. server-node ["id"])}))
                                "meta" {"kind" "request"}}}}))
    (-> (promise/x:promise-all
         [(event-node/attach-transport
           client
           "server"
           {"send_fn"
            (fn [frame]
              (return
               (event-node/receive-frame
                server
                frame
                {"transport_id" "client"})))})
          (event-node/attach-transport
           server
           "client"
           {"send_fn"
            (fn [frame]
              (return
               (event-node/receive-frame
                client
                frame
                {"transport_id" "server"})))})])
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request client "room/a" "demo/echo" [{"ping" 1}] nil))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"space" "room/a" "server" "server"})

(comment
  (s/snapto '[xt.substrate-transport])
  (s/seedgen-langremove '[xt.substrate-transport] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.substrate-transport] {:lang [:lua :python] :write true}))
