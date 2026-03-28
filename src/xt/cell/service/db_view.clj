(ns xt.cell.service.db-view
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.base-scope :as base-scope]
             [xt.db.base-view :as base-view]]
   :export  [MODULE]})

(defn.xt get-views
  "gets the db views"
  {:added "4.0"}
  [db]
  (return (or (k/get-key db "views")
              {})))

(defn.xt get-schema
  "gets the db schema"
  {:added "4.0"}
  [db]
  (return (or (k/get-key db "schema")
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
                       (k/arr-filter return-query k/is-string?)
                       return-query)
           :access {:roles {}}
           :guards []}}))

(defn.xt view-query-return-combined
  "creates the combined return entry for `return-query` key"
  {:added "4.0"}
  [table return-entry return-query data-only]
  (var query-mixin (:? data-only
                       (k/arr-filter return-query k/is-string?)
                       return-query))
  (var query (k/get-in return-entry ["view" "query"]))
  (k/arr-append query query-mixin)
  (return return-entry))

(defn.xt view-query-entries
  "gets the select and return entries"
  {:added "4.0"}
  [db table qm data-only]
  (var #{select-method
         return-method
         return-query} qm)
  (var views (-/get-views db))
  (var select-entry (:? (k/not-nil? select-method)
                        (k/get-in views [table "select" select-method])))
  (var return-entry nil)
  (cond (and return-method
             return-query)
        (:= return-entry (-/view-query-return-combined
                          table
                          (k/clone-nested (k/get-in views [table "return" return-method]))
                          return-query
                          data-only))

        return-method
        (:= return-entry (k/get-in views [table "return" return-method]))

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
                            (k/get-path select-entry ["view" "query"])
                            {})
                           {}))
  (var return-triggers (:? return-entry
                           (base-scope/get-linked-tables
                            schema table
                            (k/get-path return-entry ["view" "query"]))
                           {}))
  (return (k/obj-assign select-triggers return-triggers)))

(defn.xt view-overview
  "gets the view overview"
  {:added "4.0"}
  [db]
  (return (base-view/all-overview (-/get-views db))))

(defn.xt view-tables
  "gets the view tables"
  {:added "4.0"}
  [db]
  (return (k/obj-keys (-/get-views db))))

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
  (return (k/get-in (-/get-views db) [table type id])))

(def.xt MODULE (!:module))
