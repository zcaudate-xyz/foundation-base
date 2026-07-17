(ns xt.ui.state.dev
  "Sanitized, capability-gated diagnostics state."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt create [enabled capabilities]
  (return {"enabled" (== true enabled)
           "capabilities" (or capabilities {})
           "status" "idle"
           "summary" {}
           "panels" {}}))

(defn.xt available? [state capability-id]
  (return (and (== true (xt/x:get-key state "enabled"))
               (== true (xt/x:get-path state ["capabilities" capability-id])))))

(defn.xt set-summary! [state summary]
  (xt/x:set-key state "summary" (or summary {}))
  (xt/x:set-key state "status" "ready")
  (return state))
