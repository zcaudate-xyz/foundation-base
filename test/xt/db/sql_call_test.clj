(ns xt.db.sql-call-test
  (:use code.test)
  (:require [rt.postgres :as pg]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres.test.scratch-v1 :as scratch]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]
             [xt.db.sql-call :as call]
             [xt.sys.conn-dbsql :as driver]
             [js.lib.driver-postgres :as js-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.sql-call/decode-return :added "4.0"}
(fact "decodes the return value"
  ^:hidden
  
  (!.js
   (call/decode-return (xt/x:json-encode
                        {:status "ok"
                         :data 1})))
  => 1

  (!.js
   (call/decode-return (xt/x:json-encode
                        {:status "error"
                         :data "NOT VALID"})))
  => (throws))

^{:refer xt.db.sql-call/call-format-input :added "4.0"}
(fact "formats the inputs"
  ^:hidden
  
  (!.js
   (call/call-format-input {:input [{:type "numeric"}
                                    {:type "jsonb"}]}
                           [1
                            ["hello"]]))
  => ["'1'" "'[\"hello\"]'"])

^{:refer xt.db.sql-call/call-format-query :added "4.0"}
(fact "formats a query"
  ^:hidden
  
  (!.js
   (call/call-format-query
    (@! (pg/bind-function scratch/divf))
    [1 2]))
  => "SELECT \"scratch\".divf('1', '2');")

^{:refer xt.db.sql-call/call-raw :added "4.0"}
(fact "calls a database function"
  ^:hidden
  
  (notify/wait-on :js
    (driver/connect {:constructor js-postgres/connect-constructor
                     :database "test-scratch"}
                    {:success
                     (fn [conn]
                       (. (call/call-raw
                           conn
                           (@! (pg/bind-function scratch/addf))
                           [10 20])
                          (then (repl/>notify))))}))
  => "30")

^{:refer xt.db.sql-call/call-api :added "4.0"}
(fact "results an api style result"
  ^:hidden
  
  (notify/wait-on :js
    (driver/connect {:constructor js-postgres/connect-constructor
                     :database "test-scratch"}
                    {:success
                     (fn [conn]
                       (. (call/call-api conn
                                         (@! (pg/bind-function scratch/addf))
                                         [10 20])
                          (then (repl/>notify))))}))
  => "{\"status\": \"ok\", \"data\":\"30\"}")
