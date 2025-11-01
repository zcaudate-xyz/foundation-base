(ns code.heal.core
  (:require [code.heal.parse :as parse]
            [std.lib :as h]
            [std.string :as str]))

(defn update-content
  "performs the necessary edits to a string"
  {:added "4.0"}
  [content edits]
  (let [lines (str/split-lines content)]
    (->> edits
         (reduce (fn [lines edit]
                   (let [{:keys [action new-char]} edit
                         old-line  (nth lines
                                        (dec (:line edit)))
                         new-line  (case action
                                     :replace
                                     (str/replace-at old-line
                                                     (dec (:col edit))
                                                     new-char)
                                     :insert
                                     (str/insert-at old-line
                                                    (dec (:col edit))
                                                    new-char))]
                     (assoc lines (dec (:line edit)) new-line)))
                 lines)
         (str/join "\n"))))
 
(defn create-mismatch-edits
  "find the actions required to edit the content"
  {:added "4.0"}
  [delimiters]
  (let [pairs (->> delimiters
                   (filter :pair-id)
                   (group-by :pair-id))
        mismatched-pairs (filter (fn [[_ pair]]
                                   (not (:correct? (first pair))))
                                 pairs)]
    (for [[_ pair] mismatched-pairs
          :let [open (first (filter #(= :open (:type %)) pair))
                close (first (filter #(= :close (:type %)) pair))]]
      {:action :replace
       :line (:line close)
       :col (:col close)
       :new-char (case (:char open)
                   "(" ")"
                   "[" "]"
                   "{" "}")})))

(defn heal-mismatch
  "heals a style mismatch for paired delimiters"
  {:added "4.0"}
  [content]
  (let [delimiters (parse/pair-delimiters
                    (parse/parse-delimiters
                     content))
        edits  (create-mismatch-edits delimiters)]
    (update-content content edits)))

(defn create-unclosed-edits
  "gets the edits for healing unclosed forms"
  {:added "4.0"}
  [delimiters]
  )


(defn heal-unclosed
  "heals unclosed forms"
  {:added "4.0"}
  [content]
  (let [delimiters (parse/pair-delimiters
                    (parse/parse-delimiters
                     content))
        edits  (create-unclosed-edits delimiters)]
    (update-content content edits)))

(comment

  (let [delimiters (parse/pair-delimiters (parse/parse-delimiters content))
        lines      (vec (parse/parse-lines content))
        unmatched-open (filter #(and (= (:type %) :open)
                                     (not (:correct? %))) delimiters)]
    (h/prn unmatched-open)
    (reduce (fn [changes open-delim]
              (let [start-line-num  (:line open-delim)
                    start-line-info (get lines (dec start-line-num))
                    start-indent    (:indent start-line-info)
                    de-indent-line-info (heal-unclosed-find-line lines start-line-num start-indent)]
                (h/prn [start-line-num
                        start-line-info
                        start-indent
                        de-indent-line-info])
                (if de-indent-line-info
                  (let [insertion-line-num (dec (:line-num de-indent-line-info))
                        insertion-line-info (get lines (dec insertion-line-num))
                        insertion-col (count (:content insertion-line-info))]
                    (conj changes {:action :insert
                                   :line insertion-line-num
                                   :col (inc insertion-col)
                                   :new-char (get-closing-char (:char open-delim))}))
                  changes)))
            []
            unmatched-open)))
