(ns xt.ui.catalog
  "Compatibility facade for xt.ui.widgets.core."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.ui.widgets.core :as widgets]]})

(defn.xt register [registry component-id props events slots]
  (return (widgets/register registry component-id props events slots)))
(defn.xt semantic-registry [] (return (widgets/semantic-registry)))
(defn.xt registry [] (return (widgets/registry)))
