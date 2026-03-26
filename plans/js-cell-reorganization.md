# Implementation Plan: js.cell-v2 Reorganization

## Overview

This plan outlines the introduction of `js.cell-v2` as a new, event-centered foundation extracted from the existing `js.cell` design. Rather than reorganizing the current `js.cell` in place, the plan preserves the working `js.cell` API and uses a fresh `js.cell-v2` namespace to provide a cleaner plug-and-play architecture.

---

## Current State

### js.cell Structure

```
ref/foundation/src/js/
├── cell.clj              # Main API (cell/model/view operations)
└── cell/
    ├── base_fn.clj       # Worker-side routes (ping, echo, eval, state)
    ├── base_internal.clj # Worker message processing
    ├── base_util.clj     # Utilities
    ├── impl_common.clj   # Cell/model/view access
    ├── impl_model.clj    # Model lifecycle, dependency tracking
    ├── link_fn.clj       # Generated route callers
    ├── link_raw.clj      # Web Worker connection, message handling
    └── playground.clj    # Testing infrastructure
```

### Related Extension Patterns

```
external application layers
├── impl/
│   ├── base-api.clj      # API task handling with logging/callbacks
│   ├── base-remote.clj   # HTTP fetch wrapper for remote calls
│   ├── base-event.clj    # Event types (EV_DB_SYNC, EV_REMOTE, etc.)
│   ├── base-stream.clj   # WebSocket stream management
│   ├── base-sqlite.clj   # SQLite operations
│   └── user-event.clj    # Sync/callback wrappers
├── full/
│   ├── link-remote.clj   # Remote API route templates
│   └── link-local.clj    # Local route templates
└── app/
    └── runtime.clj       # Runtime initialization
```

---

## Goals

1. **Create a clean core**: Build `js.cell-v2` around an event kernel instead of the current worker-first file layout
2. **Support adapters**: Make routes, stores, and remotes pluggable via registries
3. **Preserve existing systems**: Leave `js.cell` working while new code is extracted
4. **Support multiple stores/remotes**: Allow multiple cache backends and remote protocols to coexist
5. **Prepare for migration**: Make it possible to add compatibility adapters later without forcing an in-place rewrite

---

## Proposed New Structure

```
ref/foundation/src/js/
├── cell.clj                    # Existing API, unchanged for now
├── cell_v2.clj                 # New main API
└── cell_v2/
    ├── event.clj               # Event constants, groups, listeners, dispatch
    ├── route.clj               # Route registry and dispatch
    ├── store.clj               # Store adapter registry
    ├── remote.clj              # Remote adapter registry
    └── adaptor/
        ├── worker.clj          # Future: worker transport adaptor
        ├── legacy.clj          # Future: compatibility adaptor
        └── sqlite.clj          # Future: SQLite/cache adaptor
```

---

## Core Design

`js.cell-v2` should be built around a small system record with registries and an event bus:

```clojure
{:events  ...
 :routes  ...
 :stores  ...
 :remotes ...
 :state   ...}
```

The central principle is that the event system is the kernel, and route/store/remote behavior is attached through adaptors rather than baked into the core.

### Event Design

The original event topics from `js.cell` have reusable semantics and should be extracted, not discarded:

- `@/::INIT`
- `@/::STATE`
- `cell/::LOCAL`
- `cell/::REMOTE`
- `db/::SYNC`
- `db/::REMOVE`
- `db/::VIEW`

`js.cell-v2` should preserve these ideas while providing a clearer grouping API:

- lifecycle events
- local action events
- remote action events
- store synchronization events

The first implementation slice should keep this simple: constants, topic grouping helpers, listener registration, and event dispatch.

### Adapter Design

`js.cell-v2` should treat these as pluggable registries:

- **Routes**: local command/query handlers
- **Stores**: memory, SQLite, IndexedDB, cache layers
- **Remotes**: HTTP, RPC, worker-bridge, or any custom protocol

The core should know how to register, look up, and invoke an adaptor. The adaptor owns transport-specific data shapes.

---

## New Module Specifications

### 1. `js.cell-v2.event`

**Purpose**: Event constants, classification, subscriptions, and dispatch

**Functions**:
```clojure
(make-bus [])                              ; Create event bus
(add-listener [bus listener-id pred f])    ; Register listener
(remove-listener [bus listener-id])        ; Remove listener
(list-listeners [bus])                     ; List listener ids
(emit [bus event])                         ; Dispatch event
(event [topic body meta])                  ; Build canonical event map
(event-topic [event])                      ; Read topic
(event-lifecycle? [topic])                 ; Topic grouping helper
(event-local? [topic])                     ; Topic grouping helper
(event-remote? [topic])                    ; Topic grouping helper
(event-store? [topic])                     ; Topic grouping helper
```

**Constants**:
```clojure
EV_INIT
EV_STATE
EV_LOCAL
EV_REMOTE
EV_DB_SYNC
EV_DB_REMOVE
EV_DB_VIEW
```

### 2. `js.cell-v2.route`

**Purpose**: Route registration and dispatch for local handlers

**Functions**:
```clojure
(make-registry [])
(register-route [registry route-id handler opts])
(unregister-route [registry route-id])
(get-route [registry route-id])
(list-routes [registry])
(dispatch-route [registry route-id args ctx])
```

### 3. `js.cell-v2.store`

**Purpose**: Store adaptor registration and dispatch

**Store Entry Format**:
```clojure
{:key key
 :read fn
 :write fn
 :sync fn
 :clear fn
 :meta map}
```

