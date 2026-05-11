(ns xt.db.node.instance-model-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.node.instance-util :as util]
             [xt.db.node.schema-spec :as spec]
             [xt.db.node.test-fixtures :as fixtures]
             [xt.event.node :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +deps-model-spec+
  {"views"
   {"main"
    {"query" {:table "Order"
              :return-method "default"
              :return-id "ord-1"}
     "input" []}
    "open"
    {"query" {:table "Order"
              :select-method "by_status"}
     "default_input" ["open"]
     "deps" ["main"]}}})

^{:refer xt.db.node.instance-model/install :added "4.1"}
(fact "installs and uninstalls xt.db.node handlers and triggers"

  (!.js
   (var node (event-node/node-create {"id" "node-a"}))
   (model/install node fixtures/InstallOpts)
   (var installed
        {"schema-id" (xtd/get-in (util/node-opts node)
                                 ["schema" "Order" "id" "ident"])
         "has-query" (xt/x:has-key? (. node ["handlers"]) spec/ACTION_QUERY)
         "has-sync" (xt/x:has-key? (. node ["handlers"]) spec/ACTION_SYNC)
         "has-cache-changed" (xt/x:has-key? (. node ["triggers"])
                                            spec/SIGNAL_CACHE_CHANGED)})
   (model/uninstall node)
   [installed
    {"has-query" (xt/x:has-key? (. node ["handlers"]) spec/ACTION_QUERY)
     "has-cache-changed" (xt/x:has-key? (. node ["triggers"])
                                        spec/SIGNAL_CACHE_CHANGED)}])
  => [{"schema-id" "id"
       "has-query" true
       "has-sync" true
       "has-cache-changed" true}
      {"has-query" false
       "has-cache-changed" false}])

^{:refer xt.db.node.instance-model/ensure-space-state :added "4.1"}
(fact "creates space state and exposes model/view helpers"

  ^{:seedgen/base
    {:lua
     {:expect
      (l/as-lua
       {"state-tag" "xt.db.state"
        "schema-id" "id"
        "model-id" "orders"
        "main-input" []
        "secondary-id" "secondary"
        "secondary-input" ["ord-2"]
        "pending?" false
        "error" nil})}}}
  (!.js
   (var node (event-node/node-create {"id" "node-a"}))
   (model/install node fixtures/InstallOpts)
   (var state (model/ensure-space-state node "room/a"))
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   (model/view-put node "room/a" "orders" "secondary" {"default_input" ["ord-2"]})
   {"state-tag" (. state ["::"])
    "schema-id" (xtd/get-in state ["schema" "Order" "id" "ident"])
   "model-id" (. (model/model-get node "room/a" "orders") ["id"])
   "main-input" (model/view-input node "room/a" "orders" "main")
   "secondary-id" (. (model/view-get node "room/a" "orders" "secondary") ["id"])
   "secondary-input" (model/view-input node "room/a" "orders" "secondary")
   "pending?" (model/view-pending node "room/a" "orders" "main")
    "error" (model/view-error node "room/a" "orders" "main")})
  => {"state-tag" "xt.db.state"
      "schema-id" "id"
      "model-id" "orders"
      "main-input" []
      "secondary-id" "secondary"
      "secondary-input" ["ord-2"]
      "pending?" false
      "error" nil})

^{:refer xt.db.node.instance-model/normalize-remote :added "4.1"}
(fact "merges state, remote-spec, and view-level remote settings"

  (!.js
   (model/normalize-remote
    {"remote" {"transport" "worker"
               "meta" {"role" "base"}}}
    {"space" "remote"}
    {"remote" {"meta" {"role" "view"}
               "target" "room/b"}}))
  => {"transport" "worker"
      "space" "remote"
      "target" "room/b"
      "meta" {"role" "view"}})

