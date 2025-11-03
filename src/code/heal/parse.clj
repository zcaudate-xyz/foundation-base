(ns code.heal.parse
  (:use [std.lib :as h]
        [std.string :as str]))

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
    \( {:type :open, :style :paren}
    \) {:type :close, :style :paren}
    \[ {:type :open, :style :square}
    \] {:type :close, :style :square}
    \{ {:type :open, :style :curly}
    \} {:type :close, :style :curly}
    nil))

;; PARSE

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
        (cond (= char \newline)
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
              (cond (= char \")
                    (recur rest-chars line-num (inc col-num) false false false delimiters)

                    :else
                    (recur rest-chars line-num (inc col-num) false true false delimiters))

              ;; Not in a comment or string
              :else
              (let [info (delimiter-info char)]
                (cond (= char \;)
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

(defn pair-delimiters
  [delimiters]
  (let [{:keys [stack output depth]}
        (reduce (fn [{:keys [stack output depth pair-id index]
                      :as acc}
                     {:keys [type style] :as entry}]
                  (case type
                    :open (assoc acc
                                 :index   (inc index)
                                 :stack   (conj stack  (assoc entry
                                                              :depth depth
                                                              :index index))
                                 :depth   (inc depth))
                    (cond (empty? stack)
                          (assoc acc
                                 :output (conj output (assoc entry
                                                             :index index
                                                             :depth (dec depth)
                                                             :correct? false))
                                 :depth  (dec depth))
                          
                          :else
                          (let [open  (last stack)
                                pairs [:pair-id pair-id
                                       :correct? (= (:style open)
                                                    style)]]
                            {:stack   (pop stack)
                             :depth   (dec depth)
                             :index   (inc index)
                             :pair-id (inc pair-id)
                             :output  (conj output
                                            (apply assoc open pairs)
                                            (apply assoc entry
                                                   :depth (dec depth)
                                                   :index index
                                                   pairs))}))))
                {:stack []
                 :processed []
                 :index 0
                 :depth 0
                 :pair-id 0}
                delimiters)]
    (vec (sort-by (juxt :line :col)
                  (concat output
                          (map #(assoc % :correct? false)
                               stack))))))

(defn parse
  "creates a parse function"
  {:added "4.0"}
  [content]
  (pair-delimiters
   (parse-delimiters content)))

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

;;
;; Lines
;;

(defn count-unescaped-quotes
  "counting unescaped quotes in a line"
  {:added "4.0"}
  [s]
  (loop [chars (seq s)
         count 0
         escaped? false]
    (if-not (seq chars)
      count
      (let [char (first chars)
            rest-chars (rest chars)]
        (cond
          ;; If previous char was escape, current char is escaped, so reset escaped?
          escaped?
          (recur rest-chars count false)

          ;; Current char is escape, set flag for next char
          (= char \\)
          (recur rest-chars count true)

          ;; Current char is a quote and not escaped, increment count
          (= char \")
          (recur rest-chars (inc count) false)

          ;; Any other character, reset escaped?
          :else
          (recur rest-chars count false))))))

(defn parse-lines
  "parse lines"
  {:added "4.0"}
  [content]
  (loop [lines (str/split-lines content)
         line-num 1
         in-multiline? false
         output []]
    (if (empty? lines)
      output
      (let [current-line (first lines)
            trimmed-line (str/trim-left current-line)
            
            
            ;; Determine if a string starts or ends on this line
            quote-count (count-unescaped-quotes current-line)
            
            next-multiline? (cond (and (zero? quote-count) in-multiline?) true
                                  (and (odd? quote-count)  in-multiline?) false
                                  (odd? quote-count) true
                                  :else false)
            
            line-type (cond next-multiline? :string
                            (str/blank? current-line) :blank
                            (str/includes? current-line ";") :commented
                            :else :code)
            
            last-idx (let [r-trimmed (str/trim-right current-line)]
                       (if (not-empty r-trimmed)
                         (dec (count r-trimmed))))]
        (recur (rest lines)
               (inc  line-num)
               next-multiline?
               (conj output (cond-> {:type line-type :line line-num}
                              last-idx (assoc :last-idx last-idx))))))))

;; Predicates

(defn is-open-heavy
  "checks if open delimiters dominate"
  {:added "4.0"}
  [delimiters]
  (boolean
   (some (fn [{:keys [type pair-id]}]
           (and (= type :open)
                (nil? pair-id)))
         delimiters)))

(defn is-balanced
  "checks if parens are balanced"
  {:added "4.0"}
  [delimiters]
  (every? :pair-id delimiters))

(defn is-readable
  "checks if parens are readable"
  {:added "4.0"}
  [delimiters]
  (every? :correct? delimiters))

(defn is-close-heavy
  "checks if parens are close heavy"
  {:added "4.0"}
  [delimiters]
  (boolean
   (some (fn [{:keys [type pair-id]}]
           (and (= type :close)
                (nil? pair-id)))
         (clojure.core/reverse delimiters))))


;;
;; indentation
;;

(defn make-delimiter-line-lu
  "creates a line lu"
  {:added "4.0"}
  [delimiters]
  (group-by :line delimiters))

(defn make-delimiter-index-lu
  "creates the index lookup"
  {:added "4.0"}
  [delimiters]
  (h/map-juxt [:index
               identity]
              delimiters))
