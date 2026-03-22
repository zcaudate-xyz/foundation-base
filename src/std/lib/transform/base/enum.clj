(ns std.lib.transform.base.enum
  (:require [std.lib.foundation :as h]
            [std.string.common :as str]
            [std.string.path :as path]
            [std.string.wrap :as wrap]))

(defn wrap-single-enum
  "wraps normalise with comprehension of the enum type
 
   (graph/normalise {:account {:type :account.type/guest}}
                    {:schema (schema/schema {:account/type [{:type :enum
                                                             :enum {:ns :account.type
                                                                    :values #{:vip :guest}}}]})}
                    {:normalise-single [wrap-single-enum]})
   => {:account {:type :guest}}"
  {:added "3.0"}
  ([f]
   (fn [subdata [attr] nsv interim fns datasource]
     (cond (= :enum (:type attr))
           (let [kns (-> attr :enum :ns)
                 v (if (and kns (= ((wrap/wrap path/path-ns) subdata) kns))
                     ((wrap/wrap path/path-stem) subdata)
                     subdata)
                 chk (-> attr :enum :values)]
             (if-not (h/suppress (chk v))
               (h/error (str "WRAP_SINGLE_ENUMS: " v " in " nsv " can only be one of: " chk)
                        {:id :wrong-input
                         :data subdata :nsv nsv :key-path (:key-path interim) :check chk})
               (f v [attr] nsv interim fns datasource)))
           :else
           (f subdata [attr] nsv interim fns datasource)))))
