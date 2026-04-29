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

^{:refer xt.runtime.type-sql-connection/connection-create :added "4.1"}
(fact "wraps fresh SQL connection implementations with runtime dispatch"

  (!.js
   (var conn (sqlrt/connection-create
              {"tag" "raw"}
              {"query" (fn [raw query]
                         (return 1))
               "query_sync" (fn [raw query]
                              (return 2))
               "disconnect" (fn [raw]
                              (return true))}))
    [(sqlrt/connection? conn)
     (sqlrt/connection-query conn "SELECT 1;")
     (sqlrt/connection-query-sync conn "SELECT 2;")
     (sqlrt/connection-disconnect conn)])
  => [true 1 2 true]

  (!.lua
   (var conn (sqlrt/connection-create
              {"tag" "raw"}
              {"query" (fn [raw query]
                         (return 1))
               "query_sync" (fn [raw query]
                              (return 2))
               "disconnect" (fn [raw]
                              (return true))}))
    [(sqlrt/connection? conn)
     (sqlrt/connection-query conn "SELECT 1;")
     (sqlrt/connection-query-sync conn "SELECT 2;")
     (sqlrt/connection-disconnect conn)])
  => [true 1 2 true]

  (if CANARY-DART
    (!.dt
      (var conn (sqlrt/connection-create
                 {"tag" "raw"}
                 {"query" (fn [raw query]
                            (return 1))
                  "query_sync" (fn [raw query]
                                 (return 2))
                  "disconnect" (fn [raw]
                                 (return true))}))
      [(sqlrt/connection? conn)
       (sqlrt/connection-query conn "SELECT 1;")
       (sqlrt/connection-query-sync conn "SELECT 2;")
       (sqlrt/connection-disconnect conn)])
    :dart-unavailable)
  => (any [true 1 2 true]
          :dart-unavailable))
