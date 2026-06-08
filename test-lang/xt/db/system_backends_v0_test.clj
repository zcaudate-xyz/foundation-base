(ns xt.db.system-backends-v0-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:host   (-> docker-min/+config+ :db :host)
            :port   (-> docker-min/+config+ :db :port)
            :user   (-> docker-min/+config+ :db :user)
            :pass   (-> docker-min/+config+ :db :password)
            :dbname (-> docker-min/+config+ :db :database)
            :startup  docker-min/start-supabase
            :teardown docker-min/stop-supabase}
   :require [[postgres.core :as pg]
             [postgres.core.supabase :as s]
             [postgres.sample.scratch-v0 :as v0]]
   :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [js.lib.client-fetch :as js-fetch]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system-backends-v0-test/supabase-backend-v0 :added "4.1"}
(fact "supabase backend v0: query scratch-v0 Log through local postgrest"

  (pg/bind-schema (:schema (pg/app "scratch_v0")))
  (pg/bind-app (pg/app "scratch_v0"))

  
  
  (do
    (cleanup-log!)
    (try
      (live/pg-exec!
       (str "INSERT INTO \"" live/+scratch-v0-schema+ "\".\"Log\" (message)"
            " VALUES (" (live/sql-literal +log-message+) ");"))
      (Thread/sleep 200)
      (notify/wait-on [:js 15000]
        (var live-config (@! live/+live-supabase-config+))
        (var live-client (. live-config ["client"]))
        (var schema
             {"Log"
              {"id" {"type" "uuid"
                     "primary" true
                     "scope" "id"
                     "order" 0
                     "ident" "id"}
               "message" {"type" "text"
                          "required" true
                          "scope" "data"
                          "order" 1
                          "ident" "message"}
               "author_id" {"type" "uuid"
                            "scope" "data"
                            "order" 2
                            "ident" "author_id"}}})
        (var lookup
             {"Log" {"position" 0}})
        (var client-config
             {"base_url" (. live-client ["base_url"])
              "schema_name" (@! live/+scratch-v0-schema+)
              "api_key" (. live-client ["api_key"])
              "auth_token" (. live-client ["auth_token"])
              "transport" (js-fetch/client {})})
        (var db (db-system/db-create
                 {"::" "db.supabase"
                  :instance client-config}
                 schema
                 lookup
                 nil))
        (promise/x:promise-then
         (db-system/db-pull
          db
          schema
          ["Log"
           {"message" (@! xt.db.system-backends-v0-test/+log-message+)}
           ["message"]])
         (fn [result]
           (repl/notify
            {"has_db" true
             "dbtype" (xt/x:get-key db "::")
             "message" (xtd/get-in result [0 "message"])}))))
      (finally
        (cleanup-log!))))
  => {"has_db" true
      "dbtype" "db.supabase"
      "message" "copilot_system_backends_v0"})


(comment
  
  (+ 1 2 3))
