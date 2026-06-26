(ns xt.db.system.impl-common-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :graal
   :require [[xt.db.system.impl-common :as impl-common]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]
   :test-mode true})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-common/sync-get-tables :added "4.1"}
(fact "extracts table names from db/sync and db/remove"

  (!.js
   (impl-common/sync-get-tables {"db/sync" {"User" [{"id" 1}]
                                            "Log"  [{"id" 2}]}
                                 "db/remove" {"Role" [1 2 3]}}))
  => (contains ["User" "Log" "Role"] :in-any-order :set)

  (!.js
   (impl-common/sync-get-tables {"db/sync" {"User" [{"id" 1}]}}))
  => ["User"]

  (!.js
   (impl-common/sync-get-tables {"db/remove" {"User" [1]}}))
  => ["User"]

  (!.js
   (impl-common/sync-get-tables {}))
  => [])

^{:refer xt.db.system.impl-common/sync-notify-listeners :added "4.1"}
(fact "notifies only listeners whose guard matches a changed table"

  (!.js
   (var called [])
   (var impl {"listeners"
              {"l1" {"guard"    (fn [table] (return (== table "User")))
                     "callback" (fn [event] (xt/x:arr-push called "l1"))}
               "l2" {"guard"    (fn [table] (return (== table "Log")))
                     "callback" (fn [event] (xt/x:arr-push called "l2"))}
               "l3" {"guard"    (fn [table] (return false))
                     "callback" (fn [event] (xt/x:arr-push called "l3"))}}})
   (impl-common/sync-notify-listeners impl ["User"] {})
   (return called))
  => ["l1"]

  (!.js
   (var called [])
   (var impl {"listeners"
              {"l1" {"guard"    (fn [table] (return (== table "User")))
                     "callback" (fn [event] (xt/x:arr-push called "l1"))}
               "l2" {"guard"    (fn [table] (return (== table "Log")))
                     "callback" (fn [event] (xt/x:arr-push called "l2"))}}})
   (impl-common/sync-notify-listeners impl ["User" "Log"] {})
   (return called))
  => (contains ["l1" "l2"] :in-any-order :set)

  (!.js
   (var called [])
   (var impl {"listeners"
              {"l1" {"guard"    (fn [table] (return (== table "User")))
                     "callback" (fn [event] (xt/x:arr-push called "l1"))}}})
   (impl-common/sync-notify-listeners impl ["Log"] {})
   (return called))
  => [])

^{:refer xt.db.system.impl-common/sync-process-payload :added "4.1.4"}
(fact "applies sync payload and notifies matching listeners"

  (!.js
   (var applied [])
   (var impl {"listeners"
              {"l1" {"guard"    (fn [table] (return (== table "User")))
                     "callback" (fn [event] (xt/x:arr-push applied "notified"))}}
              "process-add-event"
              (fn [impl data]
                (xt/x:arr-push applied ["add" data])
                (return true))
              "process-remove-event"
              (fn [impl data]
                (xt/x:arr-push applied ["remove" data])
                (return true))})
   (impl-common/sync-process-payload impl {"db/sync" {"User" [{"id" 1}]}})
   (return applied))
  => (contains [["add" {"User" [{"id" 1}]}] "notified"] :in-any-order :set)

  (!.js
   (var applied [])
   (var impl {"listeners"
              {"l1" {"guard"    (fn [table] (return (== table "Log")))
                     "callback" (fn [event] (xt/x:arr-push applied "notified"))}}
              "process-add-event"
              (fn [impl data]
                (xt/x:arr-push applied ["add" data])
                (return true))
              "process-remove-event"
              (fn [impl data]
                (xt/x:arr-push applied ["remove" data])
                (return true))})
   (impl-common/sync-process-payload impl {"db/remove" {"Log" [1 2]}})
   (return applied))
  => (contains [["remove" {"Log" [1 2]}] "notified"] :in-any-order :set)

  (!.js
   (var applied [])
   (var impl {"listeners"
              {"l1" {"guard"    (fn [table] (return (== table "User")))
                     "callback" (fn [event] (xt/x:arr-push applied "notified"))}}
              "process-add-event"
              (fn [impl data]
                (xt/x:arr-push applied ["add" data])
                (return true))
              "process-remove-event"
              (fn [impl data]
                (xt/x:arr-push applied ["remove" data])
                (return true))})
   (impl-common/sync-process-payload impl {})
   (return applied))
  => [])
