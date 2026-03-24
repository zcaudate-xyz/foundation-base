# Simplifying xt.db: Removing Legacy Access Control in Favor of PostgreSQL RLS

## Executive Summary

**Problem:** `xt.db` contains a legacy application-level access control system (`defaccess.pg`, `:access` in views, `query-fill-clause`) that is **redundant** when using PostgreSQL Row Level Security (RLS) via `defpolicy.pg`.

**Solution:** Remove the old access system entirely, simplifying view query generation and making xt.db a pure data access layer. Access control moves to the database where it belongs.

**Impact:**
- ~100 lines removed from `sql_view.clj`
- ~50 lines removed from `cache_view.clj`
- Eliminate `access-id` parameter from all view APIs
- Remove `bind-view-access` from `gen_bind.clj`
- Deprecate `defaccess.pg` macro
- **Simpler, cleaner, more maintainable codebase**

---

## Current State: Two Access Systems

### 1. Old System (Application-Level) - To Be Removed

```clojure
;; Define access pattern
(defaccess.pg
  organisation-access-is-member
  {:forward  [-/UserAccount
              {:organisation-accesses
               {:role "member"}}]
   :reverse  [-/Organisation
              {:access
               {:role "member"}}]})

;; Attach to view
(defview.pg
  user-account-view
  {:table "UserAccount"
   :type "select"
   :access [organisation-access-is-member]  ; ← Old system
   :query ["id" "email"]})

;; Query with access-id
(xt.db.sql-view/query-select schema view-args {:access-id "user-123"} as-tree)
```

**How it works:**
1. `defaccess.pg` pre-compiles forward/reverse queries for the access pattern
2. View stores reference to access definition
3. `query-fill-clause` replaces `{{<%>}}` template with `access-id` in the access query clause
4. `query-access-check` ensures select access grants return access
5. The generated SQL includes a subquery filtering by access

**Problems:**
- Duplicates what RLS does at the database level
- Adds complexity to xt.db view system
- Cache backend doesn't implement it properly (security hole!)
- Hard to maintain, test, and reason about

---

### 2. New System (Database-Level) - Keep This

```clojure
;; Define RLS policy
(defpolicy.pg organisation-member-policy [UserAccount]
  (:= (-> access.role) "member"))

;; Enable RLS on table
(defentity.pg UserAccount
  {:table true
   :rls true})  ; ← RLS enabled
```

**How it works:**
- PostgreSQL enforces row-level security automatically
- All queries (including from cache if populated from RLS-filtered queries) respect policies
- Policies are centralized in the database
- No application code needed for filtering

**Advantages:**
- Database-enforced, cannot be bypassed
- Single source of truth
- Works for all access paths (direct queries, views, cache)
- Simpler xt.db code

---

## Simplification Plan

### Phase 1: Remove Access from View Query Functions

#### Current API (with access-id)

```clojure
;; sql_view.clj
(defn.xt query-select
  [schema entry args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)          ; ← Access parameter
  (var clause (-/query-fill-clause entry access-id))  ; ← Fill clause
  (var itree  (-/tree-select schema entry clause opts))
  (var qtree  (-/query-fill-input itree args (k/arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-return
  [schema entry id args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)          ; ← Access parameter
  (var clause (-/query-fill-clause entry access-id))  ; ← Fill clause
  (var itree (-/tree-return schema entry {:id id} clause opts))
  (var qtree (-/query-fill-input itree args (k/arr-clone input) true))
  ...)
```

#### Simplified API (no access-id)

```clojure
(defn.xt query-select
  [schema entry args opts as-tree]
  (var #{input} entry)
  (var itree  (-/tree-select schema entry {} opts))  ; ← No clause
  (var qtree  (-/query-fill-input itree args (k/arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-return
  [schema entry id args opts as-tree]
  (var #{input} entry)
  (var itree (-/tree-return schema entry {:id id} {} opts))  ; ← No clause
  (var qtree (-/query-fill-input itree args (k/arr-clone input) true))
  ...)
```

**Changes:**
- Remove `access-id` from opts destructuring
- Remove `query-fill-clause` call
- Pass empty clause `{}` instead
- Remove `query-access-check` from `query-combined`

---

### Phase 2: Simplify View Structure

#### Current View Entry (with access)

```clojure
{:table "UserAccount"
 :type "select"
 :tag "by_organisation"
 :query ["id" "email"]
 :access {:symbol "organisation-access-is-member"  ; ← To remove
          :relation "forward"
          :query {...}
          :roles {...}}
 :input [...]}
```

#### Simplified View Entry (no access)

