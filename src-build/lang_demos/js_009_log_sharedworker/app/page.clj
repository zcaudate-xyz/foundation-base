(ns lang-demos.js-009-log-sharedworker.app.page
  (:require [lang.core :as l]))

(l/script :js
  {:static {:export [Page]
            :flags {:nextjs {:use-client true
                             :header "import dynamic from 'next/dynamic';\nconst App = dynamic(() => import('../generated/main.js').then((mod) => mod.App), { ssr: false });"}}}})

(defn.js Page
  []
  (return [:% App]))
