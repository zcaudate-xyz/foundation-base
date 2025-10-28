(ns rt.postgres.script.impl-base
  (:require [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common :as common]
            [std.lib :as h]
            [std.lang :as l]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as book]
            [std.lang.base.emit-common :as emit-common]
            [std.lang.base.emit-data :as emit-data]
            [std.lang.base.util :as ut]
            [std.lib.schema :as schema]
            [std.string :as str]))

(def ^:dynamic *skip-checks* false)

;;
;; PREP
;;

(defn prep-entry
  "prepares data given an entry sym"
  {:added "4.0"}
  [sym {:keys [lang module snapshot] :as mopts}]
  (let [[type sym-module] (emit-common/emit-symbol-classify sym mopts)
        sym-module  (cond (not sym-module)  (h/error "No module entry for sym" {:input sym})
                          (= '- sym-module) (:id module)
                          :else sym-module)
        sym-id (symbol (name sym))
        book   (snap/get-book snapshot lang)
        entry  (or (book/get-base-entry book
                                        sym-module
                                        sym-id
                                        :code)
                   (h/error "No entry found:" {:input sym-id
                                               :type type
                                               :module sym-module}))]
    [book entry]))

(defn prep-table
  "prepares data related to the table sym"
  {:added "4.0"}
  ([sym full {:keys [lang snapshot] :as mopts}]
   (if *skip-checks*
     [{:id sym} {} (snap/get-book snapshot lang)]
     (let [[book entry]  (prep-entry sym mopts)
           {:keys [op]
            :static/keys [application schema dbtype schema-seed]} entry
           schema  (if (or (not full)
                           (not application))
                     schema-seed
                     (:schema (app/app (first application))))
           tsch    (get-in schema [:tree (keyword (name sym))])]
       [entry tsch (merge mopts {:schema schema
                                 :book book})]))))


;;
;; INPUT
;;

(defn t-input-check
  "passes the input if check is ok"
  {:added "4.0" :guard true}
  ([tsch m]
   (t-input-check tsch m {}))
  ([tsch m {:as params
            :keys [valid missing]
            :or {valid true
                 missing true
                 format false}}]
   (let [cols    (keys m)
         [_ err] (if valid
                   (schema/check-valid-columns
                    tsch
                    cols))
         _ (and err  (h/error "Invalid keys." (assoc err :data m)))
         [_ err]  (if missing
                    (schema/check-missing-columns
                     tsch
                     cols
                     (fn [{:keys [primary required sql]}]
                       (and (or required primary)
                            (nil? (:default sql))))))
         _ (and err  (h/error "Missing keys." (assoc err :data m)))
         err (if format
               (schema/check-fn-columns tsch m))
         _ (if (not-empty err)
             (h/error "Column errors." err))]
     m)))

(defn t-input-collect
  "adds schema info to keys of map"
  {:added "4.0"}
  ([tsch m]
   (h/map-entries (fn [[k v]]
                    [k [v (first (get tsch k))]])
                  m)))

(defn t-val-process
  "allows additional filters to be added"
  {:added "4.0"}
  [process out]
  (cond (vector? process)
        (reduce
         (fn [out step]
           (cond (symbol? step)
                 (list step out)

                 (vector? step)
                 (apply list (first step) out (rest step))
                 
                 :else
                 out))
         out
         process)
        
        :else
        (list process out)))

(defn t-val-fn
  "builds a js val given input"
  {:added "4.0"}
  ([tsch k v
    {:keys [coalesce] :as params}
    {:keys [book] :as mopts}]
   (let [[{:keys [type sql] :as attrs}] (get tsch k)
         enum-fn (fn [v {:keys [enum]}]
                   (cond (and (h/form? v)
                              (= (first v) '++))
                         v
                         
                         :else
                         (list '++ v (:ns enum))))
         out (cond (= :ref type)
                   (let [{:keys [link]} (:ref attrs)
                         entry  (book/get-base-entry book
                                                     (:module link)
                                                     (:id link)
                                                     :code)
                         rattrs (:static/schema-primary entry)]
                     (if (= :enum (:type rattrs))
                       (enum-fn v rattrs)
                       (if (and (list? v)
                                (= (:type rattrs) (first v)))
                         v
                         (list (:type rattrs) v))))
                   
                   (= :enum type)
                   (enum-fn v attrs)
                   
                   :else
                   (let [type (common/pg-type-alias type)]
                     (cond (and (not (nil? (:default sql)))
                                coalesce)
                           (list 'coalesce (list type v) (:default sql))

                           (and (list? v)
                                (= type (first v)))
                           v
                           
                           :else
                           (list type v))))]
     (cond->> out
       (:process sql) (t-val-process (:process sql))))))

(defn t-key-attrs-fn
  "builds a js key"
  {:added "4.0"}
  ([[k [{:keys [type]}]]]
   (cond (keyword? k)
         (if (= type :ref)
           (str (ut/sym-default-str k) "_id")
           (ut/sym-default-str k))
         
         (string? k) k
         
         (symbol? k)
         (ut/sym-default-str k))))

(defn t-key-fn
  "builds a js key"
  {:added "4.0"}
  ([tsch k]
   (let [[attrs] (get tsch k)]
     (t-key-attrs-fn [k [attrs]]))))

(defn t-sym-fn
  "builds a json access form"
  {:added "4.0"}
  ([tsch k sym]
   (let [[{:keys [type] :as spec}] (get tsch k)]
     
     (cond (#{:ref} type)
           (let [tkey  (-> spec :ref :link :id name keyword)
                 entry (l/get-entry (-> spec :ref :link))
                 id-type (-> entry :static/schema-seed :tree tkey :id (get 0) :type)]
             (list id-type
                   (list 'coalesce
                         (list :->> sym (str (ut/sym-default-str k) "_id"))
                         (list :->> (list :-> sym (ut/sym-default-str k))
                               "id"))))
           
           :else
           (let [sym  (list :->> sym (ut/sym-default-str k))]
             (cond (not (= type :enum))
                   (list (or (common/+pg-type-alias+ type)
                             type)
                         sym)

                   :else
                   (list '++ sym (:ns (:enum spec)))))))))

(defn t-build-js-map
  "builds a js map"
  {:added "4.0"}
  ([tsch input {:keys [defaults]
                :or {defaults true}
                :as params} mopts]
   (let [m    (merge (if defaults
                       (schema/get-defaults tsch))
                     (h/map-entries (fn [[k [v attrs]]]
                                        [k (t-val-fn tsch k v params mopts)])
                                      input))
         ks   (schema/order-keys tsch (keys m))]
     (apply list 'jsonb-build-object (mapcat (fn [k]
                                               [(t-key-fn tsch k) (get m k)])
                                             ks)))))

(defn t-create-fn
  "the flat create-fn"
  {:added "4.0"}
  [spec-sym m params]
  (let [[entry tsch mopts] (prep-table spec-sym false (l/macro-opts))
        _ (t-input-check tsch m params)
        input (t-input-collect tsch m)]
    (t-build-js-map tsch input params mopts)))

;;
;;
;;

(defn t-returning-cols-default
  [returning key-fn]
  (mapv (comp hash-set ut/sym-default-str) returning))

(defn t-returning-cols
  "formats returning cols given"
  {:added "4.0"}
  ([tsch returning key-fn]
   (let [map-fn (fn [{:keys [expr as]}]
                  (if as
                    [expr :as as]
                    expr))
         map-ks   (filter map? returning)
         [_ err]  (schema/check-valid-columns tsch (map :as map-ks))
         _        (if err
                    (h/error "Not valid." (assoc err :data map-ks)))
         sym-ks   (filter symbol? returning)
         data-entries (schema/get-returning tsch (filter keyword? returning))]
     (vec (concat (map (comp hash-set key-fn) data-entries)
                  (map map-fn map-ks)
                  (map (comp hash-set ut/sym-default-str) sym-ks))))))

(defn t-returning
  "formats the returning expression"
  {:added "4.0"}
  ([tsch returning]
   (t-returning tsch returning nil))
  ([tsch returning key-fn]
   (let [key-fn (or key-fn
                    t-key-attrs-fn)]
     (cond (or (#{'*} returning)
               (h/form? returning)
               (number? returning)
               (nil? returning))
           returning
           
           (set? returning)
           (list '--- (t-returning-cols tsch returning key-fn))
           
           (or (map? returning) (keyword? returning) (symbol? returning))
           (list '--- (t-returning-cols tsch #{returning} key-fn))
           
           :else
           (h/error "Not valid" {:value returning})))))

(defn t-where-hashvec-transform
  "transforms entries"
  {:added "4.0"}
  [clauses tf-fn]
  (->> clauses
       (map (fn [arr]
              (map #(apply hash-map %)
                   (partition 2 arr))))
       (map #(map tf-fn %))
       (map (fn [arr]
              (vec (apply concat (interpose [:and] (mapcat identity arr))))))
       (interpose [:or])
       (mapcat identity)
       vec
       hash-set))

(defn t-where-hashvec
  "function for where transform"
  {:added "4.0"}
  ([where tf-fn]
   (let [[acc curr] (reduce (fn [[acc curr] e]
                              (cond (or (= e [:or])
                                        (= e :or))
                                    [(conj acc curr) []]
                                    
                                    (or (= e [:and])
                                        (= e :and))
                                    [acc curr]
                                    
                                    :else
                                    [acc (conj curr e)]))
                            [[] []]
                            (first where))]
     (t-where-hashvec-transform (conj acc curr) tf-fn))))

(defn t-where-transform
  "creates a where transform"
  {:added "4.0"}
  ([tsch where {:keys [schema
                       snapshot]
                :as mopts}]
   (let [val-fn   (fn [k v op]
                    (cond (nil? v)
                          [k [:is-null]]

                          (or (map? v) (ut/hashvec? v))
                          (let [[{:keys [type ref] :as attr}] (get tsch k)
                                _  (if (not= type :ref) (h/error "Not of type :ref" {:keys k
                                                                                     :attribute attr}))
                                rtsch (get-in schema
                                              [:tree
                                               (:ns ref)])]
                            [k [op [:select #{"id"} :from (ut/sym-full (:link ref))
                                    \\ :where (t-where-transform rtsch v mopts)]]])
                          
                          :else [k [op v]]))
         entry-fn (fn [[k v]]
                    
                    (if (vector? v)
                      (let [kop (first v)]
                        (if (common/+pg-query-alias+ kop)
                         (val-fn k (second v) kop)
                         [k v]))
                      (val-fn k v :eq)))
         tf-fn   (fn [m]
                   (->> m
                        (h/map-entries entry-fn)
                        (h/map-keys (fn [k] (t-key-fn tsch k)))))]
     (cond (ut/hashvec? where)
           (t-where-hashvec where tf-fn)
           
           (map? where)
           (let [[_ err]      (schema/check-valid-columns tsch (keys where))
                 _   (if (and err
                              (not *skip-checks*))
                       (h/error "Invalid columns." (assoc err :data where)))]
             (tf-fn where))
           
           :else (h/error "Not Allowed" {:value where})))))

;;
;; append wrappers
;;

(defn t-wrap-json
  "wraps a json return to the statement"
  {:added "4.0"}
  ([form as js-out into field]
   (cond (or (#{:json :jsonb} as)
             (and (#{:record} as)
                  into))
         `[:with ~'j-ret :as ~form
           \\ :select (~js-out
                       ~(cond-> 'j-ret
                          field (#(list '. % #{field}))))
           :from ~'j-ret]
         
         :else
         form)))

(defn t-wrap-where
  "adds a `where` clause"
  {:added "4.0"}
  ([form where tsch {:keys [newline where-args]} mopts]
   (cond-> form
     (and where newline) (conj \\)
     where      (conj :where (t-where-transform tsch where mopts))
     where-args (concat where-args)
     :then vec)))

(defn t-wrap-order-by
  "adds an `order-by` clause"
  {:added "4.0"}
  ([form cols tsch {:keys [newline]}]
   (let [[_ err] (if (vector? cols)
                   (schema/check-valid-columns tsch cols))
         _ (if err (h/error "Not valid." (assoc err :data cols)))]
     (cond-> form
       (and cols newline) (conj \\)
       cols  (conj :order-by (if (vector? cols)
                               (list 'quote (mapv (fn [k]
                                                    #{(t-key-fn tsch k)})
                                                  cols))
                               cols))))))

(defn t-wrap-order-sort
  "determines asc or desc key"
  {:added "4.0"}
  ([form sort tsch {:keys [newline]}]
   (cond-> form sort  (conj sort))))

(defn t-wrap-limit
  "adds a `limit` clause"
  {:added "4.0"}
  ([form limit {:keys [newline]}]
   (cond-> form
     (and limit newline) (conj \\)
     limit (conj :limit limit))))

(defn t-wrap-offset
  "adds a `offset` clause"
  {:added "4.0"}
  ([form offset {:keys [newline]}]
   (cond-> form
     (and offset newline) (conj \\)
     offset (conj :offset offset))))

(defn t-wrap-into
  "adds `into` clause"
  {:added "4.0"}
  ([form into {:keys [newline]}]
   (cond-> form
     (and into newline) (conj \\)
     into (conj :into into))))

(defn t-wrap-returning
  "adds `returning` clause"
  {:added "4.0"}
  [form returning {:keys [newline]}]
  (cond-> form
    (and returning newline) (conj \\)
    returning (conj :returning returning)))

(defn t-wrap-group-by
  "adds `group-by` clause"
  {:added "4.0"}
  ([form group-by {:keys [newline]}]
   (cond-> form
     (and group-by newline) (conj \\)
     group-by (conj :group-by group-by))))

(defn t-wrap-args
  "adds `additional` args"
  {:added "4.0"}
  ([form args {:keys [newline]}]
   (cond-> form
     (and args newline) (conj \\)
     args (concat args)
     :then vec)))


