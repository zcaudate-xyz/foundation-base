(ns xt.db.gen-bind
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [rt.postgres :as pg]))

(defn tmpl-route
  "creates a route template"
  {:added "4.0"}
  [[sym src tmeta]]
  (let [{:keys [root] :as tmeta} (merge tmeta
                                        (h/template-meta))
        url (str root "/" (name src))]
    (with-meta
      (list 'def.xt (with-meta sym {:api/type :route
                                    :api/url url})
            (assoc (pg/bind-function @(resolve src))
                   :url url))
      tmeta)))

(defn tmpl-view
  "creates a view template"
  {:added "4.0"}
  [[sym src tmeta]]
  (let [{:keys [view] :as tmeta} (merge tmeta
                                        (h/template-meta))]
    
    (with-meta
      (list 'def.xt (with-meta sym {:api/type :view})
            (pg/bind-view @(resolve src) view))
      tmeta)))

(defn route-map
  "lists all routes"
  {:added "4.0"}
  [& [ns]]
  (into {}
        (mapv (juxt (comp str/snake-case str :id)
                    l/sym-full)
              (l/module-entries :xtalk
                                (or ns (h/ns-sym))
                                (fn [e] (= :route (:api/type e)))))))

(defn route-list
  "lists all routes"
  {:added "4.0"}
  [& [ns]]
  (mapv l/sym-full
        (l/module-entries :xtalk
                          (or ns (h/ns-sym))
                          (fn [e] (= :route (:api/type e))))))

(defn view-list
  "lists all views"
  {:added "4.0"}
  [& [ns]]
  (mapv l/sym-full
        (l/module-entries :xtalk
                          (or ns (h/ns-sym))
                          (fn [e] (= :view (:api/type e))))))


(comment
  (into {}
        (first
         (l/module-entries :xtalk
                           'statsapi.list.fn-account
                           (fn [e] (= :route (:api/type e))))))
  {:op-key :def, :form-input (def get-privacy-policy {:input [], :return "jsonb", :schema "core/account-base", :id "get_privacy_policy", :flags {}, :url "api/account/get-privacy-policy"}), :section :code, :time 1750996747384602593, :standalone nil, :template nil, :op def, :module statsapi.list.fn-account, :lang :xtalk, :api/url "api/account/get-privacy-policy", :line 16, :priority 5, :id get-privacy-policy, :declared nil, :api/type :route, :display :default, :form (def get-privacy-policy {:input [], :return "jsonb", :schema "core/account-base", :id "get_privacy_policy", :flags {}, :url "api/account/get-privacy-policy"}), :namespace statsapi.list.fn-account, :deps #{}})
