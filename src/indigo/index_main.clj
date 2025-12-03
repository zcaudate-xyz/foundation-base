(ns indigo.index-main
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
             [indigo.client.app.components.chat-message :as cm]
             [indigo.client.app.components.code-viewer :as cv]
             [indigo.client.app.components.component-browser :as cb]
             [indigo.client.app.components.component-palette :as cp]
             [indigo.client.app.components.hierarchy-tree :as ht]
             [indigo.client.app.components.inspector :as ins]
             [indigo.client.app.components.library-browser :as lb]
             [indigo.client.app.components.properties-inspector :as pi]
             [indigo.client.app.components.canvas :as canvas]
             [indigo.client.app.components.inputs-panel :as ip]
             [indigo.client.app.components.properties-panel :as pp]
             [indigo.client.app.components.system-browser :as sb]
             [indigo.client.app :as app]]})


(defn.js AppIndex
  []
  (r/init []
    (client/client-ws "localhost" 1313 {}))
  (return
   [:% app/App]
   #_(r/ui [:app/top
            #_[:% fg/Button "HELLO"]
            #_[:% top-bar/TopBar]
            #_[:% cv/CodeViewer]
            [:% dnd/DndProvider
             {:backend dnd/HTML5Backend}
             [:% sb/SystemBrowser]
             [:% pp/PropertiesPanel]
             [:% pi/PropertiesInspector]
             [:% lb/LibraryBrowser]
             [:% ins/Inspector]
             [:% ip/InputsPanel]
             [:% ht/HierarchyTree]
             [:% cp/ComponentPalette]
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
