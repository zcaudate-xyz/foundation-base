(ns xt.protocol.simple-greeter
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]]})

(def.xt IGreeter
  ["greet"])

(defn.xt greet
  [greeter]
  (var greet-fn (xt/proto:method greeter "greet"))
  (return (greet-fn greeter)))


