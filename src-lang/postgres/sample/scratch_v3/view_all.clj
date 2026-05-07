(ns postgres.sample.scratch-v3.view-all
  (:require [hara.lang :as l]
            [postgres.sample.scratch-v3.view-currency :as view-currency]))

(l/script :xtalk
  {:require [[postgres.sample.scratch-v3.view-currency :as view-currency]]
   :export [MODULE]})

(defn.xt get-views
  "returns the generated scratch-v3 view registry"
  {:added "4.1"}
  []
  (return (view-currency/make-views)))

(def.xt MODULE (!:module))
