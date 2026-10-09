(ns js.react.ext-page-app-test
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
             [xt.db.node.kernel-base :as kernel]
             [js.react.helper-jsdom :as helper]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-page-app :as ext-page-app]]})

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



(def test-js_react_ext_page_app__useListView_live true)

^{:refer js.react.ext-page-app/listen-sync :added "4.1"}
(fact "accepts callback sources, listener objects, and missing sources"
  (!.js
    (var seen [])
    (var callback (fn [value] (. seen (push value))))
    (var cleanupFn
         (ext-page-app/listen-sync
          (fn [listener]
            (listener "function")
            (return "function-cleanup"))
          callback))
    (var cleanupObj
         (ext-page-app/listen-sync
          {"listen" (fn [listener]
                       (listener "object")
                       (return "object-cleanup"))}
          callback))
    (return {"seen" seen
             "function-cleanup" cleanupFn
             "object-cleanup" cleanupObj
             "missing" (ext-page-app/listen-sync {} callback)}))
  => {"seen" ["function" "object"]
      "function-cleanup" "function-cleanup"
      "object-cleanup" "object-cleanup"
      "missing" nil})

^{:refer js.react.ext-page-app/makeActionView :added "4.1"}
(fact "builds an action-backed view"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/rpc-call"
     (fn [space args request node]
       (return {"rpc" (. (xt/x:second args) ["id"])
                "value" (xt/x:get-idx args (xt/x:offset 2))}))
     nil)
    (var impl
         {"actions"
          {"append"
           {"rpc-spec" {"id" "log_append"}
            }}})
    (var view (ext-page-app/makeActionView
               impl
               "append"
               {"defaultArgs" ["hello"]}
               {"node" node}))
    (-> (ext-model/refresh-view view {})
        (promise/x:promise-then
         (fn [_]
           (return (repl/notify
                    {"type" (. view ["::"])
                     "output" (event-model/get-current view nil)}))))))
  => {"type" "event.model"
      "output" {"rpc" "log_append" "value" ["hello"]}})

^{:refer js.react.ext-page-app/useActionView :added "4.1"}
(fact "refreshes an action view inside jsdom"
  (helper-source/test
   (fn [props]
     (var view (ext-page-app/useActionView
                (. props ["impl"])
                "save"
                {"defaultArgs" ["initial"]}
                (. props ["context"])
                {}))
     (var output (ext-model/listenView view "output" nil nil nil))
     (xt/x:set-key props "view" view)
     (return (r/createElement "span" nil
                              (JSON.stringify (or output {})))))
   (fn [_]
     (var node (substrate/node-create {}))
     (substrate/register-handler
      node
      "@xt.db/rpc-call"
      (fn [space args request node]
        (return {"message" (xt/x:first (xt/x:get-idx args (xt/x:offset 2)))}))
      nil)
     (return {"impl" {"actions" {"save" {"rpc-spec" {"id" "save"}}}}
              "context" {"node" node}}))
   (fn [props document env]
     (return
      (helper/await-dom
       (fn []
         (return {"html" document.body.innerHTML
                  "output" (event-model/get-current (. props ["view"]) nil)}))))))
  => {"html" "<div id=\"root\"><span>{\"message\":\"initial\"}</span></div>"
      "output" {"message" "initial"}})

^{:refer js.react.ext-page-app/makeRemoteView :added "4.1"}
(fact "builds a remote table view"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (var spec (xt/x:get-idx args 1))
       (var params (xt/x:get-idx spec 1))
       (if (xt/x:get-key params "where")
         (return [{"id" "remote-1" "message" "from-remote"}])
         (return [{"id" "remote-1"}])))
     nil)
    (var view (ext-page-app/makeRemoteView
               (-/table-impl)
               "data"
               {"defaultArgs" []}
               {"node" node}))
    (-> (ext-model/refresh-view view {})
        (promise/x:promise-then
         (fn [_]
           (return (repl/notify (event-model/get-current view nil)))))))
  => [{"id" "remote-1" "message" "from-remote"}])

