(ns hara.model.spec-ruby.rest
  (:require [hara.common.emit-common :as common]))

(defn emit-input-rest
  [{:keys [symbol]} grammar mopts]
  (str "*" (common/*emit-fn* symbol grammar mopts)))
