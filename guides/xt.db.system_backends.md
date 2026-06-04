# xt.db.system Backend Guide

xt.db.system/db-create is the core factory for creating database runtime clients. It takes a tagged config map, a schema, a lookup, and backend-specific opts, then returns a unified db map that all xt.db operations work against.

The "::" key on the config map determines which backend is created:

| Backend | Tag | Description |
|---------|-----|-------------|
| Memory | `"db.cache"` | In-memory key/value store |
| SQL | `"db.sql"` | SQLite or Postgres via SQL connection |
| Supabase | `"db.supabase"` | Read-only PostgREST client |

---

## Memory Backend

The memory backend stores data in a plain JavaScript object. It is useful for tests, caching layers, and offline-first applications.

### Creating the db

```clojure
(require '[xt.db.system :as db-system])

(var db (db-system/db-create
         {"::" "db.cache"}
         schema
         lookup
         nil))  ;; opts are ignored for memory
```

The returned db contains an in-memory `rows` map. No connection setup is required.

### Sync / seed data

```clojure
(db-system/sync-event
 db
 ["add" {"Entry" [{"id" "e1"
                    "name" "hello"
                    "__deleted__" false}]}])
```

Under the hood this flattens nested data and merges rows into the store.

### Query / pull data

```clojure
;; Sync pull
(db-system/db-pull-sync db schema ["Entry" ["id" "name"]])

;; Returns:
;; {"data" [{"id" "e1" "name" "hello"}]}
```

### Clear data

```clojure
(db-system/db-clear db)
```

---

## SQL Backend (SQLite and Postgres)

The SQL backend handles both SQLite and Postgres through the same `"db.sql"` tag. The difference is in the raw `instance` (connection) and the `opts` (type mappings).

### What the instance looks like

The instance is a live SQL connection object returned by `xt.protocol.impl.connection-sql/connect`:

```clojure
{"::" "db.client.sql"
 "instance" <connection>}
```

### SQLite

#### Connecting and creating the db

```clojure
(require '[xt.db.system :as db-system]
         '[xt.protocol.impl.connection-sql :as dbsql]
         '[xt.db.text.sql-util :as sql-util]
         '[xt.db.text.sql-manage :as sql-manage]
         '[xt.lang.common-string :as str]
         '[js.lib.driver-sqlite :as js-sqlite])

(. (dbsql/connect (js-sqlite/driver) {})
   (then (fn [conn]
           (var db (db-system/db-create
                    {"::" "db.sql"
                     :instance conn}
                    schema
                    lookup
                    (sql-util/sqlite-opts nil)))
           ...)))
```

#### Creating tables

```clojure
(dbsql/query-sync
 conn
 (str/join "\n\n"
           (sql-manage/table-create-all
            schema
            lookup
            (sql-util/sqlite-opts nil))))
```

#### Seeding data

```clojure
(db-system/sync-event
 db
 ["add" {"Entry" [{"id" "e1" "name" "hello" "__deleted__" false}]}])
```

Under the hood this generates UPSERT SQL and executes it against the connection.

#### Querying

```clojure
;; Sync pull with tree query
(db-system/db-pull-sync db schema ["Entry" ["id" "name"]])

;; Raw SQL exec
(db-system/db-exec-sync db "SELECT count(*) FROM Entry;")
```

### Postgres

#### Connecting and creating the db

```clojure
(require '[xt.db.system :as db-system]
         '[xt.protocol.impl.connection-sql :as dbsql]
         '[xt.db.text.sql-util :as sql-util]
         '[js.lib.driver-postgres :as js-pg])

(. (dbsql/connect (js-pg/driver) {"database" "mydb"})
   (then (fn [conn]
           (var db (db-system/db-create
                    {"::" "db.sql"
                     :instance conn}
                    schema
                    lookup
                    (sql-util/postgres-opts lookup)))
           ...)))
```

#### Creating tables

Same pattern as SQLite, but with postgres opts:

```clojure
(dbsql/query-sync
 conn
 (str/join "\n\n"
           (sql-manage/table-create-all
            schema
            lookup
            (sql-util/postgres-opts lookup))))
```

This generates Postgres-specific DDL (e.g. `uuid`, `jsonb`, `citext`, schema-qualified table names).

