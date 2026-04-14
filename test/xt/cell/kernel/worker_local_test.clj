(ns xt.cell.kernel.worker-local-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
              [js.core :as j]
              [xt.cell.kernel.worker-state :as worker-state]
              [xt.cell.kernel.worker-local :as worker-local]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer xt.cell.kernel.worker-local/actions-baseline :added "4.0" :unchecked true}
(fact "returns the base actions"
  ^:hidden
  
  (!.js (worker-local/actions-baseline))
  => map?
  
  ;; Check that @worker/ping action exists with correct structure
  (!.js (k/get-key (worker-local/actions-baseline) "@worker/ping"))
  => (contains {"is_async" false
                "args" []})
  
  ;; Check that @worker/echo action exists
  (!.js (k/get-key (worker-local/actions-baseline) "@worker/echo"))
  => (contains {"is_async" false
                "args" ["arg"]})
  
  ;; Check that @worker/ping.async action exists and is async
  (!.js (k/get-key (worker-local/actions-baseline) "@worker/ping.async"))
  => (contains {"is_async" true
                "args" ["ms"]}))

^{:refer xt.cell.kernel.worker-local/actions-init :added "4.0" :unchecked true}
(fact "initiates the base actions"
  ^:hidden
  
  ;; Initialize actions
  (!.js
   (worker-local/actions-init {"@custom/action" {}} nil)
   ;; Check that baseline actions are registered
   (xt/x:has-key? (worker-state/WORKER_ACTIONS) "@worker/ping"))
  => true
  
  ;; Check that custom action is also registered
  (!.js
   (worker-local/actions-init {"@custom/action" {"handler" k/identity}} nil)
   (xt/x:has-key? (worker-state/WORKER_ACTIONS) "@custom/action"))
  => true)

^{:refer xt.cell.kernel.worker-local/tmpl-baseline-action :added "4.0" :unchecked true}
(fact "templates a baseline function"
  ^:hidden

  ;; Test with a static function (no self parameter)
  (let [result (worker-local/tmpl-baseline-action
                @xt.cell.kernel.worker-state/fn-ping)]
    (first result)
    => "@worker/ping"
    
    (get (second result) :is-async)
    => false
    
    (get-in (second result) [:handler])
    => 'xt.cell.kernel.worker-state/fn-ping)
  
  ;; Test with non-static function (has self parameter, needs fn-self wrapper)
  (let [result (worker-local/tmpl-baseline-action
                @xt.cell.kernel.worker-state/fn-trigger)]
    (first result)
    => "@worker/trigger"
    
    ;; Handler should be wrapped with fn-self
    (get-in (second result) [:handler])
    => '(xt.cell.kernel.worker-state/fn-self
         xt.cell.kernel.worker-state/fn-trigger)))
