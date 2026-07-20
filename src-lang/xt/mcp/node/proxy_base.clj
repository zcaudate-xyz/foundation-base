(ns xt.mcp.node.proxy-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.mcp.node.proxy-util :as proxy-util]]})

(def.xt MESSAGE_ACTION "@xt.mcp/message")

(defn.xt request-proxy
  "forwards an MCP message to the server node"
  {:added "4.1"}
  [space args request node]
  (return (proxy-util/request-proxy space args request node)))

(defn.xt init-proxy-handlers
  "installs client-side handlers mirroring the MCP server action ids"
  {:added "4.1"}
  [node]
  (substrate/register-handler node -/MESSAGE_ACTION -/request-proxy nil)
  (return node))
