(ns std.print.format.chart
  (:require [std.print.format.common :as common]
            [std.string.common]))

(defn lines:bar-graph
  "formats an ascii bar graph for output
 
   (lines:bar-graph (range 10) 6)
   => [\"    ▟\"
       \"  ▗██\"
       \" ▟███\"]"
  {:added "3.0"}
  ([xs height]
   (let [width (count xs)
         xs    (if (even? width) xs (concat xs [0]))
         _    (assert (even? height))
         m+   (apply max xs)
         m-   0
         w    (- m+ m-)
         xs'  (vec (for [x xs] (-> x (- m-) (/ w) (* height) int)))
         f    (fn [col row] (if (< (xs' col) row) 0 1))
         lookup (vec "    ▖▌  ▗ ▐ ▄▙▟█")]
     (for [row (range height 0 -2)]
       (apply str (for [col (range 0 width 2)
                        :let [a (f col row)
                              b (f (inc col) row)
                              c (f col (dec row))
                              d (f (inc col) (dec row))]]
                    (lookup (+ (* 1 a) (* 2 b) (* 4 c) (* 8 d)))))))))

(defn bar-graph
  "constructs a bar graph
 
   (-> (bar-graph (range 10) 6)
       (str/split-lines))
   => [\"    ▟\"
       \"  ▗██\"
       \" ▟███\"]"
  {:added "3.0"}
  ([xs]
   (bar-graph xs 30))
  ([xs height]
   (std.string.common/join "\n" (lines:bar-graph xs height))))

(defn sparkline
  "formats a sparkline
 
   (sparkline (range 8))
   => \"▁▂▃▅▆▇█\""
  {:added "3.0"}
  ([xs]
   (let [h+ (apply max xs)
         m  (vec " ▁▂▃▄▅▆▇█")
         spark-fn #(-> % double (/ (+ 0.00001 h+)) (* 9) int m)]
     (->> (mapv spark-fn xs)
          (apply str)))))

(defn tree-graph
  "returns a string representation of a tree
 
   (-> (tree-graph '[{a \"1.1\"}
                     [{b \"1.2\"}
                      [{c \"1.3\"}
                       {d \"1.4\"}]]])
       (str/split-lines))
   => [\"{a \\\"1.1\\\"}\"
       \" {b \\\"1.2\\\"}\"
       \"  {c \\\"1.3\\\"}\"
      \"  {d \\\"1.4\\\"}\"
       \"\"]"
  {:added "3.0"}
  ([tree] (apply str (map #(tree-graph % common/+pad+ "" nil?) tree)))
  ([tree pad prefix check]
   (if (and (vector? tree)
            (not (check tree)))
     (->> (map #(tree-graph % pad (str prefix pad) check) tree)
          (apply str))
     (str prefix tree "\n"))))

(defn table-basic:format
  "generates a table for output
 
   (table-basic:format [:id :value]
                       [{:id 1 :value \"a\"}
                        {:id 2 :value \"b\"}])
 
   => (ascii [\"| :id | :value |\"
              \"|-----+--------|\"
              \"|   1 |    \\\"a\\\" |\"
              \"|   2 |    \\\"b\\\" |\"])"
  {:added "3.0"}
  ([ks rows]
   (when (seq rows)
     (let [rows   (->> rows
                       (map (fn [row]
                              (reduce-kv (fn [out k v]
                                           (assoc out k (pr-str v)))
                                         {}
                                         row))))
           widths (map
                   (fn [k]
                     (apply max (count (str k)) (map #(count (get % k)) rows)))
                   ks)
           spacers (map #(apply str (repeat % "-")) widths)
           fmts (map #(str "%" % "s") widths)
           fmt-row (fn [leader divider trailer row]
                     (str leader
                          (apply str (interpose divider
                                                (for [[col fmt] (map vector (map #(get row %) ks) fmts)]
                                                  (format fmt (str col)))))
                          trailer))]
       (->> (map (fn [row] (fmt-row "| " " | " " |" row))
                 rows)
            (concat
             [(fmt-row "| " " | " " |" (zipmap ks ks))
              (fmt-row "|-" "-+-" "-|" (zipmap ks spacers))])
            (std.string.common/join "\n")))))
  ([rows] (table-basic:format (keys (first rows)) rows)))

(defn table-basic:parse
  "reads a table from a string
 
   (table-basic:parse (ascii
                       [\"| :id | :value |\"
                        \"|-----+--------|\"
                        \"|   1 |    \\\"a\\\" |\"
                        \"|   2 |    \\\"b\\\" |\"]))
   => {:headers [:id :value]
       :data [{:id 1 :value \"a\"}
              {:id 2 :value \"b\"}]}"
  {:added "3.0"}
  ([s]
   (let [[h _ & vs] (-> (std.string.common/trim-newlines s)
                        (std.string.common/split-lines))
         headers    (->> (std.string.common/split h #"\|")
                         (remove empty?)
                         (map std.string.common/trim)
                         (map #(subs % 1))
                         (map keyword))
         data-fn    (fn [v]
                      (->> (std.string.common/split v #"\|")
                           (remove empty?)
                           (map std.string.common/trim)
                           (map read-string)
                           (zipmap headers)))
         data       (map data-fn vs)]
     {:headers headers
      :data data})))

(defn table
  "generates a single table
 
   (table {\"a@a.com\" {:id 1 :value \"a\"}
           \"b@b.com\" {:id 2 :value \"b\"}}
          {:headers [:id :email :value]
           :sort-key :email
           :id-key :email})
   => (ascii [\"| :id |    :email | :value |\"
              \"|-----+-----------+--------|\"
              \"|   1 | \\\"a@a.com\\\" |    \\\"a\\\" |\"
             \"|   2 | \\\"b@b.com\\\" |    \\\"b\\\" |\"])"
  {:added "3.0"}
  ([m {:keys [id-key headers sort-key] :as opts}]
   (let [id-key (or id-key (first headers))
         rows (reduce-kv (fn [out k v]
                           (conj out (if id-key
                                       (assoc v id-key k)
                                       v)))
                         []
                         m)]
     (->> (sort-by (or sort-key id-key) rows)
          (table-basic:format headers)))))

(defn table:parse
  "generates a single table
 
   (table:parse
    (ascii [\"| :id |    :email | :value |\"
            \"|-----+-----------+--------|\"
            \"|   1 | \\\"a@a.com\\\" |    \\\"a\\\" |\"
            \"|   2 | \\\"b@b.com\\\" |    \\\"b\\\" |\"])
 
    {:headers [:id :email :value]
     :sort-key :email
    :id-key :email})
   => {\"a@a.com\" {:id 1 :value \"a\"}
       \"b@b.com\" {:id 2 :value \"b\"}}"
  {:added "3.0"}
  ([s {:keys [id-key] :as opts}]
   (let [{:keys [headers data]} (table-basic:parse s)
         id-key (or id-key (first headers))]
     (reduce (fn [out m]
               (assoc out (get m id-key) (dissoc m id-key)))
             {}
             data))))
