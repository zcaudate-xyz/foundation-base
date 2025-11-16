(ns xt.db.sql-manage
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.base-schema :as base-schema]]})

(defn.xt table-create-column
  "column creation function"
  {:added "4.0"}
  [schema entry opts]
  (var #{strict types column-fn table-fn} opts)
  (var ident     (k/get-key entry "ident"))
  (var itype     (k/get-key entry "type"))
  (var iprimary  (k/get-key entry "primary"))
  (var irequired (k/get-key entry "required"))
  (var stype (or (k/get-key types itype)
                 itype))
  (var default-fn
       (fn [ident]
         (return (k/cat (column-fn ident)
                        " " (:? (== "ref" stype) "text" stype)
                        (:? iprimary " PRIMARY KEY" "")
                        (:? (and irequired strict) " NOT NULL" "")))))
  
  (cond (and (== stype "ref")
             (k/has-key? schema (k/get-path entry ["ref" "ns"])))
        (do (var rtable (k/get-path entry ["ref" "ns"]))
            (var rtype  (k/get-in schema [rtable "id" "type"]))
            (cond (not rtype)
                  (return (default-fn (k/cat ident "_id")))

                  :else
                  (return (k/cat (column-fn (k/cat ident "_id"))
                                 " "
                                 (or (k/get-key types rtype)
                                     rtype)
                                 " REFERENCES "
                                 (table-fn rtable)))))
        :else
        (return (default-fn ident))))

(defn.xt table-create
  "emits a table create string"
  {:added "4.0"}
  ([schema table-name opts]
   (var table-fn         (k/get-key opts "table_fn" k/identity))
   (var columns (base-schema/table-entries schema table-name))
   (return (k/cat "CREATE TABLE IF NOT EXISTS "
                  (table-fn table-name)
                  " (\n  " 
                  (k/join ",\n  " (k/arr-map columns
                                             (fn [e]
                                               (return (-/table-create-column schema e opts)))))
                  "\n);"))))

(defn.xt table-create-all
  "creates all tables from schema"
  {:added "4.0"}
  ([schema lookup opts]
   (var table-list (base-schema/table-order lookup))
   (return (k/arr-map table-list
                      (fn [table-name]
                        (return (-/table-create schema table-name opts)))))))

(defn.xt table-drop
  "creates a table statement"
  {:added "4.0"}
  ([schema table-name opts]
   (var table-fn         (k/get-key opts "table_fn" k/identity))
   (return (k/cat "DROP TABLE IF EXISTS " (table-fn table-name) ";"))))

(defn.xt table-drop-all
  "drops all tables"
  {:added "4.0"}
  ([schema lookup opts]
   (var ks (k/arr-reverse (base-schema/table-order lookup)))
   (return (k/arr-map ks (fn [table-name]
                           (return (-/table-drop schema table-name opts)))))))

