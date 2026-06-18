(ns lib.supabase.generate
  "Generates Clojure client wrappers from `lib.supabase.api/+admin+` metadata."
  (:require [clojure.string :as str]
            [lib.supabase.api :as api]
            [net.openapi.call :as call]
            [std.block.template :as template]))

(defn string->kebab
  "Converts a snake_case string to kebab-case."
  [s]
  (-> (name s)
      (str/replace #"_" "-")
      (str/lower-case)))

(defn entry->path-keys
  "Extracts path-param names as kebab-cased symbols."
  [entry]
  (->> (:path-params entry)
       (map :name)
       (map string->kebab)
       (map symbol)
       vec))

(defn entry->query-keys
  "Extracts query-param names as kebab-cased symbols."
  [entry]
  (->> (:query-params entry)
       (map :name)
       (map string->kebab)
       (map symbol)
       vec))

(defn entry->body-keys
  "Extracts body property names as kebab-cased symbols."
  [entry]
  (->> (get-in entry [:body :properties])
       keys
       (map string->kebab)
       (map symbol)
       vec))

(defn entry->args-form
  "Builds the args vector for a generated function.
   Order: path params, query params, body params, then client."
  [entry]
  (let [path-keys  (entry->path-keys entry)
        query-keys (entry->query-keys entry)
        body-keys  (entry->body-keys entry)]
    (vec (concat (when (seq path-keys)
                   [{:keys path-keys :as 'path}])
                 (when (seq query-keys)
                   [{:keys query-keys :as 'query}])
                 (when (seq body-keys)
                   [{:keys body-keys :as 'body}])
                 ['client]))))

(defn entry->input-form
  "Builds the input map passed to `call/call`."
  [entry]
  (let [path-keys  (entry->path-keys entry)
        query-keys (entry->query-keys entry)
        body-keys  (entry->body-keys entry)]
    (cond-> {}
      (seq path-keys)  (assoc :path 'path)
      (seq query-keys) (assoc :query 'query)
      (seq body-keys)  (assoc :body 'body))))

(defn entry->template-input
  "Converts a `+admin+` map entry into template parameter values."
  [[operation-id entry]]
  {'fn-name      (symbol operation-id)
   'operation-id operation-id
   'args-form    (entry->args-form entry)
   'input-form   (entry->input-form entry)
   'client-sym   'client})

(def FN_TEMPLATE
  "Template for a single Supabase admin API wrapper function."
  "
(defn ~fn-name
  ~args-form
  (call/call (get api/+admin+ ~operation-id)
             ~input-form
             ~client-sym))")

(def ^:private compiled-fn-template
  (template/get-template FN_TEMPLATE))

(defn generate-admin-fn
  "Generates the source string for a single admin endpoint wrapper."
  [entry-pair]
  (template/fill-template compiled-fn-template
                          (entry->template-input entry-pair)))

(defn generate-admin-functions
  "Generates wrapper source strings for all endpoints in `+admin+`."
  []
  (->> api/+admin+
       (map generate-admin-fn)
       (str/join "\n\n")))

(def ^:private ns-header
  "(ns lib.supabase.common\n  (:require [clojure.string :as str]\n            [std.lib.foundation :as f]\n            [lib.supabase.api :as api]\n            [net.openapi.call :as call]))\n\n(def ^:dynamic *default*)\n\n")

(defn generate-common-file
  "Generates the full source for `lib.supabase.common`."
  []
  (str ns-header (generate-admin-functions)))

(defn write-common-file!
  "Writes the generated `lib.supabase.common` namespace to disk."
  []
  (spit "src/lib/supabase/common.clj" (generate-common-file)))
