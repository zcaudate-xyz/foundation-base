(ns postgres.sample.scratch-v3.route-currency
  (:require [std.lib :as h]
            [std.lib.foundation :as f]
            [hara.lang :as l]
            [hara.model.spec-postgres.gen-bind :as bind-pg]
            [postgres.sample.scratch-v3 :as scratch]))

(l/script :xtalk
  {:require [[xt.db.text.base-util :as ut]]
   :export [MODULE]})

(defn tmpl-route
  [[sym src tmeta]]
  (let [{:keys [root] :as tmeta} (merge tmeta
                                        (f/template-meta))
        url (str root "/" (name src))]
    (with-meta
      (list 'def.xt (with-meta sym {:api/type :route
                                    :api/url url})
            (assoc (bind-pg/bind-function @(resolve src))
                   :url url))
      tmeta)))

(defn route-list
  []
  (mapv l/sym-full
        (l/module-entries :xtalk
                          'postgres.sample.scratch-v3.route-currency
                          (fn [e] (= :route (:api/type e))))))

(h/template-ensure
 (bind-pg/list-api 'postgres.sample.scratch-v3)
 (h/template-entries [tmpl-route {:root "api/currency"}]
   (bind-pg/list-api 'postgres.sample.scratch-v3)))

(def.xt ROUTES
  (@! (-/route-list)))

(defn.xt make-routes
  "collects the generated Currency mutation routes"
  {:added "4.1"}
  []
  (return (ut/collect-routes -/ROUTES "route")))

(def.xt MODULE (!:module))
