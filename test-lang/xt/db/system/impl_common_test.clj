(ns xt.db.system.impl-common-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.db.system.impl-common :as impl-common]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.system.impl-common :as impl-common]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-common/add-db-listener-default :added "4.1"}
(fact "adds a listener handle to the impl listener map"

  (!.js
    (var impl {:listeners {}})
    (var handle (fn [event] (return event)))
    [(impl-common/add-db-listener-default impl "l-1" handle)
     (xt/x:not-nil? (xtd/get-in impl ["listeners" "l-1"]))])
  => ["l-1" true])

^{:refer xt.db.system.impl-common/get-db-listener-default :added "4.1"}
(fact "retrieves a listener handle from the impl listener map"

  (!.js
    (var handle (fn [event] (return event)))
    (var impl {:listeners {"l-1" handle
                           "l-2" (fn [_] (return nil))}})
    [(xt/x:not-nil? (impl-common/get-db-listener-default impl "l-1" nil))
     (xt/x:not-nil? (impl-common/get-db-listener-default impl "l-2" nil))
     (impl-common/get-db-listener-default impl "missing" nil)])
  => [true true nil])

^{:refer xt.db.system.impl-common/remove-db-listener-default :added "4.1"}
(fact "removes a listener handle from the impl listener map"

  (!.js
    (var handle (fn [event] (return event)))
    (var impl {:listeners {"l-1" handle}})
    [(impl-common/remove-db-listener-default impl "l-1")
     (xtd/get-in impl ["listeners" "l-1"])])
  => ["l-1" nil])