(ns xt.db.sql-table
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.base-schema :as base-schema]
             [xt.db.base-flatten :as f]
             [xt.db.sql-raw :as raw]]})

(defn.xt table-update-single
  "generates single update statement"
  {:added "4.0"}
  [schema table-name id m opts]
  (var cols  (base-schema/table-columns schema table-name))
  (return    (raw/raw-update table-name
                             {:id id}
                             (k/obj-pick m cols)
                             opts)))

(defn.xt table-insert-single
  "generates single insert statement"
  {:added "4.0"}
  [schema table-name m opts]
  (var cols  (base-schema/table-columns schema table-name))
  (var ks    (k/arr-filter cols (fn:> [col]
                                  (k/has-key? m col))))
  (return (raw/raw-insert table-name
                          ks
                          [m]
                          opts)))

(defn.xt table-delete-single
  "generates single delete statement"
  {:added "4.0"}
  [schema table-name id opts]
  (var cols  (base-schema/table-columns schema table-name))
  (return  (raw/raw-delete table-name
                           {:id id}
                           opts)))

(defn.xt table-upsert-single
  "generates single upsert statement"
  {:added "4.0"}
  [schema table-name m opts]
  (var cols  (base-schema/table-columns schema table-name))
  (var ks    (k/arr-filter cols (fn:> [col]
                                  (k/has-key? m col))))
  (return (raw/raw-upsert table-name
                          "id"
                          ks
                          [m]
                          opts)))

(defn.xt table-filter-id
  "predicate for flat entry"
  {:added "4.0"}
  [entry]
  (return (not (and (== 0 (k/len (k/obj-keys (k/get-key entry "ref_links"))))
                    (== 1 (k/len (k/obj-keys (k/get-key entry "data"))))))))

(defn.xt table-get-data
  "gets data flat entry"
  {:added "4.0"}
  [entry]
  (var out  (k/obj-clone (k/get-key entry "data")))
  (k/for:object [[link m] (k/get-key entry "ref_links")]
    (k/set-key out (k/cat link "_id") (k/obj-first-key m)))
  (return out))

(def.xt ^{:arglists '([table-name cols out opts])}
  table-emit-insert raw/raw-insert)

(def.xt ^{:arglists '([table-name cols out opts])}
  table-emit-upsert
  (fn [table-name cols out opts]
    (return (raw/raw-upsert table-name
                            "id"
                            cols
                            out
                            opts))))

(defn.xt table-emit-flat
  "emit util for insert and upsert"
  {:added "4.0"}
  [emit-fn schema lookup flat opts]
  (var ordered (k/arr-keep (base-schema/table-order lookup)
                           (fn [col]
                             (return (:? (k/has-key? flat col) [col (k/get-key flat col)] nil)))))
  (var column-fn  (k/get-key opts "column_fn" k/identity))
  (var emit-pair-fn
       (fn [pair]
         (var [table-name data] pair)
         (var cols     (base-schema/table-columns schema table-name))
         (var defaults (base-schema/table-defaults schema table-name))
         (var out  (k/arr-keepf (k/obj-vals data)
                                -/table-filter-id
                                -/table-get-data))
         (var sout (k/arr-map out (fn:> [v] (k/obj-assign (k/obj-clone defaults) v))))
         (var #{schema-update} (k/get-key lookup table-name))
         (var #{update-key} opts)
         (var sopts)
         (if (and schema-update
                  (k/not-nil? update-key))
           (:= sopts (k/obj-assign {:upsert-clause
                                    (k/cat "\"excluded\"."
                                           (column-fn update-key)
                                           " < "
                                           (column-fn update-key))}
                                   opts))
           (:= sopts (k/obj-clone opts)))
         (when (< 0 (k/len sout))
           (return (emit-fn table-name
                            cols
                            sout
                            sopts)))))
  (return (k/arr-keep ordered emit-pair-fn)))

(defn.xt table-insert
  "creates an insert statement"
  {:added "4.0"}
  [schema lookup table-name data opts]
  (var flat (f/flatten schema table-name data {}))
  (return (-/table-emit-flat -/table-emit-insert schema lookup flat opts)))

(defn.xt table-upsert
  "generate upsert statement"
  {:added "4.0"}
  [schema lookup table-name data opts]
  (var flat (f/flatten schema table-name data {}))
  (return (-/table-emit-flat -/table-emit-upsert schema lookup flat opts)))

