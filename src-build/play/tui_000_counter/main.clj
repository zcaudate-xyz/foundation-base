(ns play.tui-000-counter.main
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(fact:global
 {:prelim  [(require 'play.tui-000-counter.build)
            (eval (std.make/run:init play.tui-000-counter.build/PROJECT))]
  :setup   [(eval (std.make/run:dev play.tui-000-counter.build/PROJECT))]})

(l/script :js
  {:require [[js.react :as r]
             [js.core :as j]
             [js.blessed :as b]]
   :static {:export false}})

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

(defn.js Counter
  ([]
   (let [[count setCount] (r/local 0)]
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
                      :color "green"}]]]))))

(defn.js App
  ([]
   (return
    [:box {:label  "Tui 000 - Counter"
           :border "line"
           :style  {:border {:fg "green"}}}
     [:box {:left 5}
      [:box {:top 3}
       [:text {:top -1 :left 1} "COUNTER"]
       [:<Counter>]]]])))

(defn.js Screen
  ([]
   (const screen (b/screen
                  {:autoPadding true
                   :smartCSR true
                   :title "Tui 000 - Counter"}))
   (screen.key ["q" "C-c" "Esc"]
               (fn []
                 (. this (destroy))))
   (return screen)))

(defrun.js __init__
  (do (:# (!:uuid))
      (b/renderBlessed [:% -/App] (-/Screen))))
