(ns xt.db.substrate-test
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
             [xt.substrate.base-page :as base-page]
             [xt.substrate.transport-memory :as transport-memory]
             [js.lib.driver-postgres :as js-postgres]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.db.text.sql-call :as call]
             [xt.db.runtime.driver :as driver]
             [xt.db.substrate :as db-helper]
             [postgres.sample.scratch-v0.route-entries :as entries]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/with:print-all (l/rt:setup :postgres))]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.substrate/call-db-handler :added "4.1"}
(fact "creates the db helper for sql"

  (notify/wait-on :js
    (-> (event-node/node-create
         {"services"
          {"db/primary"
           {"database" "test-scratch"}}
          "handlers"
          {"db/fn.primary"
           {"fn"   (db-helper/call-db-handler js-postgres/driver "db/primary")
            "meta" {"kind" "request"}}}})
        (event-node/request nil
                            "db/fn.primary"
                            [{"template" entries/ping
                              "args" []}]
                            {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong")

^{:refer xt.db.substrate/call-view-request :added "4.1"}
(fact "TODO"
  
  (notify/wait-on :js
    (var node
         (event-node/node-create
          {"services"
           {"db/primary"
            {"database" "test-scratch"}}
           "handlers"
           {"db/fn.primary"
            {"fn"   (db-helper/call-db-handler js-postgres/driver "db/primary")
             "meta" {"kind" "request"}}}}))
    (base-page/add-model-attach node
                                nil
                                "page"
                                {"ping" (db-helper/call-view-request entries/ping
                                                                     "ping"
                                                                     "db/fn.primary"
                                                                     {})}))

  )



(comment
  
  (defn.xt call-view-request
    [template handler-id action-id meta]
    (return
     (fn [context]
       (var space   (. context ["space"]))
       (var args    (. context ["args"]))
       (var request {"action" action-id
                     "space"  (get space "id")
                     "args"   args})
       (var node    (. context ["node"]))
       (return
        (substrate/request node
                           (. opts ["id"])
                           handler-id
                           [{"args" (. context ["args"])
                             "template" template}]
                           meta))))))
