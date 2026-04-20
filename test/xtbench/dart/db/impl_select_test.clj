(ns
 xtbench.dart.db.impl-select-test
 (:use code.test)
 (:require [std.lang :as l] [xt.lang.common-notify :as notify]))

(l/script-
 :postgres
 {:runtime :jdbc.client, :config {:dbname "test-scratch"}, :require []})

^#:xtalk{:template true}
(l/script- :dart {:runtime :twostep, :require []})

(fact:global
 {:setup [(l/rt:restart) (l/rt:setup-to :postgres)],
  :teardown [(l/rt:stop)]})
