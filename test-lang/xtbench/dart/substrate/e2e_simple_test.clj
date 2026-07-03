(ns xtbench.dart.substrate.e2e-simple-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.net.conn-sql :as conn-sql]
             [xt.db.text.sql-call :as call]
             [xt.db.substrate :as db-helper]
             [js.net.conn-postgres :as js-postgres]
             [postgres.sample.scratch-v0.route-entries :as entries]]})

(fact:global
 {:setup [(l/rt:restart)
                  (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:description "create a xt.substrate node"}
(fact ""

  (!.js
    (substrate/node-create
     {"handlers"
      {"fn/ping"
       {"fn" (fn [space args request node]
               (return "pong"))}}}))
  (notify/wait-on :js
    (-> (substrate/node-create
         {"handlers"
          {"fn/ping"
           {"fn" (fn [space args request node]
                   (return "pong"))}}})
        (substrate/request nil
                           "fn/ping"
                           []
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong"

  (notify/wait-on :js
    (-> (substrate/node-create
         {"handlers"
          {"@xt.db/kernel-init"
           {"fn" (fn [space args request node]
                   (substrate/set-service node "db.primary"))}}})
        (substrate/request nil
                           "@xt.db/kernel-init"
                           []
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out))))))

(comment
  
  
  
  
  
  (defn )
  (page-core/group-add-attach
   node
   "space/a"
   "entries"
   {"ping" {"handler" (create-call-request
                       "entries/ping"
                       "db/fn.primary"
                       "action/ping.primary")
            "defaults" {"args" []}}})
  

  (notify/wait-on :js
    (var node
         (substrate/node-create
          {"services"
           {"db/primary"
            {"database" "test-scratch"}}
           "handlers"
           {"db/fn.primary"
            {"fn"   (db-helper/call-db-handler (js-postgres/create {:database "test-scratch"}) "db/primary")
             "meta" {"kind" "request"}}}}))
    (page-core/group-add-attach node
                                nil
                                "page"
                                {"ping" (db-helper/call-view-request entries/ping
                                                                     "ping"
                                                                     "db/fn.primary"
                                                                     {})}))
  
  
  (notify/wait-on :js
    (-> (substrate/node-create
         {"services"
          {"db/primary"
           {"database" "test-scratch"}}
          "handlers"
          {"db/fn.primary"
           {"fn" (fn [space args request node]
                   (var opts (xt/x:first args))
                   (var fn-template (. opts ["template"]))
                   (var fn-args     (. opts ["args"]))
                   (return
                    (-> (conn-sql/connect
                         (driver/get-driver "postgres")
                         (substrate/get-service node "db/primary"))
                        (promise/x:promise-then
                         (fn [conn]
                           (return (call/call-raw conn fn-template fn-args)))))))
            "meta" {"kind" "request"}}}})
        (substrate/request nil
                           "db/fn.primary"
                           [{"template" entries/ping
                             "args" []}]
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  
  
  (notify/wait-on :js
    (-> (substrate/node-create
         {"handlers"
          {"db/ping"
           {"fn" (fn [space args request node]
                   (return request))
            "meta" {"kind" "request"}}}})
        (substrate/request nil
                           "db/ping"
                           []
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"space" "__NODE__",
      "args" [],
      "id" "req-VCsVVv",
      "action" "db/ping",
      "kind" "request",
      "meta" {}}
  
  {"space" "ANY", "args" [], "id" "req-QigeQW", "action" "db/ping", "kind" "request", "meta" {}}

  
  (notify/wait-on :js
    (-> (substrate/node-create
         {"handlers"
          {"db/ping"
           {"fn" (fn [space args request node]
                   (return
                    (-> (conn-sql/connect
                         (js-postgres/driver)
                         {:database "test-scratch"})
                        (promise/x:promise-then
                         (fn [conn]
                           (return (call/call-raw conn entries/ping [])))))))
            "meta" {"kind" "request"}}}})
        (substrate/request "ANY"
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
    (-> (conn-sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-raw conn entries/ping []))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  
  (notify/wait-on :js
    (-> (conn-sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-api conn entries/log-append-public ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))

  (notify/wait-on :js
    (-> (conn-sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-raw conn entries/log-append-public ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  
  
  (notify/wait-on :js
    (-> (conn-sql/connect (js-postgres/driver)
                     {:database "test-scratch"})
        (promise/x:promise-then
         (fn [conn]
           (return (call/call-raw conn entries/log-append ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong"
  
  (notify/wait-on :js
    (-> (conn-sql/connect (js-postgres/driver)
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
    (-> (js-postgres/create {:database "test-scratch"})
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [conn]
           (return (conn-sql/query conn "SELECT scratch_v0.ping()"))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify result)))))
  
  (notify/wait-on :js
    (-> (js-postgres/create {:database "test-scratch"})
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [conn]
           (return (conn-sql/query conn "SELECT * from \"scratch_v0\".\"Log\""))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify result)))))
  
  (repl/notify conn)
  (notify/wait-on [:js 5000]
    (-> (js-postgres/create {:database "test-scratch"})
     (conn-sql/connect)
     (spec-promise/x:promise-then
      (fn [conn]
        (spec-promise/x:promise-then
         (conn-sql/query conn "SELECT \"scratch\".addf(1,2);")
         (fn [out]
           (spec-promise/x:promise-then
            (conn-sql/disconnect conn)
            (fn [_]
              (repl/notify [true
                            out])))))))))

  
  (notify/wait-on :js
    (-> (substrate/node-create
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
        (substrate/request "ANY"
                            "base/add"
                            [1 2 3 4 5]
                            {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))

  (!.js
    (-> (substrate/node-create
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
        (substrate/get-handler "base/add")))
  (notify/wait-on :js
    )
  
  (!.js
    (xt/x:arr-foldl [1 2 3 4] (fn [a b]
                                (return
                                 (+ a b)))
                    0))
  
  (notify/wait-on :js
    
    (-> (substrate/node-create
         {"handlers"
          {"base/add"
           {"fn" (fn [space args request node]
                   (return
                    (xt/x:arr-foldl args 0 xt/x:add)))
            "meta" {"kind" "request"}}}}))))
