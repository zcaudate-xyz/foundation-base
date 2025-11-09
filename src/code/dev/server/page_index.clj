(ns code.dev.server.page-index
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config  {:port 1312}
   :require [[js.react :as r :include [:dom]]
             [xt.lang.base-client :as client]]})

(defglobal.js Root nil)

(defn.js Header
  [#{context}]
  (var #{api} (:? context
                  (r/useContext context)
                  {}))
  (return
   [:nav {:className "bg-white shadow-md p-4 sticky top-0 z-50"}
    [:div {:className "flex items-center"}
     [:a {:className "text-2xl font-display text-brand-dark font-bold"}
      "Dev"]]]))

(defn.js Body
  [#{context}]
  (var #{api} (:? context
                  (r/useContext context)
                  {}))
  (return
   [:nav {:className "bg-white shadow-md p-4 sticky top-0 z-50"}
    [:div {:className "flex items-center"}
     [:a {:className "text-2xl font-display text-brand-dark font-bold"}
      "Dev"]]]))



(defn.js Footer
  [#{context}]
  (var #{api} (:? context
                  (r/useContext context)
                  {}))
  (return
   [:nav {:className "bg-white shadow-md p-4 sticky top-0 z-50"}
    [:div {:className "flex items-center"}
     [:a {:className "text-2xl font-display text-brand-dark font-bold"}
      "Footer"]]]))

(defn.js App
  []
  (r/init []
    (client/client-ws "localhost" 1312 {}))
  (return
   [:<> #_#_div {:className ""}
    [:% -/Header]
    [:div {:className "flex-auto"}]
    [:% -/Footer]]))

(defn.js main
  []
  (var rootElement (document.getElementById "root"))
  (when (not -/Root)
    (:= -/Root (ReactDOM.createRoot rootElement)))
  (. -/Root (render [:% -/App]))
  (return true))




(comment
  (l/rt:restart)
  (!.js
    (-/main))
  
  ^*(!.js
      [:a]))
