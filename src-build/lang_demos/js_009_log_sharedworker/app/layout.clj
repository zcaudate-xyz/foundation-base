(ns lang-demos.js-009-log-sharedworker.app.layout
  (:require [lang.core :as l]))

(l/script :js
  {:import [["./globals.css" :ensure true]]
   :static {:export [RootLayout]}})

(defn.js RootLayout
  [#{children}]
  (return
   [:html {:lang "en"}
    [:head
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:meta {:name "description"
             :content "A Melbourne UI activity log backed by a shared database worker."}]
     [:title "Shared activity log"]]
    [:body children]]))
