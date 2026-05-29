(ns postgres.sample.scratch-v0.route-scratch
  (:require [std.lib :as h]
            [hara.lang :as l]
            [hara.model.spec-postgres.gen-bind :as bind-pg]
            [xt.db.gen-bind :as bind]
            [postgres.sample.scratch-v0]))

(l/script :xtalk
  {:require [[xt.db.text.base-util :as ut]]})

(h/template-ensure
 (bind-pg/list-api 'postgres.sample.scratch-v0)
 (h/template-entries [bind/tmpl-route {:root "api/scratch-v0"}]
   (bind-pg/list-api 'postgres.sample.scratch-v0)))

(defn route-list
  []
  (mapv l/sym-full
        (l/module-entries :xtalk
                          'postgres.sample.scratch-v0.route-scratch
                          (fn [e] (= :route (:api/type e))))))

(def.xt ROUTES
  (@! (-/route-list)))

(defn.xt make-routes
  "collects the generated scratch-v0 routes"
  {:added "4.1.4"}
  []
  (return (ut/collect-routes -/ROUTES "route")))

(defn.xt hello [] (return 1))


(comment
  (:form-input @ping)
  )
