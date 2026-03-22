(ns rt.postgres.script.graph-view
  (:require [rt.postgres.grammar.common-application :as app]
            [rt.postgres.script.graph-base :as base]
            [rt.postgres.script.graph-query :as query]
            [std.lang :as l]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.library-snapshot :as snap]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.template]
            [std.lib.walk]
            [std.string.case]))

;;
;; select
;;

(defn create-defaccess-prep
  "creates a defaccess prep"
  {:added "4.0"}
  [sym access]
  (let [{:keys [roles forward reverse]} access
        [forward-table forward-clause] (std.lib.walk/postwalk std.lib.foundation/resolve-namespaced forward)
        [reverse-table reverse-clause] (std.lib.walk/postwalk std.lib.foundation/resolve-namespaced reverse)
        module  (l/rt:module :postgres)
        mopts   (l/rt:macro-opts :postgres)
        schema  (:schema (app/app (first (:application (:static module)))))
        [forward-form
         reverse-form] (l/with:macro-opts [mopts]
                         [(base/select-fn forward-table {:schema schema
                                                         :where forward-clause})
                          (base/select-fn reverse-table {:schema schema
                                                         :where reverse-clause})])]
    {:symbol (symbol (name (std.lib.env/ns-sym)) (name sym))
     :forward {:table forward-table
               :clause forward-clause
               :form forward-form}
     :reverse {:table reverse-table
               :clause reverse-clause
               :form reverse-form}
     :roles roles}))

(defmacro defaccess.pg
  "creates a defaccess macro"
  {:added "4.0"}
  [sym access]
  (let [access (create-defaccess-prep sym access)]
    (list 'def sym (list 'quote access))))

(defn make-view-access
  "creates view access"
  {:added "4.0"}
  [access table-sym]
  (if access
    (let [access-var (resolve (first access))
          access-map @access-var
          query-fn (fn [k]
                     (let [{:keys [table] :as e} (get access-map k)]
                       (if (= table table-sym)
                         [k e])))
          [rel entry] (or (query-fn :forward)
                          (query-fn :reverse)
                          (std.lib.foundation/error "Access not valid" {:access access
                                                       :table table-sym}))]
      {:symbol   (std.lib.foundation/var-sym access-var)
       :relation rel
       :query    entry
       :roles    (:roles access-map)})))

(defn make-view-prep
  "preps view access"
  {:added "4.0"}
  [sym & [rargs]]
  (let [{:keys [scope args tag access guards autos] :as msym} (meta sym)
        [table] (:- msym)
        table-key  (keyword (name table))
        table-sym  (std.lib.foundation/var-sym (resolve table))
        guards     (std.lib.walk/postwalk std.lib.foundation/resolve-namespaced guards)
        autos      (std.lib.walk/postwalk std.lib.foundation/resolve-namespaced autos)
        tag        (or tag
                       (-> (clojure.core/subs (std.string.case/camel-case (str sym))
                                              (clojure.core/count (name table)))
                           (std.string.case/spear-case)))]
    {:table  table-sym
     :key    table-key
     :scope  scope
     :args   (or rargs args [])
     :guards guards
     :autos  autos
     :tag    tag
     :access access}))

(defn primary-key
  "gets the primary key of a schema"
  {:added "4.0"}
  [table-sym]
  (let [table-key  (keyword (name table-sym))]
    (->> @@(resolve table-sym)
         :static/schema-seed
         :tree
         table-key
         (keep (fn [[k [{:keys [primary type]}]]]
                 (if primary type)))
         (first))))

(defn lead-symbol
  "gets the lead symbol"
  {:added "4.0"}
  [args]
  (or (first (filter symbol? args))
      (std.lib.foundation/error "No lead symbol found" {:args args})))

(defn defsel-fn
  "the defsel generator function"
  {:added "4.0"}
  [&form sym query]
  (let [{:keys [table scope args] :as view-map} (make-view-prep sym)
        view-access (make-view-access (:access view-map) table)
        mopts     (l/rt:macro-opts :postgres)
        query     (or query
                      (if (and (:personal scope)
                               view-access)
                        (std.lib.walk/postwalk-replace {'<%> (lead-symbol args)}
                                            (:clause (:query view-access)))))
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
      (std.lib.template/$ (defn.pg ~(with-meta sym
                       {:%% :sql
                        :static/view (assoc view-map
                                            :type :select
                                            :query-base query
                                            :query view-query
                                            :access view-access)})
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
  (let [{:keys [table scope access] :as view-map} (make-view-prep sym args)
        view-access (make-view-access access table)
        ret-id (lead-symbol args)
        mopts  (l/rt:macro-opts :postgres)
        main-form (l/with:macro-opts [mopts]
                    (query/query-fn table
                                    {:where {:id ret-id}
                                     :returning query
                                     :single true}))]
    (with-meta
      (std.lib.template/$ (defn.pg ~(with-meta sym
                       {:%% :sql
                        :static/view (assoc view-map
                                            :type :return
                                            :query query
                                            :access view-access)})
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
            (std.lib.foundation/error "Not the same table"))
        _ (if (not= (:type (:static/view qret-entry))
                    :return)
            (std.lib.foundation/error "Not a return type" (into {} qret-entry)))
        _ (if (not= (:type (:static/view qsel-entry))
                    :select)
            (std.lib.foundation/error "Not a select type" (into {} qsel-entry)))
        
        ;;
        ;; RET
        ;;
        
        qret-targs  (vec (drop 1 (filter symbol? (:args (:static/view qret-entry)))))
        _ (if (not= (clojure.core/count qret-args)
                    (clojure.core/count qret-targs))
            (std.lib.foundation/error "Args need to be the same length" {:input qret-args
                                                        :template qret-targs}))
        qret-map  (zipmap qret-targs qret-args)
        qret-query (std.lib.walk/prewalk (fn [x]
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
            (std.lib.foundation/error "Args need to be the same length" {:input qsel-args
                                                        :template qsel-targs}))
        qsel-map  (zipmap qsel-targs qsel-args)
        qsel-query (std.lib.walk/prewalk (fn [x]
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
