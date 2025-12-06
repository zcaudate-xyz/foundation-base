(ns code.ai.server
  (:require [mcp-clj.mcp-server.core :as mcp-server]
            [mcp-clj.tools.clj-eval :as clj-eval]
            [code.ai.server.tool.basic :as basic]
            [code.ai.server.tool.std-lang :as std-lang]
            [code.ai.server.tool.code-doc :as code-doc]
            [code.ai.server.tool.code-manage :as code-manage]
            [std.lib :as h]
            [std.lang :as l]
            [rt.basic.impl.process-lua :as lua]
            [rt.basic.impl.process-js :as js])
  (:import [java.io StringWriter]))

(defonce *server* (atom nil))

(defonce *rt* (atom nil))

;; Server with custom tools
(defn create-server
  []
  (mcp-clj.mcp-server.core/create-server
   {:transport {:type :sse :port 3001}
    :tools {"echo" basic/echo-tool
            "ping" basic/ping-tool

            "lang-emit-as" std-lang/lang-emit-as-tool
            "std-lang-list" std-lang/list-languages-tool
            "std-lang-modules" std-lang/list-modules-tool

            "clj-eval" clj-eval/clj-eval-tool
            "code-doc-init" code-doc/init-template-tool
            "code-doc-deploy" code-doc/deploy-template-tool
            "code-doc-publish" code-doc/publish-tool
            "code-manage" code-manage/manage-tool}}))

(defn start-server
  []
  (swap! *server*
         (fn [val]
           (if val val (create-server)))))

(defn stop-server
  []
  (swap! *server*
         (fn [val]
           (if val
             (do ((:stop val)) nil)))))

(comment
  (start-server)
  (stop-server)
  
  ((:stop server))
  
  ;; Add tools dynamically
  (mcp-clj.mcp-server.core/add-tool! server basic/echo-tool))
