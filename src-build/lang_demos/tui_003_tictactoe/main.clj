(ns lang-demos.tui-003-tictactoe.main
  (:require [lang.core :as  l]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/tui-003-tictactoe
            :port 28003
            :emit {:lang/jsx false}}
   :require [[js.react :as r]
             [js.blessed :as b]
             [js.module :as jm]
             [js.react.ext-box :as ext-box]
             [js.react.helper-data :as helper]
             [xt.event.base-box :as base-box]
             [xt.lang.common-client :as client]]})

(defrun.js __missing__
  (jm/import-missing)
  (jm/import-set-global))

(def.js WIN-LINES
  [[0 1 2] [3 4 5] [6 7 8]
   [0 3 6] [1 4 7] [2 5 8]
   [0 4 8] [2 4 6]])

(defn.js initialState
  ([]
   (return {:board ["" "" "" "" "" "" "" "" ""]
            :turn "X"
            :winner nil
            :draw false})))

(defn.js winnerFor
  ([board]
   (var winner nil)
   (forange [i 8]
     (var line (. -/WIN-LINES [i]))
     (var mark (. board [(. line [0])]))
     (if (and (not (=== mark ""))
              (=== mark (. board [(. line [1])]))
              (=== mark (. board [(. line [2])])))
       (:= winner mark)))
   (return winner)))

(defn.js boardFull
  ([board]
   (var full true)
   (forange [i 9]
     (if (=== "" (. board [i]))
       (:= full false)))
   (return full)))

(defn.js playMove
  ([state index]
   (if (or (< index 0) (>= index 9))
     (return state))
   (var cell (. state.board [index]))
   (if (or state.winner
           state.draw
           (not (=== cell "")))
     (return state))
   (var board (. state.board (slice)))
   (:= (. board [index]) state.turn)
   (var winner (-/winnerFor board))
   (var draw (and (not winner)
                  (-/boardFull board)))
   (return {:board board
            :turn (:? (=== state.turn "X") "O" "X")
            :winner winner
            :draw draw})))

(defn.js Button
  [#{left top width height text disabled color action}]
  (return
   [:button {:left (or left 0)
             :top  (or top 0)
             :width (or width 9)
             :height (or height 3)
             :content text
             :disabled disabled
             :shrink true
             :mouse true
             :onPress (fn [] (if (and action
                                      (not disabled))
                               (action)))
             :padding {:top 0 :right 1 :bottom 0 :left 1}
             :style {:bg (:? (not disabled) color "black")
                     :fg (:? (=== text "X")
                             "green"
                             (:? (=== text "O")
                                 "magenta"
                                 (:? disabled "gray" "white")))
                     :focus {:bold true}}}]))

(defn.js BoardView
  ([props]
   (var state props.state)
   (return
    [:box {:left 2
           :top 4
           :width 34
           :height 15
           :label " Board "
           :border "line"
           :style {:border {:fg "gray"}}}
     (. state.board
        (map (fn [mark index]
               (return
                [:% -/Button {:key index
                              :left (+ 1 (* (mod index 3) 10))
                              :top (+ 1 (* (Math.floor (/ index 3)) 4))
                              :width 9
                              :height 3
                              :text mark
                              :disabled (or (not (=== mark ""))
                                            (not (=== state.winner nil))
                                            state.draw)
                              :color "blue"
                              :action (fn [] (props.onPlay index))}]))))])))

(defn.js AppMain
  ([]
   (var [state setState] (r/local (-/initialState)))
   (var play (fn [index]
               (setState (-/playMove state index))))
   (var status (:? state.winner
                    (+ "Winner: " state.winner)
                    (:? state.draw
                        "Draw game"
                        (+ state.turn " to move"))))
   (return
    [:box {:label "Tui 003 - Tictactoe"
           :border "line"
           :width 42
           :height 26
           :style {:border {:fg "green"}}}
     [:text {:top 2 :left 3} status]
     [:% -/BoardView {:state state :onPlay play}]
     [:% -/Button {:left 2
                   :top 21
                   :width 14
                   :height 3
                   :text "NEW GAME"
                   :color "green"
                   :action (fn [] (setState (-/initialState)))}]
     [:text {:left 18 :top 22} "Choose an open square"]])))

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
