(ns xt.cell.kernel.inner-local-test
  (:require [hara.lang :as l])
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

  (!.js (xt/x:has-key? (inner-local/actions-baseline) "@cell/ping"))
  => true

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@cell/ping"))
  => (contains {"static" true
                "is_async" false
                "args" []})

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@cell/echo"))
  => (contains {"static" true
                "is_async" false
                "args" ["arg"]})

  (!.js (xt/x:get-key (inner-local/actions-baseline) "@cell/ping.async"))
  => (contains {"static" true
                "is_async" true
                "args" ["ms"]})

  (!.lua (xt/x:has-key? (inner-local/actions-baseline) "@cell/ping"))
  => true

  (!.lua (xt/x:get-key
          (xt/x:get-key (inner-local/actions-baseline) "@cell/ping")
          "is_async"))
  => false

  (!.lua (xt/x:get-key
          (xt/x:get-key (inner-local/actions-baseline) "@cell/echo")
          "args"))
  => ["arg"]

  (!.lua (xt/x:get-key
          (xt/x:get-key (inner-local/actions-baseline) "@cell/ping.async")
          "static"))
  => true

  (!.py (xt/x:has-key? (inner-local/actions-baseline) "@cell/ping"))
  => true

  (!.py (xt/x:get-key
         (xt/x:get-key (inner-local/actions-baseline) "@cell/ping")
         "is_async"))
  => false

  (!.py (xt/x:get-key
         (xt/x:get-key (inner-local/actions-baseline) "@cell/echo")
         "args"))
  => ["arg"]

  (!.py (xt/x:get-key
         (xt/x:get-key (inner-local/actions-baseline) "@cell/ping.async")
         "static"))
  => true)

^{:refer xt.cell.kernel.inner-local/actions-init :added "4.0"}
(fact "initialises baseline and custom actions"

  (!.js
   (inner-local/actions-init {"@custom/action" {}} nil)
   (xt/x:has-key? (inner-state/INNER_ACTIONS) "@cell/ping"))
  => true

  (!.js
   (inner-local/actions-init {"@custom/action" {"handler" (fn [x] (return x))}} nil)
   (xt/x:has-key? (inner-state/INNER_ACTIONS) "@custom/action"))
  => true

  (!.lua
   (inner-local/actions-init {"@custom/action" {}} nil)
   (xt/x:has-key? (inner-state/INNER_ACTIONS) "@cell/ping"))
  => true

  (!.lua
   (inner-local/actions-init {"@custom/action" {"handler" (fn [x] (return x))}} nil)
   (xt/x:has-key? (inner-state/INNER_ACTIONS) "@custom/action"))
  => true

  (!.py
   (inner-local/actions-init {"@custom/action" {}} nil)
   (xt/x:has-key? (inner-state/INNER_ACTIONS) "@cell/ping"))
  => true

  (!.py
   (inner-local/actions-init {"@custom/action" {"handler" (fn [x] (return x))}} nil)
   (xt/x:has-key? (inner-state/INNER_ACTIONS) "@custom/action"))
  => true)

(comment
  (s/snapto '[xt.cell.kernel.inner-local])
  
  (s/seedgen-langadd '[xt.cell.kernel.inner-local] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.cell.kernel.inner-local] {:lang [:lua :python] :write true}))
