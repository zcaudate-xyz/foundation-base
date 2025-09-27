(ns rt.postgres.script.impl-insert
  (:require [std.lib :as h]
            [std.string :as str]
            [std.lib.schema :as schema]
            [std.lang :as l]
            [std.lang.base.util :as ut]
            [std.lang.base.emit-preprocess :as preprocess]
            [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres.script.impl-base :as base]))

(defn t-insert-form
  "insert form"
  {:added "4.0"}
  ([tsch ks vs]
   (let [cols (list '>-<
                    (mapv (fn [k] #{(base/t-key-fn tsch k)})
                          ks))]
     [cols :values (list '>-<
                         (vec vs))])))

(defn t-insert-symbol
  "constructs an insert symbol form"
  {:added "4.0"}
  ([tsch sym cols params mopts]
   (let [trkm (tracker/tracker-map-in params)
         cols (or cols  (keys tsch))
         cols (vec (set (concat cols (keys trkm))))
         [_ err]  (schema/check-valid-columns tsch cols)
         _ (if err (h/error "Invalid columns." (assoc err :data cols)))
         cols (filter #(-> % tsch first :order) cols)
         ks   (schema/order-keys tsch cols)
         vs   (map (fn [k]
                     (or (get trkm k)
                         (base/t-sym-fn tsch k sym)))
                   ks)
         vs   (map (fn [k v]
                     (base/t-val-fn tsch k v params mopts))
                   ks
                   vs)]
     (t-insert-form tsch ks vs))))

(defn t-insert-map
  "constructs an insert map form"
  {:added "4.0"}
  ([tsch m params mopts]
     (let [m  (merge (tracker/tracker-map-in params) m)
           _  (base/t-input-check tsch m)
           ks (schema/order-keys tsch (keys m))
           vs (map (fn [k]
                     (base/t-val-fn tsch k (get m k) params mopts))
                   ks)]
       (t-insert-form tsch ks vs))))

(defn t-insert-record
  "constructs a record insert form"
  {:added "4.0"}
  ([table-sym data params]
   (let [trkm  (tracker/tracker-map-in params)
         data  (if trkm
                 (list '|| data trkm)
                 data)]
     [:select '* :from (list 'jsonb-populate-record (list '++ nil table-sym) data)])))

(defn t-insert-raw
  "contructs an insert form with prep"
  {:added "4.0"}
  ([[entry tsch mopts] data {:keys [returning into as args] :as params
                             :or {as :json}}]
   (let [{:static/keys [tracker]} entry
         table-sym (ut/sym-full entry)
         params    (tracker/add-tracker params tracker table-sym :insert)
         returning (base/t-returning tsch (or returning
                                              (if (or (not (#{:record :raw} as))
                                                      into)
                                                :*/default)))
         
         [js-out body] (cond (map? data)
                             ['to-jsonb (t-insert-map tsch data params mopts)]
                             
                             (symbol? data)
                             (if (= :record as)
                               ['to-jsonb (t-insert-record table-sym data params)]
                               ['to-jsonb (t-insert-symbol tsch data (:columns params) params mopts)])

                             :else (h/error "Invalid data type." {:input data}))
         insert  `[:insert-into ~table-sym ~@body]]
     (-> insert
         (base/t-wrap-args args {})
         (base/t-wrap-returning returning {})
         (base/t-wrap-json as js-out into nil)
         (base/t-wrap-into into {})
         (with-meta {:op/type :insert})))))

(defn t-insert
  "constructs an insert form"
  {:added "4.0"}
  ([spec-sym data & [{:keys [returning into as args] :as params
                      :or {as :json}}]]
   (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))
         {:keys [book]} mopts]
     (t-insert-raw [entry tsch mopts]
                   (first (preprocess/to-staging data
                                                 (:grammar book)
                                                 (:modules book)
                                                 mopts))
                   params))))

;;
;; upsert
;;

(defn t-upsert-raw
  "contructs an upsert form with prep"
  {:added "4.0"}
  ([[entry tsch mopts] data {:keys [where returning into as single on-conflict] :as params}]
   (let [pkeys  (keep (fn [[k [attr]]]
                        (if (:primary attr) k))
                      tsch)
         ckeys  (cond (map? data) (keys data)
                      (symbol? data) (keys tsch)
                      :else (h/error "Invalid data type." {:input data}))
         [_ ckeys]  (base/t-returning tsch (set ckeys))
         ckrow  (map (fn [col] (list '. (list :- "EXCLUDED") col))
                     ckeys)
         cargs  [:do-update
                 :set (list 'quote (apply list ckeys))
                 := (cons 'row ckrow)]
         conflicted (list 'quote (apply list (second (base/t-returning
                                                      tsch
                                                      (or on-conflict
                                                          (set pkeys))))))
         args   (vec (concat [:on-conflict conflicted] cargs))]
     (-> (t-insert-raw [entry tsch mopts]
                       data
                       (assoc params :args args))
         (with-meta {:op/type :upsert})))))

(defn t-upsert
  "constructs an upsert form"
  {:added "4.0"}
  ([spec-sym data & [{:keys [where returning into as single] :as params}]]
   (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))]
     (t-upsert-raw [entry tsch mopts] data params))))
