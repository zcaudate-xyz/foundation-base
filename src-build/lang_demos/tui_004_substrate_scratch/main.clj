(ns lang-demos.tui-004-substrate-scratch.main
  (:require [lang.core :as l]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/tui-004-substrate-scratch
            :port 28004
            :emit {:lang/jsx false}}
   :require [[js.react :as r]
             [js.blessed :as b]
             [js.module :as jm]
             [xt.lang.common-client :as client]
             [xt.lang.common-data :as data]
             [xt.lang.spec-base :as xt]
             [lang-demos.js-002-substrate-scratch.main :as substrate]]
   :import [["node:util" :as util]]})

(defrun.js __missing__
  (jm/import-missing)
  (jm/import-set-global))

(defn.js inspect-value
  [value]
  (return (. util (inspect value {:colors true
                                  :depth 5
                                  :compact 2
                                  :breakLength 68}))))

(defn.js SliceButton
  [#{entry index active onSelect}]
  (var demo-id (. entry ["id"]))
  (return
   [:button {:left 1
             :top (+ 1 (* index 3))
             :width 30
             :height 2
             :content (+ (:? active ">" " ")
                         " "
                         (. entry ["title"]))
             :keys true
             :mouse true
             :shrink true
             :onPress (fn [] (onSelect demo-id))
             :style {:bg (:? active "blue" "black")
                     :fg (:? active "white" "cyan")
                     :focus {:bold true}}}]))

(defn.js AppMain
  []
  (var [demo-id set-demo-id] (r/local "currencies"))
  (var selected
       (data/arr-find substrate/DEMOS
                      (fn [entry]
                        (return (== (. entry ["id"]) demo-id)))))
  (var model (substrate/demo-model demo-id substrate/DEFAULT_USER_ID))
  (var event (substrate/example-event demo-id))
  (return
   [:box {:label "TUI 004 - Substrate Scratch"
          :border "line"
          :style {:border {:fg "green"}}}
    [:text {:left 2 :top 0} "Select a scratch_v3 slice to inspect its model and event."]
    [:box {:left 1
           :top 2
           :width 32
           :height 18
           :label "SLICES"
           :border "line"}
     (xt/x:arr-map substrate/DEMOS
                   (fn [entry index]
                     (return
                      [:% -/SliceButton
                       {:key (. entry ["id"])
                        :entry entry
                        :index index
                        :active (== demo-id (. entry ["id"]))
                        :onSelect set-demo-id}])))]
    [:box {:left 34
           :top 2
           :width 45
           :height 6
           :label "SELECTED DEMO"
           :border "line"}
     [:text {:left 1 :top 0}
      (. selected ["title"])]
     [:text {:left 1 :top 1 :width 41 :height 3}
      (. selected ["description"])]]
    [:box {:left 34
           :top 9
           :width 45
           :height 7
           :label "DATAVIEW MODEL"
           :border "line"
           :scrollable true
           :keys true
           :content (-/inspect-value model)}]
    [:box {:left 34
           :top 17
           :width 45
           :height 7
           :label "EXAMPLE EVENT"
           :border "line"
           :scrollable true
           :keys true
           :content (-/inspect-value event)}]
    [:text {:left 2 :top 20} "Mouse or focus + Enter to select. Press q to quit."]]))

(defn.js Screen
  []
  (var screen (b/screen {:autoPadding true
                         :smartCSR true
                         :title "TUI 004 - Substrate Scratch"}))
  (screen.key ["q" "C-c" "Esc"]
              (fn [] (. this (destroy))))
  (return screen))

(defrun.js __init__
  (do
    (:# (!:uuid))
    (client/client-ws "localhost" 28004 {})
    (b/renderBlessed [:% -/AppMain] (-/Screen))))
