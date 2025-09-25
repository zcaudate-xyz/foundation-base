(ns rt.postgres.script.impl-update
  (:require [std.lib :as h]
            [std.string :as str]
            [std.lib.schema :as schema]
            [std.lang :as l]
            [std.lang.base.util :as ut]
            [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres.script.impl-base :as base]
            [rt.postgres.grammar.common :as common]))

;;
;; Symbol update
;;

(defn t-update-symbol
  "constructs with symbol"
  {:added "4.0"}
  ([tsch sym {:keys [columns
                     coalesce] :as params} mopts]
   (let [trkm (tracker/tracker-map-modify params)
         columns (set (or columns
                          (->> tsch
                               (keep (fn [[k [{:keys [primary unique order]}]]]
                                       (if (and (not (or primary #_unique))
                                                order)
                                         k))))))
         all-columns (vec (h/union columns (set (keys trkm))))
         [_ err]  (schema/check-valid-columns tsch all-columns)
         _  (if err (h/error "Not Valid." (assoc err :data all-columns)))
         ks   (schema/order-keys tsch all-columns)
         body (mapv (fn [k]
                      (let [trkv    (get trkm k)
                            {:keys [sql] :as attrs} (get-in tsch [k 0])
                            col #{(base/t-key-fn tsch k)}
                            inputs (cond-> []
                                     (get columns k) (conj (base/t-sym-fn tsch k sym))
                                     :then (conj col))
                            out (cond trkv
                                      (list (common/pg-type-alias (-> (get tsch k) first :type))
                                            trkv)
                                      
                                      (false? coalesce)
                                      (first inputs)
                                      
                                    (and (h/form? (first inputs))
                                         (= (ffirst inputs) 'coalesce))
                                    (concat (first inputs) (rest inputs))
                                    
                                    :else
                                    (apply list 'coalesce inputs))
                            out (cond->> out
                                  (:process sql) (base/t-val-process (:process sql)))]
                        (list '== #{(base/t-key-fn tsch k)} out)))
                    ks)]
     (list '--- body))))

(defn t-update-map
  "constructs with map"
  {:added "4.0"}
  ([tsch m params mopts]
   (let [m  (merge (tracker/tracker-map-modify params) m)
         [_ err]  (schema/check-valid-columns tsch (keys m))
         _  (if err (h/error "Not Valid." (assoc err :data m)))
         entry-fn  (fn [_ k] (get m k))
         m       (h/map-entries (fn [[k v]]
                                  [k (base/t-val-fn tsch k v params mopts)])
                                m)
         ks      (sort-by (fn [k] (-> (get tsch k)
                                      first
                                      :order))
                          (keys m))]
     (->> (mapv (fn [k]
                  (let [{:keys [sql] :as attrs} (get-in tsch [k 0])
                        out (get m k)
                        #_#_out (cond->> out
                              (:process sql) (base/t-val-process (:process sql)))]
                    (list '==
                          #{(base/t-key-fn tsch k)}
                          out)))
                ks)
          (list '---)))))

(defn t-update-raw
  "contructs an update form with prep"
  {:added "4.0"}
  ([[entry tsch mopts] {:keys [set where returning into as single args] :as params
                        :or {as :json}}]
   (let [{:static/keys [tracker]} entry
         table-sym (ut/sym-full entry)
         params    (tracker/add-tracker params tracker table-sym :update)
         returning (base/t-returning tsch (or returning
                                              (if (not (#{:raw} as))
                                                :*/default)))
         form  (if (map? set)
                 (t-update-map tsch set params mopts)
                 (t-update-symbol tsch set params mopts))
         op   `[:update ~table-sym :set ~form]
         js-out (if single 'to-jsonb 'jsonb-agg)]
     (-> op
         (base/t-wrap-where where tsch {} mopts)
         (base/t-wrap-args args {})
         (base/t-wrap-returning returning {:newline true})
         (base/t-wrap-json as js-out into nil)
         (base/t-wrap-into into {})
         (with-meta {:op/type :update})))))

(defn t-update
  "contructs an update form"
  {:added "4.0"}
  ([spec-sym {:keys [set where returning into as single args] :as params
              :or {as :json}}]
   (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))]
     (t-update-raw [entry tsch mopts] params))))

(defn t-modify-raw
  "contructs an upsert form with prep"
  {:added "4.0"}
  ([[entry tsch mopts] {:keys [set where returning into as single] :as params
              :or {as     :json
                   single true}}]
   (let [table-sym (ut/sym-full entry)
         returning (base/t-returning tsch (or returning '*))
         form   (t-update-raw [entry tsch mopts] 
                              (assoc params
                                     :set set
                                     :returning returning
                                     :as :raw
                                     :into 'u-ret))
         assert '(if [:not (exists [:select u-ret])]
                   [:raise-exception (% "Record Not Found")])
         js-out     (if single 'to-jsonb 'jsonb-agg)
         return-fn  (fn [e]
                      (if e
                        (if (#{:json :jsonb} as)
                          [[:select (list js-out 'u-ret) :into e]]
                          [[:select 'u-ret :into e]])
                        []))
         form-fn    (fn [e]
                      `(~'let [(~'++ ~'u-ret ~table-sym) ~form
                               ~'_ ~assert]
                        ~@(return-fn e)))]
     (with-meta (form-fn into)
       {:assign/fn form-fn}))))

(defn t-modify
  "constructs a modify form"
  {:added "4.0"}
  ([spec-sym {:keys [set where returning into as single] :as params
              :or {as     :json
                   single true}}]
   (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))]
     (t-modify-raw [entry tsch mopts] params))))
