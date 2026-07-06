(ns hara.model.spec-python.rest
  (:require [hara.common.emit-common :as common]))

(defn emit-input-rest
  [{:keys [symbol]} grammar mopts]
  (str "*" (common/*emit-fn* symbol grammar mopts)))
