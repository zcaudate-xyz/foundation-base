(ns scaffold.neon-jdbc-test
  (:require [code.test :refer [fact fact:global =>]]
            [clojure.string :as str]
            [lib.postgres.impl.postgresql :as postgresql]
            [scaffold.neon :as neon]))

(fact:global {:skip (not (System/getenv "NEON_LIVE_EMAIL"))})

^{:refer lib.postgres.impl.postgresql/create-pool :id neon-tls-jdbc}
(fact "the Foundation JDBC driver connects to the selected Neon branch over TLS"
  (let [pool (postgresql/create-pool (neon/jdbc-config (or (System/getenv "NEON_ENV") "min")))]
    (try
      (with-open [connection (.getConnection pool)
                  statement (.prepareStatement connection "SELECT 1")
                  result (.executeQuery statement)]
        (.next result)
        ;; Neon terminates client TLS at its proxy, so pg_stat_ssl describes
        ;; the separate proxy-to-compute connection. The JDBC driver's require
        ;; mode makes a successful connection conditional on client TLS.
        [(.getInt result 1) (str/includes? (.getURL (.getMetaData connection)) "sslmode=require")])
      (finally (.close pool))))
  => [1 true])
