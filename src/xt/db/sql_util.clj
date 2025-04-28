(ns xt.db.sql-util
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]
   :export [MODULE]})

(l/script :lua
  {:require [[xt.lang.base-lib :as k]]
   :export [MODULE]})

(def.xt OPERATORS
  {:neq "!="
   :gt  ">"
   :gte ">="
   :lt  "<"
   :lte "<="
   :eq  "="
   :is-not-null "IS NOT NULL"
   :is-null "IS NULL"})

(def.xt INFIX
  {"||" true
   "+"  true
   "-"  true
   "*"  true
   "/"  true})

(def.xt PG
  {:map    "jsonb"
   :long   "bigint"
   :enum   "text"
   :image  "jsonb"
   :array  "jsonb"})

(def.xt SQLITE
  {:inet    "text"
   :uuid    "text"
   :citext  "text"
   :long    "integer"
   :jsonb   "text"
   :map     "text"
   :enum    "text"
   :array   "text"
   :image   "text"})

(defn.xt sqlite-json-values
  "select values from json"
  {:added "4.0"}
  [v]
  (return (k/cat "(SELECT value from json_each(" v "))")))

(defn.xt sqlite-json-keys
  "select keys from json
 
   (!.js
    (ut/sqlite-json-keys \"'{\\\"a\\\":1}'\"))
   => \"(SELECT key from json_each('{\\\"a\\\":1}'))\""
  {:added "4.0"}
  [v]
  (return (k/cat "(SELECT key from json_each(" v "))")))

(def.xt SQLITE_FN
  {"jsonb_build_object"        {:type "alias" :name "json_object"}
   "jsonb_build_array"         {:type "alias" :name "json_array"}
   "jsonb_array_elements_text" {:type "macro" :fn -/sqlite-json-values}
   "jsonb_array_elements"      {:type "macro" :fn -/sqlite-json-values}
   "jsonb_object_keys"         {:type "macro" :fn -/sqlite-json-keys}
   "\"core/util\".as_array"    {:type "macro" :fn k/identity}})

(defn.xt encode-bool
  "encodes a boolean to sql"
  {:added "4.0"}
  [b]
  (cond (== b true) (return "TRUE")
        (== b false) (return "FALSE")
        :else (k/err "Not Valid")))

(defn.lua encode-number
  "encodes a number (for lua dates)"
  {:added "4.0"}
  [v]
  (var '[rv fv] (math.modf v))
  (if (== fv 0)
    (return (k/cat "'" (string.format "%.f" v) "'"))
    (return (k/cat "'" (k/to-string v) "'"))))

(defn.xt encode-number
  "encodes a number (for lua dates)"
  {:added "4.0"}
  [v]
  (return (k/cat "'" (k/to-string v) "'")))

(defn.xt encode-operator
  "encodes an operator to sql"
  {:added "4.0"}
  [op opts]
  (return (or (k/get-key -/OPERATORS op)
              (k/get-in opts ["operators" op])
               op)))

(defn.xt encode-json
  "encodes a json value"
  {:added "4.0"}
  [v]
  (return (k/cat "'" (k/replace (k/json-encode v)
                                "'" "''")
                 "'")))

(defn.xt encode-value
  "encodes a value to sql"
  {:added "4.0"}
  [v]
  (cond (k/nil? v)
        (return "NULL")

        (k/is-string? v)
        (return (k/cat "'" (k/replace v "'" "''") "'"))
        
        (k/is-boolean? v)
        (return (-/encode-bool v))
        
        (or (k/arr? v)
            (k/obj? v))
        (return (-/encode-json v))

        (k/is-number? v)
        (return (-/encode-number v))
        
        :else
        (return (k/cat "'" (k/to-string v) "'"))))

(defn.xt encode-sql-arg
  "encodes an sql arg (for functions)"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{name} v)
  (return (-/encode-value name)))

(defn.xt encode-sql-column
  "encodes a sql column"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{name} v)
  (return (column-fn name)))

(defn.xt encode-sql-tuple
  "encodes a sql column"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{args} v)
  (var arg-fn (fn [arg]
                (return (loop-fn arg column-fn opts loop-fn))))
  (var fargs (k/arr-map (or args []) arg-fn))
  (return (k/arr-join fargs ", ")))

