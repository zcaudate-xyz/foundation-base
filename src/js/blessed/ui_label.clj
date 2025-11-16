(ns js.blessed.ui-label
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react :as r]
             [js.lib.chalk :as chalk]]})

(defn.js ToggleLabel
  "toggle label `red`/`green`"
  {:added "4.0"}
  [#{active
     onClick
     label
     content
     width}]
  (return
   [:box {:width (or width 33)}
    [:button
     {:left 0
      :content (+ (:? active
                      (chalk/green "\u25CF ")
                      (chalk/red "\u25CF "))
                  label)
      :color "white"
      :style {:bold true}
      :shrink true
      :mouse true
      :on-click onClick}]
    [:button
     {:right 2
      :style {:fg    (:? active "green" "red")
              :bold  active}
      :shrink true
      :mouse true
      :on-click onClick
      :content (or content "")}]]))

(defn.js ActionLabel
  "action label with color"
  {:added "4.0"}
  [#{onClick
     label
     content
     width
     color}]
  (:= color (or color "blue"))
  (var no-color (or (== color "none")
                    
                    (k/nil? (. chalk [color]))))
  (var dot (:? no-color
               "  "
               ((. chalk [color]) "\u25CF ")))
  (return
   [:box {:width (or width 33)}
    [:button
     {:left 0
      :content (+ dot
                  label)
      :color "white"
      :style {:bold true}
      :shrink true
      :mouse true
      :on-click onClick}]
    [:button
     {:right 2
      :style {:fg    (:? no-color
                         "white"
                         color)
              :bold  true}
      :shrink true
      :mouse true
      :on-click onClick
      :content (or content "")}]]))

(defn.js EntryLabel
  "entry label for records"
  {:added "4.0"}
  [#{entry
     columns
     padding}]
  (when (k/is-empty? entry)
    (return [:box {:content ""}]))
  
  (var [start end] (or padding [13 18]))
  (var content
       (-> columns
           (k/arr-map 
            (fn [[label key f]]
              (:= f (or f k/identity))
              (var val (k/get-key entry key))
              (var output (f val entry))
              (return (+ (chalk/bold (j/padEnd (+ label "") start))
                         (j/padStart (+ "" (or output "-")) end)))))
           (k/arr-join "\n")))
  (return
   [:box {:content content}]))
