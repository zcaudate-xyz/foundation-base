(ns xt.mcp.node.client-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.mcp.base :as base]
             [xt.mcp.node.proxy-util :as proxy-util]]})

(def.xt MESSAGE_ACTION "@xt.mcp/message")

(defn.xt message
  "sends one decoded MCP JSON-RPC message through a substrate node"
  {:added "4.1"}
  [node service-id request context opts]
  (return
   (proxy-util/request-client node
                              -/MESSAGE_ACTION
                              [service-id request (or context {})]
                              opts)))

(defn.xt initialize
  "performs the MCP initialize request"
  {:added "4.1"}
  [node service-id request-id client-info context opts]
  (return
   (-/message node service-id
              {"jsonrpc" "2.0"
               "id" request-id
               "method" "initialize"
               "params" {"protocolVersion" base/PROTOCOL_VERSION
                         "capabilities" {}
                         "clientInfo" client-info}}
              context opts)))

(defn.xt initialized
  "sends the MCP initialized notification"
  {:added "4.1"}
  [node service-id context opts]
  (return
   (-/message node service-id
              {"jsonrpc" "2.0"
               "method" "notifications/initialized"}
              context opts)))

(defn.xt ping
  "pings the MCP addon service"
  {:added "4.1"}
  [node service-id request-id context opts]
  (return
   (-/message node service-id
              {"jsonrpc" "2.0" "id" request-id "method" "ping"}
              context opts)))

(defn.xt list-tools
  "lists tools exposed by the MCP addon service"
  {:added "4.1"}
  [node service-id request-id context opts]
  (return
   (-/message node service-id
              {"jsonrpc" "2.0" "id" request-id "method" "tools/list"}
              context opts)))

(defn.xt call-tool
  "calls a named MCP tool"
  {:added "4.1"}
  [node service-id request-id tool-name tool-args context opts]
  (return
   (-/message node service-id
              {"jsonrpc" "2.0"
               "id" request-id
               "method" "tools/call"
               "params" {"name" tool-name "arguments" (or tool-args {})}}
              context opts)))
