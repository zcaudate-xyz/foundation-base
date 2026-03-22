(ns lib.jdbc
  (:require [clojure.string :as str]
            [lib.jdbc.constants :as constants]
            [lib.jdbc.impl :as impl]
            [lib.jdbc.protocol :as protocol]
            [lib.jdbc.resultset :refer [result-set->lazyseq result-set->vector]]
            [lib.jdbc.types :as types])
  (:import (java.sql PreparedStatement ResultSet Connection)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main public api.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn connection
  "Creates a connection to a database. As parameter accepts:
 
   - dbspec map containing connection parameters
   - dbspec map containing a datasource (deprecated)
   - URI or string (interpreted as uri)
   - DataSource instance
 
   The dbspec map has this possible variants:
 
   Classic approach:
 
   - `:subprotocol` -> (required) string that represents a vendor name (ex: postgresql)
   - `:subname` -> (required) string that represents a database name (ex: test)
     (many others options that are pased directly as driver parameters)
 
   Pretty format:
 
   - `:vendor` -> (required) string that represents a vendor name (ex: postgresql)
   - `:name` -> (required) string that represents a database name (ex: test)
   - `:host` -> (optional) string that represents a database hostname (default: 127.0.0.1)
   - `:port` -> (optional) long number that represents a database port (default: driver default)
     (many others options that are pased directly as driver parameters)
 
   URI or String format: `vendor://user:password@host:post/dbname?param1=value`
 
   Additional options:
 
   - `:schema` -> string that represents a schema name (default: nil)
   - `:read-only` -> boolean for mark entire connection read only.
   - `:isolation-level` -> keyword that represents a isolation level (`:none`, `:read-committed`,
                         `:read-uncommitted`, `:repeatable-read`, `:serializable`)
 
   Opions can be passed as part of dbspec map, or as optional second argument.
   For more details, see documentation."
  {:added "4.0"}
  ([dbspec] (connection dbspec {}))
  ([dbspec options]
   (let [^Connection conn (protocol/-connection dbspec)
         options (merge (when (map? dbspec) dbspec) options)]

     ;; Set readonly flag if it found on the options map
     (some->> (:read-only options)
              (.setReadOnly conn))

     ;; Set the concrete isolation level if it found
     ;; on the options map
     (some->> (:isolation-level options)
              (get constants/isolation-levels)
              (.setTransactionIsolation conn))

     ;; Set the schema if it found on the options map
     (some->> (:schema options)
              (.setSchema conn))
     
     (types/->connection conn))))

(defn prepared-statement?
  "Check if specified object is prepared statement."
  {:added "4.0"}
  [obj]
  (instance? PreparedStatement obj))

(defn prepared-statement
  "Given a string or parametrized sql in sqlvec format
   return an instance of prepared statement."
  {:added "4.0"}
  ([conn sqlvec] (prepared-statement conn sqlvec {}))
  ([conn sqlvec options]
   (let [conn (protocol/-connection conn)]
     (protocol/-prepared-statement sqlvec conn options))))

(defn execute
  "Execute a query and return a number of rows affected.
 
       (with-open [conn (jdbc/connection dbspec)]
         (jdbc/execute conn \"create table foo (id integer);\"))
 
   This function also accepts sqlvec format."
  {:added "4.0"}
  ([conn q] (execute conn q {}))
  ([conn q opts]
   (let [rconn (protocol/-connection conn)]
     (protocol/-execute q rconn opts))))

(defn fetch
  "Fetch eagerly results executing a query.
 
   This function returns a vector of records (default) or
   rows (depending on specified opts). Resources are relased
   inmediatelly without specific explicit action for it.
 
   It accepts a sqlvec, plain sql or prepared statement
   as query parameter."
  {:added "4.0"}
  ([conn q] (fetch conn q {}))
  ([conn q opts]
   (let [rconn (protocol/-connection conn)]
     (protocol/-fetch q rconn opts))))

(defn fetch-one
  "Fetch eagerly one restult executing a query."
  {:added "4.0"}
  ([conn q] (fetch-one conn q {}))
  ([conn q opts]
   (first (fetch conn q opts))))

(defn fetch-lazy
  "Fetch lazily results executing a query.
 
       (with-open [cursor (jdbc/fetch-lazy conn sql)]
         (doseq [item (jdbc/cursor->lazyseq cursor)]
           (do-something-with item)))
 
   This function returns a cursor instead of result.
   You should explicitly close the cursor at the end of
   iteration for release resources."
  {:added "4.0"}
  ([conn q] (fetch-lazy conn q {}))
  ([conn q opts]
   (let [^Connection conn (protocol/-connection conn)
         ^PreparedStatement stmt (protocol/-prepared-statement q conn opts)]
     (types/->cursor stmt))))

(defn cursor->lazyseq
  "Transform a cursor in a lazyseq.
 
   The returned lazyseq will return values until a cursor
   is closed or all values are fetched."
  {:added "4.0"}
  ([cursor] (impl/cursor->lazyseq cursor {}))
  ([cursor opts] (impl/cursor->lazyseq cursor opts)))
