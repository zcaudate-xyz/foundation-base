(ns xt.cell.kernel.inner-local-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.cell.kernel.inner-state :as inner-state]
             [xt.cell.kernel.inner-local :as inner-local]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.cell.kernel.inner-state :as inner-state]
             [xt.cell.kernel.inner-local :as inner-local]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.cell.kernel.inner-state :as inner-state]
             [xt.cell.kernel.inner-local :as inner-local]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.inner-local/actions-baseline :added "4.0"}
(fact "returns the base actions"

  (!.js (inner-local/actions-baseline))
  => map?

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@cell/ping"))
  => (contains {"is_async" false
                "args" []})

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@cell/echo"))
  => (contains {"is_async" false
                "args" ["arg"]})

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@cell/ping.async"))
  => (contains {"is_async" true
                "args" ["ms"]})

  (!.lua (inner-local/actions-baseline))
  => map?

  (!.lua (xt/x:get-key (inner-local/actions-baseline) "@cell/ping"))
  => (contains {"is_async" false
                "args" []})

  (!.lua (xt/x:get-key (inner-local/actions-baseline) "@cell/echo"))
  => (contains {"is_async" false
                "args" ["arg"]})

  (!.lua (xt/x:get-key (inner-local/actions-baseline) "@cell/ping.async"))
  => (contains {"is_async" true
                "args" ["ms"]})

  (!.py (inner-local/actions-baseline))
  => map?

  (!.py (xt/x:get-key (inner-local/actions-baseline) "@cell/ping"))
  => (contains {"is_async" false
                "args" []})

  (!.py (xt/x:get-key (inner-local/actions-baseline) "@cell/echo"))
  => (contains {"is_async" false
                "args" ["arg"]})

  (!.py (xt/x:get-key (inner-local/actions-baseline) "@cell/ping.async"))
  => (contains {"is_async" true
                "args" ["ms"]}))

^{:refer xt.cell.kernel.inner-local/actions-init :added "4.0"}
(fact "initialises baseline and custom actions"

  (!.js
   (inner-local/actions-init {"@custom/action" {}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@cell/ping"))
  => true

  (!.js
   (inner-local/actions-init {"@custom/action" {"handler" j/identity}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@custom/action"))
  => true

  (!.lua
   (inner-local/actions-init {"@custom/action" {}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@cell/ping"))
  => true

  (!.lua
   (inner-local/actions-init {"@custom/action" {"handler" j/identity}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@custom/action"))
  => true

  (!.py
   (inner-local/actions-init {"@custom/action" {}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@cell/ping"))
  => true

  (!.py
   (inner-local/actions-init {"@custom/action" {"handler" j/identity}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@custom/action"))
  => true)

(comment
  (s/snapto '[xt.cell.kernel.inner-local])
  
  (s/seedgen-langadd '[xt.cell.kernel.inner-local] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.cell.kernel.inner-local] {:lang [:lua :python] :write true}))
