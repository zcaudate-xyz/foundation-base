(ns std.lib.collection-test
  (:use code.test)
  (:require [std.lib.collection :refer :all]
            [std.lib.foundation :as h]))

^{:refer std.lib.collection/hash-map? :added "3.0"}
(fact "Returns `true` if `x` implements `clojure.lang.APersistentMap`."

  (hash-map? {})    => true
  (hash-map? [])    => false)

^{:refer std.lib.collection/lazy-seq? :added "3.0"}
(fact "Returns `true` if `x` implements `clojure.lang.LazySeq`."

  (lazy-seq? (map inc [1 2 3]))  => true
  (lazy-seq? ())    => false)

^{:refer std.lib.collection/cons? :added "3.0"}
(fact "checks if object is instance of `clojure.lang.Cons`"

  (cons? (cons 1 [1 2 3])) => true)

^{:refer std.lib.collection/form? :added "3.0"}
(fact "checks if object is a lisp form")

^{:refer std.lib.collection/queue :added "3.0"
  :let [a (queue 1 2 3 4)]}
(fact "returns a `clojure.lang.PersistentQueue` object."

  (pop a) => [2 3 4])

^{:refer std.lib.collection/seqify :added "3.0"}
(fact "if not a sequence, then make one"

  (seqify 1)
  => [1]

  (seqify [1])
  => [1])

^{:refer std.lib.collection/unseqify :added "3.0"}
(fact "if a sequence, takes first element"

  (unseqify [1])
  => 1

  (unseqify 1)
  => 1)

^{:refer std.lib.collection/unlazy :added "3.0"}
(fact "works on both lazy seqs and objects")

^{:refer std.lib.collection/map-keys :added "3.0"}
(fact "changes the keys of a map"

  (map-keys inc {0 :a 1 :b 2 :c})
  => {1 :a, 2 :b, 3 :c})

^{:refer std.lib.collection/map-vals :added "3.0"}
(fact "changes the values of a map"

  (map-vals inc {:a 1 :b 2 :c 3})
  => {:a 2, :b 3, :c 4})

^{:refer std.lib.collection/map-juxt :added "3.0"}
(fact "creates a map from sequence with key and val functions"

  (map-juxt [str inc] [1 2 3 4 5])
  => {"1" 2, "2" 3, "3" 4, "4" 5, "5" 6})

^{:refer std.lib.collection/pmap-vals :added "3.0"}
(fact "uses pmap across the map values")

^{:refer std.lib.collection/map-entries :added "3.0"}
(fact "manipulates a map given the function"

  (map-entries (fn [[k v]]
                 [(keyword (str v)) (name k)])
               {:a 1 :b 2 :c 3})
  => {:1 "a", :2 "b", :3 "c"})

^{:refer std.lib.collection/pmap-entries :added "3.0"}
(fact "uses pmap across the entries")

^{:refer std.lib.collection/rename-keys :added "4.0"}
(fact "rename keys in map"

  (rename-keys {:a 1} {:a :b})
  => {:b 1})

^{:refer std.lib.collection/filter-keys :added "3.0"}
(fact "filters map based upon map keys"

  (filter-keys even? {0 :a 1 :b 2 :c})
  => {0 :a, 2 :c})

^{:refer std.lib.collection/filter-vals :added "3.0"}
(fact "filters map based upon map values"

  (filter-vals even? {:a 1 :b 2 :c 3})
  => {:b 2})

^{:refer std.lib.collection/keep-vals :added "3.0"}
(fact "filters map based upon map values"

  (keep-vals even? {:a 1 :b 2 :c 3})
  => {:b true})

