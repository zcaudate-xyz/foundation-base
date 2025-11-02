(ns code.heal.unclosed
  (:require [code.heal.parse :as parse]
            [std.lib :as h]
            [std.string :as str]))

(defn flag-indent-discrepancies-single
  [delimiters {:keys [depth col index]}]
  (loop [index   (dec index)
         results []]
    (let [prev    (get delimiters index)
          [flagged end?]  (cond (nil? prev)                [nil true]
                                  (= :close (:type prev))    []
                                  (and (> depth  (:depth prev))
                                       (< col    (:col   prev))) [prev false]
                                  (and (> depth  (:depth prev))
                                       (<= col   (:col   prev))) [prev true]
                                  :else [])
          _ (if (= :open (:type prev))
              (h/prn [:depth [depth (:depth prev)]
                      :col [col (:col prev)]
                                              
                      :flagged (boolean flagged)
                      :end? end?]))
          results (if flagged
                    (conj results flagged)
                    results)]
      (cond end? results
            
            
            :else (recur (dec index) results)))))


(defn flag-indent-discrepancies
  [delimiters]
  (let [singles (filter (fn [{:keys [type]}]
                          (= type :open))
                        (reverse delimiters))]
    (vec (keep (fn [single]
                 (h/prn :INDEX (:index single))
                 (let [results (flag-indent-discrepancies-single delimiters single)]
                   (when (not-empty results)
                     [(:index single) results])))
               singles))))


(comment
  (defn find-indent-discrepancies
    "Finds opening delimiters where the column is less than a previous opening delimiter at the same depth."
    {:added "4.0"}
    [delimiters]
    (-> (reduce (fn [{:keys [result state]
                      :as acc}
                     {:keys [type depth col]
                      :as entry}]
                  (if (not= :open type)
                    acc
                    (let [prev-col (get state depth)
                          discrepancy? (and prev-col (< col prev-col))]
                      {:result (if discrepancy?
                                 (conj result
                                       (assoc entry :discrepancy? true))
                                 result)
                       :state (assoc state depth col)})))
                {:result []
                 :state  {}}
                delimiters)
        :result))

  (defn find-indent-discrepancies
    "Finds opening delimiters where the column is less than a previous opening delimiter at the same depth."
    {:added "4.0"}
    [delimiters]
    )

  (defn find-indent-parent
    "Finds opening delimiters where the column is less than a previous opening delimiter at the same depth."
    {:added "4.0"}
    [delimiters descrepency]
    (let [{:keys [index depth]} descrepency]
      (loop [index (dec index)]
        (when (not= index 0)
          (let [e (get delimiters index)]
            (if (and (= :open (:type e))
                     (= (dec depth) (:depth e)))
              e
              (recur (dec index))))))))

  (defn find-indent-last-close
    "Finds opening delimiters where the column is less than a previous opening delimiter at the same depth."
    {:added "4.0"}
    [delimiters descrepency]
    (let [{:keys [index depth]} descrepency]
      (loop [index (dec index)]
        (when (not= index 0)
          (let [e (get delimiters index)]
            (if (and (= :close (:type e))
                     (= depth (:depth e)))
              e
              (recur (dec index)))))))))
