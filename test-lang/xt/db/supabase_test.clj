(ns xt.db.supabase-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
              [xt.protocol.impl.connection-sql :as dbsql]
              [xt.db.text.sql-util :as ut]
              [xt.db.text.sql-manage :as manage]
              [xt.db.helpers.data-main-test :as sample]
              [xt.db.instance :as xdb]
              [xt.db.runtime.supabase :as sup]
              [js.lib.driver-sqlite :as js-sqlite]]})

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (. (dbsql/connect (js-sqlite/driver) {})
       (then (fn [conn]
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
                 (catch e (repl/notify e))))))))

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

^{:refer xt.db.runtime.supabase/snake->kebab :added "4.1"}
(fact "TODO")

^{:refer xt.db.runtime.supabase/normalize-row :added "4.1"}
(fact "TODO")

^{:refer xt.db.runtime.supabase/payload->xdb-events :added "4.1.3"}
(fact "translates canonical supabase payloads to xt.db events"

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

^{:refer xt.db.runtime.supabase/apply-payload :added "4.1.3"}
(fact "applies supabase payload to local xt.db sqlite"

  (!.js
   (:= (!:G USD) @+usd-snake+)
   (sup/apply-payload DBSQL
                      {"type" "postgres_changes"
                       "eventType" "INSERT"
                       "schema" "public"
                       "table" "Currency"
                       "new" (!:G USD)}
                      sample/Schema sample/SchemaLookup (ut/sqlite-opts nil))
   (xdb/db-pull-sync DBSQL
                     sample/Schema
                     ["Currency" ["id"]]))
  => [{"id" "USD"}]

  (!.js
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

^{:refer xt.db.runtime.supabase/attach-events :added "4.1.3"}
(fact "attaches a stubbed supabase realtime client and applies payloads"

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
