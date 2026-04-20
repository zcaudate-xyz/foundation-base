(ns
 xtbench.lua.sys.conn-dbsql-test
 (:require
  [rt.basic.type-common :as common]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :config {:program :resty},
  :require
  [[xt.sys.conn-dbsql :as dbsql]
   [lua.nginx.driver-postgres :as lua-postgres]]})

(def CANARY-DART (common/program-exists? "dart"))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.sys.conn-dbsql/connect, :added "4.0"}
(fact
 "connects to a database"
 ^{:hidden true}
 (!.lua
  (var
   conn
   (dbsql/connect {:constructor lua-postgres/connect-constructor}))
  (dbsql/query conn "SELECT 1;"))
 =>
 1)
