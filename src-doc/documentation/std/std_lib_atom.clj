(ns documentation.std-lib-atom
  (:require [std.lib.atom :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.atom` provides enhanced atom operations for managing state with:

- Swapping with return values
- Nested path updates
- Batch operations
- Change tracking
- Derived atoms
"

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Nested reads and writes"}]]

"`std.lib.atom` extends `clojure.core/atom` with path-aware access. `atom:get`, `atom:mget`, and `atom:keys` read nested values; `atom:put`, `atom:set`, and `atom:set-keys` write them."

(fact "read nested values"
  ^{:refer std.lib.atom/atom:get :added "3.0"}
  (atom:get (atom {:a {:b 1 :c 2}})
            [:a :c])
  => 2

  ^{:refer std.lib.atom/atom:mget :added "3.0"}
  (atom:mget (atom {:a {:b 1 :c 2}})
             [[:a :b] [:a :c]])
  => [1 2]

  ^{:refer std.lib.atom/atom:keys :added "3.0"}
  (atom:keys (atom {:a {:b 1 :c 2}})
             [:a])
  => [:b :c])

(fact "write nested values"
  ^{:refer std.lib.atom/atom:put :added "3.0"}
  (atom:put (atom {:a {:b 1 :c 2}})
            []
            {:a {:d 3}})
  => [{:a {:b 1 :c 2}}
      {:a {:b 1 :c 2 :d 3}}]

  ^{:refer std.lib.atom/atom:set :added "3.0"}
  (atom:set (atom {:a {:b 1 :c 2}})
            [:a :b] 3
            [:a :d] 5)
  => '([[:a :b] 1 3]
       [[:a :d] nil 5]))

[[:section {:title "Change tracking"}]]

"Every write returns a change log. `atom:set-changed` and `atom:put-changed` turn that log into a concise `:changed` summary."

(fact "summarise what changed"
  ^{:refer std.lib.atom/atom:set-changed :added "3.0"}
  (->> (atom:set (atom {:a {:b 1 :c 2}})
                 [:a] {:b 3 :d 4})
       (atom:set-changed))
  => [:changed {:a {:b 3 :d 4}}]

  ^{:refer std.lib.atom/atom:put-changed :added "3.0"}
  (->> (atom:put (atom {:a {:b 1 :c 2}})
                 []
                 {:a {:d 3}})
       (atom:put-changed))
  => [:changed {:a {:d 3}}])

[[:section {:title "Batch updates"}]]

"`atom:batch` applies many operations in one go: `:set`, `:swap`, `:put`, and `:delete`. It returns the full change log and updates the atom."

(fact "apply several operations at once"
  ^{:refer std.lib.atom/atom:batch :added "3.0"}
  (def -atm- (atom {:a {:b 1 :c 2}}))

  (atom:batch -atm-
              [[:set  [:a :b] 3]
               [:swap [:a :c] + 10]
               [:put  [:a :d] {:x 8 :y 9}]
               [:delete [:a :d :x]]])
  => '([[:a :b] 1 3]
       [[:a :c] 2 12]
       [[:a :d] nil {:x 8 :y 9}]
       [[:a :d :x] 8 nil]))

[[:chapter {:title "Basic Operations" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [swap-return! update-diff]}]]

[[:chapter {:title "Nested Access" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:keys atom:get atom:mget]}]]

[[:chapter {:title "Put and Set" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:put atom:set atom:set-keys]}]]

[[:chapter {:title "Change Tracking" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:set-changed atom:put-changed]}]]

[[:chapter {:title "Batch Operations" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:batch atom:clear atom:delete]}]]

[[:chapter {:title "Derived Atoms" :link "std.lib.atom"}]]

[[:api {:namespace "std.lib.atom"
        :only [atom:cursor atom:derived]}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_atom_summary.md
;; sha256: 46776c5440aa7730f8818ad5cf15e503f5b8bc9d5c164ee453c8f7bdf0e7ac8b
[[:chapter {:title "std.lib.atom: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-atom-summary-md"}]]

"The `std.lib.atom` namespace provides a set of extended utilities for working with Clojure's `atom` data structure, focusing on nested updates, batch operations, and derived/cursor-like functionalities. It aims to simplify common patterns of managing mutable state, especially when dealing with deeply nested maps or when needing to track changes."

[[:section {:title "Core Concepts:" :link "merged-plans-slop-summary-std-lib-atom-summary-md-core-concepts"}]]

"*   **Nested State Management:** Many functions in this namespace are designed to operate on nested data within an atom, using paths (vectors of keys) to specify target locations.\n*   **Transactional Updates:** Operations like `swap-return!` and `atom:batch` ensure that updates to the atom are atomic and provide mechanisms to inspect the changes or the new state.\n*   **Change Tracking:** Functions like `atom:put-changed` and `atom:set-changed` help identify what parts of the atom's state have actually been modified after an operation.\n*   **Derived State:** `atom:derived` allows creating atoms whose values are computed from other atoms, automatically updating when their dependencies change.\n*   **Cursors:** `atom:cursor` provides a way to focus on a specific part of a larger atom, allowing updates to that sub-part to be reflected in the parent atom and vice-versa."

[[:section {:title "Key Functions and Macros:" :link "merged-plans-slop-summary-std-lib-atom-summary-md-key-functions-and-macros"}]]

"*   **`update-diff`**:\n    *   **Purpose:** Updates a value at a specific path within a map, returning the difference (`[old-value new-value]`) and the new map.\n    *   **Usage:** `(update-diff {:a {:b 1}} [:a] my-update-fn :c 2)`\n*   **`swap-return!`**:\n    *   **Purpose:** Similar to `clojure.core/swap!`, but returns the result of the update function (the \"output\") and optionally the new state of the atom.\n    *   **Usage:** `(swap-return! my-atom (fn [v] [(:some-key v) (update v :count inc)]))`\n*   **`atom:keys`**:\n    *   **Purpose:** Returns the keys of a map at a specified nested path within the atom's dereferenced value.\n    *   **Usage:** `(atom:keys my-atom [:path :to :map])`\n*   **`atom:get`**:\n    *   **Purpose:** Retrieves the value at a specified nested path within the atom's dereferenced value.\n    *   **Usage:** `(atom:get my-atom [:path :to :value])`\n*   **`atom:mget`**:\n    *   **Purpose:** Retrieves multiple values from different nested paths within the atom.\n    *   **Usage:** `(atom:mget my-atom [[:path1] [:path2]])`\n*   **`atom:put`**:\n    *   **Purpose:** Merges a map `m` into the value at `path` within the atom. If `path` is empty, `m` is merged with the root.\n    *   **Usage:** `(atom:put my-atom [:user :profile] {:name \"Alice\"})`\n*   **`atom:set`**:\n    *   **Purpose:** Sets specific values at multiple nested paths within the atom. It takes path-value pairs.\n    *   **Usage:** `(atom:set my-atom [:a :b] 10 [:x :y] 20)`\n*   **`atom:set-keys`**:\n    *   **Purpose:** Sets multiple keys within a map at a given path.\n    *   **Usage:** `(atom:set-keys my-atom [:user] {:name \"Bob\" :age 30})`\n*   **`atom:set-changed`**:\n    *   **Purpose:** Analyzes the output of `atom:set` (a list of `[path old-value new-value]` tuples) and returns a map representing only the changed values.\n    *   **Usage:** `(atom:set-changed outputs)`\n*   **`atom:put-changed`**:\n    *   **Purpose:** Analyzes the output of `atom:put` (`[old-state new-state]`) and returns a map representing only the changed values.\n    *   **Usage:** `(atom:put-changed [old-map new-map])`\n*   **`atom:swap`**:\n    *   **Purpose:** Applies functions to values at multiple nested paths within the atom. It takes path-function pairs.\n    *   **Usage:** `(atom:swap my-atom [:a :count] inc [:b :total] + 10)`\n*   **`atom:delete`**:\n    *   **Purpose:** Deletes values at multiple specified nested paths within the atom.\n    *   **Usage:** `(atom:delete my-atom [:user :email] [:user :address])`\n*   **`atom:clear`**:\n    *   **Purpose:** Clears (sets to `{}`) the value at a specified path, or clears the entire atom if the path is empty.\n    *   **Usage:** `(atom:clear my-atom [:temp :data])`\n*   **`atom:batch`**:\n    *   **Purpose:** Performs a sequence of `set`, `put`, `swap`, or `delete` operations on the atom in a single atomic transaction.\n    *   **Usage:** `(atom:batch my-atom [[:set [:a] 1] [:swap [:b] inc]])`\n*   **`atom:cursor`**:\n    *   **Purpose:** Creates a new atom (the \"cursor\") that reflects a sub-section of a parent atom. Changes to the cursor atom are propagated back to the parent, and changes in the parent are reflected in the cursor.\n    *   **Usage:** `(atom:cursor parent-atom [:path :to :subatom])`\n*   **`atom:derived`**:\n    *   **Purpose:** Creates a new atom whose value is derived by applying a function `f` to the dereferenced values of a collection of other atoms. The derived atom automatically updates when any of its source atoms change.\n    *   **Usage:** `(atom:derived [atom1 atom2] +)`"

[[:section {:title "Usage Pattern:" :link "merged-plans-slop-summary-std-lib-atom-summary-md-usage-pattern"}]]

"This namespace is particularly useful for managing complex application state where:"

"*   State is often represented as deeply nested maps.\n*   Multiple parts of the state need to be updated atomically.\n*   Changes need to be tracked or reacted to.\n*   Specific sub-sections of the state need to be exposed as independent, yet synchronized, atoms."

"By providing these higher-level abstractions, `std.lib.atom` simplifies common state management challenges in Clojure applications."
;; END merged documentation: plans/slop/summary/std_lib_atom_summary.md
