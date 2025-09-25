(ns rt.postgres.script.impl-main
  (:require [std.lib :as h]
            [std.string :as str]
            [std.lib.schema :as schema]
            [std.lang :as l]
            [std.lang.base.util :as ut]
            [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres.script.impl-base :as base]))

;;
;; select
;;

(defn t-select-raw
  "contructs an select form with prep"
  {:added "4.0"}
  ([[entry tsch mopts]
    {:keys [where where-args returning into field
            as args single order-by order-sort group-by limit offset key-fn]
     :as params
     :or {as :json}}]
   (let [table-sym (ut/sym-full entry)
         returning (base/t-returning tsch (or returning
                                              (if (not (#{:raw} as))
                                              :*/default
                                              '*))
                                     key-fn)
         select  [:select returning :from table-sym]
         js-out  (if single 'to-jsonb 'jsonb-agg)
         limit   (if single 1 limit)]
     (-> select
         (base/t-wrap-where where tsch
                            {:newline true
                             :where-args where-args}
                            mopts)
         (base/t-wrap-group-by group-by {:newline true})
         (base/t-wrap-order-by order-by tsch {:newline true})
         (base/t-wrap-order-sort order-sort tsch {})
         (base/t-wrap-limit limit {:newline true})
         (base/t-wrap-offset offset {})
         (base/t-wrap-args args {})
         (base/t-wrap-json as js-out into field)
         (base/t-wrap-into into {})
         (with-meta {:op/type :select})))))

(defn t-select
  "contructs an select form with prep"
  {:added "4.0"}
  ([spec-sym {:keys [where returning into as args single order-by limit key-fn] :as params
          :or {as :json}}]
   (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))]
     (t-select-raw [entry tsch mopts] params))))

;;
;; id
;;

(defn t-id-raw
  "contructs an id form with prep"
  {:added "4.0"}
  ([[entry tsch mopts]  params]
   (-> (t-select-raw [entry tsch mopts]
                     (merge {:single true
                             :as :raw
                             :returning #{:id}}
                            params))
       (with-meta {:op/type :id}))))

(defn t-id
  "contructs an id form"
  {:added "4.0"}
  ([spec-sym params]
   (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))]
     (t-id-raw [entry tsch mopts] params))))

;;
;; count
;;

(defn t-count-raw
  "constructs a count form with prep"
  {:added "4.0"}
  ([[entry tsch mopts]  params]
   (-> (t-select-raw [entry tsch mopts]
                     (merge {:as :raw
                             :returning '(count *)}
                            params))
       (with-meta {:op/type :id}))))

(defn t-count
  "create count statement"
  {:added "4.0"}
  ([spec-sym params]
   (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))]
     (t-count-raw [entry tsch mopts] params))))

;;
;; delete
;;

(defn t-delete-raw
  "contructs a delete form with prep"
  {:added "4.0"}
  ([[entry tsch mopts] {:keys [where returning into as single args] :as params
                        :or {as :json}}]
   (let [{:static/keys [tracker]} entry
         table-sym (ut/sym-full entry)
         params      (tracker/add-tracker params tracker table-sym :delete)
         returning (base/t-returning tsch (or returning
                                              (if (not (#{:raw} as))
                                                :*/all)))
         delete [:delete :from table-sym]
         js-out (if single 'to-jsonb 'jsonb-agg)]
     (-> delete
         (base/t-wrap-where where tsch {} mopts)
         (base/t-wrap-args args {})
         (base/t-wrap-returning returning {:newline true})
         (base/t-wrap-json as js-out into nil)
         (base/t-wrap-into into {})
         (with-meta {:op/type :delete})))))

(defn t-delete
  "contructs an delete form"
  {:added "4.0"}
  ([spec-sym {:keys [where returning into as single args] :as params
              :or {as :json}}]
   (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))]
     (t-delete-raw [entry tsch mopts] params))))


;;
;; fields
;;

(defn t-fields-raw
  "contructs a delete form with prep"
  {:added "4.0"}
  ([[entry tsch mopts] {:keys [scope path] :as params
                        :or {path [:web :modify]}}]
   (cond scope
         (schema/get-returning tsch scope)

         :else
         (->> tsch
              (sort-by (fn [[_ [{:keys [order]}]]]
                         order))
              (keep (fn [[k [{:keys [primary unique order]}]]]
                      (if (and (not (or primary #_unique))
                               order)
                        (str k))))))))

(defn t-fields
  [spec-sym {:keys [scope path] :as params}]
  (let [[entry tsch mopts] (base/prep-table spec-sym false (l/macro-opts))]
    (t-fields-raw [entry tsch mopts] params)))
