(ns std.lib.transform.mask
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]))

(defn process-mask
  "Determines correct output given data and mask
 
   (mask/process-mask {:name :checked}
                      {:name \"Chris\" :age 10}
                      [:account]
                      {}
                      (-> (schema/schema examples/account-name-age-sex)
                          :tree
                          :person)
                      {})
  => {:age 10}"
  {:added "3.0"}
  ([smask tdata nsv interim tsch datasource]
   (if-let [[k v] (first smask)]
     (cond (and (coll/hash-map? v)
                (-> tsch (get k) vector?)
                (-> tsch (get k) first :type (= :ref)))
           (recur (next smask) tdata nsv interim tsch datasource)

           :else
           (let [subdata (get tdata k)]
             (cond (nil? subdata)
                   (recur (next smask) tdata nsv interim tsch datasource)

                   (coll/hash-map? v)
                   (recur (next smask)
                          (assoc tdata k (process-mask v
                                                       subdata
                                                       (conj nsv k)
                                                       interim
                                                       (get tsch k)
                                                       datasource))
                          nsv interim tsch datasource)

                   (= :unchecked v)
                   (recur (next smask) tdata nsv interim tsch datasource)

                   (fn? v)
                   (let [flag (v subdata datasource)]
                     (if (or (= :unchecked flag)
                             (false? flag)
                             (nil? flag))
                       (recur (next smask) tdata nsv interim tsch datasource)
                       (recur (next smask) (dissoc tdata k) nsv interim tsch datasource)))

                   :else
                   (recur (next smask) (dissoc tdata k) nsv interim tsch datasource))))
     tdata)))

(defn wrap-model-pre-mask
  "Masks data across elements and schema
 
   (graph/normalise {:account/age 10}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:pre-mask {:account :checked}}}
                    *wrappers*)
   => {}
 
   (graph/normalise {:account/orders #{{:number 1 :items {:name \"one\"}}
                                       {:number 2 :items {:name \"two\"}}}}
                    {:schema (schema/schema examples/account-orders-items-image)
                     :pipeline {:pre-mask {:account {:orders {:number :checked}}}}}
                    *wrappers*)
   => {:account {:orders #{{:items {:name \"one\"}}
                           {:items {:name \"two\"}}}}}"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [smask (:pre-mask interim)
           output (process-mask smask tdata nsv interim tsch datasource)]
       (f output tsch nsv (update-in interim [:ref-path]
                                     #(-> %
                                          (pop)
                                          (conj output)))
          fns datasource)))))

(defn wrap-model-post-mask
  "Masks data in pipeline post transforms 
 
   (graph/normalise {:account/name \"Chris\"}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:post-mask {:account {:name :checked}}}}
                    {:normalise  [mask/wrap-model-post-mask]})
   => {:account {}}"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [smask (:post-mask interim)
           output (f tdata tsch nsv interim fns datasource)]
       (process-mask smask output nsv interim tsch datasource)))))
