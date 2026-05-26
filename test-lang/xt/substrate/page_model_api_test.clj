(ns xt.substrate.page-model-api-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.substrate.page-model :as page-model]
             [xt.substrate :as substrate]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-model/view-refresh.fn-api-example :added "4.1"}
(fact "demonstrates the canonical fn/api view shape"
  (notify/wait-on :js
    (var node
         (substrate/node-create
          {"id" "node-page-model-api"
           "meta" {"api_template"
                   {"target" "server"
                    "meta" {"headers" {"authorization" "Bearer token-a"}
                             "trace_id" "trace-1"}}}
           "spaces" {"server" {"state" {}}}
           "handlers"
           {"entry/get"
            {"fn"
             (fn [space args request _node]
               (return {"id" (xtd/get-in (xt/x:first args) ["id"])
                        "space_id" (. space ["id"])
                        "request_id" (. request ["id"])
                        "trace_id" (. request ["meta"] ["trace_id"])
                        "authorization" (. request ["meta"] ["headers"] ["authorization"])}))}}}))
    (page-model/model-put
     node
     "screen/main"
     "orders"
     {"state" {"selected_id" "task-42"}
      "views"
      {"detail"
       {"input" []
        "resolver"
        {"type" "fn/api"
         "action" "entry/get"
         "args_fn" (fn [ctx]
                     (return [{"id" (xtd/get-in ctx ["model" "state" "selected_id"])}]))
         "trigger.post" (fn [ctx result]
                         (return {"data" result
                                   "view_id" (. ctx ["view_id"])}))}}}})
    (-> (page-model/view-refresh node "screen/main" "orders" "detail")
        (promise/x:promise-then
         (fn [result]
           (repl/notify
            {"status" (. result ["status"])
             "error" (. result ["error"])
             "view_id" (. result ["value"] ["view_id"])
             "id" (. result ["value"] ["data"] ["id"])
             "space_id" (. result ["value"] ["data"] ["space_id"])
             "trace_id" (. result ["value"] ["data"] ["trace_id"])
             "authorization" (. result ["value"] ["data"] ["authorization"])
             "has_request_id" (xt/x:not-nil? (. result ["value"] ["data"] ["request_id"]))})))))
  => {"status" "ready"
      "error" nil
      "view_id" "detail"
      "id" "task-42"
      "space_id" "server"
      "trace_id" "trace-1"
      "authorization" "Bearer token-a"
      "has_request_id" true})

^{:refer xt.substrate.page-model/view-set-input.fn-api-example :added "4.1"}
(fact "fn/api views use view-set-input when args_fn should read from ctx.input"
  (notify/wait-on :js
    (var node
         (substrate/node-create
          {"id" "node-page-model-api-input"
           "meta" {"api_templates"
                   {"search" {"target" "server"
                              "meta" {"headers" {"x-client" "admin-ui"}}}}}
           "spaces" {"server" {"state" {}}}
           "handlers"
           {"entry/search"
            {"fn"
             (fn [_space args request _node]
               (return {"query" (xtd/get-in (xt/x:first args) ["query"])
                        "limit" (xtd/get-in (xt/x:first args) ["limit"])
                        "client" (. request ["meta"] ["headers"] ["x-client"])
                        "trace" (. request ["meta"] ["headers"] ["x-trace"])}))}}}))
    (page-model/model-put
     node
     "screen/main"
     "orders"
     {"views"
      {"search"
       {"resolver"
        {"type" "fn/api"
         "api_template" "search"
         "action" "entry/search"
         "meta" {"headers" {"x-trace" "trace-2"}}
         "args_fn" (fn [ctx]
                     (return [{"query" (xt/x:get-idx (. ctx ["input"]) 0)
                               "limit" (xt/x:get-idx (. ctx ["input"]) 1)}]))}}}})
    (page-model/view-set-input node "screen/main" "orders" "search" ["pending" 5])
    (-> (page-model/view-refresh node "screen/main" "orders" "search")
        (promise/x:promise-then
         (fn [result]
           (repl/notify result)))))
  => {"value" {"query" "pending"
               "limit" 5
               "client" "admin-ui"
               "trace" "trace-2"}
      "status" "ready"
      "error" nil})
