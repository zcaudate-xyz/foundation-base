(ns std.block.heal.parse
  (:require [std.lib :as h]
            [std.string :as str])
  (:import [java.io StringReader]
           [clojure.lang LineNumberingPushbackReader]))

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

(defn find-offset [s end-line end-col]
  (loop [chars (seq s)
         l 1
         c 1
         idx 0]
    (cond (and (= l end-line) (= c end-col))
          idx

          (not (seq chars))
          idx ;; EOF reached

          :else
          (let [ch (first chars)]
             (if (= ch \newline)
               (recur (rest chars) (inc l) 1 (inc idx))
               (recur (rest chars) l (inc c) (inc idx)))))))

(defn skip-next-form-length [^String content idx]
  (let [start-skip-idx (+ idx 2)
        remaining-str (subs content start-skip-idx)
        pbr (LineNumberingPushbackReader. (StringReader. remaining-str))]
    (try
      (read pbr)
      (let [end-line (.getLineNumber pbr)
            end-col  (.getColumnNumber pbr)]
        (find-offset remaining-str end-line end-col))
      (catch Throwable _
        ;; On error (e.g. unmatched delimiter), use the position where the reader stopped
        (let [end-line (.getLineNumber pbr)
              end-col  (.getColumnNumber pbr)]
          (find-offset remaining-str end-line end-col))))))

(defn count-lines-cols [s start-line start-col]
  (loop [chars (seq s)
         l (long start-line)
         c (long start-col)]
    (if-not (seq chars)
      [l c]
      (let [ch (first chars)]
        (if (= ch \newline)
          (recur (rest chars) (inc l) 1)
          (recur (rest chars) l (inc c)))))))

(defn parse-delimiters
  "gets all the delimiters in the file"
  {:added "4.0"}
  [^String content]
  (let [len (count content)]
    (loop [idx (long 0)
           line-num (long 1)
           col-num (long 1)
           in-comment? false
           in-string? false
           escaped? false
           delimiters []]
      (if (>= idx len)
        delimiters
        (let [char (.charAt content idx)]
          (cond (= char \newline)
                (recur (inc idx) (inc line-num) 1 false in-string? false delimiters)

                ;; Previous character was an escape character
                escaped?
                (recur (inc idx) line-num (inc col-num) in-comment? in-string? false delimiters)

                ;; Current character is an escape character
                (= char \\)
                (recur (inc idx) line-num (inc col-num) in-comment? in-string? true delimiters)

                ;; Inside a single-line comment
                in-comment?
                (recur (inc idx) line-num (inc col-num) true in-string? false delimiters)

                ;; Inside a string
                in-string?
                (cond (= char \")
                      (recur (inc idx) line-num (inc col-num) false false false delimiters)

                      :else
                      (recur (inc idx) line-num (inc col-num) false true false delimiters))

                ;; Not in a comment or string
                :else
                (let [info (delimiter-info char)]
                  (cond (= char \;)
                        (recur (inc idx) line-num (inc col-num) true false false delimiters)

                        (= char \")
                        (recur (inc idx) line-num (inc col-num) false true false delimiters)

                        ;; Handle #_ reader discard
                        (and (= char \#)
                             (< (inc idx) len)
                             (= (.charAt content (inc idx)) \_))
                        (let [skipped-len (skip-next-form-length content idx)]
                          (if skipped-len
                            (let [start-skip-idx (+ idx 2)
                                  skipped-str (subs content start-skip-idx (+ start-skip-idx (long skipped-len)))
                                  [new-line new-col] (count-lines-cols skipped-str line-num (+ col-num 2))]
                              (recur (+ idx 2 (long skipped-len)) (long new-line) (long new-col) false false false delimiters))

                            ;; Fallback if skipped-len is nil (should not happen with new catch block)
                            (recur (inc idx) line-num (inc col-num) false false false delimiters)))

                        info
                        (recur (inc idx) line-num (inc col-num) false false false
                               (conj delimiters (merge {:char (str char)
                                                        :line line-num
                                                        :col col-num} info)))
                        :else
                        (recur (inc idx) line-num (inc col-num) false false false delimiters)))))))))

(defn pair-delimiters
  "pairs the delimiters and annotates whether it's erroring"
  {:added "4.0"}
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

(defn parse-lines-raw
  [lines]
  (loop [lines lines
         line-num 1
         in-multiline? false
         output []]
    (if (empty? lines)
      output
      (let [current-line (first lines)
            trimmed-line (str/trim-left current-line)
            indent (- (count current-line)
                      (count trimmed-line))
            char  (str (first trimmed-line))
            dinfo (if (#{"[" "{" "("}
                           char)
                    (h/rename-keys (delimiter-info (first char))
                                   {:type :action}))
            
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

            has-code  (and (not= char ";")
                           (or (= line-type :code)
                               (= line-type :commented)))
            
            last-idx  (let [r-trimmed (str/trim-right current-line)]
                        (if (not-empty r-trimmed)
                         (dec (count r-trimmed))))]
        (recur (rest lines)
               (inc  line-num)
               next-multiline?
               (conj output (cond-> {:type line-type :line line-num}
                              last-idx  (assoc :last-idx last-idx)
                              
                              has-code  (assoc :col (inc indent)
                                               :char char)
                              :then     (merge dinfo))))))))

(defn parse-lines
  "parse lines"
  {:added "4.0"}
  [content]
  (parse-lines-raw (str/split-lines content)))

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
