## std.lib.atom: A Comprehensive Summary

The `std.lib.atom` namespace provides a set of extended utilities for working with Clojure's `atom` data structure, focusing on nested updates, batch operations, and derived/cursor-like functionalities. It aims to simplify common patterns of managing mutable state, especially when dealing with deeply nested maps or when needing to track changes.

### Core Concepts:

*   **Nested State Management:** Many functions in this namespace are designed to operate on nested data within an atom, using paths (vectors of keys) to specify target locations.
*   **Transactional Updates:** Operations like `swap-return!` and `atom:batch` ensure that updates to the atom are atomic and provide mechanisms to inspect the changes or the new state.
*   **Change Tracking:** Functions like `atom:put-changed` and `atom:set-changed` help identify what parts of the atom's state have actually been modified after an operation.
*   **Derived State:** `atom:derived` allows creating atoms whose values are computed from other atoms, automatically updating when their dependencies change.
*   **Cursors:** `atom:cursor` provides a way to focus on a specific part of a larger atom, allowing updates to that sub-part to be reflected in the parent atom and vice-versa.

### Key Functions and Macros:

*   **`update-diff`**:
    *   **Purpose:** Updates a value at a specific path within a map, returning the difference (`[old-value new-value]`) and the new map.
    *   **Usage:** `(update-diff {:a {:b 1}} [:a] my-update-fn :c 2)`
*   **`swap-return!`**:
    *   **Purpose:** Similar to `clojure.core/swap!`, but returns the result of the update function (the "output") and optionally the new state of the atom.
    *   **Usage:** `(swap-return! my-atom (fn [v] [(:some-key v) (update v :count inc)]))`
*   **`atom:keys`**:
    *   **Purpose:** Returns the keys of a map at a specified nested path within the atom's dereferenced value.
    *   **Usage:** `(atom:keys my-atom [:path :to :map])`
*   **`atom:get`**:
    *   **Purpose:** Retrieves the value at a specified nested path within the atom's dereferenced value.
    *   **Usage:** `(atom:get my-atom [:path :to :value])`
*   **`atom:mget`**:
    *   **Purpose:** Retrieves multiple values from different nested paths within the atom.
    *   **Usage:** `(atom:mget my-atom [[:path1] [:path2]])`
*   **`atom:put`**:
    *   **Purpose:** Merges a map `m` into the value at `path` within the atom. If `path` is empty, `m` is merged with the root.
    *   **Usage:** `(atom:put my-atom [:user :profile] {:name "Alice"})`
*   **`atom:set`**:
    *   **Purpose:** Sets specific values at multiple nested paths within the atom. It takes path-value pairs.
    *   **Usage:** `(atom:set my-atom [:a :b] 10 [:x :y] 20)`
*   **`atom:set-keys`**:
    *   **Purpose:** Sets multiple keys within a map at a given path.
    *   **Usage:** `(atom:set-keys my-atom [:user] {:name "Bob" :age 30})`
*   **`atom:set-changed`**:
    *   **Purpose:** Analyzes the output of `atom:set` (a list of `[path old-value new-value]` tuples) and returns a map representing only the changed values.
    *   **Usage:** `(atom:set-changed outputs)`
*   **`atom:put-changed`**:
    *   **Purpose:** Analyzes the output of `atom:put` (`[old-state new-state]`) and returns a map representing only the changed values.
    *   **Usage:** `(atom:put-changed [old-map new-map])`
*   **`atom:swap`**:
    *   **Purpose:** Applies functions to values at multiple nested paths within the atom. It takes path-function pairs.
    *   **Usage:** `(atom:swap my-atom [:a :count] inc [:b :total] + 10)`
*   **`atom:delete`**:
    *   **Purpose:** Deletes values at multiple specified nested paths within the atom.
    *   **Usage:** `(atom:delete my-atom [:user :email] [:user :address])`
*   **`atom:clear`**:
    *   **Purpose:** Clears (sets to `{}`) the value at a specified path, or clears the entire atom if the path is empty.
    *   **Usage:** `(atom:clear my-atom [:temp :data])`
*   **`atom:batch`**:
    *   **Purpose:** Performs a sequence of `set`, `put`, `swap`, or `delete` operations on the atom in a single atomic transaction.
    *   **Usage:** `(atom:batch my-atom [[:set [:a] 1] [:swap [:b] inc]])`
*   **`atom:cursor`**:
    *   **Purpose:** Creates a new atom (the "cursor") that reflects a sub-section of a parent atom. Changes to the cursor atom are propagated back to the parent, and changes in the parent are reflected in the cursor.
    *   **Usage:** `(atom:cursor parent-atom [:path :to :subatom])`
*   **`atom:derived`**:
    *   **Purpose:** Creates a new atom whose value is derived by applying a function `f` to the dereferenced values of a collection of other atoms. The derived atom automatically updates when any of its source atoms change.
    *   **Usage:** `(atom:derived [atom1 atom2] +)`

### Usage Pattern:

This namespace is particularly useful for managing complex application state where:
*   State is often represented as deeply nested maps.
*   Multiple parts of the state need to be updated atomically.
*   Changes need to be tracked or reacted to.
*   Specific sub-sections of the state need to be exposed as independent, yet synchronized, atoms.

By providing these higher-level abstractions, `std.lib.atom` simplifies common state management challenges in Clojure applications.