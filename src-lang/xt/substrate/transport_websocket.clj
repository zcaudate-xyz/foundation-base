(ns xt.substrate.transport-websocket
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.substrate :as main]
             [xt.substrate.transport-memory :as json-transport]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as wsrt]]})

(defspec.xt event-text
  [:fn [:xt/any] :xt/any])

(defspec.xt WebSocketCreateFn
  [:fn [] :xt/any])

(defspec.xt WebSocketConnectFn
  [:fn [:xt/str] :xt/any])

(defspec.xt websocket-url
  [:fn [:xt/any] [:xt/maybe :xt/str]])

(defspec.xt websocket-source
  [:fn [:xt/any] [:xt/dict :xt/str :xt/any]])

(defspec.xt websocket-endpoint
  [:fn [:xt/any] main/NodeTransport])

(defn.xt socket-open?
  [socket]
  (var ready-state (xt/x:get-key socket "readyState"))
  (if (xt/x:nil? ready-state)
    (return true)
    (return (== ready-state 1))))

(defn.xt add-socket-listener
  [socket event handler]
  (cond (xt/x:is-function? (xt/x:get-key socket "addEventListener"))
        (return (. socket (addEventListener event handler false)))

        (xt/x:is-function? (xt/x:get-key socket "on"))
        (return (. socket (on event handler)))

        :else
        (do (xt/x:set-key socket (xt/x:cat "on" event) handler)
            (return socket))))

(defn.xt remove-socket-listener
  [socket event handler]
  (cond (xt/x:is-function? (xt/x:get-key socket "removeEventListener"))
        (return (. socket (removeEventListener event handler false)))

        (xt/x:is-function? (xt/x:get-key socket "off"))
        (return (. socket (off event handler)))

        (xt/x:is-function? (xt/x:get-key socket "removeListener"))
        (return (. socket (removeListener event handler)))

        :else
        (do (xt/x:set-key socket (xt/x:cat "on" event) nil)
            (return socket))))

(defn.xt await-open
  [state]
  (var status (xt/x:get-key state "status"))
  (cond (== status "open")
        (return (promise/x:promise-run
                 (xt/x:get-key state "socket")))

        (== status "error")
        (return
         (promise/x:promise
          (fn []
            (xt/x:throw (xt/x:get-key state "error")))))

        :else
        (return
         (promise/x:promise-then
          (promise/x:with-delay 10
            (fn []
              (return nil)))
          (fn [_]
            (return (-/await-open state)))))))

(defn.xt connect-socket
  [socket-source]
  (var connect-fn (xt/x:get-key socket-source "connect_fn"))
  (if (xt/x:is-function? connect-fn)
    (return (connect-fn (-/websocket-url socket-source)))
    (do (var ctor (or (xt/x:get-key socket-source "WebSocket")
                      WebSocket))
        (when (not (xt/x:is-function? ctor))
          (xt/x:err "websocket source missing connect implementation"))
        (when (xt/x:nil? (-/websocket-url socket-source))
          (xt/x:err "websocket source missing url"))
        (return (new ctor (-/websocket-url socket-source))))))

(defn.xt resolve-socket
  [socket-source]
  (cond (xt/x:is-string? socket-source)
        (return (-/connect-socket {"url" socket-source}))

        (xt/x:is-object? socket-source)
        (do (var create-fn (xt/x:get-key socket-source "create_fn"))
            (cond (xt/x:is-function? create-fn)
                  (return (create-fn))

                  (or (xt/x:has-key? socket-source "url")
                      (xt/x:has-key? socket-source "connect_fn")
                      (xt/x:has-key? socket-source "WebSocket"))
                  (return (-/connect-socket socket-source))

                  :else
                  (return socket-source)))

        :else
        (return socket-source)))