```clojure
{:table "UserAccount"
 :type "select"
 :tag "by_organisation"
 :query ["id" "email"]
 :input [...]}
```

**Changes:**
- Remove `:access` key from view metadata
- `bind-view-access` becomes no-op or returns `nil`

---

### Phase 3: Update gen_bind.clj

#### Current Code

```clojure
(defn bind-view-access
  "gets the view access"
  {:added "4.0"}
  [access]
  (let [{:keys [relation symbol]} access]
    (-> access
        (update :query dissoc :form :table :symbol :relation)
        (update :roles to-lookup)
        (merge {:relation (if relation (name relation))
                :symbol   (if symbol  (l/sym-default-str symbol))}))))

(defn bind-view
  [ptr & [opts]]
  (let [...]
    (-> m
        (merge {:view (merge
                        {:table table
                         :type  (name type)
                         :tag   tag
                         :access (bind-view-access access)  ; ← Here
                         :query  (transform-query ...)
                         ...)}))))
```

#### Simplified Code

```clojure
(defn bind-view-access
  "DEPRECATED: Access control now handled by PostgreSQL RLS."
  [_]
  (return nil))  ; Or remove entirely

(defn bind-view
  [ptr & [opts]]
  (let [...]
    (-> m
        (merge {:view (merge
                        {:table table
                         :type  (name type)
                         :tag   tag
                         :query  (transform-query ...)
                         :guards (bind-view-guards guards)
                         :autos  (bind-view-guards autos)}
                        opts)}))))  ; ← No :access
```

---

### Phase 4: Remove Access from cache_view.clj

`cache_view.clj` already doesn't have access logic, but verify it doesn't expect `access-id` in opts.

Current `cache_view.clj` query functions don't take `opts` at all, so they're already simpler. Just ensure no references to `access-id` remain.

---

### Phase 5: Deprecate defaccess.pg Macro

#### Current Implementation (rt.postgres.script.graph_view)

```clojure
(defmacro defaccess.pg
  "creates a defaccess macro"
  [sym access]
  (let [access (create-defaccess-prep sym access)]
    (list 'def sym (list 'quote access))))
```

#### Deprecated Version (with warning)

```clojure
(defmacro defaccess.pg
  "DEPRECATED: Access control should use defpolicy.pg for RLS.
   This macro will be removed in a future version."
  [sym access]
  (println "WARNING: defaccess.pg is deprecated. Use defpolicy.pg instead.")
  (let [access (create-defaccess-prep sym access)]
    (list 'def sym (list 'quote access))))
```

Or simply remove it and update all uses to use RLS policies directly.

---

### Phase 6: Update Tests

Remove access-related tests:

- `test/xt/db/sql_view_access_test.clj` - Entire file can be removed or repurposed to test RLS integration
- Tests in `sql_view_test.clj` that use `access-id` parameter
- Update `sample_user_test.clj` to remove `defaccess.pg` definitions

Replace with tests that verify:
- Views work without `:access` field
- Queries return correct data when RLS is enabled
- Cache respects RLS-filtered data (by testing with RLS-enabled data)

---

## Benefits of Simplification

### 1. **Reduced Code Size**
- Remove ~150 lines of access-related code
- Eliminate `query-fill-clause`, `query-access-check` functions
- Simplify view query functions by ~30%

### 2. **Clearer Separation of Concerns**
- xt.db: Pure data access and query generation
- PostgreSQL: Access control via RLS
- No overlap, each layer does one thing well

### 3. **Security Improvement**
- RLS is enforced by database, cannot be accidentally bypassed
- Cache backend automatically respects RLS (because data comes from RLS-filtered queries)
- No security hole in cache backend

### 4. **Easier to Maintain**
- Fewer functions, fewer parameters
- No need to understand two access systems
- Simpler test suite

### 5. **Better Performance**
- No extra subqueries for access filtering
- Database can optimize RLS policies better than application-level joins

---

## Migration Path

### Step 1: Analysis (1 day)
- Audit all `defaccess.pg` uses in codebase
- Identify corresponding RLS policies that should replace them
- Document which views have `:access` metadata

### Step 2: Create RLS Policies (2-3 days)
- For each `defaccess.pg`, create equivalent `defpolicy.pg`
- Test policies work correctly in PostgreSQL
- Verify policies enforce intended access control

### Step 3: Remove Access from xt.db (2 days)
- Remove `query-fill-clause`, `query-access-check` from `sql_view.clj`
- Simplify query functions to not use `access-id`
- Update `gen_bind.clj` to not include `:access` in view metadata
- Deprecate `bind-view-access`

### Step 4: Update Callers (1-2 days)
- Find all code that calls view query functions with `:access-id`
- Remove that parameter
- Update tests to not expect access filtering

