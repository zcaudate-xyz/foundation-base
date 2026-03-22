(ns std.lib.transform.fill-assoc
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.lib.transform.base.complex :as complex]))

(defn process-fill-assoc
  "helper function for wrap-model-fill-assoc
 
   (process-fill-assoc {:age 10}
                       {:name \"Chris\", :age 9}
                       [:account]
                       {}
                       {:fill-assoc {:account {:age 10}}}
                       {})
   => {:name \"Chris\", :age #{9 10}}"
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
                        (assoc tdata k (process-fill-assoc v
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
             (cond (fn? v)
                   (recur (next sfill)
                          (complex/assocs tdata k (v (:ref-path interim) datasource))
                          nsv interim tsch datasource)

                   (not (coll/hash-map? v))
                   (recur (next sfill)
                          (complex/assocs tdata k v) nsv interim tsch datasource)

                   (coll/hash-map? subdata)
                   (recur (next sfill)
                          (assoc tdata k (process-fill-assoc v
                                                             (get tdata k)
                                                             (conj nsv k)
                                                             interim
                                                             (get tsch k)
                                                             datasource))
                          nsv interim tsch datasource))))
     tdata)))

(defn wrap-model-fill-assoc
  "adds an additional value to the entry
 
   (graph/normalise {:account/name \"Chris\" :account/age 9}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:fill-assoc {:account {:age 10}}}}
                    {:normalise [wrap-model-fill-assoc]})
   => {:account {:name \"Chris\", :age #{9 10}}}"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [sfill (:fill-assoc interim)]
       (if (coll/hash-map? sfill)
         (let [output (process-fill-assoc sfill tdata nsv interim tsch datasource)]
           (f output tsch nsv (update-in interim [:ref-path]
                                         #(-> %
                                              (pop)
                                              (conj output)))
              fns datasource))
         (f tdata tsch nsv interim fns datasource))))))