^{:refer xt.db.node.instance-model/request-remote :added "4.1"}
(fact "routes remote requests through the target space"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (event-node/register-handler
     node
     "echo"
     (fn [space args request node]
       (return {"space" (. space ["id"])
                "payload" (xt/x:get-idx args 0)
                "meta" (. request ["meta"])}))
     nil)
    (promise/x:promise-catch
     (promise/x:promise-then
      (model/request-remote
       node
       "room/a"
       {"space" "room/b"
        "meta" {"demo" true}}
       "echo"
       {"ping" 1})
      (fn [response]
        (repl/notify response)))
     (fn [err]
       (repl/notify err))))
  => {"space" "room/b"
      "payload" {"ping" 1}
      "meta" {"demo" true}})

^{:refer xt.db.node.instance-model/query :added "4.1"}
(fact "handles local sync/remove/query/snapshot requests"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (promise/x:promise-catch
     (promise/x:promise-then
      (model/sync node "room/a" {"db/sync" fixtures/Seed})
      (fn [_]
        (return (model/remove node "room/a" {"db/remove" {"Order" ["ord-2"]}}))))
     (fn [err]
       (repl/notify err)))
    (promise/x:promise-catch
     (promise/x:promise-then
      (model/query
       node
       "room/a"
       {"table" "Order"
        "return-method" "default"
        "return-id" "ord-1"})
       (fn [query]
         (return
          (promise/x:promise-then
           (model/snapshot node "room/a")
           (fn [snapshot]
             (repl/notify
              {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))}))))))
      (fn [err]
        (repl/notify err))))
  => {"rows" ["ord-1"]})

^{:refer xt.db.node.instance-model/clear :added "4.1"}
(fact "clears cache rows through the public request wrapper"

  ^{:seedgen/base {:lua {:expect (l/as-lua {"cleared" true
                                            "rows" []})}}}
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (promise/x:promise-catch
     (promise/x:promise-then
      (model/sync node "room/a" {"db/sync" fixtures/Seed})
      (fn [_]
        (return
         (promise/x:promise-then
          (model/clear node "room/a")
          (fn [cleared]
            (return
             (promise/x:promise-then
              (model/snapshot node "room/a")
              (fn [snapshot]
                (repl/notify
                 {"cleared" cleared
                  "rows" (xt/x:obj-keys (. snapshot ["rows"]))})))))))))
     (fn [err]
       (repl/notify err))))
  => {"cleared" true
      "rows" []})

^{:refer xt.db.node.instance-model/view-refresh :added "4.1"}
(fact "refreshes models and views with explicit space state"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (model/view-set-input node "room/a" "orders" "open" ["closed"])
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (promise/x:promise-then
        (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (fn [_]
          (return (model/view-refresh node "room/a" "orders" "main"))))
       (fn [_]
         (return (model/model-refresh node "room/a" "orders"))))
      (fn [refreshes]
        (repl/notify
         {"main-status" (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                   ["status"])
          "main-value" (xtd/get-in (model/view-val node "room/a" "orders" "main")
                                  [0 "status"])
          "open-input" (model/view-input node "room/a" "orders" "open")
          "refresh-count" (xt/x:len refreshes)})))
     (fn [err]
       (repl/notify err))))
  => {"main-status" "ready"
      "main-value" "open"
       "open-input" ["closed"]
       "refresh-count" 2})

^{:refer xt.db.node.instance-model/view-dependents :added "4.1"}
(fact "tracks dependent views and refreshes them after a root view update"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a-deps"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" (@! +deps-model-spec+))
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return (model/view-refresh node "room/a" "orders" "main"))))
       (fn [_]
        (repl/notify
         {"dependents" (model/view-dependents node "room/a" "orders" "main")
          "open-status" (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                    ["status"])
          "open-query?" (xt/x:is-string?
                         (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                     ["query_key"]))})))
     (fn [err]
       (repl/notify err))))
  => {"dependents" {"orders" ["open"]}
      "open-status" "ready"
      "open-query?" true})

^{:refer xt.db.node.instance-model/uninstall :added "4.1"}
(fact "removes all installed xt.db.node handlers and triggers"

  (!.js
   (var node (event-node/node-create {"id" "node-b"}))
   (model/install node fixtures/InstallOpts)
   (model/uninstall node)
   {"query?" (xt/x:has-key? (. node ["handlers"]) spec/ACTION_QUERY)
    "sync?" (xt/x:has-key? (. node ["handlers"]) spec/ACTION_SYNC)
    "cache-trigger?" (xt/x:has-key? (. node ["triggers"]) spec/SIGNAL_CACHE_CHANGED)})
  => {"query?" false
      "sync?" false
      "cache-trigger?" false})

