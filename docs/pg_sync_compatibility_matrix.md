# PG Sync Compatibility Matrix

## Purpose

This matrix maps the current role of each repo in the PG-first, `:db/sync`
architecture.


## Summary Matrix

| Capability | foundation-base | statstrade-core | statstrade-v1 | gw-v2 | Status |
| --- | --- | --- | --- | --- | --- |
| `rt.postgres` grammar/runtime | Canonical implementation | Consumed | Consumed | Consumed | Strong |
| `defpolicy.pg` / RLS | Canonical implementation | Mostly absent in old design | Active usage | Active usage | Strong outside `-core` |
| `xt.db` graph query and sync layer | Canonical implementation and backbone | Heavy usage and reference architecture | Minimal | Minimal | Strong but aging |
| `:db/sync` client convergence model | Partial primitives | Strong historical reference | Indirect | Indirect | Needs recovery |
| sqlite client projection pattern | Driver + backend primitives | SQL/cache split present | Not central | Not central | Needs reconnection |
| page-level data link model | Not defined as a stable product yet | Implicit server contract over `xt.db` patterns | Not primary | Not primary | Missing in canonical form |
| helper function generation from entities | Partial | Macro-heavy legacy patterns | Strong | Strong | Good reference outside foundation |
| code generation via output files | Emerging | Weak | Good | Good | Directionally correct |
| infer/openapi/runtime type extraction | Strong and growing | Minimal | Minimal | Strong | Strong in foundation/gw-v2 |


## Repo Detail

### foundation-base

Primary files:

- [src/rt/postgres.clj](/Users/chris/Development/greenways/foundation-base/src/rt/postgres.clj)
- [src/rt/postgres/grammar/common_application.clj](/Users/chris/Development/greenways/foundation-base/src/rt/postgres/grammar/common_application.clj)
- [src/xt/db.clj](/Users/chris/Development/greenways/foundation-base/src/xt/db.clj)
- [src/xt/db/impl_sql.clj](/Users/chris/Development/greenways/foundation-base/src/xt/db/impl_sql.clj)
- [src/xt/db/sql_view.clj](/Users/chris/Development/greenways/foundation-base/src/xt/db/sql_view.clj)

Current role:

- canonical home of `rt.postgres`
- canonical home of `xt.db`
- canonical location for driver contracts and SQL graph generation
- best place to define the normalized spec used by generators
- home of the most important backbone pieces

Gaps:

- no thin `defentity.pg` authoring model yet
- no canonical page/link runtime contract yet
- no explicit dashboard link test model yet
- `xt.db` is not emphasized strongly enough as the preserved backbone in plans

### statstrade-core

Primary files:

- [src/statsapi/list/db_view_sql.clj](/Users/chris/Development/greenways/statstrade-core/src/statsapi/list/db_view_sql.clj)
- [src/statsapi/list/db_view_cache.clj](/Users/chris/Development/greenways/statstrade-core/src/statsapi/list/db_view_cache.clj)
- [src/statsnet/api/common/base_view.clj](/Users/chris/Development/greenways/statstrade-core/src/statsnet/api/common/base_view.clj)

Current role:

- best reference for the old end-to-end architecture
- strongest example of server query graph -> sqlite/cache -> page flow
- strongest reference for where `xt.db` needs to be recovered and preserved

Gaps:

- auth/guard layer predates current RLS direction
- client/page contract is implicit rather than normalized
- too much behavior is spread across old runtime idioms

### statstrade-v1

Primary files:

- [src/szndb/core/type_user.clj](/Users/chris/Development/greenways/statstrade-v1/src/szndb/core/type_user.clj)
- [src/szndb/rpc/api_user.clj](/Users/chris/Development/greenways/statstrade-v1/src/szndb/rpc/api_user.clj)
- [src/szndb/rpc/policy_super.clj](/Users/chris/Development/greenways/statstrade-v1/src/szndb/rpc/policy_super.clj)
- [src/sznbuild/gen/db/rpc_policy_super_gen.clj](/Users/chris/Development/greenways/statstrade-v1/src/sznbuild/gen/db/rpc_policy_super_gen.clj)

Current role:

- reference for generated helper function patterns around entities
- reference for generated policy files
- reference for output-file oriented generation rather than giant macro expansion

Gaps:

- old app structure
- less useful as a canonical page/data sync reference

### gw-v2

Primary files:

- [backend/src/gwdb/platform/spaces/type_space_base.clj](/Users/chris/Development/greenways/gw-v2/backend/src/gwdb/platform/spaces/type_space_base.clj)
- [backend/src/gwdb/rpc/api_core_user.clj](/Users/chris/Development/greenways/gw-v2/backend/src/gwdb/rpc/api_core_user.clj)
- [backend/src/gwdb/rpc/policy_super.clj](/Users/chris/Development/greenways/gw-v2/backend/src/gwdb/rpc/policy_super.clj)
- [backend/src/gwbuild/tasks/make_openapi.clj](/Users/chris/Development/greenways/gw-v2/backend/src/gwbuild/tasks/make_openapi.clj)

Current role:

- strongest modern reference for current `rt.postgres`
- strongest modern reference for policy/RLS generation
- strongest modern reference for runtime/infer/openapi direction

Gaps:

- does not currently exercise the old `xt.db` client sync architecture
- page data layer is not yet the canonical home of the old sqlite sync model


## Architectural Conclusions

### What is stable enough now

- PG-first entity and function authoring
- server-side policy and RLS
- generated helper functions as normal output code
- DB-first source of truth
- `xt.db` as a major part of the query/sync backbone

### What must be rebuilt explicitly

- `:db/sync`-driven client convergence
- normalized dashboard/list/page link model
- thin page data contract suitable for testing before UI
- integration between modern `rt.postgres` and revived `xt.db` sync usage

### What should not be forced early

- final page DSL syntax
- rich hook/runtime semantics
- edge cache policy details
- full client architecture symmetry with Postgres


## Immediate Implications

1. `foundation-base` should own the new normalized authoring/generation contract.
2. `statstrade-core` should provide the first recovered vertical slice.
3. `statstrade-v1` and `gw-v2` should inform helper generation and policy generation.
4. `xt.db` should be treated as a preserved backbone subsystem, not a temporary bridge.
5. The first client-facing target should be a minimal Dashboard/List link contract driven by `:db/sync`.
