(ns xtbench.lua.db.text.sql-call-test
  (:use code.test)
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as gen]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise]))

^{:seedgen/scaffold true}
(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :config {:dbname "test-scratch"}
     :require [[postgres.sample.scratch-v1 :as scratch]]}))

(l/script- :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
          [xt.lang.spec-promise :as spec-promise]
          [xt.lang.common-repl :as repl]
          [xt.db.text.sql-call :as call]
          [xt.net.conn-sql :as conn-sql]
          [lua.nginx.conn-postgres :as lua-postgres]]
          :runtime :basic})

(fact:global
 {:setup [(l/rt:restart)
                  (do (l/rt:setup :postgres))]
  :teardown [(do (l/rt:teardown :postgres))
                         (l/rt:stop)]})

^{:refer xt.db.text.sql-call/decode-return :added "4.0"}
(fact "decodes the return value"

  (!.lua
   (call/decode-return (xt/x:json-encode
                        {:status "ok"
                         :data 1})
                       nil))
  => 1

  (!.lua
   (call/decode-return (xt/x:json-encode
                        {:status "error"
                         :data "NOT VALID"})
                       nil))
  => (throws))

^{:refer xt.db.text.sql-call/call-format-input :added "4.0"}
(fact "formats the inputs"

  (!.lua
   (call/call-format-input {:input [{:type "numeric"}
                                    {:type "jsonb"}]}
                           [1
                            ["hello"]]))
  => ["'1'" "'[\"hello\"]'"])

^{:refer xt.db.text.sql-call/call-format-query :added "4.0"}
(fact "formats a query"

  (!.lua
   (call/call-format-query
    (@! (gen/bind-function scratch/divf))
    [1 2]))
  => "SELECT \"scratch\".divf('1', '2');")

^{:refer xt.db.text.sql-call/call-raw :added "4.0"}
(fact "calls a database function"

  (notify/wait-on :lua.nginx
    (spec-promise/x:promise-then
     (conn-sql/connect (lua-postgres/create {:database "test-scratch"})
                     {})
     (fn [conn]
       (return
        (spec-promise/x:promise-then
         (call/call-raw conn
                        {:input [{:symbol "x" :type "numeric"}
                                 {:symbol "y" :type "numeric"}]
                         :return "numeric"
                         :schema "scratch"
                         :id "addf"
                         :flags {}}
                        [10 20])
         (fn [x]
           (repl/notify x)))))))
  => 30)

^{:refer xt.db.text.sql-call/call-api :added "4.0"}
(fact "results an api style result"

  (notify/wait-on :lua.nginx
    (spec-promise/x:promise-then
     (driver/connect (lua-postgres/driver)
                     {:database "test-scratch"})
     (fn [conn]
        (return
         (spec-promise/x:promise-then
          (call/call-api conn
                         {:input [{:symbol "x" :type "numeric"}
                                  {:symbol "y" :type "numeric"}]
                          :return "numeric"
                          :schema "scratch"
                          :id "addf"
                          :flags {}}
                         [10 20])
          (fn [x]
            (repl/notify (xt/x:json-decode x))))))))
  => {"status" "ok", "data" 30})

(comment
  (s/seedgen-benchadd '[xt.db.text.sql-call] {:lang [:python :lua.nginx] :write true})
  )
