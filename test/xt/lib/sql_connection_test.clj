(ns xt.lib.sql-connection-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lib.sql-connection :as sql]]})

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lib.sql-connection :as sql]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lib.sql-connection :as sql]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lib.sql-connection/connection-create :added "4.1"}
(fact "wraps SQL connection implementations with protocol-backed dispatch"

  (!.js
   (var conn (sql/connection-create
              {"tag" "raw"}
              {"query" (fn [raw query]
                         (return 1))
               "query_sync" (fn [raw query]
                              (return 2))
               "disconnect" (fn [raw]
                              (return true))}))
   [(sql/connection? conn)
    (sql/query conn "SELECT 1;")
    (sql/query-sync conn "SELECT 2;")
    (sql/disconnect conn)])
  => [true 1 2 true]

  (!.lua
   (var conn (sql/connection-create
              {"tag" "raw"}
              {"query" (fn [raw query]
                         (return 1))
               "query_sync" (fn [raw query]
                              (return 2))
               "disconnect" (fn [raw]
                              (return true))}))
   [(sql/connection? conn)
    (sql/query conn "SELECT 1;")
    (sql/query-sync conn "SELECT 2;")
    (sql/disconnect conn)])
  => [true 1 2 true]

  (if CANARY-DART
    (!.dt
      (var conn (sql/connection-create
                 {"tag" "raw"}
                 {"query" (fn [raw query]
                            (return 1))
                  "query_sync" (fn [raw query]
                                 (return 2))
                  "disconnect" (fn [raw]
                                 (return true))}))
      [(sql/connection? conn)
       (sql/query conn "SELECT 1;")
       (sql/query-sync conn "SELECT 2;")
       (sql/disconnect conn)])
    :dart-unavailable)
  => (any [true 1 2 true]
          :dart-unavailable))
