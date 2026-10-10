(ns js.react.ext-table-app-test
  (:require [lang.core :as l]
            [clojure.string :as str]
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
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.client-supabase :as client-supabase]
             [js.react.helper-jsdom :as helper]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-table :as ext-table]]})

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

(defn.js append-impl
  []
  (return
   {"actions"
    {"append"
     {"rpc-spec"
      {"input" [{"symbol" "i_message" "type" "text"}]
       "return" "jsonb"
       "schema" "scratch_v0"
       "id" "log_append_public"
       "flags" {}}}}}))

(defn.js log-query
  [message]
  (return
   {"defaultArgs" ["Log"
                   {"where" [{"message" message}]
                    "data" ["id" "message"]}]}))

(defn.js log-row
  [row]
  (return
   (r/createElement "tr" nil
                   (r/createElement "td" nil (. row ["id"]))
                   (r/createElement "td" nil (. row ["message"])))))

(defn.js log-view
  [props]
  (var view (. props ["view"]))
  (var rows (or (ext-model/listenModelOutput view ["main"] {} nil nil) []))
  (return
   (r/createElement "table" {"data-testid" "log-view"}
                   (r/createElement "thead" nil
                                   (r/createElement "tr" nil
                                                   (r/createElement "th" nil "ID")
                                                   (r/createElement "th" nil "Message")))
                   (r/createElement "tbody" nil
                                   (xtd/arr-map rows
                                                (fn [row]
                                                  (return (-/log-row row))))))))

(defn.js append-and-refresh
  [action view]
  (return
   (-> (. action ["init"])
       (promise/x:promise-then
        (fn [_]
          (return (ext-model/refresh-model view {})))))))

(defn.js log-app
  [props]
  (var message (. props ["message"]))
  (var node (substrate/node-create {"id" "log-app-node"}))
  (var context (-/live-context node))
  (var action
       (ext-table/useActionView
        (-/append-impl)
        "append"
        {"defaultArgs" [message]}
        context
        {}))
  (var view
       (ext-table/useRemoteView
        (-/table-impl)
        "data"
        (-/log-query message)
        context
        {}))
  (xt/x:set-key (. props ["state"]) "node" node)
  (xt/x:set-key (. props ["state"]) "action" action)
  (xt/x:set-key (. props ["state"]) "view" view)
  (var append (fn [] (return (-/append-and-refresh action view))))
  (xt/x:set-key (. props ["state"]) "append" append)
  (return
   (r/createElement "section" nil
                   (r/createElement "button"
                                    {"data-testid" "append-log"
                                     "onClick" append}
                                    "Append")
                   (r/createElement -/log-view {"view" view}))))

^{:refer js.react.ext-table-app-test/append-impl :added "4.1"}
(fact "describes the public append RPC"
  (!.js
    (return (-/append-impl)))
  => {"actions"
      {"append"
       {"rpc-spec"
        {"input" [{"symbol" "i_message" "type" "text"}]
         "return" "jsonb"
         "schema" "scratch_v0"
         "id" "log_append_public"
         "flags" {}}}}})

^{:refer js.react.ext-table-app-test/log-query :added "4.1"}
(fact "filters the Log view to the appended message"
  (!.js
    (return (-/log-query "message")))
  => {"defaultArgs" ["Log"
                      {"where" [{"message" "message"}]
                       "data" ["id" "message"]}]})

^{:refer js.react.ext-table-app-test/log-row :added "4.1"}
(fact "renders a Log row"
  (helper-source/test
   (fn [_]
     (return (r/createElement -/log-row
                              {"id" "row-id" "message" "row-message"})))
   {}
   (fn [_ document _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><tr><td>row-id</td><td>row-message</td></tr></div>")

^{:refer js.react.ext-table-app-test/log-app :added "4.1"}
(fact "refreshes the mounted Log view after log-append-public completes"
  (let [message "ext-table-app/log-append-refresh"]
    (try
      (pg/t:delete scratch-v0/Log
                  {:where {:message message}})
      (helper-source/test
       (fn [props]
         (return (-/log-app props)))
       {"message" message}
       (fn [props document env]
         (var append (. props ["state"] ["append"]))
         (var view (. props ["state"] ["view"]))
         (-> (append)
             (promise/x:promise-then
              (fn [_]
                (return
                 (repl/notify
                  {"root-id" (. env ["root" "id"])
                   "html" document.body.innerHTML
                   "rows" (event-model/get-current view nil)}))))
             (promise/x:promise-finally
              (fn []
                (return
                 (client-base/kernel-teardown
                  (. props ["state"] ["node"])
                  "db/primary"
                  {}))))))
       => (contains-in
           {"root-id" "root"
            "html" (fn [html]
                     (and (str/includes? html "data-testid=\"append-log\"")
                          (str/includes? html "data-testid=\"log-view\"")))
            "rows" [{"id" string? "message" message}]}))
      (finally
        (pg/t:delete scratch-v0/Log
                    {:where {:message message}})))))
