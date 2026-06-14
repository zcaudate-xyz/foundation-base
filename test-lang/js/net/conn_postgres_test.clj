(ns js.net.conn-postgres-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(l/script- :js
  {:runtime :basic
   :require [[xt.net.conn-sql :as conn-sql]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]
             [js.net.conn-postgres :as js-postgres]]})

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0 :as scratch-v0]]
   :config {:host   (-> local-min/+config+ :db :host)
            :port   (-> local-min/+config+ :db :port)
            :user   (-> local-min/+config+ :db :user)
            :pass   (-> local-min/+config+ :db :password)
            :dbname (-> local-min/+config+ :db :database)
            :startup  local-min/start-supabase
            :shutdown local-min/stop-supabase}})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.net.conn-postgres/coerce-number-string :added "4.1"}
(fact "coerce to number if number"

  (!.js
    [(js-postgres/coerce-number-string "1")
     (js-postgres/coerce-number-string "1.234")
     (js-postgres/coerce-number-string "abcd")])
  => [1 1.234 "abcd"])

^{:refer js.net.conn-postgres/normalise-scalar-output :added "4.1"}
(fact "")

^{:refer js.net.conn-postgres/normalise-query-output :added "4.1"}
(fact "normalises the query output")

^{:refer js.net.conn-postgres/client-connect :added "4.1"}
(fact "creates a pg connection to the client"

  (notify/wait-on :js
    (-> (js-postgres/client-connect
         
         (@! (local-min/+config+ :db)))
        (promise/x:promise-then
         (fn [client]
           (var #{raw} client)
           (return
            (. raw
               (query "SELECT 1;")
               (then js-postgres/normalise-query-output)))))
        (promise/x:promise-then
         (fn [out]
           (return
            (repl/notify out))))))
  => 1

  (notify/wait-on :js
    (-> (js-postgres/client-connect
         
         (@! (local-min/+config+ :db)))
        (promise/x:promise-then
         (fn [client]
           (var #{raw} client)
           (return
            (. raw (query "SELECT 1;")))))
        (promise/x:promise-then
         (fn [out]
           (return
            (repl/notify out))))))
  => map?)

^{:refer js.net.conn-postgres/client-disconnect :added "4.1"}
(fact "disconnects the underlying raw client"

  (notify/wait-on :js
    (-> (js-postgres/create
         (@! (local-min/+config+ :db)))
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (return
            (js-postgres/client-disconnect client))))
        (promise/x:promise-then
         (fn [client]
           (repl/notify (xt/x:has-key? client "raw"))))))
  => false)

^{:refer js.net.conn-postgres/client-query-async :added "4.1"}
(fact "runs an async query against the live postgres connection"

  (notify/wait-on :js
    (-> (js-postgres/create
         (@! (local-min/+config+ :db)))
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (return
            (js-postgres/client-query-async client "SELECT 1;"))))
        (promise/x:promise-then
         (fn [out]
           (return
            (repl/notify out))))))
  => 1)

^{:refer js.net.conn-postgres/create :added "4.1"}
(fact "creates a postgres connection"

  (!.js
    (js-postgres/create
     (@! (local-min/+config+ :db))))
  => {"::" "js.net.conn_postgres/PostgresClient",
      "::/protocols" ["xt.net.conn_sql/ISqlClient"],
      "raw" nil,
      "defaults"
      {"host" "127.0.0.1",
       "user" "postgres",
       "database" "postgres",
       "port" 55122,
       "password" "postgres"}}
  

  (notify/wait-on :js
    (-> (js-postgres/create
         (@! (local-min/+config+ :db)))
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (return
            (conn-sql/query-async client "SELECT 1;"))))
        (promise/x:promise-then
         (fn [out]
           (return
            (repl/notify out))))))
  => 1)
