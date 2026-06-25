(ns xt.db.system
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-sqlite :as impl-sqlite]
             [xt.db.system.impl-postgres :as impl-postgres]
             [xt.db.system.impl-supabase :as impl-supabase]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.conn-sql :as conn-sql]]})

(defn.xt db-create
  "creates a db implementation wrapper for the requested backend"
  {:added "4.1.4"}
  [client schema lookup opts]
  (var kind (xt/x:get-key client "::"))
  (var raw-client (or (xt/x:get-key client "instance")
                      (xt/x:get-key client "client")
                      client))
  (cond (or (== kind "db.cache")
            (== kind "db.memory"))
        (return (impl-memory/impl-memory schema lookup))

        (or (== kind "db.sql")
            (== kind "db.sqlite"))
        (return (impl-sqlite/impl-sqlite raw-client schema lookup))

        (== kind "db.postgres")
        (return (impl-postgres/impl-postgres raw-client schema lookup))

        (== kind "db.supabase")
        (return (impl-supabase/impl-supabase raw-client schema lookup))

        :else
        (return (impl-memory/impl-memory schema lookup))))

(defn.xt sync-event
  "applies a sync payload {db/sync ..., db/remove ...} to a db implementation"
  {:added "4.1.4"}
  [db payload]
  (var kind (xt/x:get-key db "::"))
  (var db-sync (xt/x:get-key payload "db/sync"))
  (var db-remove (xt/x:get-key payload "db/remove"))
  (when (xt/x:not-nil? db-sync)
    (cond (or (== kind "xt.db.system.impl_sqlite/ImplSqlite")
              (== kind "db.sql")
              (== kind "db.sqlite"))
          (impl-sqlite/process-add-event db db-sync)

          :else
          (impl-memory/process-add-event db db-sync)))
  (when (xt/x:not-nil? db-remove)
    (cond (or (== kind "xt.db.system.impl_sqlite/ImplSqlite")
              (== kind "db.sql")
              (== kind "db.sqlite"))
          (impl-sqlite/process-remove-event db db-remove)

          :else
          (impl-memory/process-remove-event db db-remove)))
  (return true))

(defn.xt db-delete-sync
  "removes ids from a local db implementation"
  {:added "4.1.4"}
  [db schema table ids]
  (return (sync-event db {"db/remove" {table ids}})))

(defn.xt db-pull-sync
  "pulls a tree from a local db implementation"
  {:added "4.1.4"}
  [db schema tree]
  (var kind (xt/x:get-key db "::"))
  (cond (or (== kind "xt.db.system.impl_memory/ImplMemory")
            (== kind "db.cache")
            (== kind "db.memory"))
        (return (impl-memory/pull db tree))

        (or (== kind "xt.db.system.impl_sqlite/ImplSqlite")
            (== kind "db.sql")
            (== kind "db.sqlite"))
        (return (impl-sqlite/pull db tree))

        (or (== kind "xt.db.system.impl_postgres/ImplPostgres")
            (== kind "db.postgres"))
        (return
         (conn-sql/query
          (xt/x:get-key db "client")
          (sql-graph/select (or schema (xt/x:get-key db "schema"))
                            tree
                            (or (xt/x:get-key db "opts") {}))))

        :else
        (return (impl-memory/pull db tree))))

(defn.xt db-exec-sync
  "runs a raw SQL statement through the wrapped db client"
  {:added "4.1.4"}
  [db sql]
  (return
   (conn-sql/query
    (or (xt/x:get-key db "client")
        (xt/x:get-key db "instance")
        db)
    sql)))
