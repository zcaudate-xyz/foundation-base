(ns lang-demos.tui-000-counter.main
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/target-websocket
            :port 28000
            :emit {:lang/jsx false}}
   :require [[js.react :as r]
             [js.blessed :as b]
             [js.module :as jm]
             [xt.lang.common-client :as client]]})

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
                               (action))) ;33M
             :padding {:top 1 :right 2 :bottom 1 :left 2}
             :style {:bg (:? (not disabled) color "black")
                     :fg (:? (not disabled) "white" "gray")
                     :focus {:bold true}}}]))

(defn.js Counter
  ([]
   (let [[count setCount] (r/local 0)
         [auto setAuto] (r/local false)]
     (r/useInterval
      (fn [] (setCount (mod (+ count 1) 10)))
      (:? auto 1000 nil))
     (return
      [:box
       [:box {:padding {:top 2 :right 5 :bottom 2 :left 5}
              :width 14 :height 7
              :border "line"}
        count]
       [:% -/Button {:top 2 :left 16
                     :action (fn [] (setCount 0))
                     :color "gray"
                     :text "RESET"}]
       [:box {:top 8}
        [:% -/Button {:text "DEC"
                      :action (fn [] (setCount
                                      (mod (- (+ count 10) 1)
                                           10)))
                      :color "red"}]
        [:% -/Button {:left 7
                      :text "INC"
                      :action (fn [] (setCount (mod (+ count 1)
                                                    10)))
                      :color "green"}]
        [:% -/Button {:left 14
                      :text "AUTO"
                      :action (fn [] (setAuto (not auto)))
                      :color (:? auto "blue" "default")}]]]))))

(defn.js AppMain
  ([]
   (return
    [:box {:label  "Tui 000 - Counter"
           :border "line"
           :style  {:border {:fg "green"}}}
     [:box {:left 5}
      [:box {:top 3}
       [:text {:top -1 :left 1} "COUNTER"]
       [:% -/Counter]]]])))

(defn.js Screen
  ([]
   (var screen (b/screen
                {:autoPadding true
                 :smartCSR true
                 :title "Tui 000 - Counter"}))
   (screen.key ["q" "C-c" "Esc"]
               (fn []
                 (. this (destroy))))
   (return screen)))

(defrun.js __init__
  (do
    (:# (!:uuid))
    (client/client-ws "localhost" 28000 {})
    (b/renderBlessed [:% -/AppMain] (-/Screen))))

(comment

  (!.js
    (b/renderBlessed [:box] (-/Screen))
    nil)
  
  (!.js
    (b/renderBlessed [:% -/AppMain] (-/Screen))
    nil)
  
  (!.js
    (+ 1 2 3)))
