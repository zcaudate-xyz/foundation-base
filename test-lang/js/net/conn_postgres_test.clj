(ns js.net.conn-postgres-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

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
   :config {:host   (-> docker-min/+config+ :db :host)
            :port   (-> docker-min/+config+ :db :port)
            :user   (-> docker-min/+config+ :db :user)
            :pass   (-> docker-min/+config+ :db :password)
            :dbname (-> docker-min/+config+ :db :database)
            :startup  docker-min/start-supabase
            :shutdown docker-min/stop-supabase}})

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
         
         (@! (docker-min/+config+ :db)))
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
         
         (@! (docker-min/+config+ :db)))
        (promise/x:promise-then
         (fn [client]
           (var #{raw} client)
           (return
            (. raw (query "SELECT 1;")))))
        (promise/x:promise-then
         (fn [out]
           (return
            (repl/notify out)))))))

^{:refer js.net.conn-postgres/create-methods :added "4.1"}
(fact "create methods for connecting"
  
  (!.js
    (tree/tree-get-spec
     (js-postgres/create-methods)))
  => {"query_async" "function", "disconnect" "function", "query" "function", "connect" "function"})

^{:refer js.net.conn-postgres/create :added "4.1"}
(fact "creates a postgres connection"

  (!.js
    (js-postgres/create
     (@! (docker-min/+config+ :db))))
  => {"::" "js.net.conn-postgres",
      "defaults" {"host" "127.0.0.1", "user" "postgres", "database" "postgres", "port" 55122, "password" "postgres"}}

  (notify/wait-on :js
    (-> (js-postgres/create
         (@! (docker-min/+config+ :db)))
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
