(ns xt.protocol.impl.type-request
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
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
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[req-if/ITypeRuntimeRequest
           {"request"          (fn [self node space action args meta]
                                 (var impl (xt/x:get-key self "_impl"))
                                 (return ((xt/x:get-key impl "request")
                                          node space action args meta)))
            "receive_request"  (fn [self node frame ctx]
                                 (var impl (xt/x:get-key self "_impl"))
                                 (return ((xt/x:get-key impl "receive_request")
                                          node frame ctx)))
            "receive_response" (fn [self node frame]
                                 (var impl (xt/x:get-key self "_impl"))
                                 (return ((xt/x:get-key impl "receive_response")
                                          node frame)))
            "respond_ok"       (fn [self node request data meta ctx]
                                 (var impl (xt/x:get-key self "_impl"))
                                 (return ((xt/x:get-key impl "respond_ok")
                                          node request data meta ctx)))
            "respond_error"    (fn [self node request error meta ctx]
                                 (var impl (xt/x:get-key self "_impl"))
                                 (return ((xt/x:get-key impl "respond_error")
                                          node request error meta ctx)))}]])))
  (var runtime {"::" "type.request"
                "_impl" impl})
  (xt/proto:set runtime protocol)
  (return runtime))

(defn.xt request
  "dispatches through the request protocol"
  {:added "4.1"}
  [runtime node space action args meta]
  (:= runtime (-/require-request-runtime runtime))
  (return ((xt/proto:method runtime "request")
           runtime node space action args meta)))

(defn.xt receive-request
  "dispatches receive_request through the request protocol"
  {:added "4.1"}
  [runtime node frame ctx]
  (:= runtime (-/require-request-runtime runtime))
  (return ((xt/proto:method runtime "receive_request")
           runtime node frame ctx)))

(defn.xt receive-response
  "dispatches receive_response through the request protocol"
  {:added "4.1"}
  [runtime node frame]
  (:= runtime (-/require-request-runtime runtime))
  (return ((xt/proto:method runtime "receive_response")
           runtime node frame)))

(defn.xt respond-ok
  "dispatches respond_ok through the request protocol"
  {:added "4.1"}
  [runtime node request data meta ctx]
  (:= runtime (-/require-request-runtime runtime))
  (return ((xt/proto:method runtime "respond_ok")
           runtime node request data meta ctx)))

(defn.xt respond-error
  "dispatches respond_error through the request protocol"
  {:added "4.1"}
  [runtime node request error meta ctx]
  (:= runtime (-/require-request-runtime runtime))
  (return ((xt/proto:method runtime "respond_error")
           runtime node request error meta ctx)))
