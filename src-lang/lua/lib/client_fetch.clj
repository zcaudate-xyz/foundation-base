(ns lua.lib.client-fetch
  (:require [hara.lang :as l]))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetchrt]]})

(defn.lua request-body
  "encodes request bodies using json for structured values"
  {:added "4.1.3"}
  [body]
  (return (fetch-if/request-body body)))

(defn.lua prepare-input
  "normalises request input for lua fetch execution"
  {:added "4.1.3"}
  [input]
  (return (fetch-if/request-prepare input)))

(defn.lua decode-body
  "decodes json response bodies when possible"
  {:added "4.1.3"}
  [body]
  (return (fetch-if/decode-body body)))

(defn.lua normalise-response
  "decodes response body payloads when wrapped in a response map"
  {:added "4.1.3"}
  [response]
  (return (fetch-if/response-normalize response)))

(defn.lua default-request
  "dispatches a request using a raw request fn"
  {:added "4.1.3"}
  [raw input opts]
  (:= raw (or raw {}))
  (var request (-/prepare-input input))
  (var request-fn (xt/x:get-key raw "request"))
  (when (xt/x:is-function? request-fn)
    (var output (request-fn request opts))
    (return (-/normalise-response output)))
  (xt/x:err "Lua client fetch missing request implementation"))

(defn.lua client
  "wraps a lua request source with the fetch client protocol"
  {:added "4.1.3"}
  [raw]
  (return (fetchrt/client-create raw {"request" -/default-request})))
