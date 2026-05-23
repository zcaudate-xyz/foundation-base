(ns xt.substrate-api-test
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
             [xt.substrate :as event-node]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.view-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as event-node]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.view-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as event-node]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate/CANARY.00-create-node :added "4.1" :adopt true}
(fact "registers a handler"

  (!.js
    (event-node/node-create {}))
  => (contains-in
      {"handlers" {},
       "id" string?
       "spaces" {},
       "::" "substrate",
       "transports" {},
       "triggers" {},
       "meta" {},
       "router" {"subscriptions" {}, "connections" {}},
       "pending" {},
       "listeners" {}})

  (!.lua
    (event-node/node-create {}))
  => (contains-in
      {"handlers" {},
       "id" string?
       "spaces" {},
       "::" "substrate",
       "transports" {},
       "triggers" {},
       "meta" {},
       "router" {"subscriptions" {}, "connections" {}},
       "pending" {},
       "listeners" {}})

  (!.py
    (event-node/node-create {}))
  => (contains-in
      {"handlers" {},
       "id" string?
       "spaces" {},
       "::" "substrate",
       "transports" {},
       "triggers" {},
       "meta" {},
       "router" {"subscriptions" {}, "connections" {}},
       "pending" {},
       "listeners" {}}))

^{:refer xt.substrate/CANARY.00-create-space :added "4.1" :adopt true}
(fact "creates a space in the node"

  (!.js
    (event-node/create-space (event-node/node-create {})
                             "room/a"))
  => {"id" "room/a", "state" {}, "meta" {}}

  (!.lua
    (event-node/create-space (event-node/node-create {})
                             "room/a"))
  => {"id" "room/a", "state" {}, "meta" {}}

  (!.py
    (event-node/create-space (event-node/node-create {})
                             "room/a"))
  => {"id" "room/a", "state" {}, "meta" {}})

^{:refer xt.substrate/CANARY.00-create-space-map :added "4.1" :adopt true}
(fact "creates a space in the node"

  (!.js
    (event-node/get-space-state
     (event-node/node-create
      {"id" "node-a"
       "meta" {"cluster" "local"}
       "spaces" {"room/a" {"state" {"count" 0}
                           "meta"  {"role" "user-a"}}}
       "handlers" {"ping" {"fn" (fn [])
                           "meta" {"kind" "request"}}}})
     "room/a"))
  => {"count" 0}

  (!.lua
    (event-node/get-space-state
     (event-node/node-create
      {"id" "node-a"
       "meta" {"cluster" "local"}
       "spaces" {"room/a" {"state" {"count" 0}
                           "meta"  {"role" "user-a"}}}
       "handlers" {"ping" {"fn" (fn [])
                           "meta" {"kind" "request"}}}})
     "room/a"))
  => {"count" 0}

  (!.py
    (event-node/get-space-state
     (event-node/node-create
      {"id" "node-a"
       "meta" {"cluster" "local"}
       "spaces" {"room/a" {"state" {"count" 0}
                           "meta"  {"role" "user-a"}}}
       "handlers" {"ping" {"fn" (fn [])
                           "meta" {"kind" "request"}}}})
     "room/a"))
  => {"count" 0})

