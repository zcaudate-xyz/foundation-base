(ns xt.db.base-flatten
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [flatten]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.base-schema :as sch]]})

(defn.xt flatten-get-links
  "flatten links"
  {:added "4.0"}
  [obj]
  (var link-fn
       (fn [e]
         (if (k/is-string? e)
           (return [e true])
           (if (k/nil? e)
             (k/err (k/cat "Invalid link - " (k/json-encode obj)))
             (return [(k/get-key e "id") true])))))
  (return
   (k/obj-keep obj
               (fn:> [v]
                     (:? (k/arr? v)
                         (-> (k/arr-map v link-fn)
                             (k/obj-from-pairs))
                         nil)))))

(defn.xt flatten-merge
  "flatten data"
  {:added "4.0"}
  [table-map data-obj ref-links rev-links]
  (var #{id} := data-obj)
  (var rec := (k/get-key table-map id))
  (when (not rec)
    (:= rec {:id id
             :data {}
             :ref-links {}
             :rev-links {}})
    (k/set-key table-map id rec))
  (k/obj-assign (k/get-key rec "data") data-obj)
  (k/swap-key rec "ref_links" k/obj-assign-with ref-links k/obj-assign)
  (k/swap-key rec "rev_links" k/obj-assign-with rev-links k/obj-assign)
  (return table-map))

(defn.xt flatten-node
  "flatten node"
  {:added "4.0"}
  [schema table-name data parent acc]
  (:= data  (k/obj-assign data (k/clone-nested parent)))
  (var table-map (k/get-key acc table-name))
  (when (not table-map)
    (:= table-map {})
    (k/set-key acc table-name table-map))
  (var data-obj     (k/obj-pick data (sch/data-keys schema table-name)))
  (var obj-fn       (fn:> [v] (:? (k/obj? v) [v] v)))
  (var rev-obj      (-> (k/obj-pick data (sch/rev-keys schema table-name))
                        (k/obj-keep obj-fn)))
  (var rev-links    (-/flatten-get-links rev-obj))
  (var ref-obj      (-> (k/obj-pick data (sch/ref-keys schema table-name))
                        (k/obj-keep obj-fn)))
  (var ref-links    (-/flatten-get-links ref-obj))
  (var ref-id-map   (sch/ref-id-keys schema table-name))
  (var ref-id-links {})
  (k/for:object [[id-k k] ref-id-map]
            (if (k/is-string? (k/get-key data id-k))
              (k/set-key ref-id-links k {(k/get-key data id-k) true})))
  (-/flatten-merge table-map
                   data-obj
                   (k/obj-assign-with ref-links ref-id-links k/obj-assign)
                   rev-links)
  (return {:table-map table-map
           :data-obj data-obj
           :ref-obj ref-obj
           :rev-obj rev-obj}))

(defn.xt flatten-linked
  "flatten linked for schema"
  {:added "4.0"}
  [schema table-name link-obj link-id acc flatten-fn]
  (var link-fn
       (fn [e]
         (var ref (k/get-in schema [table-name e "ref"]))
         (return [(k/get-key ref "ns")
                  (k/get-key ref "rval")])))
  (k/for:object [[e v] link-obj]
    (when (k/arr? v)
      (var [link-key link-path] (link-fn e))
      (k/for:array [e (k/arr-filter v k/obj?)]
        (flatten-fn schema link-key
                    e 
                    {link-path [link-id]}
                    acc))))
  (return acc))

(defn.xt flatten-obj
  "flatten data for schema"
  {:added "4.0"}
  [schema table-name obj parent acc]
  (var flattened  (-/flatten-node schema table-name obj parent acc))
  (var #{table-map
         data-obj
         ref-obj
         rev-obj} flattened)
  (var link-id (k/get-key data-obj "id"))
  (-/flatten-linked schema table-name rev-obj link-id acc -/flatten-obj)
  (-/flatten-linked schema table-name ref-obj link-id acc -/flatten-obj)
  (return acc))

(defn.xt flatten
  "flattens data schema"
  {:added "4.0"}
  [schema table-name data parent]
  (:= data (or data []))
  (var acc {})
  (if (k/arr? data)
    (k/for:array [subdata data]
      (when (k/not-nil? subdata)
        (-/flatten-obj schema table-name subdata (or parent {}) acc)))
    (-/flatten-obj schema table-name data (or parent {}) acc))
  (return acc))

(defn.xt flatten-bulk
  "flattens bulk data"
  {:added "4.0"}
  [schema m]
  (var acc {})
  (var bulk (:? (k/arr? m)
                m
                (k/obj-pairs m)))
  (k/for:array [e bulk]
    (var [table-name arr] e)
    (k/for:array [obj arr]
      (when (k/not-nil? obj)
        (-/flatten-obj schema table-name obj {} acc))))
  (return acc))

