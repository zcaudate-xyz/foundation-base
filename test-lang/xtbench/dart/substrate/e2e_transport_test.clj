(ns xtbench.dart.substrate.e2e-transport-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.transport-memory :as transport-memory]
             [js.net.conn-postgres :as js-postgres]
             [xt.net.conn-sql :as conn-sql]
             [xt.db.text.sql-call :as call]
             [xt.db.substrate :as db-helper]
             [postgres.sample.scratch-v0.route-entries :as entries]]})

(fact:global
 {:setup [(l/rt:restart)
                  (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-frame/stream-frame :name demo-001-stream-frame-trigger}
(fact "frame transport with trigger"

  (notify/wait-on :dart
    (var server (substrate/node-create                                                                                                                                                                                                    
                 {"id" "server"
                  "triggers"
                  {"event/ping"
                   {"fn" (fn [space stream node]
                           (var data (xt/x:get-key stream "data"))
                           (var current-state (substrate/get-space-state
                                               node
                                               (. space ["id"])))
                           (substrate/set-space-state
                            node
                            (. space ["id"])
                            (xt/x:obj-assign (or current-state {})
                                             {"pinged" data}))
                           ;; <RETURN>
                           (repl/notify space))
                    "meta" {"kind" "stream"}}}}))
      
    (var client (substrate/node-create {"id" "client"}))
  
    ;; One-way transport: client sends directly to server's receive-frame.
    ;; No return transport is needed because streams are fire-and-forget.
    (substrate/attach-transport
     client
     "server"               
     {"send_fn" (fn [frame]
                  (return
                   (substrate/receive-frame
                    server frame {"transport_id" "client"})))})
    (var frame (frame/stream-frame "space/a" "event/ping" {"data" 1} {}))                                                                                                                                                                   
    (substrate/send-transport client "server" frame))
  => {"id" "space/a", "state" {"pinged" {"data" 1}}, "meta" {}})

^{:refer xt.substrate.base-frame/stream-frame :name demo-001-stream-frame-trigger}
(fact "frame transport with trigger"

  (notify/wait-on :dart
    (var server (substrate/node-create                                                                                                                                                                                                    
                 {"id" "server"
                  "triggers"
                  {"event/ping"
                   {"fn" (fn [space stream node]
                           (var data (xt/x:get-key stream "data"))
                           (var current-state (substrate/get-space-state
                                               node
                                               (. space ["id"])))
                           (substrate/set-space-state
                            node
                            (. space ["id"])
                            (xt/x:obj-assign (or current-state {})
                                             {"pinged" data}))
                           ;; <RETURN>
                           (repl/notify space))
                    "meta" {"kind" "stream"}}}}))
      
    (var client (substrate/node-create {"id" "client"}))
  
    ;; One-way transport: client sends directly to server's receive-frame.
    ;; No return transport is needed because streams are fire-and-forget.
    (substrate/attach-transport
     client
     "server"               
     {"send_fn" (fn [frame]
                  (return
                   (substrate/receive-frame
                    server frame {"transport_id" "client"})))})
    (var frame (frame/stream-frame "space/a" "event/ping" {"data" 1} {}))                                                                                                                                                                   
    (substrate/send-transport client "server" frame))
  => {"id" "space/a", "state" {"pinged" {"data" 1}}, "meta" {}})

^{:refer xt.substrate.base-frame/request-frame :name demo-002-request-frame-dataview}
(fact ""

  (!.dt
    (frame/request-frame "space/a" "demo/echo" ["ping"] {}))
  => (contains-in
      {"space" "space/a", "args" ["ping"], "id" string?, "action" "demo/echo", "kind" "request", "meta" {}})

  (notify/wait-on :dart
    (var server (substrate/node-create
                 {"id" "server"
                  "handlers"
                  {"demo/echo"
                   {"fn" (fn [space args request node]
                           (return {"pong" true "args" args}))
                    "meta" {"kind" "request"}}}}))
  
    (var client (substrate/node-create {"id" "client"}))
  
    ;; client → server
    (substrate/attach-transport
     client
     "server"
     {"send_fn" (fn [frame]
                  (return
                   (substrate/receive-frame
                    server frame {"transport_id" "client"})))})
  
    ;; server → client (return path for response)
    (substrate/attach-transport
     server
     "client"
     {"send_fn" (fn [frame]
                  (return
                   (substrate/receive-frame
                    client frame {"transport_id" "server"})))})
  
    (-> (substrate/request client "space/a" "demo/echo" ["ping"] {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"pong" true "args" ["ping"]})

^{:name demo-002-request-frame-dataview  :refer xt.substrate.base-frame/request-frame}












(comment
  
  (notify/wait-on :js
    (-> (substrate/node-create
         {"services"
          {"db/primary"
           {"database" "test-scratch"}}
          "handlers"
          {"db/fn.primary"
           {"fn" (fn [space args request node]
                   (var opts (xt/x:first args))
                   (var fn-template (. opts ["template"]))
                   (var fn-args     (. opts ["args"]))
                   (return
                    (-> (conn-sql/connect
                         (js-postgres/driver)
                         (substrate/get-service node "db/primary"))
                        (promise/x:promise-then
                         (fn [conn]
                           (return (call/call-raw conn fn-template fn-args)))))))
            "meta" {"kind" "request"}}}})
        (substrate/request nil
                           "db/fn.primary"
                           [{"template" entries/ping
                             "args" []}]
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))

  )
