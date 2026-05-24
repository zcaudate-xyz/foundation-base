(ns python.lib.client-fetch
  (:require [hara.lang :as l]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [python.core :as py]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetchrt]]
   :import [["urllib.request" :as urllib_request]]})

(defn.py request-body
  "encodes request bodies using json for structured values"
  {:added "4.1.3"}
  [body]
  (return (fetch-if/request-body body)))

(defn.py prepare-input
  "normalises request input for python fetch execution"
  {:added "4.1.3"}
  [input]
  (return (fetch-if/request-prepare input)))

(defn.py decode-body
  "decodes json response bodies when possible"
  {:added "4.1.3"}
  [body]
  (return (fetch-if/decode-body body)))

(defn.py normalise-response
  "decodes response body payloads when wrapped in a response map"
  {:added "4.1.3"}
  [response]
  (return (fetch-if/response-normalize response)))

(defn.py default-request
  "dispatches a request using raw request or urllib"
  {:added "4.1.3"}
  [raw input opts]
  (:= raw (or raw {}))
  (var request (-/prepare-input input))
  (var request-fn (xt/x:get-key raw "request"))
  (when (xt/x:is-function? request-fn)
    (var output (request-fn request opts))
    (return (-/normalise-response output)))
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

(defn.py client
  "wraps a python request source with the fetch client protocol"
  {:added "4.1.3"}
  [raw]
  (return (fetchrt/client-create raw {"request" -/default-request})))
