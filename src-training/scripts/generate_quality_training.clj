(ns scripts.generate-quality-training
  "Generates scored training data with quality metrics.
   Validates through round-trip translation."
  (:require [std.lang :as l]
            [clojure.string :as str])
  (:use code.test))

;; Generators
(defn random-value [] (rand-nth [0 1 10 42 true false]))
(defn random-symbol [] (rand-nth ['x 'y 'n]))

(defn random-expr [depth]
  (if (zero? depth)
    (rand-nth [(random-value) (random-symbol)])
    (case (rand-int 3)
      0 (random-value)
      1 (random-symbol)
      2 (list (rand-nth ['+ '- '*])
              (random-expr (dec depth))
              (random-expr (dec depth))))))

(defn gen-form []
  (case (rand-int 5)
    0 `(if ~(random-expr 2) ~(random-expr 1) ~(random-expr 1))
    1 `(when ~(random-expr 2) ~(random-expr 1))
    2 `(cond ~(random-expr 2) ~(random-expr 1) :else ~(random-expr 1))
    3 `(for [(var i := 0) (< i 10) [(:= i (+ i 1))]] ~(random-expr 1))
    4 `(defn ~(symbol (str "f" (rand-int 100))) [~@(repeatedly (rand-int 2) random-symbol)] 
         (return ~(random-expr 1)))))

(defn emit [form]
  (try
    {:js (l/emit-as :js [form])
     :py (l/emit-as :python [form])
     :xtalk (pr-str form)}
    (catch Exception e nil)))

(defn score [orig round-trip]
  (let [s1 (str/lower-case (str/replace orig #"\s" ""))
        s2 (str/lower-case (str/replace round-trip #"\s" ""))
        dist (if (= s1 s2) 0 (count s1))  ; Simplified scoring
        score-val (if (zero? (count s1)) 100
                   (-> (- (count s1) dist)
                       (/ (count s1))
                       (* 100)
                       (int)))]
    score-val))

(defn generate [n]
  (println (str "Generating " n " samples..."))
  (l/script- :js {:runtime :basic})
  (l/script- :python {:runtime :basic})
  
  (loop [results [] attempts 0]
    (cond
      (>= (count results) n)
      (do
        (println (str "✓ Generated " (count results) " samples (" attempts " attempts)"))
        (vec results))
      
      (> attempts (* n 20))
      (do
        (println (str "⚠ Max attempts, generated " (count results)))
        (vec results))
      
      :else
      (let [form (gen-form)
            emitted (emit form)]
        (if emitted
          (let [rt (emit (read-string (:xtalk emitted)))
                js-score (score (:js emitted) (:js rt))
                py-score (score (:py emitted) (:py rt))
                total (int (/ (+ js-score py-score) 2))]
            (if (>= total 80)
              (recur (conj results {:xtalk (:xtalk emitted)
                                   :python (:py emitted)
                                   :javascript (:js emitted)
                                   :score total
                                   :tier (cond (>= total 95) "gold"
                                              (>= total 90) "silver"
                                              :else "bronze")})
                     (inc attempts))
              (recur results (inc attempts))))
          (recur results (inc attempts)))))))

(defn -main [& args]
  (let [n (first args)
        target-count (or (try (Integer/parseInt n) (catch Exception _ 50)) 50)
        data (generate target-count)
        lines (doall (map #(str (:xtalk %) "\t" (:python %) "\t" (:javascript %) "\t" (:score %)) data))]
    (spit "training/scored_data.tsv" (str/join "\n" lines))
    (println (str "✓ Saved to training/scored_data.tsv (" (count data) " samples)"))))

(apply -main *command-line-args*)
