(ns rt.postgres.gen-bind
  (:require [clojure.string]
            [rt.postgres.grammar.common :as common]
            [rt.postgres.grammar.common-application :as app]
            [std.lang :as l]
            [std.lib.collection :as collection]
            [std.lib.deps :as deps]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]
            [std.string.case :as case]))

(defn to-lookup
  [arr]
  (zipmap arr (repeat true)))

(defn plain-symbol?
  [form]
  (and (symbol? form)
       (not (namespace form))))

(defn transform-to-str
  "transforms relevant forms to string"
  {:added "4.0"}
  [x]
  (cond (or (coll? x)
            (number? x)
            (boolean? x))
        x

        (symbol? x)
        (cond (namespace x)
              (let [entry @@(resolve x)]
                {"::" (str "sql/" (name (:op-key entry)))
                 :schema (:static/schema entry)
                 :name (name (:id entry))})
              
              :else
              (l/sym-default-str (name x)))

        (keyword? x)
        (l/sym-default-str (f/strn x))

        :else x))

(defn transform-query-or
  "transforms a setvec form"
  {:added "4.0"}
  [form]
  (cond (and (set? form)
             (vector? (first form))
             (some (fn [v] (= v [:or]))
                   (first form)))
        (loop [acc  []
               curr {}
               [v :as input] (first form)]
          (cond (empty? input)
                (if (not-empty curr)
                  (conj acc curr)
                  acc)

                (= v [:or])
                (recur (if (not-empty curr)
                         (conj acc curr)
                         acc)
                       {}
                       (rest input))

                :else
                (recur acc
                       (assoc curr v (second input))
                       (drop 2 input))))
        
        :else form))

