(ns net.openapi.scaffold
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [net.openapi.read :as read]
            [std.json :as json]
            [std.string.case :as case]))

(def +descriptor_keys+
  [:operation-id
   :fn-name
   :method
   :path
   :summary
   :description
   :tags
   :path-params
   :query-params
   :header-params
   :cookie-params
   :body
   :response-content-types
   :auth-names])

(declare read-schema spec-title spec-version spec-base-url)

(defn operation-form
  "Creates a `defn` scaffold form for one normalized operation."
  {:added "4.1.4"}
  [{:keys [fn-name
          method
          path
          summary
          description
          path-params
          body
          response-content-types
          auth-names
           default-base-url]}]
  (let [fn-sym (symbol fn-name)
        path-bindings (mapv (fn [{:keys [name]}]
                             [name (symbol (case/spear-case name))])
                           path-params)
        path-args (mapv second path-bindings)
        body-required? (boolean (:required body))
        body-arg (when body-required? 'body)
        base-args (vec (concat ['client] path-args (when body-arg [body-arg])))
        opts-args (conj base-args 'opts)
        doc (or summary description (str (name method) " " path))
        defaults (cond-> {}
                   (seq path-bindings)
                   (assoc :path-params (into {}
                                            (map (fn [[name sym]]
                                                   [name sym]))
                                            path-bindings))

                   body-required?
                   (assoc :body body-arg)

                   (seq (or (:content-types body) []))
                   (assoc :content-type (first (:content-types body)))

                   (seq response-content-types)
                   (assoc :accepts response-content-types)

                   (seq auth-names)
                   (assoc :auth-names auth-names))]
    `(defn ~fn-sym
       ~doc
       ([~@base-args]
        (~fn-sym ~@base-args {}))
       ([~@opts-args]
        (let [client# (or client {})
              opts# (or opts {})
              base-url# (or (:base-url client#)
                            (:base-url opts#)
                            ~default-base-url)]
          (net.openapi.util/call-api
           base-url#
           ~path
           ~method
           (merge ~defaults opts#)))))))

(defn scaffold-data
  "Builds generator metadata from an OpenAPI/Swagger source."
  {:added "4.1.4"}
  [source]
  (let [spec (read-schema source)]
    {:title (spec-title spec)
     :version (spec-version spec)
     :base-url (spec-base-url spec)
     :operations (->> (read/read spec) vals vec)}))

(defn scaffold
  "Generates a `do` form containing `net.http` wrapper scaffolding for an OpenAPI spec."
  {:added "4.1.4"}
  ([source]
   (scaffold source {}))
  ([source {:keys [ns-sym]
            :or {ns-sym 'generated.openapi}}]
   (let [{:keys [title version base-url operations]} (scaffold-data source)
         operations (mapv #(assoc % :default-base-url base-url) operations)]
     `(do
        (ns ~ns-sym)
        (def ~'+openapi-meta+
          {:title ~title
           :version ~version
           :base-url ~base-url})
        (def ~'+openapi-operations+
          ~(mapv (fn [operation]
                   (select-keys operation +descriptor_keys+))
                 operations))
        ~@(map operation-form operations)))))

(defn scaffold-string
  "Pretty prints the generated scaffold form."
  {:added "4.1.4"}
  ([source]
   (scaffold-string source {}))
  ([source opts]
   (with-out-str
     (pprint/pprint (scaffold source opts)))))
(defn ^:private slurp-json-source
  [source]
  (cond
    (map? source)
    source

    (instance? java.io.File source)
    (slurp source)

    (instance? java.net.URL source)
    (slurp source)

    (string? source)
    (let [trimmed (string/triml source)
          file (io/file source)]
      (cond
        (or (string/starts-with? trimmed "{")
            (string/starts-with? trimmed "["))
        source

        (re-find #"^https?://" source)
        (slurp source)

        (.exists file)
        (slurp file)

        :else
        (if-let [resource (io/resource source)]
          (slurp resource)
          (throw (ex-info "JSON OpenAPI source not found"
                          {:source source})))))

    :else
    (throw (ex-info "Unsupported JSON OpenAPI source"
                    {:source source}))))

(defn ^:private read-schema
  [source]
  (if (map? source)
    source
    (json/read (slurp-json-source source))))

(defn ^:private data-get
  [m k]
  (or (get m k)
      (when (keyword? k) (get m (name k)))
      (when (string? k) (get m (keyword k)))))

(defn ^:private data-get-in
  [m ks]
  (reduce (fn [acc k]
            (cond
              (map? acc) (data-get acc k)
              (and (vector? acc) (integer? k) (<= 0 k) (< k (count acc))) (nth acc k)
              :else nil))
          m
          ks))

(defn ^:private spec-title
  [spec]
  (data-get-in spec ["info" "title"]))

(defn ^:private spec-version
  [spec]
  (or (data-get-in spec ["info" "version"])
      (data-get spec "openapi")
      (data-get spec "swagger")))

(defn ^:private spec-base-url
  [spec]
  (if-let [url (data-get-in spec ["servers" 0 "url"])]
    url
    (when-let [host (data-get spec "host")]
      (let [scheme (or (data-get-in spec ["schemes" 0]) "http")
            base-path (or (data-get spec "basePath") "")]
        (str scheme "://" host base-path)))))
