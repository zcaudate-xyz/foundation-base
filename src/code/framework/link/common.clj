(ns code.framework.link.common
  (:require [std.fs :as fs]
            [std.lib :refer [definvoke]]))

(defmulti -file-linkage
  "extendable function for `file-linkage`"
  {:added "3.0"}
  fs/file-suffix)

(defmethod -file-linkage :default
  ([file]
   {:file file
    :exports #{}
    :imports #{}}))

(defrecord FileInfo []
  Object
  (toString [this] (-> this :path)))

(defmethod print-method FileInfo
  ([v ^java.io.Writer w]
   (.write w (str v))))

(definvoke file-linkage-fn
  "memoized function for `file-linkage` based on time"
  {:added "3.0"}
  [:memoize]
  ([path time]
   (-file-linkage path)))
