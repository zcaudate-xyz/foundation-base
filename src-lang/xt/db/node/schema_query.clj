(ns xt.db.node.schema-query
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.runtime.cache-view :as cache-view]
             [xt.db.node.schema-state :as schema-state]
             [xt.db.text.base-check :as check]
             [xt.db.text.base-scope :as base-scope]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as xtt]]})

(defn.xt view-query-return-entry
  "creates the return entry for `return-query`"
  {:added "4.1"}
  [table return-query data-only]
  (return
   {:input []
    :return "jsonb"
    :flags {}
    :view {:table table
           :type "return"
           :query (:? data-only
                      (xt/x:arr-filter return-query xt/x:is-string?)
                      return-query)
           :access {:roles {}}
           :guards []}}))

(defn.xt view-query-return-combined
  "creates the combined return entry for `return-query`"
  {:added "4.1"}
  [table return-entry return-query data-only]
  (var query-mixin (:? data-only
                       (xt/x:arr-filter return-query xt/x:is-string?)
                       return-query))
  (var query (xtd/get-in return-entry ["view" "query"]))
  (xt/x:arr-assign query query-mixin)
  (return return-entry))

(defn.xt view-query-entries
  "gets the select and return entries"
  {:added "4.1"}
  [state table qm data-only]
  (var #{select-method
         return-method
         return-query} qm)
  (var views (schema-state/get-views state))
  (var select-entry (:? (xt/x:not-nil? select-method)
                        (xtd/get-in views [table "select" select-method])))
  (var return-entry nil)
  (cond (and return-method return-query)
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
  "gets dependent tables for a query"
  {:added "4.1"}
  [state table qm]
  (var qe (-/view-query-entries state table qm nil))
  (var #{select-entry return-entry} qe)
  (var schema (schema-state/get-schema state))
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
  (return (xt/x:obj-assign select-triggers return-triggers)))

(defn.xt view-local-transform
  "strips `__deleted__` markers from local queries"
  {:added "4.1"}
  [view-entry]
  (when (xt/x:nil? view-entry)
    (return nil))
  (var #{view} view-entry)
  (var tview (xtt/tree-walk view
                            (fn [res]
                              (return (:? (xt/x:is-array? res)
                                          (xt/x:arr-filter res
                                                           (fn [e]
                                                             (return (not= e "__deleted__"))))
                                          res)))
                            (fn [res]
                              (when (and (xt/x:not-nil? res)
                                         (xt/x:is-object? res)
                                         (xt/x:has-key? res "__deleted__"))
                                (xt/x:del-key res "__deleted__"))
                              (return res))))
  (return (xt/x:obj-assign (xtd/obj-clone view-entry)
                           {:view tview})))

(defn.xt query-check
  "checks query arguments against the entry input"
  {:added "4.1"}
  [entry args drop-first]
  (var targs (xt/x:get-key entry "input"))
  (when drop-first
    (:= targs [(xt/x:unpack targs)])
    (x:arr-pop-first targs))
  (var [l-ok l-err] (check/check-args-length args targs))
  (when (not l-ok)
    (return [l-ok l-err]))
  (var [t-ok t-err] (check/check-args-type args targs))
  (when (not t-ok)
    (return [t-ok t-err]))
  (return [true]))

(defn.xt normalize-query
  "normalizes a query spec against a view context"
  {:added "4.1"}
  [query-spec view-context]
  (var #{args} view-context)
  (var #{table
         select-method
         select-args
         select-control
         return-method
         return-query
         return-count
         return-id
         return-bulk
         return-args
         return-omit
         data-only} query-spec)
  (:= args (or args []))
  (return {:table table
           :select-method select-method
           :select-args (or select-args args)
           :select-control select-control
           :return-method return-method
           :return-query return-query
           :return-count return-count
           :return-id return-id
           :return-bulk return-bulk
           :return-args (or return-args [])
           :return-omit return-omit
           :data-only data-only}))

(defn.xt query-key
  "builds a stable cache key for a query"
  {:added "4.1"}
  [query-spec view-context]
  (var explicit (or (xt/x:get-key view-context "query_key")
                    (xt/x:get-key query-spec "key")))
  (when (xt/x:not-nil? explicit)
    (return explicit))
  (return
   (xt/x:json-encode
    {:query (-/normalize-query query-spec view-context)
     :model-id (xt/x:get-key view-context "model-id")
     :view-id (xt/x:get-key view-context "view-id")})))

(defn.xt prepare-query
  "prepares a local query plan"
  {:added "4.1"}
  [state query-spec view-context]
  (var qm (-/normalize-query query-spec view-context))
  (var #{table
         select-method
         select-args
         return-method
         return-id
         return-bulk
         return-args
         return-omit
         data-only} qm)
  (var qe (-/view-query-entries state table qm data-only))
  (var #{select-entry return-entry} qe)
  (:= select-entry (-/view-local-transform select-entry))
  (:= return-entry (-/view-local-transform return-entry))
  (when (and select-method (not select-entry))
    (return [false {:status "error"
                    :tag "net/select-method-not-found"
                    :data {:input select-method}}]))
  (when (and return-method (not return-entry))
    (return [false {:status "error"
                    :tag "net/return-method-not-found"
                    :data {:input return-method}}]))
  (var tree nil)
  (cond (and (xt/x:not-nil? select-entry)
             (xt/x:not-nil? return-entry))
        (do (var [s-ok s-err] (-/query-check select-entry select-args false))
            (when (not s-ok)
              (return [s-ok s-err]))
            (var [r-ok r-err] (-/query-check return-entry return-args true))
            (when (not r-ok)
              (return [r-ok r-err]))
            (:= tree (cache-view/query-combined
                      (schema-state/get-schema state)
                      select-entry
                      select-args
                      return-entry
                      return-args
                      return-omit)))

        (xt/x:not-nil? select-entry)
        (do (var [s-ok s-err] (-/query-check select-entry select-args false))
            (when (not s-ok)
              (return [s-ok s-err]))
            (:= tree (cache-view/query-select
                      (schema-state/get-schema state)
                      select-entry
                      select-args)))

        (xt/x:not-nil? return-id)
        (do (var rargs [return-id (xt/x:unpack return-args)])
            (var [r-ok r-err] (-/query-check return-entry rargs false))
            (when (not r-ok)
              (return [r-ok r-err]))
            (:= tree (cache-view/query-return
                      (schema-state/get-schema state)
                      return-entry
                      return-id
                      return-args)))

        (xt/x:not-nil? return-bulk)
        (do (var [r-ok r-err] (-/query-check return-entry return-args true))
            (when (not r-ok)
              (return [r-ok r-err]))
            (:= tree (cache-view/query-return-bulk
                      (schema-state/get-schema state)
                      return-entry
                      return-bulk
                      return-args))))
  (return [true {:key (-/query-key query-spec view-context)
                 :query query-spec
                 :context {:model-id (xt/x:get-key view-context "model-id")
                           :view-id (xt/x:get-key view-context "view-id")
                           :args (or (xt/x:get-key view-context "args") [])}
                 :plan tree
                 :tables (-/view-triggers state table qm)}]))
