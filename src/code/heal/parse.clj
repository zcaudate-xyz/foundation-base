(ns code.heal.parse
  (:use [std.lib :as h]))

(defn- delimiter-info [char]
  (case char
    \( {:type :open, :style :paren}
    \) {:type :close, :style :paren}
    \[ {:type :open, :style :square}
    \] {:type :close, :style :square}
    \{ {:type :open, :style :curly}
    \} {:type :close, :style :curly}
    nil))

(defn catalog-delimiters-in-string
  "Reads a Clojure string and catalogs the delimiters, tracking their line and column.
   Accounts for single-line comments (;) and strings."
  [content]
  (loop [line-num 1
         col-num 1
         in-comment? false
         lines (clojure.string/split-lines content)
         delimiters []]
    (if (empty? lines)
      delimiters
      (let [line (first lines)
            [new-delimiters new-col-num]
            (loop [col 1
                   in-comment-line? false
                   in-string? false
                   escaped? false
                   line-chars (seq line)
                   line-delimiters []]
              (if (empty? line-chars)
                [line-delimiters col]
                (let [char (first line-chars)]
                  (cond
                    ;; 1. Inside a comment, do nothing until end of line
                    in-comment-line?
                    (recur (inc col) true false false (rest line-chars) line-delimiters)

                    ;; 2. Inside a string
                    in-string?
                    (cond escaped?  ; previous char was '\'
                          (recur (inc col) false true false (rest line-chars) line-delimiters)
                          
                          (= char \\) ; current char is '\'
                          (recur (inc col) false true true (rest line-chars) line-delimiters)
                          
                          (= char \") ; end of string
                          (recur (inc col) false false false (rest line-chars) line-delimiters)
                          
                          :else ; just a regular char in a string
                          (recur (inc col) false true false (rest line-chars) line-delimiters))

                    ;; 3. Not in a comment or string
                    :else
                    (let [info (delimiter-info char)]
                      (cond (= char \;) ; start of comment
                            (recur (inc col) true false false (rest line-chars) line-delimiters)
                            
                            (= char \") ; start of string
                            (recur (inc col) false true false (rest line-chars) line-delimiters)
                            
                            info ; a delimiter!
                            (recur (inc col) false false false (rest line-chars)
                                   (conj line-delimiters (merge {:char char
                                                                 :line line-num
                                                                 :col col} info)))
                            :else ; some other char
                            (recur (inc col) false false false (rest line-chars) line-delimiters)))))))]
        (recur (inc line-num)
               1
               false
               (rest lines)
               (into delimiters new-delimiters))))))

(defn print-delimiters
  "Prints the code with carets highlighting the positions of delimiters."
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
