(ns code.heal.core
  (:require [std.lib :as h]
            [std.string :as str]
            [code.heal.parse :as parse]
            [code.heal.indent :as indent]
            [code.heal.edit :as edit]
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
        lines       (str/split-lines content)
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
  #_(h/prn {:start start
            :end end
            :start-col start-col
            :end-col end-col})
  (cond (= start end)
        (let [line (str (str/spaces (dec start-col))
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
                                 (h/prn {:count (count lines)
                                         :end end
                                         :start start
                                         :start-col start-col
                                         :end-col end-col})
                                 (throw t)))]
          (vec (concat [(str (str/spaces (dec start-col))
                             start-line)]
                       (subvec lines
                               start
                               (dec end))
                       [end-line])))))

(defn check-errored-suspect
  [block lines leftover-errors]
  (let [{:keys [line col]
         :as error} (first leftover-errors)
        row-offset  (:line (:lead block))        
        args   [[row-offset
                 (+ row-offset
                    (dec line))]
                (:col block)
                (dec col)]        
        #_#_#_#_#_#_
        _ (h/prn args)
        _ (h/prn (dissoc block :children))
        _ (h/prn error)
        snippet       (str/join-lines
                       (apply get-block-lines lines args))]
    (try
      (let [forms (read-string (str "[" snippet "]"))]
        (if (and (:last block)
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
        leftover-fn      (fn [{:keys [pair-id
                                      type]}]
                           (and (not pair-id)
                                (= type :close)))
        snippet          (str/join-lines
                          (get-block-lines lines
                                           (:line block)
                                           (:col block)))
        interim          (parse/parse snippet)
        interim-errors   (filter (comp not :correct?) interim)
        leftover-errors  (filter leftover-fn interim-errors)
        is-suspect       (if (seq leftover-errors)
                           (check-errored-suspect block lines leftover-errors))
        other-errors     (remove leftover-fn interim-errors)
        all-errors       (vec (concat (if (not is-suspect)
                                        leftover-errors)
                                      other-errors))

        block-error      (if (seq all-errors)
                           {:errors (mapv #(update % :line + (dec (first (:line block)))) all-errors)
                            :lines  (str/split-lines snippet)
                            :at     (dissoc block :children)})
        #_#_
        _                (h/pl snippet)
        #_#_
        _                (h/prf {:is-suspect is-suspect
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
      (do (h/prn)
          (h/prf info) 
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
                                    (h/prn info)
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
          _ (h/p (diff/->string deltas))]
      new-content)))

(defn wrap-diff
  "wraps the heal function to output the diff"
  {:added "4.0"}
  [print-fn]
  (fn [content]
    (let [new-content (print-fn content)]
      (diff/diff content new-content))))

