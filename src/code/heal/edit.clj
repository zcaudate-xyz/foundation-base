(ns code.heal.edit
  (:require [std.lib :as h]
            [std.string :as str]
            [code.heal.parse :as parse]))

(defn update-content
  "performs the necessary edits to a string"
  {:added "4.0"}
  [content edits]
  (cond (empty? edits)
        content

        :else
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
                                                          (:col edit)
                                                          new-char)

                                           :remove
                                           (str/replace-at old-line
                                                           (dec (:col edit))
                                                           ""))]
                           (assoc lines (dec (:line edit)) new-line)))
                       lines)
               (str/join "\n")))))
 
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

;;
;; Balance for Top Heavy code
;;

(defn check-append-fn
  "check for open unpaired"
  {:added "4.0"}
  [{:keys [type pair-id]}]
  (and (= type :open)
       (not pair-id)))

(defn check-append-edits
  "checks that append edits are value"
  {:added "4.0"}
  [delimiters]
  (= (take-while check-append-fn delimiters)
     (filter check-append-fn delimiters )))

(defn create-append-edits
  "creates the append edits"
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

(defn check-remove-fn
  "check for close unpaired"
  {:added "4.0"}
  [{:keys [type pair-id]}]
  (and (= type :close)
       (not pair-id)))

(defn create-remove-edits
  "creates removes edits"
  {:added "4.0"}
  [delimiters]
  (let [unopened (reverse (filter check-remove-fn delimiters))]
    (mapv (fn [{:keys [line col]}]
            {:action :remove
             :line line
             :col  col})
          unopened)))


