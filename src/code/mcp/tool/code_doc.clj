(ns code.mcp.tool.code-doc
  (:require [code.doc :as doc]
            [code.doc.executive :as executive]))

(defn init-template-fn
  [_ {:keys [site params]}]
  (let [site (read-string site)
        params (if params (read-string params) {})]
    (doc/init-template site params)
    {:content [{:type "text" :text (str "Initialized template for " site)}]
     :isError false}))

(def init-template-tool
  {:name "code-doc-init"
   :description (str "Initialize a `code.doc` site template. Use this when an agent needs to prepare or refresh "
                     "the generated theme/template assets for a documentation site before publishing.")
   :inputSchema {:type "object"
                 :properties {"site" {:type "string" :description "EDN string of the site key, such as `:core`, `:std.lib`, or `:all`."}
                              "params" {:type "string" :description "Optional EDN string of params passed through to `code.doc/init-template`, such as `{:write true}`."}}
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
   :description (str "Deploy a `code.doc` site template into its output location. Use this after initialization "
                     "when template assets need to be copied or refreshed before a publish step.")
   :inputSchema {:type "object"
                 :properties {"site" {:type "string" :description "EDN string of the site key to deploy, such as `:std.lib` or `:all`."}
                              "params" {:type "string" :description "Optional EDN string of params passed through to `code.doc/deploy-template`."}}
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
   :description (str "Publish a `code.doc` site from source documentation into rendered output. Use this for "
                     "documentation autopilot work after template init/deploy, or when regenerating a specific site "
                     "or page set from `src-doc` inputs.")
   :inputSchema {:type "object"
                 :properties {"site" {:type "string" :description "EDN string of the site key to publish, such as `:core`, `:std.lib`, or `:all`."}
                              "params" {:type "string" :description "Optional EDN string of params passed through to `code.doc/publish`, such as `{:write true}`."}}
                 :required ["site"]}
   :implementation #'publish-fn})
