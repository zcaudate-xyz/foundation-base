^{:seedgen/skip true}
(ns xt.db.poc.s05-sharedworker-custom-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]))

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
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.runtime :as runtime]
             [js.net.http-fetch]
             [xt.substrate :as substrate]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.s05-sharedworker-custom-test/sharedworker-connects
  :added "4.1"}
(fact "shared worker auto-connect initialises the kernel on the client"

  (notify/wait-on [:js 5000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (-> (runtime/sharedworker-connect client
                                      {"primary" {"type" "supabase"
                                                  "defaults" (@! local-min/+config-supabase-anon+)}
                                       "caching" {"type" "sqlite"
                                                  "defaults" {"filename" ":memory:"}}}
                                      -/Schema
                                      -/SchemaLookup)
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      {"status" "setup", "data" {"caching" map?, "primary" map?, "common" map?}}))

^{:refer xt.db.poc.s05-sharedworker-custom-test/supabase-reachable
  :added "4.1"
  :setup [(scratch-v0/log-append-public "hello")]}
(fact "browser page can reach supabase rest endpoint"

  (notify/wait-on [:js 20000]
    (var client (js.net.http-fetch/create
                  (@! local-min/+config-supabase-anon+)
                  (xt.net.addon-supabase/middleware-supabase)))
    (-> (js.net.http-fetch/request-http
         client
         {"path" "/rest/v1/Log"
          "method" "GET"
          "headers" {"Accept-Profile" "scratch_v0"
                     "apikey" (@! (-> local-min/+config+ :api :anon-key))}})
        (promise/x:promise-then
         (fn [res]
           (repl/notify (. res ["body"]))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify err)))))
  => (contains-in
      [{"id" string?
        "message" "hello"}]))