**Functions**:
```clojure
(make-registry [])
(register-store [registry key adaptor])
(get-store [registry key])
(list-stores [registry])
(store-read [registry key input])
(store-write [registry key input])
(store-sync [registry key input])
(store-clear [registry key input])
```

### 4. `js.cell-v2.remote`

**Purpose**: Remote adaptor registration and dispatch

**Functions**:
```clojure
(make-registry [])
(register-remote [registry key adaptor])
(get-remote [registry key])
(list-remotes [registry])
(remote-call [registry key input])
```

### 5. `js.cell-v2`

**Purpose**: Main entry point and constructor for a composable `js.cell-v2` system

**Functions**:
```clojure
(make-system [opts])                       ; Create v2 system
(add-event-listener [system key pred f])   ; Delegate to event bus
(emit [system event])                      ; Delegate to event bus
(register-route [system route-id f opts])  ; Delegate to route registry
(register-store [system key adaptor])      ; Delegate to store registry
(register-remote [system key adaptor])     ; Delegate to remote registry
```

---

## File Migration Guide

### New Files to Create

| New File | Based On | Description |
|----------|----------|-------------|
| `js.cell-v2` | `js.cell` | Main v2 API |
| `js.cell-v2.event` | `js.cell.base-util` + existing event conventions | Event kernel |
| `js.cell-v2.route` | `js.cell.base-fn` | Route registry |
| `js.cell-v2.store` | cache/store integration patterns | Store adapter registry |
| `js.cell-v2.remote` | remote call integration patterns | Remote adapter registry |
| `js.cell-v2.adaptor.worker` | `js.cell.base-internal` + `js.cell.link-raw` | Future worker adaptor |
| `js.cell-v2.adaptor.legacy` | legacy systems | Future compatibility adaptor |

### Future Compatibility Work

| File | Change |
|------|--------|
| Existing event producers | Eventually adapt to `js.cell-v2.event` |
| Existing remote wrappers | Eventually adapt to `js.cell-v2.remote` |
| Existing store/cache wrappers | Eventually adapt to `js.cell-v2.store` |
| Existing application runtime | Eventually migrate via adapter layer, not direct rewrites |

---

## Backward Compatibility

The initial `js.cell-v2` rollout should avoid compatibility risk by leaving `js.cell` untouched. Compatibility work belongs in optional adapters, not in forced namespace moves.

This means:

1. No in-place renames for existing `js.cell` files
2. No deprecation warnings yet
3. Compatibility comes later through explicit adapter namespaces
4. `js.cell-v2` is additive until migration paths are proven

---

## Implementation Order

1. **Phase 1: Plan Revision**
   - Reframe the effort around `js.cell-v2`
   - Correct the current `js.cell` file layout in docs
   - Limit the first implementation slice to core registries and events

2. **Phase 2: Core Event System**
   - Create `js.cell-v2.event`
   - Extract event constants and topic classification helpers
   - Add listener registration and event emit logic

3. **Phase 3: Adapter Registries**
   - Create `js.cell-v2.route`
   - Create `js.cell-v2.store`
   - Create `js.cell-v2.remote`

4. **Phase 4: Main API**
   - Create `js.cell-v2`
   - Provide a constructor that wires event, route, store, and remote registries

5. **Phase 5: Tests**
   - Add focused `js.cell-v2` tests
   - Re-run relevant existing `js.cell` tests to ensure isolation

6. **Phase 6: Future Adaptors**
   - Add worker transport adaptor
   - Add compatibility adaptor
   - Add SQLite/store adaptors
   - Add remote protocol adaptors

---

## Testing Strategy

1. **Unit Tests**
   - Test event construction, listener registration, and event grouping helpers
   - Test route/store/remote registry behavior independently

2. **Integration Tests**
   - Test `js.cell-v2/make-system`
   - Verify registries and event bus compose correctly

3. **Isolation Checks**
   - Re-run the existing `js.cell` test namespaces
   - Verify `js.cell-v2` does not break current `js.cell`

---

## Key Design Decisions

### 1. New Namespace, Not In-Place Reorg

`js.cell-v2` is additive. The existing `js.cell` remains the stable system while the new design is proven.

### 2. Event System as the Kernel

The event bus is the primary integration point. Routes, stores, and remotes all plug into it instead of owning the architecture.

### 3. Adapters Own Specific Shapes

HTTP payload details, SQLite sync shapes, and worker message formats should live in adaptors, not in the core registries.

### 4. Multiple Stores and Remotes

Registries allow multiple store and remote implementations to coexist under stable keys.

---

## Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Over-designing too early | Keep the first slice to event core + registries |
| Breaking existing `js.cell` | Build only additive `js.cell-v2` namespaces |
| Adapter leakage into core | Keep transport-specific logic out of initial modules |
| Migration stalls | Provide explicit future adapter phases in the plan |

---

## Success Criteria

1. `js.cell-v2` core modules exist and load
2. New `js.cell-v2` tests pass
3. Relevant existing `js.cell` tests still pass
4. No behavior changes are made to current `js.cell`
5. The plan clearly defines future adapter-based migration

---

## Future Enhancements

After the initial `js.cell-v2` core lands:

1. **Worker Adapter**: Bridge existing worker transport into `js.cell-v2`
2. **Compatibility Adapter**: Translate legacy remotes/stores/events into v2 registries
3. **SQLite Adapter**: First-class cached store implementation
4. **HTTP Adapter**: Generalized remote protocol implementation
5. **Model/View Layer**: Re-introduce higher-level cell semantics on top of the v2 core
