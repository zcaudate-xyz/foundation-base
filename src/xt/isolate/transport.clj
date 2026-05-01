(ns xt.isolate.transport
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-trace :as trace]
             [xt.isolate.frame :as frame]]})

;;
;; Transport capability map
;;
;; A transport is a plain map that abstracts the physical message-passing
;; boundary between the client and the isolate.  The map has three keys:
;;
;;   :send!   (fn [frame])   – send a frame to the other side
;;   :listen! (fn [handler]) – register a handler for incoming frames
;;   :close!  (fn [])        – optional; tear down the transport
;;
;; By using a map instead of an object hierarchy or JS-specific APIs,
;; any language or runtime can provide its own transport implementation
;; (in-process queues, WebSockets, stdio pipes, etc.).
;;

(defspec.xt transport-send
  [:fn [xt.isolate.spec/TransportMap xt.isolate.spec/RequestFrame]
   :xt/any])

(defspec.xt transport-listen
  [:fn [xt.isolate.spec/TransportMap [:fn [xt.isolate.spec/ResponseFrame] :xt/any]]
   :xt/any])

(defspec.xt transport-close
  [:fn [xt.isolate.spec/TransportMap]
   :xt/any])

(defspec.xt transport-make
  [:fn [[:fn [xt.isolate.spec/RequestFrame] :xt/any]
        [:fn [[:fn [xt.isolate.spec/ResponseFrame] :xt/any]] :xt/any]
        [:xt/maybe [:fn [] :xt/any]]]
   xt.isolate.spec/TransportMap])

;;
;; Implementations
;;

(defn.xt transport-send
  "sends a frame through the transport"
  {:added "4.0"}
  [transport frame]
  (var send (. transport ["send"]))
  (return (send frame)))

(defn.xt transport-listen
  "registers a handler for incoming frames on the transport"
  {:added "4.0"}
  [transport handler]
  (var listen (. transport ["listen"]))
  (return (listen handler)))

(defn.xt transport-close
  "closes the transport if a close function is present"
  {:added "4.0"}
  [transport]
  (var close (. transport ["close"]))
  (when close
    (return (close)))
  (return nil))

(defn.xt transport-make
  "creates a transport capability map from send/listen/close functions"
  {:added "4.0"}
  [send listen close]
  (var t {:send send :listen listen})
  (xt/x:set-key t "send"   send)
  (xt/x:set-key t "listen" listen)
  (when close
    (xt/x:set-key t "close" close))
  (return t))
