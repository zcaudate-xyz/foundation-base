(ns lang-demos.js-006-nextjs-websocket.app.page
  (:require [lang.core :as l]))

(l/script :js
  {:static {:export [Page]}})

(defn.js Page
  []
  (var socketHost (. process env NEXT_PUBLIC_LANG_WS_HOST))
  (var socketPort (Number (or (. process env NEXT_PUBLIC_LANG_WS_PORT) "29002")))
  (return [:% App
           {"socketHost" socketHost
            "socketPort" socketPort}]))
