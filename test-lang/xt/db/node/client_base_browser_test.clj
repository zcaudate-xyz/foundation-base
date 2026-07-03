(ns xt.db.node.client-base-browser-test
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
             [xt.substrate.transport-browser :as browser-transport]]})

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
  (def direct-result
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
  (def sharedworker-result
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

  direct-result
  => (contains-in
      [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
        "message" "hello"}
       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
        "message" "world"}])

  sharedworker-result
  => (contains-in
      [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
        "message" "hello"}
       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
        "message" "world"}]))

