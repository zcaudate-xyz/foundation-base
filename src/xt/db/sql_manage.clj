(ns xt.db.sql-manage
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.base-schema :as base-schema]]})

(defn.xt table-create-column
  "column creation function"
  {:added "4.0"}
  [schema entry opts]
  (var #{strict types column-fn table-fn} opts)
  (var ident     (xt/x:get-key entry "ident"))
  (var itype     (xt/x:get-key entry "type"))
  (var iprimary  (xt/x:get-key entry "primary"))
  (var irequired (xt/x:get-key entry "required"))
  (var stype (:? (xt/x:has-key? types itype)
                 (xt/x:get-key types itype)
                 itype))
  (var default-fn
       (fn [ident]
          (return (xt/x:cat (column-fn ident)
                         " " (:? (== "ref" stype) "text" stype)
                         (:? (== true iprimary) " PRIMARY KEY" "")
                         (:? (and (== true irequired)
                                  (== true strict))
                             " NOT NULL"
                             "")))))
  
  (cond (and (== stype "ref")
             (xt/x:has-key? schema (xt/x:get-path entry ["ref" "ns"])))
        (do (var rtable (xt/x:get-path entry ["ref" "ns"]))
            (var rtype  (xtd/get-in schema [rtable "id" "type"]))
            (cond (not (xt/x:is-string? rtype))
                  (return (default-fn (xt/x:cat ident "_id")))

                  :else
                  (return (xt/x:cat (column-fn (xt/x:cat ident "_id"))
                                 " "
                                  (:? (xt/x:has-key? types rtype)
                                      (xt/x:get-key types rtype)
                                      rtype)
                                  " REFERENCES "
                                  (table-fn rtable)))))
        :else
        (return (default-fn ident))))

(defn.xt table-create
  "emits a table create string"
  {:added "4.0"}
  ([schema table-name opts]
   (var table-fn         (xt/x:get-key opts "table_fn" (fn [x] (return x))))
   (var columns (base-schema/table-entries schema table-name))
   (return (xt/x:cat "CREATE TABLE IF NOT EXISTS "
                  (table-fn table-name)
                  " (\n  " 
                  (xt/x:str-join ",\n  " (xt/x:arr-map columns
                                             (fn [e]
                                               (return (-/table-create-column schema e opts)))))
                  "\n);"))))

(defn.xt table-create-all
  "creates all tables from schema"
  {:added "4.0"}
  ([schema lookup opts]
   (var table-list (base-schema/table-order lookup))
   (return (xt/x:arr-map table-list
                      (fn [table-name]
                        (return (-/table-create schema table-name opts)))))))

(defn.xt table-drop
  "creates a table statement"
  {:added "4.0"}
  ([schema table-name opts]
   (var table-fn         (xt/x:get-key opts "table_fn" (fn [x] (return x))))
   (return (xt/x:cat "DROP TABLE IF EXISTS " (table-fn table-name) ";"))))

(defn.xt table-drop-all
  "drops all tables"
  {:added "4.0"}
  ([schema lookup opts]
   (var ks (xt/x:arr-reverse (base-schema/table-order lookup)))
   (return (xt/x:arr-map ks (fn [table-name]
                           (return (-/table-drop schema table-name opts)))))))
