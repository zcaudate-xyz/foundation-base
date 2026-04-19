(ns training.util
  "Common utilities for training data generation.

   Provides:
   - Random expression generation
   - Code emission to JS/Python
   - JSONL serialization
   - Console formatting
   - File output helpers"
  (:require [std.lang :as l]
            [std.json :as json]
            [clojure.string :as str]))

;; ============================================================
;; RANDOM EXPRESSION GENERATION (BASIC)
;; ============================================================

(def +basic-values+
  "Basic primitive values for random generation"
  [0 1 2 5 10 42 100 -1 -10
   true false
   "hello" "world" "test"
   nil])

(def +basic-symbols+
  "Basic symbol names for random generation"
  ['x 'y 'n 'i 'val 'result 'acc 'item 'tmp 'total])

(def +basic-math-ops+
  "Basic math operators"
  ['+ '- '* '/])

(def +basic-comparison-ops+
  "Basic comparison operators"
  ['== 'not= '< '<= '> '>=])

(def +basic-logic-ops+
  "Basic logic operators"
  ['and 'or])

(defn random-value
  "Generate a random primitive value"
  []
  (rand-nth +basic-values+))

(defn random-symbol
  "Generate a random symbol name"
  []
  (rand-nth +basic-symbols+))

(defn random-expr
  "Generate a simple expression with depth limit.
   
   Options:
   - :max-depth (default 2) - maximum nesting depth
   - :operators - map with keys :math, :comparison, :logic (defaults to basic ops)
   - :allow-nested? (default true) - allow nested expressions"
  ([]
   (random-expr {}))
  ([opts]
   (let [max-depth (get opts :max-depth 2)
         operators (merge {:math +basic-math-ops+
                           :comparison +basic-comparison-ops+
                           :logic +basic-logic-ops+}
                          (:operators opts))
         allow-nested? (get opts :allow-nested? true)]
     (letfn [(gen [depth]
               (if (or (>= depth max-depth) (not allow-nested?))
                 (rand-nth [(random-value) (random-symbol)])
                 (case (rand-int 5)
                   0 (random-value)
                   1 (random-symbol)
                   2 (list (rand-nth (:math operators))
                           (gen (inc depth))
                           (gen (inc depth)))
                   3 (list (rand-nth (:comparison operators))
                           (gen (inc depth))
                           (gen (inc depth)))
                   4 (list (rand-nth (:logic operators))
                           (gen (inc depth))
                           (gen (inc depth))))))]
       (gen 0)))))

;; ============================================================
;; CODE EMISSION
;; ============================================================

(defn init-runtimes
  "Initialize std.lang runtimes for JS and Python.
   Call this before emitting code."
  []
  (l/script- :js {:runtime :basic})
  (l/script- :python {:runtime :basic})
  (println "✓ Runtimes initialized"))

(defn emit-to-both
  "Emit xtalk form to both JavaScript and Python.
   
   Returns map with keys:
   - :valid (boolean) - whether emission succeeded
   - :xtalk (string) - formatted xtalk code
   - :js (string) - JavaScript translation
   - :python (string) - Python translation
   - :error (string) - error message if emission failed"
  [form]
  (try
    (let [js-code (l/emit-as :js [form])
          py-code (l/emit-as :python [form])]
      {:valid true
       :xtalk (pr-str form)
       :js js-code
       :python py-code})
    (catch Exception e
      {:valid false
       :error (.getMessage e)
       :xtalk (pr-str form)})))

;; ============================================================
;; JSONL SERIALIZATION
;; ============================================================

(defn map->jsonl
  "Convert a map to a JSONL string line.
   Uses std.json/write for serialization."
  [m]
  (json/write m))

(defn pairs->jsonl
  "Convert a sequence of maps to JSONL string.
   Each map is serialized as a separate line."
  [pairs]
  (str/join "\n" (map map->jsonl pairs)))

(defn write-jsonl-file
  "Write pairs to a JSONL file.
   
   Args:
   - pairs: sequence of maps
   - filepath: output file path
   - stats? (optional): if true, print statistics
   
   Returns the file path."
  [pairs filepath & [stats?]]
  (let [content (pairs->jsonl pairs)]
    (spit filepath content)
    (when stats?
      (println (str "✓ Written " (count pairs) " pairs to " filepath)))
    filepath))

