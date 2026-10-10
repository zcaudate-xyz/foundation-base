(ns js.react.ext-table-test
  (:require [lang.core :as l]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [postgres.core.supabase :as s]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.client-supabase :as client-supabase]
             [js.react.helper-jsdom :as helper]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-table :as ext-table]]})

(declare test-js_react_ext_table__makeActionView_log_append_public
         test-js_react_ext_table__makeActionView_echo_plus)

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host (-> local-min/+config+ :db :host)
              :port (-> local-min/+config+ :db :port)
              :user (-> local-min/+config+ :db :user)
              :pass (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})
  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)]
  :teardown [(l/rt:stop)]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js RuntimeConfig
  {"primary" {"type" "supabase"
              ;; The RPC is granted to authenticated/service_role, so the
              ;; local integration fixture uses service_role. Browser code
              ;; must use the anon key together with an authenticated session.
              "defaults" (xt/x:obj-assign
                          (@! local-min/+config-supabase-service+)
                          {"token" (@! (-> local-min/+config+ :api :service-key))})}
   "caching" {"type" "memory"
              "defaults" {}}})

(defn.js live-context
  [node]
  (return {"node" node
           "runtime" {"config" -/RuntimeConfig
                      "schema" -/Schema
                      "lookup" -/SchemaLookup}}))

(defn.js table-impl
  []
  (return
   {"base"
    {"list" {"spec" ["Log" {"data" ["id" "message"]}]}
     "data" {"spec" ["Log" {"data" ["id" "message"]}]}}
    "call" {}
    "cached" {}}))

^{:refer js.react.ext-table/init-runtime :added "4.1"}
(fact "initialises the real Supabase runtime and RPC handler"
  
  (notify/wait-on :js
    (var context (-/live-context (substrate/node-create {})))
    (var node (. context ["node"]))
    (-> (ext-table/init-runtime context)
        (promise/x:promise-then
         (fn [output]
           (return
            {"status" (. output ["status"])
             "rpc" (xt/x:not-nil?
                    (xt/x:get-key (. node ["handlers"]) "@xt.db/rpc-call"))})))
        (promise/x:promise-finally
         (fn []
           (return (client-base/kernel-teardown node "db/primary" {}))))
        (promise/x:promise-then
         (fn [output]
           (return (repl/notify output))))))
  => {"status" "setup"
      "rpc" true})

^{:refer js.react.ext-table/runtime-handler :added "4.1"}
(fact "waits for runtime initialisation before calling a real RPC"

  (notify/wait-on :js
    (var context (-/live-context (substrate/node-create {})))
    (var node (. context ["node"]))
    (var runtime-init (ext-table/init-runtime context))
    (var handler
         (ext-table/runtime-handler
          runtime-init
          (fn [message]
            (return
             (client-base/rpc-call
              node
              "db/primary"
              {"input" [{"symbol" "i_input" "type" "text"}]
               "return" "text"
               "schema" "scratch_v0"
               "id" "echo_plus"
               "flags" {}}
              [message]
              {})))))
    (-> (handler "runtime-handler-echo")
        (promise/x:promise-finally
         (fn []
           (return (client-base/kernel-teardown node "db/primary" {}))))
        (promise/x:promise-then
         (fn [output]
           (return (repl/notify output))))))
  => "runtime-handler-echo-REMOTE")


^{:refer js.react.ext-table/listen-sync :added "4.1"}
(fact "accepts callback sources, listener objects, and missing sources"

  (!.js
    (var seen [])
    (var callback (fn [value] (. seen (push value))))
    (var cleanupFn
         (ext-table/listen-sync
          (fn [listener]
            (listener "function")
            (return "function-cleanup"))
          callback))
    (var cleanupObj
         (ext-table/listen-sync
          {"listen" (fn [listener]
                      (listener "object")
                      (return "object-cleanup"))}
          callback))
    (return {"seen" seen
             "function-cleanup" cleanupFn
             "object-cleanup" cleanupObj
             "missing" (ext-table/listen-sync {} callback)}))
  => {"seen" ["function" "object"]
      "function-cleanup" "function-cleanup"
      "object-cleanup" "object-cleanup"
      "missing" nil})

