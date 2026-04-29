(ns xt.cell.kernel.inner-local-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:require [[xt.lang.spec-base :as xt] [xt.cell.kernel.inner-state :as inner-state] [xt.cell.kernel.inner-local :as inner-local] [js.core :as j]] :runtime :basic})

(fact:global
 {:setup [(l/rt:restart)
                 (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.inner-local/actions-baseline :added "4.0"}
(fact "returns the base actions"

  (!.js (inner-local/actions-baseline))
  => map?

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@worker/ping"))
  => (contains {"is_async" false
                "args" []})

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@worker/echo"))
  => (contains {"is_async" false
                "args" ["arg"]})

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@worker/ping.async"))
  => (contains {"is_async" true
                "args" ["ms"]}))

^{:refer xt.cell.kernel.inner-local/actions-init :added "4.0"}
(fact "initialises baseline and custom actions"

  (!.js
   (inner-local/actions-init {"@custom/action" {}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@worker/ping"))
  => true

  (!.js
   (inner-local/actions-init {"@custom/action" {"handler" j/identity}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@custom/action"))
  => true)

^{:refer xt.cell.kernel.inner-local/tmpl-baseline-action :added "4.0"}
(fact "templates baseline function metadata"

  (let [result (inner-local/tmpl-baseline-action
                @xt.cell.kernel.inner-state/fn-ping)]
    (first result)
    => "@worker/ping"
        
    (get (second result) :is-async)
    => false
        
    (get-in (second result) [:handler])
    => 'xt.cell.kernel.inner-state/fn-ping)

  (let [result (inner-local/tmpl-baseline-action
                @xt.cell.kernel.inner-state/fn-trigger)]
    (first result)
    => "@worker/trigger"
        
    (get-in (second result) [:handler])
    => '(xt.cell.kernel.inner-state/fn-self
         xt.cell.kernel.inner-state/fn-trigger)))
