(ns play.tui-001-fetch.main
  (:require [std.lang :as l]
            [std.lib :as h]))

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
            :content (j/inspect val {:colors true :depth 0})}]
     [:% -/Button {:top 21 :left 2
                   :action (fn []
                             (-> (j/fetch "https://api.github.com/users/zcaudate"
                                           {:headers {"Accept" "application/vnd.github.v3+json"}
                                            :as "json"})
                                 (j/then  (fn [res] (setVal res)))))
                   :color "gray"
                   :text "GITHUB"}]])))

(defn.js App
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
  (do (:# (!:uuid)))
  (:= (!:G fetch)  j/NodeFetch)
  (b/renderBlessed [:% -/App] (-/Screen)))
