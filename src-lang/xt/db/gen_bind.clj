(ns xt.db.gen-bind
  (:require [hara.lang :as l]
            [hara.model.spec-postgres.gen-bind :as gen]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.string.case :as case]))

(defn tmpl-route
  "creates a route template"
  {:added "4.0"}
  [[sym src tmeta]]
  (let [{:keys [root] :as tmeta} (merge tmeta
                                        (f/template-meta))
        url (str root "/" (name src))]
    (with-meta
      (list 'def.xt (with-meta sym {:api/type :route
                                    :api/url url})
            (assoc (gen/bind-function @(resolve src))
                   :url url))
      tmeta)))

(defn tmpl-view
  "creates a view template"
  {:added "4.0"}
  [[sym src tmeta]]
  (let [{:keys [view] :as tmeta} (merge tmeta
                                        (f/template-meta))]
    (with-meta
      (list 'def.xt (with-meta sym {:api/type :view})
            (gen/bind-view @(resolve src) view))
      tmeta)))

(defn route-map
  "TODO"
  {:added "4.0"}
  [& [ns]]
  (into {}
        (mapv (juxt (comp case/snake-case str :id)
                    l/sym-full)
              (l/module-entries :xtalk
                                (or ns (env/ns-sym))
                                (fn [e] (= :route (:api/type e)))))))

(defn route-list
  "lists all routes"
  {:added "4.0"}
  [& [ns]]
  (mapv l/sym-full
        (l/module-entries :xtalk
                          (or ns (env/ns-sym))
                          (fn [e] (= :route (:api/type e))))))

(defn view-list
  "lists all views"
  {:added "4.0"}
  [& [ns]]
  (mapv l/sym-full
        (l/module-entries :xtalk
                          (or ns (env/ns-sym))
                          (fn [e] (= :view (:api/type e))))))
