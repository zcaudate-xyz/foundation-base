(ns std.lib.transform.flatten
  (:require [std.lib.transform :as graph]
            [std.lib.transform.base.ref :as ref]
            [std.lib.transform.link :as link])
  (:refer-clojure :exclude [flatten]))

(defn clean-output
  "cleans ref keys in data
 
   (clean-output {:id \"student-a\" :profile {:id \"a\" :name \"Alice\"}}
                 (get-in -schema- [:tree :student]))
   => {:id \"student-a\", :profile :profile.id/a}
 
   (clean-output {:id \"math-5\" :students []}
                 (get-in -schema- [:tree :class]))
   => {:id \"math-5\"}"
  {:added "3.0"}
  ([data schema]
   (reduce-kv (fn [output k v]
                (let [[{:keys [type] :as attr}] (get schema k)]
                  (cond (= (:type attr) :ref)
                        (cond-> output
                          (-> attr :cardinality (not= :many))
                          (assoc k (cond (map? v)
                                         (keyword (str (name k) ".id")
                                                  (or (:id v)
                                                      (throw (ex-info "Requires an id"
                                                                      {:attr attr
                                                                       :key k
                                                                       :value v}))))
                                         (keyword? v) v
                                         :else (throw (ex-info "Unsupported input" {k v})))))

                        :else (assoc output k v))))
              {}
              data)))

(defn wrap-output
  "adding cleaned up data to table"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [^clojure.lang.Volatile state (-> interim :flatten)]
       (if (vector? (second (first tsch)))
         (vswap! state update (first nsv)
                 (fnil #(->> (merge (-> interim :link :parent)
                                    (clean-output tdata tsch))
                             (conj %))
                       [])))
       (f tdata tsch nsv interim fns datasource)))))

(defn flatten
  "converts a graph datastructure into a table
 
   (flatten {:class
             {:id \"maths-5\"
              :name \"MATHS 5\"
              :students [{:id \"student-a\"
                          :profile {:id \"a\"
                                    :name \"Alice\"}}
                         {:id \"student-b\"
                          :profile {:id \"b\"
                                   :name \"Bob\"}}]}}
            -schema-)
   => {:class [{:id \"maths-5\", :name \"MATHS 5\"}],
       :student [{:id \"student-a\", :profile :profile.id/a, :class :class.id/maths-5}
                 {:id \"student-b\", :profile :profile.id/b, :class :class.id/maths-5}],
       :profile [{:id \"a\", :name \"Alice\"} {:id \"b\", :name \"Bob\"}]}"
  {:added "3.0"}
  ([data schema]
   (let [output (volatile! {})
         result (graph/normalise-base data
                                      {:schema schema
                                       :pipeline {:flatten output}}
                                      {:normalise [link/wrap-link-current wrap-output]
                                       :normalise-branch [link/wrap-link-current wrap-output]
                                       :normalise-attr   [link/wrap-link-attr]
                                       :normalise-single [ref/wrap-keyword-id]})]
     @output)))
