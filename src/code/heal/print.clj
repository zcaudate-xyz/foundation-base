(ns code.heal.print
  (:require [std.lib :as h]))

(def +rainbow-colors+
  ["\u001b[34m"                        ; blue
   "\u001b[35m"                        ; magenta
   "\u001b[32m"                        ; green
   "\u001b[31m"                        ; red
   "\u001b[36m"                        ; cyan
   "\u001b[33m"                        ; yellow
   
])
  
(def +error-color+ "\u001b[41m") ; red background
(def +unpaired-color+ "\u001b[43m") ; red background

(def +reset-color+ "\u001b[0m")

(defn print-rainbow
  "Prints the code with delimiters colored by nesting depth."
  [content delimiters]
  (let [color-map (->> delimiters
                       (map (fn [delim]
                              (let [depth (get delim :depth 0)
                                    correct? (get delim :correct? true)
                                    color (cond correct?
                                                (get +rainbow-colors+ (mod depth (count +rainbow-colors+)))

                                                (get delim :pair-id)
                                                +error-color+

                                                :else
                                                +unpaired-color+)]
                                [[(:line delim) (:col delim)] color])))
                       (into {}))]
    (loop [chars (seq content)
           line-num 1
           col-num 1]
      (when (seq chars)
        (let [char (first chars)
              color (get color-map [line-num col-num])]
          (if color
            (h/pr (str color char +reset-color+))
            (h/pr char))
          (if (= char \newline)
            (recur (rest chars) (inc line-num) 1)
            (recur (rest chars) line-num (inc col-num))))))))