^{:refer xt.db.node.instance-model/query-refresh :added "4.1"}
(fact "refreshes an existing cached query by query-key"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-c"}))
    (model/install node fixtures/InstallOpts)
    (promise/x:promise-catch
     (promise/x:promise-then
      (model/sync node "room/a" {"db/sync" fixtures/Seed})
      (fn [_]
        (return
         (promise/x:promise-then
          (model/query node "room/a" {"table" "Order"
                                      "return-method" "default"
                                      "return-id" "ord-1"})
          (fn [result]
            (return
             (promise/x:promise-then
              (model/query-refresh node "room/a" {"query_key" (. result ["query_key"])})
              (fn [entry]
                (repl/notify {"key" (. entry ["key"])
                              "status" (. entry ["status"])})))))))))
     (fn [err]
       (repl/notify err))))
  => {"key" "{\"query\":{\"select_args\":[],\"table\":\"Order\",\"return_args\":[]}}"
      "status" "stale"})

^{:refer xt.db.node.instance-model/sync :added "4.1"}
(fact "syncs rows into a node space through the request wrapper"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-d"}))
    (model/install node fixtures/InstallOpts)
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return (model/snapshot node "room/a"))))
      (fn [snapshot]
        (repl/notify {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))})))
     (fn [err]
       (repl/notify err))))
  => {"rows" ["ord-1" "ord-2"]})

^{:refer xt.db.node.instance-model/remove :added "4.1"}
(fact "removes rows from a node space through the request wrapper"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-e"}))
    (model/install node fixtures/InstallOpts)
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return
          (promise/x:promise-then
           (model/remove node "room/a" {"db/remove" {"Order" ["ord-2"]}})
           (fn [_]
             (return (model/snapshot node "room/a")))))))
      (fn [snapshot]
        (repl/notify {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))})))
     (fn [err]
       (repl/notify err))))
  => {"rows" ["ord-1"]})

^{:refer xt.db.node.instance-model/snapshot :added "4.1"}
(fact "returns the current models and rows for a node space"

  ^{:seedgen/base {:lua {:expect (l/as-lua {"models" ["orders"]
                                            "rows" []})}}}
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-f"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (promise/x:promise-catch
     (promise/x:promise-then
      (model/snapshot node "room/a")
      (fn [snapshot]
        (repl/notify {"models" (xt/x:obj-keys (. snapshot ["models"]))
                      "rows" (xt/x:obj-keys (. snapshot ["rows"]))})))
     (fn [err]
       (repl/notify err))))
  => {"models" ["orders"]
      "rows" []})

