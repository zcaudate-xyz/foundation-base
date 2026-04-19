(ns scripts.generate-llm-training-data
  "Comprehensive LLM training data generator for std.lang.
   
   Generates organized, LLM-compatible training datasets:
   - JSONL format (standard)
   - Alpaca format (instruction-following)
   - ShareGPT format (conversational)
   - Raw text format (pre-training)
   
   Organizes by category for easy filtering and curriculum learning.
   
   Usage: lein exec -p src-training/scripts/generate_llm_training_data.clj [options]"
  (:require [std.lang :as l]
            [std.json :as json]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:use code.test))

;; ============================================================
;; CONFIGURATION
;; ============================================================

(def +config+
  {:output-dir "training/llm"
   :splits {:train 0.8 :val 0.1 :test 0.1}
   :formats [:jsonl :alpaca :sharegpt :raw]
   :categories [:control-flow :grammar-spec :macros :xtalk-primitives :complete]
   :validation-threshold 5})

;; ============================================================
;; GENERATORS - Control Flow
;; ============================================================

(def +comparison-ops+ ['== 'not= '< '<= '> '>=])
(def +logic-ops+ ['not 'and 'or])
(def +math-ops+ ['+ '- '* '/])
(def +counter-ops+ [':+= ':-=])

(defn random-value []
  (rand-nth [0 1 2 5 10 42 100 -1 -10 true false nil "hello" "world" "test"]))

(defn random-symbol []
  (rand-nth ['x 'y 'n 'i 'val 'result 'acc 'item 'tmp]))

(defn random-expr [max-depth depth]
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
              (random-expr max-depth (inc depth))))))

;; Control flow generators
(defn gen-if []
  (let [cond (random-expr 2 0)
        then (random-expr 1 0)
        else (random-expr 1 0)]
    {:type :if
     :form `(if ~cond ~then ~else)
     :instruction "Write an if-else conditional statement"
     :category :control-flow}))

(defn gen-when []
  (let [cond (random-expr 2 0)
        body (take (inc (rand-int 2)) (repeatedly #(random-expr 1 0)))]
    {:type :when
     :form `(when ~cond ~@body)
     :instruction "Write a when statement (conditional without else)"
     :category :control-flow}))

(defn gen-cond []
  (let [pairs (take (inc (rand-int 2))
                    (repeatedly #(vector (random-expr 2 0) (random-expr 1 0))))
        else-case (random-expr 1 0)
        clauses (concat (mapcat identity pairs) [:else else-case])]
    {:type :cond
     :form `(cond ~@clauses)
     :instruction "Write a multi-way conditional using cond"
     :category :control-flow}))

(defn gen-ternary []
  (let [cond (random-expr 2 0)
        then (random-expr 1 0)
        else (random-expr 1 0)]
    {:type :ternary
     :form `(~':? ~cond ~then ~else)
     :instruction "Write a ternary conditional expression"
     :category :control-flow}))

(defn gen-for-loop []
  (let [var-sym (random-symbol)
        init `(var ~var-sym := 0)
        test `(< ~var-sym ~(rand-nth [5 10 20]))
        update `[(:= ~var-sym (+ ~var-sym 1))]
        body (random-expr 1 0)]
    {:type :for
     :form `(for [~init ~test ~update] ~body)
     :instruction "Write a for loop with initialization, condition, and update"
     :category :control-flow}))

(defn gen-while []
  (let [cond (random-expr 2 0)
        body (take (inc (rand-int 2)) (repeatedly #(random-expr 1 0)))]
    {:type :while
     :form `(while ~cond ~@body)
     :instruction "Write a while loop"
     :category :control-flow}))

(defn gen-do []
  (let [exprs (take (inc (rand-int 3)) (repeatedly #(random-expr 1 0)))]
    {:type :do
     :form `(do ~@exprs)
     :instruction "Write a do block for sequential evaluation"
     :category :control-flow}))

(defn gen-let []
  (let [bindings (vec (mapcat #(vector % (random-value))
                              (take (inc (rand-int 2)) (repeatedly random-symbol))))
        body (random-expr 1 0)]
    {:type :let
     :form `(let ~bindings ~body)
     :instruction "Write a let binding to declare local variables"
     :category :control-flow}))

(defn gen-fn []
  (let [args (vec (take (rand-int 2) (repeatedly random-symbol)))
        body `(return ~(random-expr 1 0))]
    {:type :fn
     :form `(fn ~args ~body)
     :instruction "Write an anonymous function"
     :category :control-flow}))

(defn gen-arrow-fn []
  (let [args (vec (take (rand-int 2) (repeatedly random-symbol)))
        body (random-expr 1 0)]
    {:type :fn-arrow
     :form `(fn:> ~args ~body)
     :instruction "Write an arrow function with implicit return"
     :category :control-flow}))

(defn gen-assignment []
  (let [var (random-symbol)
        val (random-expr 1 0)]
    {:type :assign
     :form `(:= ~var ~val)
     :instruction "Write an assignment statement"
     :category :control-flow}))

(defn gen-var-decl []
  (let [var (random-symbol)
        val (random-expr 1 0)]
    {:type :var
     :form `(var ~var := ~val)
     :instruction "Write a variable declaration"
     :category :control-flow}))

(defn gen-counter []
  (let [op (rand-nth [':+= ':-=])
        var (random-symbol)
        amt (rand-nth [1 2 5 10])]
    {:type :counter
     :form `(~op ~var ~amt)
     :instruction "Write a counter operation (increment/decrement)"
     :category :control-flow}))

(defn gen-return []
  (let [val (random-expr 1 0)]
    {:type :return
     :form `(return ~val)
     :instruction "Write a return statement"
     :category :control-flow}))

(defn gen-def []
  (let [name (symbol (str "v" (rand-int 1000)))
        val (random-expr 1 0)]
    {:type :def
     :form `(def ~name ~val)
     :instruction "Write a top-level variable definition"
     :category :control-flow}))

(defn gen-defn []
  (let [name (symbol (str "f" (rand-int 1000)))
        args (vec (take (rand-int 2) (repeatedly random-symbol)))
        body `(return ~(random-expr 1 0))]
    {:type :defn
     :form `(defn ~name ~args ~body)
     :instruction "Write a function definition"
     :category :control-flow}))

(defn gen-not []
  {:type :not
   :form `(not ~(random-expr 1 0))
   :instruction "Write a logical negation"
   :category :control-flow})

(def +generators+
  [#'gen-if #'gen-when #'gen-cond #'gen-ternary
   #'gen-for-loop #'gen-while #'gen-do #'gen-let
   #'gen-fn #'gen-arrow-fn #'gen-assignment #'gen-var-decl
   #'gen-counter #'gen-return #'gen-def #'gen-defn #'gen-not])

(defn random-control-flow []
  (let [gen-fn (rand-nth +generators+)]
    (gen-fn)))

;; ============================================================
;; EMIT AND VALIDATE
;; ============================================================

(defn emit-to-both [form]
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
;; GENERATION PIPELINE
;; ============================================================

(defn generate-batch
  "Generate a batch of validated training pairs"
  [target-count]
  (println (str "\nGenerating " target-count " training pairs..."))
  
  (loop [pairs []
         attempts 0
         max-attempts (* target-count (:validation-threshold +config+))]
    (cond
      (>= (count pairs) target-count)
      (do
        (println (str "✓ Generated " (count pairs) " valid pairs (" attempts " attempts)"))
        pairs)
      
      (>= attempts max-attempts)
      (do
        (println (str "⚠ Max attempts reached: " attempts " attempts, " (count pairs) " pairs"))
        pairs)
      
      :else
      (do
        (when (== 0 (mod attempts 100))
          (print ".") (flush))
        
        (let [form-data (random-control-flow)
              result (emit-to-both (:form form-data))]
          (if (:valid result)
            (recur (conj pairs (merge result
                                      (select-keys form-data [:type :instruction :category])))
                   (inc attempts)
                   max-attempts)
            (recur pairs (inc attempts) max-attempts)))))))

;; ============================================================
;; FORMAT CONVERTERS
;; ============================================================

(defn to-jsonl-format [pair idx]
  "Standard JSONL format"
  {:id idx
   :type (str (:type pair))
   :category (str (:category pair))
   :instruction (:instruction pair)
   :input (:xtalk pair)
   :output_js (:js pair)
   :output_python (:python pair)})

(defn to-alpaca-format [pair idx]
  "Alpaca instruction format"
  {:instruction (:instruction pair)
   :input (:xtalk pair)
   :output (str "JavaScript:\n" (:js pair) "\n\nPython:\n" (:python pair))})

(defn to-sharegpt-format [pair idx]
  "ShareGPT conversational format"
  {:id (str "std-lang-" idx)
   :conversations
   [{:from "human"
     :value (str (:instruction pair) "\n\n" (:xtalk pair))}
    {:from "gpt"
     :value (str "Here's the emitted code:\n\n"
                 "**JavaScript:**\n```javascript\n" (:js pair) "\n```\n\n"
                 "**Python:**\n```python\n" (:python pair) "\n```")}]})

(defn to-raw-format [pair idx]
  "Raw text format for pre-training"
  (str "XTALK: " (:xtalk pair) "\n"
       "JAVASCRIPT: " (:js pair) "\n"
       "PYTHON: " (:python pair) "\n"
       "---\n"))

;; ============================================================
;; FILE OUTPUT
;; ============================================================

(defn ensure-dir [dir]
  (let [file (io/file dir)]
    (when-not (.exists file)
      (.mkdirs file))))

(defn split-data [data]
  "Split data into train/val/test"
  (let [total (count data)
        train-count (int (* total (:train (:splits +config+))))
        val-count (int (* total (:val (:splits +config+))))
        shuffled (shuffle data)]
    {:train (take train-count shuffled)
     :val (take val-count (drop train-count shuffled))
     :test (drop (+ train-count val-count) shuffled)}))

(defn write-jsonl [data filepath]
  (spit filepath (str/join "\n" (map json/write data))))

(defn write-alpaca [data filepath]
  (spit filepath (json/write data)))

(defn write-sharegpt [data filepath]
  (spit filepath (json/write data)))

(defn write-raw [data filepath]
  (spit filepath (str/join "" data)))

(defn generate-dataset
  "Generate complete dataset in all formats"
  [count]
  (let [base-dir (:output-dir +config+)]
    
    ;; Ensure directories exist
    (ensure-dir base-dir)
    (doseq [subdir ["jsonl" "alpaca" "sharegpt" "raw"]]
      (ensure-dir (str base-dir "/" subdir)))
    
    ;; Generate base data
    (let [pairs (generate-batch count)
          splits (split-data pairs)]
      
      ;; Generate each format
      (doseq [format (:formats +config+)]
        (println (str "\nGenerating " (name format) " format..."))
        
        (case format
          :jsonl
          (doseq [[split split-data] splits]
            (let [converted (map-indexed #(to-jsonl-format %2 %1) split-data)
                  filepath (str base-dir "/jsonl/" (name split) ".jsonl")]
              (write-jsonl converted filepath)
              (println (str "  ✓ " filepath " (" (count converted) " samples)"))))
          
          :alpaca
          (doseq [[split split-data] splits]
            (let [converted (map-indexed #(to-alpaca-format %2 %1) split-data)
                  filepath (str base-dir "/alpaca/" (name split) ".json")]
              (write-alpaca converted filepath)
              (println (str "  ✓ " filepath " (" (count converted) " samples)"))))
          
          :sharegpt
          (doseq [[split split-data] splits]
            (let [converted (map-indexed #(to-sharegpt-format %2 %1) split-data)
                  filepath (str base-dir "/sharegpt/" (name split) ".json")]
              (write-sharegpt converted filepath)
              (println (str "  ✓ " filepath " (" (count converted) " samples)"))))
          
          :raw
          (doseq [[split split-data] splits]
            (let [converted (map-indexed #(to-raw-format %2 %1) split-data)
                  filepath (str base-dir "/raw/" (name split) ".txt")]
              (write-raw converted filepath)
              (println (str "  ✓ " filepath " (" (count converted) " samples)"))))))
      
      ;; Generate metadata
      (let [metadata {:generated_at (str (java.time.Instant/now))
                      :total_samples (count pairs)
                      :splits (zipmap (keys splits) (map count (vals splits)))
                      :formats (:formats +config+)
                      :categories (frequencies (map :type pairs))
                      :description "std.lang control flow training data"}]
        (spit (str base-dir "/metadata.json") (json/write-pp metadata))
        (println (str "\n✓ Metadata: " base-dir "/metadata.json")))
      
      ;; Generate statistics report
      (let [stats (str "TRAINING DATA STATISTICS\n"
                      "========================\n\n"
                      "Total Samples: " (count pairs) "\n"
                      "Train: " (count (:train splits)) "\n"
                      "Val: " (count (:val splits)) "\n"
                      "Test: " (count (:test splits)) "\n\n"
                      "By Type:\n"
                      (str/join "\n"
                                (map #(str "  " (key %) ": " (val %))
                                     (sort-by val > (frequencies (map :type pairs))))))]
        (spit (str base-dir "/statistics.txt") stats)
        (println (str "✓ Statistics: " base-dir "/statistics.txt")))
      
      splits)))

;; ============================================================
;; MAIN
;; ============================================================

(defn -main
  [& args]
  (let [count (or (when (seq args)
                    (try (Integer/parseInt (first args))
                         (catch Exception _ 1000)))
                  1000)]
    
    (println "╔════════════════════════════════════════════════════════════════╗")
    (println "║     LLM TRAINING DATA GENERATOR                              ║")
    (println "║     std.lang Control Flow → Multi-Format Dataset            ║")
    (println "╚════════════════════════════════════════════════════════════════╝")
    
    ;; Initialize runtimes
    (try
      (l/script- :js {:runtime :basic})
      (l/script- :python {:runtime :basic})
      (println "✓ Runtimes initialized\n")
      
      ;; Generate dataset
      (let [base-dir (:output-dir +config+)]
        (generate-dataset count)
        
        (println "\n╔════════════════════════════════════════════════════════════════╗")
        (println "║     DATASET GENERATION COMPLETE                              ║")
        (println "╚════════════════════════════════════════════════════════════════╝")
        (println (str "\nOutput directory: " base-dir))
        (println "\nGenerated formats:")
        (doseq [fmt (:formats +config+)]
          (println (str "  • " (name fmt))))
        (println "\nEach format includes:")
        (doseq [[split ratio] (:splits +config+)]
          (println (str "  • " (name split) " (" (* 100 ratio) "%)")))
        (println "\n✓ Ready for LLM training!"))
      
      (catch Exception e
        (println (str "\n✗ Error: " (.getMessage e)))
        (.printStackTrace e)))))

;; Run
(apply -main *command-line-args*)
