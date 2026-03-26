(ns js.cell-v3.transport.socket
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.cell-v3.transport.core :as transport]]})

(defn.js encode-frame
  "encodes a frame for socket transport"
  {:added "4.0"}
  [transport frame]
  (var f (or (. transport ["encode"])
             k/json-encode))
  (return (f frame transport)))

(defn.js decode-frame
  "decodes a frame from socket transport"
  {:added "4.0"}
  [transport payload]
  (cond (k/is-string? payload)
        (return (k/json-decode payload))

        (. transport ["decode"])
        (return ((. transport ["decode"]) payload transport))

        :else
        (return payload)))

(defn.js receive-message
  "handles a socket message event"
  {:added "4.0"}
  [transport event]
  (return (transport/receive-frame transport
                                   (-/decode-frame transport (. event ["data"]))
                                   nil)))

(defn.js attach-socket
  "attaches a socket-like channel to a transport"
  {:added "4.0"}
  [transport socket]
  (var listener (fn [event]
                  (return (-/receive-message transport event))))
  (:= (. transport ["channel"]) socket)
  (:= (. transport ["socket"]) socket)
  (:= (. transport ["socketListener"]) listener)
  (. socket (addEventListener "message" listener false))
  (return transport))

(defn.js detach-socket
  "detaches the current socket listener"
  {:added "4.0"}
  [transport]
  (var socket (. transport ["socket"]))
  (var listener (. transport ["socketListener"]))
  (when (and socket
             listener
             (. socket ["removeEventListener"]))
    (. socket (removeEventListener "message" listener false)))
  (:= (. transport ["socket"]) nil)
  (:= (. transport ["socketListener"]) nil)
  (return transport))

(defn.js make-socket-transport
  "creates a transport bound to a websocket-like channel"
  {:added "4.0"}
  [socket opts]
  (:= opts (or opts {}))
  (var tx (transport/make-transport socket opts))
  (:= (. tx ["encode"]) (. opts ["encode"]))
  (:= (. tx ["decode"]) (. opts ["decode"]))
  (:= (. tx ["send"])
      (fn [frame]
        (return (. socket (send (-/encode-frame tx frame))))))
  (-/attach-socket tx socket)
  (when (or (. opts ["system"])
            (. opts ["handleCall"])
            (. opts ["handleEmit"]))
    (transport/bind-system tx
                           (. opts ["system"])
                           {:forwardAll (. opts ["forwardAll"])
                            :context (. opts ["context"])
                            :handleCall (. opts ["handleCall"])
                            :handleEmit (. opts ["handleEmit"])}))
  (return tx))