^{:refer xt.db.node.instance-model/model-put :added "4.1"}
(fact "registers a model and its declared views on the node space"

  (!.js
   (var node (event-node/node-create {"id" "node-g"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"model-id" (. (model/model-get node "room/a" "orders") ["id"])
    "views" (xt/x:obj-keys (. (model/model-get node "room/a" "orders") ["views"]))})
  => {"model-id" "orders"
      "views" ["main" "open"]})

^{:refer xt.db.node.instance-model/view-put :added "4.1"}
(fact "registers a single additional view on an existing model"

  (!.js
   (var node (event-node/node-create {"id" "node-h"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   (model/view-put node "room/a" "orders" "secondary" {"default_input" ["ord-2"]})
   {"view-id" (. (model/view-get node "room/a" "orders" "secondary") ["id"])
    "input" (model/view-input node "room/a" "orders" "secondary")})
  => {"view-id" "secondary"
      "input" ["ord-2"]})

^{:refer xt.db.node.instance-model/model-get :added "4.1"}
(fact "returns a registered model from the node space"

  (!.js
   (var node (event-node/node-create {"id" "node-i"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"id" (. (model/model-get node "room/a" "orders") ["id"])
    "view-count" (xt/x:len (xt/x:obj-keys (. (model/model-get node "room/a" "orders") ["views"])))} )
  => {"id" "orders"
      "view-count" 2})

^{:refer xt.db.node.instance-model/view-get :added "4.1"}
(fact "returns a registered view from the node space"

  ^{:seedgen/base {:lua {:expect (l/as-lua {"id" "main"
                                            "input" []})}}}
  (!.js
   (var node (event-node/node-create {"id" "node-j"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"id" (. (model/view-get node "room/a" "orders" "main") ["id"])
    "input" (model/view-input node "room/a" "orders" "main")})
  => {"id" "main"
      "input" []})

^{:refer xt.db.node.instance-model/view-val :added "4.1"}
(fact "reads the current value for a refreshed view"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-k"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return (model/view-refresh node "room/a" "orders" "main"))))
      (fn [_]
        (repl/notify {"status" (xtd/get-in (model/view-val node "room/a" "orders" "main")
                                           [0 "status"])})))
     (fn [err]
       (repl/notify err))))
  => {"status" "open"})

^{:refer xt.db.node.instance-model/view-input :added "4.1"}
(fact "reads the configured input for a view"

  ^{:seedgen/base {:lua {:expect (l/as-lua {"main" []
                                            "open" ["open"]})}}}
  (!.js
   (var node (event-node/node-create {"id" "node-l"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"main" (model/view-input node "room/a" "orders" "main")
    "open" (model/view-input node "room/a" "orders" "open")})
  => {"main" []
      "open" ["open"]})

^{:refer xt.db.node.instance-model/view-pending :added "4.1"}
(fact "tracks whether a view is waiting on a refresh"

  (!.js
   (var node (event-node/node-create {"id" "node-m"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"before" (model/view-pending node "room/a" "orders" "main")
    "open" (model/view-pending node "room/a" "orders" "open")})
  => {"before" false
      "open" false})

^{:refer xt.db.node.instance-model/view-error :added "4.1"}
(fact "exposes the current view error state"

  ^{:seedgen/base {:lua {:expect (l/as-lua {"error" nil})}}}
  (!.js
   (var node (event-node/node-create {"id" "node-n"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"error" (model/view-error node "room/a" "orders" "main")})
  => {"error" nil})

^{:refer xt.db.node.instance-model/run-remote-query :added "4.1"}
(fact "stores remote query results in the local state cache"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-o"}))
    (model/install node fixtures/InstallOpts)
    (var state (model/ensure-space-state node "room/a"))
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (model/model-put node "room/b" "orders" fixtures/ModelSpec)
    (var remote-query-p
         (promise/x:promise-then
          (model/sync node "room/b" {"db/sync" fixtures/Seed})
          (fn [_]
            (return
             (model/run-remote-query
              node
              "room/a"
              state
              {:table "Order"
               :return-method "default"
               :return-id "ord-1"}
              {"model-id" "orders"
               "view-id" "main"}
              {"space" "room/b"}
              "orders"
              "main")))))
    (promise/x:promise-catch
     (promise/x:promise-then
      remote-query-p
      (fn [entry]
        (repl/notify {"status" (. entry ["status"])
                      "query-count" (xt/x:len (xt/x:obj-keys (. state ["queries"])))
                      "value?" (xt/x:not-nil? (. entry ["value"]))})))
     (fn [err]
       (repl/notify err))))
  => {"status" "ready"
      "query-count" 1
      "value?" true})

^{:refer xt.db.node.instance-model/run-remote-sync :added "4.1"}
(fact "applies remote sync responses back into the local state"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-p"}))
    (model/install node fixtures/InstallOpts)
    (var state (model/ensure-space-state node "room/a"))
    (var remote-sync-p
         (promise/x:promise-then
          (model/run-remote-sync
           node
           "room/a"
           state
           {"db/sync" fixtures/Seed}
           {"model-id" "orders"}
           {"space" "room/a"})
          (fn [_]
            (return (model/snapshot node "room/a")))))
    (promise/x:promise-catch
     (promise/x:promise-then
      remote-sync-p
      (fn [snapshot]
        (repl/notify {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))})))
     (fn [err]
       (repl/notify err))))
  => {"rows" ["ord-1" "ord-2"]})

^{:refer xt.db.node.instance-model/model-refresh :added "4.1"}
(fact "refreshes every registered view for a model"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-q"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (var model-refresh-p
         (promise/x:promise-then
          (model/sync node "room/a" {"db/sync" fixtures/Seed})
          (fn [_]
            (return (model/model-refresh node "room/a" "orders")))))
    (promise/x:promise-catch
     (promise/x:promise-then
      model-refresh-p
      (fn [refreshes]
        (repl/notify {"count" (xt/x:len refreshes)
                      "main" (xtd/get-in (model/view-val node "room/a" "orders" "main")
                                         [0 "status"])})))
     (fn [err]
       (repl/notify err))))
  => {"count" 2
      "main" "open"})

^{:refer xt.db.node.instance-model/view-set-input :added "4.1"}
(fact "stores view input and refreshes the view"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-r"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (var set-input-p
         (promise/x:promise-then
          (model/sync node "room/a" {"db/sync" fixtures/Seed})
          (fn [_]
            (return (model/view-set-input node "room/a" "orders" "open" ["closed"])))))
    (promise/x:promise-catch
     (promise/x:promise-then
      set-input-p
      (fn [result]
        (repl/notify {"input" (model/view-input node "room/a" "orders" "open")
                      "query-key?" (xt/x:is-string? (. result ["query_key"]))})))
     (fn [err]
       (repl/notify err))))
  => {"input" ["closed"]
      "query-key?" true})

^{:refer xt.db.node.instance-model/handle-query :added "4.1"}
(fact "handles a local query payload directly"

  ^{:seedgen/base {:lua {:expect (l/as-lua {"query-key?" true
                                            "value?" false
                                            "tables" []})}}}
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-s"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (event-node/ensure-space node "room/a" nil))
    (var query-p
         (promise/x:promise-then
          (model/sync node "room/a" {"db/sync" fixtures/Seed})
          (fn [_]
            (return
             (model/handle-query current-space
                                 [{"query" {"table" "Order"
                                            "return-method" "default"
                                            "return-id" "ord-1"}}]
                                 {}
                                 node)))))
    (promise/x:promise-catch
     (promise/x:promise-then
      query-p
      (fn [result]
        (repl/notify {"query-key?" (xt/x:is-string? (. result ["query_key"]))
                      "value?" (xt/x:not-nil? (. result ["value"]))
                      "tables" (xt/x:obj-keys (. result ["tables"]))})))
     (fn [err]
       (repl/notify err))))
  => {"query-key?" true
      "value?" false
      "tables" []})

^{:refer xt.db.node.instance-model/handle-query-refresh :added "4.1"}
(fact "refreshes a cached query or falls back to a normal query"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-t"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (event-node/ensure-space node "room/a" nil))
    (var initial-p
         (promise/x:promise-then
          (model/sync node "room/a" {"db/sync" fixtures/Seed})
          (fn [_]
            (return
             (model/handle-query current-space
                                 [{"query" {"table" "Order"
                                            "return-method" "default"
                                            "return-id" "ord-1"}}]
                                 {}
                                 node)))))
    (var refresh-p
         (promise/x:promise-then
          initial-p
          (fn [initial]
            (return
             (model/handle-query-refresh current-space
                                         [{"query_key" (. initial ["query_key"])}]
                                         {}
                                         node)))))
    (promise/x:promise-catch
     (promise/x:promise-then
      refresh-p
      (fn [entry]
        (repl/notify {"key" (. entry ["key"])
                      "status" (. entry ["status"])})))
     (fn [err]
       (repl/notify err))))
  => {"key" "{\"query\":{\"select_args\":[],\"table\":\"Order\",\"return_args\":[]}}"
      "status" "stale"})

^{:refer xt.db.node.instance-model/handle-sync :added "4.1"}
(fact "handles a local sync payload and publishes cache updates"

  (!.js
   {"callable?" (xt/x:is-function? model/handle-sync)})
  => {"callable?" true})

^{:refer xt.db.node.instance-model/handle-remove :added "4.1"}
(fact "normalizes db/remove payloads through the sync handler"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-v"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (event-node/ensure-space node "room/a" nil))
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return
          (promise/x:promise-then
           (model/handle-remove current-space
                                [{"db/remove" {"Order" ["ord-2"]}}]
                                {}
                                node)
           (fn [_]
             (return (model/snapshot node "room/a")))))))
      (fn [snapshot]
        (repl/notify {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))})))
     (fn [err]
       (repl/notify err))))
  => {"rows" ["ord-1"]})

