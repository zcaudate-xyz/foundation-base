(ns dupasupa.bindings
  (:require [lang.core :as l]
            [dupasupa.pages :as pages]))
(l/script :js
  {:require [[app.manage.page-runtime :as runtime]] :static {:export [DEFINITIONS request]}})
(def.js DEFINITIONS (@! (pages/definitions)))
(defn.js request
  [page kind member values]
  (return (runtime/request (runtime/entry -/DEFINITIONS page kind member) values)))
