(ns xt.db.walkthrough.guide-09-supabase-broadcast-tree-sync-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.data-main-test :as sample]
            [xt.db.helpers.supabase-pull-live-test :as live]))

(def +live-broadcast-topic+
  "room:users-tree")

(def +live-broadcast-event+
  "user_sync")

(def +live-broadcast-user-id+
  "00000000-0000-0000-0000-0000000000f1")

(def +live-broadcast-profile-id+
  "00000000-0000-0000-0000-0000000000f2")

(def +live-broadcast-request+
  {"db/sync"
   {"UserAccount"
    [{"id" +live-broadcast-user-id+
      "nickname" "broadcast-root"
      "is_super" true
      "is_suspended" false
      "is_official" false
      "is_verified" true
      "password_updated" 1630408723423619
      "time_updated" 1630408722786926
      "time_created" 1630408722786926
      "funding" nil
      "is_active" true
      "profile"
      [{"id" +live-broadcast-profile-id+
        "last_name" "User"
        "first_name" "Broadcast"
        "language" "en"
        "time_updated" 1630408722786926
        "time_created" 1630408722786926
        "detail" {"hello" "tree"}}]}]}})

(def +user-tree-query+
  ["UserAccount"
   ["id"
    "nickname"
    ["profile"
     ["id"
      "first_name"
      "detail"]]]])

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.runtime :as xdb]
             [xt.db.runtime.supabase-realtime :as realtime]
             [js.lib.client-fetch :as js-fetch]
             [js.lib.client-websocket :as js-ws]
             [js.lib.driver-sqlite :as js-sqlite]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-util :as ut]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.db.helpers.data-main-test :as sample]
             [xt.db.helpers.supabase-pull-live-test :as live]]})

(def.js UserTreeQuery
  (@! +user-tree-query+))

(defn.js create-local-user-db
  []
  (return
   (promise/x:promise-then
    (dbsql/connect (js-sqlite/driver) {})
    (fn [conn]
      (dbsql/query-sync conn
                        (str/join "\n\n"
                                  (manage/table-create-all
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil))))
      (return
       (xtd/obj-assign
        (xdb/db-create {"::" "db.sql"
                        "instance" conn}
                       sample/Schema
                       sample/SchemaLookup
                       (ut/sqlite-opts nil))
        {"schema" sample/Schema}))))))

(defn.js pull-user-tree
  [local-db]
  (return
   (xdb/db-pull-sync local-db
                     sample/Schema
                     -/UserTreeQuery)))

(defn.js pull-user-profile
  [local-db profile-id]
  (return
   (xdb/db-pull-sync local-db
                     sample/Schema
                     ["UserProfile"
                      {"id" profile-id}
                      ["first_name"]])))

(fact:global
  {:setup [(l/rt:restart)
           (do (live/init-live-postgres-runtime!)
               (l/rt:setup (live/pg-rt) live/+postgres-module+)
               (live/refresh-live-supabase-config!)
               true)]
   :teardown [(do (l/rt:teardown (live/pg-rt) live/+postgres-module+)
                  (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                  (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                  true)
              (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-09-supabase-broadcast-tree-sync/STEP.00-broadcast-tree-into-sqlite-cache
  :added "4.1"}
(fact "step 00: live supabase broadcast sends a tree payload that xt.db flattens into sqlite and reads back as nested data"

  (future
    (Thread/sleep 4000)
    (live/send-realtime-broadcast!
     +live-broadcast-topic+
     +live-broadcast-event+
     +live-broadcast-request+))

  (notify/wait-on [:js 15000]
    (var primary-config (xt/x:obj-clone (@! live/+live-supabase-config+)))
    (var primary-client (xt/x:obj-clone (. primary-config ["client"])))
    (xt/x:set-key primary-client "transport" (js-fetch/client {}))
    (xt/x:set-key primary-config "client" primary-client)
    (var sub-client
         {"transport" (js-ws/driver {})
          "base_url" (xt/x:get-key primary-client "base_url")
          "api_key" (xt/x:get-key primary-client "api_key")
          "auth_token" (xt/x:get-key primary-client "auth_token")
          "topic" (@! +live-broadcast-topic+)
          "message_event" "broadcast"})
    (-> (-/create-local-user-db)
        (promise/x:promise-then
         (fn [local-db]
           (var initial-users
               (-/pull-user-tree local-db))
           (var statuses [])
           (var request-out nil)
           (promise/x:promise-then
           (realtime/subscribe
             {"client" sub-client}
             local-db
            {"on_status"
             (fn [status _frame]
               (xt/x:arr-push statuses status))
             "on_request"
             (fn [request _payload _frame]
               (when (== (@! +live-broadcast-user-id+)
                         (xtd/get-in request ["db/sync" "UserAccount" 0 "id"]))
                 (:= request-out request)))})
           (fn [_sub]
             (var poll-id
                  (setInterval
                   (fn []
                     (when (xt/x:not-nil? request-out)
                       (clearInterval poll-id)
                       (var cached-users
                           (-/pull-user-tree local-db))
                       (var cached-user (xt/x:get-idx cached-users 0))
                       (var cached-profile (xt/x:get-idx (. cached-user ["profile"]) 0))
                       (var profile-row
                            (xt/x:get-idx
                            (-/pull-user-profile
                              local-db
                              (@! +live-broadcast-profile-id+))
                             0))
                       (repl/notify
                        {"topic" "realtime:room:users-tree"
                         "status" (xt/x:first statuses)
                         "primary_kind" "supabase"
                         "local_kind" "sqlite"
                         "initial_count" (xt/x:len initial-users)
                         "updated_count" (xt/x:len cached-users)
                         "request_profile_first" (xtd/get-in
                                                  request-out
                                                  ["db/sync" "UserAccount" 0 "profile" 0 "first_name"])
                         "cached_nickname" (. cached-user ["nickname"])
                         "cached_profile_first" (. cached-profile ["first_name"])
                         "profile_row_first" (. profile-row ["first_name"])})))
                   100))
             (return true)))))))
  => {"topic" "realtime:room:users-tree"
      "status" "SUBSCRIBED"
      "primary_kind" "supabase"
      "local_kind" "sqlite"
      "initial_count" 0
      "updated_count" 1
      "request_profile_first" "Broadcast"
      "cached_nickname" "broadcast-root"
      "cached_profile_first" "Broadcast"
      "profile_row_first" "Broadcast"})
