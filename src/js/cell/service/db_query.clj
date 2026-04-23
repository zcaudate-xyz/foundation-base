(ns js.cell.service.db-query
  (:require [std.lang :as l]))

(l/script :xtalk
  {:export [MODULE] :require [[js.cell.service.db-view :as db-view] [xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd] [xt.db :as xdb] [xt.db.base-check :as check] [xt.db.cache-view :as cache-view]]})

(defn.xt query-capable?
  "checks that the db descriptor can prepare queries"
  {:added "4.0"}
  [db]
  (return (and (xt/x:is-object? db)
               (xt/x:has-key? db "schema")
               (xt/x:has-key? db "views"))))

(defn.xt view-local-transform
  "gets rid of `__deleted__` on view queries"
  {:added "4.0"}
  [view-entry]
  (when (xt/x:nil? view-entry)
    (return nil))
  (var #{view} view-entry)
  (var tview (xtd/tree-walk view
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
  {:added "4.0"}
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
  "normalizes a query spec using the view context"
  {:added "4.0"}
  [db query-spec view-context]
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
  (return
   {:table          table
    :select-method  select-method
    :select-args    (or select-args args)
    :select-control select-control
    :return-method  return-method
    :return-query   return-query
    :return-count   return-count
    :return-id      return-id
    :return-bulk    return-bulk
    :return-args    (or return-args [])
    :return-omit    return-omit
    :data-only      data-only}))

(defn.xt prepare-query
  "prepares a db query"
  {:added "4.0"}
  [db query-spec view-context]
  (var qm (-/normalize-query db query-spec view-context))
  (var #{table
         select-method
         select-args
         return-method
         return-id
         return-bulk
         return-args
         return-omit
         data-only} qm)
  (var qe (db-view/view-query-entries db table qm data-only))
  (var #{select-entry
         return-entry} qe)
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
  (cond (and (xt/x:not-nil? select-entry)
             (xt/x:not-nil? return-entry))
        (do (var [s-ok s-err] (-/query-check select-entry select-args false))
            (when (not s-ok)
              (return [s-ok s-err]))
            (var [r-ok r-err] (-/query-check return-entry return-args true))
            (when (not r-ok)
              (return [r-ok r-err]))
            (return [true (cache-view/query-combined
                           (db-view/get-schema db)
                           select-entry
                           select-args
                           return-entry
                           return-args
                           return-omit)]))

        (xt/x:not-nil? select-entry)
        (do (var [s-ok s-err] (-/query-check select-entry select-args false))
            (when (not s-ok)
              (return [s-ok s-err]))
            (return [true (cache-view/query-select
                           (db-view/get-schema db)
                           select-entry
                           select-args)]))

        (xt/x:not-nil? return-id)
        (do (var rargs [return-id (xt/x:unpack return-args)])
            (var [r-ok r-err] (-/query-check return-entry rargs false))
            (when (not r-ok)
              (return [r-ok r-err]))
            (return [true (cache-view/query-return
                           (db-view/get-schema db)
                           return-entry
                           return-id
                           return-args)]))

        (xt/x:not-nil? return-bulk)
        (do (var [r-ok r-err] (-/query-check return-entry return-args true))
            (when (not r-ok)
              (return [r-ok r-err]))
            (return [true (cache-view/query-return-bulk
                           (db-view/get-schema db)
                           return-entry
                           return-bulk
                           return-args)]))

        :else
        (return [true nil])))

(defn.xt execute-query
  "executes a prepared db query"
  {:added "4.0"}
  [db query-plan view-context]
  (var local-db (xt/x:get-key view-context "db"))
  (when (xt/x:nil? local-db)
    (return [false {:status "error"
                    :tag "db/local-db-not-provided"}]))
  (return [true (xdb/db-pull-sync local-db
                                  (db-view/get-schema db)
                                  query-plan)]))

(defn.xt run-query
  "prepares and executes a db query"
  {:added "4.0"}
  [db query-spec view-context]
  (var [ok result] (-/prepare-query db query-spec view-context))
  (when (not ok)
    (return [ok result]))
  (when (not result)
    (return [ok nil]))
  (return (-/execute-query db result view-context)))

(def.xt MODULE (!:module))
