(ns js.blessed.frame-console
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.lib.valtio :as v]
             [js.react :as r]
             [js.blessed :as b]
             [js.blessed.ui-style :as ui-style]
             [js.blessed.ui-group :as ui-group]
             [js.blessed.ui-core :as ui-core]]})

(defn.js ConsoleMain
  "creates a primary frame-console button"
  {:added "4.0"}
  [#{[show
      current
      (:= setCurrent (fn:>))
      (:= setHeight  (fn:>))
      (:= height 10)
      (:= setShow (fn:>))
      (:= screens [])
      (:.. rprops)]}]
  (var data (k/sort (k/obj-keys screens)))
  (var target (or (k/get-key screens current)
                  (k/get-key screens (k/first data))))
  (return [:box {:height height
                 :bg "black"}
           [:% ui-core/SmallButton
            {:content "X"
             :color "yellow"
             :onClick (fn:> (setShow false))}]
           [:box {:left 5
                  :bg "black"}
            [:% ui-group/Tabs
             {:data data
              :height 1
              :color "blue"
              :value current
              :setValue setCurrent
              :format (fn:> [s] (+ " " s " "))}]]
           [:box {:top 1
                  :bg "black"}
            (ui-group/displayTarget target)]
           [:box {:bg "black"
                  :width 3
                  :right 0
                  :bottom 0
                  :height 3}
            [:% ui-core/SmallButton
             {:top 0
              :content "+"
              :color "yellow"
              :onClick (fn:> (setHeight (j/min 40 (+ height 5))))}]
            [:% ui-core/SmallButton
             {:top 2
              :content "-"
              :color "yellow"
              :onClick (fn:> (setHeight (j/max 10 (- height 5))))}]]]))

(defn.js Console
  "creates a primary frame-console button"
  {:added "4.0"}
  [#{[show
      setShow
      current
      setCurrent
      height
      setHeight
      screens
      (:.. rprops)]}]
  (return
   (:? show [:box #{[height (:.. rprops)]}
             [:% -/ConsoleMain #{current height screens setCurrent setHeight setShow show}]])))

