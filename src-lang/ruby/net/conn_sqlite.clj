(ns ruby.net.conn-sqlite
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.conn-sql :as conn-sql]]})

;;
;; minimal sqlite client over the sqlite3 CLI (no sqlite gem available)
;; mirrors python.net.conn-sqlite semantics
;;

(defn.rb decode-json-scalar
  [value]
  (cond (and (xt/x:is-string? value)
             (or (. value (start_with? "["))
                 (. value (start_with? "{"))
                 (== value "true")
                 (== value "false")
                 (== value "null")))
        (return (xt/x:json-decode value))

        :else
        (return value)))

(defn.rb raw-query
  "runs a raw sqlite query through the sqlite3 cli and normalises the result shape"
  {:added "4.1"}
  [filename query]
  (require "open3")
  (require "json")
  (var res (. (:- "Open3") (capture3 "sqlite3" "-json" filename query)))
  (var out (. res [0]))
  (var err-out (. res [1]))
  (var status (. res [2]))
  (when (not (. status success?))
    (xt/x:err (xt/x:cat "sqlite3 query failed: " err-out)))
  (:= out (. out strip))
  (when (== out "")
    (return []))
  (var rows (xt/x:json-decode out))
  (cond (== 0 (xt/x:len rows))
        (return [])

        (and (== 1 (xt/x:len rows))
             (== 1 (xt/x:len (. (. rows [0]) values))))
        (return (-/decode-json-scalar
                 (. (. (. rows [0]) values) [0])))

        :else
        (return rows)))

(defn.rb client-connect
  [client opts]
  (var #{defaults} client)
  (require "tmpdir")
  (require "securerandom")
  (var config (or opts {}))
  (var filename (or (xt/x:get-key config "filename")
                    (xt/x:get-key defaults "filename")
                    ":memory:"))
  (when (== filename ":memory:")
    (:= filename (. (:- "File") (join (. (:- "Dir") (tmpdir))
                                      (xt/x:cat "xt_sqlite_"
                                                (. (:- "SecureRandom") (hex 8))
                                                ".db"))))
    (xt/x:set-key client "tempfile" true))
  (xt/x:set-key client "filename" filename)
  (return client))

(defn.rb client-disconnect
  [client]
  (var #{filename tempfile} client)
  (when (and tempfile
             (xt/x:not-nil? filename)
             (. (:- "File") (exist? filename)))
    (. (:- "File") (delete filename)))
  (return true))

(defn.rb client-query
  [client query]
  (var #{filename} client)
  (return (-/raw-query filename query)))

(defn.rb client-query-async
  [client query]
  (return (promise/x:promise-run (-/client-query client query))))

(defimpl.xt ^{:lang :ruby}
  RubySqliteClient
  [defaults filename tempfile]
  conn-sql/ISqlClient
  {conn-sql/connect      -/client-connect
   conn-sql/disconnect   -/client-disconnect
   conn-sql/query        -/client-query
   conn-sql/query-async  -/client-query-async})

(defn.rb create
  [defaults]
  (var client (-/RubySqliteClient (or defaults {}) nil nil))
  (xt/x:set-key client "::/override"
                {"connect" -/client-connect
                 "disconnect" -/client-disconnect
                 "query" -/client-query
                 "query_async" -/client-query-async})
  (return client))
