(ns js.react.ext-page-app-test
  (:require [lang.core :as l]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [postgres.core.supabase :as s])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.db.node.kernel-base :as kernel]
             [js.react.helper-jsdom :as helper]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-page-app :as ext-page-app]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.react.ext-page-app/makeListView :added "4.1"}
(fact "exports the list-view constructor"
  (!.js
   (typeof ext-page-app/makeListView))
  => "function")

^{:refer js.react.ext-page-app/useListView :added "4.1"}
(fact "mounts a React component through the jsdom helper"
  (helper/wait-on
   (fn []
     (return (r/createElement "span" nil "ready")))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>ready</span></div>")

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
          (l/rt:scaffold-imports :js)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)]
  :teardown [(l/rt:stop)]})

 (def test-js_react_ext_page_app__useListView_live true)

^{:id test-js_react_ext_page_app__useListView_live
  :refer js.react.ext-page-app/useListView
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
