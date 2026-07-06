(ns hara.model.spec-python-varargs
  (:require [hara.common.emit-common :as common]))

(defn emit-input-rest
  [{:keys [symbol]} grammar mopts]
  (str "*" (common/*emit-fn* symbol grammar mopts)))

(defn prepare-body
  [_ body _ _]
  body)
