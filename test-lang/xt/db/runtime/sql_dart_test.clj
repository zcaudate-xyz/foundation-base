(ns xt.db.runtime.sql-dart-test
  (:require [hara.runtime.basic.type-common :as common]
            [hara.lang :as l])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  ^{:seedgen/root {:all true}}
  (l/script- :dart
    {:runtime :twostep
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
               [dart.lib.driver-sqlite :as dart-sqlite]]})

  (def CANARY-DART
    (common/program-exists? "dart"))

  (fact:global
   {:setup    [(l/rt:restart)
               (if CANARY-DART
                 (do (l/rt:scaffold :dart)
                     true)
                 true)]
    :teardown [(l/rt:stop)]})

  ^{:refer xt.db.runtime.sql/sql-pull-sync :added "4.1"}
  (fact "returns the expected nested sqlite output in dart"

    (if CANARY-DART
      (parity/sqlite-parity-dart)
      :dart-unavailable)
    => (if CANARY-DART
         parity/+sqlite-parity-output+
         :dart-unavailable)))
