(ns code.dev.index-main
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:port 1311}
   :require [[js.react :as r :include [:dom]]
             [js.core :as j]
             [js.lib.puck :as puck]
             [js.lib.radix :as radix]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]]})

(defn.js HEllo
  []
  Process)

(def.js config
  {:components
   {:HeadingBlock
    {:fields
     {:children
      {:type "text"}}}
    :render
    (fn [#{children}]
      (return
       [:h1 children]))}})

(def.js initialData {})

(def.js save
  (fn [data]
    (return nil)))

(defn.js AppIndex
  []
  (r/init []
    (client/client-ws "localhost" 1311 {}))
  (return
   (r/ui [:app/top
          [:app/body]]
     {:app/top     [:div
                    {:class ["flex flex-col w-full"]
                     :style {:top 0 :bottom 0}}]
      
      :app/body
      [:% puck/Puck {:config -/config
                     :data -/initialData
                     :onPublish -/save}]})))

(def.js AppRoot nil)

(defn.js main
  []
  (j/import-set-global)
  (var rootElement (document.getElementById "root"))
  (when (not -/AppRoot)
    (:= -/AppRoot (ReactDOM.createRoot rootElement)))
  (. -/AppRoot (render [:% -/AppIndex]))
  (return true))
