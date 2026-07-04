(ns xt.substrate.base-util-handlers
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate.base-util :as base-util]
             [xt.substrate.base-space :as base-space]]})

(defn.xt ^{:substrate/fn "@/ping"}
  ping
  "util handler: heartbeat"
  [space args request node]
  (return {"pong" true
           "node" (xt/x:get-key node "id")}))

(defn.xt ^{:substrate/fn "@/echo"}
  echo
  "util handler: returns the supplied arguments"
  [space args request node]
  (return args))

(defn.xt ^{:substrate/fn "@/list-handlers"}
  list-handlers
  "util handler: returns registered handler action ids"
  [space args request node]
  (return (base-util/list-handlers node)))

(defn.xt ^{:substrate/fn "@/list-triggers"}
  list-triggers
  "util handler: returns registered trigger signal ids"
  [space args request node]
  (return (base-util/list-triggers node)))

(defn.xt ^{:substrate/fn "@/list-spaces"}
  list-spaces
  "util handler: returns registered space ids"
  [space args request node]
  (return (base-space/list-spaces node)))

(defn.xt ^{:substrate/fn "@/list-transports"}
  list-transports
  "util handler: returns attached transport ids"
  [space args request node]
  (return (base-util/transport-list node)))

(defn.xt ^{:substrate/fn "@/node-info"}
  node-info
  "util handler: returns node id and metadata"
  [space args request node]
  (return {"id" (xt/x:get-key node "id")
           "meta" (xt/x:get-key node "meta")}))

(defn.xt ^{:substrate/fn "@/get-service"}
  handle-get-service
  "util handler: returns a service entry by id"
  [space args request node]
  (return (xt/x:get-key (or (xt/x:get-key node "services") {}) args)))

(defn.xt install-util-handlers
  "installs the @/* util handlers on a node"
  [node]
  (base-util/register-handler node "@/ping" -/ping {:substrate/fn "@/ping"})
  (base-util/register-handler node "@/echo" -/echo {:substrate/fn "@/echo"})
  (base-util/register-handler node "@/list-handlers" -/list-handlers {:substrate/fn "@/list-handlers"})
  (base-util/register-handler node "@/list-triggers" -/list-triggers {:substrate/fn "@/list-triggers"})
  (base-util/register-handler node "@/list-spaces" -/list-spaces {:substrate/fn "@/list-spaces"})
  (base-util/register-handler node "@/list-transports" -/list-transports {:substrate/fn "@/list-transports"})
  (base-util/register-handler node "@/get-service" -/handle-get-service {:substrate/fn "@/get-service"})
  (base-util/register-handler node "@/node-info" -/node-info {:substrate/fn "@/node-info"})
  (return node))
