(ns code.mcp.server
  (:require [code.mcp.base.server :as base-server]
            [code.mcp.tool.basic :as basic]
            [code.mcp.tool.clj-eval :as clj-eval]
            [code.mcp.tool.code-doc :as code-doc]
            [code.mcp.tool.code-manage :as code-manage]
            [code.mcp.tool.form-heal :as form-heal]
            [code.mcp.tool.std-lang :as std-lang]))

(defonce *server* (atom nil))

(defn default-tools
  []
  [basic/echo-tool
   basic/ping-tool
   std-lang/lang-emit-as-tool
   std-lang/list-languages-tool
   std-lang/list-modules-tool
   clj-eval/clj-eval-tool
   code-doc/init-template-tool
   code-doc/deploy-template-tool
   code-doc/publish-tool
   code-manage/manage-tool
   form-heal/list-edits-tool
   form-heal/get-dsl-deps-tool
   form-heal/refactor-directory-tool])

(defn create-server
  ([]
   (create-server {}))
  ([opts]
   (base-server/create-server
    (merge {:transport {:type :stdio}
            :server-info {:name "foundation-mcp" :version "0.1.0"}
            :tools (default-tools)}
           opts))))

(defn start-server
  ([]
   (start-server {}))
  ([opts]
   (swap! *server*
          (fn [current]
            (or current
                (create-server opts))))))

(defn stop-server
  []
  (swap! *server*
         (fn [current]
           (when current
             (base-server/close! current))
           nil)))
