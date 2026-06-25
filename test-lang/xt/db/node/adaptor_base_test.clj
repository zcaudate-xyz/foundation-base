(ns xt.db.node.adaptor-base-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

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

(l/script- :xtalk
  {:require [[xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]]})

(defn.xt invoke-attached-model
  "invokes the raw handler of an attached page model"
  [node space-id group-id model-id args]
  (var [group model] (substrate/page-model-ensure node space-id group-id model-id))
  (var handler (xtd/get-in model ["pipeline" "main" "handler"]))
  (return (handler {"node" node "args" args})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.adaptor-base :as adaptor]
             [xt.db.system.impl-common :as impl-common]
             [xt.substrate :as substrate]
             [xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.adaptor-base/init-adaptor-type :added "4.1"}
(fact "init-adaptor-type installs a live impl on the node"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/init-adaptor-type node
                              "db/primary"
                              "postgres"
                              (@! (local-min/+config+ :db))
                              -/Schema
                              -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-service node "db/primary"))))))
  => (contains-in
      {"schema" map? "lookup" map? "opts" map?
       "::" "xt.db.system.impl_postgres/ImplPostgres"
       "::/protocols" ["xt.db.system.impl_common/ISourceRemote"]
       "client" {"::" "js.net.conn_postgres/PostgresClient"
                 "::/protocols" ["xt.net.conn_sql/ISqlClient"]
                 "raw" map?}})
  
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/init-adaptor-type node
                              "db/caching"
                              "sqlite"
                              {}
                              -/Schema
                              -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-service node "db/caching"))))))
  => (contains-in
      {"schema" map? "lookup" map? "opts" map?
       "::" "xt.db.system.impl_sqlite/ImplSqlite"
       "::/protocols" ["xt.db.system.impl_common/ISourceLocal"
                       "xt.db.system.impl_common/ISourceRemote"]
       "client" {"::" "js.net.conn_sqlite/SqliteClient"
                 "::/protocols" ["xt.net.conn_sql/ISqlClient"]
                 "raw" map?}}))

^{:refer xt.db.node.adaptor-base/init-adaptor-main :added "4.1"}
(fact "init-adaptor-main installs the db/common db/primary and db/caching services"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-services node))))))
  => (contains-in
      {"db/caching" map?,
       "db/primary" map?,
       "db/common" map?}))

^{:refer xt.db.node.adaptor-base/init-adaptor-handler :added "4.1"}
(fact "init-adaptor-handler initialises services from client args"

  (notify/wait-on :js
    (-> (adaptor/init-adaptor-handler
         nil
         [{"primary" {"type" "postgres"
                      "defaults" (@! (local-min/+config+ :db))}
           "caching" {"type" "sqlite"
                      "defaults" {"filename" ":memory:"}}}
          -/Schema
          -/SchemaLookup]
         nil
         (substrate/node-create {}))
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-services node))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      {"db/caching" map?
       "db/primary" map?
       "db/common" map?}))

^{:refer xt.db.node.adaptor-base/call-rpc-handler :added "4.1"}
(fact "call-rpc-handler routes rpc args through a named service"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (return
            (adaptor/call-rpc-handler
             nil
             ["db/primary"
              {"input" [{"symbol" "i_message" "type" "text"}]
               "return" "jsonb"
               "schema" "scratch_v0"
               "id" "log_append_public"
               "flags" {}}
              ["hello"]]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"message" "hello"}))

