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
             [js.lib.figma :as fg]
             [js.lib.react-dnd :as dnd]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]
             [code.dev.client.app.components.chat-message :as cm]
             [code.dev.client.app.components.code-viewer :as cv]
             [code.dev.client.app.components.component-browser :as cb]
             [code.dev.client.app.components.canvas :as canvas]
             #_#_#_[code.dev.webapp.layout-top-bar :as top-bar]
             [code.dev.webapp.layout-hierarchy-tree :as hierarchy-tree]
             [code.dev.webapp.layout-library-browser :as library-browser]
             ]})


(defn.js AppIndex
  []
  (r/init []
    (client/client-ws "localhost" 1313 {}))
  (return
   (r/ui [:app/top
          #_[:% fg/Button "HELLO"]
          #_[:% top-bar/TopBar]
          #_[:% cv/CodeViewer]
          [:% dnd/DndProvider
           {:backend dnd/HTML5Backend}
           [:% cb/ComponentBrowser]
           [:% canvas/Canvas]]
          [:% cm/ChatMessage
           {:message {:text "hello"
                      :user "hello"
                      :timestamp (Date.now)}}]
          [:app/body]]
     [{:app/top     [:div
                     {:class ["flex flex-col w-full"]
                      :style {:top 0 :bottom 0}}]
       
       :app/body
       [:rx/theme
        [:*/h
         {:style {:top 0 :bottom 0}}
         #_[:% hierarchy-tree/HierarchyTree]
         #_[:% library-browser/LibraryBrowser]
         #_[:*/v {:gap 3}
          [:rx/button "HELLO"]
          [:rx/button "HELLO"]
          [:rx/button "HELLO"]]]]}])))

(defn.js main
  []
  (r/renderDOMRoot "root" -/AppIndex))

(defrun.js __main__
  (-/main))

(comment

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
  
  
  #_[:% puck/Puck {:config -/config
                   :data -/initialData
                   :onPublish -/save}])
