(ns net.openapi.util
  (:require [std.string :as str]
            [net.http :as http]
            [net.openapi.params :as params]))

(defn call-api
  "Call an API by making HTTP request and return its response."
  {:added "4.0"}
  [path method {:keys [path-params
                       query-params
                       header-params
                       body
                       content-type
                       accepts
                       auth-names]
                :as opts}]
  (let [url (params/make-url path path-params)
        req-opts (cond-> (assoc {}
                                :url url
                                :method method
                                :content-type content-type)
                   (seq query-params) (assoc :query-params (params/normalize-params query-params))
                   (seq header-params) (assoc :headers (params/normalize-params header-params)))]
    (http/request req-opts)))
