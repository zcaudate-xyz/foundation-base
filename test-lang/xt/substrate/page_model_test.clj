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

^{:refer xt.substrate.page-model/node-api-templates :added "4.1"}
(fact "gets named api templates from node meta"
  (!.js
    (page-model/node-api-templates
     (substrate/node-create
      {"id" "node-templates"
       "meta" {"api_templates"
               {"admin" {"target" "server"}
                "search" {"target" "search"}}}})))
  => {"admin" {"target" "server"}
      "search" {"target" "search"}})

^{:refer xt.substrate.page-model/default-api-template :added "4.1"}
(fact "gets the default api template from node meta"
  (!.js
    [(page-model/default-api-template
      (substrate/node-create
       {"id" "node-default-api-template"
        "meta" {"api_template" {"target" "server"
                                 "meta" {"headers" {"authorization" "Bearer token-a"}}}}}))
     (page-model/default-api-template
      (substrate/node-create
       {"id" "node-default-api-template-named"
        "meta" {"api_template" "admin"
                 "api_templates" {"admin" {"target" "server"}}}}))])
  => [{"target" "server"
       "meta" {"headers" {"authorization" "Bearer token-a"}}}
      {"target" "server"}])

^{:refer xt.substrate.page-model/merge-api-template :added "4.1"}
(fact "merges template defaults into an fn/api resolver with resolver overrides"
  (!.js
    (page-model/merge-api-template
     {"type" "fn/api"
      "api_template" "admin"
      "action" "entry/get"
      "meta" {"headers" {"x-trace" "trace-1"}}}
     {"node"
      (substrate/node-create
       {"id" "node-merge-api-template"
        "meta" {"api_templates"
                {"admin" {"target" "server"
                          "meta" {"headers" {"authorization" "Bearer token-a"
                                              "x-client" "admin-ui"}}}}}})}))
  => {"type" "fn/api"
      "target" "server"
      "action" "entry/get"
      "meta" {"headers" {"authorization" "Bearer token-a"
                         "x-client" "admin-ui"
                         "x-trace" "trace-1"}}})

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
(fact "runs an fn/api resolver through the default api template"
  (notify/wait-on :js
    (var node
         (substrate/node-create
          {"id" "node-c"
           "meta" {"api_template" {"target" "server"
                                  "meta" {"trace_id" "trace-1"}}}
           "spaces" {"server" {"state" {}}}
           "handlers"
           {"entry/get"
            {"fn"
             (fn [space args request node]
               (return {"id" (xtd/get-in (xt/x:first args) ["id"])
                        "space_id" (. space ["id"])
                        "trace_id" (. request ["meta"] ["trace_id"])}))}}}))
    (page-model/model-put
     node
     "screen/main"
     "orders"
     {"state" {"selected_id" "task-3"}
      "views"
      {"detail"
       {"resolver"
        {"type" "fn/api"
         "action" "entry/get"
         "args_fn" (fn [ctx]
                     (return [{"id" (xtd/get-in ctx ["model" "state" "selected_id"])}]))}}}})
    (-> (page-model/view-refresh node "screen/main" "orders" "detail")
       (promise/x:promise-then
        (fn [result]
           (repl/notify result)))))
  => {"value" {"id" "task-3" "space_id" "server" "trace_id" "trace-1"}
      "status" "ready"
      "error" nil})


^{:refer xt.substrate.page-model/ensure-space-state :added "4.1"}
(fact "creates page state on demand for a node space"
  (!.js
    (var node (substrate/node-create {"id" "node-space"}))
    (var state (page-model/ensure-space-state node "screen/main"))
    [(. state ["::"])
     (xt/x:not-nil? (. (. node ["spaces"]) ["screen/main"] ["state"]))])
  => ["substrate.page.state" true])

^{:refer xt.substrate.page-model/view-put :added "4.1"}
(fact "registers a single view on an existing page model"
  (!.js
    (var node (substrate/node-create {"id" "node-view-put"}))
    (page-model/model-put node
                          "screen/main"
                          "orders"
                          {"views" {"list" {"resolver" {"type" "fn/local"
                                                        "fn" (fn [_ctx] (return ["list"]))}}}})
    (page-model/view-put node
                         "screen/main"
                         "orders"
                         "detail"
                         {"resolver" {"type" "fn/local"
                                      "fn" (fn [_ctx] (return ["detail"]))}})
    [(xt/x:obj-keys (. (page-model/model-get node "screen/main" "orders") ["views"]))
     (. (page-model/view-get node "screen/main" "orders" "detail") ["id"])])
  => [["list" "detail"] "detail"])

