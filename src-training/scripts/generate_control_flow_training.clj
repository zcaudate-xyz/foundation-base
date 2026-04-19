(ns scripts.generate-control-flow-training
  "Generates training data for std.lang control flow constructs.
   
   Focus: Grammar operators (if/when/cond/for/while/try/etc.)
   NOT library calls (no k/* functions)
   
   Usage: lein exec -p src-training/scripts/generate_control_flow_training.clj [count]"
  (:require [std.lang :as l]
            [std.json :as json]
            [clojure.string :as str])
  (:use code.test))

;; ============================================================
;; CONTROL FLOW FORM GENERATORS
;; Based on std.lang.base.grammar-spec +op-control-* definitions
;; ============================================================

(def +comparison-ops+
  "Comparison operators"
  ['== 'not= '< '<= '> '>=])

(def +logic-ops+
  "Logic operators"
  ['and 'or])

(def +math-ops+
  "Basic math operators"
  ['+ '- '*])

(defn random-value
  "Generate a random primitive value"
  []
  (rand-nth [0 1 2 5 10 42 100 -1 -10
             true false
             "hello" "world" "test"
             nil]))

(defn random-symbol
  "Generate a random symbol name"
  []
  (symbol (rand-nth ["x" "y" "n" "i" "val" "result" "acc" "item"])))

(defn random-expr
  "Generate a simple expression (depth-limited)"
  ([max-depth]
   (random-expr max-depth 0))
  ([max-depth depth]
   (if (>= depth max-depth)
     (rand-nth [(random-value) (random-symbol)])
     (case (rand-int 4)
       0 (random-value)
       1 (random-symbol)
       2 (list (rand-nth +math-ops+)
               (random-expr max-depth (inc depth))
               (random-expr max-depth (inc depth)))
       3 (list (rand-nth +comparison-ops+)
               (random-expr max-depth (inc depth))
               (random-expr max-depth (inc depth)))))))

;; ============================================================
;; CONTROL FLOW CONSTRUCTS
;; ============================================================

(defn gen-if
  "Generate if expression: (if cond then else)"
  []
  (let [cond (random-expr 1)
        then (random-expr 1)
        else (random-expr 1)]
    {:type :if
     :form `(~'if ~cond ~then ~else)
     :description "If-else conditional"}))

(defn gen-when
  "Generate when expression: (when cond body...)"
  []
  (let [cond (random-expr 1)
        body (take (inc (rand-int 3)) (repeatedly #(random-expr 1)))]
    {:type :when
     :form `(~'when ~cond ~@body)
     :description "When conditional (single branch)"}))

(defn gen-cond
  "Generate cond expression: (cond c1 r1 c2 r2 :else r3)"
  []
  (let [pairs (take (inc (rand-int 3))
                    (repeatedly #(vector (random-expr 1) (random-expr 1))))
        else-case (random-expr 1)
        clauses (concat (mapcat identity pairs) [:else else-case])]
    {:type :cond
     :form `(~'cond ~@clauses)
     :description "Cond multi-way conditional"}))

(defn gen-ternary
  "Generate ternary expression: (:? cond then else)"
  []
  (let [cond (random-expr 1)
        then (random-expr 1)
        else (random-expr 1)]
    {:type :ternary
     :form `(~':? ~cond ~then ~else)
     :description "Ternary conditional operator"}))

(defn gen-for-loop
  "Generate for loop: (for [init test update] body)"
  []
  (let [var-sym (random-symbol)
        init `(~'var ~var-sym := 0)
        test `(~< ~var-sym 10)
        update `[(:= ~var-sym (~'+ ~var-sym 1))]
        body (random-expr 1)]
    {:type :for
     :form `(~'for [~init ~test ~update] ~body)
     :description "For loop with init/test/update"}))

(defn gen-while-loop
  "Generate while loop: (while cond body...)"
  []
  (let [cond (random-expr 1)
        body (take (inc (rand-int 2)) (repeatedly #(random-expr 1)))]
    {:type :while
     :form `(~'while ~cond ~@body)
     :description "While loop"}))

(defn gen-do-block
  "Generate do block: (do expr1 expr2 ...)"
  []
  (let [exprs (take (inc (rand-int 3)) (repeatedly #(random-expr 1)))]
    {:type :do
     :form `(~'do ~@exprs)
     :description "Do block (sequential evaluation)"}))

(defn gen-do-star
  "Generate do* block: (do* expr1 expr2 ...)"
  []
  (let [exprs (take (inc (rand-int 3)) (repeatedly #(random-expr 1)))]
    {:type :do*
     :form `(~'do* ~@exprs)
     :description "Do* block (statements)"}))

(defn gen-let
  "Generate let binding: (let [x 10 y 20] body)"
  []
  (let [bindings (vec (mapcat #(vector % (random-value))
                              (take (inc (rand-int 3))
                                    (repeatedly random-symbol))))
        body (random-expr 1)]
    {:type :let
     :form `(~'let ~bindings ~body)
     :description "Let binding"}))

(defn gen-fn-anon
  "Generate anonymous function: (fn [args] body)"
  []
  (let [args (vec (take (rand-int 3) (repeatedly random-symbol)))
        body `(~'return ~(random-expr 1))]
    {:type :fn
     :form `(~'fn ~args ~body)
     :description "Anonymous function"}))

(defn gen-arrow-fn
  "Generate arrow function: (fn:> [args] body)"
  []
  (let [args (vec (take (rand-int 3) (repeatedly random-symbol)))
        body (random-expr 1)]
    {:type :fn-arrow
     :form `(~'fn:> ~args ~body)
     :description "Arrow function (implicit return)"}))

(defn gen-assignment
  "Generate assignment: (:= var value)"
  []
  (let [var (random-symbol)
        val (random-expr 1)]
    {:type :assign
     :form `(~':= ~var ~val)
     :description "Assignment"}))

(defn gen-var-decl
  "Generate var declaration: (var x := value)"
  []
  (let [var (random-symbol)
        val (random-expr 1)]
    {:type :var
     :form `(~'var ~var := ~val)
     :description "Variable declaration"}))

(defn gen-counter
  "Generate counter op: (:+= x 1)"
  []
  (let [op (rand-nth [':+= ':-=])
        var (random-symbol)
        amt (rand-nth [1 2 5 10])]
    {:type :counter
     :form `(~op ~var ~amt)
     :description "Counter operation"}))

(defn gen-return
  "Generate return: (return value)"
  []
  (let [val (random-expr 1)]
    {:type :return
     :form `(~'return ~val)
     :description "Return statement"}))

(defn gen-defn
  "Generate function definition: (defn name [args] body)"
  []
  (let [name (symbol (str "f" (rand-int 1000)))
        args (vec (take (rand-int 3) (repeatedly random-symbol)))
        body `(~'return ~(random-expr 1))]
    {:type :defn
     :form `(~'defn ~name ~args ~body)
     :description "Function definition"}))

(defn gen-def
  "Generate variable definition: (def name value)"
  []
  (let [name (symbol (str "v" (rand-int 1000)))
        val (random-expr 1)]
    {:type :def
     :form `(~'def ~name ~val)
     :description "Top-level definition"}))

(defn gen-not
  "Generate not expression: (not x)"
  []
  {:type :not
   :form `(~'not ~(random-expr 1))
   :description "Logical not"})

;; ============================================================
;; GENERATOR REGISTRY
;; ============================================================

(def +generators+
  "All control flow generators"
  [#'gen-if
   #'gen-when
   #'gen-cond
   #'gen-ternary
   #'gen-for-loop
   #'gen-while-loop
   #'gen-do-block
   #'gen-do-star
   #'gen-let
   #'gen-fn-anon
   #'gen-arrow-fn
   #'gen-assignment
   #'gen-var-decl
   #'gen-counter
   #'gen-return
   #'gen-defn
   #'gen-def
   #'gen-not])

(defn random-control-flow-form
  "Generate a random control flow form"
  []
  (let [gen-fn (rand-nth +generators+)]
    (gen-fn)))

;; ============================================================
;; VALIDATION AND EMIT
;; ============================================================

(defn emit-to-both
  "Emit form to both JS and Python"
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
       :error (.getMessage e)})))

;; ============================================================
;; MAIN GENERATION
;; ============================================================

(defn generate-control-flow-pairs
  "Generate validated control flow training pairs"
  [target-count]
  (println (str "Generating " target-count " control flow training pairs..."))
  (println "Focus: Grammar operators only (no library calls)")
  (println)
  
  (loop [pairs []
         attempts 0
         max-attempts (* target-count 5)]
    (cond
      (>= (count pairs) target-count)
      (do
        (println (str "\n✓ Generated " (count pairs) " valid pairs"))
        (println (str "  Attempts: " attempts))
        pairs)
      
      (>= attempts max-attempts)
      (do
        (println (str "\n⚠ Max attempts reached: " attempts))
        (println (str "  Generated: " (count pairs) " pairs"))
        pairs)
      
      :else
      (do
        (when (== 0 (mod attempts 50))
          (println (str "  Progress: " (count pairs) "/" target-count 
                       " (attempt " attempts ")")))
        
        (let [form-data (random-control-flow-form)
              result (emit-to-both (:form form-data))]
           (if (:valid result)
             (recur (conj pairs (assoc result
                                       :id (inc (count pairs))
                                       :control_type (:type form-data)
                                       :description (:description form-data)))
                    (inc attempts)
                    max-attempts)
             (recur pairs (inc attempts) max-attempts)))))))

;; ============================================================
;; OUTPUT
;; ============================================================

(defn pair->jsonl
  "Convert pair to JSONL"
  [pair]
  (json/write
   {:id (:id pair)
    :control_type (str (:control_type pair))
    :description (:description pair)
    :xtalk (:xtalk pair)
    :js (:js pair)
    :python (:python pair)}))

(defn pairs->jsonl
  "Convert all pairs to JSONL"
  [pairs]
  (str/join "\n" (map pair->jsonl pairs)))

(defn format-pair-console
  "Format pair for display"
  [pair]
  (str "\n╔════════════════════════════════════════════════════════════════╗\n"
       "║ Pair #" (:id pair) " - " (:description pair) "\n"
       "║ Type: " (:control_type pair) "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ XTalk:\n║   " (:xtalk pair) "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ JavaScript:\n║   " (str/replace (:js pair) #"\n" "\\n") "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ Python:\n║   " (str/replace (:python pair) #"\n" "\\n") "\n"
       "╚════════════════════════════════════════════════════════════════╝"))

(defn -main
  [& args]
  (let [target-count (or (when (seq args)
                          (try (Integer/parseInt (first args))
                               (catch Exception _ 100)))
                        100)
        output-file "training/CONTROL_FLOW_PAIRS.jsonl"]
    
    (println "╔════════════════════════════════════════════════════════════════╗")
    (println "║     CONTROL FLOW TRAINING DATA GENERATOR                      ║")
    (println "║     Grammar Operators: if/when/cond/for/while/etc.           ║")
    (println "╚════════════════════════════════════════════════════════════════╝")
    (println)
    
    (try
      ;; Initialize runtimes
      (l/script- :js {:runtime :basic})
      (l/script- :python {:runtime :basic})
      (println "✓ Runtimes initialized")
      (println)
      
      ;; Generate pairs
      (let [pairs (generate-control-flow-pairs target-count)
            jsonl-content (pairs->jsonl pairs)]
        
        ;; Write output
        (spit output-file jsonl-content)
        (println (str "\n✓ Written to: " output-file))
        
        ;; Statistics
        (println "\n=== CONTROL FLOW TYPES ===")
        (doseq [[type type-pairs] (sort-by key (group-by :control_type pairs))]
          (println (str "  " type ": " (count type-pairs))))
        
        ;; Samples
        (println "\n=== SAMPLE OUTPUTS ===")
        (doseq [pair (take 5 pairs)]
          (println (format-pair-console pair)))
        
        (println (str "\n✓ Complete: " (count pairs) " control flow pairs generated")))
      
      (catch Exception e
        (println (str "\n✗ Error: " (.getMessage e)))
        (println "Make sure std.lang is properly loaded.")))))

;; Run
(apply -main *command-line-args*)
