(ns lang-demos.js-003-expo-rn.main
  (:require [lang.core :as l]
            [std.lib :as h]
            [std.string :as str]
            [net.http :as http]))

(l/script :js
  {:runtime :websocket
   :config {:bench true
            :id :dev/web-main
            :emit {:native {:suppress true}
                   :lang/jsx false}}
   
   :import [["react" :as React]]
   :require [[js.module :as jm]
             [js.react :as r]
             [js.react.ext-box :as ext-box]
             [js.react-native :as n]
             [js.react.helper-data :as helper]
             [js.lib.rn-expo :as x]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-client :as client]
             [xt.event.base-box :as base-box]]})

(defrun.js __import__
  (jm/import-missing)
  (jm/import-set-global))

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
               #_#_"$.user4" {:name "Charlie"
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
       :key "3"}]
     #_[:% -/WrappedUserCard
      {"$id" "user3"
       :key "4"}]]
    #_[:% -/WrappedUserCard
       {"$" {:name "Hello"
             :address "World"
             :status "now"}}]]))

(defglobal.js Global
  (base-box/make-box {}))

(defrun.js ^{:rt/init false}
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



(comment

  ^*(!.js
    -/Global)
  
  ^*(!.js
      (base-box/set-data -/Global ["Main"] -/AppMain))
  
  )

(comment
  (!.js
    [:div "hello"])
  
  (h/prn "ehhol")
  
  (h/with-out-str
    (l/with:print
      (h/suppress
       (!.js
         (base-box/set-data -/Global ["Main"] -/AppMain)))))
  
  (base-box/set-data)

  (l/with:print
    (!.js
      -/AppMain))
  
  (l/with:print
    (!.js
      -/Global))
  
  (l/with:print
    (!.js
      (+ 1 2 3)))
  
  (!.js
    )
  
  (!.js
    (base-box/set-data -/Global ["Main"] -/AppMain))
  
  (!.js
    (client/client-ws "localhost"
                      29001
                      {}))
  
  (!.js
    (alert "hello"))
  
  (!.js
    (console.log "hello"))
  
  (!.js
    (+ 1 2 3))

  (!.js
    React)
  (!.js
    ReactNative)
  )
