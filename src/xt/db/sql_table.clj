(ns xt.db.sql-table
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
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
                             (xt/x:obj-pick m cols)
                             opts)))

(defn.xt table-insert-single
  "generates single insert statement"
  {:added "4.0"}
  [schema table-name m opts]
  (var cols  (base-schema/table-columns schema table-name))
  (var ks    (xt/x:arr-filter cols (fn:> [col]
                                  (xt/x:has-key? m col))))
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
  (var ks    (xt/x:arr-filter cols (fn:> [col]
                                  (xt/x:has-key? m col))))
  (return (raw/raw-upsert table-name
                          "id"
                          ks
                          [m]
                          opts)))

(defn.xt table-filter-id
  "predicate for flat entry"
  {:added "4.0"}
  [entry]
  (return (not (and (== 0 (xt/x:len (xt/x:obj-keys (xt/x:get-key entry "ref_links"))))
                    (== 1 (xt/x:len (xt/x:obj-keys (xt/x:get-key entry "data"))))))))

(defn.xt table-get-data
  "gets data flat entry"
  {:added "4.0"}
  [entry]
  (var out  (xt/x:obj-clone (xt/x:get-key entry "data")))
  (xt/for:object [[link m] (xt/x:get-key entry "ref_links")]
    (xt/x:set-key out (xt/x:cat link "_id") (xt/x:obj-first-key m)))
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
  (var ordered (xt/x:arr-keep (base-schema/table-order lookup)
                           (fn [col]
                             (return (:? (xt/x:has-key? flat col) [col (xt/x:get-key flat col)] nil)))))
  (var column-fn  (xt/x:get-key opts "column_fn" k/identity))
  (var emit-pair-fn
       (fn [pair]
         (var [table-name data] pair)
         (var cols     (base-schema/table-columns schema table-name))
         (var defaults (base-schema/table-defaults schema table-name))
         (var out  (xt/x:arr-keepf (xt/x:obj-vals data)
                                -/table-filter-id
                                -/table-get-data))
         (var sout (xt/x:arr-map out (fn:> [v] (xt/x:obj-assign (xt/x:obj-clone defaults) v))))
         (var #{schema-update} (xt/x:get-key lookup table-name))
         (var #{update-key} opts)
         (var sopts)
         (if (and schema-update
                  (xt/x:not-nil? update-key))
           (:= sopts (xt/x:obj-assign {:upsert-clause
                                    (xt/x:cat "\"excluded\"."
                                           (column-fn update-key)
                                           " < "
                                           (column-fn update-key))}
                                   opts))
           (:= sopts (xt/x:obj-clone opts)))
         (when (< 0 (xt/x:len sout))
           (return (emit-fn table-name
                            cols
                            sout
                            sopts)))))
  (return (xt/x:arr-keep ordered emit-pair-fn)))

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

