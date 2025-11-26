(ns std.block.heal.indent
  (:require [std.block.heal.parse :as parse]
            [std.lib :as h]
            [std.string :as str]))

(defn flag-close-heavy-function
  "flags a function if at a different index"
  {:added "4.0"}
  [current {:keys [depth col index]}]
  #_(h/prn [:depth  [(:depth current) depth]
          :col    [(:col current)   col]
          :idx    index
          :current current])
  (cond (or (nil? current))          [nil true]
        (= :close (:type current))   []

        (and (= (:depth current) depth)
             (= (:col current) col))
        [nil true]
        
        (<= (:depth current) depth)
        (cond (>= (:col current) col)
              [current true]
              
              :else
              [nil true])

        :else []))

(defn flag-close-heavy-single
  "flags when there is open delimiter of the same depth with extra indentation"
  {:added "4.0"}
  [delimiters {:keys [index]
               :as entry}]
  (loop [index     (inc index)
         results   []
         current   (get delimiters index)]
    (cond (nil? current)
          results

          :else
          (let [[flagged end?]  (flag-close-heavy-function
                                 current
                                 entry)
                results (if flagged
                          (conj results flagged)
                          results)]
            (cond end? results

                  
                  :else (recur (inc index)
                               results
                               (get delimiters (inc index))))))))

(defn flag-close-heavy
  "finds open delimiters that have closed too early"
  {:added "4.0"}
  [delimiters]
  #_(if (parse/is-close-heavy delimiters))
  (let [singles (filter (fn [{:keys [type col]}]
                          (and (= type :open)
                               (= col 1)))
                        delimiters)]
    (vec (keep (fn [single]
                 (let [results (flag-close-heavy-single delimiters single)]
                   (when (not-empty results)
                     [(:index single) results])))
               singles))))

(defn flag-open-heavy-function
  "flags the delimiter if there are any discrepancies"
  {:added "4.0"}
  [current {:keys [depth col index]}]
  #_(h/prn [:depth  [(:depth current) depth]
            :col    [(:col current)   col]
            :idx    index
            :current current])
  (cond (nil? current)                [nil true]
        (= :close (:type current))    []
        
        
        (< (:depth current) depth)
        (cond (<  (:col current) col)
              [nil true]

              (= (:col current) col)
              [current true]
              
              (> (:col current) col)
              [current false])
        
        :else []))

(defn flag-open-heavy-single
  "finds previous index discrepancies given indent layout"
  {:added "4.0"}
  [delimiters line-lu {:keys [index]
                       :as entry}]
  (loop [index   (dec index)
         results []]
    (let [current   (get delimiters index)
          current   (get-in line-lu [(:line current) 0])
          index     (:index current)
          [flagged end?]  (flag-open-heavy-function
                           current
                           entry)
          results (if flagged
                    (conj results flagged)
                    results)]
      (cond end? results
            
            :else (recur (dec index) results)))))

(defn flag-open-heavy-raw
  "finds all discrepancies given some code"
  {:added "4.0"}
  [delimiters line-lu]
  (let [singles (filter (fn [{:keys [type col]}]
                          (= type :open))
                        (reverse delimiters))]
    (vec (keep (fn [single]
                 (let [results (flag-open-heavy-single delimiters line-lu single)]
                   (when (not-empty results)
                     [(:index single) results])))
               singles))))

(defn flagged-candidates-merge-common
  "merges all common"
  {:added "4.0"}
  [candidates]
  (->> (group-by second candidates)
       (vals)
       (map last)
       (sort-by first)
       (vec)))

(defn flagged-candidates-filter-run
  "cuts off all potentially difficult locations"
  {:added "4.0"}
  [candidates & [{:keys [strategy threshold]
                  :or {strategy :minimum}}]]
  (cond (empty? candidates)
        []

        :else
        (let [threshold (or threshold
                            (apply min (map (comp count second) candidates)))]
          (case strategy
            :minimum  (filterv (fn [loc]
                                 (>= threshold (count (second loc))))
                               candidates)))))

(defn flagged-candidates-invert-lookup
  "inverts a lookup"
  {:added "4.0"}
  [candidates]
  (let [lu (reduce (fn [lu [index-limit locs]]
                     (reduce (fn [lu {:keys [index]}]
                               (update-in lu [index] conj index-limit))
                             lu
                             locs))
                   {}
                   candidates)]
    (h/map-vals #(apply min %) lu)))

(defn flag-open-heavy
  "combines discrepancies that are the same"
  {:added "4.0"}
  [delimiters]
  (let [candidates (flag-open-heavy-raw
                    delimiters
                    (parse/make-delimiter-line-lu delimiters))
        candidates  (flagged-candidates-merge-common candidates)
        inv-lu      (flagged-candidates-invert-lookup candidates)
        lu          (->> (group-by second inv-lu)
                         (h/map-vals
                          (fn [idxs]
                            (->> idxs
                                 (map first)
                                 sort
                                 reverse
                                 (map delimiters)))))]
    (vec (sort lu))))

(defn find-indent-last-close
  "finds the last close delimiter"
  {:added "4.0"}
  [delimiters index-upper index-lower]
  (loop [index (dec index-upper)]
    (let [e (get delimiters index)]
      (cond (or (nil? e)
                (< index index-lower))
            nil

            (= :close (:type e))
            e
            
            :else
            (recur (dec index))))))

(defn build-insert-edit
  "constructs a single edit"
  {:added "4.0"}
  [{:keys [line col]} vals]
  {:action :insert
   :line  line
   :col   col
   :new-char (apply str
                    (map (fn [{:keys [char]}]
                           (parse/lu-close char))
                         vals))})

(defn build-insert-edits
  "builds a list of edits to be made to a"
  {:added "4.0"}
  [delimiters candidates content]
  (let [cached (volatile! nil)]
    (keep (fn [[idx vals]]
            (if-let [loc (find-indent-last-close delimiters
                                                 idx
                                                 (:index (first vals)))]
              (build-insert-edit loc vals)
              (let [lines  (or @cached
                               (vreset! cached (parse/parse-lines content)))
                    start  (dec (:line (get delimiters idx)))]
                (loop [i  (dec start)]
                  (let [{:keys [line last-idx type]
                         :as loc} (get lines i)]
                    (cond (nil? loc) nil
                          (= type :code) (build-insert-edit {:line line
                                                             :col  (inc last-idx)}
                                                            vals)
                          :else
                          (recur (dec i))))))))
          candidates)))

(defn build-remove-edits
  "builds a list of remove edits"
  {:added "4.0"}
  [delimiters candidates]
  (let [cached (volatile! nil)]
    (keep (fn [[idx vals]]
            (when-let [loc (find-indent-last-close delimiters
                                                   (:index (first vals))
                                                   idx)]
              {:action :remove
               :line  (:line loc)
               :col   (:col loc)}))
          candidates)))


(comment
  (vreset! (volatile! nil) 1)
 
  
  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/004_shorten.block"))))
  
  (build-insert-edits
   *dlm4*
   (flag-open-heavy
    *dlm4*))
  
  (flag-indent-candidates-single
   *dlm4*
   (parse/make-delimiter-line-lu *dlm4*)
   (get *dlm4* 177)))
