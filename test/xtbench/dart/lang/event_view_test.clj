(ns
 xtbench.dart.lang.event-view-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-repl :as repl]
   [xt.lang.event-view :as view]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})