^{:id test-js_react_ext_table__makeActionView_log_append_public
  :refer js.react.ext-table/makeActionView
  :added "4.1"}
(fact "builds an action-backed view through Supabase"
  
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var impl
         {"actions"
          {"echo"
           {"rpc-spec" {"input" [{"symbol" "i_input" "type" "text"}]
                        "return" "text"
                        "schema" "scratch_v0"
                        "id" "echo_plus"
                        "flags" {}}}}})
    (var view (ext-table/makeActionView impl
                                        "echo"
                                        {"defaultArgs" ["hello"]}
                                        (-/live-context node)))
    (-> (ext-model/refresh-model view {})
        (promise/x:promise-then
         (fn [_]
           (return (repl/notify
                    {"type" (. view ["::"])
                     "output" (event-model/get-current view nil)}))))))
  => {"type" "event.model"
      "output" "hello-REMOTE"})

^{:id test-js_react_ext_table__makeActionView_echo_plus
  :refer js.react.ext-table/makeActionView
  :added "4.1"}
(fact "initialises Supabase through the node runtime and calls log_append_public once"
  (try
    (pg/t:delete scratch-v0/Log {:where {:message "MY MESSAGE"}})
    (notify/wait-on :js
      (var node (substrate/node-create {"id" "action-live-node"}))
      (var context {"node" node
                    "runtime" {"config" -/RuntimeConfig
                               "schema" -/Schema
                               "lookup" -/SchemaLookup}})
      (var view
           (ext-table/makeActionView
            {"actions"
             {"append"
              {"rpc-spec"
               {"input" [{"symbol" "i_message" "type" "text"}]
                "return" "jsonb"
                "schema" "scratch_v0"
                "id" "log_append_public"
                "flags" {}}}}}
            "append"
            {"defaultArgs" ["MY MESSAGE"]}
            context))
      (repl/notify (. view ["init"])))
    => (contains-in
        {"post" [false], "::" "model.run", "main" [true {"message" "MY MESSAGE", "author_id" nil, "id" string?}], "pre" [false]})
    [(pg/t:count scratch-v0/Log {:where {:message "MY MESSAGE"}})
     (pg/t:select scratch-v0/Log {:where {:message "MY MESSAGE"}})]
    => (contains-in
        [1
         [{:id string?, :author-id nil, :message "MY MESSAGE"}]])
    (finally
      (pg/t:delete scratch-v0/Log {:where {:message "MY MESSAGE"}}))))

^{:refer js.react.ext-table/makeActionView :added "4.1"}
(fact "delegates an action to echo through the initialised runtime"

  (notify/wait-on :js
    (var node (substrate/node-create {"id" "action-echo-node"}))
    (var context {"node" node
                  "runtime" {"config" -/RuntimeConfig
                             "schema" -/Schema
                             "lookup" -/SchemaLookup}})
    (var output nil)
    (var view
         (ext-table/makeActionView
          {"actions"
           {"echo"
            {"rpc-spec"
             {"input" [{"symbol" "i_input" "type" "text"}]
              "return" "text"
              "schema" "scratch_v0"
              "id" "echo_plus"
              "flags" {}}}}}
          "echo"
          {"defaultArgs" ["HELLO"]}
          context))
    (repl/notify (. view ["init"])))
  => {"post" [false], "::" "model.run", "main" [true "HELLO-REMOTE"], "pre" [false]})

