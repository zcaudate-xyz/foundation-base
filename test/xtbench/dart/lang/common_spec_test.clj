(ns xtbench.dart.lang.common-spec-test
  (:require [clojure.set :as set]
            [std.lang :as l]
            [std.lang.model.spec-lua :as lua]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-string :as xts]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})
