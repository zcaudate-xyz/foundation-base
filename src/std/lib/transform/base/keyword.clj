(ns std.lib.transform.base.keyword
  (:require [std.lib.foundation :as h]
            [std.string.common :as str]
            [std.string.path :as path]
            [std.string.wrap :as wrap]))

(defn wrap-single-keyword
  "removes the keyword namespace if there is one
 
   (graph/normalise {:account {:type :account.type/vip}}
                    {:schema (schema/schema {:account/type [{:type :keyword
                                                             :keyword {:ns :account.type}}]})}
                    {:normalise-single [wrap-single-keyword]})
   => {:account {:type :vip}}"
  {:added "3.0"}
  ([f]
   (fn [subdata [attr] nsv interim fns datasource]
     (cond (= :keyword (:type attr))
           (let [kns (-> attr :keyword :ns)
                 v   (if (and kns (= ((wrap/wrap path/path-ns) subdata)  kns))
                       ((wrap/wrap path/path-stem) subdata)
                       subdata)]
             v)
           :else
           (f subdata [attr] nsv interim fns datasource)))))
