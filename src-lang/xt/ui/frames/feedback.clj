(ns xt.ui.frames.feedback
  "Reusable pending, error and empty presentation."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]]})

(defn.xt view [state fallback]
  (when (== true (. state ["pending"]))
    (return (ui/node "ui/spinner" {"label" "Loading"} [])))
  (when (xt/x:not-nil? (. state ["error"]))
    (return (ui/node "ui/alert" {"tone" "error"}
                     [(ui/text (xt/x:to-string (. state ["error"])) {})])))
  (return fallback))
