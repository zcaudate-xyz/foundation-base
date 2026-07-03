(ns xt.db.node.client-base-tabs-test
  "DIRECT/SHAREDWORKER parity for xt.db.node.client-base across chromedriver tabs.

   Mirrors xt.db.node.client-base-test but the PROXY variant is replaced by a
   SharedWorker kernel accessed from a second chromedriver tab. The local-min
   Supabase scaffold provides the postgres/supabase backend.

   Note: attach-model parity tests (rpc-attach-model, pull-attach-model,
   dataview-attach-model) are omitted because the model specs produced by
   xt.db.node.kernel-base contain functions that cannot be cloned across a
   SharedWorker MessagePort, preventing proxy group creation from working in
   this configuration."
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]))

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
             [xt.db.node.client-base :as client]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.db.node.runtime :as runtime]
             [xt.db.system.main :as main]
             [xt.substrate :as substrate]
             [xt.substrate.transport-browser :as browser-transport]
             [js.worker.link :as worker-link]]})

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

;;
;; SHAREDWORKER TAB HELPERS
;;

(defn.js store-sharedworker-url
  "Creates a fresh blob URL for the SharedWorker kernel script and stores it
   in localStorage so that other tabs on the same origin can connect to the
   same worker instance."
  {:added "4.1"}
  []
  (var url (worker-link/make-blob-url (@! +sharedworker-script+)))
  (. (!:G localStorage) (setItem "__client_base_tabs_worker_url__" url))
  (return url))

