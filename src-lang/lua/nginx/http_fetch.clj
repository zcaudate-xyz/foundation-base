(ns lua.nginx.http-fetch
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :lua.nginx
  {:import [["resty.http" :as ngxhttp]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as util]]})

(defn.lua request-http-raw
  [client input]
  (var #{raw} client)
  (:= raw (or raw {}))
  (var request (fetch/prepare-input client input))
  (var request-fn (xt/x:get-key raw "request"))
  (when (xt/x:is-function? request-fn)
    (var output (request-fn request {}))
    (return (util/response-normalize output)))
  (var httpc (ngxhttp.new))
  (var res (. httpc (request_uri (xt/x:get-key request "url")
                                 {:method (xt/x:get-key request "method")
                                  :headers (xt/x:get-key request "headers")
                                  :body (xt/x:get-key request "body")})))
  (return {"status" (. res status)
          "headers" (. res headers)
           "body" (util/decode-body (. res body))}))

(defn.lua request-http
  [client input]
  (var handler (fetch/prepare-middleware client -/request-http-raw))
  (return (handler client input)))

(defimpl.xt ^{:lang :lua}
  LuaNginxHttpFetchClient
  [defaults middleware]

  fetch/IHttpClient
  {fetch/request-http -/request-http})

(defn.lua create
  [defaults middleware]
  (return
   (-/LuaNginxHttpFetchClient (or defaults {}) (or middleware
                                                   [fetch/wrap-prepare-input]))))
