(ns xt.db.runtime.sql-python-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  ^{:seedgen/root {:all true}}
  (l/script- :python
    {:runtime :basic
     :require [[xt.lang.spec-base :as xt]
               [xt.lang.common-lib :as k]
               [xt.lang.common-data :as xtd]
               [xt.lang.common-string :as str]
               [xt.lang.common-repl :as repl]
               [xt.lang.spec-promise :as spec-promise]
               [xt.protocol.impl.connection-sql :as dbsql]
               [xt.db.runtime.sql :as impl-sql]
               [xt.db.text.base-flatten :as f]
               [xt.db.text.sql-util :as ut]
               [xt.db.text.sql-raw :as raw]
               [xt.db.text.sql-table :as sql-table]
               [xt.db.text.sql-manage :as manage]
               [xt.db.helpers.data-main-test :as sample]
               [xt.db.helpers.sqlite-runtime-parity-test :as parity]
               [python.lib.driver-sqlite :as py-sqlite]]})

  (fact:global
   {:setup    [(l/rt:restart)
               (do (l/rt:scaffold :python)
                   true)]
    :teardown [(l/rt:stop)]})

  ^{:refer xt.db.runtime.sql/sql-pull-sync :added "4.1"}
  (fact "returns the expected nested sqlite output in python"

    (parity/sqlite-parity-python)
    => parity/+sqlite-parity-output+))
