(ns net.openapi
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [script.yaml :as yaml]
            [std.json :as json]
            [std.string.case :as case]))

(def +http_methods+
  #{:delete :get :head :options :patch :post :put :trace})

(def +descriptor_keys+
  [:operation_id
   :fn_name
   :method
   :path
   :summary
   :description
   :tags
   :path_params
   :query_params
   :header_params
   :cookie_params
   :request_body
   :response_content_types
   :auth_names])

(defn data-get
  [m k]
  (or (get m k)
      (when (string? k)
        (get m (keyword k)))
      (when (keyword? k)
        (get m (name k)))))

(defn data-get-in
  [m ks]
  (reduce (fn [acc k]
            (cond
              (map? acc)
              (data-get acc k)

              (and (vector? acc)
                   (integer? k)
                   (< -1 k (count acc)))
              (nth acc k)

              :else
              nil))
          m
          ks))

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

(defn read-source
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
            (string/starts-with? trimmed "[")
            (string/starts-with? trimmed "openapi:")
            (string/starts-with? trimmed "swagger:"))
        source

        (re-find #"^https?://" source)
        (slurp source)

        (.exists file)
        (slurp file)

        :else
        (if-let [resource (io/resource source)]
          (slurp resource)
          source)))

    :else
    (throw (ex-info "Unsupported OpenAPI source"
                    {:source source}))))

