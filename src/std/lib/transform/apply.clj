(ns std.lib.transform.apply
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]))

(defn wrap-hash-set
  "allows operations to be performed on sets
 
   ((transform/wrap-hash-set +) #{1 2 3} 10)
   => #{13 12 11}
 
   ((transform/wrap-hash-set +) 1 10)
   => 11"
  {:added "3.0"}
  ([f]
   (fn [val datasource]
     (cond (set? val)
           (set (map #(f % datasource) val))

           :else
           (f val datasource)))))

(defn process-transform
  "Converts one value to another either through a value or function
 
   (transform/process-transform {:name \"Bob\"}
                                {:name \"Chris\"}
                                [:account]
                                {}
                                {}
                                {})
   => {:name \"Bob\"}"
  {:added "3.0"}
  ([strans tdata nsv interim tsch datasource]
   (if-let [[k v] (first strans)]
     (cond (and (coll/hash-map? v)
                (-> tsch (get k) vector?)
                (-> tsch (get k) first :type (= :ref)))
           (recur (next strans) tdata nsv interim tsch datasource)

           :else
           (let [subdata (get tdata k)
                 ntdata  (cond (nil? subdata) tdata

                               (fn? v)
                               (assoc tdata k ((wrap-hash-set v) subdata datasource))

                               (coll/hash-map? subdata)
                               (assoc tdata k (process-transform
                                               v subdata (conj nsv k)
                                               interim (get tsch k) datasource))

                               :else
                               (assoc tdata k (if (set? subdata) #{v} v)))]
             (recur (next strans) ntdata nsv interim tsch datasource)))
     tdata)))

(defn wrap-model-pre-transform
  "Applies a function transformation in the :pre-transform step
 
   (graph/normalise {:account/name \"Chris\"}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:pre-transform {:account {:name \"Bob\"}}}}
                    {:normalise [transform/wrap-model-pre-transform]})
   => {:account {:name \"Bob\"}}"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [strans (:pre-transform interim)
           output (process-transform strans tdata nsv interim tsch datasource)]
       (f output tsch nsv (update-in interim [:ref-path]
                                     #(-> %
                                          (pop)
                                          (conj output)))
          fns datasource)))))

(defn wrap-model-post-transform
  "applies a function transformation in the :post-transform step
 
   (graph/normalise {:account/name \"Chris\"}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :name \"Bob\"
                     :pipeline {:post-transform {:account {:name (fn [_ env] (:name env))}}}}
                    {:normalise [transform/wrap-model-post-transform]})
   => {:account {:name \"Bob\"}}"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [strans (:post-transform interim)
           output (f tdata tsch nsv interim fns datasource)]
       (process-transform strans output nsv interim tsch datasource)))))
