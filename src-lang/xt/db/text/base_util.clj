(ns xt.db.text.base-util
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt collect-routes
  "collect routes"
  {:added "4.0"}
  [routes type]
  (return
   (xtd/arr-juxt routes
                 (fn [e] (return (. e ["url"])))
                 (fn [e] (return (xt/x:obj-assign {:type type}
                                                  e))))))

(defn.xt collect-views
  "collect views into views structure"
  {:added "4.0"}
  [routes]
  (var out {})
  (xt/for:array [route routes]
    (var #{view} route)
    (var #{table type tag} view)

    (var v0 (xt/x:get-key out table))
    (when (xt/x:nil? v0)
      (:= v0 {})
      (xt/x:set-key out table v0))

    (var v1 (xt/x:get-key v0 type))
    (when (xt/x:nil? v1)
      (:= v1 {})
      (xt/x:set-key v0 type v1))

    (xt/x:set-key v1 tag route))
  (return out))

(defn.xt merge-views
  "merges multiple views together"
  {:added "4.0"}
  [views acc]
  (var merge-fn
       (fn [e view-entry]
         (xt/x:obj-assign (or (. e ["select"]) {})
                       (. view-entry ["select"]))
         (xt/x:obj-assign (or (. e ["return"]) {})
                       (. view-entry ["return"]))
         (return e)))
  (return (xt/x:arr-foldl views
                       (fn [out view]
                         (return (xtd/obj-assign-with
                                  out
                                  view
                                  merge-fn)))
                       (or acc {}))))

(defn.xt keepf-limit
  "keeps given limit"
  {:added "4.0"}
  ([arr pred f n]
   (var out := [])
   (var i := 0)
   (xt/for:array [e arr]
     (if (== i n) (return out))
     (when (pred e)
       (var interim (f e))
       (when (or (xt/x:is-number? interim)
                 (xt/x:is-string? interim)
                 (xtd/obj-not-empty? interim))
         (xt/x:arr-push out interim)
         (:= i (+ i 1)))))
   (return out)))

(defn.xt lu-nested
  "helper for lu-map"
  {:added "4.0"}
  [obj key-fn]
  (cond (xt/x:nil? obj)
        (return obj)
        
        (xt/x:is-object? obj)
        (return (xtd/obj-map obj (fn [v]
                                   (return (-/lu-nested v key-fn)))))
        
        (xt/x:is-array? obj)
        (return (-/lu-nested (xtd/arr-juxt obj key-fn (fn [x] (return x)))
                             key-fn))
        
        :else (return obj)))

(defn.xt lu-map
  "constructs a nested lu map of ids"
  {:added "4.0"}
  [arr]
  (return (-/lu-nested arr (fn [v] (return (. v ["id"]))))))
