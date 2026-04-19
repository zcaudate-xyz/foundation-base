(ns
 xtbench.dart.lang.event-form-test
 (:require
  [net.http :as http]
  [std.json :as json]
  [std.lang :as l]
  [std.lang.interface.type-notify :as interface]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-repl :as repl]
   [xt.lang.event-form :as form]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})
