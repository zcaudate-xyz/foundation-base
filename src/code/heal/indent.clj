(ns code.heal.indent
  (:require [code.heal.parse :as parse]
            [std.lib :as h]
            [std.string :as str]))

(defn flag-indent-flag-function
  "TODO"
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

(defn flag-indent-discrepancies-single
  "finds previous index discrepancies given indent layout"
  {:added "4.0"}
  [delimiters line-lu {:keys [index]
                       :as entry}]
  (loop [index   (dec index)
         results []]
    (let [current   (get delimiters index)
          current   (get-in line-lu [(:line current) 0])
          index     (:index current)
          [flagged end?]  (flag-indent-flag-function
                           current
                           entry)
          results (if flagged
                    (conj results flagged)
                    results)]
      (cond end? results
            
            :else (recur (dec index) results)))))

(defn flag-indent-discrepancies-raw
  "finds all discrepancies given some code"
  {:added "4.0"}
  [delimiters line-lu]
  (let [singles (filter (fn [{:keys [type col]}]
                          (= type :open))
                        (reverse delimiters))]
    (vec (keep (fn [single]
                 (let [results (flag-indent-discrepancies-single delimiters line-lu single)]
                   (when (not-empty results)
                     [(:index single) results])))
               singles))))


(defn candidates-merge-common
  "combines candidates that are the same"
  {:added "4.0"}
  [candidates]
  (->> (group-by second candidates)
       (vals)
       (map last)
       (sort-by first)
       (vec)))

(defn candidates-filter-difficult
  "combines candidates that are the same"
  {:added "4.0"}
  [candidates & [{:keys [limit minimium]
                  :or {minimium true}}]]
  (cond (empty? candidates)
        []

        :else
        (let [limit (or limit
                        (apply min (map (comp count second) candidates)))]
          (filterv (fn [loc]
                     (>= limit (count (second loc))))
                   candidates))))

(defn candidates-invert-lookup
  [candidates]
  (let [lu (reduce (fn [lu [index-limit locs]]
                     (reduce (fn [lu {:keys [index]}]
                               (update-in lu [index] conj index-limit))
                             lu
                             locs))
                   {}
                   candidates)]
    (h/map-vals #(apply min %) lu)))

(defn flag-indent-discrepancies
  [delimiters]
  (let [candidates (flag-indent-discrepancies-raw
                    delimiters
                    (parse/make-delimiter-line-lu delimiters))
        candidates  (candidates-merge-common candidates)
        inv-lu      (candidates-invert-lookup candidates)
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

(defn build-indent-edit
  [{:keys [line col]} vals]
  {:action :insert
   :line  line
   :col   col
   :new-char (apply str
                    (map (fn [{:keys [char]}]
                           (parse/lu-close char))
                         vals))})

(defn build-indent-edits
  [delimiters candidates]
  (keep (fn [[idx vals]]
          (if-let [loc (find-indent-last-close delimiters
                                               idx
                                               (:index (first vals)))]
            (build-indent-edit loc vals)))
        candidates))

(comment

  
  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/004_shorten.block"))))
  
  (build-indent-edits
   *dlm4*
   (flag-indent-discrepancies
    *dlm4*))
  
  (flag-indent-candidates-single
   *dlm4*
   (parse/make-delimiter-line-lu *dlm4*)
   (get *dlm4* 177)))
