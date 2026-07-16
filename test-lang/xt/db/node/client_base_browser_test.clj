(ns xt.db.node.client-base-browser-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [postgres.core.supabase :as s]))

;;
;; POSTGRES RUNTIME
;;

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

;;
;; JS RUNTIME (chromedriver)
;;

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.db.node.client-base :as client]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.db.node.runtime :as runtime]
             [xt.db.system.main :as main]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.system.impl-common :as impl-common]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js CONFIG
  {"postgres"  {"type" "postgres"
                "defaults" (@! (local-min/+config+ :db))}
   "sqlite"    {"type" "sqlite"
                "defaults" {"filename" ":memory:"}}
   "supabase"  {"type" "supabase"
                "defaults" {"host"    (@! (-> local-min/+config+ :api :hostname))
                            "port"    (@! (-> local-min/+config+ :api :port))
                            "secured" false
                            "basepath" ""
                            "apikey"  (@! (-> local-min/+config+ :api :anon-key))
                            "token"   (@! (-> local-min/+config+ :api :service-key))}}
   "memory"    {"type" "memory" "defaults" {}}})

(def ^:private +notify-url+
  (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/"))

(def ^:private +sharedworker-script+
  (runtime/sharedworket-init-string))

(defn.js store-sharedworker-url
  "Creates a fresh blob URL for the SharedWorker kernel script and stores it
   in localStorage so that other tabs on the same origin can connect to the
   same worker instance."
  {:added "4.1"}
  []
  (var url (browser-transport/blob-url (@! +sharedworker-script+)))
  (. (!:G localStorage) (setItem "__client_base_browser_worker_url__" url))
  (return url))

(defn.js make-shared-source
  "Returns a SharedWorker source that reuses the URL stored by
   store-sharedworker-url. The source is suitable for
   runtime/sharedworker-connect and browser-transport/connect-sharedworker."
  {:added "4.1"}
  []
  (var url (. (!:G localStorage) (getItem "__client_base_browser_worker_url__")))
  (return
   {"create_fn"
    (fn [listener]
      (var shared (new SharedWorker url {"type" "module"}))
      (var port (. shared ["port"]))
      (. port (start))
      (. port
         (addEventListener
          "message"
          (fn [e]
            (listener (. e ["data"])))
          false))
      (return port))}))

;;
;; GLOBAL SETUP
;;

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")
          (chromedriver/goto +notify-url+ 4000)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

;;
;; TESTS
;;

^{:refer xt.db.node.client-base-browser-test/sync-cached
  :added "4.1"}
(fact "sync-cached applies a db/sync payload directly and through a SharedWorker"
  {:setup [(def +logs+ [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
                         "message" "hello"}
                        {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
                         "message" "world"}])
           (-/store-sharedworker-url)
           (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
           (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))
           (chromedriver/tab-switch (l/rt :js) +tab-b+)
           (chromedriver/goto +notify-url+ 4000)
           (chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})]}

  ;;
  ;; TAB A: local kernel
  ;;
  (def sync-direct-result
    (chromedriver/with-tab (l/rt :js) +tab-a+
      (notify/wait-on [:js 20000]
        (var node (substrate/node-create {"id" "sync-cached-direct"}))
        (kernel-base/init-handlers node)
        (-> (client/kernel-init node
                                {"primary" (. -/CONFIG ["supabase"])
                                 "caching" (. -/CONFIG ["memory"])}
                                -/Schema
                                -/SchemaLookup
                                {})
            (promise/x:promise-then
             (fn []
               (return
                (client/sync-cached node
                                    "db/primary"
                                    {"db/sync" {"Log" (@! +logs+)}}
                                    {}))))
            (promise/x:promise-then
             (fn []
               (return
                (client/pull-cached node "db/primary" ["Log"]))))
            (promise/x:promise-then
             (fn [res] (repl/notify res)))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                (:? err err {"error" "nil error"}))))))))

  ;;
  ;; TAB B: SharedWorker kernel
  ;;
  (def sync-shared-result
    (chromedriver/with-tab (l/rt :js) +tab-b+
      (notify/wait-on [:js 20000]
        (var node (substrate/node-create {"id" "sync-cached-shared"}))
        (-> (runtime/sharedworker-connect node
                                          {"primary" (. -/CONFIG ["supabase"])
                                           "caching" (. -/CONFIG ["memory"])}
                                          -/Schema
                                          -/SchemaLookup
                                          (-/make-shared-source)
                                          nil)
            (promise/x:promise-then
             (fn []
               (return
                (client/sync-cached node
                                    "db/primary"
                                    {"db/sync" {"Log" (@! +logs+)}}
                                    {}))))
            (promise/x:promise-then
             (fn []
               (return
                (client/pull-cached node "db/primary" ["Log"]))))
            (promise/x:promise-then
             (fn [res] (repl/notify res)))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                (:? err err {"error" "nil error"}))))))))

  (when-let [tab-b (resolve '+tab-b+)]
    (chromedriver/tab-close (l/rt :js) @tab-b))
  (when-let [tab-a (resolve '+tab-a+)]
    (chromedriver/tab-switch (l/rt :js) @tab-a {:bootstrap false}))

  sync-direct-result
  => (contains-in
      [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
        "message" "hello"}
       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
        "message" "world"}])

  sync-shared-result
  => (contains-in
      [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
        "message" "hello"}
       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
        "message" "world"}]))

