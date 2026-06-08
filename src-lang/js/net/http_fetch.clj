(ns js.net.http-fetch
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]]})

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
  (var prepped  (fetch/prepare-input client input opts))
  (return
   (-/request-http-raw prepped)))

(defn.js create-methods
  []
  (return
   {"request_http" -/request-http-client}))

(defn.js create
  [defaults]
  (return
   (fetch/create-base nil
                      (-/create-methods)
                      defaults)))
