(ns code.dev.index-main
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:port 1313}
   :require [[js.react :as r]
             [js.core :as j]
             [js.lib.puck :as puck]
             [js.lib.radix :as radix]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]
             [code.dev.webapp.layout-top-bar :as top-bar]
             [code.dev.webapp.layout-hierarchy-tree :as hierarchy-tree]
             [code.dev.webapp.layout-library-browser :as library-browser ]]})

(def.js config
  {:components
   {:HeadingBlock
    {:fields
     {:children
      {:type "text"}}
     :render
     (fn [#{children}]
       (return
        [:h1 children]))}}})

(def.js initialData {})

(def.js save
  (fn [data]
    (return nil)))

(defn.js AppIndex
  []
  (r/init []
    (client/client-ws "localhost" 1313 {}))
  (return
   (r/ui [:app/top
          [:% top-bar/TopBar]
          [:app/body]]
     [{:app/top     [:div
                     {:class ["flex flex-col w-full"]
                      :style {:top 0 :bottom 0}}]
       
       :app/body
       [:rx/theme
        [:*/h
         {:style {:top 0 :bottom 0}}
         #_[:% hierarchy-tree/HierarchyTree]
         [:% library-browser/LibraryBrowser]
         [:*/v {:gap 3}
          [:rx/button "HELLO"]
          [:rx/button "HELLO"]
          [:rx/button "HELLO"]]]]}])))

(defn.js main
  []
  (r/renderDOMRoot "root" -/AppIndex))

(defrun.js __main__
  (-/main))

(comment
  
  #_[:% puck/Puck {:config -/config
                   :data -/initialData
                   :onPublish -/save}])