(defn.js make-shared-source
  "Returns a SharedWorker source that reuses the URL stored by
   store-sharedworker-url. The source is suitable for
   runtime/sharedworker-connect and browser-transport/connect-sharedworker."
  {:added "4.1"}
  []
  (var url (. (!:G localStorage) (getItem "__client_base_tabs_worker_url__")))
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

(defn.js init-kernel
  "DIRECT variant: kernel handlers run in the same JS process."
  {:added "4.1"}
  [node primary caching]
  (kernel-base/init-handlers node)
  (return
   (client/kernel-init node
                       {"primary" (. -/CONFIG [primary])
                        "caching" (. -/CONFIG [caching])}
                       -/Schema
                       -/SchemaLookup
                       {})))

(defn.js init-sharedworker
  "SHAREDWORKER variant: kernel handlers run in a SharedWorker accessed from
   the current tab. Proxy handlers are installed on the local client node."
  {:added "4.1"}
  [client primary caching]
  (return
   (runtime/sharedworker-connect client
                                 {"primary" (. -/CONFIG [primary])
                                  "caching" (. -/CONFIG [caching])}
                                 -/Schema
                                 -/SchemaLookup
                                 (-/make-shared-source)
                                 nil)))

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
;; OPERATION HELPERS
;;
;; Each helper returns a promise that performs the same client-base operation
;; as in client-base-test. The `mode` argument selects between DIRECT (local
;; kernel) and SHAREDWORKER (SharedWorker kernel).
;;

(defn.js kernel-init-ops
  {:added "4.1"}
  [mode]
  (var node (substrate/node-create {"id" (xt/x:cat "kernel-init-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
      (promise/x:promise-then
       (fn []
         (return
          (client/kernel-init node
                              {"primary" (. -/CONFIG ["supabase"])
                               "caching" (. -/CONFIG ["memory"])}
                              -/Schema
                              -/SchemaLookup
                              {})))))))

(defn.js kernel-setup-ops
  {:added "4.1"}
  [mode]
  (var node (substrate/node-create {"id" (xt/x:cat "kernel-setup-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
      (promise/x:promise-then
       (fn []
         (return
          (client/kernel-setup node
                               {"primary" (. -/CONFIG ["supabase"])
                                "caching" (. -/CONFIG ["memory"])}
                               -/Schema
                               -/SchemaLookup
                               {})))))))

(defn.js kernel-teardown-ops
  {:added "4.1"}
  [mode]
  (var node (substrate/node-create {"id" (xt/x:cat "kernel-teardown-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
      (promise/x:promise-then
       (fn []
         (return
          (client/kernel-teardown node
                                  "db/primary"
                                  {})))))))

(defn.js subscribe-db-ops
  {:added "4.1"}
  [mode]
  (var node (substrate/node-create {"id" (xt/x:cat "subscribe-db-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
      (promise/x:promise-then
       (fn []
         (return
          (client/subscribe-db node
                               "db/primary"
                               "default"
                               ["realtime:room:client-sub-1"
                                "realtime:room:client-sub-2"]
                               {})))))))

(defn.js unsubscribe-db-ops
  {:added "4.1"}
  [mode]
  (var node (substrate/node-create {"id" (xt/x:cat "unsubscribe-db-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
      (promise/x:promise-then
       (fn []
         (return
          (client/subscribe-db node
                               "db/primary"
                               "default"
                               ["realtime:room:client-unsub-1"
                                "realtime:room:client-unsub-2"]
                               {}))))
      (promise/x:promise-then
       (fn []
         (return
          (client/unsubscribe-db node
                                 "db/primary"
                                 "default"
                                 ["realtime:room:client-unsub-1"
                                  "realtime:room:client-unsub-2"]
                                 {})))))))

(defn.js sync-caching-ops
  {:added "4.1"}
  [mode logs]
  (var node (substrate/node-create {"id" (xt/x:cat "sync-caching-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
      (promise/x:promise-then
       (fn []
         (return
          (client/sync-caching node
                               "db/primary"
                               {"db/sync" {"Log" logs}}
                               {}))))
      (promise/x:promise-then
       (fn []
         (return true))))))

(defn.js rpc-call-ops
  {:added "4.1"}
  [mode]
  (var node (substrate/node-create {"id" (xt/x:cat "rpc-call-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
      (promise/x:promise-then
       (fn []
         (return
          (client/rpc-call node
                           "db/primary"
                           {"input" [{"symbol" "i_message" "type" "text"}]
                            "return" "jsonb"
                            "schema" "scratch_v0"
                            "id" "log_append_public"
                            "table" {"base" "Log"
                                     "type" "db/sync"}
                            "flags" {}}
                           ["hello-client"]
                           {}))))
)))

(defn.js pull-call-ops
  {:added "4.1"}
  [mode]
  (var node (substrate/node-create {"id" (xt/x:cat "pull-call-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
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
                           ["hello-pull"]
                           {}))))
      (promise/x:promise-then
       (fn []
         (return (client/pull-call node "db/primary" ["Log"] {}))))
)))

(defn.js dataview-call-ops
  {:added "4.1"}
  [mode]
  (var node (substrate/node-create {"id" (xt/x:cat "dataview-call-" mode)}))
  (return
   (-> (:? (== mode "direct")
          (-/init-kernel node "supabase" "memory")
          (-/init-sharedworker node "supabase" "memory"))
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
                           ["hello-dataview"]
                           {}))))
      (promise/x:promise-then
       (fn []
         (return
          (client/dataview-call node
                                "db/primary"
                                {"table" "Log"
                                 "select_entry" {"input" []
                                                 "view" {"table" "Log"
                                                         "type" "select"
                                                         "query" {}}}
                                 "return_entry" {"input" []
                                                 "view" {"table" "Log"
                                                         "type" "return"
                                                         "query" ["id" "message"]}}}
                                {}))))
)))

;;
;; TAB RUNNER
;;

(defmacro run-ops-on-tab
  "Runs a JS operation expression on `tab`, catching errors and notifying the
   result. The expression should return a promise. Returns the value passed to
   repl/notify."
  {:added "4.1"}
  [tab ops-expr timeout]
  `(chromedriver/with-tab (l/rt :js) ~tab
     (notify/wait-on [:js ~timeout]
       (promise/x:promise-catch
        (promise/x:promise-then
         ~ops-expr
         (~'fn [res#] (repl/notify res#)))
        (~'fn [err#]
          (repl/notify
           (:? err# err# {"error" "nil error"})))))))

;;
;; PARITY TESTS
;;

(defmacro tab-setup
  "Common fact-level setup: store worker URL on the current tab and create a
   second tab loaded on the same origin. Expanded as a literal vector so the
   JS expressions are evaluated when the fact runs, not at compile time."
  {:added "4.1"}
  []
  `[(-/store-sharedworker-url)
    (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
    (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))
    (chromedriver/tab-switch (l/rt :js) +tab-b+)
    (chromedriver/goto +notify-url+ 4000)
    (chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})])

(defmacro pg-tab-setup
  "Like tab-setup but also deletes the scratch_v0/Log table before creating
   the second tab. Used by facts that mutate Log."
  {:added "4.1"}
  []
  `[(pg/t:delete scratch-v0/Log)
    (-/store-sharedworker-url)
    (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
    (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))
    (chromedriver/tab-switch (l/rt :js) +tab-b+)
    (chromedriver/goto +notify-url+ 4000)
    (chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})])

(defn- tab-cleanup
  "Closes the secondary tab and returns focus to tab A."
  []
  (when-let [tab-b (resolve '+tab-b+)]
    (chromedriver/tab-close (l/rt :js) @tab-b))
  (when-let [tab-a (resolve '+tab-a+)]
    (chromedriver/tab-switch (l/rt :js) @tab-a {:bootstrap false})))

^{:refer xt.db.node.client-base-tabs-test/kernel-init
  :added "4.1"}
(fact "invokes kernel-init locally and through a SharedWorker on another tab"
  {:setup (tab-setup)}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/kernel-init-ops "direct") 20000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/kernel-init-ops "sharedworker") 20000))

  (tab-cleanup)

  direct-result
  => (contains-in
      {"status" "no_change", "data" {"caching" map?, "primary" map?, "common" map?}})

  sharedworker-result
  => (contains-in
      {"status" "no_change", "data" {"caching" map?, "primary" map?, "common" map?}}))

^{:refer xt.db.node.client-base-tabs-test/kernel-setup
  :added "4.1"}
(fact "sets up base db services locally and through a SharedWorker on another tab"
  {:setup (tab-setup)}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/kernel-setup-ops "direct") 20000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/kernel-setup-ops "sharedworker") 20000))

  (tab-cleanup)

  direct-result
  => (contains-in
      {"status" "setup", "data" {"caching" map?, "primary" map?, "common" map?}})

  sharedworker-result
  => (contains-in
      {"status" "setup", "data" {"caching" map?, "primary" map?, "common" map?}}))

^{:refer xt.db.node.client-base-tabs-test/kernel-teardown
  :added "4.1"}
(fact "tears down base db services locally and through a SharedWorker on another tab"
  {:setup (tab-setup)}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/kernel-teardown-ops "direct") 20000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/kernel-teardown-ops "sharedworker") 20000))

  (tab-cleanup)

  direct-result
  => (contains-in
      {"status" "teardown", "data" {"caching" map?, "primary" map?, "common" map?}})

  sharedworker-result
  => (contains-in
      {"status" "teardown", "data" {"caching" map?, "primary" map?, "common" map?}}))

^{:refer xt.db.node.client-base-tabs-test/subscribe-db
  :added "4.1"}
(fact "subscribes to db topics locally and through a SharedWorker on another tab"
  {:setup (tab-setup)}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/subscribe-db-ops "direct") 20000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/subscribe-db-ops "sharedworker") 20000))

  (tab-cleanup)

  direct-result => [true true]
  sharedworker-result => [true true])

^{:refer xt.db.node.client-base-tabs-test/unsubscribe-db
  :added "4.1"}
(fact "unsubscribes from db topics locally and through a SharedWorker on another tab"
  {:setup (tab-setup)}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/unsubscribe-db-ops "direct") 20000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/unsubscribe-db-ops "sharedworker") 20000))

  (tab-cleanup)

  direct-result => true
  sharedworker-result => true)

^{:refer xt.db.node.client-base-tabs-test/sync-caching
  :added "4.1"}
(fact "applies a db/sync payload locally and through a SharedWorker on another tab"
  {:setup [(def +logs+ [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
                         "message" "hello"}
                        {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
                         "message" "world"}])
           (tab-setup)]}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/sync-caching-ops "direct" (@! +logs+)) 20000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/sync-caching-ops "sharedworker" (@! +logs+)) 20000))

  (tab-cleanup)

  direct-result   => true

  sharedworker-result => true)

^{:refer xt.db.node.client-base-tabs-test/rpc-call
  :added "4.1"}
(fact "calls an rpc entry locally and through a SharedWorker on another tab"
  {:setup (pg-tab-setup)}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/rpc-call-ops "direct") 30000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/rpc-call-ops "sharedworker") 30000))

  (tab-cleanup)

  direct-result
  => (contains-in
      {"message" "hello-client", "author_id" nil, "id" string?})

  sharedworker-result
  => (contains-in
      {"message" "hello-client", "author_id" nil, "id" string?}))

^{:refer xt.db.node.client-base-tabs-test/pull-call
  :added "4.1"
}
(fact "pulls data locally and through a SharedWorker on another tab"
  {:setup (pg-tab-setup)}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/pull-call-ops "direct") 30000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/pull-call-ops "sharedworker") 30000))

  (tab-cleanup)

  direct-result
  => (contains-in
      [{"message" "hello-pull", "author_id" nil, "id" string?}])

  sharedworker-result
  => (contains-in
      [{"message" "hello-pull", "author_id" nil, "id" string?}]))

^{:refer xt.db.node.client-base-tabs-test/dataview-call
  :added "4.1"
}
(fact "executes a dataview query locally and through a SharedWorker on another tab"
  {:setup (pg-tab-setup)}

  (def direct-result
    (run-ops-on-tab +tab-a+ (-/dataview-call-ops "direct") 30000))

  (def sharedworker-result
    (run-ops-on-tab +tab-b+ (-/dataview-call-ops "sharedworker") 30000))

  (tab-cleanup)

  direct-result
  => (contains-in
      [{"message" "hello-dataview", "author_id" nil, "id" string?}])

  sharedworker-result
  => (contains-in
      [{"message" "hello-dataview", "author_id" nil, "id" string?}]))