^{:refer xt.substrate/CANARY.00-create-space-handler :added "4.1" :adopt true}
(fact "creates a node with a declarative map"

  (notify/wait-on :js
    (var ping-handler (fn [space args request node]
                        ;; has update space state
                        (event-node/update-space-state
                         node
                         (. space ["id"])
                         (fn [state entry node]
                           (return
                            {"count" (+ 1 (. state ["count"]))})))
                        (return (xt/x:cat "ping "
                                          (. space ["meta"] ["role"])
                                          " - "
                                          (xt/x:to-string (. space ["state"] ["count"]))))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "handlers" {"ping" {"fn" ping-handler
                                 "meta" {"kind" "request"}}}}))
    
    (-> (event-node/request n "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/request n "room/a" "ping" [] nil))))
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/request n "room/a" "ping" [] nil))))
        (promise/x:promise-then
         (fn [res]
           
           (repl/notify res)))))
  => "ping user-a - 3"

  (notify/wait-on :lua
    (var ping-handler (fn [space args request node]
                        ;; has update space state
                        (event-node/update-space-state
                         node
                         (. space ["id"])
                         (fn [state entry node]
                           (return
                            {"count" (+ 1 (. state ["count"]))})))
                        (return (xt/x:cat "ping "
                                          (. space ["meta"] ["role"])
                                          " - "
                                          (xt/x:to-string (. space ["state"] ["count"]))))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "handlers" {"ping" {"fn" ping-handler
                                 "meta" {"kind" "request"}}}}))
    
    (-> (event-node/request n "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/request n "room/a" "ping" [] nil))))
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/request n "room/a" "ping" [] nil))))
        (promise/x:promise-then
         (fn [res]
           
           (repl/notify res)))))
  => "ping user-a - 3"

  (notify/wait-on :python
    (var ping-handler (fn [space args request node]
                        ;; has update space state
                        (event-node/update-space-state
                         node
                         (. space ["id"])
                         (fn [state entry node]
                           (return
                            {"count" (+ 1 (. state ["count"]))})))
                        (return (xt/x:cat "ping "
                                          (. space ["meta"] ["role"])
                                          " - "
                                          (xt/x:to-string (. space ["state"] ["count"]))))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "handlers" {"ping" {"fn" ping-handler
                                 "meta" {"kind" "request"}}}}))
    
    (-> (event-node/request n "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/request n "room/a" "ping" [] nil))))
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/request n "room/a" "ping" [] nil))))
        (promise/x:promise-then
         (fn [res]
           
           (repl/notify res)))))
  => "ping user-a - 3")

