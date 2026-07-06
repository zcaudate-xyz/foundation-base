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