(defn.xt encode-sql-table
  "encodes an sql table"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{schema name} v)
  (if (k/get-key opts "strict")
    (return (k/cat (column-fn schema)
                   "."
                   (column-fn name)))
    (return (column-fn name))))

(defn.xt encode-sql-cast
  "encodes an sql cast"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var [out cast] (k/get-key v "args"))
  (if (k/get-key opts "strict")
    (return (k/cat (loop-fn out column-fn opts loop-fn)
                   "::"
                   (-/encode-sql-table cast column-fn opts loop-fn)))
    (return (loop-fn out column-fn opts loop-fn))))

(defn.xt encode-sql-keyword
  "encodes an sql keyword"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{name args} v)
  (var arg-fn (fn [arg]
                (return (loop-fn arg column-fn opts loop-fn))))
  (var fargs (k/arr-map (or args []) arg-fn))
  (return (k/arr-join [name (k/unpack fargs)] " ")))

(defn.xt encode-sql-fn
  "encodes an sql function"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{name args} v)
  (var arg-fn (fn [arg]
                (return (loop-fn arg column-fn opts loop-fn))))
  (var fargs (k/arr-map args arg-fn))
  (cond (k/has-key? -/INFIX name)
        (return (k/cat "(" (k/arr-join fargs (k/cat " " name " ")) ")"))
        
        :else
        (do (var lu (k/get-path opts ["values" "replace"]))
            (var fspec (k/get-key lu name))
            (cond (k/nil? fspec)
                  (return (k/cat  name "(" (k/arr-join fargs ", ") ")"))
                  
                  (== "alias" (k/get-key fspec "type"))
                  (return (k/cat (k/get-key fspec "name")
                                 "("
                                 (k/arr-join fargs ", ") ")"))

                  (== "macro" (k/get-key fspec "type"))
                  (return ((k/get-key fspec "fn") (k/unpack fargs)))

                  :else
                  (k/err (k/cat "Invalid Spec Type - " (k/get-key fspec "type")))))))

(defn.xt encode-sql-select
  "encodes an sql select statement"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{args} v)
  (var #{querystr-fn} opts)
  (var arg-fn (fn [arg]
                (cond (and (k/obj? arg)
                           (not (k/has-key? arg "::")))
                      (return (querystr-fn arg "" opts))

                      :else
                      (return (loop-fn arg column-fn opts loop-fn)))))
  (var fargs (k/arr-map args arg-fn))
  (return (k/cat "(SELECT " (k/arr-join fargs " ") ")")))

(def.xt ENCODE_SQL
  {"sql/arg"      -/encode-sql-arg
   "sql/column"   -/encode-sql-column
   "sql/tuple"    -/encode-sql-tuple
   "sql/defenum"  -/encode-sql-table
   "sql/deftype"  -/encode-sql-table
   "sql/cast"     -/encode-sql-cast
   "sql/fn"       -/encode-sql-fn
   "sql/keyword"  -/encode-sql-keyword
   "sql/select"   -/encode-sql-select})

(defn.xt encode-sql
  "encodes an sql value"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var tcls   (k/get-key v "::"))
  (var arg-fn (fn [arg]
                (return (loop-fn arg column-fn opts loop-fn))))
  (var f (k/get-key -/ENCODE_SQL tcls))
  (when (k/nil? f)
    (k/err (k/cat "Unsupported Type - " tcls)))
  (return (f v column-fn opts loop-fn)))

