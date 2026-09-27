(ns lang-demos.tui-001-fetch.main
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/target-websocket
            :port 28001
            :emit {:lang/jsx false}}
   :require [[js.react :as r]
             [js.blessed :as b]
             [js.module :as jm]
             [xt.lang.common-client :as client]]
   :import [["node:util" :as util]]})

(defrun.js __missing__
  (jm/import-missing)
  (jm/import-set-global))

(defn.js Button
  [{:# [left top text disabled color action]}]
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
                             color
                             "black")
                     :fg (:? (not disabled)
                             "white"
                             "gray")
                     :focus {:bold true}}}]))

(defn.js Fetch
  ([]
   (var [val setVal] := (r/local {}))
   (return
    [:box
     [:box {:keys true
            :mouse true
            :width 80 :height 20
            :border "line"
            :scrollable true
            :scrollbar {:style {:bg "gray"
                                :fg "gray"}
                        :track true}
            :content (. util (inspect val {:colors true :depth 0}))}]
     [:% -/Button {:top 21 :left 2
                   :action (fn []
                             (. (fetch "https://api.github.com/users/zcaudate"
                                       {:headers {"Accept" "application/vnd.github.v3+json"}
                                        :as "json"})
                                (then (fn [res] (setVal res)))))
                   :color "gray"
                   :text "GITHUB"}]
     [:% -/Button {:top 21 :left 15
                   :text "Clear"
                   :action (fn []
                             (setVal {}))}]])))

(defn.js AppMain
  ([]
   (return
    [:box {:label  "Tui 001 - Fetch"
           :border "line"
           :style  {:border {:fg "green"}}}
     [:box {:left 5}
      [:box {:top 3}
       [:text {:top -1 :left 1} "RESULT"]
       [:% -/Fetch]]]])))

(defn.js Screen
  ([]
   (var screen (b/screen
                {:autoPadding true
                 :smartCSR true
                 :title "Tui Fetch Basic"}))
   (screen.key ["q" "C-c" "Esc"]
               (fn []
                 (. this (destroy))))
   (return screen)))

(defrun.js __init__
  (:# (!:uuid))
  (:= (!:G fetch)  (require "node-fetch"))
  (client/client-ws "localhost" 28000 {})
  (b/renderBlessed [:% -/AppMain] (-/Screen)))

(comment

  (!.js
    globalThis.ReactBlessed)

  (!.js
    (x:nil? 
     (. globalThis ["React"])))

  (!.js
    [:box])
  
  (!.js
    (x:nil? 
     globalThis.Blessed))
  
  (!.js
    (b/renderBlessed [:box] (-/Screen))
    nil)
  
  (!.js
    (b/renderBlessed [:% -/AppMain] (-/Screen))
    nil)
  
  (!.js
    (+ 1 2 3)))
