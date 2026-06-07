(ns js.net.http-fetch
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as http]]})

(defn.js request-http-raw
  [input]
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

(defn.js request-http-client
  [client input opts]
  (var prepped    (http/prepare-input client input opts))
  (return
   (-/request-http-raw prepped)))

(defn.js create-methods
  []
  (return
   {"request_http" -/request-http-client}))

(defn.js create
  [defaults]
  (return
   (xt/x:obj-assign
    (http/create-base "js.net.http-fetch"
                      (-/create-methods))
    {"defaults" (or defaults {})})))