(defn detect-format
  [source content]
  (let [path (cond
               (string? source) source
               (instance? java.io.File source) (.getName ^java.io.File source)
               (instance? java.net.URL source) (str source)
               :else "")
        trimmed (when (string? content) (string/triml content))]
    (cond
      (map? source) :map
      (re-find #"\.json($|\?)" path) :json
      (re-find #"\.ya?ml($|\?)" path) :yaml
      (or (string/starts-with? trimmed "{")
          (string/starts-with? trimmed "["))
      :json
      :else
      :yaml)))

(defn read-spec
  "Reads an OpenAPI/Swagger spec from a map, file path, URL, or raw JSON/YAML string."
  {:added "4.1.4"}
  [source]
  (if (map? source)
    source
    (let [content (read-source source)]
      (case (detect-format source content)
        :json (json/read content)
        :yaml (yaml/read content :keywords false)
        content))))

(defn spec-info
  "Returns the spec info block."
  {:added "4.1.4"}
  [spec]
  (or (data-get spec "info") {}))

(defn spec-title
  "Returns the OpenAPI title."
  {:added "4.1.4"}
  [spec]
  (data-get (spec-info spec) "title"))

(defn spec-version
  "Returns the OpenAPI version."
  {:added "4.1.4"}
  [spec]
  (or (data-get (spec-info spec) "version")
      (data-get spec "openapi")
      (data-get spec "swagger")))

(defn spec-base-url
  "Returns the first declared base URL when the spec provides one."
  {:added "4.1.4"}
  [spec]
  (if-let [url (data-get-in spec ["servers" 0 "url"])]
    url
    (when-let [host (data-get spec "host")]
      (let [scheme (or (data-get-in spec ["schemes" 0]) "http")
            base-path (or (data-get spec "basePath") "")]
        (str scheme "://" host base-path)))))

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

(defn merged-parameters
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

(defn request-body-entry
  [spec operation parameters]
  (if-let [request-body (some-> (data-get operation "requestBody")
                                (resolve-ref spec))]
    {:required (boolean (data-get request-body "required"))
     :content_types (->> (keys (or (data-get request-body "content") {}))
                         vec)
     :schema_ref (some-> request-body
                         (data-get-in ["content" "application/json" "schema" "$ref"]))}
    (when-let [body-parameter (some #(when (= "body" (data-get % "in")) %) parameters)]
      {:required (boolean (data-get body-parameter "required"))
       :content_types (vec (or (data-get operation "consumes") []))
       :schema_ref (data-get-in body-parameter ["schema" "$ref"])})))

(defn response-content-types
  [spec operation]
  (let [response-types (->> (or (data-get operation "responses") {})
                            vals
                            (map #(resolve-ref spec %))
                            (mapcat #(keys (or (data-get % "content") {})))
                            distinct
                            vec)]
    (if (seq response-types)
      response-types
      (vec (or (data-get operation "produces")
               (data-get spec "produces")
               [])))))

(defn auth-names
  [spec operation]
  (->> (or (data-get operation "security")
           (data-get spec "security")
           [])
       (mapcat keys)
       distinct
       vec))

(defn normalize-operation
  "Normalizes one OpenAPI/Swagger operation into generator-friendly data."
  {:added "4.1.4"}
  [spec path path-item method operation]
  (let [parameters (merged-parameters spec path-item operation)
        non-body-parameters (remove #(= "body" (data-get % "in")) parameters)
        filter-params (fn [location]
                        (->> non-body-parameters
                             (filter #(= location (data-get % "in")))
                             (mapv (fn [parameter]
                                     {:name (data-get parameter "name")
                                      :required (boolean (data-get parameter "required"))
                                      :description (data-get parameter "description")}))))
        request-body (request-body-entry spec operation parameters)]
    {:operation_id (or (data-get operation "operationId")
                       (operation-name method path operation))
     :fn_name (operation-name method path operation)
     :method method
     :path path
     :summary (data-get operation "summary")
     :description (data-get operation "description")
     :tags (vec (or (data-get operation "tags") []))
     :path_params (filter-params "path")
     :query_params (filter-params "query")
     :header_params (filter-params "header")
     :cookie_params (filter-params "cookie")
     :request_body request-body
     :response_content_types (response-content-types spec operation)
     :auth_names (auth-names spec operation)}))

(defn operations
  "Returns all normalized operations from an OpenAPI/Swagger spec."
  {:added "4.1.4"}
  [spec]
  (->> (or (data-get spec "paths") {})
       (sort-by key)
       (mapcat (fn [[path path-item]]
                 (->> path-item
                      (filter (fn [[k _]]
                                (contains? +http_methods+ (keyword k))))
                      (sort-by key)
                      (map (fn [[method operation]]
                             (normalize-operation spec
                                                  path
                                                  path-item
                                                  (keyword method)
                                                  operation))))))
       vec))

(defn find-operation
  "Finds a normalized operation by method and path."
  {:added "4.1.4"}
  [source method path]
  (let [ops (if (vector? source)
              source
              (operations (if (map? source) source (read-spec source))))]
    (first (filter #(and (= (keyword method) (:method %))
                         (= path (:path %)))
                   ops))))

(defn operation-form
  "Creates a `defn` scaffold form for one normalized operation."
  {:added "4.1.4"}
  [{:keys [fn_name
           method
           path
           summary
           description
           path_params
           request_body
           response_content_types
           auth_names
           default_base_url]}]
  (let [fn-sym (symbol fn_name)
        path-bindings (mapv (fn [{:keys [name]}]
                              [name (symbol (case/spear-case name))])
                            path_params)
        path-args (mapv second path-bindings)
        body-required? (boolean (:required request_body))
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

                   (seq (or (:content_types request_body) []))
                   (assoc :content-type (first (:content_types request_body)))

                   (seq response_content_types)
                   (assoc :accepts response_content_types)

                   (seq auth_names)
                   (assoc :auth-names auth_names))]
    `(defn ~fn-sym
       ~doc
       ([~@base-args]
        (~fn-sym ~@base-args {}))
       ([~@opts-args]
        (let [client# (or client {})
              opts# (or opts {})
              base-url# (or (:base-url client#)
                            (:base_url client#)
                            (:base-url opts#)
                            (:base_url opts#)
                            ~default_base_url)]
          (net.openapi.util/call-api
           base-url#
           ~path
           ~method
           (merge ~defaults opts#)))))))

(defn scaffold-data
  "Builds generator metadata from an OpenAPI/Swagger source."
  {:added "4.1.4"}
  [source]
  (let [spec (if (map? source) source (read-spec source))]
    {:title (spec-title spec)
     :version (spec-version spec)
     :base_url (spec-base-url spec)
     :operations (operations spec)}))

(defn scaffold
  "Generates a `do` form containing `net.http` wrapper scaffolding for an OpenAPI spec."
  {:added "4.1.4"}
  ([source]
   (scaffold source {}))
  ([source {:keys [ns_sym]
            :or {ns_sym 'generated.openapi}}]
   (let [{:keys [title version base_url operations]} (scaffold-data source)
         operations (mapv #(assoc % :default_base_url base_url) operations)]
     `(do
        (ns ~ns_sym)
        (def ~'+openapi-meta+
          {:title ~title
           :version ~version
           :base_url ~base_url})
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
