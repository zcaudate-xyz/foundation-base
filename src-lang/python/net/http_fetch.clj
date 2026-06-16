(ns python.net.http-fetch
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [python.core :as py]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as util]]
   :import [["urllib.request" :as urllib_request]]})

(defn.py request-http-raw
  [client input]
  (var #{raw} client)
  (:= raw (or raw {}))
  (var request (fetch/prepare-input client input))
  (var request-fn (xt/x:get-key raw "request"))
  (when (xt/x:is-function? request-fn)
    (var output (request-fn request {}))
    (return (util/response-normalize output)))
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
             "body" (util/decode-body text)})
    (catch err
      (var status (:? (py/hasattr err "code")
                      (. err code)
                      nil))
      (var text nil)
      (when (py/hasattr err "read")
        (:= text (. (. err (read)) (decode "utf-8"))))
      (return {"status" status
               "body" (util/decode-body (:? (xt/x:not-nil? text)
                                             text
                                             (xt/x:to-string err)))}))))

(defn.py request-http
  [client input]
  (var handler (fetch/prepare-middleware client -/request-http-raw))
  (return (handler client input)))

(defimpl.xt ^{:lang :python}
  PythonHttpFetchClient
  [defaults middleware]
  fetch/IHttpClient
  {fetch/request-http -/request-http})

(defn.py create
  [defaults middleware]
  (return
   (-/PythonHttpFetchClient (or defaults {}) (or middleware
                                                 [fetch/wrap-prepare-input]))))
