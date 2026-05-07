(ns postgres.sample.scratch-v3.view-currency
  (:require [std.lib :as h]
            [std.lib.foundation :as f]
            [hara.lang :as l]
            [hara.model.spec-postgres.gen-bind :as bind-pg]
            [postgres.sample.scratch-v3 :as scratch]))

(l/script :xtalk
  {:require [[xt.db.text.base-util :as ut]]
   :export [MODULE]})

(defn tmpl-view
  [[sym src tmeta]]
  (let [{:keys [view] :as tmeta} (merge tmeta
                                        (f/template-meta))]
    (with-meta
      (list 'def.xt (with-meta sym {:api/type :view})
            (bind-pg/bind-view @(resolve src) view))
      tmeta)))

(defn view-list
  []
  (mapv l/sym-full
        (l/module-entries :xtalk
                          'postgres.sample.scratch-v3.view-currency
                          (fn [e] (= :view (:api/type e))))))

(h/template-ensure
 (bind-pg/list-view 'postgres.sample.scratch-v3 :select)
 (h/template-entries [tmpl-view]
   (bind-pg/list-view 'postgres.sample.scratch-v3 :select)))

(h/template-ensure
 (bind-pg/list-view 'postgres.sample.scratch-v3 :return)
 (h/template-entries [tmpl-view]
   (bind-pg/list-view 'postgres.sample.scratch-v3 :return)))

(defn.xt make-views
  "collects the generated Currency select and return views"
  {:added "4.1"}
  []
  (return (ut/collect-views (@! (-/view-list)))))

(def.xt MODULE (!:module))
