(ns std.lib.collection
  (:require [std.lib.foundation :as h]
            [std.string.common :as str]))

(defn hash-map?
  "Returns `true` if `x` implements `clojure.lang.APersistentMap`.
 
   (hash-map? {})    => true
   (hash-map? [])    => false"
  {:added "3.0"}
  ([x] (instance? clojure.lang.APersistentMap x)))

(defn lazy-seq?
  "Returns `true` if `x` implements `clojure.lang.LazySeq`.
 
   (lazy-seq? (map inc [1 2 3]))  => true
   (lazy-seq? ())    => false"
  {:added "3.0"}
  ([x] (instance? clojure.lang.LazySeq x)))

(defn cons?
  "checks if object is instance of `clojure.lang.Cons`
 
   (cons? (cons 1 [1 2 3])) => true"
  {:added "3.0"}
  ([x] (or (instance? clojure.lang.Cons x)
           (instance? clojure.lang.ChunkedCons x))))

(defn form?
  "checks if object is a lisp form"
  {:added "3.0"}
  ([form]
   (or (cons? form)
       (list? form)
       (lazy-seq? form))))

(defn queue
  "returns a `clojure.lang.PersistentQueue` object.
 
   (pop a) => [2 3 4]"
  {:added "3.0"}
  ([] (clojure.lang.PersistentQueue/EMPTY))
  ([x] (conj (queue) x))
  ([x & xs] (apply conj (queue) x xs)))

(defmethod print-method clojure.lang.PersistentQueue
  ([v ^java.io.Writer w]
   (.write w (str (into [] v)))))

(defn seqify
  "if not a sequence, then make one
 
   (seqify 1)
   => [1]
 
   (seqify [1])
   => [1]"
  {:added "3.0"}
  ([x]
   (if (or (coll? x)
           (instance? java.util.List x))
     x
     [x])))

(defn unseqify
  "if a sequence, takes first element
 
   (unseqify [1])
   => 1
 
   (unseqify 1)
   => 1"
  {:added "3.0"}
  ([x]
   (if (coll? x)
     (first x)
     x)))

(defn unlazy
  "works on both lazy seqs and objects"
  {:added "3.0"}
  ([x]
   (if (lazy-seq? x)
     (doall x)
     x)))

(defn map-keys
  "changes the keys of a map
 
   (map-keys inc {0 :a 1 :b 2 :c})
   => {1 :a, 2 :b, 3 :c}"
  {:added "3.0"}
  ([f m]
   (reduce (fn [out [k v]]
             (assoc out (f k) v))
           {}
           m)))

(defn map-vals
  "changes the values of a map
 
   (map-vals inc {:a 1 :b 2 :c 3})
   => {:a 2, :b 3, :c 4}"
  {:added "3.0"}
  ([f m]
   (reduce (fn [out [k v]]
             (assoc out k (f v)))
           {}
           m)))

