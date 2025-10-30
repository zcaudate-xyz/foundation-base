(ns rt.postgres.script.impl
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres.script.impl-base :as base]
            [rt.postgres.script.impl-insert :as insert]
            [rt.postgres.script.impl-main :as main]
            [rt.postgres.script.impl-update :as update]))

(l/script :postgres
  rt.postgres
  {:macro-only true})

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:select
  "flat select"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (main/t-select spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:get-field
  "gets single field"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args order-by] :as params
                 :or {as :json}}]]
   (or where (h/error "No WHERE clause" params))
   (-> (main/t-select spec-sym (merge {:single true
                                       :as :raw}
                                      params))
       (with-meta {:op/type :get}))))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:get
  "get single entry"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args order-by] :as params
                 :or {as :json}}]]
   (or where (h/error "No WHERE clause" params))
   (-> (main/t-select spec-sym (merge {:single true} params))
       (with-meta {:op/type :get}))))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:id
  "get id entry"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (or where (h/error "No WHERE clause" params))
   (main/t-id spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1
               :static/type [:integer]}
  t:count
  "get count entry"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (main/t-count spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1
               :static/type [:integer]}
  t:exists
  "TODO"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (main/t-exists spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:delete
  "flat delete"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as single args] :as params
                 :or {as :json}}]]
   (main/t-delete spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:insert
  "flat insert"
  {:added "4.0"}
  ([spec-sym data & [{:keys [returning into as args] :as params}]]
   (insert/t-insert spec-sym data params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:insert!
  "inserts without o-op"
  {:added "4.0"}
  ([spec-sym data & [{:keys [returning into as args] :as params}]]
   (insert/t-insert spec-sym data (merge {:track :ignore} params))))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:upsert
  "flat upsert"
  {:added "4.0"}
  ([spec-sym data & [{:keys [where returning into as single] :as params}]]
   (insert/t-upsert spec-sym data params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:update
  "flat update"
  {:added "4.0"}
  ([spec-sym & [{:keys [set where returning into as single args] :as params
             :or {as :json}}]]
   (update/t-update spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:update!
  "updates with o-op"
  {:added "4.0"}
  ([spec-sym & [{:keys [set where returning into as single args] :as params
             :or {as :json}}]]
   (update/t-update spec-sym (merge {:track :ignore :as :raw} params))))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:modify
  "flat modify"
  {:added "4.0"}
  ([spec-sym & [{:keys [set where returning into as single args] :as params
             :or {as :json}}]]
   (update/t-modify spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  t:fields
  "gets the fields"
  {:added "4.0"}
  ([spec-sym & [{:keys [scope path] :as params}]]
   (main/t-fields spec-sym params)))

(comment
  (./create-tests))
