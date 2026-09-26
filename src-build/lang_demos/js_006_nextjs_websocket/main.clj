(ns lang-demos.js-006-nextjs-websocket.main
  (:require [lang.core :as l]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/nextjs-websocket
            :port 29003}
   :import [["react" :as React]]
   :require [[js.module :as jm]
             [xt.lang.common-client :as client]
             [xt.event.base-box :as base-box]
             [js.react.ext-box :as ext-box]
             [js.react.helper-data :as helper]]})

(defrun.js __import__
  (jm/import-missing)
  (jm/import-set-global))

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
  (base-box/make-box {"DebugConnection" "waiting"}))

(defglobal.js DebugConnection
  (client/client-ws "localhost"
                    (or (. process env NEXT_PUBLIC_LANG_WS_PORT) "29003")
                    {:listeners {"open"  (fn []
                                           (console.log "open")
                                           (base-box/set-data -/Global ["DebugConnection"] "connected"))
                                 "close" (fn []
                                           (console.log "close")
                                           (base-box/set-data -/Global ["DebugConnection"] "disconnected"))
                                 "error" (fn []
                                           (console.log "error")
                                           (base-box/set-data -/Global ["DebugConnection"] "errored"))}}))

(defrun.js __init__
  (base-box/set-data -/Global ["AppMain"]
                     (fn [] (return -/AppMain))))

(defn.js App
  [#{socketHost socketPort}]
  (var AppMain (ext-box/listenBox -/Global ["AppMain"]))
  (var DebugConnection (ext-box/listenBox -/Global ["DebugConnection"]))
  (return
   [:main {:className "shell"}
    [:header {:className "hero"}
     [:div
      [:p {:className "eyebrow"} "FOUNDATION / LIVE TRANSFORM"]
      [:h1 "A page that can change while it runs"]
      [:p {:className "hero-copy"}
       "This Next.js page keeps a live view of the demo data. Connect the websocket to apply changes while the page stays open."]]
     [:div {:className "connection-card"}
      [:span {:className (+ "connection-dot " DebugConnection)}];19M
      [:div
       [:span {:className "connection-label"} "WEBSOCKET"]
       [:strong DebugConnection]]
      [:code (+ (or socketHost "page host") ":" (or socketPort 29002))]]]
    [:% AppMain]
    [:footer {:className "footer"}
     "The page listens to the shared reactive box while the websocket evaluates updates in the browser."]]))

(comment
  
  (!.js
    (base-box/set-data -/Global ["AppMain"]
                       (fn [] (return -/AppMain))))
  
  (!.js
    (. (base-box/get-data -/Global ["Main"])
       (toString)))
  (!.js
    (+ 1 2 3))
  
  (!.js
    -/Global))
