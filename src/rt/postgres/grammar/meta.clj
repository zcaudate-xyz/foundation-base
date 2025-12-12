(ns rt.postgres.grammar.meta
  (:require [rt.postgres.grammar.common-application :as app]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.util :as ut]
            [std.string :as str]
            [std.lib :as h]))

(defn has-function
  "checks for existence of a function"
  {:added "4.0"}
  ([name schema]
   [:select (list 'exists [:select '*
                           :from  'pg_catalog.pg_proc
                           :where {:proname name
                                   :pronamespace
                                   [:eq
                                    [:select #{'oid}
                                     :from  'pg_catalog.pg_namespace
                                     :where {:nspname schema}]]}])]))

(defn has-table
  "checks for existence of a table"
  {:added "4.0"}
  ([name schema]
   [:select (list 'exists [:select '*
                           :from  'information_schema.tables 
                           :where {:table-schema schema
                                   :table-name name}])]))

(defn has-enum
  "checks for existence of an enum"
  {:added "4.0"}
  ([name schema]
   [:select
    (list 'exists [:select '*
                   :from  'pg_catalog.pg_type
                   :where {:proname name
                           :pronamespace
                           [:eq
                            [:select #{'oid}
                             :from  'pg_catalog.pg_namespace
                             :where {:nspname schema}]]}])]))

(defn has-index
  "cheks for the existence of an index"
  {:added "4.0"}
  ([name schema]
   `[:select
     (list 'exists
           [:select '*
            :from 'pg_catalog.pg_index
            :where {'indkey
                    [:eq [:select #{'attrelid}
                          :from 'pg_catalog.pg_attribute
                          :where {:attrelid
                                  [:eq [:select #{'oid}
                                        :from 'pg_catalog.pg_class
                                        :where {:relname name
                                                :relnamespace
                                                [:eq
                                                 [:select #{'oid}
                                                  :from  'pg_catalog.pg_namespace
                                                  :where {:nspname schema}]]}]]}]]}])]))

(defn has-trigger
  "checks for the existence of a trigger"
  {:added "4.0"}
  ([name schema table]
   [:select
    (list 'exists
          [:select '*
           :from 'information_schema.triggers
           :where {:trigger_name name
                   :event_object_schema schema
                   :event_object_table table}])]))

(defn has-const
  "checks for the existence of a const"
  {:added "4.0"}
  ([ptr]
   (let [entry (ptr/get-entry ptr)
         {ptr-id :id form :form} entry
         [_ _ _ data] form]
     (if-let [data-id (:id data)]
       (let [{:static/keys [table]} entry
             {table-id :id module :module} table
             t-entry (ptr/get-entry (ut/lang-pointer :postgres
                                                     {:module module
                                                      :id table-id}))
             {:static/keys [schema]} t-entry]
         [:select
          (list 'exists
                [:select 1
                 :from (list '. #{(or schema "public")} #{table-id})
                 :where {:id data-id}])])))))

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
   [:create-extension :if-not-exists #{ex}]))

(defn drop-extension
  "makes drop extension forms"
  {:added "4.0"}
  ([ex]
   [:drop-extension :if-exists #{ex} :cascade]))

(defn has-policy
  "TODO"
  {:added "4.0"}
  [{:static/keys [schema
                  policy-name
                  policy-schema
                  policy-table]}]
  [:select
   (list 'exists
         [:select 1
          :from 'pg_policies
          :where {:schemaname (or policy-schema
                                  schema)
                  :tablename policy-table
                  :policyname policy-name}])])

(defn drop-policy
  "TODO"
  {:added "4.0"}
  ([{:static/keys [schema
                   policy-name
                   policy-schema
                   policy-table]}]
   [:drop-policy :if-exists #{policy-name}
    :on (list '. #{(or policy-schema
                       schema)}
              #{policy-table})]))

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
   (list 'exists [:select '*
                  :from  'pg_catalog.pg_namespace
                  :where {:nspname sch}])))

(defn create-schema
  "creates a schema"
  {:added "4.0"}
  ([sch]
   [:create-schema :if-not-exists #{sch}]))

(defn drop-schema
  "drops a schema"
  {:added "4.0"}
  ([sch]
   [:drop-schema :if-exists #{sch} :cascade]))

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
                           :function (has-function name sch)
                           :policy   (has-policy (ptr/get-entry ptr))
                           :trigger  (let [entry (ptr/get-entry ptr)
                                           [_ _ _ [table]] (:form entry)
                                           table (if (symbol? table)
                                                   (name table)
                                                   (str table))]
                                       (has-trigger name sch table))
                           :const    (has-const ptr))))
   :setup-ptr        (fn [ptr]
                       (:form (ptr/get-entry ptr)))
   :teardown-ptr     (fn [ptr]
                       (let [[name sch dbtype existing op] (classify-ptr ptr)
                             type (cond (= op :enum)
                                        :type
                                        
                                        :else dbtype)]
                         (cond (= dbtype :policy)
                               (drop-policy (ptr/get-entry ptr))

                               (= dbtype :trigger)
                               (let [entry (ptr/get-entry ptr)
                                     [_ _ _ [table]] (:form entry)
                                     table (if (symbol? table)
                                             (name table)
                                             (str table))]
                                 [:drop-trigger :if-exists #{name} :on table :cascade])

                               (= dbtype :const)
                               (let [entry (ptr/get-entry ptr)
                                     {ptr-id :id form :form} entry
                                     [_ _ _ data] form]
                                 (if-let [data-id (:id data)]
                                   (let [{:static/keys [table]} entry
                                         {table-id :id module :module} table
                                         t-entry (ptr/get-entry (ut/lang-pointer :postgres
                                                                                 {:module module
                                                                                  :id table-id}))
                                         {:static/keys [schema]} t-entry]
                                     [:delete-from (list '. #{(or schema "public")} #{table-id})
                                      :where {:id data-id}])))

                               (and type
                                    (not existing))
                               (list 'do [:drop type :if-exists (list '. #{sch} #{name}) :cascade]))))})
