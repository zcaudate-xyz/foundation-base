(ns
 xtbench.python.db.impl-select-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :postgres
 {:runtime :jdbc.client,
  :config {:dbname "test-scratch"},
  :require
  [[rt.postgres.test.scratch-v1 :as scratch] [rt.postgres :as pg]]})

^#:xtalk{:template true}
(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.db.base-schema :as sch]
   [xt.db.base-flatten :as f]
   [xt.db.cache-util :as cache-util]
   [xt.db.sql-raw :as raw]
   [xt.db.sql-graph :as graph]
   [xt.db.sql-util :as ut]
   [xt.db.sql-manage :as manage]
   [xt.db.sql-table :as table]
   [xt.lang.common-lib :as k]
   [xt.lang.common-repl :as repl]
   [xt.db.sample-scratch-test :as sample-scratch]]})

(fact:global
 {:setup
  [(l/rt:restart) (l/rt:setup-to :postgres) (l/rt:scaffold :js)],
  :teardown [(l/rt:stop)]})
