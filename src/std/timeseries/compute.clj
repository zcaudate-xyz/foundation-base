(ns std.timeseries.compute
  (:require [std.math :as math]
            [std.lib.sort :as sort]
            [std.lib :as h]
            [std.timeseries.common :as core]
            [std.string :as str]
            [std.lib.walk :as walk])
  (:refer-clojure :exclude [compile]))

(defn max-fn
  "max function accepting array"
  {:added "3.0"}
  ([arr]
   (if-not (empty? arr)
     (apply max arr))))

(defn min-fn
  "min function accepting array"
  {:added "3.0"}
  ([arr]
   (if-not (empty? arr)
     (apply min arr))))

(defn range-fn
  "range function accepting array"
  {:added "3.0"}
  ([arr]
   (if-not (empty? arr)
     (- (apply max arr) (apply min arr)))))

(defn middle-fn
  "middling function
 
   (middle-fn [1 2 3])
   => 2"
  {:added "3.0"}
  ([arr]
   (let [len (count arr)]
     (nth arr (long (/ len 2))))))

(defn wrap-not-nil
  "ensures no null values"
  {:added "3.0"}
  ([f]
   (fn [& args]
     (let [args (filter (complement nil?) args)]
       (apply f args)))))

(def +aggregations+
  (h/map-vals wrap-not-nil
              {:first    first
               :last     last
               :middle   middle-fn
               :mean     (comp float math/mean)
               :sum      #(apply + %)
               :max      max-fn
               :min      min-fn
               :range    range-fn
               :stddev   math/stdev
               :mode     math/mode
               :median   math/median
               :skew     math/skew
               :variance math/variance
               :random   rand-nth}))

(def +aggregation-forms+
  (h/map-entries (fn [[k _]]
                   [(keyword "s" (name k)) (list k `+aggregations+)])
                 +aggregations+))

(def +templates+
  {:s/norm   (fn [v] `(/ ~v [:s/max ~v]))
   :s/adj    (fn [v] `(- ~v [:s/min ~v]))
   :s/inv    (fn [v] `(- [:s/max ~v] ~v))
   :s/<+     (fn [v] `(- ~v [:s/last ~v]))
   :s/<-     (fn [v] `(- [:s/last ~v] ~v))
   :s/->     (fn [v] `(- [:s/first ~v] ~v))
   :s/+>     (fn [v] `(- ~v [:s/first ~v]))})

(defn template?
  "checks if vector is a template
 
   (template? [:s/norm 1])
   => true"
  {:added "3.0"}
  ([[k & rest]]
   (contains? +templates+ k)))

(defn apply-template
  "applies the template
 
   (= (apply-template [:s/norm :bench.stats.lag])
      (list `/ :bench.stats.lag [:s/max :bench.stats.lag]))
   => true"
  {:added "3.0"}
  ([[k & rest]]
   (apply (get +templates+ k) rest)))

(defn key-references
  "finds all references for the expr map
 
   (key-references {:t            '(* :start 100000000)
                    :t0           [:s/adj :t]
                    :t1           [:s/inv :t0]
                    :output.norm  [:s/norm :output.value]})
   => {:t #{}, :t0 #{:t}, :t1 #{:t0}, :output.norm #{}}"
  {:added "3.0"}
  ([m]
   (let [ks (set (keys m))]
     (h/map-vals (fn [form]
                   (let [store (volatile! #{})]
                     (walk/postwalk (fn [x]
                                      (if (and (keyword? x)
                                               (ks x))
                                        (vswap! store conj x))
                                      x)
                                    form)
                     @store))
                 m))))

(defn key-order
  "(key-order {:output.norm  [:s/norm :output.value]
               :t1           [:s/inv :t0]
               :t0           [:s/adj :t]
               :t            '(* :start 100000000)})
   => [:t :t0 :t1 :output.norm]"
  {:added "3.0"}
  ([m]
   (-> m
       (key-references)
       (sort/topological-sort))))

(declare compile-form)

(defn compile-keyword
  "compiles a single keyword
 
   (compile-keyword :bench.stats.lag)
   => `(get ~'output (keyword \"bench.stats.lag\"))"
  {:added "3.0"}
  ([k]
   `(get ~'output (keyword ~(name k)))))

(defn compile-aggregates
  "compiles the aggregates
 
   (compile-aggregates '{v1 [:s/max :bench.stats.lag]})"
  {:added "3.0"}
  ([aggregates]
   (mapcat (fn [[sym [k form]]]
             [sym `(~(get +aggregation-forms+ k)
                    (filter identity ~(compile-form form)))])
           aggregates)))

(defn compile-form
  "compiles the entire form
 
   (compile-form '(- [:s/norm :bench.stats.time] [:s/norm :bench.stats.lag]))"
  {:added "3.0"}
  ([form]
   (let [aggregates  (volatile! {})
         single-form (doall (walk/prewalk (fn [x]
                                            (cond (and (keyword? x)
                                                       (nil? (namespace x)))
                                                  (compile-keyword x)

                                                  (and (vector? x)
                                                       (keyword? (first x))
                                                       (= "s" (namespace (first x))))
                                                  (do (cond (template? x)
                                                            (apply-template x)

                                                            :else
                                                            (let [sym (gensym)]
                                                              (vswap! aggregates assoc sym x)
                                                              sym)))

                                                  :else x))
                                          form))
         map-form   `(map (fn [~'output] ~single-form) ~'outputs)
         aggregate-forms (compile-aggregates @aggregates)]
     (if (empty? aggregate-forms)
       map-form
       `(let [~@aggregate-forms] ~map-form)))))

(defn compile-single
  "complise a single fn form
 
   (eval (compile-single '(- [:s/norm :bench.stats.time] [:s/norm :bench.stats.lag])))
   => fn?"
  {:added "3.0"}
  ([compute]
   `(fn [~'outputs] ~(compile-form compute))))

(defn compile
  "complies a map of expressions
 
   (compile '{:diff (- [:s/norm :bench.stats.time]
                       [:s/norm :bench.stats.lag])})"
  {:added "3.0"}
  ([m]
   (h/map-vals (comp eval compile-single) m)))

(defn compute
  "computes additional values given array
 
   (-> (compute [{:start 10 :output.value 1}
                 {:start 11 :output.value 4}
                 {:start 12 :output.value 1}
                 {:start 13 :output.value 6}
                 {:start 14 :output.value 10.1}]
                {:t            '(* :start 100000000)
                 :t0           [:s/adj :t]
                 :t1           [:s/inv :t0]
                :output.norm  [:s/norm :output.value]})
       first)
   => {:start 10, :output.value 1, :t 1000000000, :t0 0, :t1 400000000, :output.norm 0.09900990099009901}"
  {:added "3.0"}
  ([arr exprs]
   (let [compute-fns   (compile exprs)
         compute-keys  (key-order exprs)]
     (reduce (fn [arr k]
               (let [f  (get compute-fns k)
                     result (f arr)]
                 (->> (map (fn [e] [k e]) result)
                      (map conj arr))))
             arr
             compute-keys))))

