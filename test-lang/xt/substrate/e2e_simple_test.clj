(ns xt.substrate.e2e-simple-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.sample.scratch-v0 :as v0]]})

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-memory :as transport-memory]
             [js.lib.driver-postgres :as js-postgres]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.db.text.sql-call :as call]
             [postgres.sample.scratch-v0.route-entries :as entries]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/with:print-all (l/rt:setup :postgres))]
  :teardown [(l/rt:stop)]})


(comment

  (event-node/node-create {})
  
  (notify/wait-on :js
    (-> (event-node/node-create
         {"handlers"
          {"db/ping"
           {"fn" (fn [space args request node]
                   (return
                    (-> (sql/connect
                         (js-postgres/driver)
                         {:database "test-scratch"})
                        (promise/x:promise-then
                         (fn [conn]
                           (return (call/call-raw conn entries/ping [])))))))
            "meta" {"kind" "request"}}}})
        (event-node/request "ANY"
                            "db/ping"
                            []
                            {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  

  )

(comment

  (v0/log-append "hello"))

(comment
  
  (notify/wait-on :js
    (-> (sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-raw conn entries/ping []))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  
  (notify/wait-on :js
    (-> (sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-api conn entries/log-append-public ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))

  (notify/wait-on :js
    (-> (sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-raw conn entries/log-append-public ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  
  
  (notify/wait-on :js
    (-> (sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-raw conn entries/log-append ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong"
  
  (notify/wait-on :js
    (-> (sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-api conn
                                  {:input []
                                   :return "text"
                                   :schema "scratch_v0"
                                   :id "ping"
                                   :flags {}}
                                  []))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  
  
  entries/log-append
  
  (v0/ping)
  
  (notify/wait-on :js
    (-> (js-postgres/connect-constructor {:database "test-scratch"})
        (promise/x:promise-then
         (fn [raw]
           (return (js-postgres/wrap-connection raw))))
        (promise/x:promise-then
         (fn [conn]
           (return (sql/query conn "SELECT scratch_v0.ping()"))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify result)))))
  
  (notify/wait-on :js
    (-> (js-postgres/connect-constructor {:database "test-scratch"})
        (promise/x:promise-then
         (fn [raw]
           (return (js-postgres/wrap-connection raw))))
        (promise/x:promise-then
         (fn [conn]
           (return (sql/query conn "SELECT * from \"scratch_v0\".\"Log\""))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify result)))))
  
  (repl/notify conn)
  (notify/wait-on [:js 5000]
    (promise/x:promise-then
     (js-postgres/connect-constructor {:database "test-scratch"})
     (fn [raw]
       (var conn (js-postgres/wrap-connection raw))
       (spec-promise/x:promise-then
        (sql/query conn "SELECT \"scratch\".addf(1,2);")
        (fn [out]
          (spec-promise/x:promise-then
           (sql/disconnect conn)
           (fn [_]
             (repl/notify [(sql/connection? conn)
                           out]))))))))

  
  (notify/wait-on :js
    (-> (event-node/node-create
         {"handlers"
          {"base/add"
           {"fn" (fn [space args request node]
                   (return
                    (xt/x:arr-foldl args
                                    (fn [a b]
                                      (return
                                       (+ a b)))
                                    0)))
            "meta" {"kind" "request"}}}})
        (event-node/request "ANY"
                            "base/add"
                            [1 2 3 4 5]
                            {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))

  (!.js
    (-> (event-node/node-create
         {"handlers"
          {"base/add"
           {"fn" (fn [space args request node]
                   (return
                    (xt/x:arr-foldl args
                                    (fn [a b]
                                      (return
                                       (+ a b)))
                                    0)))
            "meta" {"kind" "request"}}}})
        (event-node/get-handler "base/add")))
  (notify/wait-on :js
    )
  
  (!.js
    (xt/x:arr-foldl [1 2 3 4] (fn [a b]
                                (return
                                 (+ a b)))
                    0))
  
  (notify/wait-on :js
    
    (-> (event-node/node-create
         {"handlers"
          {"base/add"
           {"fn" (fn [space args request node]
                   (return
                    (xt/x:arr-foldl args 0 xt/x:add)))
            "meta" {"kind" "request"}}}}))))
