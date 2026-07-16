(ns xt.db.system.impl-common-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.db.system.impl-common :as impl-common]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-memory :as impl-memory]]})

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

  ^{:seedgen/base {:lua {:expect (l/as-lua [true true nil])}}}
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

  ^{:seedgen/base {:lua {:expect (l/as-lua ["l-1" nil])}}}
  (!.js
    (var handle (fn [event] (return event)))
    (var impl {:listeners {"l-1" handle}})
    [(impl-common/remove-db-listener-default impl "l-1")
     (xtd/get-in impl ["listeners" "l-1"])])
  => ["l-1" nil])


^{:refer xt.db.system.impl-common/sync-get-tables :added "4.1"}
(fact "extracts table names from db/sync and db/remove"

  (!.js
   (impl-common/sync-get-tables {"db/sync" {"User" [{"id" 1}]
                                            "Log"  [{"id" 2}]}
                                 "db/remove" {"Role" [1 2 3]}}))
  => (contains ["User" "Log" "Role"] :in-any-order)

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
  => (contains ["l1" "l2"] :in-any-order)

  (!.js
   (var called [])
   (var impl {"listeners"
              {"l1" {"guard"    (fn [table] (return (== table "User")))
                     "callback" (fn [event] (xt/x:arr-push called "l1"))}}})
   (impl-common/sync-notify-listeners impl ["Log"] {})
   (return called))
  => []

  (!.js
   (var called [])
   (var impl {"listeners"
              {"l1" {"guard"    {"User" true
                                "Log" false}
                     "callback" (fn [event] (xt/x:arr-push called "l1"))}
               "l2" {"guard"    {"Log" true}
                     "callback" (fn [event] (xt/x:arr-push called "l2"))}}})
   (impl-common/sync-notify-listeners impl ["User" "Log"] {})
   (return called))
  => (contains ["l1" "l2"] :in-any-order)

  (!.js
   (var called [])
   (var payload {"db/sync" {"User" [{"id" 1}]}
                 "db/remove" {"User" [2]}})
   (var impl {"listeners"
              {"l1" {"guard"    {"User" (fn [table-payload]
                                          (return (== (xtd/get-in table-payload ["db/sync" 0 "id"]) 1)))}
                     "callback" (fn [event] (xt/x:arr-push called "l1"))}
               "l2" {"guard"    {"User" (fn [table-payload]
                                          (return (== (xtd/get-in table-payload ["db/sync" 0 "id"]) 999)))}
                     "callback" (fn [event] (xt/x:arr-push called "l2"))}}})
   (impl-common/sync-notify-listeners impl ["User"] payload)
   (return called))
  => ["l1"])

^{:refer xt.db.system.impl-common/sync-process-payload :added "4.1.4"}
(fact "applies sync payload and notifies matching listeners"

  (!.js
   (var notified [])
   (var schema {"User" {"id" {"ident" "id" "order" 0 "type" "text"}}
                "Log"  {"id" {"ident" "id" "order" 0 "type" "text"}}})
   (var lookup {"User" {"position" 0}
                "Log"  {"position" 1}})
   (var impl (impl-memory/impl-memory schema lookup))
   (impl-common/add-db-listener
    impl
    "l1"
    {"guard"    (fn [table] (return (== table "User")))
     "callback" (fn [event] (xt/x:arr-push notified event))})
   (impl-common/sync-process-payload
    impl
    {"db/sync" {"User" [{"id" 1}]}})
   (return {"notified"    notified
            "has-record"  (== 1 (xt/x:len (xt/x:obj-keys (or (xtd/get-in impl ["rows" "User"]) {}))))}))
  => {"notified"   [{"db/sync" {"User" [{"id" 1}]}}]
      "has-record" true}

  (!.js
   (var notified [])
   (var schema {"User" {"id" {"ident" "id" "order" 0 "type" "text"}}
                "Log"  {"id" {"ident" "id" "order" 0 "type" "text"}}})
   (var lookup {"User" {"position" 0}
                "Log"  {"position" 1}})
   (var impl (impl-memory/impl-memory schema lookup))
   (impl-common/add-db-listener
    impl
    "l1"
    {"guard"    (fn [table] (return (== table "Log")))
     "callback" (fn [event] (xt/x:arr-push notified event))})
   (impl-common/sync-process-payload
    impl
    {"db/remove" {"Log" [1 2]}})
   (return {"notified"    notified
            "has-records" (xt/x:obj-keys (or (xtd/get-in impl ["rows" "Log"])
                                             {}))}))
  => {"notified"    [{"db/remove" {"Log" [1 2]}}]
      "has-records" []}

  (!.js
   (var notified [])
   (var schema {"User" {"id" {"ident" "id" "order" 0 "type" "text"}}})
   (var lookup {"User" {"position" 0}})
   (var impl (impl-memory/impl-memory schema lookup))
   (impl-common/add-db-listener
    impl
    "l1"
    {"guard"    (fn [table] (return (== table "User")))
     "callback" (fn [event] (xt/x:arr-push notified event))})
   (impl-common/sync-process-payload impl {})
   (return {"notified"    notified
            "has-records" (xt/x:obj-keys (or (xtd/get-in impl ["rows" "User"])
                                             {}))}))
  => {"notified" [] "has-records" []})
