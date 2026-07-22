(ns xt.mcp.node.proxy-util
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]]})

(defn.xt set-default-transport
  "sets the default server transport id for MCP proxy requests"
  {:added "4.1"}
  [node transport-id]
  (xtd/set-in node ["state" "mcp_proxy" "default_transport_id"] transport-id)
  (return transport-id))

(defn.xt get-default-transport
  "gets the default server transport id for MCP proxy requests"
  {:added "4.1"}
  [node]
  (return (xtd/get-in node ["state" "mcp_proxy" "default_transport_id"])))

(defn.xt get-transport-id
  "resolves the transport id from opts, the node default, or the first transport"
  {:added "4.1"}
  [node opts]
  (return (or (xtd/get-in opts ["transport_id"])
              (-/get-default-transport node)
              (xt/x:first (substrate/transport-list node)))))

(defn.xt request-meta
  "builds request metadata with an explicit transport id"
  {:added "4.1"}
  [node request]
  (return {"transport_id" (-/get-transport-id node request)}))

(defn.xt request-proxy
  "forwards an MCP action over the configured substrate transport"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      (. request ["action"])
                      args
                      (-/request-meta node (. request ["meta"])))))

(defn.xt request-client
  "calls a local MCP handler when installed, otherwise uses a transport"
  {:added "4.1"}
  [node action args opts]
  (return
   (:? (substrate/get-handler node action)
       (substrate/request node nil action args {"local" true})
       (substrate/request node nil action args (-/request-meta node opts)))))
