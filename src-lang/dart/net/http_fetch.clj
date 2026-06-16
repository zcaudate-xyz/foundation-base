(ns dart.net.http-fetch
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as util]]
   :import [["dart:convert" :as convert]
            ["dart:io" :as io]]})

(defn.dt request-http-raw
  [client input]
  (var #{raw} client)
  (:= raw (or raw {}))
  (var request (fetch/prepare-input client input))
  (var request-fn (xt/x:get-key raw "request"))
  (if (xt/x:is-function? request-fn)
    (do (var output (request-fn request {}))
        (return (util/response-normalize output)))
    (do (var http-client (io.HttpClient))
        (var uri (. Uri (parse (xt/x:get-key request "url"))))
        (return
         (. (. http-client (openUrl (xt/x:get-key request "method") uri))
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
                                     "headers" {}
                                     "body" (util/decode-body body)}))))))))))))))))

(defn.dt request-http
  [client input]
  (var handler (fetch/prepare-middleware client -/request-http-raw))
  (return (handler client input)))

(defimpl.xt ^{:lang :dart}
  DartHttpFetchClient
  [defaults middleware]
  fetch/IHttpClient
  {fetch/request-http -/request-http})

(defn.dt create
  [defaults middleware]
  (return
   (-/DartHttpFetchClient (or defaults {}) (or middleware
                                               [fetch/wrap-prepare-input]))))