^{:refer std.lib.collection/qualified-keys :added "3.0"}
(fact "takes only the namespaced keys of a map"

  (qualified-keys {:a 1 :ns/b 1})
  => #:ns{:b 1}

  (qualified-keys {:a 1 :ns.1/b 1 :ns.2/c 1}
                  :ns.1)
  => #:ns.1{:b 1})

^{:refer std.lib.collection/unqualified-keys :added "3.0"}
(fact "takes only the namespaced keys of a map"

  (unqualified-keys {:a 1 :ns/b 1})
  => {:a 1})

^{:refer std.lib.collection/qualify-keys :added "3.0"}
(fact "lifts all unqualified keys"

  (qualify-keys {:a 1} :ns.1)
  => #:ns.1{:a 1}

  (qualify-keys {:a 1 :ns.2/c 1} :ns.1)
  => {:ns.1/a 1, :ns.2/c 1})

^{:refer std.lib.collection/unqualify-keys :added "3.0"}
(fact "unqualifies keys in the map"

  (unqualify-keys {:a 1 :ns.1/b 1 :ns.2/c 1})
  => {:a 1, :b 1, :c 1}

  (unqualify-keys {:a 1 :ns.1/b 1 :ns.2/c 1} :ns.1)
  => {:a 1, :b 1, :ns.2/c 1})

^{:refer std.lib.collection/assoc-new :added "3.0"}
(fact "only assoc if the value in the original map is nil"

  (assoc-new {:a 1} :b 2)
  => {:a 1 :b 2}

  (assoc-new {:a 1} :a 2 :b 2)
  => {:a 1 :b 2})

^{:refer std.lib.collection/merge-nested :added "3.0"}
(fact "Merges nested values from left to right."

  (merge-nested {:a {:b {:c 3}}} {:a {:b 3}})
  => {:a {:b 3}}

  (merge-nested {:a {:b {:c 1 :d 2}}}
                {:a {:b {:c 3}}})
  => {:a {:b {:c 3 :d 2}}})

^{:refer std.lib.collection/merge-nested-new :added "3.0"}
(fact "Merges nested values from left to right, provided the merged value does not exist"

  (merge-nested-new {:a {:b 2}} {:a {:c 2}})
  => {:a {:b 2 :c 2}}

  (merge-nested-new {:b {:c :old}} {:b {:c :new}})
  => {:b {:c :old}})

^{:refer std.lib.collection/dissoc-nested :added "3.0"}
(fact "dissocs recursively into a map removing empty entries"

  (dissoc-nested {:a {:b {:c 1}}}
                 [:a :b :c])
  => {})

^{:refer std.lib.collection/flatten-nested :added "3.0"}
(fact "flattens all elements the collection"

  (flatten-nested [1 2 #{3 {4 5}}])
  => [1 2 3 4 5])

^{:refer std.lib.collection/tree-flatten :added "3.0"}
(fact "flattens the entire map tree"

  (->> (tree-flatten {"a" {"b" {"c" 3 "d" 4}
                           "e" {"f" 5 "g" 6}}
                      "h" {"i" {}}})
       (map-keys h/strn))
  => {"a/b/c" 3, "a/b/d" 4, "a/e/f" 5, "a/e/g" 6})

^{:refer std.lib.collection/tree-nestify :added "3.0"}
(fact "nests keys in the map"

  (tree-nestify {:a/b 2 :a/c 3})
  => {:a {:b 2 :c 3}}

  (tree-nestify {:a/b {:e/f 1} :a/c {:g/h 1}})
  => {:a {:b {:e/f 1}
          :c {:g/h 1}}})

^{:refer std.lib.collection/tree-nestify:all :added "3.0"}
(fact "nests keys in the map and all submaps"

  (tree-nestify:all {:a/b 2 :a/c 3})
  => {:a {:b 2 :c 3}}

  (tree-nestify:all {:a/b {:e/f 1} :a/c {:g/h 1}})
  => {:a {:b {:e {:f 1}}
          :c {:g {:h 1}}}})

^{:refer std.lib.collection/reshape :added "3.0"}
(fact "moves values around in a map according to a table"

  (reshape {:a 1 :b 2}
           {[:c :d] [:a]})
  => {:b 2, :c {:d 1}})

^{:refer std.lib.collection/find-templates :added "3.0"}
(fact "finds the template with associated path" ^:hidden

  (find-templates {:hash  "{{hash}}"
                   :salt  "{{salt}}"
                   :email "{{email}}"
                   :user {:firstname "{{firstname}}"
                          :lastname  "{{lastname}}"}})
  => {"{{hash}}" [:hash]
      "{{salt}}" [:salt]
      "{{email}}" [:email]
      "{{firstname}}" [:user :firstname]
      "{{lastname}}" [:user :lastname]})

^{:refer std.lib.collection/transform-fn :added "3.0"}
(fact "creates a transformation function"
  ((transform-fn {:keystore {:hash  "{{hash}}"
                             :salt  "{{salt}}"
                             :email "{{email}}"}

                  :db       {:login {:user {:hash "{{hash}}"
                                            :salt "{{salt}}"}
                                     :value "{{email}}"}}}
                 [:keystore :db]) ^:hidden
   {:type :email,
    :hash "1234"
    :salt "ABCD"
    :email "a@a.com"})
  => {:type :email
      :login {:user {:hash "1234",
                     :salt "ABCD"},
              :value "a@a.com"}})

^{:refer std.lib.collection/transform :added "3.0"}
(fact "creates a transformation function" ^:hidden

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
  => {:login {:user {:hash "1234",
                     :salt "ABCD"},
              :value "a@a.com"}})

^{:refer std.lib.collection/empty-record :added "3.0"}
(fact "creates an empty record from an existing one"

  (defrecord Database [host port])

  (empty-record (Database. "localhost" 8080))
  => (just {:host nil :port nil}))

^{:refer std.lib.collection/transpose :added "3.0"}
(fact "sets the vals and keys and vice-versa"

  (transpose {:a 1 :b 2 :c 3})
  => {1 :a, 2 :b, 3 :c})

^{:refer std.lib.collection/index-at :added "3.0"}
(fact "finds the index of the first matching element in an array"

  (index-at even? [1 2 3 4]) => 1

  (index-at keyword? [1 2 :hello 4]) => 2)

^{:refer std.lib.collection/element-at :added "3.0"}
(fact "finds the element within an array"

  (element-at keyword? [1 2 :hello 4])
  => :hello)

^{:refer std.lib.collection/insert-at :added "3.0"}
(fact "insert one or more elements at the given index"

  (insert-at [:a :b] 1 :b :c)
  => [:a :b :c :b])

^{:refer std.lib.collection/remove-at :added "3.0"}
(fact "removes element at the specified index"

  (remove-at [:a :b :c :d] 2)
  => [:a :b :d])

^{:refer std.lib.collection/split-by :added "4.0"}
(fact "splits a sequences using a predicate "
  ^:hidden
  
  (split-by #(= % :split) [:a :b :split :c :d :split :e])
   => [[:a :b] [:c :d] [:e]]

   (split-by even? [1 3 5 2 7 9 4 11])
   => [[1 3 5] [7 9] [11]]

   (split-by #(= % :split) [:split :a :b :split :c])
   => [[] [:a :b] [:c]]

   (split-by #(= % :split) [:a :b :c])
   => [[:a :b :c]])

^{:refer std.lib.collection/deduped? :added "3.0"}
(fact "checks if elements in the collection are unique"

  (deduped? [1 2 3 4])
  => true

  (deduped? [1 2 1 4])
  => false)

^{:refer std.lib.collection/unfold :added "3.0"}
(fact "unfolds using a generated function"

  (unfold (fn [[i :as seed]]
            (if i
              (if-not (neg? i)
                [(* i 2) [(dec i)]])))
          [10])
  => [20 18 16 14 12 10 8 6 4 2 0])

^{:refer std.lib.collection/diff:changes :added "3.0"}
(fact "Finds changes in nested maps, does not consider new elements"
  ^:hidden

  (diff:changes {:a 2} {:a 1})
  => {[:a] 2}

  (diff:changes {:a {:b 1 :c 2}} {:a {:b 1 :c 3}})
  => {[:a :c] 2}

  (diff:changes {:a 1 :b 2 :c 3} {:a 1 :b 2})
  => {}

  (diff:changes {:a 1 :b 2} {:a 1 :b 2 :c 3})
  => {}

  (diff:changes {:a 1} {:a nil})
  => {[:a] 1}

  (diff:changes {:a nil} {:a 1})
  => {[:a] nil}

  (diff:changes {:a true} {:a false})
  => {[:a] true}

  (diff:changes {:a false} {:a true})
  => {[:a] false})

^{:refer std.lib.collection/diff:new :added "3.0"}
(fact "Finds new elements in nested maps, does not consider changes"
  ^:hidden

  (diff:new {:a 2} {:a 1})
  => {}

  (diff:new {:a {:b 1}} {:a {:c 2}})
  => {[:a :b] 1}

  (diff:new {:a {:b 1 :c 2}} {:a {:b 1 :c 3}})
  => {}

  (diff:new {:a 1 :b 2 :c 3} {:a 1 :b 2})
  => {[:c] 3}

  (diff:new {:a 1 :b 2} {:a 1 :b 2 :c 3})
  => {})

^{:refer std.lib.collection/diff :added "3.0"}
(fact "Finds the difference between two maps"
  ^:hidden

  (diff {:a 2} {:a 1})
  => {:+ {} :- {} :> {[:a] 2}}

  (diff {:a {:b 1 :d 3}} {:a {:c 2 :d 4}} true)
  => {:+ {[:a :b] 1}
      :- {[:a :c] 2}
      :> {[:a :d] 3}
      :< {[:a :d] 4}})

^{:refer std.lib.collection/diff:changed :added "3.0"}
(fact "Outputs what has changed between the two maps"

  (diff:changed {:a {:b {:c 3 :d 4}}}
                {:a {:b {:c 3}}})
  => {:a {:b {:d 4}}})

^{:refer std.lib.collection/diff:patch :added "3.0"}
(fact "patch from old to new"
  ^:hidden

  (let [m1  {:a {:b 1 :d 3}}
        m2  {:a {:c 2 :d 4}}
        df  (diff m2 m1)]
    (diff:patch m1 df))
  => {:a {:c 2 :d 4}})

^{:refer std.lib.collection/diff:unpatch :added "3.0"}
(fact "unpatch from new to old"
  ^:hidden

  (let [m1  {:a {:b 1 :d 3}}
        m2  {:a {:c 2 :d 4}}
        df  (diff m2 m1 true)]
    (diff:unpatch m2 df))
  => {:a {:b 1 :d 3}})

(comment
  (./purge {:write true})
  (./import))


^{:refer std.lib.collection/merge-meta :added "4.0"}
(fact "merges metadata into an object")