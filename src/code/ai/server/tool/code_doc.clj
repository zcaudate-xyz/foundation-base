(ns code.ai.server.tool.code-doc
  (:require [code.doc :as doc]
            [code.doc.executive :as executive]
            [std.lib :as h]))

(defn init-template-fn
  [_ {:keys [site params]}]
  (let [site (read-string site)
        params (if params (read-string params) {})]
    (doc/init-template site params)
    {:content [{:type "text" :text (str "Initialized template for " site)}]
     :isError false}))

(def init-template-tool
  {:name "code-doc-init"
   :description "Initialize documentation template"
   :inputSchema {:type "object"
                 :properties {"site" {:type "string" :description "EDN string of site key (e.g. :hara)"}
                              "params" {:type "string" :description "EDN string of params"}}
                 :required ["site"]}
   :implementation #'init-template-fn})

(defn deploy-template-fn
  [_ {:keys [site params]}]
  (let [site (read-string site)
        params (if params (read-string params) {})]
    (doc/deploy-template site params)
    {:content [{:type "text" :text (str "Deployed template for " site)}]
     :isError false}))

(def deploy-template-tool
  {:name "code-doc-deploy"
   :description "Deploy documentation template"
   :inputSchema {:type "object"
                 :properties {"site" {:type "string" :description "EDN string of site key"}
                              "params" {:type "string" :description "EDN string of params"}}
                 :required ["site"]}
   :implementation #'deploy-template-fn})

(defn publish-fn
  [_ {:keys [site params]}]
  (let [site (read-string site)
        params (if params (read-string params) {})]
    (doc/publish site params)
    {:content [{:type "text" :text (str "Published documentation for " site)}]
     :isError false}))

(def publish-tool
  {:name "code-doc-publish"
   :description "Publish documentation"
   :inputSchema {:type "object"
                 :properties {"site" {:type "string" :description "EDN string of site key"}
                              "params" {:type "string" :description "EDN string of params"}}
                 :required ["site"]}
   :implementation #'publish-fn})
