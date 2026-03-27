# js.cell db/sync Protocol Usage

Extracted from statslink pattern. This provides automatic synchronization of remote responses to local SQLite.

## Overview

The db/sync protocol handles:
1. Remote API calls returning data with `{:db/sync {...}}`
2. Automatic sync to SQLite with conflict resolution
3. Event emission to main thread for UI updates

## Protocol

### Message Format

**Sync Request:**
```clojure
{:db/sync {"TableName" [{:id "x" :name "y" :time_updated 123}]}}
```

**Remove Request:**
```clojure
{:db/remove {"TableName" ["id1" "id2"]}}
```

**Event Emitted:**
```clojure
{:op "stream"
 :status "ok"
 :topic "db/sync"
 :body {"TableName.id" {:id "x"}}}
```

## Files

| File | Purpose |
|------|---------|
| `js.cell.protocol.sync` | Core sync protocol |
| `js.cell.impl-user` | User context (db, token, log-latest) |
| `js.cell.full-worker` | Worker routes with auto-sync |
| `js.cell.main` | Main thread API |

## Usage

### 1. Worker Setup (worker.js)

```clojure
(ns myapp.worker
  (:require [js.cell.full-worker :as full-worker]
            [js.cell.impl-user :as impl-user]
            [js.lib.driver-sqlite :as driver-sqlite]))

(l/script :js)

;; Create user with SQLite
(defn.js make-user [args]
  (var #{schema sql-js sql-wasm} args)
  
  ;; Load sql.js
  (importScripts sql-js)
  
  ;; Create SQL instance
  (var SQL (await (initSqlJs {:locateFile (fn:> sql-wasm)})))
  
  ;; Create user
  (return (impl-user/with-sqlite
            {:instance (driver-sqlite/make-instance SQL)}
            schema)))

;; Init worker
(full-worker/init-worker
 {:get-user make-user
  :fetch-fn js/fetch
  :event-fn (fn [data] (postMessage data))})
```

### 2. Main Thread (app.cljs)

```clojure
(ns myapp.app
  (:require [js.cell.main :as cell]
            [myapp.schema :as schema]))

(l/script :js)

;; Initialize
(defn.js init []
  (return (cell/init
           {:worker-url "./worker.js"
            :schema schema/SCHEMA
            :db-opts {:sql-js "./sql-wasm.js"
                      :sql-wasm "./sql-wasm.wasm"}
            :on-sync (fn [changes]
                      (console.log "Synced:" changes))
            :on-init (fn [cell result]
                      (console.log "Worker ready"))})))

;; Login (auto-syncs user data)
(defn.js login [cell email password]
  (return (. (cell/remote-call cell
                               "auth/login"
                               {:email email :password password})
             (then (fn [response]
                     ;; Token and user data automatically synced
                     ;; via db/sync in response
                     (cell/set-token cell (:token response))
                     (return response))))))

;; Query local (instant, no network)
(defn.js get-items [cell]
  (return (cell/local-pull cell {:Item [:id :name :price]})))

;; Refresh from remote (auto-syncs)
(defn.js refresh-items [cell]
  (return (cell/remote-call cell "item/list" {})))
```

### 3. Server Response Format

Your server should return:

```json
{
  "data": {...},
  "db/sync": {
    "Item": [
      {"id": "1", "name": "A", "time_updated": 1234567890}
    ],
    "User": [
      {"id": "u1", "name": "John", "time_updated": 1234567890}
    ]
  }
}
```

The `time_updated` field is used for conflict resolution (newer wins).

## Key Features

### 1. Automatic Sync

Remote calls automatically extract and sync `db/sync` data:

```clojure
(cell/remote-call cell "item/list" {})
;; => automatically syncs to SQLite
;; => emits sync event
```

### 2. Conflict Resolution

Uses `event-log-latest` to prevent stale data overwrites:

```clojure
;; Old data (time_updated: 100)
;; New data (time_updated: 200) -> accepted
;; Old data (time_updated: 150) -> rejected (stale)
```

### 3. Event-Driven UI

Listen for sync events to update UI:

```clojure
(cell/on-sync cell (fn [changes]
                    ;; changes: {"Item.1" {:id "1" :name "A"}}
                    (refresh-views)))
```

### 4. Model Integration

Works with js.cell models:

```clojure
(def item-model
  (cell/make-model
   {:list
    {:local-handler (fn [ctx] ...)
     :remote-url "item/list"
     :trigger #{:item/update}}}))
```

## Comparison to Statslink

| Feature | Statslink | js.cell.protocol.sync |
|---------|-----------|----------------------|
| Event topics | EV_DB_SYNC, etc. | TOPIC_SYNC, etc. |
| User context | statslink.user | js.cell.user |
| Sync filtering | user-event/filter-sync-latest | sync/filter-sync-latest |
| Response processing | remote-db-call | process-sync-response |
| Event emission | event-fn {:op "stream" ...} | Same format |

## Migration from Statslink

Replace:
- `statslink.impl.base-event/EV_DB_SYNC` → `js.cell.protocol.sync/TOPIC_SYNC`
- `statslink.impl.user-event/call-sync-event` → `js.cell.impl-user/call-sync-event`
- `statslink.full.internal-remote/remote-db-call` → `js.cell.protocol.sync/process-sync-response`

The protocol is identical - just different namespace.
