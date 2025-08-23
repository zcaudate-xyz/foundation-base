(ns playground.web-debug-index
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [net.http :as http]))

(l/script :js
  {;:runtime :websocket
   :config {:bench true
            :id :dev/web-main
            :emit {:native {:suppress true}
                   :lang/jsx false}}
   :require [[js.core :as j]
             [js.react :as r]
             [js.react.ext-box :as ext-box]
             [js.react-native :as n :include [:fn]]
             [js.react.helper-data :as helper]
             [js.lib.rn-expo :as x :include [:lib]]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]
             [xt.lang.event-box :as base-box]]
   :export [MODULE]
   :file   "App.js"})

(defrun.js __import__
  (j/import-missing)
  (j/import-set-global))

(comment
  #_(k/LOG! {:top top
             :ntop ntop
             :$id $id
             :$data $data
             :props props}))

(defn.js UserCard
  [#{name address status}]
  (return
   [:% n/View
    {:style {:padding "10px"}}
    [:% n/Text (+ "Name: " name)]
    [:% n/Text (+ "Address: " address)]
    [:% n/Text (+ "Status: " status)]]))

(def.js WrappedUserCard
  (helper/wrapData -/UserCard))

(def.js WrappedView (helper/wrapData n/View))

(defn.js AppMain
  []
  (return
   [:% n/View
    {:style {:position "absolute",
             :top 0,
             :bottom 0,
             :width "100%"
             :backgroundColor ""}}
    [:% -/WrappedView
     {"$data" {"$.user1" {:name "Aaron"
                          :address "Address A"
                          :status "busy"}
               "$.user2" {:name "Bill"
                          :address "Address B"
                          :status "busy"}
               "$.user3" {:name "Charlie"
                          :address "Address C"
                          :status "busy"}
               "$.user4" {:name "Charlie"
                          :address "Address C"
                          :status "busy"}}
      :style {:position "absolute",
              :top 10,
              :left 10
              :height 300
              :width 150
              :backgroundColor "yellow"}}
     [:% n/View
      {:key "1"}
      [:% -/WrappedUserCard
       {"$id" "user1"}]]
     [:% -/WrappedUserCard
      {"$id" "user2"
       :key "2"}]
     [:% -/WrappedUserCard
      {"$id" "user3"
       :key "3"}]]
    #_[:% -/WrappedUserCard
       {"$" {:name "Hello"
             :address "World"
             :status "now"}}]]))

(defglobal.js Global
  (base-box/make-box {}))

(defrun.js ^{:rt/init true}
  __main__
  (base-box/set-data -/Global ["Main"] -/AppMain)
  (client/client-ws "localhost"
                    29001
                    {}))

(defn.js App []
  (var #{Main} (ext-box/listenBox -/Global []))
  (return [:% Main]))

(def.js MODULE
  (x/registerRootComponent -/App))