(defn map-juxt
  "creates a map from sequence with key and val functions
 
   (map-juxt [str inc] [1 2 3 4 5])
   => {\"1\" 2, \"2\" 3, \"3\" 4, \"4\" 5, \"5\" 6}"
  {:added "3.0"}
  ([[kf vf] arr]
   (->> (map (juxt kf vf) arr)
        (into {}))))

(defn pmap-vals
  "uses pmap across the map values"
  {:added "3.0"}
  ([f m]
   (->> (pmap (fn [[k v]]
                [k (f v)])
              m)
        (into {}))))

(defn map-entries
  "manipulates a map given the function
 
   (map-entries (fn [[k v]]
                  [(keyword (str v)) (name k)])
                {:a 1 :b 2 :c 3})
   => {:1 \"a\", :2 \"b\", :3 \"c\"}"
  {:added "3.0"}
  ([f m]
   (->> (map f m)
        (into {}))))

(defn pmap-entries
  "uses pmap across the entries"
  {:added "3.0"}
  ([f m]
   (->> (pmap f m)
        (into {}))))

(defn rename-keys
  "rename keys in map
 
   (rename-keys {:a 1} {:a :b})
   => {:b 1}"
  {:added "4.0"}
  ([m trs]
   (reduce (fn [m [from to]]
             (if (contains? m from)
               (assoc (dissoc m from)
                      to (get m from))
               m))
           m
           trs)))

(defn filter-keys
  "filters map based upon map keys
 
   (filter-keys even? {0 :a 1 :b 2 :c})
   => {0 :a, 2 :c}"
  {:added "3.0"}
  ([pred m]
   (reduce (fn [out [k v]]
             (if (pred k)
               (assoc out k v)
               out))
           {}
           m)))

(defn filter-vals
  "filters map based upon map values
 
   (filter-vals even? {:a 1 :b 2 :c 3})
   => {:b 2}"
  {:added "3.0"}
  ([pred m]
   (reduce (fn [out [k v]]
             (if (pred v)
               (assoc out k v)
               out))
           {}
           m)))

(defn keep-vals
  "filters map based upon map values
 
   (keep-vals even? {:a 1 :b 2 :c 3})
   => {:b true}"
  {:added "3.0"}
  ([f m]
   (reduce (fn [out [k v]]
             (if-let [new (f v)]
               (assoc out k new)
               out))
           {}
           m))
  ([f pred m]
   (reduce (fn [out [k v]]
             (let [new (f v)]
               (if (pred new)
                 (assoc out k new)
                 out)))
           {}
           m)))

(defn qualified-keys
  "takes only the namespaced keys of a map
 
   (qualified-keys {:a 1 :ns/b 1})
   => #:ns{:b 1}
 
   (qualified-keys {:a 1 :ns.1/b 1 :ns.2/c 1}
                   :ns.1)
   => #:ns.1{:b 1}"
  {:added "3.0"}
  ([m]
   (filter-keys (fn [k] (and (keyword? k)
                             (namespace k)))
                m))
  ([m ns] 
   (let [pred (set (map h/strn (seqify ns)))]
     (filter-keys (fn [k] (and (keyword? k)
                               (pred (namespace k))))
                  m))))

(defn unqualified-keys
  "takes only the namespaced keys of a map
 
   (unqualified-keys {:a 1 :ns/b 1})
   => {:a 1}"
  {:added "3.0"}
  ([m]
   (filter-keys (fn [k] (and (keyword? k)
                             (nil? (namespace k))))
                m)))

(defn qualify-keys
  "lifts all unqualified keys
 
   (qualify-keys {:a 1} :ns.1)
   => #:ns.1{:a 1}
 
   (qualify-keys {:a 1 :ns.2/c 1} :ns.1)
   => {:ns.1/a 1, :ns.2/c 1}"
  {:added "3.0"}
  ([m ns]
   (let [ns (h/strn ns)]
     (map-keys (fn [k]
                 (if (and (keyword? k)
                          (not (namespace k)))
                   (keyword ns (name k))
                   k))
               m))))

(defn unqualify-keys
  "unqualifies keys in the map
 
   (unqualify-keys {:a 1 :ns.1/b 1 :ns.2/c 1})
   => {:a 1, :b 1, :c 1}
 
   (unqualify-keys {:a 1 :ns.1/b 1 :ns.2/c 1} :ns.1)
   => {:a 1, :b 1, :ns.2/c 1}"
  {:added "3.0"}
  ([m]
   (map-keys (fn [k] (keyword (name k))) m))
  ([m ns]
   (let [ns (h/strn ns)]
     (map-keys (fn [k]
                 (if (and (keyword? k)
                          (= (namespace k) ns))
                   (keyword (name k))
                   k))
               m))))

(defn assoc-new
  "only assoc if the value in the original map is nil
 
   (assoc-new {:a 1} :b 2)
   => {:a 1 :b 2}
 
   (assoc-new {:a 1} :a 2 :b 2)
   => {:a 1 :b 2}"
  {:added "3.0"}
  ([m k v]
   (if (contains? m k) m (assoc m k v)))
  ([m k v & more]
   (apply assoc-new (assoc-new m k v) more)))

(defn merge-nested
  "Merges nested values from left to right.
 
   (merge-nested {:a {:b {:c 3}}} {:a {:b 3}})
   => {:a {:b 3}}
 
   (merge-nested {:a {:b {:c 1 :d 2}}}
                 {:a {:b {:c 3}}})
   => {:a {:b {:c 3 :d 2}}}"
  {:added "3.0"}
  ([& maps]
   (apply merge-with (fn [& args]
                       (if (every? #(or (map? %) (nil? %)) args)
                         (apply merge-nested args)
                         (last args)))
          maps)))

(defn merge-nested-new
  "Merges nested values from left to right, provided the merged value does not exist
 
   (merge-nested-new {:a {:b 2}} {:a {:c 2}})
   => {:a {:b 2 :c 2}}
 
   (merge-nested-new {:b {:c :old}} {:b {:c :new}})
   => {:b {:c :old}}"
  {:added "3.0"}
  ([& maps]
   (apply merge-nested (reverse maps))))

(defn dissoc-nested
  "dissocs recursively into a map removing empty entries
 
   (dissoc-nested {:a {:b {:c 1}}}
                  [:a :b :c])
   => {}"
  {:added "3.0"}
  ([m [k & ks]]
   (if-not ks
     (dissoc m k)
     (let [nm (dissoc-nested ((or m {}) k) ks)]
       (cond (empty? nm) (dissoc m k)
             :else (assoc m k nm))))))

(defn flatten-nested
  "flattens all elements the collection
 
   (flatten-nested [1 2 #{3 {4 5}}])
   => [1 2 3 4 5]"
  {:added "3.0"}
  ([x]
   (filter (complement coll?)
           (rest (tree-seq coll? seq x)))))

(def re-create
  (memoize (fn [^String s]
             (if (h/regexp? s)
               s
               (-> (str s)
                   (.replaceAll "\\." "\\\\\\.")
                   (.replaceAll "\\*" "\\\\\\*")
                   (re-pattern))))))

(defn tree-flatten
  "flattens the entire map tree
 
   (->> (tree-flatten {\"a\" {\"b\" {\"c\" 3 \"d\" 4}
                            \"e\" {\"f\" 5 \"g\" 6}}
                       \"h\" {\"i\" {}}})
        (map-keys h/strn))
   => {\"a/b/c\" 3, \"a/b/d\" 4, \"a/e/f\" 5, \"a/e/g\" 6}"
  {:added "3.0"}
  ([m]
   (tree-flatten m h/*sep*))
  ([m sep]
   (tree-flatten m sep []))
  ([m sep path]
   (->> (keep (fn [[k v]]
                (if (and (map? v) (not-empty v))
                  (tree-flatten v sep (conj path k))
                  (if (or (not (coll? v))
                          (not-empty v))
                    [(->> (conj path k)
                          (map h/strn)
                          (str/join sep)
                          keyword) v])))
              m)
        (into {}))))

(defn tree-nestify
  "nests keys in the map
 
   (tree-nestify {:a/b 2 :a/c 3})
   => {:a {:b 2 :c 3}}
 
   (tree-nestify {:a/b {:e/f 1} :a/c {:g/h 1}})
   => {:a {:b {:e/f 1}
           :c {:g/h 1}}}"
  {:added "3.0"}
  ([m]
   (tree-nestify m h/*sep*))
  ([m sep]
   (tree-nestify m sep identity))
  ([m sep f]
   (let [pattern (re-create sep)]
     (reduce-kv (fn [m k v]
                  (let [path (->> (str/split (h/strn k) pattern)
                                  (map keyword))
                        v  (f v)]
                    (update-in m path
                               (fnil (fn [p]
                                       (cond (and (map? p) (map? v))
                                             (merge p v)
                                             :else v))
                                     v))))
                {}
                m))))

(defn tree-nestify:all
  "nests keys in the map and all submaps
 
   (tree-nestify:all {:a/b 2 :a/c 3})
   => {:a {:b 2 :c 3}}
 
   (tree-nestify:all {:a/b {:e/f 1} :a/c {:g/h 1}})
   => {:a {:b {:e {:f 1}}
           :c {:g {:h 1}}}}"
  {:added "3.0"}
  ([m]
   (tree-nestify:all m h/*sep*))
  ([m sep]
   (tree-nestify m sep
                 (fn [v] (if (hash-map? v)
                           (tree-nestify:all v sep)
                           v)))))

(defn reshape
  "moves values around in a map according to a table
 
   (reshape {:a 1 :b 2}
            {[:c :d] [:a]})
   => {:b 2, :c {:d 1}}"
  {:added "3.0"}
  ([m rels]
   (reduce (fn [out [to from]]
             (if (= to from)
               out
               (let [v (get-in m from)]
                 (-> out
                     (dissoc-nested from)
                     (assoc-in to v)))))
           m
           rels)))

(defn find-templates
  "finds the template with associated path"
  {:added "3.0"}
  ([m]
   (find-templates m [] {}))
  ([m path saved]
   (let [template? (fn [^String s]
                     (and (string? s)
                          (.startsWith s "{{")
                          (.endsWith s "}}")))]
     (reduce-kv (fn [out k v]
                  (cond (template? v)
                        (assoc out v (conj path k))

                        (map? v)
                        (find-templates v (conj path k) out)

                        :else
                        out))
                saved
                m))))

(defn transform-fn
  "creates a transformation function
   ((transform-fn {:keystore {:hash  \"{{hash}}\"
                              :salt  \"{{salt}}\"
                              :email \"{{email}}\"}
 
                   :db       {:login {:user {:hash \"{{hash}}\"
                                             :salt \"{{salt}}\"}
                                      :value \"{{email}}\"}}}
                  [:keystore :db]) ^:hidden
   {:type :email,
     :hash \"1234\"
     :salt \"ABCD\"
     :email \"a@a.com\"})
   => {:type :email
       :login {:user {:hash \"1234\",
                      :salt \"ABCD\"},
               :value \"a@a.com\"}}"
  {:added "3.0"}
  ([schema [from to]]
   (let [from-template (find-templates (get schema from))
         to-template   (find-templates (get schema to))
         move-template (map-keys to-template from-template)]
     (fn [data]
       (reshape data move-template)))))

(def transform-fn* (memoize transform-fn))

(defn transform
  "creates a transformation function"
  {:added "3.0"}
  ([schema [from to] data]
   (let [f (transform-fn* schema [from to])]
     (f data))))

(defn empty-record
  "creates an empty record from an existing one
 
   (defrecord Database [host port])
 
   (empty-record (Database. \"localhost\" 8080))
   => (just {:host nil :port nil})"
  {:added "3.0"}
  ([v]
   (.invoke ^java.lang.reflect.Method
    (.getMethod ^Class (type v) "create"
                (into-array Class [clojure.lang.IPersistentMap]))
            nil
            (object-array [{}]))))

(defn transpose
  "sets the vals and keys and vice-versa
 
   (transpose {:a 1 :b 2 :c 3})
   => {1 :a, 2 :b, 3 :c}"
  {:added "3.0"}
  ([m]
   (reduce (fn [out [k v]]
             (assoc out v k))
           {}
           m)))

(defn index-at
  "finds the index of the first matching element in an array
 
   (index-at even? [1 2 3 4]) => 1
 
   (index-at keyword? [1 2 :hello 4]) => 2"
  {:added "3.0"}
  ([pred coll]
   (loop [[x & more :as coll] coll
          i 0]
     (cond (empty? coll) -1

           (pred x) i

           :else
           (recur more (inc i))))))

(defn element-at
  "finds the element within an array
 
   (element-at keyword? [1 2 :hello 4])
   => :hello"
  {:added "3.0"}
  ([pred coll]
   (loop [[x & more :as coll] coll]
     (cond (empty? coll) nil

           (pred x) x

           :else
           (recur more)))))

(defn insert-at
  "insert one or more elements at the given index
 
   (insert-at [:a :b] 1 :b :c)
   => [:a :b :c :b]"
  {:added "3.0"}
  ([coll i new]
   (apply conj (subvec coll 0 i) new (subvec coll i)))
  ([coll i new & more]
   (apply conj (subvec coll 0 i) new (concat more (subvec coll i)))))

(defn remove-at
  "removes element at the specified index
 
   (remove-at [:a :b :c :d] 2)
   => [:a :b :d]"
  {:added "3.0"}
  ([coll i]
   (remove-at coll i 1))
  ([coll i n]
   (cond (vector? coll)
         (reduce conj
                 (subvec coll 0 i)
                 (subvec coll (+ i n) (count coll)))

         :else
         (keep-indexed #(if (not (and (>= %1 i)
                                      (< %1 (+ i n)))) %2) coll))))

(defn deduped?
  "checks if elements in the collection are unique
 
   (deduped? [1 2 3 4])
   => true
 
   (deduped? [1 2 1 4])
   => false"
  {:added "3.0"}
  [coll]
  (= (count (set coll))
     (count coll)))

(defn unfold
  "unfolds using a generated function
 
   (unfold (fn [[i :as seed]]
             (if i
               (if-not (neg? i)
                 [(* i 2) [(dec i)]])))
           [10])
   => [20 18 16 14 12 10 8 6 4 2 0]"
  {:added "3.0"}
  ([f coll]
   (->> coll
        (list nil)
        (iterate (comp f second))
        rest
        (take-while some?)
        (map first))))

(defn diff:changes
  "Finds changes in nested maps, does not consider new elements"
  {:added "3.0"}
  ([m1 m2]
   (diff:changes m1 m2 [] =))
  ([m1 m2 arr equal-fn]
   (reduce-kv (fn [out k1 v1]
                (if (contains? m2 k1)
                  (let [v2 (get m2 k1)]
                    (cond (and (hash-map? v1) (hash-map? v2))
                          (merge out (diff:changes v1 v2 (conj arr k1) equal-fn))

                          (equal-fn v1 v2)
                          out

                          :else
                          (assoc out (conj arr k1) v1)))
                  out))
              {}
              m1)))

(defn diff:new
  "Finds new elements in nested maps, does not consider changes"
  {:added "3.0"}
  ([m1 m2]
   (diff:new m1 m2 []))
  ([m1 m2 arr]
   (reduce-kv (fn [out k1 v1]
                (let [v2 (get m2 k1)]
                  (cond (and (hash-map? v1) (hash-map? v2))
                        (merge out (diff:new v1 v2 (conj arr k1)))

                        (not (contains? m2 k1))
                        (assoc out (conj arr k1) v1)

                        :else out)))
              {}
              m1)))

(defn diff
  "Finds the difference between two maps"
  {:added "3.0"}
  ([m1 m2] (diff m1 m2 false))
  ([m1 m2 reversible]
   (let [diff (hash-map :+ (diff:new m1 m2)
                        :- (diff:new m2 m1)
                        :> (diff:changes m1 m2))]
     (if reversible
       (assoc diff :< (diff:changes m2 m1))
       diff))))

(defn- merge-or-replace
  "If both are maps then merge, otherwis replace
 
   (merge-or-replace {:a {:b {:c 2}}} {:a {:b {:c 3 :d 4}}})
   => {:a {:b {:c 3 :d 4}}}"
  {:added "3.0"}
  ([x v]
   (cond (and (hash-map? x)
              (hash-map? v))
         (merge-nested x v)

         :else v)))

(defn diff:changed
  "Outputs what has changed between the two maps
 
   (diff:changed {:a {:b {:c 3 :d 4}}}
                 {:a {:b {:c 3}}})
   => {:a {:b {:d 4}}}"
  {:added "3.0"}
  ([new old]
   (->> (diff new old)
        ((juxt :> :+))
        (apply merge)
        (reduce-kv (fn [out ks v]
                     (assoc-in out ks v))
                   {}))))

(defn diff:patch
  "patch from old to new"
  {:added "3.0"}
  ([m diff]
   (->> m
        (#(reduce-kv (fn [m arr v]
                       (update-in m arr merge-or-replace v))
                     %
                     (merge (:+ diff) (:> diff))))
        (#(reduce (fn [m arr]
                    (dissoc-nested m arr))
                  %
                  (keys (:- diff)))))))

(defn diff:unpatch
  "unpatch from new to old"
  {:added "3.0"}
  ([m diff]
   (->> m
        (#(reduce-kv (fn [m arr v]
                       (update-in m arr merge-or-replace v))
                     %
                     (merge (:- diff) (:< diff))))
        (#(reduce (fn [m arr]
                    (dissoc-nested m arr))
                  %
                  (keys (:+ diff)))))))
