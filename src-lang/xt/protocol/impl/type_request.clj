(ns xt.protocol.impl.type-request
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.type-request :as req-if]]})

(defn.xt request-runtime?
  "checks if a value is a wrapped request runtime"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "type.request"
                   (xt/x:get-key obj "::")))))

(defn.xt require-request-runtime
  "ensures a value is a wrapped request runtime"
  {:added "4.1"}
  [value]
  (when (not (-/request-runtime? value))
    (xt/x:err "Value is not a runtime request type"))
  (return value))

(defn.xt request-runtime-create
  "wraps an implementation map with the request protocol"
  {:added "4.1"}
  [impl]
  (return {"::" "type.request"
           "_impl" impl}))

(defn.xt request
  "dispatches through the request protocol"
  {:added "4.1"}
  [runtime node space action args meta]
  (:= runtime (-/require-request-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "request")
           node space action args meta)))

(defn.xt receive-request
  "dispatches receive_request through the request protocol"
  {:added "4.1"}
  [runtime node frame ctx]
  (:= runtime (-/require-request-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "receive_request")
           node frame ctx)))

(defn.xt receive-response
  "dispatches receive_response through the request protocol"
  {:added "4.1"}
  [runtime node frame]
  (:= runtime (-/require-request-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "receive_response")
           node frame)))

(defn.xt respond-ok
  "dispatches respond_ok through the request protocol"
  {:added "4.1"}
  [runtime node request data meta ctx]
  (:= runtime (-/require-request-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "respond_ok")
           node request data meta ctx)))

(defn.xt respond-error
  "dispatches respond_error through the request protocol"
  {:added "4.1"}
  [runtime node request error meta ctx]
  (:= runtime (-/require-request-runtime runtime))
  (var impl (xt/x:get-key runtime "_impl"))
  (return ((xt/x:get-key impl "respond_error")
           node request error meta ctx)))