^{:refer js.react.ext-table/useActionView :added "4.1"}
(fact "runs the mounted action once through real Supabase"
  (let [message "useActionView/mounted-once"]
    (try
      (let [result
            (helper-source/test
               (fn [props]
                 (var view
                      (ext-table/useActionView
                       (. props ["impl"])
                       "append"
                       {"defaultArgs" ["useActionView/mounted-once"]}
                       (. props ["context"])
                       {}))
                 (xt/x:set-key (. props ["state"]) "view" view)
                 (return (r/createElement "span" nil "ready")))
               (fn [_]
                 (var node (substrate/node-create {"id" "use-action-view-node"}))
                 (return
                  {"impl"
                   {"actions"
                    {"append"
                     {"rpc-spec"
                      {"input" [{"symbol" "i_message" "type" "text"}]
                       "return" "jsonb"
                       "schema" "scratch_v0"
                       "id" "log_append_public"
                       "flags" {}}}}}
                   "context" (-/live-context node)
                   "state" {}}))
               (fn [props document env]
                 (var view (. props ["state"] ["view"]))
                 (return
                  (. (. view ["init"])
                     (then
                         (fn [init]
                           (return
                            (new Promise
                             (fn [resolve]
                               (setTimeout
                                (fn []
                                  (resolve {"root-id" (. env ["root"] ["id"])
                                            "init" init}))
                                500))))))
                     (finally
                         (fn []
                           (return
                            (client-base/kernel-teardown
                             (. props ["context"] ["node"])
                             "db/primary"
                             {}))))))))]
        result
        => (contains-in
            {"root-id" "root"
             "init"
             {"main" [true
                      {"message" "useActionView/mounted-once"
                       "author_id" nil
                       "id" string?}]}})
        (pg/t:count scratch-v0/Log {:where {:message message}})
        => 1
        (pg/t:select scratch-v0/Log {:where {:message message}})
        => (contains-in
            [{:id string?
              :author-id nil
              :message "useActionView/mounted-once"}]))
      (finally
        (pg/t:delete scratch-v0/Log {:where {:message message}})))))

^{:refer js.react.ext-table/makeRemoteView :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log
            {:where {:message "makeRemoteView-live"}})
          (pg/t:insert scratch-v0/Log
            {:message "makeRemoteView-live"}
            {:track {}})]
  :teardown [(pg/t:delete scratch-v0/Log
               {:where {:message "makeRemoteView-live"}})]}
(fact "pulls a remote table view from live Supabase"
  (notify/wait-on [:js 15000]
    (var node (substrate/node-create {}))
    (var context (-/live-context node))
    (var view (ext-table/makeRemoteView
               (-/table-impl)
               "data"
               {"defaultArgs" ["Log"
                               {"where" [{"message" "makeRemoteView-live"}]
                                "data" ["id" "message"]}]}
               context))
    (-> (ext-model/refresh-model view {})
        (promise/x:promise-finally
         (fn []
           (return (client-base/kernel-teardown node "db/primary" {}))))
        (promise/x:promise-then
         (fn [_]
           (return (repl/notify (event-model/get-current view nil)))))))
  => (contains-in [{"id" string? "message" "makeRemoteView-live"}]))

^{:refer js.react.ext-table/useRemoteView :added "4.1"}
(fact "refreshes a remote table view through live Supabase"
  (let [message "useRemoteView-live"]
    (try
      (pg/t:delete scratch-v0/Log {:where {:message message}})
      (pg/t:insert scratch-v0/Log {:message message} {:track {}})
      (helper-source/test
       (fn [props]
         (var view
              (ext-table/useRemoteView
               (. props ["impl"])
               "data"
               {"defaultArgs"
                ["Log"
                 {"where" [{"message" "useRemoteView-live"}]
                  "data" ["id" "message"]}]}
               (. props ["context"])
               {}))
         (xt/x:set-key (. props ["state"]) "view" view)
         (return (r/createElement "span" nil "ready")))
       (fn [_]
         (var node (substrate/node-create {"id" "use-remote-view-node"}))
         (return {"impl" (-/table-impl)
                  "context" (-/live-context node)
                  "state" {}}))
       (fn [props document env]
         (var view (. props ["state"] ["view"]))
         (return
          (. (. view ["init"])
             (then
              (fn [init]
                (return
                 (new Promise
                  (fn [resolve]
                    (setTimeout
                     (fn []
                       (resolve
                        {"root-id" (. env ["root"] ["id"])
                         "output" (event-model/get-current view nil)}))
                     500))))))
             (finally
              (fn []
                (return
                 (client-base/kernel-teardown
                  (. props ["context"] ["node"])
                  "db/primary"
                  {}))))))))
      => (contains-in
          {"root-id" "root"
           "output" [{"id" string? "message" "useRemoteView-live"}]})
      (finally
        (pg/t:delete scratch-v0/Log {:where {:message message}})))))

