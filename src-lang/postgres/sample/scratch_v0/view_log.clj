(ns postgres.sample.scratch-v0.view-log
  (:require [std.lib :as h]
            [hara.lang :as l]
            [hara.model.spec-postgres.gen-bind :as bind-pg]
            [xt.db.gen-bind :as bind]
            [postgres.sample.scratch-v0]))

(l/script :xtalk
  {:require [[xt.db.text.base-util :as ut]]
   :export [MODULE]})

(h/template-ensure
 (bind-pg/list-view 'postgres.sample.scratch-v0 :select)
 (h/template-entries [bind/tmpl-view]
   (bind-pg/list-view 'postgres.sample.scratch-v0 :select)))

(h/template-ensure
 (bind-pg/list-view 'postgres.sample.scratch-v0 :return)
 (h/template-entries [bind/tmpl-view]
   (bind-pg/list-view 'postgres.sample.scratch-v0 :return)))

(defn view-list
  []
  (mapv l/sym-full
        (l/module-entries :xtalk
                          'postgres.sample.scratch-v0.view-log
                          (fn [e] (= :view (:api/type e))))))

(def.xt VIEWS
  (@! (-/view-list)))

(defn.xt make-views
  "collects the generated scratch-v0 log views"
  {:added "4.1.4"}
  []
  (return (ut/collect-views -/VIEWS)))

(def.xt MODULE (!:module))
