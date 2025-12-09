(ns component.web-native-index
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [net.http :as http]))

(l/script :js
  {;;:runtime :websocket
   :config {:bench true
            :id :dev/web-main
            :emit {:native {:suppress true}
                   :lang/jsx false}}
   :require [[js.core :as j]
             [js.react.ext-box :as ext-box]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.lib.rn-expo :as x :include [:lib]]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]
             [xt.lang.event-box :as base-box]
             [component.web-native :as web-native]]
   
   :file   "App.js"})

(defrun.js __import__
  (j/import-missing)
  (j/import-set-global))

(defglobal.js Global
  (base-box/make-box {:l0 "00a-native-text"}))

(defglobal.js Screens
  (base-box/make-box {}))

(defrun.js ^{:rt/init true}
  __screen__
  (base-box/set-data
   -/Screens
   []
   (web-native/raw-controls)))

(defn.js AppMain
  []
  (var [l0 setL0] (ext-box/useBox -/Global ["l0"]))
  (var tree (ext-box/listenBox -/Screens []))
  (return
   [:% n/View
    {:style {:position "absolute",
             :top 0,
             :bottom 0,
             :width "100%"}}
    [:% n/TreePane
     {:tree tree,
      :levels
      [{:type "list",
        :initial l0,
        :setInitial setL0,
        :listWidth 120,
        :displayFn n/displayTarget}]}]]))

(defrun.js ^{:rt/init true}
  __main__
  (base-box/set-data -/Global ["Main"] -/AppMain)
  (client/client-ws "localhost"
                    29001
                    {}))

(defn.js clearScratch
  []
  (base-box/del-data -/Global ["Scratch"]))

(defn.js App []
  (var #{Main} (ext-box/listenBox -/Global []))
  (return [:% Main]))

(def.js MODULE
  (x/registerRootComponent -/App))

(comment
  (l/rt:restart)
  
  (std.make/build-triggered)
  (!.js
   (+ 1 2 3))
  
  (!.js
   (alert "hello"))
  
  (!.js
   (console.log "blah"))

  (!.js
   (+ 1 2 3)))

