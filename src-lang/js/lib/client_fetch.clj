(ns js.lib.client-fetch
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetchrt]]})

(defn.js request-body
  "encodes request bodies using json for structured values"
  {:added "4.1.3"}
  [body]
  (cond (xt/x:nil? body)
        (return nil)

        (xt/x:is-string? body)
        (return body)

        :else
        (return (xt/x:json-encode body))))

(defn.js prepare-input
  "normalises request input for js fetch execution"
  {:added "4.1.3"}
  [input]
  (var request (xt/x:obj-clone (or input {})))
  (when (xt/x:nil? (xt/x:get-key request "method"))
    (xt/x:set-key request "method" "GET"))
  (when (xt/x:nil? (xt/x:get-key request "headers"))
    (xt/x:set-key request "headers" {}))
  (when (xt/x:not-nil? (xt/x:get-key request "body"))
    (xt/x:set-key request
                  "body"
                  (-/request-body (xt/x:get-key request "body"))))
  (return request))

(defn.js decode-body
  "decodes json response bodies when possible"
  {:added "4.1.3"}
  [body]
  (cond (not (xt/x:is-string? body))
        (return body)

        (== "" body)
        (return nil)

        :else
        (try
          (return (xt/x:json-decode body))
          (catch err
            (return body)))))

(defn.js normalise-response
  "decodes response body payloads when wrapped in a response map"
  {:added "4.1.3"}
  [response]
  (if (and (xt/x:is-object? response)
           (xt/x:has-key? response "body"))
    (do (var out (xt/x:obj-clone response))
        (xt/x:set-key out
                      "body"
                      (-/decode-body (xt/x:get-key out "body")))
        (return out))
    (return response)))

(defn.js default-request
  "dispatches a request using a raw request fn or the host fetch api"
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
  (when (not (xt/x:is-function? fetch-fn))
    (xt/x:err "JS client fetch missing request implementation"))
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
                                           "body" (-/decode-body text)}))))))))))

(defn.js client
  "wraps a js request source with the fetch client protocol"
  {:added "4.1.3"}
  [raw]
  (var source (:? (xt/x:is-function? raw)
                  {"request" raw}
                  (xt/x:obj-clone (or raw {}))))
  (var request-sync-fn (xt/x:get-key source "request_sync"))
  (when (and (xt/x:nil? (xt/x:get-key source "request"))
             (xt/x:is-function? request-sync-fn))
    (xt/x:set-key source
                  "request"
                  (fn [input opts]
                    (return (request-sync-fn input opts)))))
  (xt/x:del-key source "request_sync")
  (return (fetchrt/client-create source {"request" -/default-request})))
