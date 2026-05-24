(ns dart.lib.client-fetch
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetchrt]]
   :import [["dart:convert" :as convert]
            ["dart:io" :as io]]})

(defn.dt request-body
  "encodes request bodies using json for structured values"
  {:added "4.1.3"}
  [body]
  (return (fetch-if/request-body body)))

(defn.dt prepare-input
  "normalises request input for dart fetch execution"
  {:added "4.1.3"}
  [input]
  (return (fetch-if/request-prepare input)))

(defn.dt decode-body
  "decodes json response bodies when possible"
  {:added "4.1.3"}
  [body]
  (return (fetch-if/decode-body body)))

(defn.dt normalise-response
  "decodes response body payloads when wrapped in a response map"
  {:added "4.1.3"}
  [response]
  (return (fetch-if/response-normalize response)))

(defn.dt default-request
  "dispatches a request using raw request or dart HttpClient"
  {:added "4.1.3"}
  [raw input opts]
  (:= raw (or raw {}))
  (var request (-/prepare-input input))
  (var request-fn (xt/x:get-key raw "request"))
  (if (xt/x:is-function? request-fn)
    (do (var output (request-fn request opts))
        (return (-/normalise-response output)))
    (do (var client (io.HttpClient))
        (var uri (. Uri (parse (xt/x:get-key request "url"))))
        (return
         (. (. client (openUrl (xt/x:get-key request "method") uri))
            (then
             (fn [req]
              (xt/for:object [[k v] (xt/x:get-key request "headers")]
                (. (. req headers) (set k v)))
              (when (xt/x:not-nil? (xt/x:get-key request "body"))
                (. req (write (xt/x:get-key request "body"))))
              (var response-future (. req (close)))
              (return
               (. response-future
                  (then
                   (fn [res]
                     (var body-future
                          (. (. res (transform (. (. convert utf8) decoder)))
                             (join)))
                     (return
                      (. body-future
                         (then
                          (fn [body]
                            (return {"status" (. res statusCode)
                                     "body" body}))))))))))))))))

(defn.dt client
  "wraps a dart request source with the fetch client protocol"
  {:added "4.1.3"}
  [raw]
  (return (fetchrt/client-create raw {"request" -/default-request})))
