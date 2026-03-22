(ns lib.jdbc.types
  (:require [lib.jdbc.protocol :as protocol]
            [lib.jdbc.resultset :refer [result-set->lazyseq]])
  (:import java.sql.Connection java.sql.ResultSet java.sql.PreparedStatement))

(defn ->connection
  "Create a connection wrapper.
 
   The connection  wrapper is need because it
   implemens IMeta interface that is mandatory
   for transaction management."
  {:added "4.0"}
  [^Connection conn]
  (reify
    protocol/IConnection
    (-connection [_] conn)

    protocol/IDatabaseMetadata
    (-get-database-metadata [_]
      (.getMetaData conn))

    java.io.Closeable
    (close [_]
      (.close conn))))

(deftype Cursor [^PreparedStatement stmt]
  protocol/IConnection
  (-connection [_] (.getConnection stmt))

  java.io.Closeable
  (close [_]
    (.close stmt)))

(defn ->cursor
  "creates a cursor from prepared statement"
  {:added "4.0"}
  [^PreparedStatement stmt]
  (Cursor. stmt))
