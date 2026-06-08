(ns xt.db.system.impl-common
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt impl-base
  "creates the common client record envelope"
  {:added "4.1"}
  [tag schema lookup opts]
  (return
   {"::" tag
    "schema"   schema
    "lookup"   lookup
    "opts" opts}))
