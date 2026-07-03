^{:no-test true}
(ns postgres.sample.scratch-v3.view-currency
  (:require [std.lib :as h]
            [hara.lang :as l]
            [postgres.gen.bind-macro :as bind-pg]
            [postgres.gen.gen-bind :as bind]
            [postgres.sample.scratch-v3]))

(l/script :xtalk
  {:require [[xt.db.text.base-util :as ut]]
   :export [MODULE]})

(defn view-list
  []
  (bind/view-list 'postgres.sample.scratch-v3.view-currency))

(h/template-ensure
 (bind-pg/list-view 'postgres.sample.scratch-v3 :select)
 (h/template-entries [bind/tmpl-view]
   (bind-pg/list-view 'postgres.sample.scratch-v3 :select)))

(h/template-ensure
 (bind-pg/list-view 'postgres.sample.scratch-v3 :return)
 (h/template-entries [bind/tmpl-view]
   (bind-pg/list-view 'postgres.sample.scratch-v3 :return)))

(def.xt VIEWS
  (@! (-/view-list)))

(defn.xt make-views
  "collects the generated Currency select and return views"
  {:added "4.1"}
  []
  (return (ut/collect-views -/VIEWS)))

(def.xt MODULE (!:module))
