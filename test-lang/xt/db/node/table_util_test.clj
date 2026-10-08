(ns xt.db.node.table-util-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [postgres.core.supabase :as s]
            [xt.db.node.table-util]))

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.substrate :as substrate]
             [xt.db.node.kernel-base :as kernel]
             [xt.db.node.table-util :as table-util]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(defn.js node-init-supabase
  [node]
  (:= node (or node (substrate/node-create {})))
  (kernel/init-handlers node)
  (return
   (kernel/kernel-setup-main
    node
    {"primary" {"type" "supabase"
                "defaults" (@! local-min/+config-supabase-anon+)}
     "caching" {"type" "memory"
                 "defaults" {}}}
    -/Schema
    -/SchemaLookup)))

(defn.js table-impl
  []
  (return
   {"base"
    {"list" {"spec" ["Log" {"data" ["id" "message"]}]}
     "data" {"spec" ["Log" {"data" ["id" "message"]}]}}
    "call" {}
    "cached" {}}))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)]
  :teardown [(l/rt:stop)]})

(def test-xt_db_node_table_util_live_table_requests true)

^{:id test-xt_db_node_table_util_live_table_requests
  :refer xt.db.node.table-util/table-pull
  :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)
          (pg/t:insert scratch-v0/Log
            {:message "hello"}
            {:track {}})]}
(fact "uses a live Supabase primary and its paired cache"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var context {"node" node "primary-id" "db/primary"})
    (var impl (-/table-impl))
    (-> (-/node-init-supabase node)
        (promise/x:promise-then
         (fn []
           (return
            (table-util/table-pull impl "call" "data" nil nil context))))
        (promise/x:promise-then
         (fn [primary]
           (return
            (promise/x:promise-then
             (promise/x:promise-all
              [(table-util/request-data context
                                         "cached"
                                         "pull"
                                         ["Log" {"data" ["id" "message"]}]
                                         {})
               (table-util/table-ids impl "cached" nil context)
               (table-util/table-ids impl "call" nil context)])
             (fn [values]
               (return [primary
                        (xt/x:get-idx values 0)
                        (xt/x:get-idx values 1)
                        (xt/x:get-idx values 2)]))))))
        (repl/notify)))
  => (contains-in
      [[{"id" string? "message" "hello"}]
       [{"id" string? "message" "hello"}]
       [string?]
       [string?]]))

^{:refer xt.db.node.table-util/context-node :added "4.1"}
(fact "gets the node from the table utility context"
  (!.js
    (var node {"id" "node"})
    (table-util/context-node {"node" node}))
  => {"id" "node"})

^{:refer xt.db.node.table-util/context-primary-id :added "4.1"}
(fact "gets the configured primary service id"
  (!.js
    [(table-util/context-primary-id {})
     (table-util/context-primary-id {"primary-id" "db/custom"})])
  => ["db/primary" "db/custom"])

^{:refer xt.db.node.table-util/context-opts :added "4.1"}
(fact "gets request options with an empty default"
  (!.js
    [(table-util/context-opts {})
     (table-util/context-opts {"opts" {"transport_id" "t1"}})])
  => [{} {"transport_id" "t1"}])

^{:refer xt.db.node.table-util/request-data :added "4.1"}
(fact "uses all native node client request paths"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var context {"node" node
                  "primary-id" "db/test"
                  "opts" {"transport_id" "t1"}})
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (return {"kind" "pull-call" "args" args}))
     nil)
    (substrate/register-handler
     node
     "@xt.db/pull-cached"
     (fn [space args request node]
       (return {"kind" "pull-cached" "args" args}))
     nil)
    (substrate/register-handler
     node
     "@xt.db/dataview-call"
     (fn [space args request node]
       (return {"kind" "dataview-call" "args" args}))
     nil)
    (substrate/register-handler
     node
     "@xt.db/dataview-cached"
     (fn [space args request node]
       (return {"kind" "dataview-cached" "args" args}))
     nil)
    (substrate/register-handler
     node
     "@xt.db/rpc-call"
     (fn [space args request node]
       (return {"kind" "rpc-call" "args" args}))
     nil)
    (-> (promise/x:promise-all
         [(table-util/request-data context "call" "pull" ["Log"] {})
          (table-util/request-data context "cached" "pull" ["Log"] {})
          (table-util/request-data context "call" "view" {"id" "view"} {})
          (table-util/request-data context "cached" "view" {"id" "view"} {})
          (table-util/request-data context
                                   "call"
                                   "rpc"
                                   {"rpc-spec" {"id" "run"}
                                    "rpc-args" ["x"]}
                                   {})])
        (promise/x:promise-then
         (fn [values]
           (return (repl/notify values))))))
  => [{"kind" "pull-call" "args" ["db/test" ["Log"]]}
      {"kind" "pull-cached" "args" ["db/test" ["Log"]]}
      {"kind" "dataview-call" "args" ["db/test" {"id" "view"}]}
      {"kind" "dataview-cached" "args" ["db/test" {"id" "view"}]}
      {"kind" "rpc-call" "args" ["db/test" {"id" "run"} ["x"]]}])

