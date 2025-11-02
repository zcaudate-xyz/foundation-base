(ns code.heal.unclosed-test
  (:use code.test)
  (:require [code.heal.parse :as parse]
            [code.heal.unclosed :as unclosed]
            [std.lib :as h]))


^{:refer code.heal.unclosed/flag-indent-discrepancies-single :added "4.0"}
(fact "finds previous index discrepancies given indent layout"
  ^:hidden
  
  (unclosed/flag-indent-discrepancies-single
   [{:char "(", :line 2, :col 1, :type :open, :style :paren, :depth 0, :correct? false, :index 0}
    {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? false, :index 1}
    {:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :depth 2, :line 4, :col 4, :char "("}
    {:correct? true, :index 3, :pair-id 0, :type :open, :style :paren, :depth 3, :line 5, :col 2, :char "("}
    {:correct? true, :index 4, :pair-id 0, :type :close, :style :paren, :depth 3, :line 5, :col 7, :char ")"}
    {:correct? true, :index 5, :pair-id 1, :type :close, :style :paren, :depth 2, :line 5, :col 8, :char ")"}]
   {:correct? true, :index 3, :pair-id 0, :type :open, :style :paren, :depth 3, :line 5, :col 2, :char "("})
  => [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 4, :depth 2, :char "("}
      {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? false, :index 1}])



^{:refer code.heal.unclosed/flag-indent-discrepancies :added "4.0"}
(fact "finds all discrepancies given some code"
  ^:hidden

  (unclosed/flag-indent-discrepancies
   (parse/pair-delimiters
    (parse/parse-delimiters "
(defn
 (do
   (it)
 (this))")))
  => [[4 [{:correct? true, :index 1, :pair-id 2, :type :open, :style :paren, :line 3, :col 2, :depth 1, :char "("}]]]
  
  (unclosed/flag-indent-discrepancies
   (parse/pair-delimiters
    (parse/parse-delimiters "
(defn
 (do
   (it
 (this))")))
  => [[3 [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 4, :depth 2, :char "("}
          {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? false, :index 1}]]])





^{:refer code.heal.unclosed/find-depth-indent-discrepancies :added "4.0"}
(fact "Finds opening delimiters where the column is less than a previous opening delimiter at the same depth."
  ^:hidden
  
  ;; findging the open paren at (this ...) meaning that (do ...) is not correct
  
  (parse/pair-delimiters
   (parse/parse-delimiters "
(defn
 (do
   (it)
 (this))"))
  [{:char "(", :line 2, :col 1, :type :open, :style :paren, :depth 0, :correct? false, :index 0}
   {:correct? true, :index 1, :pair-id 2, :type :open, :style :paren, :depth 1, :line 3, :col 2, :char "("}
   {:correct? true, :index 2, :pair-id 0, :type :open, :style :paren, :depth 2, :line 4, :col 4, :char "("}
   {:correct? true, :index 3, :pair-id 0, :type :close, :style :paren, :depth 2, :line 4, :col 7, :char ")"}
   {:correct? true, :index 4, :pair-id 1, :type :open, :style :paren, :depth 2, :line 5, :col 2, :char "("}
   {:correct? true, :index 5, :pair-id 1, :type :close, :style :paren, :depth 2, :line 5, :col 7, :char ")"}
   {:correct? true, :index 6, :pair-id 2, :type :close, :style :paren, :depth 1, :line 5, :col 8, :char ")"}]
  
  

  ;; findging the open paren at (this ...) meaning that (do ...) is not correct
  
  (parse/pair-delimiters
   (parse/parse-delimiters "
(defn
 (do
   (it
 (this))"))
  [{:char "(", :line 2, :col 1, :type :open, :style :paren, :depth 0, :correct? false, :index 0}
   {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? false, :index 1}
   {:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :depth 2, :line 4, :col 4, :char "("}
   {:correct? true, :index 3, :pair-id 0, :type :open, :style :paren, :depth 3, :line 5, :col 2, :char "("}
   {:correct? true, :index 4, :pair-id 0, :type :close, :style :paren, :depth 3, :line 5, :col 7, :char ")"}
   {:correct? true, :index 5, :pair-id 1, :type :close, :style :paren, :depth 2, :line 5, :col 8, :char ")"}])

^{:refer code.heal.unclosed/find-depth-indent-parent :added "4.0"}
(fact "TODO")

^{:refer code.heal.unclosed/find-depth-indent-last-close :added "4.0"}
(fact "TODO")


(defn flag-depth-indent-single
  [delimiter {:keys [depth col index]}]
  (loop [index   (dec index)
         results []]
    (let [prev    (get delimiter index)
          [flagged end?]  (cond (nil? prev)                [nil true]
                                (= :close (:type prev))    []
                                (and (> depth  (:depth prev))
                                     (< col    (:col   prev))) [prev false]
                                (and (> depth  (:depth prev))
                                     (>= col   (:col   prev))) [prev true]
                                :else [])
          results (if flagged
                    (conj results flagged)
                    results)]
      (cond end? results
            
            
            :else (recur (dec index) results)))))



(defn flag-by-indent
  [delimiters])


=> [{:correct? true, :index 4, :pair-id 1, :type :open, :discrepancy? true, :style :paren, :depth 2, :line 5, :col 2, :char "("}]