^{:refer xt.db.node.instance-model/handle-clear :added "4.1"}
(fact "clears cache state and invalidates all tables"

  ^{:seedgen/base {:lua {:expect (l/as-lua {"cleared" true
                                            "rows" []})}}}
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-w"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (event-node/ensure-space node "room/a" nil))
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return (model/handle-clear current-space [] {} node))))
      (fn [cleared]
        (return
         (promise/x:promise-then
          (model/snapshot node "room/a")
          (fn [snapshot]
            (repl/notify {"cleared" cleared
                          "rows" (xt/x:obj-keys (. snapshot ["rows"]))}))))))
     (fn [err]
       (repl/notify err))))
  => {"cleared" true
      "rows" []})

^{:refer xt.db.node.instance-model/handle-snapshot :added "4.1"}
(fact "returns snapshot data directly from the current space state"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-x"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (event-node/ensure-space node "room/a" nil))
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return (model/handle-snapshot current-space [] {} node))))
      (fn [snapshot]
        (repl/notify {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))
                      "models" (xt/x:obj-keys (. snapshot ["models"]))})))
     (fn [err]
       (repl/notify err))))
  => {"rows" ["ord-1" "ord-2"]
      "models" []})


^{:refer xt.db.node.instance-model/model-dependents :added "4.1"}
(fact "tracks dependent models for a source model"

  (!.js
   (var node (event-node/node-create {"id" "node-z"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   (model/model-put node "room/a" "stats"
                    {"views"
                     {"summary"
                      {"deps" [["orders" "main"]]}}})
   (model/model-dependents node "room/a" "orders"))
  => {"stats" true})

^{:refer xt.db.node.instance-model/view-remote-spec :added "4.1"}
(fact "returns only meaningful remote view configs"

  ^{:seedgen/base {:lua {:expect (l/as-lua [{"space" "room/b"}
                                            {"meta" {"trace" true}}
                                            nil])}}}
  (!.js
   [(model/view-remote-spec {"remote" {"space" "room/b"}})
    (model/view-remote-spec {"remote" {"meta" {"trace" true}}})
    (model/view-remote-spec {"remote" {"channel" "ignored"}})])
  => [{"space" "room/b"}
      {"meta" {"trace" true}}
      nil])

^{:refer xt.db.node.instance-model/refresh-seen? :added "4.1"}
(fact "checks whether a refresh chain has visited a view"

  (!.js
   [(model/refresh-seen? {} "orders" "main")
    (model/refresh-seen? {"orders" {"main" true}} "orders" "main")])
  => [false true])

^{:refer xt.db.node.instance-model/mark-refresh-seen :added "4.1"}
(fact "marks a view as visited in a refresh chain"

  (!.js
   (model/mark-refresh-seen {} "orders" "main"))
  => {"orders" {"main" true}})

^{:refer xt.db.node.instance-model/view-refresh-result :added "4.1"}
(fact "returns public refresh fields from the current view state"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-aa"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return (model/view-refresh node "room/a" "orders" "main"))))
       (fn [_]
         (var result
              (model/view-refresh-result
               (model/view-get node "room/a" "orders" "main")))
         (repl/notify [(xt/x:is-string? (. result ["query_key"]))
                       (xtd/get-in result ["value" 0 "status"])
                       (xt/x:obj-keys (. result ["tables"]))])))
     (fn [err]
       (repl/notify err))))
  => [true
      "open"
      ["Order"]])

