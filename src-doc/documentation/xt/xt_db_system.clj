(ns documentation.xt-db-system
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.db.system.main :as main]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-memory :as impl-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.db.system"
         :subtitle "Memory, postgres, sqlite, supabase, and websocket systems."
         :lead "`xt.db.system` provides backend implementations for the same database model, from in-memory graphs to postgres, sqlite, Supabase, realtime, and websocket-backed systems."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"The system layer lets tests and applications swap the storage backend while preserving the higher-level database contract. This is why parity tests exist for memory, SQL, and Supabase paths."

[[:chapter {:title "Internal usage" :link "internal"}]]

"The main system namespaces are exercised by `test-lang/xt/db/system/*`. POC tests use these implementations to validate worker, shared-worker, adapter, and Supabase flows."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Creating a system"}]]

"A memory system needs only a schema and a table lookup. The same `ISourceLocal` contract is also provided by postgres, sqlite, and supabase implementations."

(fact "create a memory system directly or through the factory"
  ^{:refer xt.db.system.impl-memory/impl-memory :added "4.1"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (impl-memory/impl-memory schema lookup))
    (xt/x:get-key impl "::"))
  => "xt.db.system.impl_memory/ImplMemory"

  ^{:refer xt.db.system.main/create-impl :added "4.1"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (main/create-impl "memory" {} schema lookup))
    (xt/x:get-key impl "::"))
  => "xt.db.system.impl_memory/ImplMemory")

[[:section {:title "Adding and querying records"}]]

"`record-add` writes raw rows into a table; `pull` reads them back. Pull accepts a table name, an optional query map, and an optional projection vector."

(fact "add records and pull them back"
  ^{:refer xt.db.system.impl-common/record-add :added "4.1"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (impl-memory/impl-memory schema lookup))
    (impl-common/record-add impl "Task" [{"id" "T-1" "title" "First"}
                                         {"id" "T-2" "title" "Second"}])
    (impl-common/pull impl ["Task"]))
  => (just [(just {"id" "T-1" "title" "First"})
            (just {"id" "T-2" "title" "Second"})]
           :in-any-order)

  ^{:refer xt.db.system.impl-common/pull :added "4.1"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (impl-memory/impl-memory schema lookup))
    (impl-common/record-add impl "Task" [{"id" "T-1" "title" "First"}])
    (impl-common/pull impl ["Task" {"id" "T-1"} ["id" "title"]]))
  => [{"id" "T-1" "title" "First"}])

[[:section {:title "Sync payloads and listeners"}]]

"Real-world clients receive `db/sync` payloads. `sync-process-payload` applies those payloads and notifies any listener whose guard matches the changed table."

(fact "apply a sync payload and listen for changes"
  ^{:refer xt.db.system.impl-common/sync-process-payload :added "4.1.4"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (impl-memory/impl-memory schema lookup))
    (impl-common/sync-process-payload impl {"db/sync" {"Task" [{"id" "T-3" "title" "Synced"}]}})
    (impl-common/pull impl ["Task"]))
  => [{"id" "T-3" "title" "Synced"}]

  ^{:refer xt.db.system.impl-common/add-db-listener :added "4.1"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (impl-memory/impl-memory schema lookup))
    (var events [])
    (impl-common/add-db-listener
     impl
     "logger"
     {"guard"    (fn [table] (return (== table "Task")))
      "callback" (fn [event] (xt/x:arr-push events event))})
    (impl-common/sync-process-payload impl {"db/sync" {"Task" [{"id" "T-4" "title" "Listened"}]}})
    events)
  => [{"db/sync" {"Task" [{"id" "T-4" "title" "Listened"}]}}])

[[:section {:title "Removing and clearing records"}]]

"`record-delete` removes specific ids. A sync payload can also carry `db/remove`, and `clear-db` empties every table."

(fact "delete records and clear the system"
  ^{:refer xt.db.system.impl-common/record-delete :added "4.1"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (impl-memory/impl-memory schema lookup))
    (impl-common/record-add impl "Task" [{"id" "T-5" "title" "Delete me"}])
    (impl-common/record-delete impl "Task" ["T-5"])
    (impl-common/pull impl ["Task"]))
  => []

  ^{:refer xt.db.system.impl-common/sync-process-payload :added "4.1.4"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (impl-memory/impl-memory schema lookup))
    (impl-common/sync-process-payload impl {"db/sync" {"Task" [{"id" "T-6" "title" "Remove me"}]}})
    (impl-common/sync-process-payload impl {"db/remove" {"Task" [{"id" "T-6"}]}})
    (impl-common/pull impl ["Task"]))
  => []

  ^{:refer xt.db.system.impl-common/clear-db :added "4.1"}
  (!.js
    (var schema {"Task" {"id"    {"ident" "id" "order" 0 "type" "text"}
                         "title" {"ident" "title" "order" 1 "type" "text"}}})
    (var lookup {"Task" {"position" 0}})
    (var impl (impl-memory/impl-memory schema lookup))
    (impl-common/record-add impl "Task" [{"id" "T-7" "title" "Cleared"}])
    (impl-common/clear-db impl)
    [(impl-common/pull impl ["Task"])
     (xt/x:len (xt/x:obj-keys (xt/x:get-key impl "rows")))])
  => [[] 0])

[[:chapter {:title "API" :link "api"}]]

