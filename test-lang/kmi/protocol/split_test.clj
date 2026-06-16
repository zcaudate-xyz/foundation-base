(ns kmi.protocol.split-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.protocol-base :as p]]})

(l/script- :lua
  {:runtime :basic
   :require [[kmi.lang.protocol-base :as p]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})