^{:refer xt.db.node.table-util/table-impl-entry :added "4.1"}
(fact "selects the request-specific table implementation entry"
  (!.js
    (table-util/table-impl-entry
     {"base" {"list" {"base" true}}
      "call" {"list" {"call" true}}}
     "call"))
  => {"list" {"call" true}})

^{:refer xt.db.node.table-util/table-check-entry :added "4.1"}
(fact "checks whether a table list entry accepts a selection"
  (!.js
    [(table-util/table-check-entry {"list" {}} nil)
     (table-util/table-check-entry {} nil)])
  => [true false])

^{:refer xt.db.node.table-util/table-level-entry :added "4.1"}
(fact "selects a request level from a merged table entry"
  (!.js
    (table-util/table-level-entry {"list" {"spec" ["Log"]}} "list"))
  => {"spec" ["Log"]})

^{:refer xt.db.node.table-util/table-level-spec :added "4.1"}
(fact "prefers explicit input, then spec, then query"
  (!.js
    [(table-util/table-level-spec
      {"spec" ["base"] "query" ["query"]}
      ["input"])
     (table-util/table-level-spec
      {"spec" ["base"] "query" ["query"]}
      nil)
     (table-util/table-level-spec {"query" ["query"]} nil)])
  => [["input"] ["base"] ["query"]])

^{:refer xt.db.node.table-util/table-request-kind :added "4.1"}
(fact "normalises table request kinds"
  (!.js
    [(table-util/table-request-kind {"type" "raw"} {})
     (table-util/table-request-kind {} ["Log"])
     (table-util/table-request-kind {"type" "rpc"} {})
     (table-util/table-request-kind {"type" "custom"} {})
     (table-util/table-request-kind {} {"table" "Log"})])
  => ["pull" "pull" "rpc" "rpc" "view"])

^{:refer xt.db.node.table-util/table-rows :added "4.1"}
(fact "normalises non-array results to no rows"
  (!.js
    [(table-util/table-rows [{"id" "a"}])
     (table-util/table-rows nil)])
  => [[{"id" "a"}] []])

^{:refer xt.db.node.table-util/table-ids-from :added "4.1"}
(fact "extracts non-nil ids from rows"
  (!.js
    [(table-util/table-ids-from
      [{"id" "a"} {"id" nil} {"id" "b"}]
      "id")
     (table-util/table-ids-from [{"key" 1}] "key")])
  => [["a" "b"] [1]])

^{:refer xt.db.node.table-util/table-pull-with-data :added "4.1"}
(fact "adds data fields without mutating the original pull tree"
  (!.js
    (var spec ["Log" {"data" ["id"]}])
    (var out (table-util/table-pull-with-data spec ["message"]))
    [out spec])
  => [["Log" {"data" ["id" "message"]}]
      ["Log" {"data" ["id"]}]])

^{:refer xt.db.node.table-util/table-pull-with-ids :added "4.1"}
(fact "adds id constraints to pull and dataview specifications"
  (!.js
    [(table-util/table-pull-with-ids
      ["Log" {"data" ["id" "message"]}]
      ["a" "b"])
     (table-util/table-pull-with-ids {"table" "Log"} ["a"])])
  => [["Log" {"data" ["id" "message"]
                "where" [{"id" ["in" ["a" "b"]]}]}]
      {"table" "Log" "return_bulk" ["a"]}])

^{:refer xt.db.node.table-util/table-ids :added "4.1"}
(fact "extracts ids from a node pull result"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (return [{"id" "a"} {"id" nil} {"id" "b"}]))
     nil)
    (-> (table-util/table-ids (-/table-impl) "call" nil {"node" node})
        (promise/x:promise-then
         (fn [ids]
           (return (repl/notify ids))))))
  => ["a" "b"])

^{:refer xt.db.node.table-util/table-ids-updated :added "4.1"}
(fact "extracts ids and update values from a node pull result"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var impl
         {"base"
          {"list"
           {"spec" ["Log" {"data" ["id" "version"]}]
            "updated-spec" ["Log" {"data" ["id" "version"]}]
            "update-key" "version"}}
          "call" {}
          "cached" {}})
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (return [{"id" "a" "version" 2}]))
     nil)
    (-> (table-util/table-ids-updated impl "call" nil {"node" node})
        (promise/x:promise-then
         (fn [rows]
           (return (repl/notify rows))))))
  => [{"id" "a" "version" 2}])

