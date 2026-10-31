(ns xt.db.system.impl-neon-session
  (:require [lang.core :as l]))
(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.net.addon-neon :as addon]
             [xt.db.system.impl-neon-realtime :as realtime]
             [xt.db.system.impl-common :as common]]})

(defn.xt set-session
  [impl session]
  (var current (xtd/get-in impl ["client" "defaults" "bridge_token"]))
  (xtd/set-in impl ["state" "session"] session)
  (xtd/set-in impl ["client" "defaults" "token"] (xt/x:get-key session "access_token" nil))
  (xtd/set-in impl ["client" "defaults" "bridge_token"] (or (xt/x:get-key session "bridge_token" nil) current))
  (return session))

(defn.xt reset-session
  [impl]
  (xtd/set-in impl ["state" "auth_epoch"] (+ 1 (or (xtd/get-in impl ["state" "auth_epoch"]) 0)))
  (var closing (realtime/stop impl))
  (var caching (realtime/cache-impl impl))
  (when caching (common/clear-db caching))
  (xtd/set-in impl ["state" "session"] nil)
  (xtd/set-in impl ["client" "defaults" "token"] nil)
  (xtd/set-in impl ["client" "defaults" "bridge_token"] nil)
  (return closing))

(defn.xt authenticate
  [impl path credentials]
  (-/reset-session impl)
  (var epoch (xtd/get-in impl ["state" "auth_epoch"]))
  (return (-> (addon/bridge-request (xt/x:get-key impl "client" nil) path credentials)
              (promise/x:promise-then (fn [session]
                (when (== epoch (xtd/get-in impl ["state" "auth_epoch"]))
                  (return (-/set-session impl session)))
                (return nil))))))

(defn.xt sign-up
  [impl credentials]
  (return (-/authenticate impl "/auth/sign-up" credentials)))
(defn.xt sign-in
  [impl credentials]
  (return (-/authenticate impl "/auth/sign-in" credentials)))
(defn.xt refresh-session
  [impl]
  (var epoch (xtd/get-in impl ["state" "auth_epoch"]))
  (return (-> (addon/bridge-request (xt/x:get-key impl "client" nil) "/auth/refresh" {})
    (promise/x:promise-then (fn [session]
      (when (not (== epoch (xtd/get-in impl ["state" "auth_epoch"]))) (xt/x:err "Neon session changed"))
      (return (-/set-session impl session))))
    (promise/x:promise-catch (fn [err]
      (when (== epoch (xtd/get-in impl ["state" "auth_epoch"])) (-/reset-session impl))
      (throw err))))))

(defn.xt current-session
  [impl]
  (return (-/refresh-session impl)))
(defn.xt sign-out
  [impl]
  (var task (addon/bridge-request (xt/x:get-key impl "client" nil) "/auth/sign-out" {}))
  (-/reset-session impl)
  (return task))
