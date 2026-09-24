(ns lang-demos.js-006-nextjs-websocket.app.layout
  (:require [lang.core :as l]))

(l/script :js
  {:static {:export [RootLayout]}})

(defn.js RootLayout
  [#{children}]
  (return
   [:html {:lang "en"}
    [:head
     [:title "Live websocket page"]
     [:meta {:name "description"
             :content "A Next.js page updated through the Foundation websocket runtime."}]]
    [:body children]]))