### Step 5: Test Thoroughly (2 days)
- Run full test suite
- Add integration tests with real RLS policies
- Verify cache backend works correctly with RLS-filtered data
- Performance testing

### Step 6: Remove Deprecated Code (1 day)
- Remove `defaccess.pg` macro (or leave as stub with deprecation warning)
- Clean up any remaining access-related code
- Update documentation

**Total: 1-2 weeks**

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Existing code relies on `access-id` parameter | High | Comprehensive search for all call sites; update all |
| RLS policies not equivalent to old access logic | High | Thorough testing, review policies with domain experts |
| Performance regression with RLS | Medium | Benchmark before/after; add RLS policy indexes |
| Breaking change for existing users | High | Version bump, migration guide, deprecation period |
| Cache backend still vulnerable | Medium | Verify cache only populated from RLS-filtered sources |

---

## Example: Before and After

### Before (with defaccess.pg)

```clojure
;; Define access
(defaccess.pg org-member-access
  {:forward  [-/UserAccount
              {:organisation-accesses
               {:role "member"}}]
   :reverse  [-/Organisation
              {:access
               {:role "member"}}]})

;; View uses it
(defview.pg user-account-by-org
  {:table "UserAccount"
   :type "select"
   :access [org-member-access]
   :query ["id" "email"]
   :input [i-organisation-id]})

;; Query
(xt.db.sql-view/query-select schema
                             user-account-by-org
                             ["ORG-123"]
                             {:access-id "USER-456"}
                             false)
```

**Generated SQL (simplified):**
```sql
SELECT id, email FROM UserAccount
WHERE id IN (
  SELECT account_id FROM OrganisationAccess
  WHERE organisation_id = 'ORG-123'
    AND role = 'member'
    AND account_id = 'USER-456'  -- ← injected by query-fill-clause
);
```

### After (with RLS)

```clojure
;; RLS policy (in database migration)
CREATE POLICY org_member_select ON UserAccount
  USING EXISTS (
    SELECT 1 FROM OrganisationAccess
    WHERE OrganisationAccess.account_id = UserAccount.id
      AND OrganisationAccess.organisation_id = current_setting('app.current_org')::uuid
      AND OrganisationAccess.role = 'member'
  );

;; View - no access field
(defview.pg user-account-by-org
  {:table "UserAccount"
   :type "select"
   :query ["id" "email"]
   :input [i-organisation-id]})

;; Query - no access-id needed
(xt.db.sql-view/query-select schema
                             user-account-by-org
                             ["ORG-123"]
                             {}  ; opts without access-id
                             false)
```

**Generated SQL:**
```sql
SELECT id, email FROM UserAccount
WHERE organisation_id = 'ORG-123';
-- RLS automatically adds: AND EXISTS (SELECT ... FROM OrganisationAccess ...)
```

**Key Difference:** The access filter is now in the database policy, not in the application query.

---

## What About the Cache?

The cache backend (`db.cache`) stores denormalized data in memory. With RLS:

1. **Cache population** comes from events that originated from database queries that already respect RLS
2. **Cache queries** (`cache-pull-sync`) operate on already-filtered data
3. **No additional filtering needed** - the cache only contains what the user is allowed to see

**Important:** This assumes the cache is **read-only** and **only updated via server events**. If there's any path that writes unfiltered data to the cache, that would be a security issue regardless of which access system we use.

---

## Recommendations

1. **Proceed with this simplification** - it's the right architectural direction
2. **Make RLS mandatory** for any table with access control requirements
3. **Remove defaccess.pg entirely** after migration period
4. **Document** that xt.db views are now pure query templates, no access logic
5. **Add integration tests** that verify RLS policies work correctly with xt.db queries

---

## Alternative: Keep Both (Not Recommended)

If you need to support both systems during transition:

```clojure
(defn.xt query-select
  [schema entry args opts as-tree]
  (var #{input access-id} opts)
  (var clause (if access-id
                (-/query-fill-clause entry access-id)  ; Old way
                {}))                                    ; New way (RLS)
  ...)
```

But this adds complexity and should be temporary only.

---

## Conclusion

The old access system in xt.db is a **legacy artifact** from before RLS was fully adopted. Removing it:

- ✅ Simplifies xt.db codebase
- ✅ Improves security (RLS is stronger)
- ✅ Reduces duplication
- ✅ Aligns with modern architecture (policy server-side only)
- ✅ Makes cache backend more correct

**Recommended action:** Execute the migration plan to remove all `defaccess.pg` and `:access` usage from xt.db within 1-2 weeks.
