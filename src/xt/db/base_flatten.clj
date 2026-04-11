(ns xt.db.base-flatten
  (:require [std.lang :as l])
  (:refer-clojure :exclude [flatten]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.db.base-schema :as sch]]})

(defn.xt flatten-get-links
  "flatten links"
  {:added "4.0"}
  [obj]
  (var link-fn
       (fn [e]
         (if (xt/x:is-string? e)
           (return [e true])
           (if (xt/x:nil? e)
             (xt/x:err (xt/x:cat "Invalid link - " (xt/x:json-encode obj)))
             (return [(xt/x:get-key e "id") true])))))
  (return
   (xt/x:obj-keep obj
               (fn:> [v]
                     (:? (xt/x:is-array? v)
                         (-> (xt/x:arr-map v link-fn)
                             (xt/x:obj-from-pairs))
                         nil)))))

(defn.xt flatten-merge
  "flatten data"
  {:added "4.0"}
  [table-map data-obj ref-links rev-links]
  (var #{id} := data-obj)
  (var rec := (xt/x:get-key table-map id))
  (when (not rec)
    (:= rec {:id id
             :data {}
             :ref-links {}
             :rev-links {}})
    (xt/x:set-key table-map id rec))
  (xt/x:obj-assign (xt/x:get-key rec "data") data-obj)
  (xt/x:swap-key rec "ref_links" k/obj-assign-with ref-links k/obj-assign)
  (xt/x:swap-key rec "rev_links" k/obj-assign-with rev-links k/obj-assign)
  (return table-map))

(defn.xt flatten-node
  "flatten node"
  {:added "4.0"}
  [schema table-name data parent acc]
  (:= data  (xt/x:obj-assign data (xt/x:clone-nested parent)))
  (var table-map (xt/x:get-key acc table-name))
  (when (not table-map)
    (:= table-map {})
    (xt/x:set-key acc table-name table-map))
  (var data-obj     (xt/x:obj-pick data (sch/data-keys schema table-name)))
  (var obj-fn       (fn:> [v] (:? (xt/x:is-object? v) [v] v)))
  (var rev-obj      (-> (xt/x:obj-pick data (sch/rev-keys schema table-name))
                        (xt/x:obj-keep obj-fn)))
  (var rev-links    (-/flatten-get-links rev-obj))
  (var ref-obj      (-> (xt/x:obj-pick data (sch/ref-keys schema table-name))
                        (xt/x:obj-keep obj-fn)))
  (var ref-links    (-/flatten-get-links ref-obj))
  (var ref-id-map   (sch/ref-id-keys schema table-name))
  (var ref-id-links {})
  (xt/for:object [[id-k k] ref-id-map]
            (if (xt/x:is-string? (xt/x:get-key data id-k))
              (xt/x:set-key ref-id-links k {(xt/x:get-key data id-k) true})))
  (-/flatten-merge table-map
                   data-obj
                   (xt/x:obj-assign-with ref-links ref-id-links k/obj-assign)
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
         (var ref (xtd/get-in schema [table-name e "ref"]))
         (return [(xt/x:get-key ref "ns")
                  (xt/x:get-key ref "rval")])))
  (xt/for:object [[e v] link-obj]
    (when (xt/x:is-array? v)
      (var [link-key link-path] (link-fn e))
      (xt/for:array [e (xt/x:arr-filter v xt/x:is-object?)]
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
  (var link-id (xt/x:get-key data-obj "id"))
  (-/flatten-linked schema table-name rev-obj link-id acc -/flatten-obj)
  (-/flatten-linked schema table-name ref-obj link-id acc -/flatten-obj)
  (return acc))

(defn.xt flatten
  "flattens data schema"
  {:added "4.0"}
  [schema table-name data parent]
  (:= data (or data []))
  (var acc {})
  (if (xt/x:is-array? data)
    (xt/for:array [subdata data]
      (when (xt/x:not-nil? subdata)
        (-/flatten-obj schema table-name subdata (or parent {}) acc)))
    (-/flatten-obj schema table-name data (or parent {}) acc))
  (return acc))

(defn.xt flatten-bulk
  "flattens bulk data"
  {:added "4.0"}
  [schema m]
  (var acc {})
  (var bulk (:? (xt/x:is-array? m)
                m
                (xt/x:obj-pairs m)))
  (xt/for:array [e bulk]
    (var [table-name arr] e)
    (xt/for:array [obj arr]
      (when (xt/x:not-nil? obj)
        (-/flatten-obj schema table-name obj {} acc))))
  (return acc))

