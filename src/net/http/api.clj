(ns net.http.api
  (:require [clojure.string :as string]
            [net.http.client :as client]
            [std.json :as json]
            [std.lib.foundation :as f])
  (:import java.net.URLEncoder))

(defn normalize-key
  [k]
  (cond
    (keyword? k) (name k)
    (symbol? k) (name k)
    :else (f/strn k)))

(defn param->str
  [param]
  (cond
    (keyword? param) (name param)
    (symbol? param) (name param)
    (sequential? param) (string/join "," (map param->str param))
    :else (f/strn param)))

(defn normalize-param
  [param]
  (if (sequential? param)
    (string/join "," (map param->str param))
    (param->str param)))

(defn normalize-params
  [params]
  (->> params
       (remove (comp nil? second))
       (map (fn [[k v]] [k (normalize-param v)]))
       (into {})))

(defn cookie-header
  [cookie-params]
  (when (seq cookie-params)
    (->> (normalize-params cookie-params)
         (map (fn [[k v]]
                (str (f/strn k) "=" v)))
         (string/join "; "))))

(defn json-content-type?
  [content-type]
  (and (string? content-type)
       (re-find #"(?i)\bjson\b" content-type)))

(defn form-content-type?
  [content-type]
  (and (string? content-type)
       (re-find #"(?i)application/x-www-form-urlencoded" content-type)))

(defn url-encode
  [s]
  (.replace (URLEncoder/encode (f/strn s) "UTF-8") "+" "%20"))

(defn make-url
  [base-url path path-params]
  (let [path (reduce (fn [p [k v]]
                       (string/replace p
                                       (re-pattern (str "\\{" (java.util.regex.Pattern/quote (normalize-key k)) "\\}"))
                                       (url-encode (normalize-param v))))
                     path
                     path-params)]
    (str (or base-url "") path)))

(defn encode-query-params
  [query-params]
  (->> query-params
       (keep (fn [[k v]]
               (cond
                 (nil? v)
                 nil

                 (vector? v)
                 (->> (map-indexed
                       (fn [i x]
                         (str (url-encode k)
                              "[" (+ i 1) "]="
                              (url-encode x)))
                       v)
                      (interpose "&")
                      (apply str))

                 :else
                 (str (url-encode k) "=" (url-encode v)))))
       (interpose "&")
       (apply str)))

(defn append-query-string
  [uri query-params]
  (let [query-string (-> query-params
                         normalize-params
                         encode-query-params)]
    (if (string/blank? query-string)
      uri
      (str uri
           (if (string/includes? uri "?") "&" "?")
           query-string))))

(defn map-lookup
  [m k]
  (or (get m k)
      (get m (keyword k))
      (get m (string/lower-case (f/strn k)))
      (get m (string/upper-case (f/strn k)))))

(defn normalize-cookie-params
  [cookie-params]
  (->> (or cookie-params {})
       (map (fn [[k v]]
              [k (if (map? v)
                   (or (map-lookup v "value") v)
                   v)]))
       (into {})))

(defn canonical-opts
  [{:keys [route-params
           path-params
           query-params
           header-params
           headers
           cookie-params
           cookies
           body
           body-params
           form-params
           content-type
           accepts]
    :as opts}]
  (let [header-params (or header-params headers)
        body         (cond
                       (contains? opts :body) body
                       (contains? opts :body-params) body-params
                       (contains? opts :form-params) form-params
                       :else nil)
        content-type (or content-type
                         (map-lookup header-params "Content-Type")
                         (when (contains? opts :form-params)
                           "application/x-www-form-urlencoded"))
        accepts      (or accepts
                         (some-> (map-lookup header-params "Accept")
                                 vector))]
    {:path-params   (or path-params route-params {})
     :query-params  (or query-params {})
     :header-params (or header-params {})
     :cookie-params (normalize-cookie-params (or cookie-params cookies))
     :body          body
     :content-type  content-type
     :accepts       accepts
     :passthrough   (apply dissoc opts
                           [:route-params
                            :path-params
                            :query-params
                            :header-params
                            :headers
                            :cookie-params
                            :cookies
                            :body
                            :body-params
                            :form-params
                            :content-type
                            :accepts])}))

(defn encode-body
  [body content-type]
  (cond
    (nil? body) nil
    (string? body) body
    (json-content-type? content-type) (json/write body)
    (form-content-type? content-type) (-> body
                                          normalize-params
                                          encode-query-params)
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
                          :as opts}]
   (let [{:keys [path-params
                 query-params
                 header-params
                 cookie-params
                 body
                 content-type
                 accepts
                 passthrough]}
         (canonical-opts opts)
         uri (-> (make-url base-url path (or path-params {}))
                 (append-query-string query-params))
         headers (cond-> (or (normalize-params header-params) {})
                   (seq cookie-params) (assoc "Cookie" (cookie-header cookie-params))
                   (seq accepts) (assoc "Accept" (string/join ", " accepts))
                   content-type (assoc "Content-Type" content-type))
         req-opts (cond-> (merge passthrough
                                {:uri uri
                                 :method method})
                    (seq headers) (assoc :headers headers)
                    (some? body) (assoc :body (encode-body body content-type)))]
     (client/request req-opts))))