(defn.xt event-text
  "normalizes websocket message events into text payloads"
  {:added "4.1"}
  [event]
  (return (:? (and (xt/x:is-object? event)
                   (xt/x:has-key? event "data"))
              (xt/x:get-key event "data")
              (:? (and (xt/x:is-object? event)
                       (xt/x:has-key? event "text"))
                  (xt/x:get-key event "text")
                  event))))

(defn.xt websocket-url
  "gets the websocket url for a source"
  {:added "4.1"}
  [source]
  (if (xt/x:is-string? source)
    (return source)
    (return (xt/x:get-key source "url"))))

(defn.xt websocket-source
  "creates a websocket-backed text endpoint source"
  {:added "4.1"}
  [socket-source]
  (var current-socket nil)
  (var current-message-callback nil)
  (var current-open-callback nil)
  (var current-error-callback nil)
  (var current-close-callback nil)
  (var send-fn
       (fn [text]
         (when (xt/x:nil? current-socket)
           (xt/x:err "websocket endpoint not started"))
         (return (. current-socket (send text)))))
  (var start-fn
       (fn [listener]
         (return
          (promise/x:promise-then
           (wsrt/ensure-promise (-/resolve-socket socket-source))
           (fn [socket]
             (:= current-socket socket)
             (:= current-message-callback
                 (fn [event]
                   (return (listener event nil))))
             (-/add-socket-listener socket "message" current-message-callback)
             (if (-/socket-open? socket)
               (return socket)
               (do (var state {"status" "opening"
                               "socket" socket
                               "error" "websocket failed to open"})
                   (:= current-open-callback
                       (fn [_event]
                         (xt/x:set-key state "status" "open")
                         (return socket)))
                   (:= current-error-callback
                       (fn [event]
                         (xt/x:set-key state "status" "error")
                         (xt/x:set-key state "error" event)
                         (return event)))
                   (:= current-close-callback
                       (fn [event]
                         (when (not (== (xt/x:get-key state "status") "open"))
                           (xt/x:set-key state "status" "error")
                           (xt/x:set-key state "error"
                                         (:? (xt/x:nil? event)
                                             "websocket closed before open"
                                             event)))
                         (return event)))
                   (-/add-socket-listener socket "open" current-open-callback)
                   (-/add-socket-listener socket "error" current-error-callback)
                   (-/add-socket-listener socket "close" current-close-callback)
                   (return
                    (promise/x:promise-then
                     (-/await-open state)
                     (fn [_]
                       (return socket)))))))))))
  (var stop-fn
       (fn [_]
         (when (and (xt/x:not-nil? current-socket)
                    (xt/x:not-nil? current-message-callback))
           (-/remove-socket-listener current-socket "message" current-message-callback))
         (when (and (xt/x:not-nil? current-socket)
                    (xt/x:not-nil? current-open-callback))
           (-/remove-socket-listener current-socket "open" current-open-callback))
         (when (and (xt/x:not-nil? current-socket)
                    (xt/x:not-nil? current-error-callback))
           (-/remove-socket-listener current-socket "error" current-error-callback))
         (when (and (xt/x:not-nil? current-socket)
                    (xt/x:not-nil? current-close-callback))
           (-/remove-socket-listener current-socket "close" current-close-callback))
         (when (and (xt/x:not-nil? current-socket)
                    (xt/x:is-function? (xt/x:get-key current-socket "close")))
           (. current-socket (close)))
         (:= current-socket nil)
         (:= current-message-callback nil)
         (:= current-open-callback nil)
         (:= current-error-callback nil)
         (:= current-close-callback nil)
         (return true)))
  (return
   {"meta" {"kind" "websocket"}
    "write_fn" send-fn
    "start_fn" start-fn
    "stop_fn" stop-fn}))

(defn.xt websocket-endpoint
  "adapts a websocket-like source into a JSON node transport"
  {:added "4.1"}
  [socket-source]
  (var endpoint
       (json-transport/text-endpoint
        (-/websocket-source socket-source)))
  (xt/x:set-key endpoint
                "meta"
                {"kind" "websocket"
                 "encoding" "json"})
  (return endpoint))
