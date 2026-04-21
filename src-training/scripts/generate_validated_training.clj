(ns scripts.generate-validated-training
  "Generates training data by creating random valid DSL forms,
   emitting them to JS/Python, and validating with :basic runtime.
   
   Usage: lein exec -p src-training/scripts/generate_validated_training.clj [count]"
  (:require [std.lang :as l]
            [std.lib.json :as json]
            [clojure.string :as str])
  (:use code.test))

;; ============================================================
;; RANDOM DSL FORM GENERATORS
;; ============================================================

(def +primitive-values+
  "Primitive values for generation"
  [42 3.14 -10 0 100 999
   "hello" "world" "test" "abc" "xyz"
   true false
   nil])

(def +math-ops+
  "Math operators from grammar-spec"
  ['+ '- '* '/ 'mod 'pow])

(def +compare-ops+
  "Comparison operators"
  ['== 'not= '< '<= '> '>=])

(def +logic-ops+
  "Logic operators"
  ['not 'and 'or])

(def +xtalk-fns+
  "xt.lang.base-lib functions"
  ['k/clamp 'k/abs 'k/mix 'k/sign 'k/round
   'k/arr-map 'k/arr-filter 'k/arr-clone 'k/arr-concat
   'k/obj-keys 'k/obj-vals 'k/obj-pick 'k/obj-omit
   'k/starts-with? 'k/ends-with? 'k/capitalize
   'k/fn? 'k/arr? 'k/obj?])

(defn random-int
  "Generate random integer"
  [max-val]
  (rand-int max-val))

(defn random-bool
  "Generate random boolean"
  []
  (> (rand) 0.5))

(defn random-value
  "Generate a random primitive value"
  []
  (rand-nth +primitive-values+))

(defn random-array
  "Generate random array"
  []
  (vec (take (inc (rand-int 5)) (repeatedly random-value))))

(defn random-object
  "Generate random object"
  []
  (into {} (take (inc (rand-int 3))
                 (repeatedly #(vector (str "key" (rand-int 100))
                                      (random-value))))))

(defn random-xtalk-expr
  "Generate a random xtalk expression (max depth)"
  ([max-depth]
   (random-xtalk-expr max-depth 0))
  ([max-depth current-depth]
   (if (>= current-depth max-depth)
     (random-value)
     (case (rand-int 10)
       0 (random-value)
       1 (random-array)
       2 (random-object)
       3 (list (rand-nth +math-ops+)
               (random-xtalk-expr max-depth (inc current-depth))
               (random-xtalk-expr max-depth (inc current-depth)))
       4 (list (rand-nth +compare-ops+)
               (random-xtalk-expr max-depth (inc current-depth))
               (random-xtalk-expr max-depth (inc current-depth)))
       5 (list (rand-nth +logic-ops+)
               (random-xtalk-expr max-depth (inc current-depth))
               (random-xtalk-expr max-depth (inc current-depth)))
       6 (list 'k/clamp
               (random-int 100)
               (random-int 100)
               (random-int 100))
       7 (list 'k/abs (random-int 100))
       8 (list (rand-nth ['k/arr-map 'k/arr-filter])
               (random-array)
               '(fn [x] x))
       9 (list (rand-nth ['k/obj-keys 'k/obj-vals])
               (random-object))))))

(defn random-defn
  "Generate a random function definition"
  [idx]
  (let [fn-name (symbol (str "fn" idx))
        args (vec (take (rand-int 3)
                        (repeatedly #(symbol (str "arg" (rand-int 100))))))
        body (random-xtalk-expr 2)]
    {:type :defn
     :name fn-name
     :args args
     :body body
     :form `(~'defn.js ~fn-name ~args (~'return ~body))}))

(defn random-def
  "Generate a random variable definition"
  [idx]
  (let [var-name (symbol (str "var" idx))
        value (random-xtalk-expr 1)]
    {:type :def
     :name var-name
     :value value
     :form `(~'def.js ~var-name ~value)}))

;; ============================================================
;; VALIDATION WITH :BASIC RUNTIME
;; ============================================================

(defn emit-and-validate
  "Emit xtalk form to JS and Python, return both if valid"
  [form]
  (try
    (let [js-code (l/emit-as :js [form])
          py-code (l/emit-as :python [form])]
      {:valid true
       :xtalk (pr-str form)
       :js js-code
       :python py-code
       :error nil})
    (catch Exception e
      {:valid false
       :xtalk (pr-str form)
       :error (.getMessage e)})))

;; ============================================================
;; GENERATION PIPELINE
;; ============================================================

(defn generate-validated-pair
  "Generate a random form and validate it"
  [idx]
  (let [gen-type (rand-nth [:expr :def :defn])
        form-data (case gen-type
                    :expr {:type :expr :form (random-xtalk-expr 2)}
                    :def (random-def idx)
                    :defn (random-defn idx))
        result (emit-and-validate (:form form-data))]
    (when (:valid result)
      (assoc result
             :id (inc idx)
             :generation_type gen-type
             :intent (case gen-type
                      :expr "Random expression"
                      :def "Variable definition"
                      :defn "Function definition")))))

(defn generate-validated-batch
  "Generate a batch of validated training pairs"
  [target-count]
  (println (str "Generating " target-count " validated training pairs..."))
  (println "Using :basic runtime for validation")
  (println)
  
  (loop [pairs []
         idx 0
         attempts 0
         max-attempts (* target-count 10)]
    (cond
      ;; Success - got enough pairs
      (>= (count pairs) target-count)
      (do
        (println (str "\n✓ Generated " (count pairs) " valid pairs"))
        (println (str "  Total attempts: " attempts))
        (println (str "  Success rate: "
                     (format "%.1f%%" (* 100.0 (/ (count pairs) attempts)))))
        pairs)
      
      ;; Too many attempts
      (>= attempts max-attempts)
      (do
        (println (str "\n⚠ Reached max attempts after " attempts " tries"))
        (println (str "  Generated " (count pairs) " valid pairs"))
        pairs)
      
      ;; Continue generating
      :else
      (do
        (when (== 0 (mod attempts 100))
          (println (str "  Progress: " (count pairs) "/" target-count 
                       " valid (attempt " attempts ")")))
        (let [pair (generate-validated-pair idx)]
          (if pair
            (recur (conj pairs pair) (inc idx) (inc attempts))
            (recur pairs idx (inc attempts))))))))

;; ============================================================
;; OUTPUT
;; ============================================================

(defn pair->jsonl
  "Convert a pair to JSONL"
  [pair]
  (json/write-str
   {:id (:id pair)
    :type (str (:generation_type pair))
    :intent (:intent pair)
    :xtalk (:xtalk pair)
    :js (:js pair)
    :python (:python pair)}))

(defn pairs->jsonl
  "Convert pairs to JSONL"
  [pairs]
  (str/join "\n" (map pair->jsonl pairs)))

(defn format-pair-console
  "Format a pair for console display"
  [pair]
  (str "\n╔════════════════════════════════════════════════════════════════╗\n"
       "║ Pair #" (:id pair) " - " (:intent pair) "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ XTalk: " (:xtalk pair) "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ JS:    " (str/replace (:js pair) #"\n" "\\n") "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ Python: " (str/replace (:python pair) #"\n" "\\n") "\n"
       "╚════════════════════════════════════════════════════════════════╝"))

(defn -main
  [& args]
  (let [target-count (or (when (seq args)
                          (try (Integer/parseInt (first args))
                               (catch Exception _ 100)))
                        100)
        output-file "training/VALIDATED_PAIRS.jsonl"]
    
    (println "╔════════════════════════════════════════════════════════════════╗")
    (println "║     VALIDATED TRAINING DATA GENERATOR                         ║")
    (println "║     Random DSL Forms → Emit → Validate → Save                ║")
    (println "╚════════════════════════════════════════════════════════════════╝")
    (println)
    
    ;; Initialize runtimes for validation
    (try
      (l/script- :js
        {:runtime :basic
         :require [[xt.lang.base-lib :as k]]})
      (l/script- :python
        {:runtime :basic
         :require [[xt.lang.base-lib :as k]]})
      (println "✓ Runtimes initialized (JS and Python with :basic)")
      (println)
      
      ;; Generate pairs
      (let [pairs (generate-validated-batch target-count)
            jsonl-content (pairs->jsonl pairs)]
        
        ;; Write to file
        (spit output-file jsonl-content)
        (println (str "\n✓ Written to: " output-file))
        
        ;; Statistics
        (println "\n=== GENERATION STATISTICS ===")
        (let [by-type (group-by :generation_type pairs)]
          (doseq [[type type-pairs] (sort-by key by-type)]
            (println (str "  " type ": " (count type-pairs) " pairs"))))
        
        ;; Sample outputs
        (println "\n=== SAMPLE OUTPUTS ===")
        (doseq [pair (take 3 pairs)]
          (println (format-pair-console pair)))
        
        (println (str "\n✓ Generation complete! Total: " (count pairs) " validated pairs")))
      
      (catch Exception e
        (println (str "\n✗ Error initializing runtimes: " (.getMessage e)))
        (println "Make sure you're running in a REPL or environment with std.lang loaded.")))))

;; Run if executed directly
(apply -main *command-line-args*)