^{:refer xt.db.node.adaptor-base/call-fetch-handler :added "4.1"}
(fact "call-fetch-handler routes fetch args through a named http service"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (promise/x:promise-run)
        (promise/x:promise-then
         (fn [node]
           (substrate/set-service
            node
            "http/client"
            (js-fetch/create
             {:headers {"apikey" (@! (-> local-min/+config+ :api :anon-key))}
              :host (@! (-> local-min/+config+ :api :hostname))
              :port (@! (-> local-min/+config+ :api :port))}))
           (return node)))
        (promise/x:promise-then
         (fn [node]
           (return
            (adaptor/call-fetch-handler
             nil
             ["http/client" {"path" "/auth/v1/health"}]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => 200)

^{:refer xt.db.node.adaptor-base/call-primary-handler :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "call-primary-handler routes rpc args through the live primary impl"
  
  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (return
            (adaptor/call-primary-handler
             nil
             [{"input" [{"symbol" "i_message" "type" "text"}]
               "return" "jsonb"
               "schema" "scratch_v0"
               "id" "log_append_public"
               "flags" {}}
              ["hello"]]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"message" "hello"}))

^{:refer xt.db.node.adaptor-base/create-pull-model :added "4.1"}
(fact "create-pull-model builds a page model spec with local and remote handlers"

  (!.js
   (var spec (adaptor/create-pull-model
              {"caching_id" "db/caching"
               "primary_id" "db/primary"}
              {"pipeline" {}
               "options" {}
               "defaults" {"args" [["Log"]]}}))
   {"has-main" (xt/x:is-function? (xtd/get-in spec ["handler"]))
    "has-remote" (xt/x:is-function? (xtd/get-in spec ["pipeline" "remote" "handler"]))
    "defaults" (. spec ["defaults"])})
  => {"has-main" true
      "has-remote" true
      "defaults" {"args" [["Log"]]}})

^{:refer xt.db.node.adaptor-base/attach-pull-model :added "4.1"}
(fact "attach-pull-model attaches and invokes a pull-view model"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (adaptor/attach-pull-model
            nil
            [{"space_id" "room/a"
              "group_id" "demo"
              "model_id" "custom-view"
              "service" {"caching_id" "db/caching"
                         "primary_id" "db/primary"}}
             {"pipeline" {}
              "options" {}
              "defaults" {"args" [["Log"]]}}]
            nil
            node)
           (var result (-/invoke-attached-model
                        node "room/a" "demo" "custom-view"
                        [["Log"]]))
           (return (repl/notify result))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => [])

^{:refer xt.db.node.adaptor-base/create-tree-view-model :added "4.1"}
(fact "create-tree-view-model builds a page model spec with local and remote handlers"

  (!.js
   (var spec (adaptor/create-tree-view-model
              {"caching_id" "db/caching"
               "primary_id" "db/primary"}
              {"table" "Log"
               "select_entry" {"input" []
                               "view" {"table" "Log"
                                       "type" "select"
                                       "query" {}}}
               "return_entry" {"input" []
                               "view" {"table" "Log"
                                       "type" "return"
                                       "query" ["id" "message"]}}
               "pipeline" {}
               "options" {}
               "defaults" {"select_args" []
                           "return_args" []}}))
   {"has-main" (xt/x:is-function? (xtd/get-in spec ["handler"]))
    "has-remote" (xt/x:is-function? (xtd/get-in spec ["pipeline" "remote" "handler"]))
    "defaults" (. spec ["defaults"])})
  => {"has-main" true
      "has-remote" true
      "defaults" {"select_args" []
                  "return_args" []}})

^{:refer xt.db.node.adaptor-base/attach-tree-view-model :added "4.1"}
(fact "attach-tree-view-model attaches and invokes a tree-view model"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (adaptor/attach-tree-view-model
            nil
            [{"space_id" "room/a"
              "group_id" "demo"
              "model_id" "tree-view"
              "service" {"caching_id" "db/caching"
                         "primary_id" "db/primary"}}
             {"table" "Log"
              "select_entry" {"input" []
                              "view" {"table" "Log"
                                      "type" "select"
                                      "query" {}}}
              "return_entry" {"input" []
                              "view" {"table" "Log"
                                      "type" "return"
                                      "query" ["id" "message"]}}
              "pipeline" {}
              "options" {}
              "defaults" {"select_args" []
                          "return_args" []}}]
            nil
            node)
           (var result (-/invoke-attached-model
                        node "room/a" "demo" "tree-view"
                        [[] []]))
           (return (repl/notify result))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => [])

^{:refer xt.db.node.adaptor-base/create-rpc-model :added "4.1"}
(fact "create-rpc-model builds a page model spec with an rpc handler"

  (!.js
   (var spec (adaptor/create-rpc-model
              "db/primary"
              {"rpc_spec" {"input" [{"symbol" "i_message" "type" "text"}]
                           "return" "jsonb"
                           "schema" "scratch_v0"
                           "id" "log_append_public"
                           "flags" {}}
               "pipeline" {}
               "options" {}
               "defaults" {"fn_args" ["hello"]}}))
   {"has-main" (xt/x:is-function? (xtd/get-in spec ["handler"]))
    "defaults" (. spec ["defaults"])})
  => {"has-main" true
      "defaults" {"fn_args" ["hello"]}})

^{:refer xt.db.node.adaptor-base/attach-rpc-model :added "4.1"}
(fact "attach-rpc-model attaches and invokes an rpc model"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-adaptor-main {"primary" {"type" "postgres"
                                     "defaults" (@! (local-min/+config+ :db))}
                          "caching" {"type" "sqlite"
                                     "defaults" {"filename" ":memory:"}}}
                         -/Schema
                         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (adaptor/attach-rpc-model
            nil
            [{"space_id" "room/a"
              "group_id" "demo"
              "model_id" "rpc-view"
              "service" "db/primary"}
             {"rpc_spec" {"input" [{"symbol" "i_message" "type" "text"}]
                          "return" "jsonb"
                          "schema" "scratch_v0"
                          "id" "log_append_public"
                          "flags" {}}
              "pipeline" {}
              "options" {}
              "defaults" {"fn_args" ["hello"]}}]
            nil
            node)
           (var result (-/invoke-attached-model
                        node "room/a" "demo" "rpc-view"
                        [["hello"]]))
           (return
            (-> result
                (promise/x:promise-then
                 (fn [out]
                   (repl/notify out)))))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"message" "hello"}))

^{:refer xt.db.node.adaptor-base/init-handlers :added "4.1"}
(fact "init-handlers registers the @xt.db/init-adaptor-handler handler"

  (notify/wait-on :js
    (var node (substrate/node-create {"schema" -/Schema
                                      "lookup" -/SchemaLookup
                                      "services" {}}))
    (adaptor/init-handlers node)
    (substrate/set-service node "db/common" {"schema" -/Schema
                                             "lookup" -/SchemaLookup})
    (-> (substrate/request node "room/a" "@xt.db/init-adaptor"
                           [{"primary" {"type" "memory" "defaults" {}}
                             "caching" {"type" "memory" "defaults" {}}}]
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in {"services" {"db/primary" map?
                              "db/caching" map?}}))

^{:refer xt.db.node.adaptor-base/list-substrate-fn :added "4.1"}
(fact "list-substrate-fn lists public vars tagged with :substrate/fn"

  (map first (adaptor/list-substrate-fn 'xt.db.node.adaptor-base))
  => '[attach-pull-model
       attach-rpc-model
       attach-tree-view-model
       call-fetch-handler
       call-rpc-handler
       init-adaptor-handler])