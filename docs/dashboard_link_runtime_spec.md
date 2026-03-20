# Dashboard Link Runtime Spec

## Purpose

Define the smallest useful client-facing contract for a Dashboard page that is:

- driven by named server calls
- refreshed only by server-originated `:db/sync`
- testable before full UI implementation


## Core Rule

The Dashboard link does not emit sync events.

It only:

- performs named server calls
- listens to `:db/sync`
- maintains page data state


## Dashboard Link Shape

### Compiled Spec

Conceptual form:

```clojure
(deflink.js Dashboard
  {:queries {:summary summary-query
             :members members-query}
   :actions {:invite-member invite-member!
             :remove-member remove-member!}
   :listen  ["Space" "Access" "AccessRole" "User"]})
```

The output of `deflink.js` should be a plain object spec.


## Runtime API

The first runtime API should stay minimal.

### Live Link State

```clojure
{:data   {:summary ...
          :members ...}
 :status :ready      ;; or :loading | :acting | :error
 :error  nil}
```

### Live Link Methods

```clojure
{:load! fn
 :set!  fn
 :act!  fn}
```

Meaning:

- `load!`
  - run the dashboard queries
- `set!`
  - update params or filters and rerun
- `act!`
  - call a named server action


## Runtime Behavior

### Initial Load

1. Page creates the link instance.
2. Page calls `load!`.
3. Link enters `:loading`.
4. Queries resolve.
5. Link enters `:ready`.

### Mutation

1. Page calls `act!`.
2. Link enters `:acting`.
3. Server performs named mutation.
4. Server later emits `:db/sync`.
5. Link receives matching `:db/sync`.
6. Link reloads or refreshes affected queries.
7. Link returns to `:ready`.

### Error

If action or refresh fails:

- link enters `:error`
- `:error` contains the failure


## Page Interaction Model

The page should only:

- create the link
- call `load!`
- read `state`
- call `set!`
- call `act!`

The page should not:

- subscribe to raw `:db/sync` directly
- manage query refresh logic
- know transport details


## Dashboard Test Model

The first executable test target should be a link-level convergence test,
not a full rendered page test.

### Test Scenario

1. Create a Dashboard link with initial params.
2. Call `load!`.
3. Assert `:status` becomes `:ready`.
4. Call `act! :invite-member`.
5. Assert `:status` becomes `:acting`.
6. Inject matching `:db/sync`.
7. Assert the link refreshes data.
8. Assert `:status` returns to `:ready`.

### Pseudo-Test

```clojure
(fact "dashboard link converges from server calls and :db/sync"
  (let [link (dashboard-runtime/create
              Dashboard
              {:space-id "space-1"})]

    (.load! link)
    (:status (dashboard-runtime/state link))
    => :ready

    (.act! link :invite-member
           {:email "new@example.com"
            :role "member"})
    (:status (dashboard-runtime/state link))
    => :acting

    (dashboard-runtime/apply-sync!
     link
     {:topic ":db/sync"
      :tables ["AccessRole" "User"]})

    (:status (dashboard-runtime/state link))
    => :ready))
```


## Why This Is Enough For Now

This model is intentionally narrow.

It is enough to:

- prove the client/server sync contract
- support dashboards, lists, and filtered queries
- define a testable page data interface

It avoids premature complexity in:

- full page DSL design
- rich hook systems
- UI implementation details
- advanced edge caching semantics


## Immediate Next Step

Implement the first Dashboard link runtime test against the first `Space`
vertical slice.
