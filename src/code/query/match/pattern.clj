(ns code.query.match.pattern
  (:require [code.query.match.impl :as match]
            [code.query.match.optional :as optional]
            [std.lib.collection :as collection]
            [std.protocol.match :as protocol.match]))

(defn transform-pattern
  "converts an input into an actual matchable pattern
 
   (transform-pattern ^:& #{:a :b})
   => (all match/actual-pattern?
           #(= (:expression %) #{:a :b}))
 
   (transform-pattern '^:% (symbol \"_\"))
   => (all match/eval-pattern?
           #(= (:expression %) '(symbol \"_\")))
 
   (transform-pattern #{:a :b})
   => #{:a :b}
 
   (transform-pattern [:a :b])
   => [:a :b]
 
   (transform-pattern [[:code {:a #'number?} #'string?]])
   => [[:code {:a #'clojure.core/number?}
        #'clojure.core/string?]]"
  {:added "3.0"}
  ([pattern]
   (cond (:& (meta pattern)) (match/actual-pattern pattern)
         (:% (meta pattern)) (match/eval-pattern pattern)
         (or (collection/lazy-seq? pattern)
             (list? pattern))      (apply list (map transform-pattern pattern))
         (vector? pattern)         (vec (map transform-pattern pattern))
         (set? pattern)            (set (map transform-pattern pattern))
         (collection/hash-map? pattern)  (->> pattern
                                     (map (fn [[k v]]
                                            [k (transform-pattern v)]))
                                     (into {}))
         :else pattern)))

(defn pattern-single-fn
  "creates a function based on template
 
   ((pattern-single-fn '(a)) '(a))
   => true
 
   ((pattern-single-fn '(a)) '(b))
   => false"
  {:added "3.0"}
  ([pattern]
   (let [template (transform-pattern pattern)]
     (fn [obj]
       (protocol.match/-match template obj)))))

(defn pattern-matches
  "pattern matches for a given template
   ((pattern-matches ()) ())
   => '(())
 
   ((pattern-matches []) ())
   => ()
 
   ((pattern-matches '(^:% symbol? ^:? (+ 1 _ ^:? _))) '(+ (+ 1 2 3)))
   => '((^{:% true} symbol? ^{:? 0} (+ 1 _ ^{:? 1} _)))"
  {:added "3.0"}
  ([template]
   (let [all-fns (->> template
                      (optional/pattern-seq)
                      (mapv (juxt identity pattern-single-fn)))]
     (fn [form]
       (or (mapcat (fn [[template f]]
                     (if (f form) [template])) all-fns)
           [])))))

(defn pattern-fn
  "make sure that functions are working properly
   ((pattern-fn vector?) [])
   => true
 
   ((pattern-fn #'vector?) [])
   => true
 
   ((pattern-fn '^:% vector?) [])
   => true
 
   ((pattern-fn '^:% symbol?) [])
   => false
 
   ((pattern-fn '[^:% vector?]) [[]])
   => true
 
   ((pattern-fn [[:code map? string?]]) [[:code {} \"hello\"]])
   => true
 
   ((pattern-fn [[:code {:a number?} string?]]) [[:code {:a 1} \"hello\"]])
   => true
 
   ((pattern-fn [[:code {:a {:b number?}} string?]]) [[:code {:a {:b 1}} \"hello\"]])
   => true"
  {:added "3.0"}
  ([template]
   (fn [value]
     (-> ((pattern-matches template) value)
         empty?
         not))))
