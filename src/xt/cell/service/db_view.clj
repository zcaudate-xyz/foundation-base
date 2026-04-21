(ns xt.cell.service.db-view
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.base-scope :as base-scope]
             [xt.db.base-view :as base-view]]
   :export  [MODULE]})

(defn.xt get-views
  "gets the db views"
  {:added "4.0"}
  [db]
  (return (or (xt/x:get-key db "views")
              {})))

(defn.xt get-schema
  "gets the db schema"
  {:added "4.0"}
  [db]
  (return (or (xt/x:get-key db "schema")
              {})))

(defn.xt view-query-return-entry
  "creates the return entry for `return-query` key"
  {:added "4.0"}
  [table return-query data-only]
  (return
   {:input []
    :return "jsonb"
    :flags {}
    :view {:table table
           :type "return"
           :query  (:? data-only
                       (xt/x:arr-filter return-query xt/x:is-string?)
                       return-query)
           :access {:roles {}}
           :guards []}}))

(defn.xt view-query-return-combined
  "creates the combined return entry for `return-query` key"
  {:added "4.0"}
  [table return-entry return-query data-only]
  (var query-mixin (:? data-only
                       (xt/x:arr-filter return-query xt/x:is-string?)
                       return-query))
  (var query (xtd/get-in return-entry ["view" "query"]))
  (xt/x:arr-concat query query-mixin)
  (return return-entry))

(defn.xt view-query-entries
  "gets the select and return entries"
  {:added "4.0"}
  [db table qm data-only]
  (var #{select-method
         return-method
         return-query} qm)
  (var views (-/get-views db))
  (var select-entry (:? (xt/x:not-nil? select-method)
                        (xtd/get-in views [table "select" select-method])))
  (var return-entry nil)
  (cond (and return-method
             return-query)
        (:= return-entry (-/view-query-return-combined
                          table
                          (xtd/clone-nested (xtd/get-in views [table "return" return-method]))
                          return-query
                          data-only))

        return-method
        (:= return-entry (xtd/get-in views [table "return" return-method]))

        return-query
        (:= return-entry (-/view-query-return-entry table return-query data-only)))
  (return {:select-entry select-entry
           :return-entry return-entry}))

(defn.xt view-triggers
  "gets the triggers for a given view"
  {:added "4.0"}
  [db table qm]
  (var qe (-/view-query-entries db table qm nil))
  (var #{select-entry
         return-entry} qe)
  (var schema (-/get-schema db))
  (var select-triggers (:? select-entry
                           (base-scope/get-query-tables
                            schema table
                            (xt/x:get-path select-entry ["view" "query"])
                            {})
                           {}))
  (var return-triggers (:? return-entry
                           (base-scope/get-linked-tables
                            schema table
                            (xt/x:get-path return-entry ["view" "query"]))
                           {}))
  (return (xtd/obj-assign select-triggers return-triggers)))

(defn.xt view-overview
  "gets the view overview"
  {:added "4.0"}
  [db]
  (return (base-view/all-overview (-/get-views db))))

(defn.xt view-tables
  "gets the view tables"
  {:added "4.0"}
  [db]
  (return (xtd/obj-keys (-/get-views db))))

(defn.xt view-methods
  "gets the view methods"
  {:added "4.0"}
  [db table]
  (var views (-/get-views db))
  (return {:return (base-view/all-keys views table "return")
           :select (base-view/all-keys views table "select")}))

(defn.xt view-detail
  "gets the view detail"
  {:added "4.0"}
  [db table type id]
  (return (xtd/get-in (-/get-views db) [table type id])))

(def.xt MODULE (!:module))
