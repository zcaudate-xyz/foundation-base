(ns kmi.mcp.node.runtime
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[kmi.mcp.node.kernel-base :as kernel-base]
             [kmi.mcp.node.proxy-base :as proxy-base]
             [kmi.mcp.node.proxy-util :as proxy-util]]})

(defn.xt init-server
  "installs the MCP addon and a named service on an existing substrate node"
  {:added "4.1"}
  [node service-id opts]
  (kernel-base/init-handlers node)
  (kernel-base/ensure-service node service-id opts)
  (return node))

(defn.xt init-server-proxy
  "installs the MCP proxy addon on an existing substrate client node"
  {:added "4.1"}
  [node transport-id]
  (proxy-base/init-proxy-handlers node)
  (when transport-id
    (proxy-util/set-default-transport node transport-id))
  (return node))