^{:refer xt.db.node.table-util/table-ids-data :added "4.1"}
(fact "adds id constraints before issuing a data request"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (return args))
     nil)
    (-> (table-util/table-ids-data
         (-/table-impl)
         "call"
         "data"
         ["a" "b"]
         nil
         {"node" node})
        (promise/x:promise-then
         (fn [args]
           (return (repl/notify args))))))
  => ["db/primary"
      ["Log" {"data" ["id" "message"]
               "where" [{"id" ["in" ["a" "b"]]}]}]])

^{:refer xt.db.node.table-util/sync-unknown-ids :added "4.1"}
(fact "finds ids present in the primary but not the cache"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/pull-cached"
     (fn [space args request node]
       (return [{"id" "a"}]))
     nil)
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (return [{"id" "a"} {"id" "b"}]))
     nil)
    (-> (table-util/sync-unknown-ids (-/table-impl) nil {"node" node})
        (promise/x:promise-then
         (fn [ids]
           (return (repl/notify ids))))))
  => ["b"])

^{:refer xt.db.node.table-util/sync-outdated-ids :added "4.1"}
(fact "finds primary ids whose update values differ from the cache"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var impl
         {"base"
          {"list"
           {"spec" ["Log" {"data" ["id" "version"]}]
            "updated-spec" ["Log" {"data" ["id" "version"]}]
            "update-key" "version"}}
          "call" {}
          "cached" {}})
    (substrate/register-handler
     node
     "@xt.db/pull-cached"
     (fn [space args request node]
       (return [{"id" "a" "version" 1}]))
     nil)
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (return [{"id" "a" "version" 2}
                {"id" "b" "version" 1}]))
     nil)
    (-> (table-util/sync-outdated-ids impl nil {"node" node})
        (promise/x:promise-then
         (fn [ids]
           (return (repl/notify ids))))))
  => ["a" "b"])

^{:refer xt.db.node.table-util/sync-unknown :added "4.1"}
(fact "pulls records for ids that are missing from the cache"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/pull-cached"
     (fn [space args request node]
       (return [{"id" "a"}]))
     nil)
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (var spec (xt/x:get-idx args 1))
       (var params (xt/x:get-idx spec 1))
       (if (xt/x:get-key params "where")
         (return [{"id" "b" "message" "from-primary"}])
         (return [{"id" "a"} {"id" "b"}])))
     nil)
    (-> (table-util/sync-unknown
         (-/table-impl)
         "data"
         nil
         nil
         {"node" node})
        (promise/x:promise-then
         (fn [rows]
           (return (repl/notify rows))))))
  => [{"id" "b" "message" "from-primary"}])

^{:refer xt.db.node.table-util/sync-outdated :added "4.1"}
(fact "returns no records when cached update values are current"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var impl
         {"base"
          {"list"
           {"spec" ["Log" {"data" ["id" "version"]}]
            "updated-spec" ["Log" {"data" ["id" "version"]}]
            "update-key" "version"}}
          "call" {}
          "cached" {}})
    (substrate/register-handler
     node
     "@xt.db/pull-cached"
     (fn [space args request node]
       (return [{"id" "a" "version" 1}]))
     nil)
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (return [{"id" "a" "version" 1}]))
     nil)
    (-> (table-util/sync-outdated
         impl
         "data"
         nil
         nil
         {"node" node})
        (promise/x:promise-then
         (fn [rows]
           (return (repl/notify rows))))))
  => [])

^{:refer xt.db.node.table-util/action-fn :added "4.1"}
(fact "creates an action backed by a node RPC handler"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/rpc-call"
     (fn [space args request node]
       (return {"service" (xt/x:first args)
                "spec" (xt/x:second args)
                "rpc-args" (xt/x:get-idx args (xt/x:offset 2))}))
     nil)
    (var action
         (table-util/action-fn
          {"rpc-spec" {"id" "save"}}
          {"node" node}))
    (-> (action "x")
        (promise/x:promise-then
         (fn [result]
           (return (repl/notify result))))))
  => {"service" "db/primary"
      "spec" {"id" "save"}
      "rpc-args" ["x"]})

^{:refer xt.db.node.table-util/make-actions :added "4.1"}
(fact "creates callable actions for an implementation"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/rpc-call"
     (fn [space args request node]
       (return {"service" (xt/x:first args)
                "spec" (xt/x:second args)
                "rpc-args" (xt/x:get-idx args (xt/x:offset 2))}))
     nil)
    (var actions
         (table-util/make-actions
          {"actions" {"save" {"rpc-spec" {"id" "save"}}}}
          {"node" node}))
    (-> ((xt/x:get-key actions "save") "x")
        (promise/x:promise-then
         (fn [result]
           (return (repl/notify result))))))
  => {"service" "db/primary"
      "spec" {"id" "save"}
      "rpc-args" ["x"]})
