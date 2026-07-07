(ns documentation.std-lib-collection
  (:require [std.lib.collection :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.collection` provides extended collection utilities beyond clojure.core, including:

- Advanced map operations (map-keys, map-vals, filter-keys, filter-vals)
- Nested data structure manipulation (merge-nested, flatten-nested)
- Sequence utilities (queue, seqify, unlazy)
- Tree operations (tree-flatten, tree-nestify)
- Data diffing and patching (diff, diff:patch, diff:unpatch)
"

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Working with maps"}]]

"Most day-to-day use starts with map transformations. `map-keys` and `map-vals` apply a function to every key or value, while `filter-keys` and `filter-vals` keep entries that satisfy a predicate."

(fact "transform map keys and values"
  (map-keys inc {0 :a 1 :b 2 :c})
  => {1 :a 2 :b 3 :c}

  (map-vals inc {:a 1 :b 2 :c 3})
  => {:a 2 :b 3 :c 4})

(fact "filter entries by key or value"
  (filter-keys even? {0 :a 1 :b 2 :c})
  => {0 :a 2 :c}

  (filter-vals even? {:a 1 :b 2 :c 3})
  => {:b 2})

(fact "rename and qualify keys"
  (rename-keys {:a 1 :b 2} {:a :name :b :id})
  => {:name 1 :id 2}

  (qualify-keys {:a 1 :b 2} :user)
  => #:user{:a 1 :b 2}

  (unqualify-keys {:user/a 1 :user/b 2})
  => {:a 1 :b 2})

[[:section {:title "Nested maps"}]]

"Configuration data is often deeply nested. `merge-nested` recurses into maps, `assoc-new` only adds missing keys, and `dissoc-nested` removes a path and collapses empty parents."

(fact "merge nested configurations"
  (merge-nested {:server {:host "localhost" :port 8080}}
                {:server {:port 3000}})
  => {:server {:host "localhost" :port 3000}}

  (merge-nested-new {:server {:host "localhost"}}
                    {:server {:port 3000}})
  => {:server {:host "localhost" :port 3000}})

(fact "update a nested path safely"
  (dissoc-nested {:a {:b {:c 1 :d 2}}}
                 [:a :b :c])
  => {:a {:b {:d 2}}})

[[:section {:title "Tree-shaped data"}]]

"Flat maps with slash-separated keys can be nested into trees with `tree-nestify`, and deep trees can be flattened back with `tree-flatten`."

(fact "flatten and nest tree keys"
  (tree-flatten {:a {:b {:c 1 :d 2}
                     :e {:f 3}}})
  => {:a/b/c 1 :a/b/d 2 :a/e/f 3}

  (tree-nestify {:a/b/c 1 :a/b/d 2})
  => {:a {:b {:c 1 :d 2}}})

[[:section {:title "Diffing and patching"}]]

"`diff` compares two maps and returns additions (`:+`), removals (`:-`), and changes (`:>` and `:<`). The resulting patch can be applied or reversed."

(fact "compute and apply a diff"
  (diff {:a 2} {:a 1})
  => {:+ {} :- {} :> {[:a] 2}}

  (let [old {:a {:b 1 :d 3}}
        new {:a {:c 2 :d 4}}
        d   (diff new old true)]
    (diff:patch old d))
  => {:a {:c 2 :d 4}}

  (let [old {:a {:b 1 :d 3}}
        new {:a {:c 2 :d 4}}
        d   (diff new old true)]
    (diff:unpatch new d))
  => {:a {:b 1 :d 3}})

[[:section {:title "Sequences and queues"}]]

"`queue` builds a persistent queue, `seqify` normalises values to sequences, and `unlazy` forces lazy sequences."

(fact "use a persistent queue"
  (-> (queue 1 2 3 4)
      pop
      vec)
  => [2 3 4])

(fact "normalise values to sequences"
  (seqify 1)
  => [1]

  (seqify [1 2])
  => [1 2]

  (unlazy (map inc [1 2 3]))
  => [2 3 4])

[[:section {:title "End-to-end: reshaping config data"}]]

"Combining the utilities above makes it easy to reshape data. Here a flat keystore is transformed into a nested login record using `find-templates` and `transform`."

(fact "transform a keystore into a nested record"
  (transform {:keystore {:hash  "{{hash}}"
                         :salt  "{{salt}}"
                         :email "{{email}}"}
              :db       {:login {:user {:hash "{{hash}}"
                                        :salt "{{salt}}"}
                                 :value "{{email}}"}}}
             [:keystore :db]
             {:hash "1234"
              :salt "ABCD"
              :email "a@a.com"})
  => {:login {:user {:hash "1234"
                     :salt "ABCD"}
              :value "a@a.com"}})

[[:chapter {:title "Type Predicates" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [hash-map? lazy-seq? cons? form?]}]]

[[:chapter {:title "Sequence Utilities" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [queue seqify unseqify unlazy]}]]

[[:chapter {:title "Map Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [map-keys map-vals map-juxt pmap-vals map-entries pmap-entries]}]]

[[:chapter {:title "Map Filtering" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [filter-keys filter-vals keep-vals]}]]

[[:chapter {:title "Key Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [qualified-keys unqualified-keys qualify-keys unqualify-keys rename-keys]}]]

[[:chapter {:title "Nested Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [assoc-new merge-nested merge-nested-new dissoc-nested flatten-nested]}]]

[[:chapter {:title "Tree Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [tree-flatten tree-nestify tree-nestify:all reshape find-templates transform]}]]

[[:chapter {:title "Diff Operations" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [diff diff:changes diff:new diff:changed diff:patch diff:unpatch]}]]

[[:chapter {:title "Collection Manipulation" :link "std.lib.collection"}]]

[[:api {:namespace "std.lib.collection"
        :only [index-at element-at insert-at remove-at split-by transpose deduped? unfold]}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_collection_summary.md
;; sha256: 5013b8c41a69d0244cdcbf0f8b4e79cb0333baf9007eded046d146029d100bbb
[[:chapter {:title "std.lib.collection: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-collection-summary-md"}]]
"## std.lib.collection: A Comprehensive Summary\n\nThe `std.lib.collection` namespace provides a rich set of utility functions for working with Clojure's core data structures (maps, sequences, vectors, sets). It extends the functionality of `clojure.core` with specialized operations for nested data manipulation, key/value transformations, sequence processing, and advanced map differencing and patching. This module aims to simplify common collection-related tasks and provide more powerful tools for data transformation.\n\n### Core Concepts:\n\n*   **Collection Predicates:** Functions to check the type of various collections (e.g., `hash-map?`, `lazy-seq?`, `cons?`, `form?`).\n*   **Sequence Utilities:** Functions for manipulating sequences, including `seqify`, `unseqify`, `unlazy`, `insert-at`, `remove-at`, `deduped?`, and `unfold`.\n*   **Map Transformations:** Extensive functions for transforming map keys, values, and entries, including `map-keys`, `map-vals`, `map-juxt`, `map-entries`, `pmap-vals`, `pmap-entries`, `rename-keys`, `filter-keys`, `filter-vals`, `keep-vals`, `transpose`.\n*   **Namespaced Key Handling:** Utilities for working with namespaced keywords in maps, such as `qualified-keys`, `unqualified-keys`, `qualify-keys`, `unqualify-keys`.\n*   **Nested Map Operations:** Powerful functions for merging, associating, and disassociating values in nested maps, including `merge-nested`, `merge-nested-new`, `dissoc-nested`, `flatten-nested`, `tree-flatten`, `tree-nestify`, `tree-nestify:all`.\n*   **Map Differencing and Patching:** A sophisticated set of functions (`diff`, `diff:changes`, `diff:new`, `diff:changed`, `diff:patch`, `diff:unpatch`) for comparing two maps, identifying differences, and applying/reverting patches.\n*   **Data Transformation Pipelines:** Functions like `reshape` and `transform` (with `find-templates`) for defining and applying complex data transformations based on schemas or templates.\n\n### Key Functions:\n\n*   **`hash-map?`, `lazy-seq?`, `cons?`, `form?`**: Predicates for checking collection types.\n*   **`queue`**: Creates a `clojure.lang.PersistentQueue`.\n*   **`seqify`, `unseqify`**: Convert non-sequences to sequences and vice-versa.\n*   **`unlazy`**: Forces evaluation of a lazy sequence.\n*   **`map-keys`, `map-vals`, `map-juxt`, `map-entries`**: Apply functions to map keys, values, or entries. `pmap-vals` and `pmap-entries` provide parallel versions.\n*   **`rename-keys`**: Renames keys in a map based on a mapping.\n*   **`filter-keys`, `filter-vals`, `keep-vals`**: Filter map entries based on keys or values.\n*   **`qualified-keys`, `unqualified-keys`, `qualify-keys`, `unqualify-keys`**: Manipulate namespaced keywords in maps.\n*   **`assoc-new`**: Associates a key-value pair only if the key is not already present or its value is `nil`.\n*   **`merge-nested`, `merge-nested-new`**: Recursively merge nested maps. `merge-nested-new` only merges if the target key doesn't exist.\n*   **`dissoc-nested`**: Recursively disassociates keys from nested maps, removing empty intermediate maps.\n*   **`flatten-nested`**: Flattens all elements of a collection into a single sequence.\n*   **`tree-flatten`, `tree-nestify`, `tree-nestify:all`**: Convert between flat (path-based keys) and nested map representations.\n*   **`reshape`**: Moves values within a map according to a specified transformation table.\n*   **`find-templates`, `transform-fn`, `transform`**: Tools for defining and applying data transformations using template strings.\n*   **`empty-record`**: Creates an empty instance of a `defrecord`.\n*   **`transpose`**: Swaps keys and values in a map.\n*   **`index-at`, `element-at`**: Find the index or element matching a predicate in a collection.\n*   **`insert-at`, `remove-at`**: Insert or remove elements at a specific index in a vector.\n*   **`deduped?`**: Checks if all elements in a collection are unique.\n*   **`unfold`**: Generates a sequence by repeatedly applying a function to a seed value.\n*   **`diff`, `diff:changes`, `diff:new`, `diff:changed`, `diff:patch`, `diff:unpatch`**: Functions for computing differences between nested maps and applying/reverting those differences.\n\n### Usage Pattern:\n\nThis namespace is a utility belt for any Clojure project that deals heavily with data manipulation. It's particularly valuable for:\n*   **Configuration Management:** Merging and transforming configuration maps.\n*   **State Management:** Efficiently updating and tracking changes in complex application state.\n*   **Data Processing Pipelines:** Building transformations for data flowing through an application.\n*   **API Development:** Reshaping data structures for different API endpoints or external systems.\n*   **Metaprogramming:** Manipulating data structures that represent code or schemas.\n\nBy providing these powerful and often recursive collection utilities, `std.lib.collection` significantly enhances Clojure's already strong data-oriented programming capabilities."
;; END merged documentation: plans/slop/summary/std_lib_collection_summary.md
