(ns xt.db.sql-util
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})

(l/script :lua
  {:require [[xt.lang.common-spec :as xt]]})

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
  (return (xt/x:cat "(SELECT value from json_each(" v "))")))

(defn.xt sqlite-json-keys
  "select keys from json
 
   (!.js
    (ut/sqlite-json-keys \"'{\\\"a\\\":1}'\"))
   => \"(SELECT key from json_each('{\\\"a\\\":1}'))\""
  {:added "4.0"}
  [v]
  (return (xt/x:cat "(SELECT key from json_each(" v "))")))

(def.xt SQLITE_FN
  {"jsonb_build_object"        {:type "alias" :name "json_object"}
   "jsonb_build_array"         {:type "alias" :name "json_array"}
   "jsonb_array_elements_text" {:type "macro" :fn -/sqlite-json-values}
   "jsonb_array_elements"      {:type "macro" :fn -/sqlite-json-values}
   "jsonb_object_keys"         {:type "macro" :fn -/sqlite-json-keys}
   "\"core/util\".as_array"    {:type "macro" :fn (fn [x] (return x))}})

(defn.xt encode-bool
  "encodes a boolean to sql"
  {:added "4.0"}
  [b]
  (cond (== b true) (return "TRUE")
        (== b false) (return "FALSE")
        :else (xt/x:err "Not Valid")))

(defn.lua encode-number
  "encodes a number (for lua dates)"
  {:added "4.0"}
  [v]
  (var '[rv fv] (math.modf v))
  (if (== fv 0)
    (return (xt/x:cat "'" (string.format "%.f" v) "'"))
    (return (xt/x:cat "'" (xt/x:to-string v) "'"))))

(defn.xt encode-number
  "encodes a number (for lua dates)"
  {:added "4.0"}
  [v]
  (return (xt/x:cat "'" (xt/x:to-string v) "'")))

(defn.xt encode-operator
  "encodes an operator to sql"
  {:added "4.0"}
  [op opts]
  (return (or (xt/x:get-key -/OPERATORS op)
              (xtd/get-in opts ["operators" op])
               op)))

(defn.xt encode-json
  "encodes a json value"
  {:added "4.0"}
  [v]
  (return (xt/x:cat "'" (xt/x:replace (xt/x:json-encode v)
                                "'" "''")
                 "'")))

(defn.xt encode-value
  "encodes a value to sql"
  {:added "4.0"}
  [v]
  (cond (xt/x:nil? v)
        (return "NULL")

        (xt/x:is-string? v)
        (return (xt/x:cat "'" (xt/x:replace v "'" "''") "'"))
        
        (xt/x:is-boolean? v)
        (return (-/encode-bool v))
        
        (or (xt/x:is-array? v)
            (xt/x:is-object? v))
        (return (-/encode-json v))

        (xt/x:is-number? v)
        (return (-/encode-number v))
        
        :else
        (return (xt/x:cat "'" (xt/x:to-string v) "'"))))

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
  "encodes a sql tuple"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{args} v)
  (var arg-fn (fn [arg]
                (return (loop-fn arg column-fn opts loop-fn))))
  (var fargs (xt/x:arr-map (or args []) arg-fn))
  (return (xt/x:arr-join fargs ", ")))

(defn.xt encode-sql-table
  "encodes an sql table"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{schema name} v)
  (if (xt/x:get-key opts "strict")
    (return (xt/x:cat (column-fn schema)
                   "."
                   (column-fn name)))
    (return (column-fn name))))