(defn transform-query-classify
  "transform function and quote representations"
  {:added "4.0"}
  [form]
  (let [transform-fn (fn [form]
                       (cond (and (set? form)
                                  (string? (first form)))
                             {"::" "sql/column"
                              :name (first form)}

                             (string? form)
                             {"::" "sql/arg"
                              :name form}
                             
                             :else
                             form))]
    (cond (collection/form? form)
          (cond (= 'quote (first form))
                (vector (vec (second form)))

                (= '++ (first form))
                {"::" "sql/cast"
                 :args  (mapv transform-fn (rest form))}
                
                (and (symbol? (first form)) 
                     (namespace (first form)))
                {"::" "sql/fn"
                 :name  (let [tok         (common/pg-linked-token (first form)
                                                                  {:lang :postgres
                                                                   :snapshot (l/get-snapshot (l/runtime-library))})]
                          (cond (symbol? tok)
                                (l/sym-default-str tok)
                                
                                :else
                                (let [[_ ns name] tok]
                                  (str (pr-str (first ns)) "."  (l/sym-default-str name)))))
                 :args  (mapv transform-fn (rest form))}
                
                :else
                {"::" "sql/fn"
                 :name  (l/sym-default-str (first form))
                 :args  (mapv transform-fn (rest form))})
          
          (and (vector? form)
               (= :select (first form)))
          {"::" "sql/select"
           :args (mapv transform-fn (vec (rest form)))}
          
          :else
          form)))

(defn transform-query
  "generates the query interface"
  {:added "4.0"}
  [query & [sym-pred]]
  (let [sym-pred (or sym-pred plain-symbol?)]
    (->> query
         (walk/prewalk  transform-query-or)
         (walk/postwalk (fn [form]
                       (if (sym-pred form)
                         (str "{{" (l/sym-default-str form) "}}")
                         form)))
         (walk/prewalk  transform-query-classify)
         (walk/postwalk (fn [form]
                       (cond (set? form)
                             (vec (map transform-to-str form))
                             
                             (vector? form)
                             (vec (map transform-to-str form))
                             
                             :else form))))))

(defn transform-schema
  "transforms the schema"
  {:added "4.0"}
  [schema]
  (walk/postwalk (fn [x]
                (cond (keyword? x)
                      (.replaceAll (name x) "-" "_")
                      
                      (set? x)
                      (vec x)
                      
                      (symbol? x)
                      (str x)
                      
                      (map? x)
                      (let [{:strs [sql]} x
                            {:strs [default unique]} sql]
                        (cond-> (dissoc x "web" "sql")
                          (not (or (nil? default)
                                   (collection/form? default)))
                          (assoc "sql" {"default" default})))
                      
                      :else x))
              schema))

(defn bind-function
  "generates the type signatures for a pg function"
  {:added "4.0"}
  [ptr]
  (let [entry  (l/get-entry ptr)
        events (select-keys entry [:api/mq.event
                                   :api/ui.event])]
    (-> (select-keys entry
                     [:static/input
                      :static/return
                      :static/schema
                      :id
                      :api/flags])
        (collection/unqualify-keys)
        (update-in [:flags] to-lookup)
        (update-in [:flags] merge (collection/unqualify-keys events))
        (update-in [:id] l/sym-default-str)
        (update-in [:return] (comp name first))
        (update-in [:input]  (fn [input] (mapv l/emit-type-record input))))))

(defn bind-view-guards
  "gets more guards"
  {:added "4.0"}
  [guards]
  (mapv (fn [[f & args]]
          {:function (bind-function @(resolve f))
           :args (transform-query (vec args))})
        guards))

(defn bind-view
  "generates the view interface"
  {:added "4.0"}
  [ptr & [opts]]
  (let [entry (l/get-entry ptr)
        {:keys [table type guards autos scope query query-base args] :as view} (:static/view entry)
        {:keys [id] :as m} (bind-function ptr)
        
        table     (name table)
        tag-table (case/snake-case table)
        tag    (if (not (clojure.string/starts-with? id tag-table))
                 (f/error "View should start with table name"
                          {:id id
                           :table table
                           :assert tag-table})
                 (subs id (inc (count tag-table))))]
    (binding [*ns* (the-ns (:namespace entry))]
      (-> m
          (update :flags merge (to-lookup scope))
          (merge {:view  (merge
                          {:table table
                           :type  (name type)
                           :tag   tag
                           :query  (transform-query (or query-base
                                                        query)
                                                    (set (filter symbol? args)))
                           #_#_:guards (bind-view-guards guards)
                           #_#_:autos  (bind-view-guards autos)}
                          opts)})))))

(defn bind-table
  "gets the table interface"
  {:added "4.0"}
  [ptr & [update-key]]
  (let [{:keys [id] :as entry} (l/get-entry ptr)
        schema-seed (:static/schema-seed entry)
        schema-update (if update-key
                        (-> schema-seed
                            :tree
                            ((keyword (str id)))
                            update-key
                            boolean)
                        false)]
    (-> (select-keys entry
                     [:static/schema
                      :static/schema-primary
                      :static/public])
        (collection/unqualify-keys)
        (assoc  :schema-update  schema-update)
        (update :schema-primary transform-query))))

(defn bind-app
  "gets the app interface given a name"
  {:added "4.0"}
  [app & [update-key]]
  (let [{:keys [pointers]} app
        module-lu    (zipmap (reverse (deps/deps-ordered
                                       (l/get-book (l/default-library)
                                                   :postgres)
                                       (dedupe (map :module (vals pointers)))))
                             (range))
        entries (->> (mapv l/get-entry (vals pointers))
                     (sort-by (fn [{:keys [module line]}]
                                [(module-lu module) line]))
                     (filter identity))]
    (->> (map-indexed (fn [pos {:keys [id] :as entry}]
                        [(name id) (assoc (bind-table entry update-key)
                                          :position pos)])
                      entries)
         (into {}))))

(defn bind-schema
  "binds a schema"
  {:added "4.0"}
  [schema & [excluded]]
  (transform-schema
   (collection/map-vals (fn [m]
                 (cond->>  m
                   excluded (collection/filter-keys (comp not excluded))
                   :then (collection/map-vals first)))
               (:tree schema))))

(defn list-view
  "lists all views in the schema"
  {:added "4.0"}
  [ns type]
  (->> (l/module-entries
        :postgres ns
        (fn [e]
          (= type (:type (:static/view e)))))
       (mapv (juxt :id l/sym-full))))

(defn list-api
  "lists all apis"
  {:added "4.0"}
  [ns & [pred]]
  (->> (l/module-entries
        :postgres ns
        (or pred :api/flags))
       (mapv (juxt :id l/sym-full))))

(defn list-debug
  "lists all debug apis"
  {:added "4.0"}
  [ns & [pred]]
  (->> (l/module-entries
        :postgres ns
        (or pred (fn [e]
                   (and (= :defn (:op-key e))
                        (not (:api/flags e))
                        (not (:static/view e))
                        (not (:api/suppress e))))))
       (mapv (juxt :id l/sym-full))))

(defn list-all
  "lists all function forms"
  {:added "4.0"}
  [ns]
  (->> (l/module-entries
        :postgres ns
        #(-> % :op-key (= :defn)) )
       (mapv (juxt :id l/sym-full))))


(comment
  (./create-tests))
