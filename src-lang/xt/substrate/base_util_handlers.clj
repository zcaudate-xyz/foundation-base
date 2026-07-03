(ns xt.substrate.base-util-handlers
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate.base-util :as base-util]
             [xt.substrate.base-space :as base-space]]})

(defn.xt ^{:substrate/fn "@xt.substrate/ping"}
  ping
  "util handler: heartbeat"
  [space args request node]
  (return {"pong" true
           "node" (xt/x:get-key node "id")}))

(defn.xt ^{:substrate/fn "@xt.substrate/echo"}
  echo
  "util handler: returns the supplied arguments"
  [space args request node]
  (return args))

(defn.xt ^{:substrate/fn "@xt.substrate/list-handlers"}
  list-handlers
  "util handler: returns registered handler action ids"
  [space args request node]
  (return (base-util/list-handlers node)))

(defn.xt ^{:substrate/fn "@xt.substrate/list-triggers"}
  list-triggers
  "util handler: returns registered trigger signal ids"
  [space args request node]
  (return (base-util/list-triggers node)))

(defn.xt ^{:substrate/fn "@xt.substrate/list-spaces"}
  list-spaces
  "util handler: returns registered space ids"
  [space args request node]
  (return (base-space/list-spaces node)))

(defn.xt ^{:substrate/fn "@xt.substrate/list-transports"}
  list-transports
  "util handler: returns attached transport ids"
  [space args request node]
  (return (base-util/transport-list node)))

(defn.xt ^{:substrate/fn "@xt.substrate/node-info"}
  node-info
  "util handler: returns node id and metadata"
  [space args request node]
  (return {"id" (xt/x:get-key node "id")
           "meta" (xt/x:get-key node "meta")}))

(defn.xt ^{:substrate/fn "@xt.substrate/get-service"}
  handle-get-service
  "util handler: returns a service entry by id"
  [space args request node]
  (return (xt/x:get-key (or (xt/x:get-key node "services") {}) args)))

(defn.xt install-util-handlers
  "installs the @xt.substrate/* util handlers on a node"
  [node]
  (base-util/register-handler node "@xt.substrate/ping" -/ping {:substrate/fn "@xt.substrate/ping"})
  (base-util/register-handler node "@xt.substrate/echo" -/echo {:substrate/fn "@xt.substrate/echo"})
  (base-util/register-handler node "@xt.substrate/list-handlers" -/list-handlers {:substrate/fn "@xt.substrate/list-handlers"})
  (base-util/register-handler node "@xt.substrate/list-triggers" -/list-triggers {:substrate/fn "@xt.substrate/list-triggers"})
  (base-util/register-handler node "@xt.substrate/list-spaces" -/list-spaces {:substrate/fn "@xt.substrate/list-spaces"})
  (base-util/register-handler node "@xt.substrate/list-transports" -/list-transports {:substrate/fn "@xt.substrate/list-transports"})
  (base-util/register-handler node "@xt.substrate/get-service" -/handle-get-service {:substrate/fn "@xt.substrate/get-service"})
  (base-util/register-handler node "@xt.substrate/node-info" -/node-info {:substrate/fn "@xt.substrate/node-info"})
  (return node))
