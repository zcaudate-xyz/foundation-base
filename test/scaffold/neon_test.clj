(ns scaffold.neon-test
  (:require [code.test :refer [fact => throws]]
            [scaffold.neon :as neon]))

^{:refer scaffold.neon/environment-name}
(fact "selectors cannot target another cloud branch"
  [(neon/environment-name nil) (neon/environment-name :dev)] => ["min" "dev"]
  (neon/environment-name :production) => (throws))

^{:refer scaffold.neon/jdbc-config}
(fact "JDBC uses direct TLS credentials and lifecycle closures without changing global config"
  (with-redefs [neon/environment-config (fn [_] {"DATABASE_URL_UNPOOLED" "postgresql://test:p%40ss+word@ep.test:5432/neondb?sslmode=require"})]
    (let [config (neon/jdbc-config :dev)]
      [(select-keys config [:rt/id :host :port :user :pass :dbname :sslmode])
       (fn? (:startup config)) (:shutdown config)]))
  => [{:rt/id :test.neon-dev :host "ep.test" :port 5432 :user "test" :pass "p@ss+word" :dbname "neondb" :sslmode "require"}
      true nil])

^{:refer scaffold.neon/adapter-defaults}
(fact "adapter defaults contain only public endpoints"
  (with-redefs [neon/environment-config (fn [_] {"NEON_DATA_API_URL" "https://data.test/rest/v1"
                                                 "NEON_BRIDGE_URL" "http://localhost:18787"
                                                 "DATABASE_URL_UNPOOLED" "private"
                                                 "NEON_REALTIME_SECRET" "private"})]
    (neon/adapter-defaults :min))
  => {"data_url" "https://data.test/rest/v1" "bridge_url" "http://localhost:18787"})
