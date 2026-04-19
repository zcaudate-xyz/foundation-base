(ns scripts.generate-training-pairs
  "Generates 1000 training pairs for JS and Python from xtalk atoms.
   
   This script must be run within a REPL or environment where xt.lang
   namespaces are already loaded.
   
   Usage:
     lein repl
     (load-file \"src-training/scripts/generate_training_pairs.clj\")
     (generate-1000-pairs)"
  (:require [std.lang :as l]
            [std.lib.json :as json]
            [clojure.string :as str]
            [xt.lang.base-lib :as k])
  (:use code.test))

;; ============================================================
;; ATOM DEFINITIONS
;; ============================================================

(def +training-atoms+
  "Core xtalk atoms for training data generation"
  [
   ;; Math functions
   {:category :math :name "clamp" :fn 'k/clamp :args [0 100 50]
    :intent "Clamp a value between min and max"}
   {:category :math :name "mix" :fn 'k/mix :args [0.0 1.0 0.5]
    :intent "Linear interpolation between two values"}
   {:category :math :name "sign" :fn 'k/sign :args [-42]
    :intent "Get the sign of a number"}
   {:category :math :name "round" :fn 'k/round :args [3.7]
    :intent "Round a number to nearest integer"}
   {:category :math :name "abs" :fn 'k/abs :args [-5]
    :intent "Calculate absolute value"}
   {:category :math :name "gcd" :fn 'k/gcd :args [48 18]
    :intent "Calculate greatest common divisor"}
   {:category :math :name "lcm" :fn 'k/lcm :args [4 6]
    :intent "Calculate least common multiple"}
   {:category :math :name "mod-pos" :fn 'k/mod-pos :args [-1 5]
    :intent "Calculate positive modulo"}
   
   ;; Array functions
   {:category :array :name "arr-map" :fn 'k/arr-map 
    :args [[1 2 3] '(fn [x] (* x 2))]
    :intent "Map a function over an array"}
   {:category :array :name "arr-filter" :fn 'k/arr-filter
    :args [[1 2 3 4 5] '(fn [x] (> x 2))]
    :intent "Filter array elements by predicate"}
   {:category :array :name "arr-slice" :fn 'k/arr-slice :args [[1 2 3 4 5] 1 3]
    :intent "Extract a slice from an array"}
   {:category :array :name "arr-append" :fn 'k/arr-append :args [[1 2] [3 4]]
    :intent "Append two arrays"}
   {:category :array :name "arr-clone" :fn 'k/arr-clone :args [[1 2 3]]
    :intent "Clone an array"}
   {:category :array :name "arr-every" :fn 'k/arr-every
    :args [[2 4 6] '(fn [x] (== 0 (% x 2)))]
    :intent "Check if all elements satisfy predicate"}
   {:category :array :name "arr-some" :fn 'k/arr-some
    :args [[1 3 5] '(fn [x] (== 0 (% x 2)))]
    :intent "Check if any element satisfies predicate"}
   {:category :array :name "arr-range" :fn 'k/arr-range :args [5]
    :intent "Create a range array"}
   
   ;; Object functions  
   {:category :object :name "obj-keys" :fn 'k/obj-keys :args [{:a 1 :b 2 :c 3}]
    :intent "Get all keys from an object"}
   {:category :object :name "obj-vals" :fn 'k/obj-vals :args [{:a 1 :b 2}]
    :intent "Get all values from an object"}
   {:category :object :name "obj-pairs" :fn 'k/obj-pairs :args [{:x 10 :y 20}]
    :intent "Convert object to key-value pairs"}
   {:category :object :name "obj-clone" :fn 'k/obj-clone :args [{:a 1 :b 2}]
    :intent "Clone an object"}
   {:category :object :name "obj-assign" :fn 'k/obj-assign :args [{:a 1} {:b 2}]
    :intent "Assign properties from one object to another"}
   {:category :object :name "obj-pick" :fn 'k/obj-pick :args [{:a 1 :b 2 :c 3} ["a" "c"]]
    :intent "Pick specific keys from an object"}
   {:category :object :name "obj-omit" :fn 'k/obj-omit :args [{:a 1 :b 2 :c 3} ["b"]]
    :intent "Omit specific keys from an object"}
   {:category :object :name "obj-del" :fn 'k/obj-del :args [{:a 1 :b 2} "a"]
    :intent "Delete a key from an object"}
   {:category :object :name "get-in" :fn 'k/get-in :args [{:a {:b {:c 1}}} ["a" "b" "c"]]
    :intent "Get nested value by path"}
   
   ;; String functions
   {:category :string :name "starts-with?" :fn 'k/starts-with? :args ["hello world" "hello"]
    :intent "Check if string starts with prefix"}
   {:category :string :name "ends-with?" :fn 'k/ends-with? :args ["hello.txt" ".txt"]
    :intent "Check if string ends with suffix"}
   {:category :string :name "capitalize" :fn 'k/capitalize :args ["hello"]
    :intent "Capitalize first character of string"}
   {:category :string :name "decapitalize" :fn 'k/decapitalize :args ["HELLO"]
    :intent "Decapitalize first character of string"}
   {:category :string :name "pad-left" :fn 'k/pad-left :args ["42" 5 "0"]
    :intent "Pad string on the left"}
   {:category :string :name "pad-right" :fn 'k/pad-right :args ["hi" 5 " "]
    :intent "Pad string on the right"}
   {:category :string :name "sym-name" :fn 'k/sym-name :args ["my-ns/my-fn"]
    :intent "Extract symbol name from namespaced symbol"}
   
   ;; Type checking
   {:category :type :name "fn?-true" :fn 'k/fn? :args ['(fn [] 1)]
    :intent "Check if value is a function"}
   {:category :type :name "fn?-false" :fn 'k/fn? :args [42]
    :intent "Check if value is not a function"}
   {:category :type :name "arr?" :fn 'k/arr? :args [[1 2 3]]
    :intent "Check if value is an array"}
   {:category :type :name "obj?" :fn 'k/obj? :args [{:a 1}]
    :intent "Check if value is an object"}
   {:category :type :name "is-empty?" :fn 'k/is-empty? :args [[]]
    :intent "Check if value is empty"}
   {:category :type :name "not-empty?" :fn 'k/not-empty? :args [[1 2]]
    :intent "Check if value is not empty"}
   {:category :type :name "identity" :fn 'k/identity :args [42]
    :intent "Return input unchanged"}
   ])

;; ============================================================
;; GENERATION LOGIC
;; ============================================================

(defn emit-pair
  "Emit a single training pair for an atom"
  [atom-def]
  (try
    (let [form (cons (:fn atom-def) (:args atom-def))
          js-code (l/emit-as :js [form])
          py-code (l/emit-as :python [form])]
      {:intent (:intent atom-def)
       :category (str (:category atom-def))
       :function (:name atom-def)
       :xtalk (pr-str form)
       :js js-code
       :python py-code
       :valid true})
    (catch Exception e
      {:intent (:intent atom-def)
       :category (str (:category atom-def))
       :function (:name atom-def)
       :xtalk (pr-str (cons (:fn atom-def) (:args atom-def)))
       :error (.getMessage e)
       :valid false})))

(defn generate-variations
  "Generate multiple pairs per atom with slight variations"
  [atom-def count]
  (for [i (range count)]
    (emit-pair atom-def)))

(defn generate-all-pairs
  "Generate all training pairs"
  [total-count]
  (let [atom-count (count +training-atoms+)
        variations-per-atom (max 1 (int (/ total-count atom-count)))
        _ (println (str "Generating " total-count " pairs..."))
        _ (println (str "  Atoms: " atom-count))
        _ (println (str "  Variations per atom: " variations-per-atom))
        pairs (doall
               (mapcat (fn [atom]
                         (generate-variations atom variations-per-atom))
                       +training-atoms+))
        valid-pairs (filter :valid pairs)]
    (println (str "\nGenerated: " (count pairs) " total"))
    (println (str "Valid: " (count valid-pairs)))
    (take total-count valid-pairs)))

;; ============================================================
;; OUTPUT
;; ============================================================

(defn format-pair-for-display
  "Format a pair for console output"
  [pair]
  (str "\n" 
       "┌─────────────────────────────────────────────────────────────┐\n"
       "│ " (:function pair) "\n"
       "│ Category: " (:category pair) "\n"
       "│ Intent: " (:intent pair) "\n"
       "├─────────────────────────────────────────────────────────────┤\n"
       "│ XTalk: " (:xtalk pair) "\n"
       "├─────────────────────────────────────────────────────────────┤\n"
       "│ JS:\n"
       (str/join "\n" (map #(str "│   " %) (str/split (:js pair) #"\n"))) "\n"
       "├─────────────────────────────────────────────────────────────┤\n"
       "│ Python:\n"
       (str/join "\n" (map #(str "│   " %) (str/split (:python pair) #"\n"))) "\n"
       "└─────────────────────────────────────────────────────────────┘"))

(defn pairs->jsonl
  "Convert pairs to JSONL"
  [pairs]
  (str/join "\n" (map json/write-str pairs)))

;; ============================================================
;; MAIN ENTRY POINT
;; ============================================================

(defn generate-1000-pairs
  "Main function to generate 1000 training pairs"
  []
  (let [pairs (generate-all-pairs 1000)
        jsonl (pairs->jsonl pairs)
        output-file "training/ROSETTA_BIBLE_1000.jsonl"]
    
    ;; Write to file
    (spit output-file jsonl)
    
    ;; Print summary
    (println "\n╔═══════════════════════════════════════════════════════════════╗")
    (println "║           ROSSETTA FORGE - GENERATION COMPLETE               ║")
    (println "╚═══════════════════════════════════════════════════════════════╝")
    (println (str "\n✓ Generated: " (count pairs) " training pairs"))
    (println (str "✓ Output: " output-file))
    
    ;; Category breakdown
    (println "\n=== CATEGORY BREAKDOWN ===")
    (doseq [[cat cat-pairs] (sort-by key (group-by :category pairs))]
      (println (str "  " cat ": " (count cat-pairs) " pairs")))
    
    ;; Print 3 samples
    (println "\n=== SAMPLE OUTPUTS ===")
    (doseq [pair (take 3 pairs)]
      (println (format-pair-for-display pair)))
    
    ;; Return pairs for REPL use
    pairs))

;; Auto-run if loaded directly
(println "\n═══════════════════════════════════════════════════════════════")
(println "  Rosetta Forge Training Generator Loaded")
(println "═══════════════════════════════════════════════════════════════")
(println "\nTo generate 1000 training pairs, run:")
(println "  (generate-1000-pairs)")
(println "\nOr for a custom count:")
(println "  (generate-all-pairs 500)")
