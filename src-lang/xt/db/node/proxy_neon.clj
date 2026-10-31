(ns xt.db.node.proxy-neon
  (:require [lang.core :as l]))
(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.db.system.impl-neon-session :as session]
             [xt.db.node.proxy-util :as proxy]]})

(defn.xt init-handlers
  [node]
  (substrate/register-handler node "@xt.neon/sign-up" proxy/request-proxy nil)
  (substrate/register-handler node "@xt.neon/sign-in" proxy/request-proxy nil)
  (substrate/register-handler node "@xt.neon/sign-out" proxy/request-proxy nil)
  (substrate/register-handler node "@xt.neon/current-session" proxy/request-proxy nil)
  (substrate/register-handler node "@xt.neon/refresh-session" proxy/request-proxy nil)
  (return node))
