(ns lang-demos.js-006-nextjs-websocket.main
  (:require [lang.core :as l]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/nextjs-websocket
            :port 29002}
   :import [["react" :as React]]
   :require [[xt.lang.common-client :as client]
             [xt.event.base-box :as base-box]
             [js.react.ext-box :as ext-box]
             [js.react.helper-data :as helper]]})

(defn.js UserCard
  [#{name address status}]
  (return
   [:article {:className "user-card"}
    [:div {:className "user-card-status"}
     [:span {:className "user-card-dot"}]
     [:span status]]
    [:h2 name]
    [:p address]]))

(def.js WrappedUserCard
  (helper/wrapData -/UserCard))

(def.js WrappedGrid
  (helper/wrapData "div"))

(defn.js AppMain
  []
  (return
   [:section {:className "users-panel"}
    [:div {:className "panel-heading"}
     [:div
      [:p {:className "eyebrow"} "LIVE COMPONENT"]
      [:h2 "Team status"]]
     [:span {:className "panel-count"} "4 users"]]
    [:% -/WrappedGrid
     {"className" "user-grid"
      "$data" {"$.user1" {"name" "Aaron"
                           "address" "Address A"
                           "status" "busy"}
               "$.user2" {"name" "Bill"
                          "address" "Address B"
                          "status" "busy"}
               "$.user3" {"name" "Charlie"
                          "address" "Address C"
                          "status" "busy"}
               "$.user4" {"name" "David"
                          "address" "Address D"
                          "status" "busy"}}}
     [:% -/WrappedUserCard {"$id" "user1" :key "user1"}]
     [:% -/WrappedUserCard {"$id" "user2" :key "user2"}]
     [:% -/WrappedUserCard {"$id" "user3" :key "user3"}]
     [:% -/WrappedUserCard {"$id" "user4" :key "user4"}]]]))

(defglobal.js Global
  (base-box/make-box {}))

(defrun.js __init__
  (base-box/set-data -/Global ["Main"] -/AppMain))

(defn.js App
  [#{socketHost socketPort}]
  (var [connectionStatus setConnectionStatus]
       (React.useState "waiting"))
  (React.useEffect
   (fn []
     (var location (. window location))
     (var host (or socketHost (. location hostname)))
     (var port (or socketPort 29002))
     (setConnectionStatus "connecting")
     (var connection
          (client/client-ws host port
                            {:secured (== (. location protocol) "https:")}))
     (. connection
        (addEventListener "open"
                          (fn [] (setConnectionStatus "connected"))))
     (. connection
        (addEventListener "error"
                          (fn [] (setConnectionStatus "error"))))
     (. connection
        (addEventListener "close"
                          (fn [] (setConnectionStatus "disconnected"))))
     (return
      (fn []
        (. connection (close)))))
   [socketHost socketPort])
  (var #{Main} (ext-box/listenBox -/Global []))
  (return
   [:main {:className "shell"}
    [:header {:className "hero"}
     [:div
      [:p {:className "eyebrow"} "FOUNDATION / LIVE TRANSFORM"]
      [:h1 "A page that can change while it runs"]
      [:p {:className "hero-copy"}
       "This Next.js page keeps a live view of the demo data. Connect the websocket to apply changes while the page stays open."]]
     [:div {:className "connection-card"}
      [:span {:className (+ "connection-dot " connectionStatus)}]
      [:div
       [:span {:className "connection-label"} "WEBSOCKET"]
       [:strong connectionStatus]]
      [:code (+ (or socketHost "page host") ":" (or socketPort 29002))]]]
    [:% Main]
    [:footer {:className "footer"}
     "The page listens to the shared reactive box while the websocket evaluates updates in the browser."]]))
