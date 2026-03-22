(ns code.framework.text
  (:require [clojure.set]
            [clojure.string]
            [code.framework.common :as common]
            [std.print.ansi :as ansi]
            [std.text.diff :as text.diff]))

(defn summarise-deltas
  "summary of what changes have been made"
  {:added "3.0"}
  ([deltas]
   (-> (text.diff/summary deltas)
       (assoc :changed (->> (reduce (fn [out {:keys [functions]}]
                                      (clojure.set/union out functions))
                                    #{}
                                    deltas)
                            (sort)
                            (vec))))))

(defn deltas
  "returns a list of changes between the original and revised versions"
  {:added "3.0"}
  ([ns analysis original revised]
   (let [line-lu   (common/line-lookup ns analysis)
         deltas    (->> (text.diff/diff original revised)
                        (map (fn [{:keys [original] :as d}]
                               (let [{:keys [position count]} original
                                     lines  (range position (inc (+ position count)))
                                     funcs  (set (keep line-lu lines))]
                                 (-> d
                                     (assoc :label (if (seq funcs) (str (vec (sort funcs)))))
                                     (assoc :functions funcs))))))]
     deltas)))

(defn- insert
  [^String s ^long i ^String extra]
  (try 
    (-> (StringBuffer. s)
        (.insert i extra)
        (str))
    (catch Throwable t
      s)))

(defn highlight-lines
  "highlights a segment of code in a different color"
  {:added "3.0"}
  ([lines {:keys [row col end-row end-col] :as cinfo} offset {:keys [text]}]
   (let [start (- row offset)
         end   (- end-row offset)
         lines (-> (reduce (fn [lines i]
                             (update-in lines [i] #(ansi/style % #{(:highlight text)})))
                           lines
                           (range (inc start) end))
                   (update-in [end]  insert (dec end-col) (ansi/encode (:normal text)))
                   (update-in [start] insert (dec col) (ansi/encode (:highlight text))))
         lines (cond-> lines
                 (not= start end)
                 (update-in [end]  insert 0 (ansi/encode (:highlight text))))]
     lines)))

(defn ->string
  "turns a set of results into a output string for printing"
  {:added "3.0"}
  ([results lines line-lu {:keys [highlight text]
                           :or {text {:normal :cyan
                                      :highlight :yellow}}}]
   (->> results
        (map (fn [{:keys [row col end-row end-col] :as info}]
               (let [lines  (cond-> (mapv #(get lines (dec %))
                                          (range row (inc end-row)))
                              highlight (highlight-lines info row {:text text}))
                     func   (line-lu row)
                     output (->> (concat [(ansi/bold (format "@@ (%d,%d) - (%d,%d) %s"
                                                             row
                                                             col
                                                             end-row
                                                             end-col
                                                             (if func
                                                               (str ":: " (ansi/underline [func]))
                                                               "")))]
                                         (mapv #(ansi/style (str "-" (ansi/on-grey %)) #{(:normal text)})
                                               lines))
                                 (clojure.string/join "\n"))]
                 output)))
        (map #(str % "\n"))
        (clojure.string/join "\n"))))
