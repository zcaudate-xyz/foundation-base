(ns xt.db.base-schema
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})

(def.xt CACHED_SCHEMA (xt/x:lu-create))

(def.xt CACHED_LOOKUP (xt/x:lu-create))

(def.xt ^{:arglists '([e])}
  get-order (fn:> [e] (xt/x:get-key e "order")))

(def.xt ^{:arglists '([e])}
  get-ident (fn:> [e] (xt/x:get-key e "ident")))

(defn.xt get-ident-id
  "gets the ident id for a schema entry"
  {:added "4.0"}
  [e]
  (return (:? (== "ref" (xt/x:get-key e "type"))
              (xt/x:cat (xt/x:get-key e "ident") "_id")
              (xt/x:get-key e "ident"))))

(defn.xt list-tables
  "list tables"
  {:added "4.0"}
  [schema]
  (return  (xtd/arr-sort (xt/x:obj-keys schema)
                         (fn [x] (return x))
                         xt/x:str-lt)))

(defn.xt get-cached-schema
  "get lookup"
  {:added "4.0"}
  [schema]
  (var cached := (xt/x:lu-get -/CACHED_SCHEMA schema))
  (when (xt/x:nil? cached)
    (:= cached {})
    (xt/x:lu-set -/CACHED_SCHEMA schema cached))
  (return cached))

(defn.xt create-data-keys
  "creates data keys"
  {:added "4.0"}
  ([schema table-name]
   (var table-def := (xt/x:get-key schema table-name))
   (return (-> (xt/x:obj-vals table-def)
                (xt/x:arr-filter (fn:> [e] (and (xt/x:is-number? (xt/x:get-key e "order"))
                                             (not= (xt/x:get-key e "type") "ref"))))
                (xtd/arr-sort    -/get-order xt/x:lt)
                (xt/x:arr-map    -/get-ident)))))

(defn.xt create-ref-keys
  "creates ref keys"
  {:added "4.0"}
  ([schema table-name]
   (var table-def := (xt/x:get-key schema table-name))
   (return (-> (xt/x:obj-vals table-def)
                (xt/x:arr-filter (fn:> [e] (and (xt/x:is-number? (xt/x:get-key e "order"))
                                             (== (xt/x:get-key e "type") "ref"))))
                (xtd/arr-sort    -/get-order xt/x:lt)
                (xt/x:arr-map    -/get-ident)))))

(defn.xt create-rev-keys
  "creates rev keys"
  {:added "4.0"}
  ([schema table-name]
   (var table-def := (xt/x:get-key schema table-name))
   (return (-> (xt/x:obj-vals table-def)
                (xt/x:arr-filter (fn:> [e] (not (xt/x:is-number?
                                              (xt/x:get-key e "order")))))
                (xt/x:arr-map    -/get-ident)))))

(defn.xt create-table-entries
  "creates the table keys"
  {:added "4.0"}
  [schema table-name]
  (var table-def := (xt/x:get-key schema table-name))
  (return (-> (xt/x:obj-vals table-def)
               (xt/x:arr-filter (fn [e]
                               (return (xt/x:is-number? (xt/x:get-key e "order")))))
               (xtd/arr-sort    -/get-order xt/x:lt))))

(defn.xt create-defaults
  "creates defaults from sql inputs"
  {:added "4.0"}
  [schema table-name]
  (var table-def := (xt/x:get-key schema table-name))
  (return (xtd/obj-keepf table-def
                         (fn:> [m]
                           (and (xt/x:is-object? (xt/x:get-key m "sql"))
                                (xt/x:has-key? (xt/x:get-key m "sql") "default")))
                         (fn [m]
                           (return (xt/x:get-path m ["sql" "default"]))))))

(defn.xt create-all-keys
  "creates all keys"
  {:added "4.0"}
  ([schema table-name]
   (var ref-ks := (-/create-ref-keys  schema table-name))
   (var ref-id-ks := (-> (xt/x:arr-map ref-ks (fn:> [k] [(xt/x:cat k "_id") k]))
                         (xt/x:obj-from-pairs)))
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
    (var table-keys (xt/x:get-key cached table-name))
   (when (xt/x:nil? table-keys)
       (:= table-keys (-/create-all-keys schema table-name))
       (xt/x:set-key cached table-name table-keys))
    (return table-keys)))

(defn.xt data-keys
  "gets data keys"
  {:added "4.0"}
  [schema table-name]
  (return (xt/x:get-path (-/get-all-keys schema table-name) ["data"])))

(defn.xt ref-keys
  "gets ref keys"
  {:added "4.0"}
  [schema table-name]
  (return (xt/x:get-path (-/get-all-keys schema table-name) ["ref"])))

(defn.xt ref-id-keys
  "gets ref id keys"
  {:added "4.0"}
  [schema table-name]
  (return (xt/x:get-path (-/get-all-keys schema table-name) ["ref_id"])))

(defn.xt rev-keys
  "gets rev keys"
  {:added "4.0"}
  [schema table-name]
  (return (xt/x:get-path (-/get-all-keys schema table-name) ["rev"])))

(defn.xt table-defaults
  "gets the table defaults"
  {:added "4.0"}
  [schema table-name]
  (return (xt/x:get-path (-/get-all-keys schema table-name) ["defaults"])))

(defn.xt table-entries
  "gets the table entries"
  {:added "4.0"}
  [schema table-name]
  (return (xt/x:get-path (-/get-all-keys schema table-name) ["table"])))

(defn.xt table-columns
  "ges the table columns"
  {:added "4.0"}
  [schema table-name]
  (return (xt/x:arr-map (-/table-entries schema table-name)
                        -/get-ident-id)))

(defn.xt create-table-order
  "creates the table order"
  {:added "4.0"}
  [lookup]
  (return (-> (xtd/arr-sort (xt/x:obj-pairs lookup)
                            (fn [pair]
                              (return (xt/x:get-key (xt/x:second pair) "position")))
                            xt/x:lt)
               (xt/x:arr-map xt/x:first))))

(defn.xt table-order
  "table order with caching"
  {:added "4.0"}
  [lookup]
  (var cached (xt/x:lu-get -/CACHED_LOOKUP lookup))
  (when (xt/x:nil? cached)
    (:= cached (-/create-table-order lookup))
    (xt/x:lu-set -/CACHED_LOOKUP lookup cached))
  (return cached))

(defn.xt table-coerce
  "coerces output given schema and type functions"
  {:added "4.0"}
  [schema table data ctypes]
  
  (var out {})
  (var ref-fn (fn:> [ntable vdata] (-/table-coerce schema ntable vdata ctypes)))
  (when (xt/x:is-array? data)
    (return (xt/x:arr-map data (fn:> [vdata] (ref-fn table vdata)))))
  (xt/for:object [[key v] data]
    (var rec (xtd/get-in schema [table key]))
    (cond (xt/x:nil? rec)
          (xt/x:set-key out key v)

          (== "ref" (xt/x:get-key rec "type"))
          (do (var ntable (xt/x:get-path rec ["ref" "ns"]))
              (xt/x:set-key out key (xt/x:arr-map v (fn:> [vdata] (ref-fn ntable vdata)))))
          
          :else
          (do (var f (xt/x:get-key ctypes (xt/x:get-key rec "type")))
              (var val (:? (xt/x:nil? f)
                           v
                           (f v)))
              #_(xt/x:LOG! [table key (. rec ["type"]) v val])
              (xt/x:set-key out key val))))
  (return out))
