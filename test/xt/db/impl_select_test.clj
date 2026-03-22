(ns xt.db.impl-select-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres.script.test.scratch-v1 :as scratch]
             [rt.postgres :as pg]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.db.base-flatten :as f]
             [xt.db.cache-util :as cache-util]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-graph :as graph]
             [xt.db.sql-util :as ut]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.db.sample-scratch-test :as sample-scratch]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.base-schema :as sch]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-scratch-test :as sample-scratch]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup-to :postgres)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)]
  :teardown [(l/rt:stop)]})


