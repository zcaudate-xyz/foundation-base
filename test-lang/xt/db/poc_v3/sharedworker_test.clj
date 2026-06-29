(ns xt.db.poc-v3.sharedworker-test
  "Smoke test for `xt.db.poc-v3.sharedworker`.

   Verifies that a module SharedWorker running the xt.db.node adaptor can be
   loaded in the :playground browser runtime, that a browser client can connect
   to it, and that the client can initialise the scratch-v3 adaptor (schema +
   lookup + Supabase primary / SQLite cache) over the SharedWorker transport.

   The full UserProfile read/update/sync helpers live in the
   `xt.db.poc-v3.sharedworker` namespace; they require a browser environment
   where the SharedWorker can make outbound HTTP requests."
  (:use code.test)
  (:require [hara.lang :as l]
            [std.lib.component :as component]
            [hara.runtime.js-playground :as js-playground]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.sample.scratch-v3 :as scratch-v3]
            [postgres.core.supabase :as s]
            [xt.db.poc-v3.sharedworker :as sharedworker]))

(def +account-id+
  "Deterministic user account id shared between the postgres seed and the
   browser client."
  "11111111-1111-1111-1111-111111111111")

;;
;; Postgres runtime for scratch_v3 seeding and verification.
;;

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v3 :as scratch-v3]
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
  (s/grant-usage #{"scratch_v3"}))

;;
;; JS :playground runtime for the browser page and SharedWorker.
;;

(l/script- :js
  {:runtime :playground
   :config {:port 0}
   :test-mode true
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.poc-v3.sharedworker :as sharedworker]]
   :emit {:lang/jsx false}})

(defn- wait-for-channel
  "Waits up to 5s for the playground websocket channel to be connected."
  [rt]
  (let [channel (:channel rt)]
    (loop [i 0]
      (when (and (< i 50) (not @channel))
        (Thread/sleep 100)
        (recur (inc i))))))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (local-min/wait-for-postgrest-ready "scratch_v3" "UserProfile")
          (scratch-v3/insert-user +account-id+ "poc-alice" "alice@poc.local" true false {})
          (scratch-v3/insert-user-profile +account-id+ "Alicia" "Adams" "EN" "scratch v3 user" {})
          (l/rt:scaffold-imports :js)
          (sharedworker/write-worker-files! (:root (l/rt :js)))
          (def +url+ (js-playground/play-url (l/rt :js)))
          (def +browser+ (chromedriver/browser {}))
          (chromedriver/goto +url+ 5000 +browser+)
          (wait-for-channel (l/rt :js))]
  :teardown [(l/rt:stop)
             (component/stop +browser+)]})

^{:refer xt.db.poc-v3.sharedworker-test/connect-sharedworker
  :added "4.1"}
(fact "sharedworker connects"

  (notify/wait-on [:js 10000]
    (-> (sharedworker/connect-sharedworker "debug-client")
        (promise/x:promise-then
         (fn [conn]
           (repl/notify {"stage" "connected" "conn" conn})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"stage" "error"
                         "msg" (or (. err ["message"])
                                   (xt/x:ex-message err)
                                   ((!:G String) err))
                         "data" (xt/x:ex-data err)
                         "stack" (. err ["stack"])})))))
  => (contains-in {"stage" "connected"}))

^{:refer xt.db.poc-v3.sharedworker-test/browser-fetch-supabase
  :added "4.1"}
(fact "browser fetch to supabase responds"
  (notify/wait-on [:js 10000]
    (-> ((!:G fetch) "http://127.0.0.1:55121/rest/v1/")
        (promise/x:promise-then
         (fn [res]
           (repl/notify {"ok" true "status" (. res ["status"])})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"ok" false
                         "msg" (or (. err ["message"])
                                   (xt/x:ex-message err)
                                   ((!:G String) err))})))))
  => (contains-in {"ok" true}))

^{:refer xt.db.poc-v3.sharedworker-test/init-adaptor-works
  :added "4.1"}
(fact "init-adaptor returns from sharedworker"
  (notify/wait-on [:js 20000]
    (-> (sharedworker/connect-sharedworker "init-client")
        (promise/x:promise-then
         (fn [conn]
           (var client (. conn ["node"]))
           (var transport-id (. conn ["transport_id"]))
           (return (sharedworker/init-adaptor client transport-id))))
        (promise/x:promise-then
         (fn [res]
           (repl/notify {"stage" "init-adaptor-ok" "res" res})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"stage" "error"
                         "msg" (or (. err ["message"])
                                   (xt/x:ex-message err)
                                   ((!:G String) err))
                         "data" (xt/x:ex-data err)
                         "stack" (. err ["stack"])})))))
  => (contains-in {"stage" "init-adaptor-ok"}))