(defn.xt encode-loop-fn
  "loop function to encode"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (cond (and (k/obj? v)
             (k/has-key? v "::"))
        (return (-/encode-sql v column-fn opts loop-fn))

        (k/is-string? v)
        (return v)

        :else
        (return (-/encode-value v))))

(defn.xt encode-query-segment
  "encodes a query segment"
  {:added "4.0"}
  [key v column-fn opts]
  (var col (column-fn key))
  (var encode-fn
       (fn [v]
         (cond (and (k/obj? v)
                    (k/has-key? v "::"))
               (return (-/encode-loop-fn v column-fn opts -/encode-loop-fn))
               
               (k/arr? v)
               (cond (and (== 1 (k/len v))
                          (k/arr? (k/first v))
                          (k/arr-every (k/first v) k/is-string?))
                     (return (k/cat "(" (k/join ", " (k/arr-map (k/first v)
                                                                -/encode-value))
                                    ")"))
                     
                     ;; HACK FOR ENCODING IDS
                     (and (== 1 (k/len v))
                          (k/is-string? (k/first v)))
                     (return (k/first v))
                     
                     :else
                     (return (k/arr-join (k/arr-map v encode-fn)
                                         " ")))

               (or (== v "and")
                   (== v "or"))
               (return v)
               
               :else
               (return (-/encode-value v)))))
  (cond (k/arr? v)
        (return (k/cat col
                       " " (-/encode-operator (k/first v) opts)
                       " " (-> (k/arr-slice v 1 (k/len v))
                               (k/arr-map encode-fn)
                               (k/arr-join " "))))
        
        :else
        (return (k/cat col " = " (encode-fn v)))))

(defn.xt encode-query-single-string
  "helper for encode-query-string"
  {:added "4.0"}
  ([params opts]
   (var column-fn  (k/get-key opts "column_fn" k/identity))
   (var out := "")
   (k/for:object [[key v] params]
     (when (< 0 (k/len out))
       (:= out (k/cat out " AND ")))
     (:= out (k/cat out (-/encode-query-segment key v column-fn opts))))
   (return out)))

(defn.xt encode-query-string
  "encodes a query string"
  {:added "4.0"}
  ([params prefix opts]
   (var out (-> (k/arrayify params)
                (k/arr-map (fn:> [p] (-/encode-query-single-string p opts)))
                (k/arr-filter k/not-empty?)))
   (cond (== 0 (k/len out))
         (return "")

         (== 1 (k/len out))
         (return (k/cat prefix " " (k/first out)))

         :else
         (return (k/cat prefix " " (->  out
                                        (k/arr-map (fn:> [s] (k/cat "(" s ")")))
                                        (k/arr-join " OR ")))))))

(defn.xt LIMIT
  "creates a LIMIT keyword"
  {:added "4.0"}
  [val]
  (return {"::" "sql/keyword"
           :name "LIMIT"
           :args [{"::" "sql/keyword"
                   :name val}]}))

(defn.xt OFFSET
  "creates a OFFSET keyword"
  {:added "4.0"}
  [val]
  (return {"::" "sql/keyword"
           :name "OFFSET"
           :args [{"::" "sql/keyword"
                   :name val}]}))

(defn.xt ORDER-BY
  "creates an ORDER BY keyword"
  {:added "4.0"}
  [columns]
  (return {"::" "sql/keyword"
           :name "ORDER BY"
           :args [{"::" "sql/tuple"
                   :args (k/arr-map columns
                                    (fn:> [column]
                                      {"::" "sql/column"
                                       :name column}))}]}))

(defn.xt ORDER-SORT
  "creates an ORDER BY keyword"
  {:added "4.0"}
  [order]
  (return {"::" "sql/keyword"
           :name (k/to-uppercase order)}))

(defn.xt default-quote-fn
  "wraps a column in double quotes"
  {:added "4.0"}
  [s]
  (return (k/cat "\"" s "\"")))

