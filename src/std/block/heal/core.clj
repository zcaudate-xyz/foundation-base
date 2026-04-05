(ns std.block.heal.core
  (:require [clojure.string]
            [std.block.heal.edit :as edit]
            [std.block.heal.indent :as indent]
            [std.block.heal.parse :as parse]
            [std.lib.env :as env]
            [std.string.prose :as prose]
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
                                                      (conj grouped
                                                            interim))
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
             :level  0
             :col   (:col (first entries))}
      (seq children) (assoc :children children
                            :level  (inc (apply max (map :level children)))))))

(defn group-blocks-multi
  "categorises the blocks"
  {:added "4.0"}
  ([entries]
   (group-blocks-multi entries 0))
  ([entries col]
   (if-let [min-col    (group-min-col entries col)]
     (let [groups      (group-entries entries min-col)
           blocks      (mapv #(group-blocks-single % min-col) groups)
           blocks      (if (= min-col 0)
                         (mapv  #(assoc % :last true) blocks)
                         (conj (vec (butlast blocks))
                               (assoc (last blocks) :last true)))]
       (vec (mapcat (fn [{:keys [lead
                                 children]
                          :as block}]
                      (case (:type lead)
                        :code children
                        :blank []
                        [block]))
                    blocks))))))

(defn group-blocks-prep-entries
  "prepares the block entries for a file"
  {:added "4.0"}
  [delimiters starts]
  (let [lu          (->> delimiters
                         (filter #(= (:type %) :open))
                         (group-by :line))]
    (vec (mapcat (fn [{:keys [line] :as e}]
                   
                   (if-let [delims  (get lu line)]
                     (if (and (:char e)
                              (not (:style e)))
                       (cons e delims)
                       delims)
                     [e]))
                 starts))))

(defn group-blocks-prep
  "prepares the block entries for a file"
  {:added "4.0"}
  [content]
  (let [delimiters  (parse/parse-delimiters content)
        lines       (clojure.string/split-lines content)
        starts      (parse/parse-lines-raw lines)
        entries     (group-blocks-prep-entries delimiters starts)]
    {:lines lines
     :entries entries
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
  [lines [start end] start-col & [end-col]]
  #_(env/prn {:start start
            :end end
            :start-col start-col
            :end-col end-col})
  (cond (= start end)
        (let [line (str (prose/spaces (dec start-col))
                        (-> (get lines (dec start))
                            (subs (dec start-col))))]
          (if end-col
            [(subs line 0 end-col)]
            [line]))
        
        :else
        (let [start-line  (-> (get lines (dec start))
                              (subs (dec start-col)))
              end-line    (try (cond-> (get lines (dec end))
                                 end-col (subs 0 end-col))
                               (catch Throwable t
                                 (env/prn {:count (count lines)
                                         :end end
                                         :start start
                                         :start-col start-col
                                         :end-col end-col})
                                 (throw t)))]
          (vec (concat [(str (prose/spaces (dec start-col))
                             start-line)]
                        (subvec lines
                                start
                                (dec end))
                        [end-line])))))

(defn create-block-scan
  "creates a scan window for a block.

   The default arity scans the full block. The extended arities allow the
   caller to override the scan line range, start column, and optional end
   column when a smaller suspect region has already been identified."
  {:added "4.1"}
  ([block lines]
   (create-block-scan block lines (:line block) (:col block)))
  ([block lines line col]
   (create-block-scan block lines line col nil))
  ([block lines line col end-col]
   {:line line
    :col col
    :last (:last block)
    :end-col end-col
    :offset (dec (first line))
    :snippet (prose/join-lines
              (get-block-lines lines line col end-col))}))

(def default-max-col
  Integer/MAX_VALUE)

(defn tighter-scan?
  "checks if the candidate scan is narrower than the current scan"
  {:added "4.1"}
  [scan line col end-col]
  (let [scan-start-line (first (:line scan))
        scan-end-line   (second (:line scan))
        scan-start-col  (:col scan)
        scan-end-col    (or (:end-col scan)
                            default-max-col)]
    (and (<= scan-start-line (first line))
         (<= (second line) scan-end-line)
         (<= scan-start-col col)
         (<= end-col scan-end-col)
         (or (> (first line) scan-start-line)
             (> col scan-start-col)
             (< (second line) scan-end-line)
             (< end-col scan-end-col)))))

(defn create-close-hint-scan
  "uses a later correct close delimiter to narrow the suspect scan window"
  {:added "4.1"}
  [block lines scan interim interim-errors]
  (let [first-open-error (first (filter #(= :open (:type %))
                                        interim-errors))
        global-line (fn [line]
                      (+ (:offset scan) line))]
    (when first-open-error
      (let [after-anchor? (fn [{:keys [line col]}]
                            (or (> line (:line first-open-error))
                                (and (= line (:line first-open-error))
                                     (> col (:col first-open-error)))))
            close-hint    (first (filter #(and (:correct? %)
                                               (= :close (:type %))
                                               (after-anchor? %))
                                         interim))]
        (when close-hint
          (let [line    [(global-line (:line first-open-error))
                         (global-line (:line close-hint))]
                col     (:col first-open-error)
                end-col (:col close-hint)]
            (when (tighter-scan? scan line col end-col)
              (create-block-scan block lines line col end-col))))))))

(defn localize-close-hint-scan
  "runs a close-delimiter localization pass before the full structural check"
  {:added "4.1"}
  [block lines]
  (let [scan           (create-block-scan block lines)
        interim        (parse/parse (:snippet scan))
        interim-errors (vec (filter (comp not :correct?) interim))]
    (if (seq (:children block))
      ;; Child blocks are already checked first in get-errored-loop. Their
      ;; indentation already gives us a tighter boundary, so applying the
      ;; close-hint prepass at the parent level tends to pull the scan back out
      ;; toward later closes that belong to the wider enclosing form.
      {:scan scan
       :interim interim
       :errors interim-errors}
      (if-let [narrowed (create-close-hint-scan
                         block lines scan interim interim-errors)]
        (let [narrowed-interim (parse/parse (:snippet narrowed))
              narrowed-errors  (vec (filter (comp not :correct?)
                                            narrowed-interim))]
          (if (and (seq narrowed-errors)
                   (< (count narrowed-errors)
                      (count interim-errors)))
            {:scan narrowed
             :interim narrowed-interim
             :errors narrowed-errors}
            {:scan scan
             :interim interim
             :errors interim-errors}))
        {:scan scan
         :interim interim
         :errors interim-errors}))))

(defn check-errored-suspect
  [scan lines leftover-errors]
  (let [{:keys [line col]
         :as error} (first leftover-errors)
        line-range  (:line scan)
        row-offset  (first line-range)
        args   [[row-offset
                 (+ row-offset
                    (dec line))]
                 (:col scan)
                 (dec col)]
        #_#_#_#_#_#_
        _ (env/prn args)
        _ (env/prn scan)
        _ (env/prn error)
        snippet       (prose/join-lines
                       (apply get-block-lines lines args))]
    (try
      (let [forms (read-string (str "[" snippet "]"))]
        (if (and (:last scan)
                 (<  1 (count forms)))
          true
          false))
         (catch Throwable t
           true))))

(defn get-errored-loop
  "runs the check block loop"
  {:added "4.0"}
  [block lines]
  ;;
  ;; This is the main error checking loop.
  ;;
  ;; Child errors might be reporting something that is a stylistic issue and 
  ;; not structural, and there are many cases where the indentation could be off
  ;; checking right up to the top level form to see if the lower level error
  ;; acually affects the top level form is super important to not prematurely
  ;; destroy the structure by adding unnecessary and damaging parens to the body
  ;;
  ;; There are a few checks in place:
  ;;  - check for leftover delimiters on a line ie. ')', '}', ']'
  ;;  - if there are, check if the form within the delimiters are valid
  ;;  - if the element is the last form, make sure that it can only be a single
  ;;    form. this prevents early termination errors
   ;;  - if there are child errors and they are less than the currently found errors
   ;;    return these. otherwise return the errors in the current block
   ;;
   (let [{:keys [children]} block
         child-error      (if (seq children)
                            (reduce (fn [_ child]
                                      (if-let [errored (get-errored-loop child lines)]
                                        (reduced errored)))
                                    nil
                                    children))
         {:keys [scan
                 interim
                 errors]} (localize-close-hint-scan block lines)
         leftover-fn      (fn [{:keys [pair-id
                                       type]}]
                            (and (not pair-id)
                                 (= type :close)))
         snippet          (:snippet scan)
         interim-errors   errors
         leftover-errors  (filter leftover-fn interim-errors)
         is-suspect       (if (seq leftover-errors)
                            (check-errored-suspect scan lines leftover-errors))
         other-errors     (remove leftover-fn interim-errors)
         all-errors       (vec (concat (if (not is-suspect)
                                         leftover-errors)
                                        other-errors))

         block-error      (if (seq all-errors)
                            {:errors (mapv #(update % :line + (:offset scan)) all-errors)
                             :lines  (clojure.string/split-lines snippet)
                             :at     (dissoc block :children)})
        #_#_
        _                (env/pl snippet)
        #_#_
        _                (env/prf {:is-suspect is-suspect
                                 :child child-error
                                 :block   (dissoc block :children)
                                 #_#_:interim interim-errors
                                 :leftover  leftover-errors
                                 :other  other-errors
                                 #_#_:all all-errors})]
    
    (if is-suspect
      (or child-error block-error)
      (if (and child-error
               (<= (count (:errors child-error))
                   (count (:errors block-error))))
        child-error
        block-error))))

(defn get-errored-raw
  "helper function for get-errored"
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

(defn heal-content-complex-edits
  [info errors]
  (let [[e1 e2 e3 & more] errors
        lead (get-in info [:at :lead])]
    (cond

      ;; if there is a leftover :close delimiter that is the same style as the lead
      ;; delimiter, then delete the paired :close parameter
      (and (= (:type e1) :close)
           (= (:style lead) (:style e1))
           (= -1 (:depth e1))
           (= :open (:type e2))
           (= :close (:type e3)))
      (edit/create-remove-edits [e3])

      ;; if parens are balanced but just mismatched, heal those
      (and (even? (count errors))
           (every? :pair-id errors))
      (edit/create-mismatch-edits errors)      
      
      :else
      (do (env/prn)
          (env/prf info) 
          (throw
           (ex-info "Not Supported"
                    {:info info}))))))

(defn heal-content-single-pass
  "heals the content in a single pass"
  {:added "4.0"}
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
                              (edit/create-remove-edits [e1])

                              ;; Remove Delimiters
                              (and (= (:type e1) :close)
                                   (= (:type e2) :close))
                              (edit/create-remove-edits [e1 e2])
                              
                              ;; Mismatched Delimiters
                              (and (= (:type e1) :open)
                                   (= (:type e2) :close))
                              (edit/create-mismatch-edits [e1 e2])
                              
                              :else
                              (heal-content-complex-edits info errors))))
                        errored)
        new-content  (try (edit/update-content content
                                               edits)
                          (catch Throwable t
                            (mapv (fn [{:keys [at]
                                        :as info}]
                                    (env/prn info)
                                    (throw
                                     (ex-info "Not Supported"
                                              {:info info})))
                                  errored)
                            (throw t)))]
    new-content))

(defn heal-content
  "allow multiple passes to heal the delimiter"
  {:added "4.0"}
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

(defn wrap-print-diff
  "print wrapper for the heal function"
  {:added "4.0"}
  [print-fn]
  (fn [content]
    (let [new-content (print-fn content)
          deltas (diff/diff content new-content)
          _ (env/p (diff/->string deltas))]
      new-content)))

(defn wrap-diff
  "wraps the heal function to output the diff"
  {:added "4.0"}
  [print-fn]
  (fn [content]
    (let [new-content (print-fn content)]
      (diff/diff content new-content))))
