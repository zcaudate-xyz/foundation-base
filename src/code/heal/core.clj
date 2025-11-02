(ns code.heal.core
  (:require [code.heal.parse :as parse]
            [code.heal.indent :as indent]
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
                                                    new-char)

                                     :remove
                                     (str/replace-at old-line
                                                     (dec (:col edit))
                                                     ""))]
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
       :new-char (or (parse/lu-close (:char open))
                     "")})))

(defn heal-mismatch
  "heals a style mismatch for paired delimiters"
  {:added "4.0"}
  [content]
  (let [delimiters (parse/pair-delimiters
                    (parse/parse-delimiters
                     content))
        edits  (create-mismatch-edits delimiters)]
    (update-content content edits)))


;;
;; Balance for Top Heavy code
;;

(defn check-append-fn
  [{:keys [type pair-id]}]
  (and (= type :open)
       (not pair-id)))

(defn check-append-edits
  "find the actions required to edit the content"
  {:added "4.0"}
  [delimiters]
  (= (take-while check-append-fn delimiters)
     (filter check-append-fn delimiters )))

(defn create-append-edits
  "find the actions required to edit the content"
  {:added "4.0"}
  [delimiters]
  (let [unclosed (take-while check-append-fn delimiters)
        close (last delimiters)]
    [{:action :insert
      :line (:line close)
      :col  (:col close)
      :new-char (apply str
                       (map (fn [{:keys [char]}]
                              (parse/lu-close char))
                            unclosed))}]))

(defn heal-append
  [content & [{:keys [ensure]}]]
  (let [delimiters (parse/pair-delimiters
                    (parse/parse-delimiters
                     content))
        enabled    (check-append-edits delimiters)]
    (cond enabled 
          (update-content content (create-append-edits
                                   delimiters))

          ensure
          (h/error "Not supported" {})

          :else content)))


;;
;; Balance for Bottom Heavy Code
;;

(defn check-remove-fn
  [{:keys [type pair-id]}]
  (and (= type :close)
       (not pair-id)))

(defn create-remove-edits
  "find the actions required to edit the content"
  {:added "4.0"}
  [delimiters]
  (let [unclosed (reverse (filter check-remove-fn delimiters))
        close (last delimiters)]
    [{:action :remove
      :line (:line close)
      :col  (:col close)}]))

(defn heal-remove
  [content & [{:keys [ensure]}]]
  (let [delimiters (parse/pair-delimiters
                    (parse/parse-delimiters
                     content))]
    (update-content content
                    (create-remove-edits
                     delimiters))))

;;
;; Heal generated code that is indented correctly but parens has not be placed right
;;

(defn heal-indented-single-pass
  "heals unclosed forms"
  {:added "4.0"}
  [content & [{:keys [limit minimum print]
               :as opts}]]
  (let [delimiters (parse/pair-delimiters
                    (parse/parse-delimiters
                     content))
        candidates (indent/flag-indent-discrepancies
                    delimiters)
        selected   (indent/candidates-filter-difficult candidates opts)
        _          (when print
                     (h/prn :ALL      (map (comp count second) candidates))
                     (h/prn :SELECTED (map (comp count second) selected)))
        
        edits      (indent/build-indent-edits delimiters
                                              selected)]
    (update-content content edits)))

(defn heal-indented-multi-pass
  "heals unclosed forms"
  {:added "4.0"}
  [content & [{:keys [limit minimum print]
               :as opts}]]
  (loop [old-content content
         pass 0]
    (when print
      (h/prn "Pass:" pass))
    (let [new-content (heal-indented-single-pass old-content opts)]
      (if (= new-content old-content)
        new-content
        (recur new-content (inc pass))))))

(defn heal-indented
  "heals unclosed forms"
  {:added "4.0"}
  [content & [{:keys [limit minimum print]
               :as opts}]]
  (-> (heal-indented-multi-pass content opts)
      (heal-append)
      (heal-mismatch)
      (heal-remove)))

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
