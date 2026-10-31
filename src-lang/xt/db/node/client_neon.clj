(ns xt.db.node.client-neon
  (:require [lang.core :as l]))
(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.db.system.impl-neon-session :as session]
             [xt.db.node.proxy-util :as proxy]]})

(defn.xt sign-up
  [node service-id credentials opts]
  (return (proxy/request-client node "@xt.neon/sign-up" [service-id credentials] opts)))

(defn.xt sign-in
  [node service-id credentials opts]
  (return (proxy/request-client node "@xt.neon/sign-in" [service-id credentials] opts)))

(defn.xt sign-out
  [node service-id opts]
  (return (proxy/request-client node "@xt.neon/sign-out" [service-id] opts)))

(defn.xt current-session
  [node service-id opts]
  (return (proxy/request-client node "@xt.neon/current-session" [service-id] opts)))

(defn.xt refresh-session
  [node service-id opts]
  (return (proxy/request-client node "@xt.neon/refresh-session" [service-id] opts)))