(defn.xt default-return-format-fn
  "default return format-fn"
  {:added "4.0"}
  [input nest-fn column-fn opts]
  (cond (k/obj? input)
        (if (k/has-key? input "::")
          (return (-/encode-sql input column-fn opts -/encode-loop-fn))
          (return (k/cat (k/get-key input "expr")
                         (:? (k/has-key? input "as")
                             (k/cat " AS " (k/get-key input "as"))
                             ""))))
        
        (k/arr? input)
        (return (nest-fn input))

        (k/is-string? input)
        (return (column-fn input))

        :else
        (k/err (k/cat "Invalid input - " (k/to-string input)))))

(defn.xt default-table-fn
  "wraps a table in schema"
  {:added "4.0"}
  [table lookup]
  (return (k/cat "\"" (k/get-path lookup
                                  [table "schema"])
                 "\".\"" table "\"" )))

(defn.xt postgres-wrapper-fn
  "wraps a call for postgres"
  {:added "4.0"}
  [s indent]
  (return (k/cat "WITH j_ret AS (\n"
                 (k/pad-lines s 2 " ")
                 "\n) SELECT jsonb_agg(j_ret) FROM j_ret")))

(defn.xt postgres-opts
  "constructs postgres options"
  {:added "4.0"}
  [lookup]
  (return {:types -/PG
           :values {:cast true
                    :replace {}}
           :strict true
           :querystr-fn -/encode-query-string
           :wrapper-fn -/postgres-wrapper-fn
           :coerce     {}
           :column-fn  -/default-quote-fn
           :table-fn   (fn [table]
                         (return (-/default-table-fn table lookup)))
           :return-format-fn -/default-return-format-fn
           :return-count-fn  (fn []
                               (return (k/cat "count(*)")))
           :return-join-fn   (fn [arr] (return (k/join ", " arr)))
           :return-link-fn   (fn [s link-name]
                               (return (k/cat "(" s ") AS " link-name)))}))

(defn.xt sqlite-return-format-fn
  "sqlite return format function"
  {:added "4.0"}
  [input nest-fn column-fn]
  (cond (k/obj? input)
        (return (k/cat "'" (k/get-key input "as") "'"
                       ", " (k/get-key input "expr")))
        
        (k/arr? input)
        (return (nest-fn input))

        (k/is-string? input)
        (return (k/cat "'" input "'"
                       ", " (column-fn input)))
        
        :else
        (k/err (k/cat "Invalid input - " (k/to-string input)))))

(defn.xt sqlite-to-boolean
  "coerces 1 to true and 0 to false"
  {:added "4.0"}
  [v]
  (when (k/is-number? v)
    (return (== 1 v)))
  (return v))

(defn.xt sqlite-opts
  "constructs sqlite options"
  {:added "4.0"}
  [lookup]
  (return {:types -/SQLITE
           :values {:cast false
                    :replace -/SQLITE_FN}
           :strict false
           :querystr-fn -/encode-query-string
           :wrapper-fn (fn [s indent]
                         (return (:? (< indent 2) s (k/cat "(\n" (k/pad-lines s 2 " ") ")"))))
           :operators        {:ilike "LIKE"}
           :coerce           {:boolean -/sqlite-to-boolean
                              :jsonb   k/json-decode
                              :map     k/json-decode
                              :array   k/json-decode}
           :column-fn        -/default-quote-fn
           :table-fn         -/default-quote-fn
           :return-format-fn -/sqlite-return-format-fn
           :return-count-fn  (fn []
                               (return (k/cat "json_array(json_object('count',count(*)))")))
           :return-join-fn   (fn [arr]
                               (return (k/cat "json_group_array(json_object(" (k/join ", " arr) "))")))
           :return-link-fn   (fn [s link-name]
                               (return (k/cat "'" link-name "', " s)))}))

(def.xt MODULE (!:module))


(comment
  (./create-tests))
