(ns xt.db.runtime.sql-lua-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  ^{:seedgen/root {:all true}}
  (l/script- :lua.nginx
    {:runtime :basic
     :config {:program :resty}
     :require [[xt.lang.spec-base :as xt]
               [xt.lang.common-lib :as k]
               [xt.lang.common-data :as xtd]
               [xt.lang.common-string :as str]
               [xt.lang.common-repl :as repl]
               [xt.lang.spec-promise :as spec-promise]
               [xt.protocol.impl.connection-sql :as dbsql]
               [xt.db.runtime.sql :as impl-sql]
               [xt.db.text.sql-util :as ut]
               [xt.db.text.sql-raw :as raw]
               [xt.db.text.sql-manage :as manage]
               [xt.db.helpers.data-main-test :as sample]
               [xt.db.helpers.sqlite-runtime-parity-test :as parity]
               [lua.nginx.driver-sqlite :as lua-sqlite]]})

  (fact:global
   {:setup    [(l/rt:restart)
               (do (l/rt:scaffold :lua.nginx)
                   true)]
    :teardown [(l/rt:stop)]})

  ^{:refer xt.db.runtime.sql/sql-pull-sync :added "4.1"}
  (fact "returns the expected nested sqlite output in lua"

    (parity/sqlite-parity-lua)
    => parity/+sqlite-parity-output+))
