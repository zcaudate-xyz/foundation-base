(ns kmi.node-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true :langs [:js :lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.substrate :as substrate]
             [kmi.lang.runtime :as kmi]
             [kmi.node :as node]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer kmi.node/install :added "4.1"}
(fact "installs into an existing node without replacing unrelated state"
  (!.js
   (var n (substrate/node-create
           {"spaces" {"kmi.session" {"state" {"other" 42}}}
            "handlers" {"@demo/ping" {"fn" (fn [space args request handler-node]
                                                   (return {"value" "pong"}))}}}))
   (node/install n nil)
   [(xt/x:get-key (substrate/get-space-state n "kmi.session") "other")
    (xt/x:not-nil? (xt/x:get-key (substrate/get-space-state n "kmi.session") "runtime"))
    (xt/x:not-nil? (substrate/get-handler n "@demo/ping"))
    (xt/x:not-nil? (substrate/get-handler n "@kmi.lang/eval"))])
  => [42 true true true])

^{:refer kmi.node/eval-string :added "4.1"}
(fact "client eval preserves runtime state across calls"
  (notify/wait-on :js
   (var n (node/create-node {}))
   (-> (node/eval-string n "(def x 42)" nil)
       (promise/x:promise-then
        (fn [_]
          (return (node/eval-string n "x" nil))))
       (repl/notify)))
  => {"value" 42})

^{:refer kmi.node/load-string :added "4.1"}
(fact "client load evaluates all forms"
  (notify/wait-on :js
    (-> (node/load-string (node/create-node {}) "(def z 8) (+ z 2)" nil)
        (repl/notify)))
  => {"value" 10})

^{:refer kmi.node/read-string :added "4.1"}
(fact "client read returns a parsed form"
  (notify/wait-on :js
    (-> (node/read-string (node/create-node {}) "42" nil)
        (repl/notify)))
  => {"form" 42})

^{:refer kmi.node/describe-string :added "4.1"}
(fact "client describe returns managed metadata"
  (notify/wait-on :js
    (-> (node/describe-string (node/create-node {}) "[1 2 3]" nil)
        (repl/notify)))
  => {"tag" "vector" "type" "object" "size" 3 "string" "[1, 2, 3]"})
