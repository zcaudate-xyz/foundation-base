(ns std.lib.transform.fill-empty
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]))

(defn process-fill-empty
  "helper functio for wrap-model-fill-empty
 
   (process-fill-empty {:age 10}
                       {:name \"Chris\"}
                       [:account]
                       {}
                       (-> (schema/schema examples/account-name-age-sex)
                           :tree
                           :person)
                       {})
  => {:name \"Chris\", :age 10}"
  {:added "3.0"}
  ([sfill tdata nsv interim tsch datasource]
   (if-let [[k v] (first sfill)]
     (cond (not (get tdata k))
           (cond (fn? v)
                 (recur (next sfill)
                        (assoc tdata k (v (:ref-path interim) datasource))
                        nsv interim tsch datasource)

                 (coll/hash-map? v)
                 (recur (next sfill)
                        (assoc tdata k (process-fill-empty v
                                                           (get tdata k)
                                                           (conj nsv k)
                                                           interim
                                                           (get tsch k)
                                                           datasource))
                        nsv interim tsch datasource)

                 :else
                 (recur (next sfill)
                        (assoc tdata k v) nsv interim tsch datasource))

           (and (coll/hash-map? v)
                (-> tsch (get k) vector?)
                (-> tsch (get k) first :type (= :ref)))
           (recur (next sfill) tdata nsv interim tsch datasource)

           :else
           (let [subdata (get tdata k)]
             (cond (coll/hash-map? subdata)
                   (recur (next sfill)
                          (assoc tdata k (process-fill-empty v
                                                             (get tdata k)
                                                             (conj nsv k)
                                                             interim
                                                             (get tsch k)
                                                             datasource))
                          nsv interim tsch datasource)
                   :else
                   (recur (next sfill) tdata nsv interim tsch datasource))))
     tdata)))

(defn wrap-model-fill-empty
  "fill empty entry with either value or function
 
   (graph/normalise {:account/name \"Chris\"}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:fill-empty {:account {:age 10}}}}
                    {:normalise [wrap-model-fill-empty]})
   => {:account {:name \"Chris\", :age 10}}
 
   (graph/normalise {:account/name \"Chris\"}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:fill-empty {:account {:age (fn [_ datasource]
                                                              (:age datasource))}}}
                     :age 10}
                    {:normalise [wrap-model-fill-empty]})
   => {:account {:name \"Chris\", :age 10}}
 
   (graph/normalise {:account/name \"Chris\" :account/age 9}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:fill-empty {:account {:age 10}}}}
                    {:normalise [wrap-model-fill-empty]})
   => {:account {:name \"Chris\" :age 9}}"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [sfill (:fill-empty interim)
           output (process-fill-empty sfill tdata nsv interim tsch datasource)]
       (f output tsch nsv (update-in interim [:ref-path]
                                     #(-> %
                                          (pop)
                                          (conj output)))
          fns datasource)))))
