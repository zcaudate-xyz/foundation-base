(ns app.manage.page-runtime-test
  (:use code.test)
  (:require [lang.core :as l]
            [app.manage.integration :as integration]))
(l/script- :js
  {:runtime :basic
   :test-mode true
   :require [[app.manage.page-runtime :as runtime]
             [xt.lang.common-repl :as repl]]})
(fact:global {:setup [(l/rt:restart)] :teardown [(l/rt:stop)]})
^{:refer app.manage.page-runtime/execute :id automatic-page}
(fact "new declarations reuse supported operations without page dispatch edits"
  (integration/await-js
   (runtime/execute
    [{"id" "new-page" "models" {"rows" {"operation" "list"}}}]
    {"list" (fn [input] (return {"result" input.payload}))}
    {"page" "new-page" "kind" "model" "member" "rows" "payload" {"title" "Declared"}}))
  =>
  {"result" {"title" "Declared"}})
^{:refer app.manage.page-runtime/request :id bindings}
(fact "request bindings preserve false and null"
  (!.js (runtime/request {"transport" {"method" "POST"
                                       "path" "/create"
                                       "constants" {"action" "create"}
                                       "bindings" {"enabled" "active" "title" "name"}}}
                         {"active" false "name" nil}))
  =>
  {"path" "/create" "method" "POST" "body" {"action" "create" "enabled" false "title" nil}})
^{:refer app.manage.page-runtime/execute :id unavailable}
(fact "unavailable adapters remain blocked"
  (integration/await-js (runtime/execute [{"id" "page" "models" {"one" {"operation" "missing"}}}]
                                         {}
                                         {"page" "page" "kind" "model" "member" "one"}))
  =>
  {"blocked" "This operation has no live workbench adapter yet."})
^{:refer app.manage.page-runtime/execute :id ordered-page}
(fact "ordered page models use declared bindings and defaults"
  (integration/await-js (runtime/execute
                         [{"id" "composed"
                           "order" ["second" "first"]
                           "models" {"first" {"operation" "read" "defaults" {"label" "default"}}
                                     "second" {"operation" "read" "bindings" {"label" "name"}}}}]
                         {"read" (fn [input] (return {"result" input.payload.label}))}
                         {"page" "composed" "kind" "page" "inputs" {"name" "bound"}}))
  =>
  {"result" {"second" "bound" "first" "default"} "state" {}})
