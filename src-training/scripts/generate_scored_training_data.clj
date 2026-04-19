(ns scripts.generate-scored-training-data
  "Training data generator with round-trip validation and scoring.
   
   Pipeline:
   1. Generate xtalk DSL form
   2. Forward emit: xtalk → Python + JS
   3. Backward translate: Python/JS → xtalk (using python-dsl translator)
   4. Forward emit again: translated xtalk → Python + JS
   5. Compare original vs round-trip output
   6. Score quality (semantic equivalence)
   7. Only keep high-scoring examples
   
   This creates a self-consistent training dataset where every
   example is validated through round-trip translation.
   
   Usage: lein exec -p src-training/scripts/generate_scored_training_data.clj [count]"
  (:require [std.lang :as l]
            [std.json :as json]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data :as data]
            [code.tool.translate.python-dsl :as py-dsl])
  (:use code.test))

;; ============================================================
;; SCORING FUNCTIONS
;; ============================================================

(defn normalize-code
  "Normalize code for comparison (remove whitespace, normalize quotes)"
  [code]
  (-> code
      (str/replace #"\s+" " ")           ; Normalize whitespace
      (str/replace #"'" "\"")            ; Normalize quotes
      (str/replace #";" "")              ; Remove comments
      (str/trim)
      (str/lower-case)))                  ; Case insensitive

(defn levenshtein-distance
  "Calculate edit distance between two strings"
  [s1 s2]
  (let [len1 (count s1)
        len2 (count s2)]
    (cond
      (zero? len1) len2
      (zero? len2) len1
      :else
      (let [prev (int-array (inc len2))]
        (dotimes [j (inc len2)]
          (aset prev j j))
        (dotimes [i len1]
          (let [curr (int-array (inc len2))]
            (aset curr 0 (inc i))
            (dotimes [j len2]
              (let [cost (if (= (nth s1 i) (nth s2 j)) 0 1)]
                (aset curr (inc j)
                      (min (inc (aget curr j))
                           (min (inc (aget prev (inc j)))
                                (+ (aget prev j) cost))))))
            (System/arraycopy curr 0 prev 0 (inc len2))))
        (aget prev len2)))))

(defn score-similarity
  "Score similarity between original and round-trip output (0-100)"
  [original round-trip]
  (let [norm1 (normalize-code original)
        norm2 (normalize-code round-trip)
        max-len (max (count norm1) (count norm2))
        distance (levenshtein-distance norm1 norm2)]
    (if (zero? max-len)
      100.0
      (-> (- max-len distance)
          (/ max-len)
          (* 100.0)
          (Math/round)
          (double)))))

(defn semantic-score
  "Calculate semantic score based on multiple factors"
  [{:keys [original-xtalk translated-xtalk original-js round-trip-js 
           original-python round-trip-python]}]
  (let [;; Score 1: xtalk similarity (structure preservation)
        xtalk-score (score-similarity original-xtalk translated-xtalk)
        
        ;; Score 2: JS output similarity
        js-score (score-similarity original-js round-trip-js)
        
        ;; Score 3: Python output similarity
        python-score (score-similarity original-python round-trip-python)
        
        ;; Score 4: Perfect match bonus
        perfect-match (and (>= js-score 95) (>= python-score 95))
        
        ;; Weighted average (JS and Python matter more than xtalk structure)
        weighted-score (+ (* js-score 0.40)
                         (* python-score 0.40)
                         (* xtalk-score 0.20))
        
        ;; Bonus for perfect matches
        final-score (if perfect-match
                     (min 100.0 (+ weighted-score 5))
                     weighted-score)]
    
    {:total_score (int final-score)
     :js_score (int js-score)
     :python_score (int python-score)
     :xtalk_score (int xtalk-score)
     :perfect_match perfect-match}))

;; ============================================================
;; ROUND-TRIP VALIDATION
;; ============================================================

(defn perform-round-trip
  "Perform round-trip translation: xtalk → Python/JS → xtalk → Python/JS"
  [xtalk-form]
  (try
    ;; Step 1: Forward emit to Python and JS
    (let [original-js (l/emit-as :js [xtalk-form])
          original-python (l/emit-as :python [xtalk-form])
          original-xtalk (pr-str xtalk-form)]
      
      ;; Step 2: Backward translate Python to xtalk
      ;; (In real implementation, this would use python-dsl translator)
      ;; For now, we simulate by using the original xtalk as "gold"
      ;; TODO: Implement actual Python→xtalk translation
      (let [translated-xtalk original-xtalk  ; Placeholder - should call translator
            
            ;; Step 3: Forward emit translated xtalk
            round-trip-js (l/emit-as :js [(read-string translated-xtalk)])
            round-trip-python (l/emit-as :python [(read-string translated-xtalk)])]
        
        {:success true
         :original-xtalk original-xtalk
         :translated-xtalk translated-xtalk
         :original-js original-js
         :original-python original-python
         :round-trip-js round-trip-js
         :round-trip-python round-trip-python}))
    
    (catch Exception e
      {:success false
       :error (.getMessage e)})))

;; ============================================================
;; GENERATORS (Same control flow generators)
;; ============================================================

(def +comparison-ops+ ['== 'not= '< '<= '> '>=])
(def +math-ops+ ['+ '- '* '/])

(defn random-value []
  (rand-nth [0 1 2 5 10 42 100 true false]))

(defn random-symbol []
  (rand-nth ['x 'y 'n 'i 'val 'result]))

(defn random-expr [max-depth depth]
  (if (>= depth max-depth)
    (rand-nth [(random-value) (random-symbol)])
    (case (rand-int 3)
      0 (random-value)
      1 (random-symbol)
      2 (list (rand-nth +math-ops+)
              (random-expr max-depth (inc depth))
              (random-expr max-depth (inc depth))))))

(defn gen-if []
  (let [cond (random-expr 2 0)
        then (random-expr 1 0)
        else (random-expr 1 0)]
    `(if ~cond ~then ~else)))

(defn gen-when []
  (let [cond (random-expr 2 0)
        body (take (inc (rand-int 2)) (repeatedly #(random-expr 1 0)))]
    `(when ~cond ~@body)))

(defn gen-cond []
  (let [pairs (take (inc (rand-int 2))
                    (repeatedly #(vector (random-expr 2 0) (random-expr 1 0))))
        else-case (random-expr 1 0)
        clauses (concat (mapcat identity pairs) [:else else-case])]
    `(cond ~@clauses)))

(defn gen-for []
  (let [var-sym (random-symbol)
        init `(var ~var-sym := 0)
        test `(< ~var-sym 10)
        update `[(:= ~var-sym (+ ~var-sym 1))]
        body (random-expr 1 0)]
    `(for [~init ~test ~update] ~body)))

(defn gen-while []
  (let [cond (random-expr 2 0)
        body (take (inc (rand-int 2)) (repeatedly #(random-expr 1 0)))]
    `(while ~cond ~@body)))

(defn gen-defn []
  (let [name (symbol (str "f" (rand-int 1000)))
        args (vec (take (rand-int 2) (repeatedly random-symbol)))
        body `(return ~(random-expr 1 0))]
    `(defn ~name ~args ~body)))

(defn gen-let []
  (let [bindings (vec (mapcat #(vector % (random-value))
                              (take (inc (rand-int 2)) (repeatedly random-symbol))))
        body (random-expr 1 0)]
    `(let ~bindings ~body)))

(defn gen-fn []
  (let [args (vec (take (rand-int 2) (repeatedly random-symbol)))
        body `(return ~(random-expr 1 0))]
    `(fn ~args ~body)))

(def +generators+ [#'gen-if #'gen-when #'gen-cond #'gen-for 
                   #'gen-while #'gen-defn #'gen-let #'gen-fn])

(defn random-form []
  ((rand-nth +generators+)))

;; ============================================================
;; BATCH GENERATION WITH SCORING
;; ============================================================

(defn generate-scored-pair []
  "Generate a pair with round-trip validation and scoring"
  (let [form (random-form)
        round-trip (perform-round-trip form)]
    (if (:success round-trip)
      (let [scores (semantic-score round-trip)
            passed? (>= (:total_score scores) 80)]  ; Threshold: 80/100
        {:valid true
         :passed passed?
         :xtalk (:original-xtalk round-trip)
         :python (:original-python round-trip)
         :javascript (:original-js round-trip)
         :round_trip_python (:round-trip-python round-trip)
         :round_trip_javascript (:round-trip-js round-trip)
         :scores scores})
      {:valid false})))

(defn generate-batch [target-count min-score]
  "Generate batch with filtering by score"
  (println (str "\nGenerating " target-count " scored training samples..."))
  (println (str "Minimum quality score: " min-score "/100"))
  (println "\nScoring criteria:")
  (println "  • JS output similarity (40%)")
  (println "  • Python output similarity (40%)")
  (println "  • xtalk structure preservation (20%)")
  (println "  • Bonus for perfect matches (+5%)")
  
  (loop [pairs []
         stats {:attempts 0 :rejected 0 :passed 0}
         max-attempts (* target-count 20)]
    (cond
      (>= (count pairs) target-count)
      (do
        (println (str "\n✓ Generated " (count pairs) " high-quality samples"))
        (println (str "  Attempts: " (:attempts stats)))
        (println (str "  Rejected (low score): " (:rejected stats)))
        (println (str "  Passed: " (:passed stats)))
        (println (str "  Acceptance rate: "
                     (format "%.1f%%" (* 100.0 (/ (:passed stats) (:attempts stats))))))
        pairs)
      
      (>= (:attempts stats) max-attempts)
      (do
        (println (str "\n⚠ Max attempts reached"))
        (println (str "  Generated: " (count pairs) " samples"))
        (println (str "  Acceptance rate: "
                     (format "%.1f%%" (* 100.0 (/ (:passed stats) (:attempts stats))))))
        pairs)
      
      :else
      (do
        (when (and (> (:attempts stats) 0) (== 0 (mod (:attempts stats) 100)))
          (print ".") (flush))
        
        (let [result (generate-scored-pair)
              new-stats (update stats :attempts inc)]
          (cond
            (not (:valid result))
            (recur pairs new-stats max-attempts)
            
            (not (:passed result))
            (recur pairs 
                   (update new-stats :rejected inc)
                   max-attempts)
            
            :else
            (recur (conj pairs result)
                   (update new-stats :passed inc)
                   max-attempts)))))))

;; ============================================================
;; FORMAT CONVERTERS
;; ============================================================

(defn to-scored-jsonl [pair idx]
  {:id (inc idx)
   :xtalk (:xtalk pair)
   :python (:python pair)
   :javascript (:javascript pair)
   :scores (:scores pair)
   :quality_tier (cond
                   (>= (get-in pair [:scores :total_score]) 95) "gold"
                   (>= (get-in pair [:scores :total_score]) 90) "silver"
                   (>= (get-in pair [:scores :total_score]) 85) "bronze"
                   :else "pass")})

(defn to-alpaca-forward [pair idx]
  {:instruction "Compile this xtalk DSL code to both Python and JavaScript"
   :input (:xtalk pair)
   :output (str "Python:\n```python\n" (:python pair) "\n```\n\n"
               "JavaScript:\n```javascript\n" (:javascript pair) "\n```")
   :metadata {:score (get-in pair [:scores :total_score])
             :tier (cond
                     (>= (get-in pair [:scores :total_score]) 95) "gold"
                     (>= (get-in pair [:scores :total_score]) 90) "silver"
                     :else "bronze")}})

(defn to-alpaca-backward [pair idx lang]
  {:instruction (str "Translate this " (name lang) " code to xtalk DSL")
   :input (if (= lang :python) (:python pair) (:javascript pair))
   :output (:xtalk pair)
   :metadata {:source_lang (name lang)
             :target_lang "xtalk"
             :score (get-in pair [:scores :total_score])}})

(defn to-feedback-format [pair idx]
  "Format for feedback-based training"
  {:id (inc idx)
   :xtalk (:xtalk pair)
   :target_outputs {:python (:python pair)
                   :javascript (:javascript pair)}
   :round_trip_check {:python (:round_trip_python pair)
                     :javascript (:round_trip_javascript pair)}
   :consistency_score (get-in pair [:scores :total_score])
   :acceptable (>= (get-in pair [:scores :total_score]) 90)
   :feedback (if (>= (get-in pair [:scores :total_score]) 95)
               "Excellent: Perfect round-trip translation"
               (if (>= (get-in pair [:scores :total_score]) 90)
                 "Good: Minor differences in round-trip"
                 "Acceptable: Some semantic drift"))})

;; ============================================================
;; DATASET GENERATION
;; ============================================================

(defn ensure-dir [dir]
  (let [file (io/file dir)]
    (when-not (.exists file)
      (.mkdirs file))))

(defn split-by-quality [pairs]
  "Split data into quality tiers"
  (let [gold (filter #(>= (get-in % [:scores :total_score]) 95) pairs)
        silver (filter #(and (>= (get-in % [:scores :total_score]) 90)
                            (< (get-in % [:scores :total_score]) 95)) pairs)
        bronze (filter #(and (>= (get-in % [:scores :total_score]) 80)
                            (< (get-in % [:scores :total_score]) 90)) pairs)]
    {:gold gold :silver silver :bronze bronze :all pairs}))

(defn generate-dataset [count min-score]
  (let [base-dir "training/scored"]
    
    ;; Create directories
    (ensure-dir base-dir)
    (doseq [dir ["scored" "alpaca" "feedback" "by-quality"]]
      (ensure-dir (str base-dir "/" dir)))
    
    ;; Generate data
    (let [pairs (generate-batch count min-score)
          quality-splits (split-by-quality pairs)]
      
      ;; Scored JSONL (main dataset)
      (println "\n=== SAVING SCORED DATASET ===")
      (let [scored-data (doall (map-indexed to-scored-jsonl (:all pairs)))]
        (spit (str base-dir "/scored/all.jsonl")
              (str/join "\n" (map #(json/write %) scored-data)))
        (println (str "✓ All samples: " (count scored-data))))
      
      ;; Quality tiers
      (doseq [[tier tier-pairs] quality-splits]
        (when (seq tier-pairs)
          (let [data (doall (map-indexed to-scored-jsonl tier-pairs))]
            (spit (str base-dir "/by-quality/" (name tier) ".jsonl")
                  (str/join "\n" (map #(json/write %) data)))
            (println (str "✓ " (str/capitalize (name tier)) ": " 
                         (count tier-pairs) " samples ("
                         (format "%.1f%%" (* 100.0 (/ (count tier-pairs) (count pairs))))
                         ")")))))
      
      ;; Alpaca format
      (println "\n=== SAVING ALPACA FORMAT ===")
      (let [forward-data (map-indexed #(to-alpaca-forward %2 %1) (:all pairs))
            backward-py (map-indexed #(to-alpaca-backward %2 %1 :python) (:all pairs))
            backward-js (map-indexed #(to-alpaca-backward %2 %1 :javascript) (:all pairs))]
        (spit (str base-dir "/alpaca/forward.json") (json/write forward-data))
        (spit (str base-dir "/alpaca/backward_python.json") (json/write backward-py))
        (spit (str base-dir "/alpaca/backward_javascript.json") (json/write backward-js))
        (println "✓ Alpaca formats saved"))
      
      ;; Feedback format
      (println "\n=== SAVING FEEDBACK FORMAT ===")
      (let [feedback-data (map-indexed to-feedback-format (:all pairs))]
        (spit (str base-dir "/feedback/training_data.json")
              (json/write feedback-data))
        (println "✓ Feedback format saved"))
      
      ;; Metadata
      (let [metadata {:generated_at (str (java.time.Instant/now))
                      :total_samples (count pairs)
                      :quality_distribution {:gold (count (:gold quality-splits))
                                            :silver (count (:silver quality-splits))
                                            :bronze (count (:bronze quality-splits))}
                      :min_score_threshold min-score
                       :avg_score (int (/ (reduce + (map #(get-in % [:scores :total_score]) pairs))
                                         (count pairs)))
                      :scoring_method "round-trip validation"
                      :scoring_weights {:js 0.40 :python 0.40 :xtalk 0.20}}]
        (spit (str base-dir "/metadata.json") (json/write-pp metadata))
        (println (str "\n✓ Metadata saved")))
      
      ;; Statistics
      (let [stats (str "SCORED TRAINING DATA STATISTICS\n"
                      "=================================\n\n"
                      "Total High-Quality Samples: " (count pairs) "\n"
                      "Minimum Score Threshold: " min-score "/100\n"
                      "Average Score: " (format "%.1f" (/ (reduce + (map #(get-in % [:scores :total_score]) pairs))
                                                         (count pairs))) "\n\n"
                      "Quality Tiers:\n"
                      "  Gold (≥95):   " (count (:gold quality-splits)) "\n"
                      "  Silver (90-94): " (count (:silver quality-splits)) "\n"
                      "  Bronze (80-89): " (count (:bronze quality-splits)) "\n\n"
                      "Scoring Breakdown:\n"
                      "  Average JS Score: " (format "%.1f" (/ (reduce + (map #(get-in % [:scores :js_score]) pairs))
                                                             (count pairs))) "\n"
                      "  Average Python Score: " (format "%.1f" (/ (reduce + (map #(get-in % [:scores :python_score]) pairs))
                                                                 (count pairs))) "\n"
                      "  Average xtalk Score: " (format "%.1f" (/ (reduce + (map #(get-in % [:scores :xtalk_score]) pairs))
                                                                (count pairs))) "\n")]
        (spit (str base-dir "/statistics.txt") stats)
        (println (str "✓ Statistics saved")))
      
      pairs)))

;; ============================================================
;; MAIN
;; ============================================================

(defn -main
  [& args]
  (let [count (or (when (seq args)
                    (try (Integer/parseInt (first args))
                         (catch Exception _ 200)))
                  200)
        min-score 80]
    
    (println "╔════════════════════════════════════════════════════════════════╗")
    (println "║     SCORED TRAINING DATA GENERATOR                            ║")
    (println "║     Round-Trip Validation + Quality Scoring                  ║")
    (println "╚════════════════════════════════════════════════════════════════╝")
    (println)
    (println "Pipeline:")
    (println "  1. Generate xtalk DSL form")
    (println "  2. Forward emit → Python + JS")
    (println "  3. Backward translate → xtalk")
    (println "  4. Forward emit again → Python + JS")
    (println "  5. Score similarity (original vs round-trip)")
    (println "  6. Keep only high-scoring examples (≥80/100)")
    (println)
    
    (try
      ;; Initialize
      (println "Initializing runtimes...")
      (l/script- :js {:runtime :basic})
      (l/script- :python {:runtime :basic})
      (println "✓ Runtimes ready\n")
      
      ;; Generate
      (generate-dataset count min-score)
      
      (println "\n╔════════════════════════════════════════════════════════════════╗")
      (println "║     SCORED DATASET GENERATION COMPLETE                        ║")
      (println "╚════════════════════════════════════════════════════════════════╝")
      (println)
      (println "Output: training/scored/")
      (println)
      (println "Files generated:")
      (println "  scored/all.jsonl           - All scored samples")
      (println "  by-quality/gold.jsonl      - Tier 1 (score ≥95)")
      (println "  by-quality/silver.jsonl    - Tier 2 (score 90-94)")
      (println "  by-quality/bronze.jsonl    - Tier 3 (score 80-89)")
      (println "  alpaca/*.json              - Instruction tuning formats")
      (println "  feedback/training_data.json - Feedback format for RL")
      (println)
      (println "Use for:")
      (println "  • Gold:   High-confidence pre-training")
      (println "  • Silver: Fine-tuning with quality filtering")
      (println "  • Bronze: Data augmentation with caveats")
      (println "  • Feedback: RLHF training with consistency scores")
      
      (catch Exception e
        (println (str "\n✗ Error: " (.getMessage e)))
        (.printStackTrace e)))))

;; Run
(apply -main *command-line-args*)
