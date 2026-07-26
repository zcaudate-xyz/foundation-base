tahto/runtime/nginx/config_test.clj:1:(ns tahto.runtime.nginx.config-test
tahto/runtime/nginx/config_test.clj:2:  (:require [tahto.runtime.nginx.config :refer :all])
  (:use code.test))

tahto/runtime/nginx/config_test.clj:5:^{:refer tahto.runtime.nginx.config/create-resty-params :added "4.0"}
(fact "creates default resty params"
  (create-resty-params)
  => string?)

tahto/runtime/nginx/config_test.clj:10:^{:refer tahto.runtime.nginx.config/create-conf :added "4.0"}
(fact "cerates default conf"
  (create-conf {:port 80})
  => vector?)
