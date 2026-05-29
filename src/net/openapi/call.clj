(ns net.openapi.call
  (:require [net.http.api :as http.api]))

(defn path-param-names
  [path]
  (->> (re-seq #"\{([^}]+)\}" (or path ""))
       (map second)
       vec))

(defn normalize-key
  [k]
  (cond
    (keyword? k) (name k)
    (symbol? k) (name k)
    :else (str k)))

(defn normalize-map
  [m]
  (->> (or m {})
       (map (fn [[k v]] [(normalize-key k) v]))
       (into {})))

(defn normalize-path
  [entry path-input]
  (cond
    (nil? path-input)
    {}

    (map? path-input)
    (normalize-map path-input)

    (sequential? path-input)
    (zipmap (path-param-names (:path entry)) path-input)

    :else
    (throw (ex-info "Invalid :path input"
                    {:entry entry
                     :path path-input}))))

(defn append-query-string
  [url query-params]
  (http.api/append-query-string url query-params))

(defn merge-value
  [default input]
  (if (and (map? default)
           (map? input))
    (merge default input)
    (if (some? input) input default)))

(defn canonical-input
  [input]
  (let [input (or input {})]
    (cond-> input
      (and (contains? input :path-params)
           (not (contains? input :path)))
      (assoc :path (:path-params input))

      (and (contains? input :query-params)
           (not (contains? input :query))
           (not (contains? input :params)))
      (assoc :query (:query-params input))

      (and (contains? input :header-params)
           (not (contains? input :headers)))
      (assoc :headers (:header-params input))

      (and (contains? input :cookie-params)
           (not (contains? input :cookies)))
      (assoc :cookies (:cookie-params input)))))

(defn merge-inputs
  [defaults input]
  (let [defaults (canonical-input defaults)
        input    (canonical-input input)]
    (cond-> (merge defaults input)
      (or (contains? defaults :path)
          (contains? input :path))
      (assoc :path (merge-value (:path defaults)
                                (:path input)))

      (or (contains? defaults :params)
          (contains? input :params))
      (assoc :params (merge-value (:params defaults)
                                  (:params input)))

      (or (contains? defaults :query)
          (contains? input :query))
      (assoc :query (merge-value (:query defaults)
                                 (:query input)))

      (or (contains? defaults :headers)
          (contains? input :headers))
      (assoc :headers (merge-value (:headers defaults)
                                   (:headers input)))

      (or (contains? defaults :cookies)
          (contains? input :cookies))
      (assoc :cookies (merge-value (:cookies defaults)
                                   (:cookies input)))

      (or (contains? defaults :body)
          (contains? input :body))
      (assoc :body (merge-value (:body defaults)
                                (:body input))))))

(defn call
  ([entry]
   (call entry {}))
  ([entry input]
   (call entry input (:defaults entry)))
  ([entry input defaults]
   (let [input (merge-inputs defaults input)
         {:keys [base-url client path params query headers cookies body
                 accept content-type type as timeout version]}
         input
         route-params (normalize-path entry path)
         query-params (normalize-map (or query params))
         headers      (normalize-map headers)
         cookies      (normalize-map cookies)
         content-type (or content-type
                          (first (get-in entry [:body :content-types])))
         accepts      (cond-> []
                       (or accept
                           (first (:response-content-types entry)))
                       (conj (or accept
                                 (first (:response-content-types entry)))))
         req-opts     (cond-> {:route-params route-params
                              :query-params query-params}
                       (seq headers)
                       (assoc :headers headers)

                       (seq cookies)
                       (assoc :cookies cookies)

                       (contains? input :body)
                       (assoc :body body)

                       content-type
                       (assoc :content-type content-type)

                       (seq accepts)
                       (assoc :accepts accepts)

                       client
                       (assoc :client client)

                        (contains? input :type)
                        (assoc :type type)

                        (contains? input :as)
                        (assoc :as as)

                        timeout
                        (assoc :timeout timeout)

                        version
                       (assoc :version version))]
     (http.api/call-api (or base-url
                          (:base-url entry)
                          (:default-base-url entry))
                       (:path entry)
                       (:method entry)
                       req-opts))))
