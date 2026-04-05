(ns code.mcp.scaffold
  (:require [code.ai.server.tool.basic :as basic]
            [code.mcp.rag :as rag]
            [code.mcp.server :as server]
            [code.mcp.tool.rag :as rag-tool]))

(defn base-tools
  []
  [basic/echo-tool
   basic/ping-tool])

(defn rag-tools
  [store]
  (rag-tool/rag-tools store))

(defn create-rag-server
  ([]
   (create-rag-server {}))
  ([{:keys [store tools]
     :as opts}]
   (let [store (or store (rag/create-store))
         tools (vec (concat (base-tools)
                            (rag-tools store)
                            tools))]
     (assoc (server/create-server (assoc opts :tools tools))
            :rag/store store))))
