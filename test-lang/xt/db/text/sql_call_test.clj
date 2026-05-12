(ns xt.db.text.sql-call-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.model.spec-postgres.gen-bind :as gen]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise]))

^{:seedgen/scaffold true}
(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :config {:dbname "test-scratch"}
     :require [[postgres.sample.scratch-v1 :as scratch]]}))

^{:seedgen/root {:all true
                 :js           {:extra [[js.lib.driver-postgres :as js-postgres]]}
                 :lua.nginx    {:extra [[lua.nginx.driver-postgres :as lua-postgres]]}
                 :python       {:extra [[python.lib.driver-postgres :as py-postgres]]}
                 :dart         {:extra [[dart.lib.driver-postgres :as dart-postgres]]}}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
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

  ^{:seedgen/base {:lua.nginx    {:transform '{(js-postgres/driver) (lua-postgres/driver)}}
                   :python       {:transform '{(js-postgres/driver) (py-postgres/driver)}}
                   :dart         {:transform '{(js-postgres/driver) (dart-postgres/driver)}}}}      
  (notify/wait-on :js
    (spec-promise/x:promise-then
     (driver/connect (js-postgres/driver)
                     {:database "test-scratch"})
     (fn [conn]
       (spec-promise/x:promise-then
        (call/call-raw conn
                       {:input [{:symbol "x" :type "numeric"}
                                {:symbol "y" :type "numeric"}]
                        :return "numeric"
                        :schema "scratch"
                        :id "addf"
                        :flags {}}
                       [10 20])
        (repl/>notify)))))
  => "30")

^{:refer xt.db.text.sql-call/call-api
  :added "4.0"}
(fact "results an api style result"

  ^{:seedgen/base {:lua.nginx    {:transform '{(js-postgres/driver) (lua-postgres/driver)}}
                   :python       {:transform '{(js-postgres/driver) (py-postgres/driver)}}
                   :dart         {:transform '{(js-postgres/driver) (dart-postgres/driver)}}}}
  (notify/wait-on :js
    (spec-promise/x:promise-then
     (driver/connect (js-postgres/driver)
                     {:database "test-scratch"})
     (fn [conn]
       (spec-promise/x:promise-then
        (call/call-api conn
                       {:input [{:symbol "x" :type "numeric"}
                                {:symbol "y" :type "numeric"}]
                        :return "numeric"
                        :schema "scratch"
                        :id "addf"
                        :flags {}}
                       [10 20])
        (repl/>notify)))))
  => "{\"status\": \"ok\", \"data\":\"30\"}")

(comment
  (s/seedgen-benchadd '[xt.db.text.sql-call] {:lang [:python :lua.nginx] :write true})
  )
