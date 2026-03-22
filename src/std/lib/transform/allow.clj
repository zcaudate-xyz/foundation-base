(ns std.lib.transform.allow
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.string.common :as str]
            [std.string.path :as path]
            [std.string.wrap :as wrap]))

(defn wrap-branch-model-allow
  "Works together with wrap-attr-model-allow to control access to data
   (graph/normalise {:account/name \"Chris\"}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:allow {}}}
                    *wrappers*)
   => (throws-info {:data {:name \"Chris\"}
                    :key-path [:account]
                    :id :not-allowed
                    :nsv [:account]})
 
   (graph/normalise {:account/name \"Chris\"}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:allow {:account {:name :checked}}}}
                    *wrappers*)
   => {:account {:name \"Chris\"}}"
  {:added "3.0"}
  ([f]
   (fn [subdata subsch nsv interim fns datasource]
     (let [suballow (:allow interim)
           nsubdata  (if (nil? suballow)
                       (h/error (str "WRAP_BRANCH_MODEL_ALLOW: key " nsv " is not accessible.")
                                {:id :not-allowed :data subdata :nsv nsv :key-path (:key-path interim)})
                       subdata)]
       (f nsubdata subsch nsv interim fns datasource)))))

(defn wrap-attr-model-allow
  "wrapper function for only allowing values defined to be included
 
   (graph/normalise {:account {:name \"Chris\"
                               :age 10}}
                    {:schema (schema/schema examples/account-name-age-sex)
                     :pipeline {:allow {:account {:name :checked}}}}
                    *wrappers*)
   => (throws)"
  {:added "3.0"}
  ([f]
   (fn [subdata [attr] nsv interim fns datasource]
     (let [suballow (:allow interim)]
       (cond (= (:type attr) :ref)
             (cond (= suballow :yield)
                   (let [ynsv   ((wrap/wrap path/path-split) (-> attr :ref :ns))
                         tmodel (get-in datasource (concat [:pipeline :allow] ynsv))]
                     (f subdata [attr] ynsv (assoc interim :allow tmodel) fns datasource))

                   (or (= suballow :id) (coll/hash-map? suballow))
                   (f subdata [attr] nsv interim fns datasource)

                   :else
                   (h/error (str "WRAP_ATTR_MODEL_ALLOW: " nsv " is not accessible")
                            {:id :not-allowed :data subdata :nsv nsv :key-path (:key-path interim)}))

             (or (nil? suballow) (not= suballow :checked))
             (let [nsubdata (h/error (str "WRAP_ATTR_MODEL_ALLOW: " nsv " is not accessible")
                                     {:id :not-allowed :data subdata :nsv nsv
                                      :key-path (:key-path interim)})]
               (f nsubdata [attr] nsv interim fns datasource))

             :else
             (f subdata [attr] nsv interim fns datasource))))))
