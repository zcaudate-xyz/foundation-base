(ns net.openapi.generate
  (:require [net.openapi :as openapi]))

(def +function_keys+
  [:operation_id
   :fn_name
   :method
   :path
   :summary
   :description
   :tags])

(defn read-schema
  "Reads an OpenAPI/Swagger schema from a map, file path, URL, or raw JSON/YAML string."
  {:added "4.1.4"}
  [source]
  (openapi/read-spec source))

(defn schema-operations
  "Returns normalized operations for a schema source."
  {:added "4.1.4"}
  [source]
  (openapi/operations (read-schema source)))

(defn api-function-name
  "Returns the generated function name for a normalized operation."
  {:added "4.1.4"}
  [operation]
  (:fn_name operation))

(defn api-functions
  "Returns the function-name projection for all operations in a schema source."
  {:added "4.1.4"}
  [source]
  (->> (schema-operations source)
       (mapv #(select-keys % +function_keys+))))
