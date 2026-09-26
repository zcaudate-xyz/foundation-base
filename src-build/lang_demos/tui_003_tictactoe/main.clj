(ns lang-demos.tui-003-tictactoe.main
  (:require [lang.core :as  l]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/tui-003-tictactoe
            :port 28003}
   :require [[js.react :as r]
             [js.blessed :as b]
             [js.react.ext-box :as ext-box]
             [js.react.helper-data :as helper]
             [xt.event.base-box :as base-box]
             [xt.lang.common-client :as client]]})

(defn.js Button
  [#{left top text disabled color action}]
  (return
   [:button {:left (or left 0)
             :top  (or top 0)
             :content text
             :shrink true
             :mouse true
             :onPress (fn [] (if (and action
                                      (not disabled))
                               (action)))
             :padding {:top 1 :right 2 :bottom 1 :left 2}
             :style {:bg (:? (not disabled)
                             [color
                              "black"])
                     :fg (:? (not disabled)
                             ["white"
                              "gray"])
                     :focus {:bold true}}}]))

(defn.js AppMain
  ([]
   (return
    [:box {:label  "Tui 003 - Tictactoe"
           :border "line"
           :style  {:border {:fg "green"}}}
     [:box {:left 5}
      [:box {:top 3}]]])))

(defglobal.js Global
  (base-box/make-box {"AppMain" nil}))

(defn.js App
  ([]
   (var AppMain (ext-box/listenBox -/Global ["AppMain"]))
   (return [:% AppMain])))

(defn.js Screen
  ([]
   (var screen (b/screen
                {:autoPadding true
                 :smartCSR true
                 :title "Tui 003 - Tictactoe"}))
   (screen.key ["q" "C-c" "Esc"]
               (fn []
                 (. this (destroy))))
   (return screen)))

(defrun.js __init__
  (do (:# (!:uuid))
      (base-box/set-data -/Global
                         ["AppMain"]
                         (fn [] (return -/AppMain)))
      (client/client-ws "localhost" 28003)
      (b/renderBlessed [:% -/App] (-/Screen))))

(comment

  (b/renderBlessed [:% -/App]
                   [:box {:label  "Tui 003 - Tictactoe"
                          :border "line"
                          :style  {:border {:fg "green"}}}])
  
  (b/renderBlessed [:% -/App]
                   [:box {:label  "Tui 003 - Tictactoe"
                          :border "line"
                          :style  {:border {:fg "green"}}}])
  )


(comment

  (!.js
    (+ 1 2 3))

  )


