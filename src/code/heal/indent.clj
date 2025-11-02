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
                          (and (= type :open)
                               (> col 1)))
                        (reverse delimiters))]
    (vec (keep (fn [single]
                 (let [results (flag-indent-discrepancies-single delimiters line-lu single)]
                   (when (not-empty results)
                     [(:index single) results])))
               singles))))

(defn flag-indent-discrepancies
  "combines discrepancies that are the same"
  {:added "4.0"}
  [delimiters]
  (let [discrepancies (flag-indent-discrepancies-raw
                       delimiters
                       (parse/make-delimiter-line-lu delimiters))]
    (->> (group-by second discrepancies)
         (vals)
         (map last)
         (sort-by first)
         (vec))))

(defn find-indent-last-close
  "finds the last close delimiter"
  {:added "4.0"}
  [delimiters entry index-limit]
  (let [{:keys [depth]} entry]
    (loop [index (dec index-limit)]
      (when (> index (:index entry) )
        (let [e (get delimiters index)]
          (if (= :close (:type e))
            e
            (recur (dec index))))))))


(comment

  
  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/004_shorten.block"))))
  
  (flag-indent-discrepancies-single
   *dlm4*
   (parse/make-delimiter-line-lu *dlm4*)
   (get *dlm4* 177)))
