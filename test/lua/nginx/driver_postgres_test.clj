(ns lua.nginx.driver-postgres-test
  (:require [lua.nginx.driver-postgres :refer :all])
  (:use code.test))

^{:refer lua.nginx.driver-postgres/default-env :added "4.0"}
(fact "gets the default env")

^{:refer lua.nginx.driver-postgres/default-env-set :added "4.0"}
(fact "sets the default env")

^{:refer lua.nginx.driver-postgres/db-error :added "4.0"}
(fact "gets the db error")

^{:refer lua.nginx.driver-postgres/raw-query :added "4.0"}
(fact "creates a raw-query")

^{:refer lua.nginx.driver-postgres/connect-constructor :added "4.0"}
(fact "connects to postgres")
