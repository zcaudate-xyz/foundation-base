(ns xt.runtime.type-sql-connection-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-sql-connection :as sqlrt]]})

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.runtime.type-sql-connection :as sqlrt]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.runtime.type-sql-connection :as sqlrt]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-sql-connection/connection-legacy :added "4.1"}
(fact "wraps legacy SQL connection maps with runtime dispatch"

  (!.js
   (var conn (sqlrt/connection-legacy
              {"::query" (fn [query callback]
                           (return 1))
               "::query_sync" (fn [query]
                                 (return 2))
               "::disconnect" (fn [callback]
                                  (return true))}))
    [(sqlrt/connection? conn)
     (sqlrt/connection-query conn "SELECT 1;")
     (sqlrt/connection-query-sync conn "SELECT 2;")
     (sqlrt/connection-disconnect conn)])
  => [true 1 2 true]

  (!.lua
   (var conn (sqlrt/connection-legacy
              {"::query" (fn [query callback]
                           (return 1))
               "::query_sync" (fn [query]
                                 (return 2))
               "::disconnect" (fn [callback]
                                  (return true))}))
    [(sqlrt/connection? conn)
     (sqlrt/connection-query conn "SELECT 1;")
     (sqlrt/connection-query-sync conn "SELECT 2;")
     (sqlrt/connection-disconnect conn)])
  => [true 1 2 true]

  (if CANARY-DART
    (!.dt
      (var conn (sqlrt/connection-legacy
                 {"::query" (fn [query callback]
                              (return 1))
                  "::query_sync" (fn [query]
                                    (return 2))
                  "::disconnect" (fn [callback]
                                    (return true))}))
      [(sqlrt/connection? conn)
       (sqlrt/connection-query conn "SELECT 1;")
       (sqlrt/connection-query-sync conn "SELECT 2;")
       (sqlrt/connection-disconnect conn)])
    :dart-unavailable)
  => (any [true 1 2 true]
          :dart-unavailable))
