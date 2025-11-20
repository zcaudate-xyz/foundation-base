(ns code.heal.level
  (:require [std.lib :as h]
            [std.string :as str]
            [code.heal.parse :as parse]
            [code.heal.indent :as indent]
            [code.heal.core :as core]
            [std.text.diff :as diff]))

(defn group-min-col
  "gets the minimum column"
  {:added "4.0"}
  [entries prev]
  (let [entries (filter #(> % prev)
                        (keep :col entries))]
    (if (not-empty entries)
      (apply min entries))))

(defn group-entries
  "groups entries by colation"
  {:added "4.0"}
  [entries col]
  (let [{:keys [grouped
                interim]} (reduce (fn [{:keys [grouped
                                               interim]} e]
                                    (cond (= col (:col e))
                                          {:grouped (if (empty? interim)
                                                      grouped
                                                      (conj grouped interim))
                                           :interim [e]}
                                          
                                          :else
                                          {:grouped grouped
                                           :interim (conj interim e)}))
                                  {:grouped []
                                   :interim []}
                                  entries)]
    (conj grouped interim)))

(declare group-blocks-multi)

(defn group-blocks-single
  "creates a single block"
  {:added "4.0"}
  [entries col]
  (let [line     [(:line (first entries))
                  (:line (last  entries))]
        children (group-blocks-multi
                  (drop-while (comp nil? :col)
                              (rest entries))
                  col)]
    (cond-> {:lead  (first entries)
             :line   line
             :col col}
      children (assoc :children children))))

(defn group-blocks-multi
  "categorises the blocks"
  {:added "4.0"}
  ([entries]
   (group-blocks-multi entries 0))
  ([entries col]
   (if-let [min-col    (group-min-col entries col)]
     (let [groups      (group-entries entries min-col)
           blocks      (mapv #(group-blocks-single % min-col) groups)]
       (conj (vec (butlast blocks))
             (assoc (last blocks) :last true))))))

(defn group-blocks-prep
  "prepares the block entries for a file"
  {:added "4.0"}
  [content]
  (let [delimiters  (parse/parse-delimiters content)
        lu          (->> delimiters
                         (filter #(= (:type %) :open))
                         (group-by :line))
        lines       (str/split-lines content)
        starts      (parse/parse-lines-raw lines)
        entries     (mapcat (fn [{:keys [line] :as e}]
                              (or (get lu line)
                                  [e]))
                            starts)]
    {:lines lines
     :entries (vec entries)
     :delimiters delimiters
     :starts starts}))

(defn group-blocks
  "groups the lines by colation sections"
  {:added "4.0"}
  [content]
  (let [{:keys [lines entries]} (group-blocks-prep content)]
    (group-blocks-multi entries)))

(defn get-block-lines
  "gets the block lines"
  {:added "4.0"}
  [lines [start end] col]
  (let [start-line  (-> (get lines (dec start))
                        (subs (dec col)))]
    (vec (cons (str (str/spaces (dec col))
                    start-line)
               (subvec lines
                       start
                       end)))))

(defn get-errored-loop
  "runs the check block loop"
  {:added "4.0"}
  [block lines]
  (let [{:keys [children]} block
        child-errors (if (seq children)
                       (reduce (fn [_ child]
                                 (if-let [errored (get-errored-loop child lines)]
                                   (reduced errored)))
                               nil
                               children))]
    (if (not-empty child-errors)
      child-errors
      (let [unclosed-fn      (fn [{:keys [pair-id
                                          type]}]
                               (and (not pair-id)
                                    (= type :close)))
            snippet          (str (str/join-lines
                                   (get-block-lines lines
                                                    (:line block)
                                                    (:col block)))
                                  (str/spaces (:col block)))
            interim          (parse/parse snippet)
            interim-errors   (filter (comp not :correct?) interim)
            unclosed-errors  (if (and
                                  ;; Always check tailend forms at the right level
                                  (not (:last block))    
                                  ;; ignore non multiline forms
                                  (not (apply = (:line block))))
                               (filter unclosed-fn interim-errors)) 
            
            other-errors    (remove unclosed-fn interim-errors)
            all-errors      (vec (concat unclosed-errors
                                         other-errors))]
        (if (not-empty all-errors)
          {:errors (mapv #(update % :line + (dec (first (:line block)))) all-errors)
           :lines  (str/split-lines snippet)
           :at     (dissoc block :children)})))))

(defn get-errored-raw
  "checks content for irregular blocks"
  {:added "4.0"}
  [lines entries blocks]
  (vec (keep #(get-errored-loop % lines)
             blocks)))

(defn get-errored
  "checks content for irregular blocks"
  {:added "4.0"}
  [content]
  (let [{:keys [lines
                entries]}  (group-blocks-prep content)
        blocks     (group-blocks-multi entries)]
    (vec (keep #(get-errored-loop % lines)
               blocks))))

(defn heal-content-single-pass
  [content]
  (let [{:keys [lines
                entries
                delimiters
                starts]}  (group-blocks-prep content)
        blocks     (group-blocks-multi entries)
        errored (get-errored-raw lines entries blocks)
        _        (h/prf errored)
        edits   (mapcat (fn [{:keys [errors at]
                              :as info}]
                          (let [[e1 e2 & more]  errors]
                            (cond
                              
                              ;; Append Edit
                              (and (= (:type e1) :open)
                                   (nil? e2))
                              (let [closed (->> delimiters
                                                (filter #(= (:type %) :close))
                                                (group-by :line))
                                    {:keys [line col]} (first
                                                        (keep #(last (get closed %)) 
                                                              (reverse (range (first (:line at))
                                                                              (inc (second (:line at)))))))]
                                [{:action :insert
                                  :new-char (parse/lu-close (:char e1))
                                  :line  line
                                  :col   col}])
                              
                              
                              ;; Remove Edit
                              (and (= (:type e1) :close)
                                   (nil? e2))
                              (core/create-remove-edits [e1])
                              
                              ;; Mismatched parens
                              (and (= (:type e1) :open)
                                   (= (:type e2) :close))
                              (core/create-mismatch-edits [e1 e2])
                              
                              :else
                              (throw
                               (ex-info "Not Supported"
                                        {:info info})))))
                        errored)]
    (core/update-content content
                         edits)))

(defn heal-content-print
  [content]
  (let [new-content (heal-content-single-pass content)
        deltas (diff/diff content new-content)
        _ (h/p (diff/->string deltas))]
    new-content))


(defn heal-content
  [content]
  (loop [new-content (heal-content-single-pass content)]
    (if (= new-content content)
      content
      (return ))))



(comment
  (group-blocks
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser2.clj"))
  

  (heal-content-print
   (heal-content-print
    (heal-content-print
     (heal-content-print
      (heal-content-print
       (heal-content-print
        (heal-content-print
         (heal-content-print
          (heal-content-print
           (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser2.clj"))))))))))
  
  (code.heal/print-rainbow
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))
  (get-errored
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")))
