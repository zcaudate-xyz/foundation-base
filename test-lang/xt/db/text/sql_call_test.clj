(ns xt.db.text.sql-call-test
  (:use code.test)
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as gen]
            [xt.lang.common-notify :as notify]))

^{:seedgen/scaffold true}
(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :config {:dbname "test-scratch"}
     :require [[postgres.sample.scratch-v1 :as scratch]]}))

^{:seedgen/root {:all true
                 :js           {:extra [[js.net.conn-postgres :as js-postgres]]}
                 :lua.nginx    {:extra [[lua.nginx.driver-postgres :as lua-postgres]]}
                 :python       {:extra [[python.net.conn-postgres :as py-postgres]]}
                 :dart         {:extra [[dart.net.conn-postgres :as dart-postgres]]}}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.db.text.sql-call :as call]
             [xt.net.conn-sql :as conn-sql]
             ^{:seedgen/extra true}
             [js.net.conn-postgres :as js-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
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

  ^{:seedgen/base {:lua.nginx    {:transform '{js-postgres/create lua-postgres/create}}
                   :python       {:transform '{js-postgres/create py-postgres/create}}
                   :dart         {:transform '{js-postgres/create dart-postgres/create}}}}      
  (notify/wait-on :js
    (-> (js-postgres/create {:database "test-scratch"})
        (conn-sql/connect {:host "127.0.0.1",
                           :port 5432
                           :user "postgres",
                           :password "postgres"})
        (spec-promise/x:promise-then
         (fn [client]
           (return
            (call/call-raw client
                           {:input [{:symbol "x" :type "numeric"}
                                     {:symbol "y" :type "numeric"}]
                            :return "numeric"
                            :schema "scratch"
                            :id "addf"
                            :flags {}}
                           [10 20]))))
        (spec-promise/x:promise-then
         (fn [x]
           (repl/notify x)))
        (spec-promise/x:promise-catch
         (fn [err]
           (repl/notify (. err message))))))
  => 30)

^{:refer xt.db.text.sql-call/call-api
  :added "4.0"}
(fact "results an api style result"

  ^{:seedgen/base {:lua.nginx    {:transform '{js-postgres/create lua-postgres/create}}
                   :python       {:transform '{js-postgres/create py-postgres/create}}
                   :dart         {:transform '{js-postgres/create dart-postgres/create}}}}
  (notify/wait-on :js
    (-> (js-postgres/create {:database "test-scratch"})
        (conn-sql/connect)
        (spec-promise/x:promise-then
         (fn [client]
           (return
            (call/call-api client
                           {:input [{:symbol "x" :type "numeric"}
                                    {:symbol "y" :type "numeric"}]
                            :return "numeric"
                            :schema "scratch"
                            :id "addf"
                            :flags {}}
                           [10 20]))))
        (spec-promise/x:promise-then
         (fn [x]
           (repl/notify (xt/x:json-decode x))))
        (spec-promise/x:promise-catch
         (fn [err]
           (repl/notify (. err message))))))
  => {"status" "ok", "data" 30})

(comment
  (s/seedgen-benchadd '[xt.db.text.sql-call] {:lang [:python :lua.nginx] :write true})
  )
