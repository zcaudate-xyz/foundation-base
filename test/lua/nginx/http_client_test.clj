(ns lua.nginx.http-client-test
  (:require [rt.nginx]
            [std.json :as json]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.common-lib :as k :include [:json]]
             [lua.nginx :as n]
             [lua.nginx.http-client :as http]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.http-client/new :added "4.0"}
(fact "creates a new lua client"

  (!.lua
   (local ngxhttp (require "resty.http"))
   (http/new))
  => {"sock" {}, "keepalive" true})
