(ns net.openapi.read
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [std.json :as json]
            [std.lib.env :as env]
            [std.string.case :as case]))

(def +http-methods+
  #{:delete :get :head :options :patch :post :put :trace})

(def +body-locations+
  #{"body" "formData"})

(defn data-get
  [m k]
  (let [keys (cond
               (keyword? k) [k (name k)]
               (string? k)  [k (keyword k)]
               :else [k])]
    (some (fn [candidate]
            (let [value (get m candidate ::missing)]
              (when-not (= ::missing value)
                value)))
          keys)))

(defn data-get-in
  [m ks]
  (reduce (fn [acc k]
            (cond
              (map? acc)
              (data-get acc k)

              (and (vector? acc)
                   (integer? k)
                   (<= 0 k)
                   (< k (count acc)))
              (nth acc k)

              :else
              nil))
          m
          ks))

(defn read-json-string
  [s]
  (json/read s))

(defonce +resource-schema-cache+
  (atom {}))

(defn resource-path
  [source]
  (when (string? source)
    (let [trimmed (string/triml source)
          candidates (cond-> [source]
                       (string/starts-with? source "resources/")
                       (conj (subs source (count "resources/"))))]
      (when-not (or (string/starts-with? trimmed "{")
                    (string/starts-with? trimmed "[")
                    (re-find #"^https?://" source))
        (some #(when (env/sys:resource %)
                 %)
              candidates)))))

(defn slurp-source
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
  [source]
  (if (map? source)
    source
    (if-let [path (resource-path source)]
      (env/sys:resource-cached +resource-schema-cache+
                               path
                               (fn [url]
                                 (read-json-string (slurp url))))
      (-> source
          slurp-source
          read-json-string))))

(defn spec-version
  [spec]
  (cond
    (data-get spec "swagger") :v2
    (data-get spec "openapi") :v3
    :else nil))

(defn unescape-ref-token
  [token]
  (-> token
      (string/replace "~1" "/")
      (string/replace "~0" "~")))

(defn ref-path
  [ref]
  (when (and (string? ref)
             (string/starts-with? ref "#/"))
    (mapv unescape-ref-token
          (rest (string/split ref #"/")))))

(defn resolve-ref
  [spec node]
  (if-let [ref (and (map? node) (data-get node "$ref"))]
    (let [resolved (get-in spec (ref-path ref))]
      (merge (resolve-ref spec resolved)
             (dissoc node "$ref")))
    node))

(defn vendor-extension-map
  [m]
  (->> (or m {})
       (keep (fn [[k v]]
               (let [k (name k)]
                 (when (string/starts-with? k "x-")
                   [k v]))))
       (into (sorted-map))))

(declare normalize-schema)

(defn normalize-properties
  [spec properties]
  (->> (or properties {})
       (map (fn [[k v]]
              [k (normalize-schema spec v)]))
       (into (sorted-map))))

(defn operation-name
  [method path operation]
  (or (some-> (data-get operation "operationId")
              case/spear-case)
      (->> (string/split path #"/")
           (remove string/blank?)
           (map (fn [segment]
                  (if (and (string/starts-with? segment "{")
                           (string/ends-with? segment "}"))
                    (str "by-" (case/spear-case (subs segment 1 (dec (count segment)))))
                    (case/spear-case segment))))
           (cons (name method))
           (string/join "-")
           case/spear-case)))

(defn merge-parameters
  [spec path-item operation]
  (->> (concat (or (data-get path-item "parameters") [])
               (or (data-get operation "parameters") []))
       (map #(resolve-ref spec %))
       (reduce (fn [out parameter]
                 (assoc out [(data-get parameter "name")
                             (data-get parameter "in")]
                        parameter))
               {})
       vals
       vec))

(defn normalize-schema
  [spec schema]
  (when schema
    (let [schema (resolve-ref spec schema)
          custom (vendor-extension-map schema)]
      (cond-> {}
        (contains? schema "type")
        (assoc :type (data-get schema "type"))

        (contains? schema "description")
        (assoc :description (data-get schema "description"))

        (contains? schema "properties")
        (assoc :properties (normalize-properties spec (data-get schema "properties")))

        (contains? schema "items")
        (assoc :items (normalize-schema spec (data-get schema "items")))

        (contains? schema "required")
        (assoc :required (vec (or (data-get schema "required") [])))

        (contains? schema "enum")
        (assoc :enum (vec (or (data-get schema "enum") [])))

        (contains? schema "format")
        (assoc :format (data-get schema "format"))

        (contains? schema "nullable")
        (assoc :nullable (boolean (data-get schema "nullable")))

        (seq custom)
        (assoc :custom custom)))))

(defn normalize-content
  [spec content]
  (let [content (or content {})
        preferred (or (data-get content "application/json")
                      (some->> content vals first))
        schema (some-> preferred
                       (data-get "schema")
                       (#(normalize-schema spec %)))]
    (cond-> {:content-types (vec (keys content))}
     schema (merge schema))))

(defn normalize-body-v2
  [spec operation parameters]
  (when-let [parameter (some #(when (contains? +body-locations+ (data-get % "in"))
                                %)
                             parameters)]
    (let [raw-schema (or (data-get parameter "schema")
                         (when (= "formData" (data-get parameter "in"))
                           {"type" "object"}))
          schema (normalize-schema spec raw-schema)]
      (cond-> {:required (boolean (data-get parameter "required"))
              :content-types (vec (or (data-get operation "consumes")
                                      (data-get spec "consumes")
                                      []))}
        schema (merge schema)))))

(defn normalize-body-v3
  [spec operation]
  (when-let [raw-request-body (data-get operation "requestBody")]
    (let [request-body (resolve-ref spec raw-request-body)
          content (normalize-content spec (data-get request-body "content"))]
      (assoc content
            :required
            (boolean (or (data-get raw-request-body "required")
                         (data-get request-body "required")))))))

(defn normalize-parameter
  [spec parameter]
  (let [schema (normalize-schema spec (data-get parameter "schema"))]
    (cond-> {:name (data-get parameter "name")
             :required (boolean (data-get parameter "required"))
             :description (data-get parameter "description")}
      schema (merge schema))))

(defn parameter-bucket
  [spec parameters location]
  (->> parameters
       (filter #(= location (data-get % "in")))
       (mapv #(normalize-parameter spec %))))

(defn response-content-types-v2
  [spec operation]
  (vec (or (data-get operation "produces")
           (data-get spec "produces")
           [])))

(defn response-content-types-v3
  [spec operation]
  (let [content-types (->> (or (data-get operation "responses") {})
                           vals
                           (map #(resolve-ref spec %))
                           (mapcat #(keys (or (data-get % "content") {})))
                           distinct
                           vec)]
    (if (seq content-types)
      content-types
      (response-content-types-v2 spec operation))))

(defn auth-names
  [spec operation]
  (->> (or (data-get operation "security")
           (data-get spec "security")
           [])
       (mapcat keys)
       distinct
       vec))

(defn operation-entry
  [spec version path path-item method operation]
  (let [parameters (merge-parameters spec path-item operation)
        non-body-parameters (remove #(contains? +body-locations+ (data-get % "in"))
                                    parameters)
        body (case version
               :v2 (normalize-body-v2 spec operation parameters)
               :v3 (normalize-body-v3 spec operation))
        response-content-types (case version
                                 :v2 (response-content-types-v2 spec operation)
                                 :v3 (response-content-types-v3 spec operation))]
    {:operation-id (or (data-get operation "operationId")
                       (operation-name method path operation))
     :fn-name (operation-name method path operation)
     :method method
     :path path
     :summary (data-get operation "summary")
     :description (data-get operation "description")
     :tags (vec (or (data-get operation "tags") []))
     :path-params (parameter-bucket spec non-body-parameters "path")
     :query-params (parameter-bucket spec non-body-parameters "query")
     :header-params (parameter-bucket spec non-body-parameters "header")
     :cookie-params (parameter-bucket spec non-body-parameters "cookie")
     :body body
     :response-content-types response-content-types
     :auth-names (auth-names spec operation)}))

(defn operation-map
  [spec version]
  (->> (or (data-get spec "paths") {})
       (sort-by key)
       (mapcat (fn [[path path-item]]
                 (->> path-item
                      (filter (fn [[method _]]
                                (contains? +http-methods+ (keyword method))))
                      (sort-by key)
                      (map (fn [[method operation]]
                             (let [method (keyword method)
                                   operation (resolve-ref spec operation)]
                               (operation-entry spec version path path-item method operation)))))))
       (sort-by :fn-name)
       (map (fn [operation]
              [(:fn-name operation) operation]))
       (into (sorted-map))))

(defn select-filter
  [selector id]
  (cond (or (fn? selector)
           (var? selector))
       (boolean (selector id))

       (or (string? selector)
           (symbol? selector)
           (keyword? selector))
       (.startsWith ^String (str id) (str selector))

       (instance? java.util.regex.Pattern selector)
       (boolean (re-find selector (str id)))

       (set? selector)
       (contains? selector id)

       (seq? selector)
       (every? #(select-filter % id) selector)

       (vector? selector)
       (some #(select-filter % id) selector)

       (number? selector)
       (= selector id)

       :else
       (throw (ex-info "Selector not valid" {:selector selector}))))

(defn select-operations
  [operations selector]
  (if (nil? selector)
    operations
    (->> operations
        (filter (fn [[_ entry]]
                  (select-filter selector (:path entry))))
        (into (sorted-map)))))

(def +empty-option-keys+
  [:cookie-params :query-params :header-params :path-params])

(defn remove-empty-options
  [entry]
  (reduce (fn [out k]
           (if (and (vector? (get out k))
                    (empty? (get out k)))
             (dissoc out k)
             out))
         entry
         +empty-option-keys+))

(defn format-operations
  [operations {:keys [remove-empty?]}]
  (if remove-empty?
    (->> operations
        (map (fn [[k entry]]
               [k (remove-empty-options entry)]))
        (into (sorted-map)))
    operations))

(defn read-v2
  "Reads a Swagger 2 JSON schema and returns a normalized operation map keyed by function name."
  {:added "4.1.4"}
  [source]
  (let [spec (read-schema source)]
    (when-not (= :v2 (spec-version spec))
      (throw (ex-info "Expected Swagger 2 JSON schema"
                      {:source source
                       :version (spec-version spec)})))
    (operation-map spec :v2)))

(defn read-v3
  "Reads an OpenAPI 3 JSON schema and returns a normalized operation map keyed by function name."
  {:added "4.1.4"}
  [source]
  (let [spec (read-schema source)]
    (when-not (= :v3 (spec-version spec))
      (throw (ex-info "Expected OpenAPI 3 JSON schema"
                      {:source source
                       :version (spec-version spec)})))
    (operation-map spec :v3)))

(defn read
  "Reads a Swagger 2 or OpenAPI 3 JSON schema and returns a normalized operation map keyed by function name.

   The optional `selector` filters normalized operations by `:path`.
   Pass `{:remove-empty? true}` to omit empty parameter buckets."
  {:added "4.1.4"}
  ([source]
   (read source nil))
  ([source selector-or-options]
   (if (map? selector-or-options)
     (read source
          (:selector selector-or-options)
          selector-or-options)
     (read source selector-or-options nil)))
  ([source selector opts]
   (let [spec (read-schema source)
         operations (case (spec-version spec)
                      :v2 (read-v2 spec)
                      :v3 (read-v3 spec)
                      (throw (ex-info "Unsupported OpenAPI JSON schema version"
                                      {:source source
                                       :version (or (data-get spec "openapi")
                                                    (data-get spec "swagger"))})))]
     (-> operations
         (select-operations selector)
         (format-operations opts)))))
