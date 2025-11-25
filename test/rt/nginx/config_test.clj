(ns rt.nginx.config-test
  (:use code.test)
  (:require [rt.nginx.config :refer :all]
            [std.lib :as h]))

^{:refer rt.nginx.config/create-resty-params :added "4.0"}
(fact "creates default resty params"
  (create-resty-params)
  => string?)

^{:refer rt.nginx.config/create-conf :added "4.0"}
(fact "cerates default conf"
  (create-conf {:port 80})
  => vector?)
