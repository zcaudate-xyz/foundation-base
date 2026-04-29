(ns xt.old.db.impl-select-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[rt.postgres.test.scratch-v1 :as scratch]
             [rt.postgres :as pg]]})

^{:xtalk/template true}
(l/script- :js
  {:runtime :basic
   :require [[xt.old.db.base-schema :as sch]
             [xt.old.db.base-flatten :as f]
             [xt.old.db.cache-util :as cache-util]
             [xt.old.db.sql-raw :as raw]
             [xt.old.db.sql-graph :as graph]
             [xt.old.db.sql-util :as ut]
             [xt.old.db.sql-manage :as manage]
             [xt.old.db.sql-table :as table]
             [xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.old.db.sample-scratch-test :as sample-scratch]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup-to :postgres)
             (l/rt:scaffold :js)]
  :teardown [(l/rt:stop)]})