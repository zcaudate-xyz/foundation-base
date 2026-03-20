# PG Sync First Slice

## Goal

Recover one end-to-end slice that proves:

- PG-first entity/function authoring
- named server mutations
- server-side policy only
- `:db/sync` server-to-client convergence
- client/dashboard link refresh behavior
- page-level testability before full UI work


## Recommended Slice

Use a `Space`-style slice as the first recovery target.

Why:

- `gw-v2` already has a strong `Space` entity and helper function pattern in
  [type_space_base.clj](/Users/chris/Development/greenways/gw-v2/backend/src/gwdb/platform/spaces/type_space_base.clj)
- it naturally supports dashboard/list flows
- it naturally supports membership and filtered queries
- it matches the direction toward collaborative/workspace applications


## First Slice Shape

### Entity

- `Space`

### Mutations

- `create-space`
- `update-space`
- `add-space-member`
- `remove-space-member`

### Queries

- `space-summary`
- `space-members`
- `space-activity` or a placeholder summary block

### Client Page Shape

- `Dashboard`
  - summary block
  - members list block
  - filtered members list
  - membership action

### Sync Inputs

- `:db/sync` events touching:
  - `Space`
  - `Access`
  - `AccessRole` or equivalent member table
  - `User`


## Acceptance Criteria

### Server

1. The entity and helper functions can be represented from a thin PG-first spec.
2. The mutation path is via named server functions only.
3. The mutation causes canonical database change.
4. The mutation results in canonical `:db/sync` visibility on the client side.

### Client Link

1. A Dashboard link can load the initial blocks.
2. The link can invoke a named action.
3. The link enters `:acting`.
4. The link receives `:db/sync`.
5. The link refreshes affected blocks.
6. The link returns to `:ready`.

### Test

1. The dashboard link can be tested without full UI rendering.
2. The test asserts state transitions and data refresh.


## Recovery Steps

### Step 1. Normalize The Entity Shape

Derive a normalized entity spec for `Space` from the current `gw-v2` entity file.

Deliverable:

- thin spec document or registry entry

### Step 2. Define Generated Helper Output

Specify which helper functions are expected to be generated:

- `create-space`
- `update-space`
- `purge-space`
- `get-space`
- `list-spaces`
- `add-space-member`
- `remove-space-member`

Deliverable:

- generated output target list

### Step 3. Define Dashboard Link Contract

The first dashboard link should expose:

- summary query
- members query
- invite/remove actions
- `:db/sync` listen list

Deliverable:

- Dashboard link spec

### Step 4. Define Runtime Test

Write the first dashboard link convergence test before full UI integration.

Deliverable:

- test scenario document
- later, executable runtime test


## Suggested Test Scenario

1. Load dashboard with `space-id`.
2. Summary and members blocks become ready.
3. Invoke `add-space-member`.
4. Link enters `:acting`.
5. Inject `:db/sync` affecting membership tables.
6. Members block refreshes.
7. Link returns to `:ready`.
8. New member appears in the dashboard state.


## Why This Slice

- small enough to stabilize quickly
- representative of dashboards/lists/filtered queries
- exercises helper function generation
- exercises client convergence through `:db/sync`
- avoids overcommitting to old `statstrade-core` auth assumptions
