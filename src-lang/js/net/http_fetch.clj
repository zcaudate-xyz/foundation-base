(ns js.net.http-fetch
  (:require [lang.core :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-fetch :as http-fetch]]})

(defn.js request-http-raw
  [client input]
  (var #{url
         method
         headers
         body} input)
  (return (. (fetch url {"method" method
                         "headers" headers
                         "body" body})
             (then (fn [res]
                     (return (. res
                                (text)
                                (then (fn [text]
                                        (return {"status" (. res ["status"])
                                                 "headers" (. res ["headers"])
                                                 "body" text}))))))))))

(defn.js request-http
  [client input]
  (var handler  (http-fetch/prepare-middleware client -/request-http-raw))
  (return
   (handler client input)))

(defimpl.xt ^{:lang :js}
  HttpFetchClient
  [defaults middleware]
  http-fetch/IHttpClient
  {http-fetch/request-http -/request-http})

(defn.js create
  [defaults middleware]
  (return
   (-/HttpFetchClient defaults (or middleware
                                   [http-fetch/wrap-prepare-input]))))

(comment
  (:id @http-fetch/IHttpClient)
  (:module @http-fetch/IHttpClient)
  )
