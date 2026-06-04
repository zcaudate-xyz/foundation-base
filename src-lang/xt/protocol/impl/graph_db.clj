(ns xt.protocol.impl.graph-db
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
             [xt.protocol.graphdb :as graph-if]]})
