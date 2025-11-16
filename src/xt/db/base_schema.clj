(ns xt.db.base-schema
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(def.xt CACHED_SCHEMA (x:lu-create))

(def.xt CACHED_LOOKUP (x:lu-create))

(def.xt ^{:arglists '([e])}
  get-order (fn:> [e] (k/get-key e "order")))

(def.xt ^{:arglists '([e])}
  get-ident (fn:> [e] (k/get-key e "ident")))

(defn.xt get-ident-id
  "gets the ident id for a schema entry"
  {:added "4.0"}
  [e]
  (return (:? (== "ref" (k/get-key e "type"))
              (k/cat (k/get-key e "ident") "_id")
              (k/get-key e "ident"))))

(defn.xt list-tables
  "list tables"
  {:added "4.0"}
  [schema]
  (return  (k/arr-sort (k/obj-keys schema)
                       k/identity
                       k/lt-string)))

(defn.xt get-cached-schema
  "get lookup"
  {:added "4.0"}
  [schema]
  (var cached := (k/lu-get -/CACHED_SCHEMA schema))
  (when (not cached)
    (:= cached {})
    (k/lu-set -/CACHED_SCHEMA schema cached))
  (return cached))

(defn.xt create-data-keys
  "creates data keys"
  {:added "4.0"}
  ([schema table-name]
   (var table := (k/get-key schema table-name))
   (return (-> (k/obj-vals table)
               (k/arr-filter (fn:> [e] (and (k/is-number? (k/get-key e "order"))
                                            (not= (k/get-key e "type") "ref"))))
               (k/arr-sort   -/get-order k/lt)
               (k/arr-map    -/get-ident)))))

(defn.xt create-ref-keys
  "creates ref keys"
  {:added "4.0"}
  ([schema table-name]
   (var table := (k/get-key schema table-name))
   (return (-> (k/obj-vals table)
               (k/arr-filter (fn:> [e] (and (k/is-number? (k/get-key e "order"))
                                            (== (k/get-key e "type") "ref"))))
               (k/arr-sort   -/get-order k/lt)
               (k/arr-map    -/get-ident)))))

(defn.xt create-rev-keys
  "creates rev keys"
  {:added "4.0"}
  ([schema table-name]
   (var table := (k/get-key schema table-name))
   (return (-> (k/obj-vals table)
               (k/arr-filter (fn:> [e] (not (k/is-number?
                                             (k/get-key e "order")))))
               (k/arr-map    -/get-ident)))))

(defn.xt create-table-entries
  "creates the table keys"
  {:added "4.0"}
  [schema table-name]
  (var table := (k/get-key schema table-name))
  (return (-> (k/obj-vals table)
              (k/arr-filter (fn [e]
                              (return (k/is-number? (k/get-key e "order")))))
              (k/arr-sort   -/get-order k/lt))))

(defn.xt create-defaults
  "creates defaults from sql inputs"
  {:added "4.0"}
  [schema table-name]
  (var table := (k/get-key schema table-name))
  (return (k/obj-keepf table
                       (fn [m]
                         (return (and (k/get-key m "sql")
                                      (k/has-key? (k/get-key m "sql")
                                                  "default"))))
                       (fn [m]
                         (return (k/get-path m ["sql" "default"]))))))

(defn.xt create-all-keys
  "creates all keys"
  {:added "4.0"}
  ([schema table-name]
   (var ref-ks := (-/create-ref-keys  schema table-name))
   (var ref-id-ks := (-> (k/arr-map ref-ks (fn:> [k] [(k/cat k "_id") k]))
                         (k/obj-from-pairs)))
   (return {:data     (-/create-data-keys schema table-name)
            :ref      ref-ks
            :ref-id   ref-id-ks
            :rev      (-/create-rev-keys  schema table-name)
            :defaults (-/create-defaults schema table-name)
            :table    (-/create-table-entries schema table-name)})))

(defn.xt get-all-keys
  "get all keys"
  {:added "4.0"}
  ([schema table-name]
   (var cached  (-/get-cached-schema schema))
   (var table   (k/get-key cached table-name))
   (when (not table)
     (:= table (-/create-all-keys schema table-name))
     (k/set-key cached table-name table))
   (return table)))

(defn.xt data-keys
  "gets data keys"
  {:added "4.0"}
  [schema table-name]
  (return (k/get-path (-/get-all-keys schema table-name) ["data"])))

(defn.xt ref-keys
  "gets ref keys"
  {:added "4.0"}
  [schema table-name]
  (return (k/get-path (-/get-all-keys schema table-name) ["ref"])))

(defn.xt ref-id-keys
  "gets ref id keys"
  {:added "4.0"}
  [schema table-name]
  (return (k/get-path (-/get-all-keys schema table-name) ["ref_id"])))

(defn.xt rev-keys
  "gets rev keys"
  {:added "4.0"}
  [schema table-name]
  (return (k/get-path (-/get-all-keys schema table-name) ["rev"])))

(defn.xt table-defaults
  "gets the table defaults"
  {:added "4.0"}
  [schema table-name]
  (return (k/get-path (-/get-all-keys schema table-name) ["defaults"])))

(defn.xt table-entries
  "gets the table entries"
  {:added "4.0"}
  [schema table-name]
  (return (k/get-path (-/get-all-keys schema table-name) ["table"])))

(defn.xt table-columns
  "ges the table columns"
  {:added "4.0"}
  [schema table-name]
  (return (k/arr-map (-/table-entries schema table-name)
                     -/get-ident-id)))

(defn.xt create-table-order
  "creates the table order"
  {:added "4.0"}
  [lookup]
  (return (-> (k/arr-sort (k/obj-pairs lookup)
                          (fn [pair]
                            (return (k/get-key (k/second pair) "position")))
                          k/lt)
              (k/arr-map k/first))))

(defn.xt table-order
  "table order with caching"
  {:added "4.0"}
  [lookup]
  (var cached (k/lu-get -/CACHED_LOOKUP lookup))
  (when (not cached)
    (:= cached (-/create-table-order lookup))
    (k/lu-set -/CACHED_LOOKUP lookup cached))
  (return cached))

(defn.xt table-coerce
  "coerces output given schema and type functions"
  {:added "4.0"}
  [schema table data ctypes]
  
  (var out {})
  (var ref-fn (fn:> [ntable vdata] (-/table-coerce schema ntable vdata ctypes)))
  (when (k/arr? data)
    (return (k/arr-map data (fn:> [vdata] (ref-fn table vdata)))))
  (k/for:object [[key v] data]
    (var rec (k/get-in schema [table key]))
    (cond (k/nil? rec)
          (k/set-key out key v)

          (== "ref" (k/get-key rec "type"))
          (do (var ntable (k/get-path rec ["ref" "ns"]))
              (k/set-key out key (k/arr-map v (fn:> [vdata] (ref-fn ntable vdata)))))
          
          :else
          (do (var f (k/get-key ctypes (k/get-key rec "type")))
              (var val (:? (k/nil? f)
                           v
                           (f v)))
              #_(k/LOG! [table key (. rec ["type"]) v val])
              (k/set-key out key val))))
  (return out))

