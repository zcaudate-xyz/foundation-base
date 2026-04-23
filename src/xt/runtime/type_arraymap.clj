(ns xt.runtime.type-arraymap
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-iter :as it]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-collection :as interface-collection]
             [xt.runtime.type-vector-node :as node]]})

