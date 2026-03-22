(ns std.lib.transform.base.type-check
  (:require [std.lib.foundation :as h]
            [std.lib.transform.coerce :as coerce]))

(defn wrap-single-type-check
  "wraps normalise to type check inputs as well as to coerce incorrect inputs"
  {:added "3.0"}
  ([f]
   (fn [subdata [attr] nsv interim fns datasource]

     (let [t (:type attr)
           chk (h/error "TODO")
          ;;chk (base/type-checks (:type datasource) t)
           ]
       (cond
         (or (nil? chk) (chk subdata))
         (f subdata [attr] nsv interim fns datasource)

         (-> datasource :options :use-coerce)
         (f (coerce/coerce subdata t) [attr] nsv interim fns datasource)

         :else
         (h/error (str "WRAP_SINGLE_TYPE_CHECK: " subdata " in " nsv " is not of type " t)
                  {:id :wrong-type
                   :data subdata :nsv nsv :key-path (:key-path interim) :type t}))))))
