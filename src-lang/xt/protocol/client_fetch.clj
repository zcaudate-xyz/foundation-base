(ns xt.protocol.client-fetch
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(def.xt IFetchClient
  ["request"
   "query"
   "rpc"])

(def.xt IFetchRuntimeClient
  (proto/iface-combine [-/IFetchClient]))
