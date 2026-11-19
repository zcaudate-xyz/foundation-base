(ns dupasupa.groups
  (:require [dupasupa.pages :as pages]))

(def CAMPAIGNS
  (let [page (first (pages/definitions))]
    {"id" (:group-id page)
     "area" (:area page)
     "models" (:models page)
     "actions"
     (into {} (for [[id entry] (:actions page)] [id {"rpc" (get entry "operation-spec")}]))}))
