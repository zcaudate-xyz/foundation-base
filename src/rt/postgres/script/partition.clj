(ns rt.postgres.script.partition
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang :as l]
            [rt.postgres.grammar.common-application :as app]))

(defn get-app-schema []
  (try
    (let [module  (l/rt:module :postgres)]
      (if module
        (:schema (app/app (first (:application (:static module)))))
        "public"))
    (catch Throwable _ "public")))

(defn fmt-val [v]
  (str "'" v "'"))

(defn fmt-col [c]
  (str/snake-case (name c)))

(defn table-name [root schema path]
  (let [root-name (name root)
        parts (cons root-name path) ;; Rev, ChatChannel, user -> Rev_ChatChannel_user
        full (str/join "_" parts)]
    (if schema
      (str "\"" schema "\".\"" full "\"")
      (str "\"" full "\""))))

(defn generate-level [root schema levels path parent-table ctr]
  (let [curr (first levels)
        next-lvl (second levels)
        rem-lvls (rest levels)
        vals (:in curr)
        next-col (:use next-lvl)]

    (str/join "\n\n"
      (for [v vals]
        (let [curr-path (cons v path)
              tname (table-name root schema curr-path)
              is-leaf (nil? next-lvl)
              idx @ctr
              _ (swap! ctr inc)

              comment (str "-- " idx ". "
                           (if is-leaf
                             (str "Create Storage for " v)
                             (str "Create Router Tables for '" v "' (Partitioned by " (if next-col (fmt-col next-col) "List") ")")))

              create-sql (str "CREATE TABLE IF NOT EXISTS " tname " \n"
                              "    PARTITION OF " parent-table " \n"
                              "    FOR VALUES IN (" (fmt-val v) ")"
                              (if is-leaf
                                ";"
                                (str " \n    PARTITION BY LIST (\"" (if next-col (fmt-col next-col)) "\");")))

              children-sql (if next-lvl
                             (generate-level root schema rem-lvls curr-path tname ctr)
                             nil)]
          (if children-sql
            (str comment "\n" create-sql "\n\n" children-sql)
            (str comment "\n" create-sql)))))))

(defn partition-fn [sym root levels]
  (let [schema (get-app-schema) ;; "szn_type" in user example
        root-sym (if (vector? root)
                   (let [s (first root)]
                     (if (and (symbol? s) (str/starts-with? (name s) "-/"))
                       (symbol (subs (name s) 2))
                       s))
                   root)
        root-table (if schema
                     (str "\"" schema "\".\"" (name root-sym) "\"")
                     (str "\"" (name root-sym) "\""))

        curr (first levels)
        next-lvl (second levels)
        rem-lvls (rest levels)
        vals (:in curr)
        next-col (:use next-lvl)]

    (str/join "\n\n"
      (for [v vals]
        (let [ctr (atom 1)
              path (list v)
              tname (table-name root-sym schema path)
              is-leaf (nil? next-lvl)

              comment (str "-- 1. "
                           (if is-leaf
                             (str "Create Storage for " v)
                             (str "Create Router Tables for '" v "' (Partitioned by " (if next-col (fmt-col next-col) "List") ")")))

              create-sql (str "CREATE TABLE IF NOT EXISTS " tname " \n"
                              "    PARTITION OF " root-table " \n"
                              "    FOR VALUES IN (" (fmt-val v) ")"
                              (if is-leaf
                                ";"
                                (str " \n    PARTITION BY LIST (\"" (if next-col (fmt-col next-col)) "\");")))

              children-sql (if next-lvl
                             (generate-level root-sym schema rem-lvls path tname (atom 2))
                             nil)]
          (if children-sql
            (str comment "\n" create-sql "\n\n" children-sql)
            (str comment "\n" create-sql)))))))

(defmacro defpartitions.pg [sym root levels]
  (let [sql (partition-fn sym root levels)]
    `(def ~sym ~sql)))
