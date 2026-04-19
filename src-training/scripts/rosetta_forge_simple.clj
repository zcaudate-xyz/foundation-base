(ns scripts.rosetta-forge-simple
  "Phase II: The Rosetta Forge (Simple Version)
   Generates training pairs by directly using l/emit-as on xtalk forms.
   
   Usage: lein exec -p src-training/scripts/rosetta_forge_simple.clj [count] [output-file]"
  (:require [std.lang :as l]
            [std.lib.json :as json]
            [clojure.string :as str])
  (:use code.test))

;; ============================================================
;; CORE XTALK ATOMS (extracted from xt.lang.base-lib)
;; ============================================================

(def +xtalk-atoms+
  "Core xtalk functions organized by category"
  [
   ;; Math functions
   {:category :math :name "clamp" :xtalk '(k/clamp 0 100 50) 
    :intent "Clamp a value between min and max"}
   {:category :math :name "mix" :xtalk '(k/mix 0.0 1.0 0.5)
    :intent "Linear interpolation between two values"}
   {:category :math :name "sign" :xtalk '(k/sign -42)
    :intent "Get the sign of a number"}
   {:category :math :name "round" :xtalk '(k/round 3.7)
    :intent "Round a number to nearest integer"}
   {:category :math :name "abs" :xtalk '(k/abs -5)
    :intent "Calculate absolute value"}
   {:category :math :name "gcd" :xtalk '(k/gcd 48 18)
    :intent "Calculate greatest common divisor"}
   {:category :math :name "lcm" :xtalk '(k/lcm 4 6)
    :intent "Calculate least common multiple"}
   {:category :math :name "mod-pos" :xtalk '(k/mod-pos -1 5)
    :intent "Calculate positive modulo"}
   
   ;; Array functions
   {:category :array :name "arr-map" :xtalk '(k/arr-map [1 2 3] (fn [x] (* x 2)))
    :intent "Map a function over an array"}
   {:category :array :name "arr-filter" :xtalk '(k/arr-filter [1 2 3 4 5] (fn [x] (> x 2)))
    :intent "Filter array elements by predicate"}
   {:category :array :name "arr-slice" :xtalk '(k/arr-slice [1 2 3 4 5] 1 3)
    :intent "Extract a slice from an array"}
   {:category :array :name "arr-append" :xtalk '(k/arr-append [1 2] [3 4])
    :intent "Append two arrays"}
   {:category :array :name "arr-clone" :xtalk '(k/arr-clone [1 2 3])
    :intent "Clone an array"}
   {:category :array :name "arr-every" :xtalk '(k/arr-every [2 4 6] (fn [x] (== 0 (% x 2))))
    :intent "Check if all elements satisfy predicate"}
   {:category :array :name "arr-some" :xtalk '(k/arr-some [1 3 5] (fn [x] (== 0 (% x 2))))
    :intent "Check if any element satisfies predicate"}
   {:category :array :name "arr-keep" :xtalk '(k/arr-keep [1 nil 2 nil 3] (fn [x] x))
    :intent "Map and filter out nil values"}
   {:category :array :name "arr-range" :xtalk '(k/arr-range 5)
    :intent "Create a range array"}
   {:category :array :name "arr-lookup" :xtalk '(k/arr-lookup [{:id 1} {:id 2}])
    :intent "Convert array to lookup map by id"}
   
   ;; Object functions  
   {:category :object :name "obj-keys" :xtalk '(k/obj-keys {:a 1 :b 2 :c 3})
    :intent "Get all keys from an object"}
   {:category :object :name "obj-vals" :xtalk '(k/obj-vals {:a 1 :b 2})
    :intent "Get all values from an object"}
   {:category :object :name "obj-pairs" :xtalk '(k/obj-pairs {:x 10 :y 20})
    :intent "Convert object to key-value pairs"}
   {:category :object :name "obj-clone" :xtalk '(k/obj-clone {:a 1 :b 2})
    :intent "Clone an object"}
   {:category :object :name "obj-assign" :xtalk '(k/obj-assign {:a 1} {:b 2})
    :intent "Assign properties from one object to another"}
   {:category :object :name "obj-pick" :xtalk '(k/obj-pick {:a 1 :b 2 :c 3} ["a" "c"])
    :intent "Pick specific keys from an object"}
   {:category :object :name "obj-omit" :xtalk '(k/obj-omit {:a 1 :b 2 :c 3} ["b"])
    :intent "Omit specific keys from an object"}
   {:category :object :name "obj-del" :xtalk '(k/obj-del {:a 1 :b 2} "a")
    :intent "Delete a key from an object"}
   {:category :object :name "get-in" :xtalk '(k/get-in {:a {:b {:c 1}}} ["a" "b" "c"])
    :intent "Get nested value by path"}
   
   ;; String functions
   {:category :string :name "starts-with?" :xtalk '(k/starts-with? "hello world" "hello")
    :intent "Check if string starts with prefix"}
   {:category :string :name "ends-with?" :xtalk '(k/ends-with? "hello.txt" ".txt")
    :intent "Check if string ends with suffix"}
   {:category :string :name "capitalize" :xtalk '(k/capitalize "hello")
    :intent "Capitalize first character of string"}
   {:category :string :name "decapitalize" :xtalk '(k/decapitalize "HELLO")
    :intent "Decapitalize first character of string"}
   {:category :string :name "pad-left" :xtalk '(k/pad-left "42" 5 "0")
    :intent "Pad string on the left"}
   {:category :string :name "pad-right" :xtalk '(k/pad-right "hi" 5 " ")
    :intent "Pad string on the right"}
   {:category :string :name "split-long" :xtalk '(k/split-long "abcdefghij" 3)
    :intent "Split string into chunks of max length"}
   {:category :string :name "sym-name" :xtalk '(k/sym-name "my-ns/my-fn")
    :intent "Extract symbol name from namespaced symbol"}
   {:category :string :name "sym-ns" :xtalk '(k/sym-ns "my-ns/my-fn")
    :intent "Extract namespace from namespaced symbol"}
   
   ;; Type checking
   {:category :type :name "fn?" :xtalk '(k/fn? (fn [] 1))
    :intent "Check if value is a function"}
   {:category :type :name "arr?" :xtalk '(k/arr? [1 2 3])
    :intent "Check if value is an array"}
   {:category :type :name "obj?" :xtalk '(k/obj? {:a 1})
    :intent "Check if value is an object"}
   {:category :type :name "is-empty?" :xtalk '(k/is-empty? [])
    :intent "Check if value is empty"}
   {:category :type :name "not-empty?" :xtalk '(k/not-empty? [1 2])
    :intent "Check if value is not empty"}
   {:category :type :name "identity" :xtalk '(k/identity 42)
    :intent "Return input unchanged"}
   
   ;; Function utilities
   {:category :function :name "key-fn" :xtalk '((k/key-fn "name") {:name "Alice" :age 30})
    :intent "Create a key accessor function"}
   {:category :function :name "eq-fn" :xtalk '((k/eq-fn "active" true) {:active true})
    :intent "Create an equality predicate function"}
   ])

