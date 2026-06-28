(ns lua.nginx.http-client-test
  (:require [hara.runtime.nginx]
            [std.json :as json]
            [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :test-mode true
   :config  {:program :resty}
   :require [[xt.lang.spec-base :as xt :include [:json]]
             [lua.nginx :as n]
             [lua.nginx.http-client :as http]]})

(fact:global
 {:skip     (not (env/program-exists? "resty"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.http-client/new :added "4.0"}
(fact "creates a new lua client"

  (!.lua
   (local ngxhttp (require "resty.http"))
   (http/new))
  => {"sock" {}, "keepalive" true})
