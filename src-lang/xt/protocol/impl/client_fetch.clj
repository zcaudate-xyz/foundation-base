(ns xt.protocol.impl.client-fetch
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
             [xt.protocol.client-fetch :as fetch-if]]})

(defn.xt client?
  "checks if a value is a wrapped fetch client"
  {:added "4.1.3"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "fetch.client"
                   (xt/x:get-key obj "::")))))

(defn.xt require-client
  "ensures a value is a wrapped fetch client"
  {:added "4.1.3"}
  [value]
  (when (not (-/client? value))
    (xt/x:err "Value is not a fetch client"))
  (return value))

(defn.xt client-create
  "wraps a raw transport client with the fetch protocol"
  {:added "4.1.3"}
  [raw impl]
  (:= impl (or impl {}))
  (var impl-request-fn (xt/x:get-key impl "request"))
  (var impl-query-fn (xt/x:get-key impl "query"))
  (var impl-rpc-fn (xt/x:get-key impl "rpc"))
  (var raw-request-fn (xt/x:get-key raw "request"))
  (var raw-query-fn (xt/x:get-key raw "query"))
  (var raw-rpc-fn (xt/x:get-key raw "rpc"))
  (var request-fn
       (or (:? (xt/x:is-function? impl-request-fn)
               (fn [raw input opts]
                 (return (impl-request-fn raw input opts))))
           (:? (xt/x:is-function? raw-request-fn)
               (fn [_raw input opts]
                 (return (raw-request-fn input opts))))
           (:? (xt/x:is-function? raw-query-fn)
               (fn [_raw input opts]
                 (return (raw-query-fn input opts))))
           (:? (xt/x:is-function? raw-rpc-fn)
               (fn [_raw input opts]
                 (return (raw-rpc-fn input opts))))))
  (var query-fn
       (or (:? (xt/x:is-function? impl-query-fn)
               (fn [raw input opts]
                 (return (impl-query-fn raw input opts))))
           (:? (xt/x:is-function? raw-query-fn)
               (fn [_raw input opts]
                 (return (raw-query-fn input opts))))
           request-fn))
  (var rpc-fn
       (or (:? (xt/x:is-function? impl-rpc-fn)
               (fn [raw input opts]
                 (return (impl-rpc-fn raw input opts))))
           (:? (xt/x:is-function? raw-rpc-fn)
               (fn [_raw input opts]
                 (return (raw-rpc-fn input opts))))
           request-fn))
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[fetch-if/IFetchRuntimeClient
           {"request" (fn [self input opts]
                        (var raw        (xt/x:get-key self "_raw"))
                        (var request-fn (xt/x:get-key self "__request"))
                        (when (not (xt/x:is-function? request-fn))
                          (xt/x:err "Fetch client missing request implementation"))
                        (return (request-fn raw input opts)))
            "query"   (fn [self input opts]
                        (var raw      (xt/x:get-key self "_raw"))
                        (var query-fn (xt/x:get-key self "__query"))
                        (when (not (xt/x:is-function? query-fn))
                          (xt/x:err "Fetch client missing query implementation"))
                        (return (query-fn raw input opts)))
            "rpc"     (fn [self input opts]
                        (var raw    (xt/x:get-key self "_raw"))
                        (var rpc-fn (xt/x:get-key self "__rpc"))
                        (when (not (xt/x:is-function? rpc-fn))
                          (xt/x:err "Fetch client missing rpc implementation"))
                        (return (rpc-fn raw input opts)))}]])))
  (var client {"::" "fetch.client"
               "_raw" raw
               "_impl" impl
               "__request" request-fn
               "__query" query-fn
               "__rpc" rpc-fn})
  (xt/proto:set client protocol)
  (return client))

(defn.xt request
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input opts]
  (:= client (-/require-client client))
  (var request-fn (xt/proto:method client "request"))
  (when (xt/x:nil? request-fn)
    (xt/x:err "Fetch client missing request method"))
  (return (request-fn client input opts)))

(defn.xt query
  "dispatches query through the wrapped fetch client"
  {:added "4.1.3"}
  [client input opts]
  (:= client (-/require-client client))
  (var query-fn (xt/proto:method client "query"))
  (when (xt/x:nil? query-fn)
    (xt/x:err "Fetch client missing query method"))
  (return (query-fn client input opts)))

(defn.xt rpc
  "dispatches rpc through the wrapped fetch client"
  {:added "4.1.3"}
  [client input opts]
  (:= client (-/require-client client))
  (var rpc-fn (xt/proto:method client "rpc"))
  (when (xt/x:nil? rpc-fn)
    (xt/x:err "Fetch client missing rpc method"))
  (return (rpc-fn client input opts)))
