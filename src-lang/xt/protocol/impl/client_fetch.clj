(ns xt.protocol.impl.client-fetch
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
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

(defn.xt ensure-promise
  "wraps sync values in a native host promise while passing promises through"
  {:added "4.1.3"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise-run value))))

(defn.xt client-source
  "normalises a fetch client source into a raw object"
  {:added "4.1.3"}
  [value]
  (cond (-/client? value)
        (return (xt/x:get-key value "_raw"))

        (xt/x:is-function? value)
        (return {"request" value})

        (xt/x:is-object? value)
        (return value)

        (xt/x:nil? value)
        (return {})

        :else
        (xt/x:err "Unsupported fetch client source")))

(defn.xt client-impl
  "normalises a fetch client implementation map"
  {:added "4.1.3"}
  [value]
  (cond (xt/x:is-function? value)
        (return {"request" value})

        (xt/x:is-object? value)
        (return value)

        (xt/x:nil? value)
        (return {})

        :else
        (xt/x:err "Unsupported fetch client implementation")))

(defn.xt client-create
  "wraps a raw transport client with the fetch protocol"
  {:added "4.1.3"}
  [raw impl]
  (when (-/client? raw)
    (return raw))
  (:= raw  (-/client-source raw))
  (:= impl (-/client-impl impl))
  (var impl-request-fn (xt/x:get-key impl "request"))
  (var raw-request-fn (xt/x:get-key raw "request"))
  (var request-fn
       (or (:? (xt/x:is-function? impl-request-fn)
              (fn [raw input opts]
                  (return (impl-request-fn raw input opts))))
             (:? (xt/x:is-function? raw-request-fn)
                 (fn [_raw input opts]
                   (return (raw-request-fn input opts))))))
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[fetch-if/IFetchRuntimeClient
           {"request" (fn [self input opts]
                        (var raw (xt/x:get-key self "_raw"))
                        (var request-fn (xt/x:get-key self "__request"))
                        (var request (fetch-if/request-prepare input))
                        (when (not (xt/x:is-function? request-fn))
                          (xt/x:err "Fetch client missing request implementation"))
                        (var output (request-fn raw request opts))
                        (return
                         (promise/x:promise-then
                          (-/ensure-promise output)
                          (fn [result]
                            (return (fetch-if/response-normalize result))))))}]])))
  (var client {"::" "fetch.client"
               "_raw" raw
               "_impl" impl
               "__request" request-fn})
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
  (return (-/ensure-promise
           (request-fn client input opts))))
