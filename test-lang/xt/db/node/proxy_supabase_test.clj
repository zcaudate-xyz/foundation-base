(ns xt.db.node.proxy-supabase-test
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

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.kernel-base :as adaptor]
             [xt.db.node.kernel-supabase :as kernel-supabase]
             [xt.db.node.proxy-supabase :as proxy-supabase]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.db.system.main :as system-main]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.db.system.impl-supabase-session :as session]
             [xt.db.helpers.data-main-test :as sample]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.net.http-fetch :as fetch]
             [xt.net.addon-supabase :as addon]
             [js.net.http-fetch :as js-fetch]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(defn.js node-init-supabase
  []
  (var node (substrate/node-create {}))
  (return
   (adaptor/init-base-handler
    nil
    [{"primary" {"type" "supabase"
                 "defaults" (@! local-min/+config-supabase-anon+)}
      "caching" {"type" "sqlite"
                 "defaults" {"filename" ":memory:"}}}
     -/Schema
     -/SchemaLookup]
    nil
    node)))

(defn.js server-node
  "creates a server node with supabase adaptor handlers and a local-min service"
  {:added "4.1"}
  []
  (var node (substrate/node-create {"id" "proxy-supabase-server"}))
  (kernel-supabase/init-handlers node)
  (var client (js-fetch/create
               {"host" (@! (-> local-min/+config+ :api :hostname))
                "port" (@! (-> local-min/+config+ :api :port))
                "secured" false
                "apikey" (@! (-> local-min/+config+ :api :anon-key))}
               (addon/middleware-supabase)))
  (var impl (system-main/create-impl
             "supabase"
             (xt/x:get-key client "defaults")
             nil
             nil))
  (session/set-session impl nil)
  (substrate/set-service node "auth/supabase" impl)
  (return node))

(defn.js client-node
  "creates a client node with supabase proxy handlers"
  {:added "4.1"}
  []
  (var node (substrate/node-create {"id" "proxy-supabase-client"}))
  (proxy-supabase/init-proxy-handlers node)
  (return node))

(defn.js link-nodes
  "links two nodes with an in-memory transport wire"
  {:added "4.1"}
  [server client]
  (var wire (transport-memory/memory-pair {"left_id" "client"
                                           "right_id" "server"}))
  (return
   (promise/x:promise-all
    [(substrate/attach-transport
      client
      "server"
      (transport-memory/text-endpoint (. wire ["left"])))
     (substrate/attach-transport
      server
      "client"
      (transport-memory/text-endpoint (. wire ["right"])))])))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.node.proxy-supabase/init-proxy-handlers :added "4.1"}
(fact "init-proxy-handlers registers all supabase proxy actions on the node"

  (!.js
   (var node (-/client-node))
   (var handlers (xt/x:get-key node "handlers"))
   (and (xt/x:not-nil? (xt/x:get-key handlers "@xt.supabase/sign-up"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.supabase/sign-in"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.supabase/sign-out"))
        (xt/x:not-nil? (xt/x:get-key handlers "@xt.supabase/health"))
        (== 27 (xt/x:len (xtd/obj-keys handlers)))))
  => true)

^{:refer xt.db.node.proxy-supabase-test/server-health-direct :added "4.1"}
(fact "server node can call local-min health directly"

  (notify/wait-on :js
    (var server (-/server-node))
    (-> (substrate/request server
                           nil
                           "@xt.supabase/health"
                           ["auth/supabase" {}]
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in {"name" "GoTrue"}))

^{:refer xt.db.node.proxy-supabase-test/proxy-health :added "4.1"}
(fact "proxy handlers forward health check to local-min through the server"

  (notify/wait-on :js
    (var server (-/server-node))
    (var client (-/client-node))
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client "server")
           (return (substrate/request client
                                      nil
                                      "@xt.supabase/health"
                                      ["auth/supabase" {}]
                                      {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in {"name" "GoTrue"}))

^{:refer xt.db.node.proxy-supabase-test/proxy-signed-in? :added "4.1"}
(fact "proxy handlers forward signed-in? query to local-min through the server"

  (notify/wait-on :js
    (var server (-/server-node))
    (var client (-/client-node))
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client "server")
           (return (substrate/request client
                                      nil
                                      "@xt.supabase/signed-in?"
                                      ["auth/supabase"]
                                      {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => false)

^{:refer xt.db.node.proxy-supabase-test/proxy-current-session :added "4.1"}
(fact "proxy handlers forward current-session queries to local-min through the server"

  (notify/wait-on :js
    (var server (-/server-node))
    (var client (-/client-node))
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client "server")
           (return (substrate/request client
                                      nil
                                      "@xt.supabase/current-session"
                                      ["auth/supabase"]
                                      {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => nil)

^{:refer xt.db.node.proxy-supabase-test/proxy-sign-up :added "4.1"}
(fact "proxy handlers forward sign-up to local-min through the server"

  (notify/wait-on :js
    (var email (xt/x:cat "proxy-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var server (-/server-node))
    (var client (-/client-node))
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client "server")
           (return (substrate/request client
                                      nil
                                      "@xt.supabase/sign-up"
                                      ["auth/supabase" {"email" email "password" "secret123"} {}]
                                      {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in {"access_token" string? "refresh_token" string? "user" {"email" string?}}))

^{:refer xt.db.node.proxy-supabase-test/proxy-rpc-call :added "4.1"}
(fact "proxy handlers forward rpc-call to local-min through the server"

  (notify/wait-on :js
    (var server (-/server-node))
    (var client (-/client-node))
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client "server")
           (return (substrate/request client
                                      nil
                                      "@xt.supabase/rpc-call"
                                      ["auth/supabase"
                                       "ping"
                                       {}
                                       {"headers" {"Accept-Profile" "scratch_v0"
                                                   "Content-Profile" "scratch_v0"}}]
                                      {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => "pong")

^{:refer xt.db.node.proxy-supabase-test/proxy-query-table :added "4.1"}
(fact "proxy handlers forward query-table to local-min through the server"
  {:setup [(scratch-v0/log-append-public "hello-proxy")]}

  (notify/wait-on :js
    (var server (-/server-node))
    (var client (-/client-node))
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client "server")
           (return (substrate/request client
                                      nil
                                      "@xt.supabase/query-table"
                                      ["auth/supabase"
                                       "Log"
                                       "select=*"
                                       {"headers" {"Accept-Profile" "scratch_v0"}}]
                                      {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in [{"message" "hello-proxy", "author_id" nil, "id" string?}]))