(defn.xt encode-sql-cast
  "encodes an sql cast"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var [out cast] (xt/x:get-key v "args"))
  (if (xt/x:get-key opts "strict")
    (return (xt/x:cat (loop-fn out column-fn opts loop-fn)
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
  (var fargs (xt/x:arr-map (or args []) arg-fn))
  (return (xt/x:arr-join [name (xt/x:unpack fargs)] " ")))

(defn.xt encode-sql-fn
  "encodes an sql function"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{name args} v)
  (var arg-fn (fn [arg]
                (return (loop-fn arg column-fn opts loop-fn))))
  (var fargs (xt/x:arr-map args arg-fn))
  (cond (xt/x:has-key? -/INFIX name)
        (return (xt/x:cat "(" (xt/x:arr-join fargs (xt/x:cat " " name " ")) ")"))
        
        :else
        (do (var lu (xt/x:get-path opts ["values" "replace"]))
            (var fspec (xt/x:get-key lu name))
            (cond (xt/x:nil? fspec)
                  (return (xt/x:cat  name "(" (xt/x:arr-join fargs ", ") ")"))
                  
                  (== "alias" (xt/x:get-key fspec "type"))
                  (return (xt/x:cat (xt/x:get-key fspec "name")
                                 "("
                                 (xt/x:arr-join fargs ", ") ")"))

                  (== "macro" (xt/x:get-key fspec "type"))
                  (return ((xt/x:get-key fspec "fn") (xt/x:unpack fargs)))

                  :else
                  (xt/x:err (xt/x:cat "Invalid Spec Type - " (xt/x:get-key fspec "type")))))))

(defn.xt encode-sql-select
  "encodes an sql select statement"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (var #{args} v)
  (var #{querystr-fn} opts)
  (var arg-fn (fn [arg]
                (cond (and (xt/x:is-object? arg)
                           (not (xt/x:has-key? arg "::")))
                      (return (querystr-fn arg "" opts))

                      :else
                      (return (loop-fn arg column-fn opts loop-fn)))))
  (var fargs (xt/x:arr-map args arg-fn))
  (return (xt/x:cat "(SELECT " (xt/x:arr-join fargs " ") ")")))

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
  (var tcls   (xt/x:get-key v "::"))
  (var arg-fn (fn [arg]
                (return (loop-fn arg column-fn opts loop-fn))))
  (var f (xt/x:get-key -/ENCODE_SQL tcls))
  (when (xt/x:nil? f)
    (xt/x:err (xt/x:cat "Unsupported Type - " tcls)))
  (return (f v column-fn opts loop-fn)))

(defn.xt encode-loop-fn
  "loop function to encode"
  {:added "4.0"}
  [v column-fn opts loop-fn]
  (cond (and (xt/x:is-object? v)
             (xt/x:has-key? v "::"))
        (return (-/encode-sql v column-fn opts loop-fn))

        (xt/x:is-string? v)
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
         (cond (and (xt/x:is-object? v)
                    (xt/x:has-key? v "::"))
               (return (-/encode-loop-fn v column-fn opts -/encode-loop-fn))
               
               (xt/x:is-array? v)
               (cond (and (== 1 (xt/x:len v))
                          (xt/x:is-array? (xt/x:first v))
                          (xt/x:arr-every (xt/x:first v) k/is-string?))
                     (return (xt/x:cat "(" (xt/x:join ", " (xt/x:arr-map (xt/x:first v)
                                                                -/encode-value))
                                    ")"))
                     
                     ;; HACK FOR ENCODING IDS
                     (and (== 1 (xt/x:len v))
                          (xt/x:is-string? (xt/x:first v)))
                     (return (xt/x:first v))
                     
                     :else
                     (return (xt/x:arr-join (xt/x:arr-map v encode-fn)
                                         " ")))

               (or (== v "and")
                   (== v "or"))
               (return v)
               
               :else
               (return (-/encode-value v)))))
  (cond (xt/x:is-array? v)
        (return (xt/x:cat col
                       " " (-/encode-operator (xt/x:first v) opts)
                       " " (-> (xt/x:arr-slice v 1 (xt/x:len v))
                               (xt/x:arr-map encode-fn)
                               (xt/x:arr-join " "))))
        
        :else
        (return (xt/x:cat col " = " (encode-fn v)))))

(defn.xt encode-query-single-string
  "helper for encode-query-string"
  {:added "4.0"}
  ([params opts]
   (var column-fn  (xt/x:get-key opts "column_fn" (fn [x] (return x))))
   (var out := "")
   (xt/for:object [[key v] params]
     (when (< 0 (xt/x:len out))
       (:= out (xt/x:cat out " AND ")))
     (:= out (xt/x:cat out (-/encode-query-segment key v column-fn opts))))
   (return out)))

(defn.xt encode-query-string
  "encodes a query string"
  {:added "4.0"}
  ([params prefix opts]
   (var out (-> (xtd/arrayify params)
                (xt/x:arr-map (fn:> [p] (-/encode-query-single-string p opts)))
                (xt/x:arr-filter k/not-empty?)))
   (cond (== 0 (xt/x:len out))
         (return "")

         (== 1 (xt/x:len out))
         (return (xt/x:cat prefix " " (xt/x:first out)))

         :else
         (return (xt/x:cat prefix " " (->  out
                                        (xt/x:arr-map (fn:> [s] (xt/x:cat "(" s ")")))
                                        (xt/x:arr-join " OR ")))))))

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
                   :args (xt/x:arr-map columns
                                    (fn:> [column]
                                      {"::" "sql/column"
                                       :name column}))}]}))