;; ============================================================
;; CONSOLE FORMATTING
;; ============================================================

(defn format-pair-console
  "Format a training pair for console display.
   
   Expected keys in pair map:
   - :id (optional) - pair identifier
   - :type (optional) - pair type/category
   - :description (optional) - description
   - :xtalk - xtalk code
   - :js - JavaScript translation
   - :python - Python translation
   
   Returns formatted string."
  [pair]
  (let [id (:id pair "?")
        type (or (:type pair) (:form_type pair) (:control_type pair) "unknown")
        desc (or (:description pair) "")
        xtalk (:xtalk pair)
        js-code (:js pair)
        py-code (:python pair)]
    (str "\n╔════════════════════════════════════════════════════════════════╗\n"
         "║ Pair #" id " - " type
         (if (not-empty desc) (str " - " desc) "") "\n"
         "╠════════════════════════════════════════════════════════════════╣\n"
         "║ XTalk:\n║   " xtalk "\n"
         "╠════════════════════════════════════════════════════════════════╣\n"
         "║ JavaScript:\n║   " (str/replace js-code #"\n" "\\n") "\n"
         "╠════════════════════════════════════════════════════════════════╣\n"
         "║ Python:\n║   " (str/replace py-code #"\n" "\\n") "\n"
         "╚════════════════════════════════════════════════════════════════╝")))

(defn print-sample-pairs
  "Print sample pairs to console.
   
   Args:
   - pairs: sequence of pairs
   - count (optional): number of samples to show (default 3)"
  ([pairs]
   (print-sample-pairs pairs 3))
  ([pairs n]
   (println "\n=== SAMPLE OUTPUTS ===")
   (doseq [pair (take n pairs)]
     (println (format-pair-console pair)))))

;; ============================================================
;; STATISTICS
;; ============================================================

(defn print-pair-stats
  "Print statistics about generated pairs.
   
   Groups pairs by :type key (falls back to :form_type, :control_type)."
  [pairs]
  (println "\n=== PATTERN TYPES ===")
  (let [type-key (fn [pair]
                   (or (:type pair) (:form_type pair) (:control_type pair) "unknown"))
        grouped (group-by type-key pairs)]
    (doseq [[type type-pairs] (sort-by key grouped)]
      (println (str "  " type ": " (count type-pairs) " pairs")))))

;; ============================================================
;; GENERATION LOOP HELPER
;; ============================================================

(defn generate-pairs-loop
  "Generic loop for generating valid pairs.
   
   Args:
   - generate-fn: function that returns a candidate pair or nil
   - validate-fn: function that validates a pair (default: always true)
   - target-count: number of pairs to generate
   - max-attempts: maximum attempts before giving up (default: target-count * 10)
   
   Returns vector of valid pairs."
  ([generate-fn target-count]
   (generate-pairs-loop generate-fn (constantly true) target-count))
  ([generate-fn validate-fn target-count]
   (generate-pairs-loop generate-fn validate-fn target-count (* target-count 10)))
  ([generate-fn validate-fn target-count max-attempts]
   (println (str "Generating " target-count " pairs..."))
   (loop [pairs []
          attempts 0]
     (cond
       (>= (count pairs) target-count)
       (do
         (println (str "\n✓ Generated " (count pairs) " valid pairs"))
         (println (str "  Attempts: " attempts " (success rate: "
                      (format "%.1f" (* 100.0 (/ (count pairs) attempts))) "%)"))
         pairs)
       
       (>= attempts max-attempts)
       (do
         (println (str "\n⚠ Max attempts reached: " attempts))
         (println (str "  Generated: " (count pairs) " pairs"))
         pairs)
       
       :else
       (do
         (when (zero? (mod attempts 100))
           (println (str "  Progress: " (count pairs) "/" target-count
                        " (attempt " attempts ")")))
         (let [candidate (generate-fn)]
           (if (and candidate (validate-fn candidate))
             (recur (conj pairs (assoc candidate :id (inc (count pairs))))
                    (inc attempts))
             (recur pairs (inc attempts)))))))))