#### Seeding and querying

Identical to SQLite:

```clojure
(db-system/sync-event db ["add" seed-data])
(db-system/db-pull-sync db schema ["Entry" ["id" "name"]])
(db-system/db-exec-sync db "SELECT count(*) FROM Entry;")
```

---

## Supabase Backend

The Supabase backend is a **read-only** PostgREST client. It does not support writes, table creation, or sync events. It compiles tree queries into HTTP requests against a Supabase/PostgREST endpoint.

### Creating the db

```clojure
(require '[xt.db.system :as db-system])

(var db (db-system/db-create
         {"::" "db.supabase"
          "client" {"base_url" "https://<project>.supabase.co"
                    "api_key" "<anon-key>"
                    "auth_token" "<jwt-token>"}}
         schema
         lookup
         nil))
```

The `instance` here is a config object, not a live connection. The actual HTTP transport is resolved lazily at query time.

### Query / pull data

Only **async** pull is supported. `db-pull-sync` will throw.

```clojure
(. (db-system/db-pull db schema ["Entry" ["id" "name"]])
   (then (fn [result]
           (println result))))
```

Under the hood this:
1. Compiles the tree query into a PostgREST request spec via `pgrest-graph/select-return`
2. Resolves headers (`apikey`, `Authorization`, `Content-Profile`)
3. Executes via the fetch client

### Not supported

| Operation | Status |
|-----------|--------|
| Create tables | Not applicable |
| `sync-event` / seed | Throws unsupported-op |
| `db-exec-sync` | Throws unsupported-op |
| `db-pull-sync` | Throws unsupported-op |
| `db-delete-sync` | Throws unsupported-op |

---

## Summary Comparison

| Feature | Memory | SQL (SQLite/Postgres) | Supabase |
|---------|--------|----------------------|----------|
| Tag | `"db.cache"` | `"db.sql"` | `"db.supabase"` |
| Instance | None (in-memory map) | Live SQL connection | Config object |
| Opts | `nil` | `sqlite-opts` / `postgres-opts` | `nil` or overrides |
| Create tables | N/A | `sql-manage/table-create-all` + `query-sync` | N/A |
| Seed data | `sync-event` | `sync-event` | Not supported |
| Sync pull | `db-pull-sync` | `db-pull-sync` | Not supported |
| Async pull | Not supported | `db-pull` | `db-pull` |
| Raw exec | Not supported | `db-exec-sync` | Not supported |
| Delete | `db-delete-sync` | `db-delete-sync` | Not supported |
| Clear | `db-clear` | Not supported | Not supported |

---

## Minimal Examples

### Memory

```clojure
(var db (db-system/db-create {"::" "db.cache"} schema lookup nil))
(db-system/sync-event db ["add" seed-data])
(db-system/db-pull-sync db schema ["Entry" ["name"]])
```

### SQLite

```clojure
(. (dbsql/connect (js-sqlite/driver) {})
   (then (fn [conn]
           (var db (db-system/db-create
                    {"::" "db.sql" :instance conn}
                    schema lookup (sql-util/sqlite-opts nil)))
           (dbsql/query-sync conn (str/join "\n\n"
             (sql-manage/table-create-all schema lookup (sql-util/sqlite-opts nil))))
           (db-system/sync-event db ["add" seed-data])
           (db-system/db-pull-sync db schema ["Entry" ["name"]]))))
```

### Postgres

```clojure
(. (dbsql/connect (js-pg/driver) {"database" "mydb"})
   (then (fn [conn]
           (var db (db-system/db-create
                    {"::" "db.sql" :instance conn}
                    schema lookup (sql-util/postgres-opts lookup)))
           (dbsql/query-sync conn (str/join "\n\n"
             (sql-manage/table-create-all schema lookup (sql-util/postgres-opts lookup))))
           (db-system/sync-event db ["add" seed-data])
           (db-system/db-pull-sync db schema ["Entry" ["name"]]))))
```

### Supabase

```clojure
(var db (db-system/db-create
         {"::" "db.supabase"
          "client" {"base_url" "https://..."
                    "api_key" "..."
                    "auth_token" "..."}}
         schema lookup nil))
(. (db-system/db-pull db schema ["Entry" ["name"]])
   (then (fn [result] ...)))
```
