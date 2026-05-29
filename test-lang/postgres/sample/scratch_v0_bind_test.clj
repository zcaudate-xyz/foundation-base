(ns postgres.sample.scratch-v0-bind-test
  (:require [hara.lang :as l]
            [postgres.sample.scratch-v0.route-scratch :as route-scratch]
            [postgres.sample.scratch-v0.view-log :as view-log])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [postgres.sample.scratch-v0.route-scratch :as route-scratch]
             [postgres.sample.scratch-v0.view-log :as view-log]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer postgres.sample.scratch-v0.route-scratch/make-routes :added "4.1.4"}
(fact "generates routes from scratch-v0 postgres functions"
  (!.js
   (var routes (route-scratch/make-routes))
   [(xt/x:has-key? routes "api/scratch-v0/ping")
    (xt/x:has-key? routes "api/scratch-v0/log-append")])
  => [true true])

^{:refer postgres.sample.scratch-v0.view-log/make-views :added "4.1.4"}
(fact "generates views from scratch-v0 postgres view definitions"
  (!.js
   (var views (view-log/make-views))
   [(xtd/get-in views ["Log" "select" "all" "view" "tag"])
    (xtd/get-in views ["Log" "return" "default" "view" "tag"])])
  => ["all" "default"])