^{:refer xt.substrate/CANARY.01-register-handler :added "4.1" :adopt true}
(fact "registers a handler"

  (notify/wait-on :js
    (-> (event-node/node-create
         {"spaces" {"room/a" {"state" {"count" 0}
                              "meta"  {"role" "user-a"}}}
          "handlers" {"ping" {"fn" (fn [space args request node]
                                     (return {"ok" true
                                              "reply" "pong"
                                              "space"   (. space ["id"])}))
                              "meta" {"kind" "request"}}}})
        (event-node/request "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"space" "room/a", "reply" "pong", "ok" true}

  (notify/wait-on :lua
    (-> (event-node/node-create
         {"spaces" {"room/a" {"state" {"count" 0}
                              "meta"  {"role" "user-a"}}}
          "handlers" {"ping" {"fn" (fn [space args request node]
                                     (return {"ok" true
                                              "reply" "pong"
                                              "space"   (. space ["id"])}))
                              "meta" {"kind" "request"}}}})
        (event-node/request "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"space" "room/a", "reply" "pong", "ok" true}

  (notify/wait-on :python
    (-> (event-node/node-create
         {"spaces" {"room/a" {"state" {"count" 0}
                              "meta"  {"role" "user-a"}}}
          "handlers" {"ping" {"fn" (fn [space args request node]
                                     (return {"ok" true
                                              "reply" "pong"
                                              "space"   (. space ["id"])}))
                              "meta" {"kind" "request"}}}})
        (event-node/request "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res]
           (repl/notify res)))))
  => {"space" "room/a", "reply" "pong", "ok" true})

^{:refer xt.substrate/CANARY.01-register-trigger :added "4.1" :adopt true}
(fact "registers a trigger"

  (notify/wait-on :js
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "triggers" {"event/pinged"
                         {"fn" (fn [space stream node]
                                 (event-node/update-space-state
                                  node
                                  (. space ["id"])
                                  (fn [state entry node]
                                    (return
                                     {"count"      (+ 1 (. state ["count"]))
                                      "last_event" (. stream ["signal"])
                                      "last_data"  (. stream ["data"])
                                      "role"       (. space ["meta"] ["role"])})))
                                 (return true))
                          "meta" {"kind" "stream"}}}}))
    (-> n
        (event-node/publish "room/a" "event/pinged" {"source" "publish-1"} nil)
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/publish n "room/a" "event/pinged" {"source" "publish-2"} nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/publish n "room/a" "event/pinged" {"source" "publish-3"} nil))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (event-node/get-space-state n "room/a"))))))
  => {"role" "user-a",
      "last_event" "event/pinged",
      "count" 3,
      "last_data" {"source" "publish-3"}}

  (notify/wait-on :lua
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "triggers" {"event/pinged"
                         {"fn" (fn [space stream node]
                                 (event-node/update-space-state
                                  node
                                  (. space ["id"])
                                  (fn [state entry node]
                                    (return
                                     {"count"      (+ 1 (. state ["count"]))
                                      "last_event" (. stream ["signal"])
                                      "last_data"  (. stream ["data"])
                                      "role"       (. space ["meta"] ["role"])})))
                                 (return true))
                          "meta" {"kind" "stream"}}}}))
    (-> n
        (event-node/publish "room/a" "event/pinged" {"source" "publish-1"} nil)
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/publish n "room/a" "event/pinged" {"source" "publish-2"} nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/publish n "room/a" "event/pinged" {"source" "publish-3"} nil))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (event-node/get-space-state n "room/a"))))))
  => {"role" "user-a",
      "last_event" "event/pinged",
      "count" 3,
      "last_data" {"source" "publish-3"}}

  (notify/wait-on :python
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "triggers" {"event/pinged"
                         {"fn" (fn [space stream node]
                                 (event-node/update-space-state
                                  node
                                  (. space ["id"])
                                  (fn [state entry node]
                                    (return
                                     {"count"      (+ 1 (. state ["count"]))
                                      "last_event" (. stream ["signal"])
                                      "last_data"  (. stream ["data"])
                                      "role"       (. space ["meta"] ["role"])})))
                                 (return true))
                          "meta" {"kind" "stream"}}}}))
    (-> n
        (event-node/publish "room/a" "event/pinged" {"source" "publish-1"} nil)
        (promise/x:promise-then
         (fn [res]
           (return
            (event-node/publish n "room/a" "event/pinged" {"source" "publish-2"} nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/publish n "room/a" "event/pinged" {"source" "publish-3"} nil))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (event-node/get-space-state n "room/a"))))))
  => {"role" "user-a",
      "last_event" "event/pinged",
      "count" 3,
      "last_data" {"source" "publish-3"}})

