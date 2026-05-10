(ns dart.lib.client-fetch
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetchrt]]})

(defn.dt request-body
  "encodes request bodies using json for structured values"
  {:added "4.1.3"}
  [body]
  (cond (xt/x:nil? body)
        (return nil)

        (xt/x:is-string? body)
        (return body)

        :else
        (return (xt/x:json-encode body))))

(defn.dt prepare-input
  "normalises request input for dart fetch execution"
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

(defn.dt decode-body
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

(defn.dt normalise-response
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

(defn.dt default-request
  "dispatches a request using raw request or request_sync behaviour"
  {:added "4.1.3"}
  [raw input opts]
  (return (-/default-request-sync raw input opts)))

(defn.dt default-request-sync
  "dispatches a sync request using raw request_sync or a non-promise request fn"
  {:added "4.1.3"}
  [raw input opts]
  (:= raw (or raw {}))
  (var request (-/prepare-input input))
  (var request-sync-fn (xt/x:get-key raw "request_sync"))
  (when (xt/x:is-function? request-sync-fn)
    (return (-/normalise-response
             (request-sync-fn request opts))))
  (var request-fn (xt/x:get-key raw "request"))
  (when (not (xt/x:is-function? request-fn))
    (xt/x:err "Dart client fetch missing request_sync implementation"))
  (var output (request-fn request opts))
  (if (and (xt/x:is-object? output)
            (xt/x:is-function? (xt/x:get-key output "then")))
    (xt/x:err "Dart client fetch request_sync cannot unwrap async request")
    (return (-/normalise-response output))))

(defn.dt client
  "wraps a dart request source with the fetch client protocol"
  {:added "4.1.3"}
  [raw]
  (return (fetchrt/client-create raw {"request" -/default-request
                                      "request_sync" -/default-request-sync})))
