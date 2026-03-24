# Splitting Lua Tests: FINAL Plan

## New Structure

For each failing test namespace, create a Lua-only variant with `xt.db-lua.*` namespace and convert the original to JS-only:

| Original File | JS-Only Namespace | Lua-Only Namespace | Lua File |
|--------------|-------------------|-------------------|----------|
| `test/xt/db_test.clj` | `xt.db-test` | `xt.db-lua.test` | `test/xt/db_lua/test.clj` |
| `test/xt/db/impl_sql_test.clj` | `xt.db.impl-sql-test` | `xt.db-lua.impl-sql-test` | `test/xt/db_lua/impl_sql_test.clj` |
| `test/xt/db/impl_select_sql_test.clj` | `xt.db.impl-select-sql-test` | `xt.db-lua.impl-select-sql-test` | `test/xt/db_lua/impl_select_sql_test.clj` |
| `test/xt/db/impl_select_view_test.clj` | `xt.db.impl-select-view-test` | `xt.db-lua.impl-select-view-test` | `test/xt/db_lua/impl_select_view_test.clj` |
| `test/xt/db/sql_sqlite_test.clj` | `xt.db.sql-sqlite-test` | `xt.db-lua.sql-sqlite-test` | `test/xt/db_lua/sql_sqlite_test.clj` |

## File Structure

```
test/xt/
├── db_test.clj              # JS-only: xt.db-test
├── db_lua/                  # New directory for Lua tests
│   ├── test.clj             # Lua-only: xt.db-lua.test
│   ├── impl_sql_test.clj    # Lua-only: xt.db-lua.impl-sql-test
│   ├── impl_select_sql_test.clj   # Lua-only: xt.db-lua.impl-select-sql-test
│   ├── impl_select_view_test.clj  # Lua-only: xt.db-lua.impl-select-view-test
│   └── sql_sqlite_test.clj  # Lua-only: xt.db-lua.sql-sqlite-test
│
└── db/
    ├── impl_sql_test.clj         # JS-only: xt.db.impl-sql-test
    ├── impl_select_sql_test.clj  # JS-only: xt.db.impl-select-sql-test
    ├── impl_select_view_test.clj # JS-only: xt.db.impl-select-view-test
    └── sql_sqlite_test.clj       # JS-only: xt.db.sql-sqlite-test
```

## Step-by-Step Process

### Step 1: Create Directory Structure

```bash
mkdir -p test/xt/db_lua
```

### Step 2: Create Lua Test Files

For each original file, create a Lua-only copy in `test/xt/db_lua/`:

**Example: `test/xt/db_lua/test.clj`**

```clojure
(ns xt.db-lua.test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.db :as impl]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.base-flatten :as f]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [lua.nginx.driver-sqlite :as lua-sqlite]]})

;; Removed: (l/script- :js ...)

(defn bootstrap-lua
  []
  (!.lua
   (var ngxsqlite (require "lsqlite3"))
   (:= (!:G DBSQL) (impl/db-create {"::" "db.sql"
                                    :constructor lua-sqlite/connect-constructor
                                    :memory true}
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil)))
   (dbsql/query-sync (k/get-key DBSQL "instance")
                     (k/join "\n\n"
                             (manage/table-create-all
                              sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil))))
   (:= (!:G DBCACHE) (impl/db-create {"::" "db.cache"}
                                     sample/Schema
                                     sample/SchemaLookup
                                     (ut/sqlite-opts nil)))
   true))

;; Removed: bootstrap-js

(fact:global
 {:setup [(bootstrap-lua)]})  ; Only Lua

;; Tests with only Lua assertions
(fact "processes an event"
  (!.lua ...)
  => expected)
  ;; Removed: (!.js ...) assertions
```

### Step 3: Modify Original Files to JS-Only

**Example: `test/xt/db_test.clj`**

