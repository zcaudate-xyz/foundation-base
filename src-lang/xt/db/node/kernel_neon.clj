(ns xt.db.node.kernel-neon
  (:require [lang.core :as l]))
(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.db.system.impl-neon-session :as session]
             [xt.db.node.proxy-util :as proxy]]})

(defn.xt ^{:substrate/fn "@xt.neon/sign-up"} sign-up-handler
  [space args request node]
  (return (session/sign-up (substrate/get-service node (xt/x:first args)) (xt/x:second args))))

(defn.xt ^{:substrate/fn "@xt.neon/sign-in"} sign-in-handler
  [space args request node]
  (return (session/sign-in (substrate/get-service node (xt/x:first args)) (xt/x:second args))))

(defn.xt ^{:substrate/fn "@xt.neon/sign-out"} sign-out-handler
  [space args request node]
  (return (session/sign-out (substrate/get-service node (xt/x:first args)))))

(defn.xt ^{:substrate/fn "@xt.neon/current-session"} current-session-handler
  [space args request node]
  (return (session/current-session (substrate/get-service node (xt/x:first args)))))

(defn.xt ^{:substrate/fn "@xt.neon/refresh-session"} refresh-session-handler
  [space args request node]
  (return (session/refresh-session (substrate/get-service node (xt/x:first args)))))

(defn.xt init-handlers
  [node]
  (substrate/register-handler node "@xt.neon/sign-up" -/sign-up-handler nil)
  (substrate/register-handler node "@xt.neon/sign-in" -/sign-in-handler nil)
  (substrate/register-handler node "@xt.neon/sign-out" -/sign-out-handler nil)
  (substrate/register-handler node "@xt.neon/current-session" -/current-session-handler nil)
  (substrate/register-handler node "@xt.neon/refresh-session" -/refresh-session-handler nil)
  (return node))
