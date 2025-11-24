(ns code.manage.unit.require
  (:require [code.framework :as base]
            [code.project :as project]
            [std.lib.result :as res]))

(defn require-file
  "requires the file and returns public vars

   (require-file 'code.manage)
   => (contains '[analyse extract ...])"
  {:added "3.0"}
  ([ns params lookup project]
   (try
     (clojure.core/require ns)
     (vec (sort (keys (ns-publics ns))))
     (catch Throwable t
       (res/result {:status :error
                    :data (str t)})))))
