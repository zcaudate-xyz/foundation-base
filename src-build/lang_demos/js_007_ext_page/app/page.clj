(ns lang-demos.js-007-ext-page.app.page
  (:require [lang.core :as l]))

(l/script :js
  {:static {:export [Page]}})

(defn.js Page
  []
  (return [:% App]))
