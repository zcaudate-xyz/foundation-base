(ns postgres.core.graph-view
  (:require [lang.model.annex.spec-postgres.common :as common]
            [postgres.core.graph-base :as base]
            [postgres.core.graph-query :as query]
            [postgres.core.impl-base :as impl]
            [lang.core :as l]
            [lang.base.emit-preprocess :as preprocess] [lang.base.preprocess-base :as preprocess-base]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [std.lib.walk :as walk]
            [std.string.case :as case]))

;;
;; select
;;

(defn make-view-prep
  "preps view access"
  {:added "4.0"}
  [sym & [rargs]]
  (let [{:keys [scope args tag guards autos] :as msym} (meta sym)
        [table] (:- msym)
        table-key  (keyword (name table))
        table-sym  (f/var-sym (resolve table))
        guards     (walk/postwalk f/resolve-namespaced guards)
        autos      (walk/postwalk f/resolve-namespaced autos)
        tag        (or tag
                       (-> (clojure.core/subs (case/camel-case (str sym))
                                              (clojure.core/count (name table)))
                           (case/spear-case)))]
    {:table  table-sym
     :key    table-key
     :scope  scope
     :args   (or rargs args [])
     :guards guards
     :autos  autos
     :tag    tag}))

(defn- table-schema
  [table-sym]
  (let [[entry schema _] (impl/prep-table table-sym true (l/rt:macro-opts :postgres))]
    {:entry entry
     :schema schema}))

(defn- column-key
  [column]
  (cond (keyword? column) column
        (symbol? column) (keyword (name column))
        (string? column) (keyword column)
        :else column))

(defn- column-descriptor
  [schema descriptor]
  (let [id    (column-key (or (:id descriptor) descriptor))
        attrs (first (get schema id))]
    (when attrs
      (merge attrs
             {:id id}
             (if (map? descriptor)
               (select-keys descriptor [:type :enum :ref])
               {})))))

