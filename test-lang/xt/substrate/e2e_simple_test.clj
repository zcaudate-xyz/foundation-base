(ns xt.substrate.e2e-simple-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-memory :as transport-memory]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(comment

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
  
  ^*(!.js
    (xt/x:arr-foldl [1 2 3 4] 0 (fn [a b]
                                  (return
                                   (+ a b)))))
  
  (notify/wait-on :js
    
    (-> (event-node/node-create
         {"handlers"
          {"base/add"
           {"fn" (fn [space args request node]
                   (return
                    (xt/x:arr-foldl args 0 xt/x:add)))
            "meta" {"kind" "request"}}}}))))
