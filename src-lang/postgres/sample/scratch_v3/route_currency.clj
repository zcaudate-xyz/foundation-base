(ns postgres.sample.scratch-v3.route-currency
  (:require [std.lib :as h]
            [hara.lang :as l]
            [postgres.gen.bind-macro :as bind-pg]
            [xt.db.gen-bind :as bind]
            [postgres.sample.scratch-v3]))

(l/script :xtalk
  {:require [[xt.db.text.base-util :as ut]]
   :export [MODULE]})

(defn route-list
  []
  (bind/route-list 'postgres.sample.scratch-v3.route-currency))

(h/template-ensure
 (bind-pg/list-api 'postgres.sample.scratch-v3)
 (h/template-entries [bind/tmpl-route {:root "api/currency"}]
   (bind-pg/list-api 'postgres.sample.scratch-v3)))

(def.xt ROUTES
  (@! (-/route-list)))

(defn.xt make-routes
  "collects the generated Currency mutation routes"
  {:added "4.1"}
  []
  (return (ut/collect-routes -/ROUTES "route")))

(def.xt MODULE (!:module))
