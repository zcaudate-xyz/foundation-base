(ns std.lib.transform.require
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]))

(defn process-require
  "Checks for correct entry
 
   (require/process-require {:name :checked}
                            :no-required
                            {:name \"Chris\"}
                            [:account]
                            (-> (schema/schema examples/account-name-age-sex)
                                :tree
                                :account)
                            {})
  => {:name \"Chris\"}"
  {:added "3.0"}
  ([req require-key tdata nsv tsch datasource]
   (if-let [[k v] (first req)]
     (cond (= v :checked)
           (do (if (not (get tdata k))
                 (h/error (str "PROCESS_REQUIRE: key " (conj nsv k) " is not present")
                          {:id :require-key :key require-key
                           :nsv (conj nsv k) :data tdata}))
               (recur (next req) require-key tdata nsv tsch datasource))

           (fn? v)
           (let [subdata (get tdata k)
                 flag (v subdata datasource)]
             (do (if (and (or (= flag :checked)
                              (true? flag))
                          (nil? subdata))
                   (h/error (str "PROCESS_REQUIRE: key " (conj nsv k) " is not present")
                            {:id :require-key :key require-key
                             :nsv (conj nsv k) :data tdata}))
                 (recur (next req) require-key tdata nsv tsch datasource)))

           (and (-> tsch (get k) vector?)
                (-> tsch (get k) first :type (= :ref)))
           (recur (next req) require-key tdata nsv tsch datasource)

           :else
           (let [subdata (get tdata k)]
             (cond (nil? subdata)
                   (h/error (str "PROCESS_REQUIRE: key " (conj nsv k) " is not present")
                            {:id :require-key :key require-key
                             :nsv (conj nsv k) :data tdata})

                   (coll/hash-map? subdata)
                   (process-require v require-key (get tdata k) (conj nsv k) (get tsch k) datasource))
             (recur (next req) require-key tdata nsv tsch datasource)))
     tdata)))

(defn wrap-model-pre-require
  "Checks for data across elements and schema pre transforms
   (graph/normalise
    {:account/orders #{{:number 1}
                       {:number 2}}}
    {:schema (schema/schema examples/account-orders-items-image)
     :pipeline {:pre-require
                {:account {:orders
                           {:number :checked}}}}}
    *wrappers*)
   => {:account {:orders #{{:number 1}
                           {:number 2}}}}
 
   (graph/normalise
    {:account/orders #{{:items {:name \"stuff\"}}
                       {:number 2}}}
    {:schema (schema/schema
              examples/account-orders-items-image)
     :pipeline {:pre-require
                {:account
                 {:orders {:number :checked}}}}}
   *wrappers*)
   => (throws-info {:data {:items {:name \"stuff\"}}
                    :nsv [:order :number]
                    :id :require-key})"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [req (:pre-require interim)]
       (process-require req :no-required tdata nsv tsch datasource)
       (f tdata tsch nsv interim fns datasource)))))

(defn wrap-model-post-require
  "Checks for data across elements and schema post transforms
 
   (graph/normalise
    {:account/name \"Chris\"}
    {:schema (schema/schema examples/account-name-age-sex)
     :pipeline {:post-require {:account {:name :checked}}}}
    {:normalise [require/wrap-model-post-require]})
   => {:account {:name \"Chris\"}}
 
   (graph/normalise
    {:account/age 10}
    {:schema (schema/schema examples/account-name-age-sex)
     :pipeline {:post-require
                {:account {:name :checked}}}}
    {:normalise [require/wrap-model-post-require]})
   => (throws-info {:nsv [:account :name]
                    :id :require-key})"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [req (:post-require interim)
           output (f tdata tsch nsv interim fns datasource)]
       (process-require req :no-required output nsv tsch datasource)))))
