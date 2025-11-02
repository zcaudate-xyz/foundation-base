(ns code.heal.parse
  (:use [std.lib :as h]))

(def lu-close
  {"(" ")"
   "[" "]"
   "{" "}"})

(def lu-open
  {")" "(" 
   "]" "[" 
   "}" "{"})

(defn- delimiter-info [char]
  (case char
    \( {:type :open, :style :paren }
    \) {:type :close, :style :paren}
    \[ {:type :open, :style :square}
    \] {:type :close, :style :square}
    \{ {:type :open, :style :curly}
    \} {:type :close, :style :curly}
    nil))

(defn parse-delimiters
  "gets all the delimiters in the file"
  {:added "4.0"}
  [content]
  (loop [chars (seq content)
         line-num 1
         col-num 1
         in-comment? false
         in-string? false
         escaped? false
         delimiters []]
    (if-not (seq chars)
      delimiters
      (let [char (first chars)
            rest-chars (rest chars)]
        (cond
          ;; Newline character
          (= char \newline)
          (recur rest-chars (inc line-num) 1 false in-string? false delimiters)

          ;; Previous character was an escape character
          escaped?
          (recur rest-chars line-num (inc col-num) in-comment? in-string? false delimiters)

          ;; Current character is an escape character
          (= char \\)
          (recur rest-chars line-num (inc col-num) in-comment? in-string? true delimiters)

          ;; Inside a single-line comment
          in-comment?
          (recur rest-chars line-num (inc col-num) true in-string? false delimiters)

          ;; Inside a string
          in-string?
          (cond
            (= char \")
            (recur rest-chars line-num (inc col-num) false false false delimiters)

            :else
            (recur rest-chars line-num (inc col-num) false true false delimiters))

          ;; Not in a comment or string
          :else
          (let [info (delimiter-info char)]
            (cond
              (= char \;)
              (recur rest-chars line-num (inc col-num) true false false delimiters)

              (= char \")
              (recur rest-chars line-num (inc col-num) false true false delimiters)

              info
              (recur rest-chars line-num (inc col-num) false false false
                     (conj delimiters (merge {:char (str char)
                                              :line line-num
                                              :col col-num} info)))
              :else
              (recur rest-chars line-num (inc col-num) false false false delimiters))))))))

(defn print-delimiters
  "prints all the parsed carets"
  {:added "4.0"}
  [content delimiters & [{:keys [line-numbers print-blank-carets]
                          :or {pad-width 4
                               print-blank-carets true}}]]
  (let [lines (clojure.string/split-lines content)
        delimiters-by-line (group-by :line delimiters)]
    (doseq [[line-idx line-content] (map-indexed vector lines)]
      (let [line-num (inc line-idx)
            line-prefix (if line-numbers
                          (format "%4d " line-num)
                          "")]
        (h/p (str line-prefix line-content))
        (let [line-delimiters (get delimiters-by-line line-num)]
          (when (or line-delimiters print-blank-carets)
            (let [caret-chars (char-array (count line-content) \space)]
              (when line-delimiters
                (doseq [delim line-delimiters]
                  (when (>= (dec (:col delim)) 0)
                    (aset caret-chars (dec (:col delim)) \^))))
              (h/p (str (apply str (repeat (count line-prefix) " "))
                        (String. caret-chars))))))))))

(defn pair-delimiters
  "pairs the delimiters and annotates whether it's erroring"
  {:added "4.0"}
  [delimiters]
  (let [initial-state {:stack []
                       :processed []
                       :pair-id 0
                       :unmatched-close-count 0}
        final-state (reduce (fn [state delim]
                              (let [{:keys [stack processed pair-id unmatched-close-count]} state]
                                (if (= (:type delim) :open)
                                  ;; Handle open delimiter
                                  (let [depth (count stack)
                                        new-delim (assoc delim :depth depth)]
                                    (assoc state
                                           :stack (conj stack new-delim)))
                                  ;; Handle close delimiter
                                  (if-let [open-delim (peek stack)]
                                    ;; Stack not empty, try to match
                                    (let [depth (:depth open-delim)
                                          correct? (= (:style open-delim) (:style delim))
                                          updated-open (assoc open-delim :correct? correct? :pair-id pair-id)
                                          updated-close (assoc delim :depth depth :correct? correct? :pair-id pair-id)]
                                      (assoc state
                                             :stack (pop stack)
                                             :processed (conj processed updated-open updated-close)
                                             :pair-id (inc pair-id)))
                                    ;; Stack empty, unmatched close delimiter
                                    (let [new-unmatched-close-count (inc unmatched-close-count)
                                          new-delim (assoc delim :depth (- new-unmatched-close-count) :correct? false)]
                                      (assoc state
                                             :processed (conj processed new-delim)
                                             :unmatched-close-count new-unmatched-close-count))))))
                            initial-state
                            delimiters)
        {:keys [stack processed]} final-state
        unmatched (map #(assoc % :correct? false) stack)]
    (->> (concat processed unmatched)
         (sort-by (juxt :line :col))
         (map-indexed (fn [i entry]
                        (assoc entry :index i)))
         (vec))))

;;
;; indentation
;;

(defn make-delimiter-line-lu
  {:added "4.0"}
  [delimiters]
  (group-by :line delimiters))

(defn make-delimiter-index-lu
  "creates a delimiter lookup"
  {:added "4.0"}
  [delimiters]
  (h/map-juxt [:index
               identity]
              delimiters))
