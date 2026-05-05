(ns js.cell.service.db-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:export [MODULE]
   :require [[js.cell.service.db-query :as db-query]
              [xt.db.instance :as xdb]
              [xt.lang.spec-base :as xt]
              [xt.db.runtime.supabase :as supabase]
              [xt.db.text.pgrest :as pgrest]]})

(def.xt supabase-capable? supabase/supabase-capable?)

(def.xt compile-select-item pgrest/compile-select-item)

(def.xt compile-select pgrest/compile-select)

(def.xt compile-filters-into pgrest/compile-filters-into)

(defn.xt compile-query
  "compiles a query plan into a Supabase request description"
  {:added "4.0"}
  [db query-plan view-context]
  (return (pgrest/compile-query query-plan)))

(defn.xt execute-query
  "executes a compiled Supabase query via an injected executor"
  {:added "4.0"}
  [db query-plan view-context]
  (var result (xdb/db-pull-sync {"::" "db.supabase"
                                 :instance db
                                 :opts view-context}
                                (xt/x:get-key db "schema")
                                query-plan))
  (if (== "error" (xt/x:get-key result "status"))
    (return [false result])
    (return [true result])))

(def.xt map-supabase-error supabase/map-supabase-error)

(defn.xt run-supabase-query
  "prepares, compiles, and executes a Supabase query"
  {:added "4.0"}
  [db query-spec view-context]
  (var [ok query-plan] (db-query/prepare-query db query-spec view-context))
  (when (not ok)
    (return [ok query-plan]))
  (var [e-ok result] (-/execute-query db query-plan view-context))
  (return [e-ok result]))

(def.xt MODULE (!:module))
