(ns std.lib.transform.base.complex
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.lib.transform.base.combine :as combine]))

(defn assocs
  "Similar to `assoc` but conditions of association is specified
   through `sel` (default: `identity`) and well as merging specified
   through `func` (default: `combine`).
   (assocs {:a #{1}} :a #{2 3 4})
   => {:a #{1 2 3 4}}
 
   (assocs {:a {:id 1}} :a {:id 1 :val 1} :id merge)
   => {:a {:val 1, :id 1}}
 
   (assocs {:a #{{:id 1 :val 2}
                 {:id 1 :val 3}}} :a {:id 1 :val 4} :id merges)
   => {:a #{{:id 1 :val #{2 3 4}}}}"
  {:added "3.0"}
  ([m k v] (assocs m k v identity combine/combine))
  ([m k v sel func]
   (let [z (get m k)]
     (cond (nil? z) (assoc m k v)
           :else
           (assoc m k (combine/combine z v sel func))))))

(defn dissocs
  "Similar to `dissoc` but allows dissassociation of sets of values from a map.
 
   (dissocs {:a 1} :a)
   => {}
 
   (dissocs {:a #{1 2}} [:a #{0 1}])
   => {:a #{2}}
 
   (dissocs {:a #{1 2}} [:a #{1 2}])
   => {}"
  {:added "3.0"}
  ([m k]
   (cond (vector? k)
         (let [[k v] k
               z (get m k)
               res (combine/decombine z v)]
           (if (nil? res)
             (dissoc m k)
             (assoc m k res)))
         :else
         (dissoc m k))))

(defn gets
  "Returns the associated values either specified by a key or a key and predicate pair.
 
   (gets {:a 1} :a) => 1
 
   (gets {:a #{0 1}} [:a zero?]) => #{0}
 
   (gets {:a #{{:b 1} {}}} [:a :b]) => #{{:b 1}}"
  {:added "3.0"}
  ([m k]
   (if-not (vector? k)
     (get m k)
     (let [[k prchk] k
           val (get m k)]
       (if-not (set? val) val
               (->> (filter prchk val)
                    set))))))

(defn merges
  "Like `merge` but works across sets and will also
    combine duplicate key/value pairs together into sets of values.
 
   (merges {:a 1} {:a 2})
   => {:a #{1 2}}
 
   (merges {:a #{{:id 1 :val 1}}}
           {:a {:id 1 :val 2}}
           :id merges)
   => {:a #{{:id 1 :val #{1 2}}}}"
  {:added "3.0"}
  ([m1 m2] (merges m1 m2 identity combine/combine))
  ([m1 m2 sel] (merges m1 m2 sel combine/combine))
  ([m1 m2 sel func]
   (reduce-kv (fn [out k v]
                (assoc out k (combine/combine (get out k) v sel func)))
              m1
              m2)))

(defn merges-nested
  "Like `merges` but works on nested maps
 
   (merges-nested {:a {:b 1}} {:a {:b 2}})
   => {:a {:b #{1 2}}}
 
   (merges-nested {:a #{{:foo #{{:bar #{{:baz 1}}}}}}}
                  {:a #{{:foo #{{:bar #{{:baz 2}}}}}}}
                  map?
                  merges-nested)
   => {:a #{{:foo #{{:bar #{{:baz 2}}}
                    {:bar #{{:baz 1}}}}}}}"
  {:added "3.0"}
  ([] nil)
  ([m] m)
  ([m1 m2] (merges-nested m1 m2 identity combine/combine))
  ([m1 m2 sel] (merges-nested m1 m2 sel combine/combine))
  ([m1 m2 sel func]
   (reduce-kv (fn [out k v]
                (let [v1 (get out k)]
                  (cond (not (and (coll/hash-map? v1) (coll/hash-map? v)))
                        (assoc out k (combine/combine v1 v sel func))

                        :else
                        (assoc out k (merges-nested v1 v sel func)))))
              m1
              m2)))

(defn merges-nested*
  "Like `merges-nested but can recursively merge nested sets and values
 
   (merges-nested* {:a #{{:id 1 :foo
                          #{{:id 2 :bar
                             #{{:id 3 :baz 1}}}}}}}
                   {:a {:id 1 :foo
                        {:id 2 :bar
                         {:id 3 :baz 2}}}}
                   :id)
 
   => {:a #{{:id 1 :foo
            #{{:id 2 :bar
                #{{:id 3 :baz #{1 2}}}}}}}}"
  {:added "3.0"}
  ([] nil)
  ([m] m)
  ([m1 m2] (merges-nested* m1 m2 coll/hash-map? combine/combine))
  ([m1 m2 sel] (merges-nested* m1 m2 sel combine/combine))
  ([m1 m2 sel func]
   (reduce-kv (fn [out k v]
                (let [v1 (get out k)]
                  (cond (and (coll/hash-map? v1) (coll/hash-map? v))
                        (assoc out k (merges-nested* v1 v sel func))

                        (or (set? v1) (set? v))
                        (assoc out k (combine/combine v1 v sel #(merges-nested* %1 %2 sel func)))

                        :else
                        (assoc out k (func v1 v)))))
              m1
              m2)))

(declare gets-in)

(defn- gets-in-loop
  [m [k & ks :as all-ks]]
  (cond (nil? ks)
        (let [val (gets m k)]
          (cond (set? val) val
                :else (list val)))
        :else
        (let [val (gets m k)]
          (cond (set? val)
                (apply concat (map #(gets-in-loop % ks) val))
                :else (gets-in-loop val ks)))))

(defn gets-in
  "Similar in style to `get-in` with operations on sets. returns a set of values.
 
   (gets-in {:a 1} [:a]) => #{1}
 
   (gets-in {:a 1} [:b]) => #{}
 
   (gets-in {:a #{{:b 1} {:b 2}}} [:a :b]) => #{1 2}"
  {:added "3.0"}
  ([m ks]
   (-> (gets-in-loop m ks) set (disj nil))))