^{:refer js.react.ext-page-app/useRemoteView :added "4.1"}
(fact "refreshes a remote table view inside jsdom"
  (helper-source/test
   (fn [props]
     (var view (ext-page-app/useRemoteView
                (. props ["impl"])
                "data"
                {"defaultArgs" []}
                (. props ["context"])
                {}))
     (var output (ext-model/listenView view "output" nil nil nil))
     (xt/x:set-key props "view" view)
     (return (r/createElement "span" nil
                              (JSON.stringify (or output {})))))
   (fn [_]
     (var node (substrate/node-create {}))
     (substrate/register-handler
      node
      "@xt.db/pull-call"
      (fn [space args request node]
        (var spec (xt/x:get-idx args 1))
        (var params (xt/x:get-idx spec 1))
        (if (xt/x:get-key params "where")
          (return [{"id" "remote-1" "message" "from-remote"}])
          (return [{"id" "remote-1"}])))
      nil)
     (return {"impl" (-/table-impl)
              "context" {"node" node}}))
   (fn [props document env]
     (return
      (helper/await-dom
       (fn []
         (return {"html" document.body.innerHTML
                  "output" (event-model/get-current (. props ["view"]) nil)}))))))
  => {"html" "<div id=\"root\"><span>[{\"id\":\"remote-1\",\"message\":\"from-remote\"}]</span></div>"
      "output" [{"id" "remote-1" "message" "from-remote"}]})

^{:refer js.react.ext-page-app/makeListView :added "4.1"}
(fact "runs cached, remote, and sync table pipelines"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node
     "@xt.db/pull-cached"
     (fn [space args request node]
       (var spec (xt/x:get-idx args 1))
       (var params (xt/x:get-idx spec 1))
       (if (xt/x:get-key params "where")
         (return [{"id" "cached-1" "message" "cached-row"}])
         (return [{"id" "cached-1"}])))
     nil)
    (substrate/register-handler
     node
     "@xt.db/pull-call"
     (fn [space args request node]
       (var spec (xt/x:get-idx args 1))
       (var params (xt/x:get-idx spec 1))
       (if (xt/x:get-key params "where")
         (return [{"id" "primary-1" "message" "primary-row"}])
         (return [{"id" "primary-1"}])))
     nil)
    (var view (ext-page-app/makeListView
               (-/table-impl)
               "data"
               {"defaultArgs" []}
               {"node" node}))
    (-> (ext-model/refresh-view view {})
        (promise/x:promise-then
         (fn [_]
           (return (ext-model/refresh-view-remote view true {}))))
        (promise/x:promise-then
         (fn [_]
           (return (ext-model/refresh-view-sync view true {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (repl/notify
             {"main" (event-model/get-current view nil)
              "remote" (event-model/get-current view "remote")
              "sync" (event-model/get-current view "sync")}))))))
  => {"main" [{"id" "cached-1" "message" "cached-row"}]
      "remote" [{"id" "primary-1" "message" "primary-row"}]
      "sync" [{"id" "primary-1" "message" "primary-row"}]})

^{:refer js.react.ext-page-app/useListView
  :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)
          (pg/t:insert scratch-v0/Log
                       {:id "00000000-0000-0000-0000-000000000001"
                        :message "jsdom-live"}
                       {:track {}})]}
(fact "renders a live Supabase-backed view in jsdom"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-supabase node)
        (promise/x:promise-then
         (fn []
           (var Probe
                (fn []
                  (return (r/createElement "span" nil "live"))))
           (var env (helper/setup {}))
           (return
            (. (helper/render env Probe {})
               (then (fn [_]
                       (var html document.body.innerHTML)
                       (helper/teardown env)
                       (return (repl/notify html))))))))))
  => "<div id=\"root\"><span>live</span></div>")

^{:refer js.react.ext-page-app/makeSingleView :added "4.1"}
(fact "TODO")

^{:refer js.react.ext-page-app/useSingleView :added "4.1"}
(fact "TODO")

^{:refer js.react.ext-page-app/useActions :added "4.1"}
(fact "TODO")

