(ns net.http.api
  (:require [clojure.string :as string]
            [net.http.client :as client]
            [net.openapi.params :as params]
            [std.json :as json]
            [std.lib.foundation :as f]))

(defn cookie-header
  [cookie-params]
  (when (seq cookie-params)
    (->> (params/normalize-params cookie-params)
         (map (fn [[k v]]
                (str (f/strn k) "=" v)))
         (string/join "; "))))

(defn json-content-type?
  [content-type]
  (and (string? content-type)
       (re-find #"(?i)\bjson\b" content-type)))

(defn encode-body
  [body content-type]
  (cond
    (nil? body) nil
    (string? body) body
    (json-content-type? content-type) (json/write body)
    :else body))

(defn call-api
  "Call an API by making an HTTP request and return its response."
  {:added "4.1.4"}
  ([path method opts]
   (call-api nil path method opts))
  ([base-url path method {:keys [path-params
                                 query-params
                                 header-params
                                 cookie-params
                                 body
                                 content-type
                                 accepts]
                          :as _opts}]
   (let [uri (params/make-url base-url path (or path-params {}))
         headers (cond-> (or (params/normalize-params header-params) {})
                   (seq cookie-params) (assoc "Cookie" (cookie-header cookie-params))
                   (seq accepts) (assoc "Accept" (string/join ", " accepts))
                   content-type (assoc "Content-Type" content-type))
         req-opts (cond-> {:uri uri
                           :method method}
                    (seq query-params) (assoc :query-params (params/normalize-params query-params))
                    (seq headers) (assoc :headers headers)
                    (some? body) (assoc :body (encode-body body content-type)))]
     (client/request req-opts))))
