(ns js.blessed.frame-sidemenu
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react   :as r]
             [js.blessed :as b]
             [js.blessed.ui-style :as ui-style]
             [js.blessed.ui-group :as ui-group]
             [js.blessed.ui-core :as ui-core]
             [js.lib.chalk :as chalk]]})

(defn.js SideButton
  "creates a primary frame-sidebutton button"
  {:added "4.0"}
  ([#{[label
       index
       setIndex
       selected
       noIndex
       refLink
       (:.. rprops)]}]
   (let [colorFn (:? selected chalk/yellow chalk/bold)
         content (colorFn 
                  (+ "" (:? noIndex
                            ""
                            (:? selected
                                (chalk/inverse (+ " " index " "))
                                (+ " " index " ")))
                     " " label))]
     (return
      [:button #{[:ref refLink
                  :width 26
                  ;; :right 0
                  :mouse  true
                  :keys   true
                  :shrink true
                  :border  {}
                  :content content
                  :style   {:bg "black"
                            :border {:fg "black"
                                     :bg "black"}}
                  :onClick (fn [] (setIndex index))
                  (:.. rprops)]}]))))

(defn.js SideMenu
  "creates a primary frame-sidemenu button"
  {:added "4.0"}
  ([#{[items
       label
       width
       (:= index 1)
       noIndex
       setIndex
       menuContent
       menuFooter]}]
   (let [_ (:= items (:? (j/isArray items) items (j/keys items)))
         entries (j/map items
                         (fn [e i]
                           (return (j/assign
                                    {:top   (+ 2 (* i 2))
                                     :index (+ i 1)}
                                    e))))
         box (r/ref nil)
         MenuContent menuContent
         MenuFooter menuFooter]
     (r/run []
       (when (not noIndex)
         (. (r/curr box)
            (onScreenEvent "keypress"
                           (fn [_ key]
                             (let [i   (j/parseInt key.full)
                                   sel (j/filter entries
                                                  (fn:> [e] (== e.index i)))]
                               
                               (when (and sel (< 0 (k/len sel)))
                                 (setIndex i))))))
         (return (fn []
                   (. (r/curr box) (free))))))
     (return
      [:box {:ref box
             :top  0
             :width (or width 26)
             :height "100%"
             ;; :right 0
             :shrink true
             :scrollable true
             :style {:bold true
                     :bg "black"}}
       (j/map entries (fn [e]
                         (return
                          [:% -/SideButton
                           #{[:key e.index
                              setIndex
                              noIndex
                              :selected (== index e.index)
                              (:.. e)]}])))
       [:box {:style {:bold true
                      :bg "black"
                      :fg "white"}
              :align "left"
              :top 0
              :height 1
              :shrink true
              :width "100%"
              :content (chalk/inverse (chalk/yellow (+ " " (j/toUpperCase label) " ")))}]
       (:? MenuContent
           [:box {:left 1
                  :right 1
                  :style {:bg "black"}
                  :top (+ 4 (* 2 (k/len items)))}
            [:% MenuContent]])
       (:? MenuFooter
           [:box
            {:bottom 0,
             :height 2,
             :right 0,
             :style {:bg "black"}}
            [:% MenuFooter]])]))))
