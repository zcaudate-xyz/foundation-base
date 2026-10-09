(ns xt.substrate.request-promise-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-util/request :added "4.1"}
(fact "settles linked requests through promise resolver callbacks"

  (notify/wait-on :js
    (var server
         (event-node/node-create
          {"id" "request-server"
           "handlers"
           {"demo/echo"
            {"fn" (fn [space args request server-node]
                    (return {"echo" args}))
             "meta" {"kind" "request"}}}}))
    (var client (event-node/node-create {"id" "request-client"}))
    (-> (transport-memory/link-pair server client)
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request client
                                "room/a"
                                "demo/echo"
                                ["ping"]
                                nil))))
        (repl/notify)))
  => {"echo" ["ping"]})