^{:refer xt.substrate/CANARY.02-request-publish-trigger-workflow :added "4.1" :adopt true}
(fact "request handlers can publish events and wait for trigger side effects"

  (notify/wait-on :js
    (var ping-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"      (. state ["count"])
                "last_event" (. stream ["signal"])
                "last_ping"  (. stream ["data"] ["count"])
                "role"       (. stream ["data"] ["role"])})))
           (return true)))
    
    (var ping-handler
         (fn [space args request node]
           (var next-state
                (event-node/update-space-state
                 node
                 (. space ["id"])
                 (fn [state entry node]
                   (return
                    {"count" (+ 1 (. state ["count"]))}))))
           (return
            (promise/x:promise-then
             (event-node/publish
              node
              (. space ["id"])
              "event/pinged"
              {"count" (. next-state ["count"])
               "role"  (. space ["meta"] ["role"])}
              nil)
             (fn [_]
               (return
                (xt/x:cat "ping "
                          (. space ["meta"] ["role"])
                          " - "
                          (xt/x:to-string (. next-state ["count"])))))))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "handlers" {"ping" {"fn" ping-handler
                                 "meta" {"kind" "request"}}}
             "triggers" {"event/pinged" {"fn" ping-trigger
                                         "meta" {"kind" "stream"}}}}))
    
    (-> (event-node/request n "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res1]
           (return
            (promise/x:promise-then
             (event-node/request n "room/a" "ping" [] nil)
             (fn [res2]
               (return
                (promise/x:promise-then
                 (event-node/request n "room/a" "ping" [] nil)
                 (fn [res3]
                   (return
                    {"responses" [res1 res2 res3]
                     "state"     (event-node/get-space-state n "room/a")})))))))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"responses" ["ping user-a - 1"
                   "ping user-a - 2"
                   "ping user-a - 3"],
      "state" {"count" 3
               "last_event" "event/pinged"
               "last_ping" 3
               "role" "user-a"}}

  (notify/wait-on :lua
    (var ping-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"      (. state ["count"])
                "last_event" (. stream ["signal"])
                "last_ping"  (. stream ["data"] ["count"])
                "role"       (. stream ["data"] ["role"])})))
           (return true)))
    
    (var ping-handler
         (fn [space args request node]
           (var next-state
                (event-node/update-space-state
                 node
                 (. space ["id"])
                 (fn [state entry node]
                   (return
                    {"count" (+ 1 (. state ["count"]))}))))
           (return
            (promise/x:promise-then
             (event-node/publish
              node
              (. space ["id"])
              "event/pinged"
              {"count" (. next-state ["count"])
               "role"  (. space ["meta"] ["role"])}
              nil)
             (fn [_]
               (return
                (xt/x:cat "ping "
                          (. space ["meta"] ["role"])
                          " - "
                          (xt/x:to-string (. next-state ["count"])))))))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "handlers" {"ping" {"fn" ping-handler
                                 "meta" {"kind" "request"}}}
             "triggers" {"event/pinged" {"fn" ping-trigger
                                         "meta" {"kind" "stream"}}}}))
    
    (-> (event-node/request n "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res1]
           (return
            (promise/x:promise-then
             (event-node/request n "room/a" "ping" [] nil)
             (fn [res2]
               (return
                (promise/x:promise-then
                 (event-node/request n "room/a" "ping" [] nil)
                 (fn [res3]
                   (return
                    {"responses" [res1 res2 res3]
                     "state"     (event-node/get-space-state n "room/a")})))))))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"responses" ["ping user-a - 1"
                   "ping user-a - 2"
                   "ping user-a - 3"],
      "state" {"count" 3
               "last_event" "event/pinged"
               "last_ping" 3
               "role" "user-a"}}

  (notify/wait-on :python
    (var ping-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"      (. state ["count"])
                "last_event" (. stream ["signal"])
                "last_ping"  (. stream ["data"] ["count"])
                "role"       (. stream ["data"] ["role"])})))
           (return true)))
    
    (var ping-handler
         (fn [space args request node]
           (var next-state
                (event-node/update-space-state
                 node
                 (. space ["id"])
                 (fn [state entry node]
                   (return
                    {"count" (+ 1 (. state ["count"]))}))))
           (return
            (promise/x:promise-then
             (event-node/publish
              node
              (. space ["id"])
              "event/pinged"
              {"count" (. next-state ["count"])
               "role"  (. space ["meta"] ["role"])}
              nil)
             (fn [_]
               (return
                (xt/x:cat "ping "
                          (. space ["meta"] ["role"])
                          " - "
                          (xt/x:to-string (. next-state ["count"])))))))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "meta" {"cluster" "local"}
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "handlers" {"ping" {"fn" ping-handler
                                 "meta" {"kind" "request"}}}
             "triggers" {"event/pinged" {"fn" ping-trigger
                                         "meta" {"kind" "stream"}}}}))
    
    (-> (event-node/request n "room/a" "ping" [] nil)
        (promise/x:promise-then
         (fn [res1]
           (return
            (promise/x:promise-then
             (event-node/request n "room/a" "ping" [] nil)
             (fn [res2]
               (return
                (promise/x:promise-then
                 (event-node/request n "room/a" "ping" [] nil)
                 (fn [res3]
                   (return
                    {"responses" [res1 res2 res3]
                     "state"     (event-node/get-space-state n "room/a")})))))))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"responses" ["ping user-a - 1"
                   "ping user-a - 2"
                   "ping user-a - 3"],
      "state" {"count" 3
               "last_event" "event/pinged"
               "last_ping" 3
               "role" "user-a"}})

^{:refer xt.substrate/CANARY.03-trigger-chain-workflow :added "4.1" :adopt true}
(fact "triggers can publish follow-on events into another trigger"

  (notify/wait-on :js
    (var audit-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"       (. state ["count"])
                "last_event"  (. state ["last_event"])
                "audit_event" (. stream ["signal"])
                "audit_data"  (. stream ["data"])})))
           (return true)))
    
    (var ping-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"      (+ 1 (. state ["count"]))
                "last_event" (. stream ["signal"])})))
           (return
            (event-node/publish
             node
             (. space ["id"])
             "event/pinged.audit"
             {"source" (. stream ["signal"])
              "label"  (. stream ["data"] ["label"])}
             nil))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "triggers" {"event/pinged" {"fn" ping-trigger
                                         "meta" {"kind" "stream"}}
                         "event/pinged.audit" {"fn" audit-trigger
                                               "meta" {"kind" "stream"}}}}))
    
    (-> (event-node/publish n "room/a" "event/pinged" {"label" "first"} nil)
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (event-node/get-space-state n "room/a"))))))
  => {"count" 1
      "last_event" "event/pinged"
      "audit_event" "event/pinged.audit"
      "audit_data" {"source" "event/pinged"
                    "label" "first"}}

  (notify/wait-on :lua
    (var audit-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"       (. state ["count"])
                "last_event"  (. state ["last_event"])
                "audit_event" (. stream ["signal"])
                "audit_data"  (. stream ["data"])})))
           (return true)))
    
    (var ping-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"      (+ 1 (. state ["count"]))
                "last_event" (. stream ["signal"])})))
           (return
            (event-node/publish
             node
             (. space ["id"])
             "event/pinged.audit"
             {"source" (. stream ["signal"])
              "label"  (. stream ["data"] ["label"])}
             nil))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "triggers" {"event/pinged" {"fn" ping-trigger
                                         "meta" {"kind" "stream"}}
                         "event/pinged.audit" {"fn" audit-trigger
                                               "meta" {"kind" "stream"}}}}))
    
    (-> (event-node/publish n "room/a" "event/pinged" {"label" "first"} nil)
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (event-node/get-space-state n "room/a"))))))
  => {"count" 1
      "last_event" "event/pinged"
      "audit_event" "event/pinged.audit"
      "audit_data" {"source" "event/pinged"
                    "label" "first"}}

  (notify/wait-on :python
    (var audit-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"       (. state ["count"])
                "last_event"  (. state ["last_event"])
                "audit_event" (. stream ["signal"])
                "audit_data"  (. stream ["data"])})))
           (return true)))
    
    (var ping-trigger
         (fn [space stream node]
           (event-node/update-space-state
            node
            (. space ["id"])
            (fn [state entry node]
              (return
               {"count"      (+ 1 (. state ["count"]))
                "last_event" (. stream ["signal"])})))
           (return
            (event-node/publish
             node
             (. space ["id"])
             "event/pinged.audit"
             {"source" (. stream ["signal"])
              "label"  (. stream ["data"] ["label"])}
             nil))))
    
    (var n (event-node/node-create
            {"id" "node-a"
             "spaces" {"room/a" {"state" {"count" 0}
                                 "meta"  {"role" "user-a"}}}
             "triggers" {"event/pinged" {"fn" ping-trigger
                                         "meta" {"kind" "stream"}}
                         "event/pinged.audit" {"fn" audit-trigger
                                               "meta" {"kind" "stream"}}}}))
    
    (-> (event-node/publish n "room/a" "event/pinged" {"label" "first"} nil)
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (event-node/get-space-state n "room/a"))))))
  => {"count" 1
      "last_event" "event/pinged"
      "audit_event" "event/pinged.audit"
      "audit_data" {"source" "event/pinged"
                    "label" "first"}})

(comment
  (s/snapto '[xt.substrate.base-space])
  (s/seedgen-langremove '[xt.substrate.base-space] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.substrate.base-space] {:lang [:lua :python] :write true}))
