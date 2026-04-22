(ns scripts.generate-batch
  "Generates a batch of training patterns on demand.
   
   Designed for interactive training loops - outputs JSONL to stdout.
   
   Usage: lein exec -p src-training/scripts/generate_batch.clj [batch-size] [pattern-types...]
   
   Example:
     lein exec -p src-training/scripts/generate_batch.clj 10 control-flow
     lein exec -p src-training/scripts/generate_batch.clj 5 all"
  (:require [std.lang :as l]
            [std.json :as json]
            [clojure.string :as str]
            [training.util :as tu]
            [training.type-system :as ts]
            [training.control-flow :as cf]))

;; ============================================================
;; INITIALIZATION
;; ============================================================

(defn init-runtimes
  "Initialize std.lang runtimes"
  []
  (tu/init-runtimes))

;; ============================================================
;; BATCH GENERATION WITH PARAMETERS
;; ============================================================

(defn valid-pattern-type?
  "Check if pattern type is supported"
  [type]
  (contains? #{"control-flow" "expressions" "all"} type))

(defn validate-semantics
  "Basic semantic validation - check for obvious nonsense"
  [form]
  (let [s (pr-str form)]
    ;; Reject forms with obviously mismatched comparisons
    (not (re-find #"true\s*[<>]=?\s*\"|\"\s*[<>]=?\s*true|false\s*[<>]=?\s*\"|\"\s*[<>]=?\s*false" s))))

(defn generate-control-flow-pair
  "Generate a control flow training pair"
  []
  (let [form (cf/generate-control-flow-form)]
    (if (validate-semantics form)
      (let [result (tu/emit-to-both form)]
        (if (:valid result)
          (assoc result
                 :form_type (str (first form))
                 :source "control-flow")
          nil))
      nil)))

(defn generate-expression-pair
  "Generate an expression training pair"
  []
  (let [form (ts/generate-typed-expr :type :any :max-depth 3 :compound-prob 0.3)]
    (if (validate-semantics form)
      (let [result (tu/emit-to-both form)]
        (if (:valid result)
          (assoc result
                 :form_type "expression"
                 :source "expression")
          nil))
      nil)))

(defn generate-pair
  "Generate a single training pair based on requested type"
  [pattern-type]
  (case pattern-type
    "control-flow" (generate-control-flow-pair)
    "expressions" (generate-expression-pair)
    ;; default: random selection
    (if (< (rand) 0.5)
      (generate-control-flow-pair)
      (generate-expression-pair))))

(defn generate-batch
  "Generate a batch of training pairs"
  [batch-size pattern-types]
  (println (str "Generating batch of " batch-size " patterns...") *err*)
  (println (str "Pattern types: " (str/join ", " pattern-types)) *err*)
  
  (loop [pairs []
         attempts 0
         max-attempts (* batch-size 10)]
    (cond
      (>= (count pairs) batch-size)
      (do
        (println (str "✓ Generated " (count pairs) " valid pairs") *err*)
        pairs)
      
      (>= attempts max-attempts)
      (do
        (println (str "⚠ Only generated " (count pairs) " pairs after " max-attempts " attempts") *err*)
        pairs)
      
      :else
      (let [type (if (= pattern-types ["all"])
                   (rand-nth ["control-flow" "expressions"])
                   (rand-nth pattern-types))
            pair (generate-pair type)]
        (if pair
          (recur (conj pairs pair) (inc attempts) max-attempts)
          (recur pairs (inc attempts) max-attempts))))))

(defn pair->jsonl
  "Convert a pair to JSONL string"
  [pair]
  (json/write-str pair))

;; ============================================================
;; COMMAND-LINE INTERFACE
;; ============================================================

(defn -main
  "Main entry point for batch generation"
  [& args]
  (let [batch-size (if (seq args)
                     (try
                       (Integer/parseInt (first args))
                       (catch Exception e
                         (println "Error: Batch size must be an integer" *err*)
                         (System/exit 1)))
                     10)  ; default batch size
        pattern-types (if (> (count args) 1)
                        (rest args)
                        ["all"])  ; default: all types
        
        ;; Validate pattern types
        valid-types (filter valid-pattern-type? pattern-types)]
    
    (when (empty? valid-types)
      (println "Error: No valid pattern types specified. Valid types: control-flow, expressions, all" *err*)
      (System/exit 1))
    
    ;; Initialize runtimes
    (init-runtimes)
    
    ;; Generate batch
    (let [batch (generate-batch batch-size valid-types)]
      ;; Output JSONL to stdout
      (doseq [pair batch]
        (println (pair->jsonl pair))))))

;; Allow execution via lein exec
(definvoke -main
  {:added "1.0"}
  [& args]
  (apply -main args))