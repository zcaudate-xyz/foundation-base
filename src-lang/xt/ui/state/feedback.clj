(ns xt.ui.state.feedback
  "Normalized pending, error and retry state."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt create []
  (return {"pending" false "error" nil "message" nil "retryable" false}))

(defn.xt pending! [state value]
  (xt/x:set-key state "pending" (== true value))
  (when (== true value) (xt/x:set-key state "error" nil))
  (return state))

(defn.xt fail! [state error retryable]
  (xt/x:set-key state "pending" false)
  (xt/x:set-key state "error" error)
  (xt/x:set-key state "retryable" (== true retryable))
  (return state))

(defn.xt clear! [state]
  (xt/x:set-key state "pending" false)
  (xt/x:set-key state "error" nil)
  (xt/x:set-key state "message" nil)
  (xt/x:set-key state "retryable" false)
  (return state))
