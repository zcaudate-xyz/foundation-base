(ns python.lib.client-fetch
  (:require [hara.lang :as l]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [python.core :as py]
             [xt.protocol.impl.client-fetch :as fetchrt]]
   :import [["urllib.request" :as urllib_request]]})

(defn.py request-body
  "encodes request bodies using json for structured values"
  {:added "4.1.3"}
  [body]
  (cond (xt/x:nil? body)
        (return nil)

        (xt/x:is-string? body)
        (return body)

        :else
        (return (xt/x:json-encode body))))

(defn.py prepare-input
  "normalises request input for python fetch execution"
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

(defn.py decode-body
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

(defn.py normalise-response
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

(defn.py native-request
  "dispatches a request through urllib"
  {:added "4.1.3"}
  [request]
  (var body-str (xt/x:get-key request "body"))
  (var data nil)
  (when (xt/x:not-nil? body-str)
    (:= data (. body-str (encode "utf-8"))))
  (var req (. urllib_request
              (Request (xt/x:get-key request "url")
                       :data data
                       :headers (or (xt/x:get-key request "headers") {})
                       :method (xt/x:get-key request "method"))))
  (try
    (var res (. urllib_request (urlopen req)))
    (var text (. (. res (read)) (decode "utf-8")))
    (return {"status" (. res (getcode))
             "headers" (. res headers)
             "body" (-/decode-body text)})
    (catch err
      (var status (:? (py/hasattr err "code")
                      (. err code)
                      nil))
      (var text nil)
      (when (py/hasattr err "read")
        (:= text (. (. err (read)) (decode "utf-8"))))
      (return {"status" status
               "body" (-/decode-body (:? (xt/x:not-nil? text)
                                         text
                                         (xt/x:to-string err)))}))))

(defn.py default-request
  "dispatches a request using raw request or request_sync behaviour"
  {:added "4.1.3"}
  [raw input opts]
  (return (-/default-request-sync raw input opts)))

(defn.py default-request-sync
  "dispatches a sync request using raw request_sync, request, or urllib"
  {:added "4.1.3"}
  [raw input opts]
  (:= raw (or raw {}))
  (var request (-/prepare-input input))
  (var request-sync-fn (xt/x:get-key raw "request_sync"))
  (when (xt/x:is-function? request-sync-fn)
    (return (-/normalise-response (request-sync-fn request opts))))
  (var request-fn (xt/x:get-key raw "request"))
  (when (xt/x:is-function? request-fn)
    (var output (request-fn request opts))
    (when (py/hasattr output "then")
      (xt/x:err "Python client fetch request_sync cannot unwrap async request"))
    (return (-/normalise-response output)))
  (return (-/native-request request)))

(defn.py client
  "wraps a python request source with the fetch client protocol"
  {:added "4.1.3"}
  [raw]
  (return (fetchrt/client-create raw {"request" -/default-request
                                      "request_sync" -/default-request-sync})))
