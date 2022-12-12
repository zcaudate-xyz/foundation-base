(ns xt.runtime.type-arraymap
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-iter :as it]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-collection :as interface-collection]
             [xt.runtime.type-vector-node :as node]]
   :export [MODULE]})

(def.xt MODULE (!:module))
