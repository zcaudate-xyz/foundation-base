(ns lib.jdbc.meta
  (:require [lib.jdbc.protocol :as protocol]
            [lib.jdbc.types :as types])
  "Connection metadata access methods.")

(defn vendor-name
  "Get connection vendor name."
  {:added "4.0"}
  [c]
  (let [^java.sql.DatabaseMetaData meta (:metadata c)]
    (.getDatabaseProductName meta)))

(defn catalog-name
  "Given a connection, get a catalog name."
  {:added "4.0"}
  [c]
  (let [^java.sql.Connection conn (protocol/-connection c)]
    (.getCatalog conn)))

(defn schema-name
  "Given a connection, get a schema name."
  {:added "4.0"}
  [c]
  (let [^java.sql.Connection conn (protocol/-connection c)]
    (.getSchema conn)))

(defn is-readonly?
  "Returns true if a current connection is in read-only model."
  {:added "4.0"}
  [c]
  (let [^java.sql.Connection conn (protocol/-connection c)]
    (.isReadOnly conn)))

(defn is-valid?
  "Given a connection, return true if connection has not ben closed it still valid."
  {:added "4.0"}
  ([c]
     (is-valid? c 0))
  ([c ^long timeout]
     (let [^java.sql.Connection conn (protocol/-connection c)]
       (.isValid conn timeout))))

(defn network-timeout
  "Given a connection, get network timeout."
  {:added "4.0"}
  [c]
  (let [^java.sql.Connection conn (protocol/-connection c)]
    (.getNetworkTimeout conn)))

(defn isolation-level
  "Given a connection, get a current isolation level."
  {:added "4.0"}
  [c]
  (let [^java.sql.Connection conn (protocol/-connection c)
        ilvalue (.getTransactionIsolation conn)]
    (condp = ilvalue
      java.sql.Connection/TRANSACTION_READ_UNCOMMITTED :read-commited
      java.sql.Connection/TRANSACTION_REPEATABLE_READ  :repeatable-read
      java.sql.Connection/TRANSACTION_SERIALIZABLE     :serializable
      :none)))

(defn db-major-version
  "Given a connection, return a database major version number."
  {:added "4.0"}
  [c]
  (let [^java.sql.DatabaseMetaData meta (protocol/-get-database-metadata c)]
    (.getDatabaseMajorVersion meta)))

(defn db-minor-version
  "Given a connection, return a database minor version number."
  {:added "4.0"}
  [c]
  (let [^java.sql.DatabaseMetaData meta (protocol/-get-database-metadata c)]
    (.getDatabaseMinorVersion meta)))

(defn db-product-name
  "Given a connection, return a database product name."
  {:added "4.0"}
  [c]
  (let [^java.sql.DatabaseMetaData meta (protocol/-get-database-metadata c)]
    (.getDatabaseProductName meta)))

(defn db-product-version
  "Given a connection, return a database product version."
  {:added "4.0"}
  [c]
  (let [^java.sql.DatabaseMetaData meta (protocol/-get-database-metadata c)]
    (.getDatabaseProductVersion meta)))

(defn driver-name
  "Given a connection, return a current driver name"
  {:added "4.0"}
  [c]
  (let [^java.sql.DatabaseMetaData meta (protocol/-get-database-metadata c)]
    (.getDriverName meta)))

(defn driver-version
  "Given a connection, return a current driver version"
  {:added "4.0"}
  [c]
  (let [^java.sql.DatabaseMetaData meta (protocol/-get-database-metadata c)]
    (.getDriverVersion meta)))
