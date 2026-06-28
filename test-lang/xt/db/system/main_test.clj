(ns xt.db.system.main-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
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
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-postgres :as impl-postgres]
             [xt.db.system.main :as main]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {
  :skip (not (std.lib.env/program-exists? "supabase"))
  :setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

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
                                       (@! (local-min/+config+ :db))
                                       -/Schema
                                       -/SchemaLookup)
                     ["::"]))
   (!.js (xtd/get-in (main/create-impl "supabase"
                                       {"host" "127.0.0.1"
                                        "port" (@! (-> local-min/+config+ :api :port))
                                        "secured" false
                                        "basepath" ""
                                        "apikey" (@! (-> local-min/+config+ :api :service-key))}
                                       -/Schema
                                       -/SchemaLookup)
                     ["::"]))]
  => ["xt.db.system.impl_memory/ImplMemory"
      "xt.db.system.impl_sqlite/ImplSqlite"
      "xt.db.system.impl_postgres/ImplPostgres"
      "xt.db.system.impl_supabase/ImplSupabase"])

^{:refer xt.db.system.main/create-impl-init :added "4.1"}
(fact "initialises postgres impls and leaves the wrapper output usable"

  (notify/wait-on :js
    (-> (main/create-impl
         "postgres"
         (@! (local-min/+config+ :db))
         -/Schema
         -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (repl/notify (xt/x:get-key impl "client"))))))
  => map?)

^{:refer xt.db.system.impl-common/pull :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "pull reads from the local memory impl"

  (!.js
    (main/create-impl "sqlite" {} -/Schema -/SchemaLookup))

  (notify/wait-on :js
    (-> (main/create-impl "sqlite" {} -/Schema -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (impl-common/record-add impl
                                   "Log"
                                   [{"id" "LOG-10" "message" "hello"}
                                    {"id" "LOG-20" "message" "world"}])
           (repl/notify
            (impl-common/pull impl ["Log"]))
           #_(repl/notify impl)))))
  => (contains [(contains {"id" "LOG-10" "message" "hello"})
                (contains {"id" "LOG-20" "message" "world"})]
               :in-any-order)
  
  (notify/wait-on :js
    (-> (main/create-impl "memory" {} -/Schema -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (impl-common/record-add impl
                                   "Log"
                                   [{"id" "LOG-10" "message" "hello"}
                                    {"id" "LOG-20" "message" "world"}])
           (repl/notify
            (impl-common/pull impl ["Log"]))))))
  => (contains [(contains {"id" "LOG-10" "message" "hello"})
                (contains {"id" "LOG-20" "message" "world"})]
               :in-any-order))

^{:refer xt.db.system.impl-common/pull-async :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)
          (scratch-v0/log-append "hello")
          (scratch-v0/log-append "world")]}
(fact "getting same semantics for supabase and postgres"
  
  (notify/wait-on :js
    (-> (main/create-impl "postgres"
                          (@! (:db local-min/+config+))
                          -/Schema
                          -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl-common/pull-async impl ["Log"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains
      [(contains {"message" "hello", "author_id" nil, "id" string?})
       (contains {"message" "world", "author_id" nil, "id" string?})]
      :in-any-order)

  (notify/wait-on :js
    (-> (main/create-impl "supabase"
                          (@! local-min/+config-supabase-anon+)
                          -/Schema
                          -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl-common/pull-async impl ["Log"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains
      [(contains {"message" "hello", "author_id" nil, "id" string?})
       (contains {"message" "world", "author_id" nil, "id" string?})]
      :in-any-order))

^{:refer xt.db.system.impl-common/rpc-call-async :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "rpc-call-async reaches the live supabase rpc endpoint"

  ;; POSTGRES
  (notify/wait-on :js
    (-> (main/create-impl "postgres"
                          (@! (:db local-min/+config+))
                          -/Schema
                          -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl-common/rpc-call-async impl {"input" []
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
    (-> (main/create-impl "supabase" (@! local-min/+config-supabase-anon+)
                          -/Schema
                          -/SchemaLookup)
        (main/create-impl-init)
        (promise/x:promise-then
         (fn [impl]
           (return
            (impl-common/rpc-call-async impl {"input" []
                                              "return" "text"
                                              "schema" "scratch_v0"
                                              "id" "ping"
                                              "flags" {}}
                                        []))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong")

^{:refer xt.db.system.impl-common/record-add :added "4.1"}
(fact "record-add writes through the local memory impl"

  (notify/wait-on :js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (->  (main/create-impl-init impl)
         (promise/x:promise-then
          (fn [impl]
            (impl-common/record-add impl
                                    "Log"
                                    [{"id" "00000000-0000-0000-0000-000000000000"
                                      "author_id" "00000000-0000-0000-0000-000000000000"
                                      "message" "delta"}])))
         (promise/x:promise-then
          (fn [_]
            (repl/notify
             (impl-common/pull impl ["Log"]))))))
  => [{"id" "00000000-0000-0000-0000-000000000000"
       "message" "delta"}]
  
  (notify/wait-on :js
    (var impl (main/create-impl "sqlite" {} -/Schema -/SchemaLookup))
    (->  (main/create-impl-init impl)
         (promise/x:promise-then
          (fn [impl]
            (impl-common/record-add impl
                                    "Log"
                                    [{"id" "00000000-0000-0000-0000-000000000000"
                                      "author_id" "00000000-0000-0000-0000-000000000000"
                                      "message" "delta"}])))
         (promise/x:promise-then
          (fn [_]
            (repl/notify
             (impl-common/pull impl ["Log"]))))))
  => [{"id" "00000000-0000-0000-0000-000000000000"
       "author_id" "00000000-0000-0000-0000-000000000000"
       "message" "delta"}])

^{:refer xt.db.system.impl-common/record-delete :added "4.1"}
(fact "record-delete removes ids through the local memory impl"

  (!.js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (impl-common/record-add impl "Log" [{"id" "LOG-30" "message" "root"}])
    (impl-common/record-delete impl
                               "Log"
                               ["LOG-30"])
    (xtd/get-in impl ["client"
                      "Log"
                      "LOG-30"]))
  => nil)


^{:refer xt.db.system.impl-common/process-add-event :added "4.1"}
(fact "process-add-event merges nested data into the local memory impl"

  (!.js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (var out (impl-common/process-add-event impl {"Log" [{"id" "LOG-50" "message" "root"}]}))
    [(xt/x:len out)
     (xtd/get-in impl ["rows"
                       "Log"
                       "LOG-50"
                       "record"
                       "data"
                       "message"])])
  => [0
      "root"])

^{:refer xt.db.system.impl-common/process-remove-event :added "4.1"}
(fact "process-remove-event removes nested data in lookup order"

  (!.js
    (var impl (main/create-impl "memory" {} -/Schema -/SchemaLookup))
    (impl-common/process-add-event impl {"Log" [{"id" "LOG-60" "message" "root"}]})
    [(impl-common/process-remove-event impl {"Log" [{"id" "LOG-60" "message" "root"}]})
     (xtd/get-in impl ["rows"
                       "Log"
                       "LOG-60"])])
  => [["Log"]
      nil])
