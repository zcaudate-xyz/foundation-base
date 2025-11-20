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
        (when (not-empty all-errors)
          #_(h/prn [all-errors
                  (:line block)
                  (dissoc block :children)])
          {:errors (mapv #(update % :line + (dec (first (:line block)))) all-errors)
           :lines  (str/split-lines snippet)
           :at     (dissoc block :children)})))))

(defn get-errored-raw
  "checks content for irregular blocks"
  {:added "4.0"}
  [lines blocks]
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
        errored (get-errored-raw lines blocks)
        edits   (mapcat (fn [{:keys [errors at]
                              :as info}]
                          (let [[e1 e2 & more]  errors]
                            (cond
                              
                              ;; Append Delimiter
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
                              
                              
                              ;; Remove Delimiter
                              (and (= (:type e1) :close)
                                   (nil? e2))
                              (core/create-remove-edits [e1])
                              
                              ;; Mismatched Delimiters
                              (and (= (:type e1) :open)
                                   (= (:type e2) :close))
                              (core/create-mismatch-edits [e1 e2])
                              
                              :else
                              (do (h/prf info)
                                (throw
                                 (ex-info "Not Supported"
                                          {:info info}))))))
                        errored)
        new-content  (try (core/update-content content
                                               edits)
                          (catch Throwable t
                            
                            (mapv (fn [{:keys [at]}]
                                    (h/prn at)
                                    (h/pl (str/join-lines
                                           (get-block-lines lines
                                                            (:line at)
                                                            (:col at)))))
                                  errored)
                            (throw t)))]
    new-content))

(defn heal-content
  [content]
  (loop [content content
         retries  50]
    (if (< retries 0)
      content
      (let [new-content (heal-content-single-pass content)]
        (if (= new-content content)
          content
          (recur new-content
                 (dec retries)))))))

(defn heal-content-print
  [content]
  (let [new-content (heal-content content)
        deltas (diff/diff content new-content)
        _ (h/p (diff/->string deltas))]
    new-content))

(defn wrap-print-diff
  [print-fn]
  (fn [content]
    (h/p "DIFF")
    (let [new-content (print-fn content)
          deltas (diff/diff content new-content)
          _ (h/p (diff/->string deltas))]
      new-content)))

(heal-content-print
 "
(oheuoeu)
 (hoeuoe
      (var a b)

      (hello))

      (hello))            
(oheuoeu)")

(h/pl
 "
(oheuoeu)
 (hoeuoe
      (var a b)


      (hello))

      (hello))            
(oheuoeu)")


(comment
  (heal-content-print
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser2.clj"))
  
  (s/layout
   (read-string
    (str "["
         (heal-content-print
          (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))
         "]")))
  
  (read-string
   (str "["
        ((wrap-print-diff code.heal.tokens/heal-tokens)
         ((wrap-print-diff core/heal)
          ((wrap-print-diff heal-content)
           (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))))
        "]"))
  
  (read-string
   (str "["
        (h/suppress
         ((wrap-print-diff core/heal)
          ((wrap-print-diff heal-content)
           (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/states_triggers_panel.clj"))))
        "]"))
  
  (h/suppress
   ((wrap-print-diff core/heal)
    ((wrap-print-diff heal-content)
     (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/states_triggers_panel2.clj"))))
  
  
  
  (doseq [f (keys
             (std.fs/list
              "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/"
              {:include [".clj$"]}))]
    
    (h/p f)
    (try (read-string
          (str "["
               ((wrap-print-diff code.heal.tokens/heal-tokens)
                ((wrap-print-diff core/heal)
                 ((wrap-print-diff heal-content)
                  (slurp f))))
               "]"))
         (catch Throwable t
           (h/p :FAILED)))))


(comment
  (count
   (str/split-lines
    (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")))
  
  (heal-content-single-pass
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))

  (h/meter :out
    (heal-content
     (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")))
  
  (h/p (diff/->string
        (diff/diff
         (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")
         *new-content*
         )))
  
  
  (group-blocks
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser2.clj"))

  (h/p
   (diff/->string
    (diff/diff
     (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")
     (heal-content-single-pass
      (heal-content-single-pass
       (heal-content-single-pass
        (heal-content-single-pass
         (heal-content-single-pass
          (heal-content-single-pass
           (heal-content-single-pass
            (heal-content-single-pass
             (heal-content-single-pass
              (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")))))))))))))
  
  

  (heal-content-print
   (heal-content-print
    (heal-content-print
     (heal-content-print
      (heal-content-print
       (heal-content-print
        (heal-content-print
         (heal-content-print
          ))))))))
  
  (heal-content
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))
  
  (code.heal/print-rainbow
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))
  (get-errored
   (slurp "/Users/chris/Development/buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")))
