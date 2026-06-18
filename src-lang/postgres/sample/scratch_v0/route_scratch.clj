(ns postgres.sample.scratch-v0.route-scratch
  (:require [hara.lang :as l]
            [postgres.sample.scratch-v0.route-entries]
            [postgres.gen.gen-bind :as bind]))

(l/script :xtalk
  {:require [[xt.db.text.base-util :as ut]]})

(defn route-list
  []
  (mapv l/sym-full
        (l/module-entries :xtalk
                          'postgres.sample.scratch-v0.route-entries
                          (fn [_] true))))

(def.xt ROUTES
  (@! (-/route-list)))

(defn.xt make-routes
  "collects the generated scratch-v0 routes"
  {:added "4.1.4"}
  []
  (return (ut/collect-routes -/ROUTES "route")))
