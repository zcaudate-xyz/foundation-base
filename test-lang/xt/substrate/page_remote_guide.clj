(ns xt.substrate.page-remote-guide-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-page :as base-page]
             [xt.substrate.page-remote :as page-remote]]})

(def.js Server
  (substrate/node-create
   {"id" "server"
    "spaces" {"user/alice"
              {"state" {}}}})) 

(defn.js setup-server-page
  "creates a simple demo page on the server"
  {:added "4.1"}
  [node]
  (return
   (base-page/add-group
    node
    "user/alice"
    "demo.simple"
    {"hello-demo" {"defaults" {"args" ["hello"]
                         "output" {}
                         "process" (fn [x] (return x))
                         "init" (fn [] (return nil))}
             "handler" (fn [ctx]
                         (var data (xtd/get-in ctx ["input" "data"]))
                         (return {"value" (xt/x:first data)}))
             "trigger" true
             "options" {}}})))



(!.js
  (-/setup-server-page -/Server)
  -/Server)
