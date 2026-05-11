(ns xt.db.text.sql-call-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.model.spec-postgres.gen-bind :as gen]
            [xt.lang.common-notify :as notify]))

^{:seedgen/scaffold true}
(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :config {:dbname "test-scratch"}
     :require [[postgres.sample.scratch-v1 :as scratch]]}))

^{:seedgen/root {:all true
                 :js           {:extra [[js.lib.driver-sqlite :as js-sqlite]]}
                 :lua.nginx    {:extra [[lua.nginx.driver-sqlite :as lua-sqlite]]}
                 :python       {:extra [[python.lib.driver-sqlite :as py-sqlite]]}
                 :dart         {:extra [[dart.lib.driver-sqlite :as dart-sqlite]]}}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.db.text.sql-call :as call]
             [xt.protocol.impl.connection-sql :as driver]
             ^{:seedgen/extra true}
             [js.lib.driver-postgres :as js-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:setup :postgres))]
  :teardown [(do (l/rt:teardown :postgres))
             (l/rt:stop)]})

^{:refer xt.db.text.sql-call/decode-return :added "4.0"}
(fact "decodes the return value"

  (!.js
   (call/decode-return (xt/x:json-encode
                        {:status "ok"
                         :data 1})
                       nil))
  => 1

  (!.js
   (call/decode-return (xt/x:json-encode
                        {:status "error"
                         :data "NOT VALID"})
                       nil))
  => (throws))

^{:refer xt.db.text.sql-call/call-format-input :added "4.0"}
(fact "formats the inputs"

  (!.js
   (call/call-format-input {:input [{:type "numeric"}
                                    {:type "jsonb"}]}
                           [1
                            ["hello"]]))
  => ["'1'" "'[\"hello\"]'"])

^{:refer xt.db.text.sql-call/call-format-query :added "4.0"}
(fact "formats a query"

  (!.js
   (call/call-format-query
    (@! (gen/bind-function scratch/divf))
    [1 2]))
  => "SELECT \"scratch\".divf('1', '2');")

^{:refer xt.db.text.sql-call/call-raw :added "4.0"}
(fact "calls a database function"

  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}      
  (notify/wait-on :js
    (. (driver/connect (js-postgres/driver)
                       {:database "test-scratch"})
       (then
        (fn [conn]
          (. (call/call-raw
              conn
              (@! (gen/bind-function scratch/addf))
                                [10 20])
             (then (repl/>notify)))))))
  => "30")

^{:refer xt.db.text.sql-call/call-api
  :added "4.0"}
(fact "results an api style result"

  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on :js
    (. (driver/connect (js-postgres/driver)
                       {:database "test-scratch"})
       (then
        (fn [conn]
          (. (call/call-api conn
                            (@! (gen/bind-function scratch/addf))
                            [10 20])
             (then (repl/>notify)))))))
  => "{\"status\": \"ok\", \"data\":\"30\"}")
