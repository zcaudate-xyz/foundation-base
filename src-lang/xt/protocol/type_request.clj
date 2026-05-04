(ns xt.protocol.type-request
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt ITypeRequest
  ["request"
   "receive_request"
   "receive_response"
   "respond_ok"
   "respond_error"])

(def.xt ITypeRuntimeRequest
  (proto/iface-combine [-/ITypeRequest]))
