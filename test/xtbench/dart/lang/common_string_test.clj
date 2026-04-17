(ns xtbench.dart.lang.common-string-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.string.prose :as prose]))

(l/script- :dart
 {:runtime :twostep,
  :require [[xt.lang.common-string :as xts]
            [xt.lang.common-spec :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})
