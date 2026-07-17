(ns xt.ui.page
  "Compatibility facade for xt.ui.state.core."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.ui.state.core :as state]]})

(defn.xt controller-create [initial-state handlers lifecycle deps]
  (return (state/controller-create initial-state handlers lifecycle deps)))
(defn.xt snapshot [controller] (return (state/snapshot controller)))
(defn.xt revision [controller] (return (state/revision controller)))
(defn.xt notify! [controller] (return (state/notify! controller)))
(defn.xt set-state! [controller value] (return (state/set-state! controller value)))
(defn.xt update-state! [controller update-fn]
  (return (state/update-state! controller update-fn)))
(defn.xt subscribe! [controller listener-id listener]
  (return (state/subscribe! controller listener-id listener)))
(defn.xt unsubscribe! [controller listener-id]
  (return (state/unsubscribe! controller listener-id)))
(defn.xt dispatch! [controller action-id payload]
  (return (state/dispatch! controller action-id payload)))
(defn.xt actions-create [controller action-ids]
  (return (state/actions-create controller action-ids)))
(defn.xt open! [controller] (return (state/open! controller)))
(defn.xt close! [controller] (return (state/close! controller)))
