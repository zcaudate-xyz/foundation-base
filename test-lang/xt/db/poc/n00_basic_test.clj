^{:seedgen/skip true}
(ns xt.db.poc.n00-basic-test
  (:use code.test)
  (:require [hara.lang :as l]
            [net.http :as net.http]
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
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.node.kernel-base :as kernel-base]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/refresh-postgrest-schema "scratch_v0" "Log")
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.node-basic-test/init-services :added "4.1"}
(fact "installs db/primary (supabase) and db/caching (sqlite) services on the node"

  (notify/wait-on :js
    (var node (substrate/node-create {"id" "poc-node"}))
    (-> (kernel-base/kernel-init-main
         node
         {"primary" {"type" "supabase"
                     "defaults" (@! local-min/+config-supabase-anon+)}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (repl/notify)))
  => (contains-in
      {"status" "setup", "data" {"caching" {"id" "db/caching", "type" "sqlite", "defaults" {"filename" ":memory:"}}, "primary" map?, "common" {"id" "db/common"}}}))

^{:refer xt.db.poc.node-basic-test/create-page-model :added "4.1"}
(fact "returns a model spec with local and remote handlers"

  (!.js
    (var spec {"handler" (fn [context]
                           (var node (. context ["node"]))
                           (var args (. context ["args"]))
                           (var pull-tree (xt/x:first args))
                           (var caching (substrate/get-service node "db/caching"))
                           (return (impl-common/pull caching pull-tree)))
               "pipeline" {"remote" {"handler" (fn [context]
                                                 (var node (. context ["node"]))
                                                 (var args (. context ["args"]))
                                                 (var pull-tree (xt/x:first args))
                                                 (var primary (substrate/get-service node "db/primary"))
                                                 (return (impl-common/pull-async primary pull-tree)))}}
               "defaults" {"args" [["Log"]]}
               "options" {}})
    spec)
  => (contains-in
      {"pipeline" {"remote" {}}, "options" {}, "defaults" {"args" [["Log"]]}}))

^{:refer xt.db.poc.node-basic-test/install-page-models :added "4.1"
  :setup [(scratch-v0/log-append-public "cached")]}
(fact "attaches db-model-service models to a node space"

  (notify/wait-on :js
    (var node (substrate/node-create {"id" "poc-node"}))
    (-> (kernel-base/kernel-init-main
         node
         {"primary" {"type" "supabase"
                     "defaults" (@! local-min/+config-supabase-anon+)}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (var primary (substrate/get-service node "db/primary"))
           (var caching (substrate/get-service node "db/caching"))
           ;; pull from supabase primary and add to sqlite caching
           (return
            (-> (impl-common/pull-async primary ["Log"])
                (promise/x:promise-then
                 (fn [records]
                   (impl-common/record-add caching "Log" records)
                   (page-core/group-add-attach
                    node nil "page"
                    {"entry" {"handler" (fn [context]
                                          (var node (. context ["node"]))
                                          (var args (. context ["args"]))
                                          (var pull-tree (xt/x:first args))
                                          (var caching (substrate/get-service node "db/caching"))
                                          (return (impl-common/pull caching pull-tree)))
                              "pipeline" {"remote" {"handler" (fn [context]
                                                                (var node (. context ["node"]))
                                                                (var args (. context ["args"]))
                                                                (var pull-tree (xt/x:first args))
                                                                (var primary (substrate/get-service node "db/primary"))
                                                                (return (impl-common/pull-async primary pull-tree)))}}
                              "defaults" {"args" [["Log"]]}
                              "options" {}}})
                   (return node)))))))
        (promise/x:promise-then
         (fn [node]
           (-> (substrate/page-model-update node nil "page" "entry" {})
               (promise/x:promise-then
                (fn [_]
                  (var model-result (page-core/model-ensure node nil "page" "entry"))
                  (var model (xt/x:get-idx model-result 1))
                  (repl/notify (event-model/get-current model nil)))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      [{"id" string?
        "message" "cached"}]))

^{:refer xt.db.poc.node-basic-test/create-page-model.remote :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "remote handler pulls asynchronously from db/primary"

  (notify/wait-on :js
    (var node (substrate/node-create {"id" "poc-node"}))
    (-> (kernel-base/kernel-init-main
         node
         {"primary" {"type" "supabase"
                     "defaults" (@! local-min/+config-supabase-anon+)}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (var primary (substrate/get-service node "db/primary"))
           (var caching (substrate/get-service node "db/caching"))
           ;; pull from supabase primary and add to sqlite caching
           (return
            (-> (impl-common/pull-async primary ["Log"])
                (promise/x:promise-then
                 (fn [records]
                   (impl-common/record-add caching "Log" records)
                   (page-core/group-add-attach
                    node nil "page"
                    {"entry" {"handler" (fn [context]
                                          (var node (. context ["node"]))
                                          (var args (. context ["args"]))
                                          (var pull-tree (xt/x:first args))
                                          (var caching (substrate/get-service node "db/caching"))
                                          (return (impl-common/pull caching pull-tree)))
                              "pipeline" {"remote" {"handler" (fn [context]
                                                                (var node (. context ["node"]))
                                                                (var args (. context ["args"]))
                                                                (var pull-tree (xt/x:first args))
                                                                (var primary (substrate/get-service node "db/primary"))
                                                                (return (impl-common/pull-async primary pull-tree)))}}
                              "defaults" {"args" [["Log"]]}
                              "options" {}}})
                   (return node)))))))
        (promise/x:promise-then
         (fn [node]
           (-> (page-core/model-remote-call node nil "page" "entry" [["Log"]] true)
               (promise/x:promise-then
                (fn [_]
                  (var model-result (page-core/model-ensure node nil "page" "entry"))
                  (var model (xt/x:get-idx model-result 1))
                  (repl/notify (event-model/get-current model nil)))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      [{"id" string?
        "message" "remote"}]))