^{:refer js.react.ext-table/makeListView :added "4.1"}
(fact "wires the remote and sync handlers"
  (notify/wait-on :js
    (var view (ext-table/makeListView (-/table-impl) "data" {} {}))
    (return
     (repl/notify
      {"remote" (xt/x:is-function? (. view ["pipeline"] ["remote"] ["handler"]))
       "sync" (xt/x:is-function? (. view ["pipeline"] ["sync"] ["handler"]))})))
  => {"remote" true "sync" true})

^{:refer js.react.ext-table/useListView
  :added "4.1"}
(fact "renders a list view with a reversible JSDOM lifecycle"
  (helper-source/test
   (fn [props]
     (var view (ext-table/useListView
                (. props ["impl"])
                "data"
                {}
                (. props ["context"])
                {"sync_manual" true}))
     (xt/x:set-key (. props ["state"]) "view" view)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (var impl (-/table-impl))
     (xt/x:set-key impl "event-sync"
                   (fn [listener]
                     (return (fn [] true))))
     (return {"impl" impl
              "context" {"node" (substrate/node-create {})}
              "state" {}}))
   (fn [props document env]
     (return
      (helper/await-dom
       (fn []
         (return {"root-id" (. env ["root"] ["id"])
                  "output" (event-model/get-current
                            (. props ["state"] ["view"]) nil)}))))))
  => {"root-id" "root"
      "output" nil})

^{:refer js.react.ext-table/makeSingleView :added "4.1"}
(fact "guards the single-row pipeline for missing ids"
  (!.js
    (var view (ext-table/makeSingleView
               (-/table-impl)
               "data"
               {}
               {}))
    (var main-handler (. view ["pipeline"] ["main"] ["handler"]))
    (var remote-handler (. view ["pipeline"] ["remote"] ["handler"]))
    (return [(xt/x:nil? (. (event-model/get-input view) ["current"] ["data"]))
             (xt/x:nil? (main-handler nil))
             (xt/x:is-function? remote-handler)]))
  => [true true true])

^{:refer js.react.ext-table/useSingleView :added "4.1"}
(fact "refreshes cached and remote rows inside jsdom"
  (helper-source/test
   (fn [props]
     (var view (ext-table/useSingleView
                (. props ["impl"])
                "data"
                {}
                (. props ["context"])
                {}))
     (xt/x:set-key (. props ["state"]) "view" view)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (var impl (-/table-impl))
     (xt/x:set-key impl "event-sync"
                   (fn [listener]
                     (return (fn [] true))))
     (return {"impl" impl
              "context" {"node" (substrate/node-create {})}
              "state" {}}))
   (fn [props document env]
     (return
      (helper/await-dom
       (fn []
         (return
          (helper/await-dom
           (fn []
             (return {"root-id" (. env ["root"] ["id"])
                      "output" (event-model/get-current
                                (. props ["state"] ["view"]) nil)})))))))))
  => {"root-id" "root"
      "output" nil})

^{:refer js.react.ext-table/useActions :added "4.1"}
(fact "combines action maps into a mounted action surface"
  (helper-source/test
   (fn [props]
     (var actions (ext-table/useActions
                   (. props ["impls"])
                   (. props ["context"])))
     (xt/x:set-key (. props ["state"]) "actions" actions)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"impls" [{"actions" {"save" {"rpc-spec" {"id" "save"}}}}
                       {"actions" {"remove" {"rpc-spec" {"id" "remove"}}}}]
              "context" {}
              "state" {}}))
   (fn [props document env]
     (return {"root-id" (. env ["root"] ["id"])
              "save" (xt/x:is-function? (. props ["state"] ["actions"] ["save"]))
              "remove" (xt/x:is-function? (. props ["state"] ["actions"] ["remove"]))})))
  => {"root-id" "root"
      "save" true
      "remove" true})

