(ns
 xtbench.dart.lang.event-route-test
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
   [xt.lang.common-spec :as xt]
   [xt.lang.event-route :as route]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})
