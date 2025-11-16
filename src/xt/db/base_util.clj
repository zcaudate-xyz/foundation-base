(ns xt.db.base-util
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt collect-routes
  "collect routes"
  {:added "4.0"}
  [routes type]
  (return
   (k/arr-juxt routes
               (fn:> [e] (k/get-key e "url"))
               (fn:> [e] (k/obj-assign {:type type}
                                       e)))))

(defn.xt collect-routes-object
  "TODO"
  {:added "4.0"}
  [routes type]
  (return
   (k/arr-juxt (k/obj-vals routes)
               (fn:> [e] (k/get-key e "url"))
               (fn:> [e] (k/obj-assign {:type type}
                                       e)))))

(defn.xt collect-views
  "collect views into views structure"
  {:added "4.0"}
  [routes]
  (var out {})
  (k/for:array [route routes]
    (var #{view} route)
    (var #{table type tag} view)

    (var v0 (k/get-key out table))
    (when (k/nil? v0)
      (:= v0 {})
      (k/set-key out table v0))

    (var v1 (k/get-key v0 type))
    (when (k/nil? v1)
      (:= v1 {})
      (k/set-key v0 type v1))

    (k/set-key v1 tag route))
  (return out))

(defn.xt merge-views
  "merges multiple views together"
  {:added "4.0"}
  [views acc]
  (var merge-fn
       (fn [e view-entry]
         (k/obj-assign (or (k/get-key e "select") {})
                       (k/get-key view-entry "select"))
         (k/obj-assign (or (k/get-key e "return") {})
                       (k/get-key view-entry "return"))
         (return e)))
  (return (k/arr-foldl views
                       (fn [out view]
                         (return (k/obj-assign-with
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
   (k/for:array [e arr]
     (if (== i n) (return out))
     (when (pred e)
       (var interim (f e))
       (when (or (k/is-number? interim)
                 (k/is-string? interim)
                 (k/not-empty? interim))
         (x:arr-push out interim)
         (:= i (+ i 1)))))
   (return out)))

(defn.xt lu-nested
  "helper for lu-map"
  {:added "4.0"}
  [obj key-fn]
  (cond (k/nil? obj)
        (return obj)
        
        (k/obj? obj)
        (return (k/obj-map obj (fn:> [v] (-/lu-nested v key-fn))))
        
        (k/arr? obj)
        (return (-/lu-nested (k/arr-juxt obj key-fn k/identity)
                             key-fn))
        
        :else (return obj)))

(defn.xt lu-map
  "constructs a nested lu map of ids"
  {:added "4.0"}
  [arr]
  (return (-/lu-nested arr (fn:> [v] (k/get-key v "id")))))

(comment
  (./create-tests)
  (./arrange)
  (l/rt:module-purge :xtalk))