;; ============================================================
;; TRAINING PAIR GENERATION
;; ============================================================

(defn generate-variations
  "Generate multiple variations of the same atom with different arguments"
  [atom-def n]
  (for [i (range n)]
    (assoc atom-def :variation i)))

(defn emit-training-pair
  "Emit xtalk atom to JavaScript and Python"
  [atom-def]
  (try
    (let [xtalk-form (:xtalk atom-def)
          js-code (l/emit-as :js [xtalk-form])
          py-code (l/emit-as :python [xtalk-form])]
      {:intent (:intent atom-def)
       :category (str (:category atom-def))
       :function (:name atom-def)
       :xtalk (pr-str xtalk-form)
       :js js-code
       :python py-code
       :valid true})
    (catch Exception e
      {:intent (:intent atom-def)
       :category (str (:category atom-def))
       :function (:name atom-def)
       :xtalk (pr-str (:xtalk atom-def))
       :error (.getMessage e)
       :valid false})))

(defn generate-rosetta-bible
  "Generate the specified number of training pairs"
  [target-count]
  (let [base-count (count +xtalk-atoms+)
        variations-per-atom (max 1 (int (/ target-count base-count)))
        total-needed (* base-count variations-per-atom)
        ;; Generate variations
        all-atoms (mapcat #(generate-variations % variations-per-atom) +xtalk-atoms+)
        ;; Emit each one
        _ (println (str "Generating " (count all-atoms) " training pairs..."))
        pairs (doall (map-indexed 
                       (fn [idx atom-def]
                         (when (== 0 (mod (inc idx) 50))
                           (println (str "  Progress: " idx "/" (count all-atoms))))
                         (emit-training-pair atom-def))
                       all-atoms))
        valid-pairs (filter :valid pairs)
        result (take target-count valid-pairs)]
    (println (str "\n✓ Generated " (count valid-pairs) " valid pairs"))
    (println (str "✓ Returning " (count result) " pairs"))
    result))

;; ============================================================
;; OUTPUT FORMATTING
;; ============================================================

(defn pairs->jsonl
  "Convert pairs to JSONL format"
  [pairs]
  (str/join "\n"
            (map #(json/write-str %) pairs)))

(defn print-sample
  "Print a sample pair for verification"
  [pair]
  (println "\n╔══════════════════════════════════════════════════════════════╗")
  (println (str "║ Function: " (:function pair)))
  (println (str "║ Category: " (:category pair)))
  (println (str "║ Intent: " (:intent pair)))
  (println "╠══════════════════════════════════════════════════════════════╣")
  (println (str "║ XTalk:"))
  (println (str "║   " (:xtalk pair)))
  (println "╠══════════════════════════════════════════════════════════════╣")
  (println (str "║ JavaScript:"))
  (doseq [line (str/split (:js pair) #"\n")]
    (println (str "║   " line)))
  (println "╠══════════════════════════════════════════════════════════════╣")
  (println (str "║ Python:"))
  (doseq [line (str/split (:python pair) #"\n")]
    (println (str "║   " line)))
  (println "╚══════════════════════════════════════════════════════════════╝"))

(defn print-stats
  "Print generation statistics"
  [pairs]
  (let [by-category (group-by :category pairs)
        valid-count (count (filter :valid pairs))
        invalid-count (count (remove :valid pairs))]
    (println "\n=== GENERATION STATISTICS ===")
    (println (str "Total pairs: " (count pairs)))
    (println (str "Valid: " valid-count))
    (println (str "Invalid: " invalid-count))
    (println "\nBy category:")
    (doseq [[cat cat-pairs] (sort-by key by-category)]
      (println (str "  " cat ": " (count cat-pairs) " pairs")))))

;; ============================================================
;; MAIN
;; ============================================================

(defn -main
  [& args]
  (let [target-count (or (when (seq args)
                           (try (Integer/parseInt (first args))
                                (catch Exception _ 1000)))
                         1000)
        output-file (or (second args)
                        "training/ROSETTA_BIBLE.jsonl")]
    
    (println "╔══════════════════════════════════════════════════════════════╗")
    (println "║             ROSSETTA FORGE - TRAINING GENERATOR              ║")
    (println "║         Phase II: Synthetic Data Generation                  ║")
    (println "╚══════════════════════════════════════════════════════════════╝")
    (println (str "\nTarget: " target-count " pairs"))
    (println (str "Output: " output-file "\n"))
    
    ;; Generate pairs
    (let [pairs (generate-rosetta-bible target-count)
          jsonl (pairs->jsonl pairs)]
      
      ;; Write output
      (spit output-file jsonl)
      (println (str "\n✓ Written to: " output-file))
      
      ;; Print stats
      (print-stats pairs)
      
      ;; Print samples
      (println "\n=== SAMPLE OUTPUTS ===")
      (doseq [pair (take 3 pairs)]
        (print-sample pair))
      
      ;; Print category breakdown
      (println "\n=== CATEGORY BREAKDOWN ===")
      (doseq [[cat cat-pairs] (sort-by key (group-by :category pairs))]
        (let [sample-fn (->> cat-pairs first :function)]
          (println (str cat ": " (count cat-pairs) " pairs (e.g., " sample-fn ")"))))
      
      (println "\n✓ Generation complete!"))))

;; Run if executed directly
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