```clojure
(ns xt.db-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

;; Removed: (l/script- :lua ...)

(l/script- :js
  {:runtime :basic
   :require [[xt.db :as impl]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.base-flatten :as f]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [js.lib.driver-sqlite :as js-sqlite]]})

;; Removed: bootstrap-lua

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (var initSql (require "sql.js"))
    ...))

(fact:global
 {:setup [(bootstrap-js)]})  ; Only JS

;; Tests with only JS assertions
(fact "processes an event"
  (!.js ...)
  => expected)
  ;; Removed: (!.lua ...) assertions
```

## Namespace Naming Convention

- **JS tests:** Use original namespace (`xt.db-test`, `xt.db.impl-sql-test`)
- **Lua tests:** Use `xt.db-lua.*` prefix (`xt.db-lua.test`, `xt.db-lua.impl-sql-test`)

This follows the pattern:
- `xt.db.*` = JavaScript tests
- `xt.db-lua.*` = Lua tests

## Running Tests

### Run JS tests only (default):
```bash
lein test :only xt.db-test
lein test :only xt.db.impl-sql-test
# etc.
```

### Run Lua tests only (when Lua available):
```bash
lein test :only xt.db-lua.test
lein test :only xt.db-lua.impl-sql-test
# etc.
```

### Run all tests:
```bash
lein test  # Will run both JS and Lua if both are available
```

## Complete File Mapping

| Source File | Target Lua File | Lua Namespace |
|------------|-----------------|---------------|
| `test/xt/db_test.clj` | `test/xt/db_lua/test.clj` | `xt.db-lua.test` |
| `test/xt/db/impl_sql_test.clj` | `test/xt/db_lua/impl_sql_test.clj` | `xt.db-lua.impl-sql-test` |
| `test/xt/db/impl_select_sql_test.clj` | `test/xt/db_lua/impl_select_sql_test.clj` | `xt.db-lua.impl-select-sql-test` |
| `test/xt/db/impl_select_view_test.clj` | `test/xt/db_lua/impl_select_view_test.clj` | `xt.db-lua.impl-select-view-test` |
| `test/xt/db/sql_sqlite_test.clj` | `test/xt/db_lua/sql_sqlite_test.clj` | `xt.db-lua.sql-sqlite-test` |

## Benefits

1. **Clear namespace hierarchy:** `xt.db.*` for JS, `xt.db-lua.*` for Lua
2. **Organized file structure:** Lua tests in dedicated `db_lua/` directory
3. **Easy to run selectively:** Filter by namespace prefix
4. **No breaking changes:** Original test namespaces remain
5. **Extensible:** Easy to add more Lua test files in the future

## Verification

After implementation:

1. **Check JS tests run:**
   ```bash
   lein test :only xt.db-test
   ```

2. **Check Lua tests exist:**
   ```bash
   ls test/xt/db_lua/
   # Should show: test.clj, impl_sql_test.clj, etc.
   ```

3. **Check namespaces:**
   ```clojure
   ;; In REPL
   (require 'xt.db-lua.test)
   (require 'xt.db-test)
   ```

## Estimated Effort

- **Create directory:** 1 minute
- **Create each Lua file:** 10-15 minutes × 5 = 50-75 minutes
- **Modify each original file:** 10-15 minutes × 5 = 50-75 minutes
- **Testing:** 20-30 minutes
- **Total:** 2-3 hours

## Commands Summary

```bash
# Create directory
mkdir -p test/xt/db_lua

# Copy files
cp test/xt/db_test.clj test/xt/db_lua/test.clj
cp test/xt/db/impl_sql_test.clj test/xt/db_lua/impl_sql_test.clj
cp test/xt/db/impl_select_sql_test.clj test/xt/db_lua/impl_select_sql_test.clj
cp test/xt/db/impl_select_view_test.clj test/xt/db_lua/impl_select_view_test.clj
cp test/xt/db/sql_sqlite_test.clj test/xt/db_lua/sql_sqlite_test.clj

# Then edit each file to remove JS/Lua-specific code
# and update namespaces accordingly
```

## Implementation Notes

- Use **Clojure Dev Mode** to perform file operations and edits
- Ensure namespace declarations match file paths
- Test each file after modification