^{:refer xt.db.node.instance-model/run-view-main :added "4.1"}
(fact "runs the local query stage for a view context"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-ab"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (var state (model/ensure-space-state node "room/a"))
    (promise/x:promise-catch
     (promise/x:promise-then
      (model/sync node "room/a" {"db/sync" fixtures/Seed})
      (fn [_]
        (var result
             (model/run-view-main
              {"state" state
               "model-id" "orders"
               "view-id" "main"
               "args" []
               "view" (model/view-get node "room/a" "orders" "main")}))
        (repl/notify [(. result ["key"])
                      (xtd/get-in result ["value" 0 "status"])
                      (xt/x:obj-keys (. result ["tables"]))])))
     (fn [err]
       (repl/notify err))))
  => [nil
      "open"
      ["Order"]])

^{:refer xt.db.node.instance-model/run-view-remote :added "4.1"}
(fact "runs the remote query stage when a view has remote settings"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-ac"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders"
                     {"views"
                      {"main"
                       {"query" {:table "Order"
                                 :return-method "default"
                                 :return-id "ord-1"}
                        "input" []
                        "remote" {"space" "room/b"}}}})
    (model/model-put node "room/b" "orders" fixtures/ModelSpec)
    (var state (model/ensure-space-state node "room/a"))
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/b" {"db/sync" fixtures/Seed})
       (fn [_]
         (return
          (model/run-view-remote
           {"node" node
            "space-id" "room/a"
            "state" state
            "model-id" "orders"
            "view-id" "main"
            "args" []
            "view" (model/view-get node "room/a" "orders" "main")}))))
      (fn [entry]
        (repl/notify {"status" (. entry ["status"])
                      "value?" (xt/x:not-nil? (. entry ["value"]))})))
     (fn [err]
       (repl/notify err))))
  => {"status" "ready"
      "value?" true})

