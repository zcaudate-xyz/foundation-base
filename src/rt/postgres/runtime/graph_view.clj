(ns rt.postgres.runtime.graph-view
  (:require [rt.postgres.runtime.graph-base :as base]
            [rt.postgres.runtime.graph-query :as query]
            [std.lang :as l]
            [std.lang.base.emit-preprocess :as preprocess] [std.lang.base.preprocess-base :as preprocess-base]
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
        ret-id (lead-symbol args)
        mopts  (l/rt:macro-opts :postgres)
        main-form (l/with:macro-opts [mopts]
                    (query/query-fn table
                                    {:where {:id ret-id}
                                     :returning query
                                     :single true}))]
    (with-meta
      (template/$ (defn.pg ~(with-meta sym
                       {:%% :sql
                        :static/view (assoc view-map
                                            :type :return
                                            :query query)})
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
