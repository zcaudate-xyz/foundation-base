(ns xt.substrate.page-spec-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.substrate.page-spec :as page-spec]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-spec/resolver-type :added "4.1"}
(fact "uses snake_case resolver defaults"
  (!.js
    [(page-spec/resolver-type {})
     (page-spec/view-default-input {"default_input" ["task-1"]})
     (page-spec/view-source {"use" {"source" "archive"}})
     (page-spec/resolver-action {"action" "entry/get"})])
  => ["fn/local" ["task-1"] "archive" "entry/get"])

^{:refer xt.substrate.page-spec/view-deps :added "4.1"}
(fact "normalizes dependency groups from snake_case specs"
  (!.js
    [(page-spec/view-deps {"deps" ["list"]})
     (page-spec/view-deps {"deps" {"views" ["list"]
                                   "state" ["selected_id"]}})])
  => [{"views" ["list"] "state" []}
      {"views" ["list"] "state" ["selected_id"]}])


^{:refer xt.substrate.page-spec/model-state :added "4.1"}
(fact "gets model state from the page spec"
  (!.js
    [(page-spec/model-state {"state" {"selected_id" "task-1"}})
     (page-spec/model-state {})])
  => [{"selected_id" "task-1"}
      {}])

^{:refer xt.substrate.page-spec/model-views :added "4.1"}
(fact "gets model views from either the nested or direct view map"
  (!.js
    [(xt/x:not-nil? (. (page-spec/model-views {"views" {"list" {} "detail" {}}}) ["list"]))
     (xt/x:not-nil? (. (page-spec/model-views {"views" {"list" {} "detail" {}}}) ["detail"]))
     (xt/x:not-nil? (. (page-spec/model-views {"list" {} "detail" {}}) ["list"]))
     (xt/x:not-nil? (. (page-spec/model-views {"list" {} "detail" {}}) ["detail"]))])
  => [true true true true])

^{:refer xt.substrate.page-spec/model-actions :added "4.1"}
(fact "gets model actions from the page spec"
  (!.js
    [(xt/x:not-nil? (. (page-spec/model-actions {"actions" {"save" {} "remove" {}}}) ["save"]))
     (xt/x:not-nil? (. (page-spec/model-actions {"actions" {"save" {} "remove" {}}}) ["remove"]))
     (page-spec/model-actions {})])
  => [true true
      {}])

^{:refer xt.substrate.page-spec/view-default-input :added "4.1"}
(fact "prefers input over default_input and falls back to []"
  (!.js
    [(page-spec/view-default-input {"input" ["task-1"]
                                    "default_input" ["ignored"]})
     (page-spec/view-default-input {"default_input" ["task-2"]})
     (page-spec/view-default-input {})])
  => [["task-1"]
      ["task-2"]
      []])

^{:refer xt.substrate.page-spec/view-source :added "4.1"}
(fact "reads the declared view source with caching as the default"
  (!.js
    [(page-spec/view-source {"source" "primary"})
     (page-spec/view-source {"use" {"source" "archive"}})
     (page-spec/view-source {})])
  => ["primary" "archive" "caching"])

^{:refer xt.substrate.page-spec/view-resolver :added "4.1"}
(fact "gets the view resolver or an empty map"
  (!.js
    [(page-spec/view-resolver {"resolver" {"type" "fn/local"}})
     (page-spec/view-resolver {})])
  => [{"type" "fn/local"}
      {}])

^{:refer xt.substrate.page-spec/resolver-fn :added "4.1"}
(fact "gets the resolver fn from the canonical key"
  (!.js
    (var direct-fn (fn [_ctx] (return "ok")))
    [(xt/x:is-function? (page-spec/resolver-fn {"fn" direct-fn}))
     (page-spec/resolver-fn {})])
  => [true nil])

^{:refer xt.substrate.page-spec/resolver-args-fn :added "4.1"}
(fact "gets the resolver args builder"
  (!.js
    (var args-fn (fn [ctx] (return [(. ctx ["id"])])))
    [(xt/x:is-function? (page-spec/resolver-args-fn {"args_fn" args-fn}))
     (page-spec/resolver-args-fn {})])
  => [true nil])

^{:refer xt.substrate.page-spec/resolver-action :added "4.1"}
(fact "gets the resolver action"
  (!.js
    [(page-spec/resolver-action {"action" "entry/get"})
     (page-spec/resolver-action {})])
  => ["entry/get" nil])

^{:refer xt.substrate.page-spec/resolver-target :added "4.1"}
(fact "gets the resolver target"
  (!.js
    [(page-spec/resolver-target {"target" "server"})
     (page-spec/resolver-target {})])
  => ["server" nil])

^{:refer xt.substrate.page-spec/resolver-meta :added "4.1"}
(fact "gets resolver request meta with {} as the default"
  (!.js
    [(page-spec/resolver-meta {"meta" {"transport_id" "ws-main"}})
     (page-spec/resolver-meta {})])
  => [{"transport_id" "ws-main"}
      {}])

^{:refer xt.substrate.page-spec/resolver-api-template :added "4.1"}
(fact "gets the configured api template reference"
  (!.js
    [(page-spec/resolver-api-template {"api_template" "admin"})
     (page-spec/resolver-api-template {"api_template" {"target" "server"}})
     (page-spec/resolver-api-template {})])
  => ["admin"
      {"target" "server"}
      nil])

^{:refer xt.substrate.page-spec/resolver-args :added "4.1"}
(fact "builds resolver args from args_fn, args, or ctx input"
  (!.js
    (var args-fn (fn [ctx] (return [(. ctx ["id"])])))
    [(page-spec/resolver-args {"args_fn" args-fn} {"id" "task-1" "input" ["ignored"]})
     (page-spec/resolver-args {"args" ["task-2"]} {"input" ["ignored"]})
     (page-spec/resolver-args {} {"input" ["task-3"]})
     (page-spec/resolver-args {} {})])
  => [["task-1"]
      ["task-2"]
      ["task-3"]
      []])

^{:refer xt.substrate.page-spec/resolver-trigger-pre :added "4.1"}
(fact "gets the pre trigger from the canonical trigger.pre key"
  (!.js
    (var trigger-fn (fn [ctx] (return ctx)))
    [(xt/x:is-function? (page-spec/resolver-trigger-pre {"trigger.pre" trigger-fn}))
     (page-spec/resolver-trigger-pre {})])
  => [true nil])

^{:refer xt.substrate.page-spec/resolver-trigger-post :added "4.1"}
(fact "gets the post trigger from the canonical trigger.post key"
  (!.js
    (var trigger-fn (fn [ctx result] (return result)))
    [(xt/x:is-function? (page-spec/resolver-trigger-post {"trigger.post" trigger-fn}))
     (page-spec/resolver-trigger-post {})])
  => [true nil])