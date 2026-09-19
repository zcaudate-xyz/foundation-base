(ns xt.db.node.view-base-test
  (:require [lang.core :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.view-base :as view-base]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +views+
  [{"input" []
    "return" "jsonb"
    "schema" "core"
    "id" "task_all"
    "flags" {}
    "view" {"table" "Task"
            "type" "select"
            "tag" "all"
            "query" {}}}
   {"input" [{"symbol" "i_task_id" "type" "uuid"}]
    "return" "jsonb"
    "schema" "core"
    "id" "task_default"
    "flags" {}
    "view" {"table" "Task"
            "type" "return"
            "tag" "default"
            "query" ["id" "name"]}}])

(def +registry+
  (!.js
   (view-base/collect-views (@! +views+))))

(fact "collects and resolves a select/return pair"
  (!.js
   (view-base/view-query-spec
    (@! +registry+)
    "Task"
    {"select_method" "all"
     "return_method" "default"
     "select_args" []
     "return_args" ["task-id"]}))
  => (contains-in
      {"table" "Task"
       "select_entry" map?
       "return_entry" map?
       "select_args" []
       "return_args" ["task-id"]
       "return_omit" []}))

(fact "preserves node dataview controls"
  (!.js
   (view-base/view-query-spec
    (@! +registry+)
    "Task"
    {"return_method" "default"
     "return_id" "task-id"
     "return_omit" ["secret"]}))
  => (contains-in
      {"table" "Task"
       "select_entry" nil
       "return_entry" map?
       "select_args" []
       "return_args" []
       "return_id" "task-id"
       "return_omit" ["secret"]}))

(fact "rejects unknown methods"
  (!.js
   (view-base/view-query-spec
    (@! +registry+)
    "Task"
    {"select_method" "missing"}))
  => (throws))
