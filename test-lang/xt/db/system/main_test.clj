(ns xt.db.system.main-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> docker-min/+config+ :db :host)
              :port   (-> docker-min/+config+ :db :port)
              :user   (-> docker-min/+config+ :db :user)
              :pass   (-> docker-min/+config+ :db :password)
              :dbname (-> docker-min/+config+ :db :database)
              :startup  docker-min/start-supabase
              :shutdown docker-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-postgres :as impl-postgres]
             [xt.db.system.main :as main]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.system.main/create-impl :added "4.1"}
(fact "creates impls for local and live backends"

  [(!.js (xtd/get-in (main/create-impl "memory" {} -/Schema -/SchemaLookup)
                     ["::"]))
   (!.js (xtd/get-in (main/create-impl "sqlite"
                                       {"filename" ":memory:"}
                                       -/Schema
                                       -/SchemaLookup)
                     ["::"]))
   (!.js (xtd/get-in (main/create-impl "postgres"
                                       (@! (docker-min/+config+ :db))
                                       -/Schema
                                       -/SchemaLookup)
                     ["::"]))
   (!.js (xtd/get-in (main/create-impl "supabase"
                                       {"host" "127.0.0.1"
                                        "port" (@! (-> docker-min/+config+ :api :port))
                                        "secured" false
                                        "basepath" ""
                                        "apikey" (@! (-> docker-min/+config+ :api :service-key))}
                                       -/Schema
                                       -/SchemaLookup)
                     ["::"]))]
  => ["db.impl.memory"
      "db.impl.sqlite"
      "db.impl.postgres"
      "db.impl.supabase"])

^{:refer xt.db.system.main/create-impl-init :added "4.1"}
(fact "initialises postgres impls and leaves the wrapper output usable"

  (notify/wait-on :js
    (-> (main/create-impl
         "postgres"
          (@! (docker-min/+config+ :db))
          -/Schema
          -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (repl/notify (xt/x:get-key impl "client"))))))
  => map?)

^{:refer xt.db.system.main/pull :added "4.1"}
(fact "pull reads from the local memory impl"

  (notify/wait-on :js
    (-> (main/create-impl "sqlite" {} -/Schema -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (main/record-add impl
                     "Log"
                     [{"id" "USD" "message" "USD"}
                      {"id" "AUD" "message" "AUD"}])
           (repl/notify
            (main/pull impl ["Log"]))))))
  => (contains [(contains {"id" "USD" "message" "USD"})
                (contains {"id" "AUD" "message" "AUD"})]
               :in-any-order)
  
  (notify/wait-on :js
    (-> (main/create-impl "memory" {} -/Schema -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (main/record-add impl
                     "Log"
                     [{"id" "USD" "message" "USD"}
                      {"id" "AUD" "message" "AUD"}])
           (repl/notify
            (main/pull impl ["Log"]))))))
  => (contains [(contains {"id" "USD" "message" "USD"})
                (contains {"id" "AUD" "message" "AUD"})]
               :in-any-order))

^{:refer xt.db.system.main/rpc-call-async :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "rpc-call-async reaches the live supabase rpc endpoint"

  ;; POSTGRES
  (notify/wait-on :js
    (-> (main/create-impl "postgres" (@! (:db docker-min/+config+))
                          -/Schema
                          -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (main/rpc-call-async impl {"input" []
                                       "return" "text"
                                       "schema" "scratch_v0"
                                       "id" "ping"
                                       "flags" {}}
                                 []))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong"

  ;; SUPABASE
  (notify/wait-on :js
    (-> (main/create-impl "supabase" (@! docker-min/+config-supabase-anon+)
                          -/Schema
                          -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (main/rpc-call-async impl {"input" []
                                       "return" "text"
                                       "schema" "scratch_v0"
                                       "id" "ping"
                                       "flags" {}}
                                 []))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong")

^{:refer xt.db.system.main/record-add :added "4.1"}
(fact "record-add writes through the local memory impl"

  (!.js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (main/record-add impl "UserAccount" [{"id" "USER-10" "nickname" "echo"}]))
  => {"id" "USER-10" "nickname" "echo"})

^{:refer xt.db.system.main/record-add-async :added "4.1"}
(fact "record-add-async writes through promise semantics"

  (notify/wait-on :js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (-> (main/record-add-async impl
                               "UserAccount"
                               [{"id" "USER-20" "nickname" "delta"}])
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (xtd/get-in impl ["client"
                              "UserAccount"
                              "USER-20"
                              "record"
                              "data"]))))))
  => {"id" "USER-20" "nickname" "delta"})

^{:refer xt.db.system.main/record-delete :added "4.1"}
(fact "record-delete removes ids through the local memory impl"
  (!.js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (main/record-add impl "UserAccount" [sample/RootUser])
    (main/record-delete impl
                        "UserAccount"
                        ["00000000-0000-0000-0000-000000000000"])
    (xtd/get-in impl ["client"
                      "UserAccount"
                      "00000000-0000-0000-0000-000000000000"]))
  => nil)

^{:refer xt.db.system.main/record-delete-async :added "4.1"}
(fact "record-delete-async removes ids through promise semantics"
  (notify/wait-on :js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (main/record-add impl "UserAccount" [sample/RootUser])
    (-> (main/record-delete-async impl
                                  "UserAccount"
                                  ["00000000-0000-0000-0000-000000000000"])
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (xtd/get-in impl ["client"
                              "UserAccount"
                              "00000000-0000-0000-0000-000000000000"]))))))
  => nil)

^{:refer xt.db.system.main/process-add-event :added "4.1"}
(fact "process-add-event merges nested data into the local memory impl"
  (!.js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (var out (main/process-add-event impl {"UserAccount" [sample/RootUser]}))
    [(xt/x:len out)
     (xtd/get-in impl ["client"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "data"
                       "nickname"])
     (xtd/get-in impl ["client"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"
                       "record"
                       "rev_links"
                       "profile"])
     (xtd/get-in impl ["client"
                       "UserProfile"
                       "c4643895-b0ce-44cc-b07b-2386bf18d43b"
                       "record"
                       "ref_links"
                       "account"])])
  => [2
      "root"
      {"c4643895-b0ce-44cc-b07b-2386bf18d43b" true}
      {"00000000-0000-0000-0000-000000000000" true}])

^{:refer xt.db.system.main/process-remove-event :added "4.1"}
(fact "process-remove-event removes nested data in lookup order"
  (!.js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (main/process-add-event impl {"UserAccount" [sample/RootUser]})
    [(main/process-remove-event impl {"UserAccount" [sample/RootUser]})
     (xtd/get-in impl ["client"
                       "UserAccount"
                       "00000000-0000-0000-0000-000000000000"])
     (xtd/get-in impl ["client"
                       "UserProfile"
                       "c4643895-b0ce-44cc-b07b-2386bf18d43b"])])
  => [["UserAccount" "UserProfile"]
      nil
      nil])
