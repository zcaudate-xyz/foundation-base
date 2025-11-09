(ns code.dev.server.frontend
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.html :as html]
            [std.string :as str]
            [code.dev.server.page-demo :as demo]
            [code.dev.server.page-index :as index]
            [std.lang.base.runtime :as default]))

(defn emit-main
  [ns]
  (l/emit-script
   '(-/main)
   {:lang :js
    :library (l/default-library)
    :module  (l/get-module (l/default-library)
                           :js
                           ns)
    :emit {:native {:suppress true}
           :lang/jsx false}
    :layout :flat}))


(defn make-page
  [{:keys [title
           body]}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:title title]
    #_[:link   {:href "https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.3.0/flowbite.min.css"
                :rel "stylesheet"}]
    [:script {:src "https://cdn.tailwindcss.com"}]
    [:script {:src "https://unpkg.com/react@18/umd/react.development.js", :crossorigin ""}]
    [:script {:src "https://unpkg.com/react-dom@18/umd/react-dom.development.js" :crossorigin ""}]
    #_[:script {:src "https://unpkg.com/@babel/standalone/babel.min.js"}]
    [:script {:src "https://unpkg.com/react-query@3/dist/react-query.production.min.js"}]
    #_#_
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.3.0/flowbite.min.js"}]
    [:script {:src "https://unpkg.com/flowbite-react@0.9.0/dist/flowbite-react.umd.js"}]
    
    ]
   [:body {:class "h-screen w-screen flex bg-gray-200"}
    [:div {:id "root"}]
    [:script #_{:type "text/babel"}
     {:type "text/javascript"}
     (or body "")]]])

(defn index-page
  []
  (make-page
   {:title "Dev Interface"
    :body (emit-main 'code.dev.server.page-index)}))

(defn demo-page
  []
  (make-page
   {:title "Dev Demo Page HELLO WORLD"
    :body (emit-main 'code.dev.server.page-demo)}))


