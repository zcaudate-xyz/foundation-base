(ns xt.db.base-view
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt  all-overview
  "gets an overview of the views"
  {:added "4.0"}
  [views]
  (return (xtd/obj-map
           views
           (fn [m]
             (return (xtd/obj-map m xt/x:obj-keys))))))

(defn.xt all-keys
  "gets all table keys for a view"
  {:added "4.0"}
  [views table type]
  (var tviews (xt/x:get-key views table))
  (var ttypes (:? (xt/x:not-nil? tviews)
                  (xt/x:get-key tviews type)
                  {}))
  (return (xtd/arr-clone (xt/x:obj-keys ttypes))))

(defn.xt all-methods
  "gets all methods for views"
  {:added "4.0"}
  [views]
  (var method-fn
       (fn [views table type]
          (return (xt/x:arr-map (-/all-keys views table type)
                                (fn [sk] (return [table type sk]))))))
  (return (xtd/arr-clone
           (xtd/arr-mapcat (xt/x:obj-keys views)
                           (fn [k]
                             (return (xt/x:arr-append (method-fn views k "select")
                                                      (method-fn views k "return"))))))))
