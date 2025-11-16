(ns play.tui-002-game-of-life.main
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]
             [js.react :as r]
             [js.blessed :as b]]
   :static {:export false}})

(def.js ROWS 32)

(def.js COLS 32)

(def.js INTERVAL 100)

(def.js PARAMS
  {:born {:min 3 :max 3}
   :live {:min 2 :max 3}})

(defn.js gridNew
  ([rows cols]
   (var grid (new Array rows))
   (var y nil)
   (forange [y rows]
      (:= (. grid [y])
          (new Array cols)))
   (return grid)))

(defn.js gridSeed
  ([grid rows cols]
   (forange [y rows]
     (forange [x cols]
       (:= (. grid [y] [x])
           (j/round (* 0.8 (j/random))))))))

(defn.js gridCreate
  ([rows cols]
   (var grid (-/gridNew rows cols))
   (-/gridSeed grid rows cols)
   (return grid)))

(defn.js gridCount
  ([grid y x rows cols]
   (var sum 0)
   (forange [v [-1 2]]
     (forange [h [-1 2]]
       (var yi (mod (+ y v rows) rows))
       (var xi (mod (+ x h cols) cols))
       (:+= sum (. grid [yi] [xi]))))
   (:-= sum (. grid [y] [x]))
   (return sum)))

(defn.js gridNext
  ([grid rows cols]
   (var next (-/gridNew rows cols))
   (forange [y rows]
     (forange [x cols]
       (var curr  (. grid [y] [x]))
       (var near  (-/gridCount grid y x rows cols))
       (cond (and (=== curr 0)
                  (=== near 3))
             (:= (. next [y] [x]) 1)
             
             (and (=== curr 1)
                  (or (< near 2)
                      (> near 3)))
             (:= (. next [y] [x]) 0)
             
             :else
             (:= (. next [y] [x]) curr))))
   (return next)))
  
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

(defn.js TimeControl
  ([props]
   (return
    [:box {:shrink true}
     ;; START
     [:<Button> {:left 1
                 :text  "START"
                 :color "green"
                 :disabled (not props.state.paused)
                 :action props.fn.start}]
     ;; STOP
     [:<Button> {:left  10
                 :text  "STOP"
                 :color "red"
                 :disabled props.state.paused
                 :action props.fn.stop}]
     ;; NEXT
     [:<Button> {:left  21
                 :text  "NEXT"
                 :color "blue"
                 :disabled (not props.state.paused)
                 :action props.fn.next}]
     ;; RESET
     [:<Button> {:left  54
                 :text  "RESET"
                 :color "grey"
                 :action props.fn.reset}]])))

(defn.js initialState
  ([rows cols]
   (return {:rows    rows
            :cols    cols
            :grid    (-/gridCreate rows cols)
            :paused  true
            :counter 0})))

(defn.js GridView
  ([props]
   (var #{grid rows cols} props.state)
   (return
    [:box {:label " Grid "
           :width (+ 2 (* 2 rows))
           :height (+ 2 cols)
           :border "line"}
     (j/map grid
            (fn [row i]
              (return
               (j/map row
                      (fn [col j]
                        (return
                         [:box {:top i
                                :width 2
                                :left (* 2 j)
                                :key (+ i "_" j)
                                :content ""
                                :style {:bg (:? (== 1 col) ["yellow" "black"])}
                                :shrink true}]))))))])))

(defn.js App
  ([]
   (var [state setState]  (r/local (-/initialState -/ROWS -/COLS)))
   (var next-fn
        (fn []
          (let [#{grid rows cols counter} state]
            (setState #{...state
                        {:counter (+ counter 1)
                         :grid (-/gridNext grid rows cols)}}))))
   (var actions {:reset (fn []
                           (let [#{grid rows cols} state]
                             (setState #{...state
                                         {:grid (-/gridCreate rows cols)}})))
                  :next  next-fn
                  :start (fn []
                           (let [#{paused} state]
                             (setState #{...state, {:paused (not paused)}})))
                  :stop  (fn []
                           (let [#{paused} state]
                             (setState #{...state, {:paused (not paused)}})))})
   (r/useInterval
    (fn []
      (if (not state.paused)
        (next-fn)))
    -/INTERVAL)
   (return
    [:box {:left 2
           :top 1
           :shrink true}
     [:box {:shrink true}
      [:% -/TimeControl {:state state
                       :fn actions}]]
     [:box {:top 4
            :shrink true}
      [:% -/GridView {:state state
                    :fn actions}]]])))

(defn.js Screen
  ([]
   (var screen (b/screen
                {:autoPadding true
                 :smartCSR true
                 :title "Tui 002 - Game of Life"}))
   (screen.key ["q" "C-c" "Esc"]
               (fn [] (. this (destroy))))
   (return screen)))

(defrun.js __main__
  (b/renderBlessed [:% -/App] (-/Screen)))



(comment
  (l/rt:module-meta
   :js))
