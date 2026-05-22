(ns xtbench.ruby.db.node.instance-model-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.node.instance-util :as util]
             [xt.db.node.schema-spec :as spec]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as event-node]
             [xt.substrate.base-space :as node-space]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.instance-model/install :added "4.1"}
(fact "installs and uninstalls xt.db.node handlers and triggers"

  (!.rb
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

  (!.rb
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

  (!.rb
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

^{:refer xt.db.node.instance-model/uninstall :added "4.1"}
(fact "removes all installed xt.db.node handlers and triggers"

  (!.rb
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

  (!.rb
   (var node (event-node/node-create {"id" "node-g"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"model-id" (. (model/model-get node "room/a" "orders") ["id"])
    "views" (xt/x:obj-keys (. (model/model-get node "room/a" "orders") ["views"]))})
  => {"model-id" "orders"
      "views" ["main" "open"]})

^{:refer xt.db.node.instance-model/view-put :added "4.1"}
(fact "registers a single additional view on an existing model"

  (!.rb
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

  (!.rb
   (var node (event-node/node-create {"id" "node-i"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"id" (. (model/model-get node "room/a" "orders") ["id"])
    "view-count" (xt/x:len (xt/x:obj-keys (. (model/model-get node "room/a" "orders") ["views"])))} )
  => {"id" "orders"
      "view-count" 2})

^{:refer xt.db.node.instance-model/view-get :added "4.1"}
(fact "returns a registered view from the node space"

  (!.rb
   (var node (event-node/node-create {"id" "node-j"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"id" (. (model/view-get node "room/a" "orders" "main") ["id"])
    "input" (model/view-input node "room/a" "orders" "main")})
  => {"id" "main"
      "input" []})

^{:refer xt.db.node.instance-model/view-val :added "4.1"}
(fact "reads the current value for a refreshed view"

  (notify/wait-on :ruby
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

  (!.rb
   (var node (event-node/node-create {"id" "node-l"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"main" (model/view-input node "room/a" "orders" "main")
    "open" (model/view-input node "room/a" "orders" "open")})
  => {"main" []
      "open" ["open"]})

^{:refer xt.db.node.instance-model/view-pending :added "4.1"}
(fact "tracks whether a view is waiting on a refresh"

  (!.rb
   (var node (event-node/node-create {"id" "node-m"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"before" (model/view-pending node "room/a" "orders" "main")
    "open" (model/view-pending node "room/a" "orders" "open")})
  => {"before" false
      "open" false})

^{:refer xt.db.node.instance-model/view-error :added "4.1"}
(fact "exposes the current view error state"

  (!.rb
   (var node (event-node/node-create {"id" "node-n"}))
   (model/install node fixtures/InstallOpts)
   (model/model-put node "room/a" "orders" fixtures/ModelSpec)
   {"error" (model/view-error node "room/a" "orders" "main")})
  => {"error" nil})

^{:refer xt.db.node.instance-model/run-remote-query :added "4.1"}
(fact "stores remote query results in the local state cache"

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
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

  (notify/wait-on :ruby
    (var node (event-node/node-create {"id" "node-s"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (node-space/ensure-space node "room/a" nil))
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

  (notify/wait-on :ruby
    (var node (event-node/node-create {"id" "node-t"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (node-space/ensure-space node "room/a" nil))
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

  (!.rb
   {"callable?" (xt/x:is-function? model/handle-sync)})
  => {"callable?" true})

^{:refer xt.db.node.instance-model/handle-remove :added "4.1"}
(fact "normalizes db/remove payloads through the sync handler"

  (notify/wait-on :ruby
    (var node (event-node/node-create {"id" "node-v"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (node-space/ensure-space node "room/a" nil))
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

  (notify/wait-on :ruby
    (var node (event-node/node-create {"id" "node-w"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (node-space/ensure-space node "room/a" nil))
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

  (notify/wait-on :ruby
    (var node (event-node/node-create {"id" "node-x"}))
    (model/install node fixtures/InstallOpts)
    (var current-space (node-space/ensure-space node "room/a" nil))
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
