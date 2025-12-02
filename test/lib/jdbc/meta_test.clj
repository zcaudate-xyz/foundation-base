(ns lib.jdbc.meta-test
  (:use code.test)
  (:require [lib.jdbc.meta :refer :all]
            [lib.jdbc.protocol :as protocol])
  (:import (java.sql Connection DatabaseMetaData)))

(defn mock-conn-meta []
  (reify DatabaseMetaData
    (getDatabaseProductName [_] "PostgreSQL")
    (getDatabaseMajorVersion [_] 10)
    (getDatabaseMinorVersion [_] 1)
    (getDatabaseProductVersion [_] "10.1")
    (getDriverName [_] "pgjdbc")
    (getDriverVersion [_] "9.4")))

(defn mock-conn-impl []
  (reify Connection
    (getCatalog [_] "catalog")
    (getSchema [_] "schema")
    (isReadOnly [_] true)
    (isValid [_ _] true)
    (getNetworkTimeout [_] 1000)
    (getTransactionIsolation [_] java.sql.Connection/TRANSACTION_READ_UNCOMMITTED)))

(defn mock-conn []
  (reify protocol/IConnection
    (-connection [_] (mock-conn-impl))
    protocol/IDatabaseMetadata
    (-get-database-metadata [_] (mock-conn-meta))))

^{:refer lib.jdbc.meta/vendor-name :added "4.0"}
(fact "Get connection vendor name."
  (vendor-name {:metadata (mock-conn-meta)}) => "PostgreSQL")

^{:refer lib.jdbc.meta/catalog-name :added "4.0"}
(fact "Given a connection, get a catalog name."
  (catalog-name (mock-conn)) => "catalog")

^{:refer lib.jdbc.meta/schema-name :added "4.0"}
(fact "Given a connection, get a schema name."
  (schema-name (mock-conn)) => "schema")

^{:refer lib.jdbc.meta/is-readonly? :added "4.0"}
(fact "Returns true if a current connection is in read-only model."
  (is-readonly? (mock-conn)) => true)

^{:refer lib.jdbc.meta/is-valid? :added "4.0"}
(fact "Given a connection, return true if connection has not ben closed it still valid."
  (is-valid? (mock-conn)) => true)

^{:refer lib.jdbc.meta/network-timeout :added "4.0"}
(fact "Given a connection, get network timeout."
  (network-timeout (mock-conn)) => 1000)

^{:refer lib.jdbc.meta/isolation-level :added "4.0"}
(fact "Given a connection, get a current isolation level."
  (isolation-level (mock-conn)) => :read-commited)

^{:refer lib.jdbc.meta/db-major-version :added "4.0"}
(fact "Given a connection, return a database major version number."
  (db-major-version (mock-conn)) => 10)

^{:refer lib.jdbc.meta/db-minor-version :added "4.0"}
(fact "Given a connection, return a database minor version number."
  (db-minor-version (mock-conn)) => 1)

^{:refer lib.jdbc.meta/db-product-name :added "4.0"}
(fact "Given a connection, return a database product name."
  (db-product-name (mock-conn)) => "PostgreSQL")

^{:refer lib.jdbc.meta/db-product-version :added "4.0"}
(fact "Given a connection, return a database product version."
  (db-product-version (mock-conn)) => "10.1")

^{:refer lib.jdbc.meta/driver-name :added "4.0"}
(fact "Given a connection, return a current driver name"
  (driver-name (mock-conn)) => "pgjdbc")

^{:refer lib.jdbc.meta/driver-version :added "4.0"}
(fact "Given a connection, return a current driver version"
  (driver-version (mock-conn)) => "9.4")
