(ns xt.substrate.walkthrough.s03-transport-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.walkthrough.s03-transport-test/demo-000-stream-frame-basic}
(fact "frame transport with trigger"

  
  ;; A stream-frame carries a signal, space, and data payload.
  ;; Its shape is: {:kind "stream", :space "...", :signal "...", :data {...}, :meta {}}
  (!.js
    (frame/stream-frame "space/a" "event/ping" {"data" 1} {}))
  => (contains {"space" "space/a", "id" string?, "signal" "event/ping", "kind" "stream", "meta" {}, "data" {"data" 1}})

  ;; To move a stream-frame between nodes, attach a transport with a send_fn
  ;; that forwards directly into the remote node's receive-frame.
  ;;
  ;; Note: send-transport bypasses the router and subscriptions entirely.
  ;;       The frame is delivered regardless of what the remote node is
  ;;       subscribed to.
  ;;
  ;; Flow:
  ;;   1. Client calls send-transport with the stream-frame
  ;;   2. Client's send_fn calls server.receive-frame(...)
  ;;   3. Server demuxes to receive-publish → invoke-trigger
  ;;   4. Server finds the "event/ping" trigger and fires it
  ;;   5. Trigger calls repl/notify with the stream payload
  (notify/wait-on :js
    (var server (substrate/node-create                                                                                                                                                                                                    
                 {"id" "server"
                  "triggers"
                  {"event/ping"
                   {"fn" (fn [space stream node]
                           ;; <RETURN>
                           (repl/notify stream))
                    "meta" {"kind" "stream"}}}}))
    
    (var client (substrate/node-create {"id" "client"}))

    ;; One-way transport: client can send to server, but server has no
    ;; return transport. This is fine for streams (fire-and-forget).
    (substrate/attach-transport
     client
     "server"               
     {"send_fn" (fn [frame]
                  (return
                   (substrate/receive-frame
                    server frame {"transport_id" "client"})))})
    (var frame (frame/stream-frame "space/a" "event/ping" {"data" 1} {}))                                                                                                                                                                   
    (substrate/send-transport client "server" frame))
  ;; <RETURN>
  => (contains {"space" "space/a", "id" string?, "signal" "event/ping", "kind" "stream", "meta" {}, "data" {"data" 1}}))


^{:refer xt.substrate.walkthrough.s03-transport-test/demo-001-stream-frame-trigger}
(fact "frame transport with trigger"

  ;; A stream-frame can trigger state mutation on the receiving node.
  ;;
  ;; Here the server has a trigger for "event/ping" that:
  ;;   1. Extracts the payload from the stream frame
  ;;   2. Reads the current space state
  ;;   3. Merges {"pinged" data} into that state
  ;;   4. Calls repl/notify with the space object so the test can verify
  ;;
  ;; Flow:
  ;;   client.send-transport → server.receive-frame → invoke-trigger
  ;;   → trigger fn mutates state via set-space-state → repl/notify
  (notify/wait-on :js
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
  ;; </RETURN>
  => {"id" "space/a", "state" {"pinged" {"data" 1}}, "meta" {}})



^{:refer xt.substrate.walkthrough.s03-transport-test/demo-002-request-frame-handle}
(fact "frame transport with request"
  
  ;; A request-frame carries an action, args, and a reply address.
  ;; Its shape is: {:kind "request", :space "...", :action "...", :args [...], :meta {}}
  (!.js
    (frame/request-frame "space/a" "demo/echo" ["ping"] {}))
  => (contains-in
      {"space" "space/a", "args" ["ping"], "id" string?, "action" "demo/echo", "kind" "request", "meta" {}})
  
  ;; Request/response round-trip over transport.
  ;;
  ;; Unlike streams (fire-and-forget), requests need a return path.
  ;; respond-ok looks at ctx.transport_id to decide where to send the
  ;; response frame. Without a return transport, the response is lost
  ;; and the caller's promise never settles.
  ;;
  ;; Flow:
  ;;   1. substrate/request creates a pending entry + request frame
  ;;   2. Client sends frame via "server" transport
  ;;   3. Server receive-frame → receive-request → invoke-handler
  ;;   4. Handler returns {"pong" true ...}
  ;;   5. respond-ok sees transport_id="client", sends response via
  ;;      server's "client" transport
  ;;   6. Client receive-frame → receive-response → settles pending
  ;;   7. Promise resolves with the handler's return value
  (notify/wait-on :js
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