(defn.xt ORDER-SORT
  "creates an ORDER BY keyword"
  {:added "4.0"}
  [order]
  (return {"::" "sql/keyword"
           :name (xt/x:to-uppercase order)}))

(defn.xt default-quote-fn
  "wraps a column in double quotes"
  {:added "4.0"}
  [s]
  (return (xt/x:cat "\"" s "\"")))

(defn.xt default-return-format-fn
  "default return format-fn"
  {:added "4.0"}
  [input nest-fn column-fn opts]
  (cond (xt/x:is-object? input)
        (if (xt/x:has-key? input "::")
          (return (-/encode-sql input column-fn opts -/encode-loop-fn))
          (return (xt/x:cat (xt/x:get-key input "expr")
                         (:? (xt/x:has-key? input "as")
                             (xt/x:cat " AS " (xt/x:get-key input "as"))
                             ""))))
        
        (xt/x:is-array? input)
        (return (nest-fn input))

        (xt/x:is-string? input)
        (return (column-fn input))

        :else
        (xt/x:err (xt/x:cat "Invalid input - " (xt/x:to-string input)))))

(defn.xt default-table-fn
  "wraps a table in schema"
  {:added "4.0"}
  [table lookup]
  (return (xt/x:cat "\"" (xt/x:get-path lookup
                                  [table "schema"])
                 "\".\"" table "\"" )))

(defn.xt postgres-wrapper-fn
  "wraps a call for postgres"
  {:added "4.0"}
  [s indent]
  (return (xt/x:cat "WITH j_ret AS (\n"
                 (xt/x:pad-lines s 2 " ")
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
                               (return (xt/x:cat "count(*)")))
           :return-join-fn   (fn [arr] (return (xt/x:join ", " arr)))
           :return-link-fn   (fn [s link-name]
                               (return (xt/x:cat "(" s ") AS " link-name)))}))

(defn.xt sqlite-return-format-fn
  "sqlite return format function"
  {:added "4.0"}
  [input nest-fn column-fn]
  (cond (xt/x:is-object? input)
        (return (xt/x:cat "'" (xt/x:get-key input "as") "'"
                       ", " (xt/x:get-key input "expr")))
        
        (xt/x:is-array? input)
        (return (nest-fn input))

        (xt/x:is-string? input)
        (return (xt/x:cat "'" input "'"
                       ", " (column-fn input)))
        
        :else
        (xt/x:err (xt/x:cat "Invalid input - " (xt/x:to-string input)))))

(defn.xt sqlite-to-boolean
  "coerces 1 to true and 0 to false"
  {:added "4.0"}
  [v]
  (when (xt/x:is-number? v)
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
                         (return (:? (< indent 2) s (xt/x:cat "(\n" (xt/x:pad-lines s 2 " ") ")"))))
           :operators        {:ilike "LIKE"}
           :coerce           {:boolean -/sqlite-to-boolean
                              :jsonb   k/json-decode
                              :map     k/json-decode
                              :array   k/json-decode}
           :column-fn        -/default-quote-fn
           :table-fn         -/default-quote-fn
           :return-format-fn -/sqlite-return-format-fn
           :return-count-fn  (fn []
                               (return (xt/x:cat "json_array(json_object('count',count(*)))")))
           :return-join-fn   (fn [arr]
                               (return (xt/x:cat "json_group_array(json_object(" (xt/x:join ", " arr) "))")))
           :return-link-fn   (fn [s link-name]
                               (return (xt/x:cat "'" link-name "', " s)))}))

(comment
  (./create-tests))
