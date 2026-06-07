(ns js.net.http-fetch
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-fetch :as http]
             [xt.net.http-util :as util]]})

(defn.js request-http
  [request opts]
  (var #{url
         method
         headers
         body} request)
  (return (. (fetch url {"url" url
                         "method" method
                         "headers" headers
                         "body" body})
             (then (fn [res]
                     (return (. res
                                (text)
                                (then (fn [text]
                                        (return {"status" (. res ["status"])
                                                 "headers" (. res ["headers"])
                                                 "body" text}))))))))))

(defn.js client-methods
  []
  (return
   {"request_http"
    (fn [client input opts]
      (-/request-http request opts))}))

(defn.js client-fetch
  []
  (return
   (fetch/client-fetch (-/client-methods))))
