^{:no-test true}
(ns scratch
  (:require [std.lang :as l]
            [clojure.set :as set]
            [std.lib :as h]))

(l/script+ [:live :js]
           {:runtime :basic})

(l/script :lua    {:runtime :basic})
(l/script :python {:runtime :basic})
(l/script :js     {:runtime :basic})

(require '[js.core :as j])


[(!.lua
  ((fn []
     (return ((fn [] (return 2)))))))


 (!.js
  ((fn []
     (return ((fn [] (return 2)))))))

 (!.py
  ((fn []
     (return ((fn [] (return 2)))))))]

(!.js
 ((fn []
    (var a 1)
    (var b 2)
    (return
     (+ a b 3)))))


(defn.js add-10
  [x]
  (return (+ x 10)))

(comment

  @(h/sh {:print true
          :args ["python", "-c", "import os; print(os.getenv('STATEMENT', 'Undefined'))"]
          :env {"STATEMENT" "Hello World!!"}})
  => "Hello World!!")


(do

  (l/script :python
            {:require [[xt.lang.base-lib :as k]]})

  (l/script :python
            {:runtime :basic
             :config {:program :conda
                      :params  {:venv "dummy"}
                      :shell   {:env {"HELLO" "1"
                                      "WORLD" "2"}
                                :print true}}
             :require [[xt.lang.base-lib :as k]]}))


(!.py
 ((fn []
    ((fn [] 1)))))

(!.py
 (:- import os)
 (os.getenv "HELLO")
 (os.getenv "WORLD"))

(defn.py add-20
  [y]
  (return (+ y 20)))


;;;
;;; TIC TAC TOE
;;;
;;; [[AA AB AC]]
;;; [[BA BB BC]]
;;; [[CA CB CC]]
;;;

(defn new-game
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
  [board]
  (boolean
   (some (fn [c]
           (set/subset? c board))
         +winning-conditions+)))

(defn next-move
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








(comment


  (check-win #{:aa :ab :cc})

  (add-10 10)
  (add-20 10))

;;;
;;; MULTI-LANGUAGE EXAMPLES
;;;

;; JavaScript Examples
(l/script :js
          {:runtime :basic})

(defn.js calculate-fibonacci
  [n]
  (if (<= n 1)
    (return n)
    (return (+ (-/calculate-fibonacci (- n 1))
               (-/calculate-fibonacci (- n 2))))))

(!.js
 (x:cat "Fibonacci of 10: " (-/calculate-fibonacci 10)))

;; Python Examples
(l/script :python
          {:runtime :basic
           :require [[xt.lang.base-lib :as k]]})


(!.py
 (+ 1 2 3))

(!.py
 
(def hello
  (fn [x] 
  (return x))))

(defn.py process-list
  [items]
  (:- import json)
  (let [result []]
    (k/for:array [item items]
                 (. result (append (* item 2))))
    (return (json.dumps result))))

(!.py
 (:- import json)
 (def data [1 2 3 4 5])
 (print "Processed data:" (process-list data)))

;; Lua Examples
(l/script :lua
          {:runtime :basic})

(defn.lua merge-tables
  [t1 t2]
  (let [result {}]
    (for [[k v] (pairs t1)]
      (set (get result k) v))
    (for [[k v] (pairs t2)]
      (set (get result k) v))
    (return result)))

(!.lua
 (let [table1 {:a 1 :b 2}
       table2 {:c 3 :d 4}]
   (print "Merged tables:"
          (merge-tables table1 table2))))

;; Cross-language Function Composition
(defn.js format-data
  [data]
  (return (.stringify js/JSON data)))

(defn.py analyze-data
  [json-str]
  (:- import json)
  (let [data (json.loads json-str)
        total (sum data)]
    (return (str "Sum: " total))))

(!.js
 (let [data [1 2 3 4 5]]
   (console.log "JavaScript data:" (format-data data))))

(!.py
 (let [json-data "[1, 2, 3, 4, 5]"]
   (print (analyze-data json-data))))

;; Example of using shared state
(l/script+ [:live :js]
           {:runtime :basic
            :require [[xt.lang.base-lib :as k]]})

(def shared-counter (atom 0))

(defn.js increment-counter
  []
  (swap! shared-counter inc)
  (return @shared-counter))

(defn.py get-counter
  []
  (return @shared-counter))

(comment
  ;; Test the multi-language examples
  (!.js (increment-counter))
  (!.py (get-counter))

  ;; Test the Fibonacci function
  (!.js (calculate-fibonacci 10))

  ;; Test the Python list processing
  (!.py (process-list [1 2 3 4 5]))

  ;; Test the Lua table merging
  (!.lua (merge-tables {:a 1} {:b 2})))
