# `xt.lib.cell` Port Plan (Task-First)

## Scope

This plan intentionally narrows the async model to **`x:task-*` only** as the first-class surface.

- No Promise-first API in the core design.
- Existing `k/for:async` remains a compatibility mechanism during migration.
- JS/Lua/Python backends implement a common task contract and are free to use host-native primitives internally.

---

## 1) Task contract for xtalk

## Task states

- `"pending"`
- `"ok"`
- `"error"`
- `"cancelled"`

## Proposed xtalk primitives

- `x:task-run`
- `x:task-then`
- `x:task-catch`
- `x:task-finally`
- `x:task-cancel`
- `x:task-status`
- `x:task-await`
- `x:task-from-async` (bridge for existing `k/for:async`)

## Behavioral contract

- `x:task-run` returns a task handle.
- `x:task-then`/`x:task-catch` return chained task handles.
- `x:task-finally` always runs after resolution/rejection.
- `x:task-status` is query-only and does not mutate.
- `x:task-cancel` is best-effort/cooperative.
- `x:task-await` is optional blocking helper where runtime supports it.

---

## 2) Rewriting `k/for:async` call sites

## Why rewrite

`k/for:async` is workable for single success/error branches but is awkward for:

- linear chains
- fan-in/fan-out (`all`/`race` style)
- cancellation propagation
- reusable composition across runtime boundaries

## Rewrite example (`load-tasks-single` pattern)

```clojure
(defn.xt load-tasks-single-task
  [loader id hook-fn complete-fn loop-fn]
  (var #{tasks loading completed} loader)
  (var task-entry (k/get-key tasks id))
  (k/set-key loading id true)
  (return
   (-> (x:task-run (fn [] (-/task-load task-entry)))
       (x:task-then
        (fn [res]
          (k/del-key loading id)
          (k/set-key completed id true)
          (when hook-fn (hook-fn id true))
          (if loop-fn
            (loop-fn loader hook-fn complete-fn)
            res)))
       (x:task-catch
        (fn [err]
          (k/del-key loading id)
          (k/set-key loader "errored" id)
          (when hook-fn     (hook-fn id false))
          (when complete-fn (complete-fn err))
          nil)))))
```

## Migration bridge

```clojure
(x:task-from-async
  (fn [resolve reject]
    (k/for:async [[v e] expr]
      {:success (resolve v)
       :error   (reject e)})))
```

---

## 3) Protocol/worker integration

Task operations are normalized into protocol frames:

- accepted: `{:op "task" :ref task-id :status "accepted"}`
- progress: `{:op "task" :ref task-id :status "pending" :body {...}}`
- done: `{:op "result" :id call-id :status "ok" :body value}`
- error: `{:op "result" :id call-id :status "error" :body err}`

Worker abstraction remains transport-level:

- spawn endpoint
- send/receive frames
- terminate endpoint

Cell core only sees normalized task lifecycle and protocol frames.

---

## 4) Backend start-point implementations

## JavaScript backend

Implementation strategy:

- `x:task-run` wraps thunk in native `Promise.resolve().then(...)`
- track task state in an attached `__xt_status` field
- `x:task-then/catch/finally` chain Promise
- `x:task-cancel` calls optional `cancel` hook (or no-op)

## Lua backend

Implementation strategy:

- represent task as table with `state/value/error`
- `x:task-run` executes thunk via `pcall` (phase 1)
- `x:task-then/catch/finally` dispatch based on task state
- `x:task-cancel` sets cancelled state cooperatively
- phase 2 can attach coroutine scheduler for true async continuation

## Python backend

Implementation strategy:

- represent task as dict with `status/value/error`
- `x:task-run` executes thunk with `try/except` (phase 1)
- `x:task-then/catch/finally` chain task wrappers
- `x:task-cancel` sets cooperative cancelled state
- phase 2 can wrap `concurrent.futures`/`asyncio` for native async scheduling

---

## 5) Phased rollout

1. Add xtalk `x:task-*` ops and backend mappings (JS/Lua/Python).
2. Add `x:task-from-async` bridge for incremental migration.
3. Start porting `js.cell` paths from `k/for:async` to `x:task-*`.
4. Normalize worker/call handling to always return/stream task lifecycle.
5. Add runtime-specific async scheduler upgrades (Lua coroutine queue, Python futures).

This gives a single portable async abstraction without forcing Promise as public xtalk API.
