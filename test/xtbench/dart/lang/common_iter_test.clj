(ns xtbench.dart.lang.common-iter-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-iter :as it]
             [xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})
