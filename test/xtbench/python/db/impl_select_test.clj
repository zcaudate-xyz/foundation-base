(ns
 xtbench.python.db.impl-select-test
 (:use code.test)
 (:require [std.lang :as l] [xt.lang.common-notify :as notify]))

(l/script-
 :postgres
 {:runtime :jdbc.client, :config {:dbname "test-scratch"}, :require []})

^#:xtalk{:template true}
(l/script- :python {:runtime :basic, :require []})

(fact:global
 {:setup [(l/rt:restart) (l/rt:setup-to :postgres)],
  :teardown [(l/rt:stop)]})
