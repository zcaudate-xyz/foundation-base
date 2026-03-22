(ns std.lib.schema
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.lib.schema.base :as base]
            [std.lib.schema.impl :as impl]
            [std.lib.schema.ref :as ref])
  (:refer-clojure :exclude [flatten]))

(h/intern-in impl/schema
             impl/schema?
             base/check-scope
             ref/with:ref-fn)

(defn expand-scopes
  "expand `*` keys into `-` keys"
  {:added "4.0"}
  [k]
  (get base/+scope+ k))

(defn linked-primary
  "gets the linked primary column"
  {:added "4.0"}
  ([tsch k schema]
   (let [link  (-> tsch k first :ref :link)
         rid   (keyword (name (:id link)))
         rsch  (get (:tree schema) rid)
         [k [attrs]] (first (filter (fn [[k [{:keys [primary] :as m}]]]
                                (if primary [k m]))
                              rsch))]
     (if (nil? attrs)
       (h/error "Column not found" {:link link
                                    :key k
                                    :tree rsch})
       attrs))))

(defn order-keys
  "order keys given schema"
  {:added "4.0"}
  ([tsch ks]
   (sort-by (fn [k] (or (-> tsch k (first) :order)
                        Integer/MAX_VALUE))
            ks)))

(defn get-defaults
  "get defaults in the schema"
  {:added "4.0"}
  ([tsch]
   (coll/keep-vals (fn [[{:keys [sql]}]] (:default sql))
                   (comp not nil?)
                   tsch)))


;;
;; assertions
;;

(defn check-valid-columns
  "check if columns are valid"
  {:added "4.0"}
  ([tsch columns]
   (let [diff (clojure.set/difference (set (filter keyword? columns))
                                      (set (keys tsch)))]
     (if (empty? diff)
       [true]
       [false {:not-allowed diff
               :valid (set (keys tsch))}]))))

(defn check-missing-columns
  "check if columns are missing"
  {:added "4.0"}
  ([tsch columns required-fn]
   (let [required (set (keep (fn [[k [attrs]]]
                               (if (required-fn attrs) k))
                             tsch))
         diff (clojure.set/difference required
                                      (set columns))]
     (if (empty? diff)
       [true]
       [false {:missing diff
               :required required}]))))

(defn check-fn-columns
  "perform a check using the `:check` key
 
   (schema/check-fn-columns -tsch- {:status 'a})
   => {}"
  {:added "4.0"}
  ([tsch data]
   (let [checks (coll/keep-vals (fn [[{:keys [check]}]]
                               check)
                             tsch)]
     (->> data
          (keep (fn [[k v]]
                  (if-let [check (get checks k)]
                    (if (not (h/suppress (check v)))
                      [k {:key k
                          :value v
                          :check check
                          :message "Check failed for input" }]))))
          (into {})))))

(defn get-returning
  "collects the returning ids and columns
   
   (->> (schema/get-returning -tsch-
                       [:*/data :cache])
        (map first))
   => '(:id :status :name :cache :time-created :time-updated)"
  {:added "4.0"}
  ([tsch returning]
   (let [scope-ks (filter (fn [x] (and (keyword? x) (namespace x))) returning)
         col-ks   (set (filter (fn [x] (and (keyword? x) (nil? (namespace x)))) returning))
         [_ err]  (check-valid-columns tsch col-ks)
         _ (if err (h/error "Not valid." (assoc err :data returning)))
         union-fn (fn union-fn [s]
                    (apply clojure.set/union
                           (map (fn [k]
                                  (if (= "*" (namespace k))
                                    (union-fn (expand-scopes k))
                                    #{k}))
                                s))) 
         all-fn (union-fn scope-ks)]
     (->> (seq tsch)
          (sort-by (fn [[k [{:keys [order]}]]]
                     [(or order Integer/MAX_VALUE) k]))
          (filter  (fn [[k [{:keys [scope]}]]]
                     (or (all-fn scope)
                         (col-ks k))))))))