^{:refer xt.db.node.instance-model/configure-view-pipeline :added "4.1"}
(fact "installs local and remote pipeline handlers on a view"

  (!.js
   (var view {})
   (model/configure-view-pipeline view)
   [(xt/x:is-function? (. (. (. view ["pipeline"]) ["main"]) ["handler"]))
    (xt/x:is-function? (. (. (. view ["pipeline"]) ["remote"]) ["handler"]))])
  => [true true])

^{:refer xt.db.node.instance-model/refresh-view-dependents :added "4.1"}
(fact "refreshes dependent views through the per-model throttle"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-ad"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" (@! +deps-model-spec+))
    (var state (model/ensure-space-state node "room/a"))
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return
          (model/refresh-view-dependents
           node
           "room/a"
           state
           "orders"
           "main"
           {"visited" {"orders" {"main" true}}}))))
      (fn [refreshes]
        (repl/notify {"count" (xt/x:len refreshes)
                      "open-status" (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                                ["status"])})))
     (fn [err]
       (repl/notify err))))
  => {"count" 1
      "open-status" "ready"})

^{:refer xt.db.node.instance-model/view-refresh-impl :added "4.1"}
(fact "refreshes one view and returns its public result"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-ae"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (model/sync node "room/a" {"db/sync" fixtures/Seed})
       (fn [_]
         (return (model/view-refresh-impl node "room/a" "orders" "main" nil))))
      (fn [result]
        (repl/notify {"query-key?" (xt/x:is-string? (. result ["query_key"]))
                      "value" (xtd/get-in result ["value" 0 "status"])
                      "tables" (xt/x:obj-keys (. result ["tables"]))})))
     (fn [err]
       (repl/notify err))))
  => {"query-key?" true
      "value" "open"
      "tables" ["Order"]})

^{:refer xt.db.node.instance-model/ensure-model-throttle :added "4.1"}
(fact "creates and reuses one throttle per model"

  (!.js
   (var node (event-node/node-create {"id" "node-af"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   (var state (model/ensure-space-state node "room/a"))
   (var first (model/ensure-model-throttle node "room/a" state "orders"))
   (var second (model/ensure-model-throttle node "room/a" state "orders"))
   [(== first second)
    (== first (xtd/get-in state ["models" "orders" "throttle"]))])
  => [true true])

^{:refer xt.db.node.instance-model/pipeline-run-async :added "4.1"}
(fact "emits js source for the async pipeline adapter"
  (let [out (l/emit-as :js
                       '[(xt.db.node.instance-model/pipeline-run-async
                          handler
                          context
                          callbacks)])]
    [(string? out)
     (< 0 (count out))])
  => [true true])
