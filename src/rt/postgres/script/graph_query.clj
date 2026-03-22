(ns rt.postgres.script.graph-query
  (:require [clojure.set]
            [rt.postgres.grammar.common :as common]
            [rt.postgres.script.graph-base :as base]
            [rt.postgres.script.impl-base :as impl]
            [rt.postgres.script.impl-main :as main]
            [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.util :as ut]
            [std.lib.foundation]
            [std.lib.schema :as schema]))

(def +t-additional-scope+
  {:*/linked     #{:-/linked}
   :*/all        #{:-/linked}
   :*/everything #{:-/linked}})

(def ^:dynamic *top* false)

(defn table-col-token
  "constructs a table ref token"
  {:added "4.0"}
  ([table-sym table-key]
   (with-meta
     (list '. table-sym #{(str (ut/sym-default-str table-key) "_id")})
     {:fn/value true})))

(defn table-id-token
  "constructs a table id token"
  {:added "4.0"}
  ([table-sym]
   (with-meta
     (list '. table-sym #{"id"})
     {:fn/value true})))

(defn returning-block
  "constructs a returning block"
  {:added "4.0"}
  [entry attrs where returning params query-fn {:keys [schema
                                                 book
                                                 snapshot]
                                          :as mopts}]
  (let [{:keys [ident type ref unique]} attrs
        {:keys [link]} ref
        k     (keyword (name ident))
        nstr  (ut/sym-default-str (or (:name params)
                                      (name ident)))
        table-sym (ut/sym-full entry)
        query (case (:type ref)
                :forward {:id (table-col-token table-sym k)}
                :reverse {(:rval ref) (table-id-token table-sym)})
        nentry (book/get-base-entry book
                                    (:module link)
                                    (:id link)
                                    (:section link))
        ntsch   (get-in schema [:tree (keyword (name (:id link)))])
        where-clause (cond (nil? where) query

                           (clojure.set/intersection (set (keys where))
                                           (set (keys query)))
                           #{(vec (concat
                                   (first query)
                                   [:and]
                                   (mapcat identity (base/where-fn ntsch
                                                                   where
                                                                   mopts))))}
                           :else
                           (base/where-fn {k [attrs]}
                                          (merge where query)
                                          mopts))]
    (list '% [(query-fn
               [nentry ntsch mopts]
               (merge {:where where-clause
                       :returning (cond (nil? returning)
                                        :*/info
                                        
                                        :else
                                        returning)
                       :single (case (:type ref)
                                 :forward true
                                 :reverse unique)}
                      params))
              :as #{nstr}])))

(defn returning-map-markers
  "prepares the map markers"
  {:added "4.0"}
  ([entry tsch markers query-fn mopts]
   (let [ks   (set (map first markers))
         [_ err]    (schema/check-valid-columns tsch ks)
         _ (if err (std.lib.foundation/error "Not valid." (assoc err :data markers)))]
     (mapv (fn [[k & [where returning params] :as arr]]
             (let [[where returning params] (if (map? where)
                                              [where returning params]
                                              [{} where returning])
                   [{:keys [type ref unique] :as attrs}] (get tsch k)
                   _  (if-not (= :ref type)
                        (std.lib.foundation/error "Column not a ref" {:column k
                                                     :attrs attrs}))]
               {:expr (returning-block entry
                                       attrs
                                       where
                                       returning
                                       params
                                       query-fn
                                       mopts)}))
           markers))))

(defn- reverse-keys
  "get linked keys"
  {:added "4.0"}
  ([tsch]
   (->> (seq tsch)
        (keep  (fn [[k [{:keys [ref]}]]]
                 (if (= :reverse (:type ref)) k)))
        (set))))

(defn returning-all-markers
  "returns all markers for return"
  {:added "4.0"}
  ([entry tsch returning query-fn mopts]
   (let [returning (if (keyword? returning)
                     #{returning}
                     returning)
         pure-markers   (filter (fn [x] (keyword? x)) returning)
         vec-markers    (filter (fn [x] (vector? x)) returning)
         sym-markers    (filter (fn [x] (symbol? x)) returning)
         custom-markers (filter (fn [x] (map? x)) returning)
         pure-ks        (set (map first (schema/get-returning tsch pure-markers)))
         rev-ks         (reverse-keys tsch)
         linked-ks      (if (not-empty (clojure.set/intersection #{:*/everything
                                                         :*/all
                                                         :*/linked
                                                         :-/linked}
                                                       (set pure-markers)))
                          rev-ks)
         data-ks        (apply disj pure-ks
                               (concat (map first vec-markers)
                                       rev-ks))
         ref-ks         (map vector (clojure.set/union (clojure.set/intersection pure-ks
                                                             rev-ks)
                                             linked-ks))
         
         map-markers (returning-map-markers entry
                                            tsch
                                            (concat vec-markers
                                                    ref-ks)
                                            query-fn
                                            mopts)]
     (set (concat data-ks
                  map-markers
                  custom-markers
                  sym-markers)))))

(defn query-raw-fn
  "constructs a query form with prep"
  {:added "4.0"}
  ([[entry tsch mopts] {:keys [where returning] :as params}]
   (let [where   (base/where-fn tsch where mopts)
         returning (if (symbol? returning)
                     returning
                     (returning-all-markers entry
                                            tsch
                                            (or returning :*/default)
                                            (fn [[entry tsch mopts] params]
                                              (list 'quote (list (query-raw-fn [entry tsch mopts] params))))
                                            mopts))]
     (main/t-select-raw [entry tsch mopts]
                        (assoc params
                               :where where
                               :returning returning)))))

(defn query-fn
  "constructs a query form"
  {:added "4.0"}
  ([spec-sym {:keys [where returning] :as params}]
   (let [[entry tsch mopts] (impl/prep-table spec-sym true (l/macro-opts))]
     (query-raw-fn [entry tsch mopts] params))))



(comment
  (./create-tests)
  (./import))


