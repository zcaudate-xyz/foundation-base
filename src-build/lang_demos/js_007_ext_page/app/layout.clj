(ns lang-demos.js-007-ext-page.app.layout
  (:require [lang.core :as l]))

(l/script :js
  {:static {:export [RootLayout]
            :flags {:nextjs {:header "import './globals.css';"}}}})

(defn.js RootLayout
  [#{children}]
  (return
   [:html {:lang "en"}
    [:head
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:meta {:name "description"
             :content "A Melbourne UI activity log built with Foundation ext-page."}]
     [:title "Shared activity log"]]
    [:body children]]))
