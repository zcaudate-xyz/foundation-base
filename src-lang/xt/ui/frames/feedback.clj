(ns xt.ui.frames.feedback
  "Reusable pending, error and empty presentation."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]]})

(defn.xt view [state fallback]
  (when (== true (xt/x:get-key state "pending"))
    (return (ui/node "ui/spinner" {"label" "Loading"} [])))
  (when (xt/x:not-nil? (xt/x:get-key state "error"))
    (return (ui/node "ui/alert" {"tone" "error"}
                     [(ui/text (xt/x:to-string (xt/x:get-key state "error")) {})])))
  (return fallback))
