(ns rt.postgres.script.graph
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres.script.graph-base :as base]
            [rt.postgres.script.graph-insert :as insert]
            [rt.postgres.script.graph-query :as query]
            [rt.postgres.script.graph-view :as view]))

(l/script :postgres
  rt.postgres
  {:macro-only true})

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:where
  "TODO"
  {:added "4.0"}
  ([spec-sym where]
   (or (not-empty where)
       (h/error "No WHERE clause" {:where where}))
   (base/id-where-fn spec-sym {:where where}))) 

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:id
  "gets only id"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (or where (h/error "No WHERE clause" params))
   (base/id-fn spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:count
  "gets only count"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (base/count-fn spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:exists
  "TODO"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
             :or {as :json}}]]
   (base/exists-fn spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:select
  "returns matching entries"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (base/select-fn spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:get
  "gets a single entry"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (or where (h/error "No WHERE clause" params))
   (-> (base/select-fn spec-sym (merge {:single true} params))
       (with-meta {:op/type :get}))))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:update
  "constructs the update form"
  {:added "4.0"}
  ([spec-sym & [{:keys [set where returning into as single args] :as params
             :or {as :json}}]]
   (base/update-fn spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:modify
  "constructs the modify form"
  {:added "4.0"}
  ([spec-sym & [{:keys [set where returning into as args] :as params
             :or {as :json}}]]
   (base/modify-fn spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:delete
  "constructs the delete form"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as] :as params
             :or {as :json}}]]
   (base/delete-fn spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  g:insert
  "constructs an insert form"
  {:added "4.0"}
  ([spec-sym data & [params]]
   (insert/insert-fn spec-sym data params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  q
  "constructs a query form"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args single order-by limit] :as params
                 :or {as :json}}]]
   (query/query-fn spec-sym params)))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  q:get
  "constructs a single query form"
  {:added "4.0"}
  ([spec-sym & [{:keys [where returning into as args order-by limit] :as params
                 :or {as :json}}]]
   (-> (query/query-fn spec-sym (merge params {:single true}))
       (with-meta {:op/type :get}))))

(defmacro.pg ^{:- [:block]
               :style/indent 0}
  view
  "constructs a view form"
  {:added "4.0"}
  [qret
   qsel
   & [qopts]]
  (apply query/query-fn (view/view-fn qret qsel qopts))
  ;;(list 'quote )
  ;;(apply list query/query-fn (view/view-fn qret qsel qopts))
  )
