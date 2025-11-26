(ns indigo.tool.explore
  (:require [std.lib.resource :as res]))

(defn explore-resources-tool
  [_]
  (res/res:spec-list))

(defn inspect-resource-tool
  [{:keys [resource-name]}]
  (res/res:spec-get (keyword resource-name)))