(defn- primary-key-descriptors
  [table-sym]
  (let [{:keys [entry schema]} (table-schema table-sym)
        primary (:static/schema-primary entry)
        primary (cond (map? primary) [primary]
                      (vector? primary) primary
                      :else (->> schema
                                 (keep (fn [[id [attrs]]]
                                         (when (:primary attrs)
                                           {:id id :type (:type attrs)})))
                                 vec))
        primary (if (seq primary)
                  primary
                  (when-let [attrs (first (get schema :id))]
                    [{:id :id :type (:type attrs)}]))]
    (mapv #(column-descriptor schema %) primary)))

(defn- identity-descriptors
  [table-sym sym]
  (let [{:keys [schema]} (table-schema table-sym)
        identity (:identity (meta sym))]
    (if (nil? identity)
      (primary-key-descriptors table-sym)
      (do
        (when-not (and (vector? identity) (seq identity))
          (f/error "defret.pg :identity must be a non-empty vector"
                   {:table table-sym
                    :identity identity}))
        (mapv (fn [column]
                (or (column-descriptor schema column)
                    (f/error "defret.pg :identity contains an unknown column"
                             {:table table-sym
                              :column column
                              :identity identity})))
              identity)))))

(defn primary-key
  "gets the type of the first primary key column of a schema"
  {:added "4.0"}
  [table-sym]
  (:type (first (primary-key-descriptors table-sym))))

(defn- ret-args
  [args]
  (when-not (even? (count args))
    (f/error "defret.pg arguments must be type/symbol pairs"
             {:args args}))
  (mapv (fn [[type sym]]
          (when-not (symbol? sym)
            (f/error "defret.pg arguments must bind symbols"
                     {:type type
                      :argument sym
                      :args args}))
          {:type type :symbol sym})
        (partition 2 args)))

(defn- compatible-type?
  [actual expected]
  (or (= actual expected)
      (and (= expected :enum)
           (contains? #{:enum :text :citext} actual))
      (and (= expected :citext)
           (= actual :text))
      (and (= expected :text)
           (= actual :citext))
      (and (= expected :ref)
           (contains? #{:ref :uuid :text :citext} actual))))

(defn- validate-ret-args
  [table-sym sym descriptors args]
  (when (empty? descriptors)
    (f/error "defret.pg could not resolve an identity"
             {:table table-sym
              :symbol sym}))
  (when (not= (count descriptors) (count args))
    (f/error "defret.pg argument count does not match identity"
             {:table table-sym
              :symbol sym
              :identity (mapv :id descriptors)
              :expected (count descriptors)
              :actual (count args)}))
  (doseq [[descriptor {:keys [type symbol]}] (map vector descriptors args)]
    (when-not (compatible-type? type (:type descriptor))
      (f/error "defret.pg argument type does not match identity column"
               {:table table-sym
                :symbol sym
                :column (:id descriptor)
                :expected (:type descriptor)
                :actual type
                :argument symbol})))
  args)

(defn- ret-identity
  [sym descriptors]
  (let [explicit? (contains? (meta sym) :identity)]
    (when (or explicit? (< 1 (count descriptors)))
      (mapv :id descriptors))))

(defn lead-symbol
  "gets the lead symbol"
  {:added "4.0"}
  [args]
  (or (first (filter symbol? args))
      (f/error "No lead symbol found" {:args args})))

(defn defsel-fn
  "the defsel generator function"
  {:added "4.0"}
  [&form sym query]
  (let [{:keys [table args] :as view-map} (make-view-prep sym)
        mopts     (l/rt:macro-opts :postgres)
        query     query
        main-query  (cond-> {:returning #{:id}
                             :as :raw}
                      (not-empty query) (assoc :where query))
        main-form   (preprocess/with:macro-opts
                     [mopts]
                     (base/select-fn table main-query))
        view-query (if (not-empty query)
                     (last main-form)
                     nil)]
    (with-meta
      (template/$ (defn.pg ~(with-meta sym
                       {:%% :sql
                        :static/view (assoc view-map
                                            :type :select
                                            :query-base query
                                            :query view-query)})
             [~@args]
             [:with o :as
              ~main-form
              [:select (jsonb-agg o.id)
               :from o]]))
      (meta &form))))

(defmacro defsel.pg
  "creates a select function"
  {:added "4.0"}
  [sym & [query]]
  (defsel-fn &form sym query))

(defn defret-fn
  "the defref generator function"
  {:added "4.0"}
  [&form sym args query]
  (let [{:keys [table] :as view-map} (make-view-prep sym args)
        descriptors (identity-descriptors table sym)
        args'       (ret-args args)
        _           (validate-ret-args table sym descriptors args')
        ret-where   (zipmap (map :id descriptors)
                            (map :symbol args'))
        identity    (ret-identity sym descriptors)
        mopts       (l/rt:macro-opts :postgres)
        main-form   (l/with:macro-opts [mopts]
                      (query/query-fn table
                                      {:where ret-where
                                       :returning query
                                       :single true}))
        view-map    (cond-> (assoc view-map :type :return :query query)
                      identity (assoc :identity identity))]
    (with-meta
      (template/$ (defn.pg ~(with-meta sym
                       {:%% :sql
                        :static/view view-map})
             [~@args]
             ~main-form))
      (meta &form))))

(defmacro defret.pg
  "creates a returns function"
  {:added "4.0"}
  [sym args query]
  (defret-fn &form sym args query))

(defn view-fn
  "constructs a view function"
  {:added "4.0"}
  [qret
   qsel
   qopts]
  (let [[qret-sym qret-args] (if (vector? qret)
                               [(first qret) (eval (vec (rest qret)))]
                               [qret []])
        [qsel-sym qsel-args] (if (vector? qsel)
                               [(first qsel) (eval (vec (rest qsel)))]
                               [qsel []])
        qret-entry @@(resolve qret-sym)
        qsel-entry @@(resolve qsel-sym)
        _ (if (not= (:table (:static/view qret-entry))
                    (:table (:static/view qsel-entry)))
            (f/error "Not the same table"))
        _ (if (not= (:type (:static/view qret-entry))
                    :return)
            (f/error "Not a return type" (into {} qret-entry)))
        _ (if (not= (:type (:static/view qsel-entry))
                    :select)
            (f/error "Not a select type" (into {} qsel-entry)))
        
        ;;
        ;; RET
        ;;
        
        qret-targs  (vec (drop 1 (filter symbol? (:args (:static/view qret-entry)))))
        _ (if (not= (clojure.core/count qret-args)
                    (clojure.core/count qret-targs))
            (f/error "Args need to be the same length" {:input qret-args
                                                        :template qret-targs}))
        qret-map  (zipmap qret-targs qret-args)
        qret-query (walk/prewalk (fn [x]
                                (if (contains? qret-map x)
                                  (qret-map x)
                                  x))
                              (:query (:static/view qret-entry)))
        
        ;;
        ;; SEL
        ;;
        
        qsel-targs  (vec (filter symbol? (:args (:static/view qsel-entry))))
        _ (if (not= (clojure.core/count qsel-args)
                    (clojure.core/count qsel-targs))
            (f/error "Args need to be the same length" {:input qsel-args
                                                        :template qsel-targs}))
        qsel-map  (zipmap qsel-targs qsel-args)
        qsel-query (walk/prewalk (fn [x]
                                (if (contains? qsel-map x)
                                  (qsel-map x)
                                  x))
                              (:query (:static/view qsel-entry)))]
    [(:table (:static/view qret-entry))
     (merge {:where qsel-query
             :returning qret-query}
            qopts)]))

(defmacro view
  "view macro"
  {:added "4.0"}
  [qret
   qsel
   & [qopts]]
  (l/with:macro-opts [(l/rt:macro-opts :postgres)]
    (apply query/query-fn (view-fn qret qsel qopts))
    #_(list 'quote )))
