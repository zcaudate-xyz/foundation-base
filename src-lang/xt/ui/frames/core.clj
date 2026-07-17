(ns xt.ui.frames.core
  "Declarative frame specifications and native region overrides."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt spec [id kind regions opts]
  (return {"id" id
           "kind" kind
           "regions" (or regions {})
           "opts" (or opts {})}))

(defn.xt region [frame region-id fallback]
  (return (or (xt/x:get-path frame ["regions" region-id]) fallback)))

(defn.xt override [frame overrides]
  (var next (xtd/clone-nested frame))
  (xt/for:object [[region-id value] (or overrides {})]
    (xtd/set-in next ["regions" region-id] value))
  (return next))
