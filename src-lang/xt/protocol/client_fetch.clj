(ns xt.protocol.client-fetch
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt IFetchClient
  ["request"
   "request_sync"])

(def.xt IFetchRuntimeClient
  (proto/iface-combine [-/IFetchClient]))
