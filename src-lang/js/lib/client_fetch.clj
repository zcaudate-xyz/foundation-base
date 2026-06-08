(ns js.lib.client-fetch
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetchrt]]})

(defn.js request-body
  "encodes request bodies using json for structured values"
  {:added "4.1.3"}
  [body]
  (return (fetch-if/request-body body)))

(defn.js prepare-input
  "normalises request input for js fetch execution"
  {:added "4.1.3"}
  [input]
  (return (fetch-if/request-prepare input)))

(defn.js decode-body
  "decodes json response bodies when possible"
  {:added "4.1.3"}
  [body]
  (return (fetch-if/decode-body body)))

(defn.js normalise-response
  "decodes response body payloads when wrapped in a response map"
  {:added "4.1.3"}
  [response]
  (return (fetch-if/response-normalize response)))

(defn.js default-request
  "dispatches a request using a raw request fn, fetch api, or curl fallback"
  {:added "4.1.3"}
  [raw input opts]
  (:= raw (or raw {}))
  (var request (-/prepare-input input))

  (var request-fn (xt/x:get-key raw "request"))
  (when (xt/x:is-function? request-fn)
    (var output (request-fn request opts))
    (if (and (xt/x:is-object? output)
             (xt/x:is-function? (xt/x:get-key output "then")))
      (return (. output
                 (then (fn [result]
                         (return (-/normalise-response result))))))
      (return (-/normalise-response output))))

  (var fetch-fn (or (xt/x:get-key raw "fetch")
                    fetch))
  (var params {"method" (xt/x:get-key request "method")
               "headers" (or (xt/x:get-key request "headers") {})})
  (when (xt/x:not-nil? (xt/x:get-key request "body"))
    (xt/x:set-key params "body" (xt/x:get-key request "body")))
  (return (. (fetch-fn (xt/x:get-key request "url") params)
             (then (fn [res]
                     (return
                      (. (. res (text))
                         (then (fn [text]
                                 (return {"status" (. res ["status"])
                                          "headers" (. res ["headers"])
                                          "body" text}))))))))))

(defn.js client
  "wraps a js request source with the fetch client protocol"
  {:added "4.1.3"}
  [raw]
  (var source (:? (xt/x:is-function? raw)
                  {"request" raw}
                  (xt/x:obj-clone (or raw {}))))
  (return (fetchrt/client-create source {"request" -/default-request})))
