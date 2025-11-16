(ns xt.db.base-view
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt  all-overview
  "gets an overview of the views"
  {:added "4.0"}
  [views]
  (return (k/obj-map views
                     (fn [m]
                       (return (k/obj-map m k/obj-keys))))))

(defn.xt all-keys
  "gets all table keys for a view"
  {:added "4.0"}
  [views table type]
  (var tviews (k/get-key views table))
  (var ttypes (:? (k/not-nil? tviews)
                  (k/get-key tviews type)
                  {}))
  (return (k/obj-keys (or ttypes {}))))

(defn.xt all-methods
  "gets all methods for views"
  {:added "4.0"}
  [views]
  (var method-fn
       (fn [views table type]
         (return (k/arr-map (-/all-keys views table type)
                            (fn [sk] (return [table type sk]))))))
  (return (k/arr-mapcat (k/obj-keys views)
                        (fn [k]
                          (return (k/arr-append (method-fn views k "select")
                                                (method-fn views k "return")))))))

