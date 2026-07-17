(ns xt.ui.model
  "Compatibility facade for xt.ui.state.model."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.ui.state.model :as model]]})

(defn.xt store-create [node space-id group-id mode opts]
  (return (model/store-create node space-id group-id mode opts)))
(defn.xt store-version [store] (return (model/store-version store)))
(defn.xt store-open [store] (return (model/store-open store)))
(defn.xt model [store model-id] (return (model/model store model-id)))
(defn.xt model-slot [store model-id slot path fallback]
  (return (model/model-slot store model-id slot path fallback)))
(defn.xt model-input [store model-id path fallback]
  (return (model/model-input store model-id path fallback)))
(defn.xt model-output [store model-id path fallback]
  (return (model/model-output store model-id path fallback)))
(defn.xt model-pending? [store model-id] (return (model/model-pending? store model-id)))
(defn.xt model-disabled? [store model-id] (return (model/model-disabled? store model-id)))
(defn.xt model-error [store model-id] (return (model/model-error store model-id)))
(defn.xt model-remote [store model-id path fallback]
  (return (model/model-remote store model-id path fallback)))
(defn.xt model-sync [store model-id path fallback]
  (return (model/model-sync store model-id path fallback)))
(defn.xt set-input! [store model-id value event]
  (return (model/set-input! store model-id value event)))
(defn.xt patch-input! [store model-id path value event]
  (return (model/patch-input! store model-id path value event)))
(defn.xt invoke! [store model-id args] (return (model/invoke! store model-id args)))
(defn.xt refresh! [store model-id event] (return (model/refresh! store model-id event)))
(defn.xt subscribe! [store subscription-id callback]
  (return (model/subscribe! store subscription-id callback)))
(defn.xt unsubscribe! [store subscription-id]
  (return (model/unsubscribe! store subscription-id)))
(defn.xt store-close [store] (return (model/store-close store)))
