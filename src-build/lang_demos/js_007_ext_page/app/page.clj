(ns lang-demos.js-007-ext-page.app.page
  (:require [lang.core :as l]))

(l/script :js
  {:require [[lang-demos.js-007-ext-page.generated.main :as main-app]]
   :static {:export [Page]
            :flags {:nextjs
                    {:use-client true}}}})

(defn.js Page
  []
  (return [:% main-app/App]))