^{:refer xt.substrate.page-model/model-get :added "4.1"}
(fact "gets a model from a node space"
  (!.js
    (var node (substrate/node-create {"id" "node-model-get"}))
    (page-model/model-put node
                          "screen/main"
                          "orders"
                          {"state" {"selected_id" "task-1"}
                           "views" {"detail" {"resolver" {"type" "fn/local"
                                                          "fn" (fn [_ctx] (return {"id" "task-1"}))}}}})
    [(. (page-model/model-get node "screen/main" "orders") ["id"])
     (xtd/get-in (. (page-model/model-get node "screen/main" "orders") ["state"])
                  ["selected_id"])])
  => ["orders" "task-1"])

^{:refer xt.substrate.page-model/view-get :added "4.1"}
(fact "gets a view from a node space"
  (!.js
    (var node (substrate/node-create {"id" "node-view-get"}))
    (page-model/model-put node
                          "screen/main"
                          "orders"
                          {"views" {"detail" {"resolver" {"type" "fn/local"
                                                          "fn" (fn [_ctx] (return {"id" "task-1"}))}}}})
    (. (page-model/view-get node "screen/main" "orders" "detail") ["resolver"] ["type"]))
  => "fn/local")

^{:refer xt.substrate.page-model/model-set-state :added "4.1"}
(fact "sets nested local state on a model"
  (!.js
    (var node (substrate/node-create {"id" "node-model-set-state"}))
    (page-model/model-put node
                          "screen/main"
                          "orders"
                          {"state" {"selected_id" nil}
                           "views" {"detail" {"resolver" {"type" "fn/local"
                                                          "fn" (fn [ctx]
                                                                 (return {"id" (xtd/get-in ctx ["model" "state" "selected_id"])}))}}}})
    (page-model/model-set-state node "screen/main" "orders" ["selected_id"] "task-9")
    (xtd/get-in (. (page-model/model-get node "screen/main" "orders") ["state"])
                ["selected_id"]))
  => "task-9")

^{:refer xt.substrate.page-model/view-set-input :added "4.1"}
(fact "sets view input on the page model state"
  (!.js
    (var node (substrate/node-create {"id" "node-view-set-input"}))
    (page-model/model-put node
                          "screen/main"
                          "orders"
                          {"views" {"detail" {"resolver" {"type" "fn/local"
                                                          "fn" (fn [ctx]
                                                                 (return {"id" (xt/x:first (. ctx ["input"]))}))}}}})
    (page-model/view-set-input node "screen/main" "orders" "detail" ["task-7"])
    (. (page-model/view-get node "screen/main" "orders" "detail") ["input"]))
  => ["task-7"])

^{:refer xt.substrate.page-model/view-refresh-result :added "4.1"}
(fact "returns the public refresh result shape"
  (!.js
    (page-model/view-refresh-result {"value" {"id" "task-1"}
                                     "status" "ready"
                                     "error" nil}))
  => {"value" {"id" "task-1"}
      "status" "ready"
      "error" nil})

^{:refer xt.substrate.page-model/resolver-pre-trigger :added "4.1"}
(fact "gets the resolver pre trigger"
  (!.js
    (var trigger-fn (fn [ctx] (return ctx)))
    [(xt/x:is-function? (page-model/resolver-pre-trigger {"trigger.pre" trigger-fn}))
     (page-model/resolver-pre-trigger {})])
  => [true nil])

^{:refer xt.substrate.page-model/resolver-post-trigger :added "4.1"}
(fact "gets the resolver post trigger"
  (!.js
    (var trigger-fn (fn [ctx result] (return result)))
    [(xt/x:is-function? (page-model/resolver-post-trigger {"trigger.post" trigger-fn}))
     (page-model/resolver-post-trigger {})])
  => [true nil])

^{:refer xt.substrate.page-model/apply-pre-trigger :added "4.1"}
(fact "applies a pre trigger and falls back to the original context"
  (!.js
    (var mutate (fn [ctx]
                  (xtd/set-in ctx ["model" "state" "selected_id"] "task-2")
                  (return ctx)))
    (var noop (fn [_ctx] (return nil)))
    [(xtd/get-in
      (page-model/apply-pre-trigger {"trigger.pre" mutate}
                                    {"model" {"state" {"selected_id" "task-1"}}})
      ["model" "state" "selected_id"])
     (xtd/get-in
      (page-model/apply-pre-trigger {"trigger.pre" noop}
                                    {"model" {"state" {"selected_id" "task-1"}}})
      ["model" "state" "selected_id"])])
  => ["task-2" "task-1"])

^{:refer xt.substrate.page-model/apply-post-trigger :added "4.1"}
(fact "applies a post trigger and falls back to the original result"
  (!.js
    (var wrap (fn [_ctx result] (return {"wrapped" result})))
    (var noop (fn [_ctx _result] (return nil)))
    [(page-model/apply-post-trigger {"trigger.post" wrap} {} {"id" "task-1"})
     (page-model/apply-post-trigger {"trigger.post" noop} {} {"id" "task-1"})])
  => [{"wrapped" {"id" "task-1"}}
      {"id" "task-1"}])

