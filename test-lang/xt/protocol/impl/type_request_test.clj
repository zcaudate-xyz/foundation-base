(ns xt.protocol.impl.type-request-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.type-request :as req]
             [xt.event.node :as node]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.type-request :as req]
             [xt.event.node :as node]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.type-request :as req]
             [xt.event.node :as node]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.type-request/request-runtime-create :added "4.1"}
(fact "wraps node request functions behind the request protocol"

  (!.js
    (var runtime
         (req/request-runtime-create
          {:request node/request
           :receive_request node/receive-request
           :receive_response node/receive-response
           :respond_ok node/respond-ok
           :respond_error node/respond-error}))
    [(req/request-runtime? runtime)
     (req/request-runtime? nil)])
  => [true false]

  (l/with:print-all
    (notify/wait-on :js
      (do
        (var runtime
             (req/request-runtime-create
              {:request node/request
               :receive_request node/receive-request
               :receive_response node/receive-response
               :respond_ok node/respond-ok
               :respond_error node/respond-error}))
        (var n (node/node-create {}))
        (node/register-handler
         n
         "echo"
         (fn [space args request node]
           (return {:space (. space ["id"])
                    :args args}))
         nil)
        (promise/x:promise-then
         (req/request runtime n "room/a" "echo" [1 2] nil)
         (repl/>notify)))))
  => {:space "room/a"
      :args [1 2]})
