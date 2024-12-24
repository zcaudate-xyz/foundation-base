(ns rt.postgres.grammar.meta
  (:require [std.lang.base.pointer :as ptr]
            [std.lang.base.util :as ut]
            [std.string :as str]
            [std.lib :as h]))

(defn has-function
  "checks for existence of a function"
  {:added "4.0"}
  ([name schema]
   `[:select (~'exists [:select ~'*
                        :from  ~'pg_catalog.pg_proc
                        :where {:proname ~name
                                :pronamespace
                                [:eq
                                 [:select #{~'oid}
                                  :from  ~'pg_catalog.pg_namespace
                                  :where {:nspname ~schema}]]}])]))

(defn has-table
  "checks for existence of a table"
  {:added "4.0"}
  ([name schema]
  `[:select (~'exists [:select ~'*
                     :from  ~'information_schema.tables 
                     :where {:table-schema ~schema
                             :table-name ~name}])]))

(defn has-enum
  "checks for existence of an enum"
  {:added "4.0"}
  ([name schema]
   `[:select
     (~'exists [:select ~'*
              :from  ~'pg_catalog.pg_type
              :where {:proname ~name
                      :pronamespace
                      [:eq
                       [:select #{~'oid}
                        :from  ~'pg_catalog.pg_namespace
                        :where {:nspname ~schema}]]}])]))

(defn has-index
  "cheks for the existence of an index"
  {:added "4.0"}
  ([name schema]
   `[:select
     (~'exists
      [:select ~'*
       :from ~'pg_catalog.pg_index
       :where {~'indkey
               [:eq [:select #{~'attrelid}
                     :from ~'pg_catalog.pg_attribute
                     :where {:attrelid
                             [:eq [:select #{~'oid}
                                   :from ~'pg_catalog.pg_class
                                   :where {:relname ~name
                                           :relnamespace
                                           [:eq
                                            [:select #{~'oid}
                                             :from  ~'pg_catalog.pg_namespace
                                             :where {:nspname ~schema}]]}]]}]]}])]))

(defn get-extensions
  "gets import forms"
  {:added "4.0"}
  ([module & [seed-only]]
   (->> module
        :native
        (keep (fn [[k m]] (if (not seed-only)
                            k
                            (if (:seed m) k)))))))

(defn create-extension
  "makes create extension forms"
  {:added "4.0"}
  ([ex]
   `[:create-extension :if-not-exists #{~ex}]))

(defn drop-extension
  "makes drop extension forms"
  {:added "4.0"}
  ([ex]
   `[:drop-extension :if-exists #{~ex} :cascade]))

(defn get-schema-seed
  "gets schema seed for a given module"
  {:added "4.0"}
  ([module]
   (-> module
       :static
       :seed)))

(defn has-schema
  "checks that schema exists"
  {:added "4.0"}
  ([sch]
   `(~'exists [:select ~'*
               :from  ~'pg_catalog.pg_namespace
               :where {:nspname ~sch}])))

(defn create-schema
  "creates a schema"
  {:added "4.0"}
  ([sch]
   `[:create-schema :if-not-exists #{~sch}]))

(defn drop-schema
  "drops a schema"
  {:added "4.0"}
  ([sch]
   `[:drop-schema :if-exists #{~sch} :cascade]))

(defn classify-ptr
  "classifies the pointer"
  {:added "4.0"}
  ([ptr]
   (let [{:static/keys [schema dbtype]
          :keys [id op existing]} (ptr/get-entry ptr)
         name  (ut/sym-default-str (str id))
         sch   (or schema "public")]
     [name sch dbtype existing op])))

(def +fn+
  {:has-module       (fn [module]
                       (let [schemas (get-schema-seed module)]
                         (if schemas
                           `[:select ~@(interpose :and (map has-schema schemas))])))
   :setup-module     (fn [module]
                       (let [extensions (get-extensions module)
                             schemas    (get-schema-seed module)
                             body       (concat (map create-schema schemas)
                                                (map create-extension extensions))]
                         (if (or schemas extensions)
                           (apply list 'do body))))
   :teardown-module  (fn [module]
                       (let [extensions (get-extensions module)
                             schemas    (get-schema-seed module)
                             body       (concat (map drop-extension extensions)
                                                (map drop-schema schemas))]
                         (if (or schemas extensions)
                           (apply list 'do body))))
   
   :has-ptr          (fn [ptr]
                       (let [[name sch dbtype] (classify-ptr ptr)]
                         (case dbtype
                           :table    (has-table name sch)
                           :enum     (has-enum name sch)
                           :index    (has-index name sch)
                           :function (has-function name sch))))
   :setup-ptr        (fn [ptr]
                       (:form (ptr/get-entry ptr)))
   :teardown-ptr     (fn [ptr]
                       (let [[name sch dbtype existing op] (classify-ptr ptr)
                             type (cond (= op :enum)
                                        :type

                                        :else dbtype)]
                         (if (and type (not existing))
                           `(~'do [:drop ~type :if-exists (. #{~sch} #{~name}) :cascade]))))})
