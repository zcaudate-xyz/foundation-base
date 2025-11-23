(ns demo.tictactoe
  (:require [clojure.set :as set]))

;;;
;;; TIC TAC TOE
;;;
;;; [[AA AB AC]]
;;; [[BA BB BC]]
;;; [[CA CB CC]]
;;;

(defn new-game
  "creates a new game"
  {:added "4.0"}
  []
  {:board {:bg #{:aa :ab :ac
                 :ba :bb :bc
                 :ca :cb :cc}
           :p1 #{}
           :p2 #{}}
   :turn   :p1
   :status :active
   :winner nil})

(def +winning-conditions+
  [#{:aa :ab :ac}
   #{:ba :bb :bc}
   #{:ca :cb :cc}

   #{:aa :ba :ca}
   #{:ab :bb :cb}
   #{:ac :bc :cc}

   #{:aa :bb :cc}
   #{:ac :bb :ca}])

(defn check-win
  "checks if pieces are in the winning position"
  {:added "4.0"}
  [board]
  (boolean
   (some (fn [c]
           (set/subset? c board))
         +winning-conditions+)))

(defn next-move
  "transitions from one state to the next"
  {:added "4.0"}
  [game move]
  (let [[side pos] move
        {:keys [board turn status]} game
        _ (when (#{:finished} status)
            (throw (ex-info "Game has finished." {:game game :move move})))
        _ (when (not= turn side)
            (throw (ex-info (str "Not " side "'s turn") {:game game :move move})))
        {:keys [bg p1 p2]} board
        _ (when (not (contains? bg pos))
            (throw (ex-info "Position already taken." {:game game :move move})))

        ;; Update board
        new-board (-> board
                      (update :bg disj pos)
                      (update side conj pos))
        
        ;; Check for winner 
        is-winner (check-win (get new-board side))

        ;; Check for full
        is-full   (empty? (:bg new-board))]
    {:board new-board
     :turn   (if (= side :p1) :p2 :p1)
     :status (if (or is-winner
                     is-full)
               :done
               :active)
     :winner (cond is-winner
                   side

                   is-full
                   :draw)}))

