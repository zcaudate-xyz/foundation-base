(ns net.openapi.generate
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [net.openapi.read :as read]
            [std.json :as json]
            [std.string.case :as case]))

(def +function_keys+
  [:operation-id
   :fn-name
   :method
   :path
   :summary
   :description
   :tags])

(def +optional_keys+
  [:query-params
   :header-params
   :cookie-params
   :auth-names])

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

(defn read-schema
  "Reads an OpenAPI/Swagger schema from a JSON map, file path, URL, or raw JSON string."
  {:added "4.1.4"}
  [source]
  (if (map? source)
    source
    (json/read (slurp-json-source source))))

(defn schema-operations
  "Returns normalized operations for a schema source."
  {:added "4.1.4"}
  [source]
  (->> (read/read source)
       vals
       vec))

(defn api-function-name
  "Returns the generated function name for a normalized operation."
  {:added "4.1.4"}
  [operation]
  (:fn-name operation))

(defn path-arg-names
  "Returns path parameter names in path order."
  {:added "4.1.4"}
  [operation]
  (let [path (:path operation)
        names (map second (re-seq #"\{([^}]+)\}" (or path "")))
        known (into {}
                    (map (juxt :name identity))
                    (:path-params operation))]
    (mapv (fn [name]
            (or (get known name)
                {:name name
                 :required true
                 :description nil}))
          names)))

(defn operation-args
  "Returns a declarative argument specification for a normalized operation."
  {:added "4.1.4"}
  [operation]
  (let [body (:body operation)
        path-args (mapv (fn [{:keys [name required description] :as parameter}]
                         {:name name
                          :symbol (symbol (case/spear-case name))
                          :kind :path
                          :required (boolean required)
                          :description description
                          :parameter parameter})
                        (path-arg-names operation))
        body-arg (when body
                   [{:name "body"
                     :symbol 'body
                     :kind :body
                     :required (boolean (:required body))
                     :description nil
                     :parameter body}])
        opts-arg [{:name "opts"
                   :symbol 'opts
                   :kind :opts
                   :required false
                   :description nil
                   :parameter (select-keys operation +optional_keys+)}]]
    (vec (concat [{:name "client"
                   :symbol 'client
                   :kind :client
                   :required true
                   :description nil
                   :parameter nil}]
                 path-args
                 body-arg
                 opts-arg))))

(defn operation-arglist
  "Returns the canonical ordered argument list for a normalized operation."
  {:added "4.1.4"}
  [operation]
  (->> (operation-args operation)
       (mapv :symbol)))

(defn operation-call-map
  "Returns the call-shape metadata derived from a normalized operation."
  {:added "4.1.4"}
  [operation]
  (let [args (operation-args operation)
        path-params (->> args
                         (filter #(= :path (:kind %)))
                         (map (fn [{:keys [name symbol]}]
                                [name symbol]))
                         (into {}))
        body-arg (some #(when (= :body (:kind %)) (:symbol %)) args)
        opts-arg (some #(when (= :opts (:kind %)) (:symbol %)) args)]
    (cond-> {:arglist (operation-arglist operation)}
      (seq path-params) (assoc :path-params path-params)
      body-arg (assoc :body body-arg)
      opts-arg (assoc :opts opts-arg)
      (seq (:query-params operation)) (assoc :query-params (:query-params operation))
      (seq (:header-params operation)) (assoc :header-params (:header-params operation))
      (seq (:cookie-params operation)) (assoc :cookie-params (:cookie-params operation))
      (seq (:auth-names operation)) (assoc :auth-names (:auth-names operation)))))

(defn api-functions
  "Returns the function-name projection for all operations in a schema source."
  {:added "4.1.4"}
  [source]
  (->> (schema-operations source)
       (mapv (fn [operation]
               (merge (select-keys operation +function_keys+)
                      {:args (operation-args operation)
                       :arglist (operation-arglist operation)
                       :call-map (operation-call-map operation)})))))
