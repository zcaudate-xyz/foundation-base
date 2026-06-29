(ns xt.db.node.adaptor-proxy-supabase-subset-test
  "Subset of proxy tests using the original namespace's helpers."
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.common-repl :as repl]
            [xt.lang.spec-base :as xt]
            [xt.lang.spec-promise :as promise]
            [xt.db.node.adaptor-proxy-supabase :as proxy]
            [xt.db.node.adaptor-proxy-supabase-test :as orig]
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
  {:runtime :chromedriver.instance
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.adaptor-proxy-supabase :as proxy]
             [js.worker.link :as worker-link]]})

(def +notify-url+
  (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/"))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")
          (chromedriver/goto +notify-url+ 4000)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop :js)]})

^{:refer xt.db.node.adaptor-proxy-supabase-subset-test/original-health
  :added "4.1"}
(fact "health via original helpers"
  (notify/wait-on [:js 30000]
    (-> (orig/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify {"stage" "ok" "out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"stage" "error" "err" err}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"stage" "connect-error" "err" err})))))
  => (contains-in {"stage" "ok"}))

^{:refer xt.db.node.adaptor-proxy-supabase-subset-test/original-sign-in
  :added "4.1"}
(fact "sign-in via original helpers"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "subset-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (orig/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-sign-up-handler nil
                                               ["auth/supabase" {"email" email "password" "secret123"} {}]
                                               nil
                                               client)
               (promise/x:promise-then
                (fn [_]
                  (proxy/supabase-sign-in-handler nil
                                                  ["auth/supabase" {"email" email "password" "secret123"} {}]
                                                  nil
                                                  client)))
               (promise/x:promise-then
                (fn [out]
                  (repl/notify {"stage" "ok" "out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"stage" "error" "err" err}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"stage" "connect-error" "err" err})))))
  => (contains-in {"stage" "ok"}))
