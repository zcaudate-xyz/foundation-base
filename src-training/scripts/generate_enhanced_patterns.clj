(ns scripts.generate-enhanced-patterns
  "Generates enhanced training data using std.lang grammar specifications.
   
   Features:
   1. Type-consistent expression generation
   2. Semantic validity (no nonsense like 'true <= \"hello\"')
   3. Pattern-based on actual grammar operators
   4. Controlled nesting depth
   5. AST validation for semantic equivalence
   
   Usage: lein exec -p src-training/scripts/generate_enhanced_patterns.clj [count]"
  (:require [std.lang :as l]
            [std.json :as json]
            [clojure.string :as str]
            [std.lib :refer [definvoke]]
            [training.util :as tu]
            [training.type-system :as ts]
            [training.control-flow :as cf]))



;; ============================================================
;; VALIDATION AND EMIT
;; ============================================================



(defn validate-semantics
  "Basic semantic validation - check for obvious nonsense"
  [form]
  (let [s (pr-str form)]
    ;; Reject forms with obviously mismatched comparisons
    (not (re-find #"true\s*[<>]=?\s*\"|\"\s*[<>]=?\s*true|false\s*[<>]=?\s*\"|\"\s*[<>]=?\s*false" s))))

;; ============================================================
;; MAIN GENERATION
;; ============================================================

(defn generate-enhanced-pairs
  "Generate enhanced training pairs"
  [target-count]
  (println (str "Generating " target-count " enhanced training pairs..."))
  (println "Features: Type consistency, semantic validity, grammar patterns")
  (println)
  
  (loop [pairs []
         attempts 0
          max-attempts (* target-count 10)]  ; Higher success rate expected
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
        (when (== 0 (mod attempts 100))
          (println (str "  Progress: " (count pairs) "/" target-count 
                       " (attempt " attempts ")")))
        
        (let [form (cf/generate-control-flow-form)]
          (if (validate-semantics form)
            (let [result (tu/emit-to-both form)]
              (if (:valid result)
                (recur (conj pairs (assoc result
                                          :id (inc (count pairs))
                                          :form_type (str (first form))))
                       (inc attempts)
                       max-attempts)
                (recur pairs (inc attempts) max-attempts)))
            (recur pairs (inc attempts) max-attempts)))))))

;; ============================================================
;; OUTPUT
;; ============================================================



;; ============================================================
;; MAIN FUNCTION
;; ============================================================

(defn -main
  [& args]
  (let [target-count (or (when (seq args)
                          (try (Integer/parseInt (first args))
                               (catch Exception _ 100)))
                        100)
        output-file "training/ENHANCED_PATTERNS.jsonl"]
    
    (println "╔════════════════════════════════════════════════════════════════╗")
    (println "║     ENHANCED PATTERN GENERATOR                                ║")
    (println "║     Type-consistent, semantically valid patterns             ║")
    (println "╚════════════════════════════════════════════════════════════════╝")
    (println)
    
    (try
      ;; Initialize runtimes using utility
      (tu/init-runtimes)
      
      ;; Generate pairs
      (let [pairs (generate-enhanced-pairs target-count)]
        
        ;; Write output using utility
        (tu/write-jsonl-file pairs output-file true)
        
        ;; Statistics using utility
        (tu/print-pair-stats pairs)
        
        ;; Samples using utility
        (tu/print-sample-pairs pairs 5)
        
        (println (str "\n✓ Complete: " (count pairs) " enhanced pairs generated")))
      
      (catch Exception e
        (println (str "\n✗ Error: " (.getMessage e)))
        (println "Make sure std.lang is properly loaded.")))))

;; Run
(apply -main (rest *command-line-args*))