^{:refer xt.substrate.page-model/finalize-result :added "4.1"}
(fact "stores either a ready value or an error on the view state"
  (!.js
    (var node-ok (substrate/node-create {"id" "node-finalize-ok"}))
    (page-model/model-put node-ok
                          "screen/main"
                          "orders"
                          {"views" {"detail" {"resolver" {"type" "fn/local"
                                                          "fn" (fn [_ctx] (return {"id" "task-1"}))}}}})
    (var state-ok (. (. node-ok ["spaces"]) ["screen/main"] ["state"]))
    (var ok-view (page-model/finalize-result state-ok
                                             "orders"
                                             "detail"
                                             {"trigger.post" (fn [_ctx result] (return {"wrapped" result}))}
                                             {}
                                             {"id" "task-1"}))
    (var node-err (substrate/node-create {"id" "node-finalize-err"}))
    (page-model/model-put node-err
                          "screen/main"
                          "orders"
                          {"views" {"detail" {"resolver" {"type" "fn/local"
                                                          "fn" (fn [_ctx] (return {"id" "task-1"}))}}}})
    (var state-err (. (. node-err ["spaces"]) ["screen/main"] ["state"]))
    (var err-view (page-model/finalize-result state-err
                                              "orders"
                                              "detail"
                                              {}
                                              {}
                                              {"status" "error"
                                               "tag" "bad"}))
    [{"status" (. ok-view ["status"])
      "value" (. ok-view ["value"])}
     {"status" (. err-view ["status"])
      "error" (. err-view ["error"] ["tag"])}])
  => [{"status" "ready"
       "value" {"wrapped" {"id" "task-1"}}}
      {"status" "error"
       "error" "bad"}])

^{:refer xt.substrate.page-model/run-api-resolver :added "4.1"}
(fact "merges a named api template before running an api resolver"
  (notify/wait-on :js
    (var node
         (substrate/node-create
          {"id" "node-api-run"
          "meta" {"api_templates"
                  {"admin" {"target" "server"
                            "meta" {"headers" {"authorization" "Bearer token-a"
                                                "x-client" "admin-ui"}}}}}
          "spaces" {"server" {"state" {}}}
          "handlers"
          {"entry/get"
           {"fn"
            (fn [space args request _node]
               (return {"id" (xtd/get-in (xt/x:first args) ["id"])
                        "space_id" (. space ["id"])
                        "auth" (. request ["meta"] ["headers"] ["authorization"])
                        "client" (. request ["meta"] ["headers"] ["x-client"])
                        "trace" (. request ["meta"] ["headers"] ["x-trace"]) }))}}}))
    (-> (page-model/run-api-resolver
         {"type" "fn/api"
          "api_template" "admin"
          "action" "entry/get"
          "meta" {"headers" {"x-trace" "trace-5"}}
          "args_fn" (fn [ctx] (return [{"id" (xtd/get-in ctx ["model" "state" "selected_id"])}]))}
         {"node" node
          "model" {"state" {"selected_id" "task-5"}}})
        (promise/x:promise-then
         (fn [result]
           (repl/notify result)))))
  => {"id" "task-5"
      "space_id" "server"
      "auth" "Bearer token-a"
      "client" "admin-ui"
      "trace" "trace-5"})

^{:refer xt.substrate.page-model/resolve-view :added "4.1"}
(fact "resolves local and api views into normalized page view state"
  (notify/wait-on :js
    (var node
         (substrate/node-create
          {"id" "node-resolve-view"
           "spaces" {"server" {"state" {}}}
           "handlers"
           {"entry/get"
            {"fn"
             (fn [space args _request _node]
               (return {"id" (xtd/get-in (xt/x:first args) ["id"])
                        "space_id" (. space ["id"])}))}}}))
    (page-model/model-put
     node
     "screen/main"
     "orders"
     {"state" {"selected_id" "task-8"}
      "views"
      {"local" {"resolver" {"type" "fn/local"
                            "fn" (fn [ctx]
                                   (return {"id" (xtd/get-in ctx ["model" "state" "selected_id"])}))}}
       "remote" {"resolver" {"type" "fn/api"
                             "target" "server"
                             "action" "entry/get"
                             "args_fn" (fn [ctx]
                                         (return [{"id" (xtd/get-in ctx ["model" "state" "selected_id"])}]))}}}})
    (-> (promise/x:promise-all
         [(page-model/resolve-view node "screen/main" "orders" "local")
          (page-model/resolve-view node "screen/main" "orders" "remote")])
        (promise/x:promise-then
         (fn [results]
           (repl/notify
            {"local" (. (xt/x:get-idx results 0) ["value"] ["id"])
             "remote" (. (xt/x:get-idx results 1) ["value"] ["space_id"])})))))
  => {"local" "task-8"
      "remote" "server"})