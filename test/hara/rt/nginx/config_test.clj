(ns hara.runtime.nginx.config-test
  (:require [hara.runtime.nginx.config :refer :all])
  (:use code.test))

^{:refer hara.runtime.nginx.config/create-resty-params :added "4.0"}
(fact "creates default resty params"
  (create-resty-params)
  => string?)

^{:refer hara.runtime.nginx.config/create-conf :added "4.0"}
(fact "cerates default conf"
  (create-conf {:port 80})
  => vector?)