^{:refer xt.db.node.client-base-browser-test/dataview-attach-model
  :added "4.1"}
(fact "attaches and invokes a dataview model directly and through a SharedWorker"
  {:setup [(pg/t:delete scratch-v0/Log)
           (-/store-sharedworker-url)
           (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
           (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))
           (chromedriver/tab-switch (l/rt :js) +tab-b+)
           (chromedriver/goto +notify-url+ 4000)
           (chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})]}

  ;;
  ;; TAB A: local kernel
  ;;
  (def dataview-direct-result
    (chromedriver/with-tab (l/rt :js) +tab-a+
      (notify/wait-on [:js 30000]
        (var node (substrate/node-create {"id" "dataview-attach-direct"}))
        (kernel-base/init-handlers node)
        (-> (client/kernel-init node
                                {"primary" (. -/CONFIG ["supabase"])
                                 "caching" (. -/CONFIG ["memory"])}
                                -/Schema
                                -/SchemaLookup
                                {})
            (promise/x:promise-then
             (fn []
               (return
                (client/rpc-call node
                                 "db/primary"
                                 {"input" [{"symbol" "i_message" "type" "text"}]
                                  "return" "jsonb"
                                  "schema" "scratch_v0"
                                  "id" "log_append_public"
                                  "flags" {}}
                                 ["hello-dataview-attach"]
                                 {}))))
            (promise/x:promise-then
             (fn []
               (return
                (client/dataview-attach-model node
                                              "db/primary"
                                              {"space_id" "room/a"
                                               "group_id" "demo"
                                               "model_id" "dataview-view"}
                                              {"table" "Log"
                                               "select_entry" {"input" []
                                                               "view" {"table" "Log"
                                                                       "type" "select"
                                                                       "query" {}}}
                                               "return_entry" {"input" []
                                                               "view" {"table" "Log"
                                                                       "type" "return"
                                                                       "query" ["id" "message"]}}}
                                              {"pipeline" {}
                                               "options" {}
                                               "defaults" {"select_args" []
                                                           "return_args" []}}
                                              {}))))
            (promise/x:promise-then
             (fn []
               (return (page-core/model-refresh-remote node "room/a" "demo" "dataview-view" nil))))
            (promise/x:promise-then
             (fn [out]
               (var caching (kernel-base/get-caching-impl node "db/primary"))
               (return
                [out
                 (impl-common/pull caching ["Log"])])))
            (promise/x:promise-then
             (fn [res] (repl/notify res)))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                (:? err err {"error" "nil error"}))))))))

  ;;
  ;; TAB B: SharedWorker kernel
  ;;
  (def dataview-shared-result
    (chromedriver/with-tab (l/rt :js) +tab-b+
      (notify/wait-on [:js 30000]
        (var node (substrate/node-create {"id" "dataview-attach-shared"}))
        (-> (runtime/sharedworker-connect node
                                          {"primary" (. -/CONFIG ["supabase"])
                                           "caching" (. -/CONFIG ["memory"])}
                                          -/Schema
                                          -/SchemaLookup
                                          (-/make-shared-source)
                                          nil)
            (promise/x:promise-then
             (fn []
               (return
                (client/rpc-call node
                                 "db/primary"
                                 {"input" [{"symbol" "i_message" "type" "text"}]
                                  "return" "jsonb"
                                  "schema" "scratch_v0"
                                  "id" "log_append_public"
                                  "flags" {}}
                                 ["hello-dataview-attach"]
                                 {}))))
            (promise/x:promise-then
             (fn []
               (return
                (client/dataview-attach-model node
                                              "db/primary"
                                              {"space_id" "room/a"
                                               "group_id" "demo"
                                               "model_id" "dataview-view"}
                                              {"table" "Log"
                                               "select_entry" {"input" []
                                                               "view" {"table" "Log"
                                                                       "type" "select"
                                                                       "query" {}}}
                                               "return_entry" {"input" []
                                                               "view" {"table" "Log"
                                                                       "type" "return"
                                                                       "query" ["id" "message"]}}}
                                              {"pipeline" {}
                                               "options" {}
                                               "defaults" {"select_args" []
                                                           "return_args" []}}
                                              {}))))
            (promise/x:promise-then
             (fn []
               (return (page-proxy/group-open-proxy node "room/a" "demo" {}))))
            (promise/x:promise-then
             (fn []
               (return (page-proxy/model-proxy-call node "room/a" "demo" "dataview-view" [{"select_args" [] "return_args" []}] true {}))))
            (promise/x:promise-then
             (fn [_]
               (var group (page-core/group-get node "room/a" "demo"))
               (var model (xtd/get-in group ["models" "dataview-view"]))
               (return
                {"output" (event-model/get-current model nil)})))
            (promise/x:promise-then
             (fn [res] (repl/notify res)))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                (:? err err {"error" "nil error"}))))))))

  (when-let [tab-b (resolve '+tab-b+)]
    (chromedriver/tab-close (l/rt :js) @tab-b))
  (when-let [tab-a (resolve '+tab-a+)]
    (chromedriver/tab-switch (l/rt :js) @tab-a {:bootstrap false}))

  dataview-direct-result
  => (contains-in
      [{"path" ["demo" "dataview-view"]
        "remote" [true [{"message" "hello-dataview-attach", "author_id" nil, "id" string?}]]
        "post" [false], "::" "model.run", "pre" [false]}
       [{"message" "hello-dataview-attach", "author_id" nil, "id" string?}]])

  dataview-shared-result
  => (contains-in
      {"output" [{"message" "hello-dataview-attach", "id" string?}]}))

^{:refer xt.db.node.client-base-browser-test/pull-attach-model
  :added "4.1"}
(fact "attaches and invokes a pull-view model directly and through a SharedWorker"
  {:setup [(pg/t:delete scratch-v0/Log)
           (-/store-sharedworker-url)
           (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
           (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))
           (chromedriver/tab-switch (l/rt :js) +tab-b+)
           (chromedriver/goto +notify-url+ 4000)
           (chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})]}

  ;;
  ;; TAB A: local kernel
  ;;
  (def pull-direct-result
    (chromedriver/with-tab (l/rt :js) +tab-a+
      (notify/wait-on [:js 30000]
        (var node (substrate/node-create {"id" "pull-attach-direct"}))
        (kernel-base/init-handlers node)
        (-> (client/kernel-init node
                                {"primary" (. -/CONFIG ["supabase"])
                                 "caching" (. -/CONFIG ["memory"])}
                                -/Schema
                                -/SchemaLookup
                                {})
            (promise/x:promise-then
             (fn []
               (return
                (client/pull-attach-model node
                                          "db/primary"
                                          {"space_id" "room/a"
                                           "group_id" "demo"
                                           "model_id" "pull-view"}
                                          ["Log"]
                                          {"pipeline" {}
                                           "options" {}
                                           "defaults" {"args" []
                                                       "output" {}}}
                                          {}))))
            (promise/x:promise-then
             (fn []
               (return
                (client/rpc-call node
                                 "db/primary"
                                 {"input" [{"symbol" "i_message" "type" "text"}]
                                  "return" "jsonb"
                                  "schema" "scratch_v0"
                                  "id" "log_append_public"
                                  "flags" {}}
                                 ["hello-pull-attach"]
                                 {}))))
            (promise/x:promise-then
             (fn []
               (return (page-core/model-refresh-remote node "room/a" "demo" "pull-view" nil))))
            (promise/x:promise-then
             (fn [res] (repl/notify res)))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                (:? err err {"error" "nil error"}))))))))

  ;;
  ;; TAB B: SharedWorker kernel
  ;;
  (def pull-shared-result
    (chromedriver/with-tab (l/rt :js) +tab-b+
      (notify/wait-on [:js 30000]
        (var node (substrate/node-create {"id" "pull-attach-shared"}))
        (-> (runtime/sharedworker-connect node
                                          {"primary" (. -/CONFIG ["supabase"])
                                           "caching" (. -/CONFIG ["memory"])}
                                          -/Schema
                                          -/SchemaLookup
                                          (-/make-shared-source)
                                          nil)
            (promise/x:promise-then
             (fn []
               (return
                (client/pull-attach-model node
                                          "db/primary"
                                          {"space_id" "room/a"
                                           "group_id" "demo"
                                           "model_id" "pull-view"}
                                          ["Log"]
                                          {"pipeline" {}
                                           "options" {}
                                           "defaults" {"args" []
                                                       "output" {}}}
                                          {}))))
            (promise/x:promise-then
             (fn []
               (return
                (client/rpc-call node
                                 "db/primary"
                                 {"input" [{"symbol" "i_message" "type" "text"}]
                                  "return" "jsonb"
                                  "schema" "scratch_v0"
                                  "id" "log_append_public"
                                  "flags" {}}
                                 ["hello-pull-attach"]
                                 {}))))
            (promise/x:promise-then
             (fn []
               (return (page-proxy/group-open-proxy node "room/a" "demo" {}))))
            (promise/x:promise-then
             (fn []
               (return (page-proxy/model-proxy-call node "room/a" "demo" "pull-view" [] true {}))))
            (promise/x:promise-then
             (fn [_]
               (var group (page-core/group-get node "room/a" "demo"))
               (var model (xtd/get-in group ["models" "pull-view"]))
               (return
                {"output" (event-model/get-current model nil)})))
            (promise/x:promise-then
             (fn [res] (repl/notify res)))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                (:? err err {"error" "nil error"}))))))))

  (when-let [tab-b (resolve '+tab-b+)]
    (chromedriver/tab-close (l/rt :js) @tab-b))
  (when-let [tab-a (resolve '+tab-a+)]
    (chromedriver/tab-switch (l/rt :js) @tab-a {:bootstrap false}))

  pull-direct-result
  => (contains-in
      {"path" ["demo" "pull-view"]
       "remote" [true [{"message" "hello-pull-attach", "author_id" nil, "id" string?}]]
       "post" [false], "::" "model.run", "pre" [false]})

  pull-shared-result
  => (contains-in
      {"output" [{"message" "hello-pull-attach", "id" string?}]}))

