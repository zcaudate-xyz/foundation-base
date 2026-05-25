(ns xt.substrate.page-model-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.substrate.page-model :as page-model]
             [xt.substrate :as substrate]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-model/resolver-fn :added "4.1"}
(fact "uses only the standard fn key for resolvers"
  (!.js
    (var local-fn (fn [_ctx] (return "local")))
    (var direct-fn (fn [_ctx] (return "direct")))
    [(xt/x:is-function?
      (page-model/resolver-fn {"fn_local" local-fn}))
     (xt/x:is-function?
      (page-model/resolver-fn {"fn" direct-fn
                               "fn_local" local-fn}))])
  => [false true])

^{:refer xt.substrate.page-model/model-put :added "4.1"}
(fact "stores page models on a substrate space"
  (!.js
    (var node (substrate/node-create {"id" "node-a"}))
    (page-model/model-put
     node
     "screen/main"
     "orders"
     {"state" {"selected_id" "task-1"}
      "views" {"detail" {"resolver" {"type" "fn/local"
                                     "fn" (fn [ctx]
                                            (return {"id" (xt/x:get-path ctx
                                                                         ["model" "state" "selected_id"])}))}}}})
    [(. (page-model/model-get node "screen/main" "orders") ["id"])
     (. (page-model/view-get node "screen/main" "orders" "detail") ["resolver"] ["type"])])
  => ["orders" "fn/local"])

^{:refer xt.substrate.page-model/view-refresh :added "4.1"}
(fact "applies trigger.pre and trigger.post around resolver execution"
  (!.js
    (var node (substrate/node-create {"id" "node-b"}))
    (page-model/model-put
     node
     "screen/main"
     "orders"
     {"state" {"selected_id" "task-1"}
      "views"
      {"detail"
       {"resolver"
        {"type" "fn/local"
         "trigger.pre" (fn [ctx]
                          (xtd/set-in ctx ["model" "state" "selected_id"] "task-2")
                          (return ctx))
         "fn" (fn [ctx]
                (return {"id" (xtd/get-in ctx ["model" "state" "selected_id"])}))
         "trigger.post" (fn [ctx result]
                          (return {"wrapped" result}))}}}})
    (page-model/view-refresh node "screen/main" "orders" "detail"))
  => {"value" {"wrapped" {"id" "task-2"}}
      "status" "ready"
      "error" nil})

^{:refer xt.substrate.page-model/view-refresh.api :added "4.1"}
(fact "runs an fn/api resolver through substrate request"
  (notify/wait-on :js
    (var node
         (substrate/node-create
          {"id" "node-c"
           "spaces" {"server" {"state" {}}}
           "handlers"
           {"entry/get"
            {"fn"
             (fn [space args request node]
               (return {"id" (xtd/get-in (xt/x:first args) ["id"])
                        "space_id" (. space ["id"])}))}}}))
    (page-model/model-put
     node
     "screen/main"
     "orders"
     {"state" {"selected_id" "task-3"}
      "views"
      {"detail"
       {"resolver"
        {"type" "fn/api"
         "target" "server"
         "action" "entry/get"
         "args_fn" (fn [ctx]
                     (return [{"id" (xtd/get-in ctx ["model" "state" "selected_id"])}]))}}}})
    (-> (page-model/view-refresh node "screen/main" "orders" "detail")
        (promise/x:promise-then
         (fn [result]
           (repl/notify result)))))
  => {"value" {"id" "task-3" "space_id" "server"}
      "status" "ready"
      "error" nil})
