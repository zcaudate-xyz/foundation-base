(ns xt.net.ws-native
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defprotocol.xt defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-util :as util]
             [xt.net.ws-native :as websocket]]})

(defprotocol.xt IWebsocket
  (connect [client opts])
  (disconnect [client])
  (send [client input])
  (add-listeners [client m])
  (start-heartbeat [client name f interval])
  (stop-heartbeat [client name]))

(defn.xt prepare-url
  [client input]
  (var #{url path} input)
  (if (not (xt/x:nil? url))
    (return url))
  
  (var #{defaults} client)
  (var #{secured
         host
         port
         basepath} defaults)
  
  (return (xt/x:cat "ws" (:? secured "s" "")
                    "://" host
                    ":"
                    (or port "80")
                    (or basepath "")
                    (or path ""))))

(defn.xt socket-open?
  "checks whether a raw websocket socket is in the open state"
  {:added "4.1"}
  [socket]
  (var ready-state (xt/x:get-key socket "readyState"))
  (if (xt/x:nil? ready-state)
    (return true)
    (return (== ready-state 1))))

(defn.xt add-socket-listener
  "attaches an event listener to a raw websocket socket"
  {:added "4.1"}
  [socket event handler]
  (cond (xt/x:is-function? (xt/x:get-key socket "addEventListener"))
        (return (. socket (addEventListener event handler false)))

        (xt/x:is-function? (xt/x:get-key socket "on"))
        (return (. socket (on event handler)))

        :else
        (do (xt/x:set-key socket (xt/x:cat "on" event) handler)
            (return socket))))

(defn.xt remove-socket-listener
  "removes an event listener from a raw websocket socket"
  {:added "4.1"}
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

(defn.xt connect-raw
  "connects a raw websocket client, creating the underlying socket if needed"
  {:added "4.1"}
  [client opts]
  (var raw (xt/x:get-key client "raw"))
  (if (xt/x:not-nil? raw)
    (return client))
  (var defaults (xt/x:get-key client "defaults"))
  (var url (or (xt/x:get-key opts "url")
               (xt/x:get-key defaults "url")))
  (var connect-fn (xt/x:get-key defaults "connect_fn"))
  (var ctor (or (xt/x:get-key defaults "websocket")
                (xt/x:get-key defaults "WebSocket")
                WebSocket))
  (when (xt/x:nil? url)
    (:= url (-/prepare-url client (or opts {}))))
  (cond (xt/x:is-function? connect-fn)
        (:= raw (connect-fn url))

        (xt/x:is-function? ctor)
        (:= raw (new ctor url))

        :else
        (xt/x:err "websocket source missing connect implementation"))
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.xt disconnect-raw
  "disconnects a raw websocket client"
  {:added "4.1"}
  [client]
  (var raw (xt/x:get-key client "raw"))
  (when (and (xt/x:not-nil? raw)
             (xt/x:is-function? (xt/x:get-key raw "close")))
    (. raw (close 1000 "done")))
  (return client))

(defn.xt send-raw
  "sends through a raw websocket client"
  {:added "4.1"}
  [client input]
  (var raw (xt/x:get-key client "raw"))
  (when (and (xt/x:not-nil? raw)
             (xt/x:is-function? (xt/x:get-key raw "send")))
    (. raw (send input)))
  (return client))

(defn.xt add-listeners-raw
  "adds listeners to the underlying raw websocket socket"
  {:added "4.1"}
  [client m]
  (var raw (xt/x:get-key client "raw"))
  (when (xt/x:not-nil? raw)
    (xt/for:object [[k handler] m]
      (-/add-socket-listener raw k handler)))
  (return (xt/x:obj-keys m)))

(defn.xt start-heartbeat-raw
  "raw websocket clients do not provide a built-in heartbeat"
  {:added "4.1"}
  [client name f interval]
  (return nil))

(defn.xt stop-heartbeat-raw
  "raw websocket clients do not provide a built-in heartbeat"
  {:added "4.1"}
  [client name]
  (return nil))

(defimpl.xt RawWebsocketClient
  [defaults raw]
  websocket/IWebsocket
  {connect -/connect-raw
   disconnect -/disconnect-raw
   send -/send-raw
   add-listeners -/add-listeners-raw
   start-heartbeat -/start-heartbeat-raw
   stop-heartbeat -/stop-heartbeat-raw})

(defn.xt client?
  "checks if a value is a ws-native client"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (xt/x:not-nil? (xt/x:get-key obj "::")))))
