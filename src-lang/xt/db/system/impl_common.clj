(ns xt.db.system.impl-common
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt impl-base
  "creates the common client record envelope"
  {:added "4.1"}
  [tag client schema lookup opts]
  (return
   {"::" tag
    "client"   client
    "schema"   schema
    "lookup"   lookup
    "opts" opts}))
