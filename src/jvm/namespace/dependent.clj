(ns jvm.namespace.dependent
  (:require [std.protocol.deps :as protocol.deps]
            [jvm.namespace.common :as common]
            [std.pipe.util :as ut]
            [std.lib :as h]))

(defn ns-select
  "selects a bunch of namespaces
 
   (ns-select [(.getName *ns*)])
   => '[jvm.namespace.dependent-test]"
  {:added "3.0"}
  ([input]
   (ns-select input (common/ns-list)))
  ([input nss]
   (ut/select-inputs {:item {:list (fn [_ _] nss)}}
                     {}
                     {}
                     input)))

(defn ns-has-deps
  "checks if current namespace depends on `test`
 
   (ns-has-deps 'jvm.namespace.dependent
                'std.lib)
   => true"
  {:added "3.0"}
  ([ns test]
   (let [deps (->>  (vals (ns-aliases ns))
                    (map #(.getName ^clojure.lang.Namespace %))
                    set)]
     (boolean (deps test)))))

(defn ns-dependents
  "finds dependent namespaces
   (ns-dependents 'std.lib
                  '#{jvm.namespace.dependent})
   => '#{jvm.namespace.dependent}"
  {:added "3.0"}
  ([ns input]
   (set (filter #(ns-has-deps % ns) (ns-select input)))))

(defn ns-level-dependents
  "arranges dependent namespaces in map
 
   (ns-level-dependents '#{std.lib}
                        '#{jvm.namespace.dependent})
   => '{std.lib #{jvm.namespace.dependent}}"
  {:added "3.0"}
  ([nss input]
   (reduce (fn [out ns]
             (assoc out ns (ns-dependents ns input)))
           {}
           nss)))

(defn ns-all-dependents
  "gets all dependencies
 
   (ns-all-dependents 'std.lib.sort '[hara])
   => map?"
  {:added "3.0"}
  ([ns input]
   (loop [acc {ns (ns-dependents ns input)}]
     (let [curr (set (keys acc))
           next (apply h/union (vals acc))
           diff (h/difference next curr)]
       (if (empty? diff)
         acc
         (recur (merge acc (ns-level-dependents diff input))))))))

(defn reeval
  "reevaluates all files dependent on current
 
   (->> (reeval '#{jvm.namespace.dependent})
        (map str))
   => [\"jvm.namespace.dependent-test\"]"
  {:added "3.0"}
  ([]
   (reeval (.getName *ns*) :all))
  ([input]
   (reeval (.getName *ns*) input))
  ([ns input]
   (->> (ns-all-dependents ns input)
        (h/topological-sort)
        (reverse)
        (mapv #(doto % (require :reload))))))

(comment
  (set (filter #(ns-has-deps % ns) (ns-select input)))

  (defn ns-has-deps
    "checks if current namespace depends on `test`
 
   (ns-has-deps 'jvm.namespace.dependent
                'clojure.set)
   => true"
    {:added "3.0"}
    ([ns test]
     (let [deps (->>  (vals (ns-aliases ns))
                      (map #(.getName ^clojure.lang.Namespace %))
                      set)]
       (boolean (deps test)))))

  (ns-select :all)
  (ns-dependents (.getName *ns*))
  (ns-all-dependents *ns* :all)

  (reeval 'std.lib.sort '[hara])

  (ns-all-dependents 'std.lib.sort '[hara])
  {std.lib.sort #{jvm.namespace.dependent}, jvm.namespace.dependent #{}}

  (h/difference #{1 2} #{2 3})

  (h/topological-sort)

  (ns-level-dependents (ns-dependents 'hara.data.base.map '[hara])
                       '[hara])

  (common/+namespaces+))
(defn sort-topo
  "topologically sorts a collection of namespaces
   
   (sort-topo '[jvm.namespace.dependent std.lib])
   => '[std.lib jvm.namespace.dependent]"
  {:added "3.0"}
  [nss]
  (let [nss (set (map symbol nss))
        graph (reduce (fn [acc ns]
                        (let [deps (->> (vals (ns-aliases ns))
                                        (map #(.getName ^clojure.lang.Namespace %))
                                        (filter nss)
                                        set)]
                          (assoc acc ns deps)))
                      {}
                      nss)]
    (h/topological-sort graph)))
