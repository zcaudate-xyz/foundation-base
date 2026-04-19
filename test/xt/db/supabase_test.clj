(ns xt.db.supabase-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.sql-util :as ut]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-test :as sample]
             [xt.db :as xdb]
             [xt.db.supabase :as sup]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (dbsql/connect {:constructor js-sqlite/connect-constructor}
                   {:success (fn [conn]
                               (try
                                 (:= (!:G DBSQL)
                                     (xdb/db-create
                                      {"::" "db.sql"
                                       :instance conn}
                                      sample/Schema
                                      sample/SchemaLookup
                                      (ut/sqlite-opts nil)))
                                 (dbsql/query-sync (xt/x:get-key DBSQL "instance")
                                                   (str/join "\n\n"
                                                             (manage/table-create-all
                                                              sample/Schema
                                                              sample/SchemaLookup
                                                              (ut/sqlite-opts nil))))
                                 (repl/notify true)
                                 (catch e (repl/notify e))))})))


(def +usd-snake+
  {"id" "USD"
   "name" "US Dollar"
   "symbol" "USD"
   "type" "fiat"
   "description" "Default Current for the United States of America"
   "time-updated" 1631909757019247
   "time-created" 1631909757019247
   "decimal" 2})

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js) true)
             (bootstrap-js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.supabase/payload->xdb-events :added "4.1.3"}
(fact "translates canonical supabase payloads to xt.db events"
  ^:hidden

  (!.js
    (:= (!:G USD) @+usd-snake+)
    (sup/payload->xdb-events
     {"type" "postgres_changes"
      "eventType" "INSERT"
      "schema" "public"
      "table" "Currency"
      "new" (!:G USD)}))
  => [["add" {"Currency" [{"id" "USD"
                           "name" "US Dollar"
                           "symbol" "USD"
                           "type" "fiat"
                           "description" "Default Current for the United States of America"
                           "time-updated" 1631909757019247
                           "time-created" 1631909757019247
                           "decimal" 2}]}]]

  (!.js
   (sup/payload->xdb-events
    {"type" "postgres_changes"
     "eventType" "DELETE"
     "schema" "public"
     "table" "Currency"
     "old" {"id" "USD"}}))
  => [["remove" {"Currency" [{"id" "USD"}]}]]

  (!.js
   (sup/payload->xdb-events
    {"type" "postgres_changes"
     "eventType" "UPDATE"
     "schema" "public"
     "table" "Currency"
     "old" {"id" "USD"}
     "new" {"id" "USD2" "name" "US Dollar 2"}}))
  => [["remove" {"Currency" [{"id" "USD"}]}]
      ["add" {"Currency" [{"id" "USD2" "name" "US Dollar 2"}]}]])

;;
;; Integration: apply to sqlite-backed xt.db
;;

^{:refer xt.db.supabase/apply-payload :added "4.1.3"}
(fact "applies supabase payload to local xt.db (sqlite)"
  ^:hidden

  (!.js
   ;; stash USD row in JS global so the expected map is stable in compiled output
    (:= (!:G USD) @+usd-snake+)
    ;; insert
    (sup/apply-payload DBSQL
                        {"type" "postgres_changes"
                         "eventType" "INSERT"
                         "schema" "public"
                         "table" "Currency"
                         "new" (!:G USD)}
                       sample/Schema sample/SchemaLookup (ut/sqlite-opts nil))
    ;; query
    (xdb/db-pull-sync DBSQL
                      sample/Schema
                      ["Currency" ["id"]]))
  => [{"id" "USD"}]
  
  (!.js
    ;; delete
   (sup/apply-payload DBSQL
                       {"type" "postgres_changes"
                        "eventType" "DELETE"
                        "schema" "public"
                        "table" "Currency"
                        "old" {"id" "USD"}}
                       sample/Schema sample/SchemaLookup (ut/sqlite-opts nil))
   (xdb/db-pull-sync DBSQL
                     sample/Schema
                     ["Currency" ["id"]]))
  => [])

^{:refer xt.db.supabase/attach-events :added "4.1.3"}
(fact "attaches a stubbed supabase realtime client and applies payloads"
  ^:hidden

  (!.js
   (:= (!:G USD) @+usd-snake+)
   (var handlers [])
   (var channel {:on (fn [_type _binding handler]
                       (x:arr-push handlers handler)
                       (return channel))
                 :subscribe (fn [] (return channel))
                 :unsubscribe (fn [] (return true))})
   (var supabase {:channel (fn [_name] (return channel))})
   (var res (sup/attach-events
             {"supabase" supabase
              "xdb" DBSQL
              "schema" sample/Schema
              "lookup" sample/SchemaLookup
              "opts" (ut/sqlite-opts nil)
              "channel-name" "test"
              "bindings" [{"event" "*"
                           "schema" "public"
                           "table" "Currency"}]}))
    (var detach-fn (xt/x:get-key res "detach-fn"))
   ;; feed one insert payload
   ((x:get-idx handlers 0)
    {"type" "postgres_changes"
     "eventType" "INSERT"
     "schema" "public"
     "table" "Currency"
     "new" (!:G USD)})
   (var rows (xdb/db-pull-sync DBSQL
                               sample/Schema
                               ["Currency" ["id"]]))
   (detach-fn)
   rows)
  => [{"id" "USD"}])


^{:refer xt.db.supabase/snake->kebab :added "4.1"}
(fact "TODO")

^{:refer xt.db.supabase/normalize-row :added "4.1"}
(fact "TODO")