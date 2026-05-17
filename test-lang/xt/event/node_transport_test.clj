(ns xt.event.node-transport-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.instance-model :as model]
             [xt.db.node.test-fixtures :as fixtures]
             [xt.event.node :as event-node]
             [xt.event.node-main :as node-main]]})

(l/script- :lua.nginx
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.instance-model :as model]
             [xt.db.node.test-fixtures :as fixtures]
             [xt.event.node :as event-node]
             [xt.event.node-main :as node-main]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.instance-model :as model]
             [xt.db.node.test-fixtures :as fixtures]
             [xt.event.node :as event-node]
             [xt.event.node-main :as node-main]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node/CANARY.00-transport-attach :added "4.1" :adopt true}
(fact "create transports between nodes"

  (notify/wait-on :js
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

  (notify/wait-on :lua
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

  (notify/wait-on :python
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

^{:refer xt.event.node/CANARY.00-transport-two-way :added "4.1" :adopt true}
(fact "create transports between nodes"

  (notify/wait-on :js
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
               (node-main/receive-frame
                server
                frame
                {"transport_id" "client"})))})
          (event-node/attach-transport
           server
           "client"
           {"send_fn"
            (fn [frame]
              (return
               (node-main/receive-frame
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

  (notify/wait-on :lua
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
               (node-main/receive-frame
                server
                frame
                {"transport_id" "client"})))})
          (event-node/attach-transport
           server
           "client"
           {"send_fn"
            (fn [frame]
              (return
               (node-main/receive-frame
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

  (notify/wait-on :python
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
               (node-main/receive-frame
                server
                frame
                {"transport_id" "client"})))})
          (event-node/attach-transport
           server
           "client"
           {"send_fn"
            (fn [frame]
              (return
               (node-main/receive-frame
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
  (s/snapto '[xt.event.node-transport])
  (s/seedgen-langremove '[xt.event.node-transport] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.event.node-transport] {:lang [:lua :python] :write true}))
