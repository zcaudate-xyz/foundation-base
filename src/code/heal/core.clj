(ns code.heal.core
  (:require [code.heal.parse :as parse]
            [code.heal.indent :as indent]
            [std.lib :as h]
            [std.string :as str]))

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

(defn heal-mismatch
  "heals a style mismatch for paired delimiters"
  {:added "4.0"}
  [content & [{:keys [ensure
                      debug]}]]
  (let [delimiters (parse/parse content)
        edits  (create-mismatch-edits delimiters)]
    (update-content content edits)))


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

(defn heal-append
  "appends at the"
  {:added "4.0"}
  [content & [{:keys [debug]}]]
  (let [delimiters (parse/parse content)
        enabled    (check-append-edits delimiters)]
    (cond (parse/is-close-heavy delimiters)
          content
          
          enabled 
          (update-content content (create-append-edits
                                   delimiters))

          #_#_ensure
          (h/error "Not supported" {})

          :else content)))


;;
;; Balance for Bottom Heavy Code
;;

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

(defn heal-remove
  "removed unmatched parens"
  {:added "4.0"}
  [content & [{:keys [debug]}]]
  (let [delimiters (parse/parse content)
        edits  (create-remove-edits
                delimiters)
        #_#__ (when debug
            (h/prn :REMOVE   (count edits)))]
    (update-content content edits)))

;;
;; Heal generated code that is indented correctly but parens has not be placed right
;;

(defn heal-close-heavy-single-pass
  [content & [{:keys [debug]
               :as opts}]]
  (let [delimiters (parse/parse content)
        candidates (indent/flag-close-heavy delimiters)
        
        #_#__          (when debug
                         (h/prn :ALL      (map (comp count second) candidates)))
        edits      (indent/build-remove-edits delimiters
                                              candidates)]
    (update-content content edits)))

(defn heal-close-heavy
  [content & [{:keys [limit debug]
               :or {limit 50}
               :as opts}]]
  (loop [old-content content
         pass 0]
    (when debug
      (h/prn "Heal Close Heavy:" pass))
    (let [new-content (heal-close-heavy-single-pass old-content opts)]
      (if (or (= new-content old-content)
              (> pass limit))
        new-content
        (recur new-content (inc pass))))))

(defn heal-open-heavy-single-pass
  "heals content that has been wrongly 
 
   (read-string
    (core/heal-open-heavy-single-pass \"
 (defn
  (do
    (it)
  (this)
  (this))\"))
   => '(defn (do (it)) (this) (this))"
  {:added "4.0"}
  [content & [{:keys [debug]
               :as opts}]]
  (let [delimiters (parse/parse content)
        candidates (indent/flag-open-heavy
                    delimiters)
        selected   (indent/flagged-candidates-filter-run candidates opts)
        #_#__          (when debug
                         (h/prn :ALL      (map (comp count second) candidates))
                         (h/prn :SELECTED (map (comp count second) selected)))
        
        edits      (indent/build-insert-edits delimiters
                                              selected
                                              content)]
    (update-content content edits)))

(defn heal-open-heavy
  "fixes indentation parens"
  {:added "4.0"}
  [content & [{:keys [limit minimum debug]
               :or {limit 50}
               :as opts}]]
  (loop [old-content content
         pass 0]
    (when debug
      (h/prn "Heal Open Heavy:" pass))
    (let [new-content (heal-open-heavy-single-pass old-content opts)]
      (if (or (= new-content old-content)
              (> pass limit))
        new-content
        (recur new-content (inc pass))))))

(defn heal-raw
  "combining all strategies for code heal"
  {:added "4.0"}
  [content & [{:keys [limit minimum debug]
               :as opts}]]
  (-> content
      (heal-close-heavy opts)
      (heal-open-heavy opts)
      (heal-append opts)
      (heal-mismatch opts)
      (heal-remove opts)
      (heal-open-heavy opts)))

(defn heal
  "heals the content"
  {:added "4.0"}
  [s & [{:keys [write]
         :as opts}]]
  (let [[path content] (cond (std.fs/path? s)
                             [(str s)
                              (slurp s)]

                             (string? s)
                             [nil s]

                             :else
                             [nil s])
        healed (heal-raw content opts)]
    (if (and write path)
      (spit path healed))
    healed))

