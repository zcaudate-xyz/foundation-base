(ns xt.substrate.s01-basic-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

;; create 

^{:refer xt.substrate.walkthrough.s01-basic-test/f00-ping}
(fact "the simplest handler"

  ;;
  ;; simplest configuration
  ;;
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
        (repl/notify)))
  => "pong"
  

  ;;
  ;; using xt.substrate register-handler api
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create))
    (substrate/register-handler
     node
     "fn/ping"
     (fn [space args request node]
       (return "pong"))
     {})
    (-> (substrate/request node
                           nil
                           "fn/ping"
                           []
                           {})
        (repl/notify)))
  => "pong")


^{:refer xt.substrate.walkthrough.s01-basic-test/f01-echo}
(fact "the simplest handler with arguments"

  (notify/wait-on :js
    (-> (substrate/node-create
         {"handlers"
          {"fn/echo"
           {"fn" (fn [space args request node]
                   (return args))}}})
        (substrate/request nil "fn/echo" [1 2 3] {})
        (repl/notify)))
  => [1 2 3])

^{:refer xt.substrate.walkthrough.s01-basic-test/f02-space-state}
(fact "spaces carry state that handlers can read and update"
  
  (notify/wait-on :js
    (var node (substrate/node-create
               {"spaces"
                {"default::space" {"state" {"count" 0}}}
                "handlers"
                {"counter/get"
                 {"fn" (fn [space args request node]
                         (return (substrate/get-space-state node
                                                            (. space ["id"]))))}
                 "counter/inc"
                 {"fn" (fn [space args request node]
                         (return
                          (substrate/update-space-state
                           node
                           (. space ["id"])
                           (fn [state space node]
                             (return {"count" (+ (or (. state ["count"]) 0)
                                                 1)})))))}}}))
    (-> (promise/x:promise-all
         [(substrate/request node "default::space" "counter/inc" [] {})
          (substrate/request node "default::space" "counter/inc" [] {})
          (substrate/request node "default::space" "counter/inc" [] {})])
        (repl/notify)))
  => [{"count" 1} {"count" 2} {"count" 3}])
