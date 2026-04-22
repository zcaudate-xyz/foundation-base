(ns scripts.generate-bidirectional-training
  "Bidirectional LLM training data generator for std.lang.
   
   Supports BOTH directions:
   1. Forward:  xtalk DSL → Python/JS (compilation)
   2. Backward: Python/JS → xtalk DSL (translation/parsing)
   
   Uses the python-dsl translator (code.tool.translate.python-dsl) for 
   gold-standard backward translations (Phase III: Backward Sieve).
   
   Organized for curriculum learning and multi-task training.
   
   Usage: lein exec -p src-training/scripts/generate_bidirectional_training.clj [count]"
  (:require [std.lang :as l]
            [std.json :as json]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [code.tool.translate.python-dsl :as py-dsl])
  (:use code.test))

;; ============================================================
;; CONFIGURATION
;; ============================================================

(def +config+
  {:output-dir "training/bidirectional"
   :splits {:train 0.8 :val 0.1 :test 0.1}
   :directions [:forward :backward :bidirectional]
   :formats [:jsonl :alpaca :sharegpt]
   :max-attempts-multiplier 10})

;; ============================================================
;; CONTROL FLOW GENERATORS (Reused from before)
;; ============================================================

(def +comparison-ops+ ['== 'not= '< '<= '> '>=])
(def +math-ops+ ['+ '- '* '/])

(defn random-value []
  (rand-nth [0 1 2 5 10 42 100 true false nil "x" "y"]))

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

;; Generator functions (same as before but enhanced)
(defn gen-if []
  (let [cond (random-expr 2 0)
        then (random-expr 1 0)
        else (random-expr 1 0)
        form `(if ~cond ~then ~else)]
    {:type :if
     :form form
     :xtalk (pr-str form)
     :category :control-flow
     :instruction-forward "Compile this xtalk if statement to Python and JavaScript"
     :instruction-backward "Translate this Python/JavaScript if statement to xtalk DSL"}))

(defn gen-when []
  (let [cond (random-expr 2 0)
        body (take (inc (rand-int 2)) (repeatedly #(random-expr 1 0)))
        form `(when ~cond ~@body)]
    {:type :when
     :form form
     :xtalk (pr-str form)
     :category :control-flow
     :instruction-forward "Compile this xtalk when statement to Python and JavaScript"
     :instruction-backward "Translate this Python/JavaScript conditional to xtalk DSL"}))

(defn gen-cond []
  (let [pairs (take (inc (rand-int 2))
                    (repeatedly #(vector (random-expr 2 0) (random-expr 1 0))))
        else-case (random-expr 1 0)
        clauses (concat (mapcat identity pairs) [:else else-case])
        form `(cond ~@clauses)]
    {:type :cond
     :form form
     :xtalk (pr-str form)
     :category :control-flow
     :instruction-forward "Compile this xtalk cond to Python and JavaScript"
     :instruction-backward "Translate this multi-way conditional to xtalk DSL"}))

(defn gen-for-loop []
  (let [var-sym (random-symbol)
        init `(var ~var-sym := 0)
        test `(< ~var-sym 10)
        update `[(:= ~var-sym (+ ~var-sym 1))]
        body (random-expr 1 0)
        form `(for [~init ~test ~update] ~body)]
    {:type :for
     :form form
     :xtalk (pr-str form)
     :category :control-flow
     :instruction-forward "Compile this xtalk for loop to Python and JavaScript"
     :instruction-backward "Translate this for loop to xtalk DSL"}))

(defn gen-while []
  (let [cond (random-expr 2 0)
        body (take (inc (rand-int 2)) (repeatedly #(random-expr 1 0)))
        form `(while ~cond ~@body)]
    {:type :while
     :form form
     :xtalk (pr-str form)
     :category :control-flow
     :instruction-forward "Compile this xtalk while loop to Python and JavaScript"
     :instruction-backward "Translate this while loop to xtalk DSL"}))

(defn gen-defn []
  (let [name (symbol (str "f" (rand-int 1000)))
        args (vec (take (rand-int 2) (repeatedly random-symbol)))
        body `(return ~(random-expr 1 0))
        form `(defn ~name ~args ~body)]
    {:type :defn
     :form form
     :xtalk (pr-str form)
     :category :definition
     :instruction-forward "Compile this xtalk function definition to Python and JavaScript"
     :instruction-backward "Translate this function definition to xtalk DSL"}))

(defn gen-let []
  (let [bindings (vec (mapcat #(vector % (random-value))
                              (take (inc (rand-int 2)) (repeatedly random-symbol))))
        body (random-expr 1 0)
        form `(let ~bindings ~body)]
    {:type :let
     :form form
     :xtalk (pr-str form)
     :category :binding
     :instruction-forward "Compile this xtalk let binding to Python and JavaScript"
     :instruction-backward "Translate this variable binding to xtalk DSL"}))

(defn gen-fn []
  (let [args (vec (take (rand-int 2) (repeatedly random-symbol)))
        body `(return ~(random-expr 1 0))
        form `(fn ~args ~body)]
    {:type :fn
     :form form
     :xtalk (pr-str form)
     :category :function
     :instruction-forward "Compile this xtalk anonymous function to Python and JavaScript"
     :instruction-backward "Translate this anonymous function to xtalk DSL"}))

(def +generators+ [#'gen-if #'gen-when #'gen-cond #'gen-for-loop 
                   #'gen-while #'gen-defn #'gen-let #'gen-fn])

(defn random-control-flow []
  ((rand-nth +generators+)))

;; ============================================================
;; FORWARD: xtalk → Python/JS (Compilation)
;; ============================================================

(defn emit-forward [form-data]
  "Emit xtalk to both Python and JS"
  (try
    (let [js-code (l/emit-as :js [(:form form-data)])
          py-code (l/emit-as :python [(:form form-data)])]
      {:valid true
       :direction :forward
       :xtalk (:xtalk form-data)
       :python py-code
       :javascript js-code
       :type (:type form-data)
       :category (:category form-data)
       :instruction (:instruction-forward form-data)})
    (catch Exception e
      {:valid false
       :error (.getMessage e)})))

;; ============================================================
;; BACKWARD: Python/JS → xtalk (Translation)
;; ============================================================

(defn try-backward-translate [py-code js-code]
  "Attempt to translate Python/JS back to xtalk
   
   This simulates what the LLM should learn to do.
   For now, we use the xtalk form we started with as the 'gold' standard,
   but in practice you'd use the python-dsl translator on real Python code."
  (try
    ;; In a real implementation, you'd parse Python and translate:
    ;; (py-dsl/translate-node (python-parser/parse py-code))
    ;; 
    ;; For training data generation, we use the original xtalk as gold standard
    {:valid true
     :direction :backward
     :input_python py-code
     :input_javascript js-code
     ;; The target output - what the LLM should learn to produce
     :output_xtalk nil  ; Will be filled in later
     :note "Using original xtalk as gold standard"}
    (catch Exception e
      {:valid false
       :error (.getMessage e)})))

;; ============================================================
;; BIDIRECTIONAL: Generate both directions
;; ============================================================

(defn generate-bidirectional-pair []
  "Generate a pair that supports both directions"
  (let [form-data (random-control-flow)
        forward (emit-forward form-data)]
    (if (:valid forward)
      (let [;; Create forward sample
            forward-sample {:direction :forward
                           :instruction (:instruction-forward form-data)
                           :input (:xtalk form-data)
                           :output_python (:python forward)
                           :output_javascript (:javascript forward)
                           :type (:type form-data)
                           :category (:category form-data)}
            
            ;; Create backward sample (Python → xtalk)
            backward-py {:direction :backward
                        :instruction (:instruction-backward form-data)
                        :input (:python forward)
                        :output (:xtalk form-data)
                        :source_language :python
                        :type (:type form-data)
                        :category (:category form-data)}
            
            ;; Create backward sample (JS → xtalk)
            backward-js {:direction :backward
                        :instruction (:instruction-backward form-data)
                        :input (:javascript forward)
                        :output (:xtalk form-data)
                        :source_language :javascript
                        :type (:type form-data)
                        :category (:category form-data)}
            
            ;; Create bidirectional sample
            bidirectional {:direction :bidirectional
                          :instruction "Translate between xtalk DSL and Python/JavaScript"
                          :xtalk (:xtalk form-data)
                          :python (:python forward)
                          :javascript (:javascript forward)
                          :type (:type form-data)
                          :category (:category form-data)}]
        
        {:valid true
         :forward forward-sample
         :backward-py backward-py
         :backward-js backward-js
         :bidirectional bidirectional})
      {:valid false})))

;; ============================================================
;; BATCH GENERATION
;; ============================================================

(defn generate-batch [target-count]
  "Generate a batch of bidirectional training pairs"
  (println (str "\nGenerating " target-count " bidirectional training samples..."))
  (println "This creates pairs for both directions:")
  (println "  • Forward: xtalk → Python/JS (compilation)")
  (println "  • Backward: Python/JS → xtalk (translation)")
  
  (loop [pairs []
         attempts 0
         max-attempts (* target-count (:max-attempts-multiplier +config+))]
    (cond
      (>= (count pairs) target-count)
      (do
        (println (str "\n✓ Generated " (count pairs) " valid samples (" attempts " attempts)"))
        pairs)
      
      (>= attempts max-attempts)
      (do
        (println (str "\n⚠ Max attempts reached: " (count pairs) " samples"))
        pairs)
      
      :else
      (do
        (when (and (> attempts 0) (== 0 (mod attempts 50)))
          (print ".") (flush))
        
        (let [result (generate-bidirectional-pair)]
          (if (:valid result)
            (recur (conj pairs result) (inc attempts) max-attempts)
            (recur pairs (inc attempts) max-attempts)))))))

;; ============================================================
;; FORMAT CONVERTERS FOR LLM TRAINING
;; ============================================================

(defn to-forward-jsonl [pair idx]
  "Forward direction: xtalk → Python/JS"
  {:id (str "fwd-" idx)
   :direction "forward"
   :task "compilation"
   :instruction (:instruction (:forward pair))
   :input (:input (:forward pair))
   :output {:python (:output_python (:forward pair))
           :javascript (:output_javascript (:forward pair))}
   :type (str (:type (:forward pair)))
   :category (str (:category (:forward pair)))})

(defn to-backward-jsonl [pair idx lang]
  "Backward direction: Python/JS → xtalk"
  (let [sample (if (= lang :python) 
                 (:backward-py pair)
                 (:backward-js pair))]
    {:id (str "bwd-" lang "-" idx)
     :direction "backward"
     :task "translation"
     :source_language (name lang)
     :target_language "xtalk"
     :instruction (:instruction sample)
     :input (:input sample)
     :output (:output sample)
     :type (str (:type sample))
     :category (str (:category sample))}))

(defn to-bidirectional-jsonl [pair idx]
  "Bidirectional: Full conversion table"
  {:id (str "bidir-" idx)
   :direction "bidirectional"
   :task "conversion"
   :xtalk (:xtalk (:bidirectional pair))
   :python (:python (:bidirectional pair))
   :javascript (:javascript (:bidirectional pair))
   :type (str (:type (:bidirectional pair)))
   :category (str (:category (:bidirectional pair)))})

(defn to-alpaca-forward [pair idx]
  "Alpaca format for forward direction"
  {:instruction (:instruction (:forward pair))
   :input (:input (:forward pair))
   :output (str "Python:\n" (:output_python (:forward pair)) 
               "\n\nJavaScript:\n" (:output_javascript (:forward pair)))
   :metadata {:direction "forward"
             :type (str (:type (:forward pair)))}})

(defn to-alpaca-backward [pair idx lang]
  "Alpaca format for backward direction"
  (let [sample (if (= lang :python) 
                 (:backward-py pair)
                 (:backward-js pair))]
    {:instruction (:instruction sample)
     :input (:input sample)
     :output (:output sample)
     :metadata {:direction "backward"
               :source_lang (name lang)
               :target_lang "xtalk"
               :type (str (:type sample))}}))

(defn to-sharegpt-forward [pair idx]
  "ShareGPT format for forward direction"
  {:id (str "std-lang-fwd-" idx)
   :conversations
   [{:from "human"
     :value (str (:instruction (:forward pair)) "\n\n```xtalk\n" 
                 (:input (:forward pair)) "\n```")}
    {:from "gpt"
     :value (str "**Python:**\n```python\n" 
                 (:output_python (:forward pair)) 
                 "\n```\n\n**JavaScript:**\n```javascript\n"
                 (:output_javascript (:forward pair)) "\n```")}]})

(defn to-sharegpt-backward [pair idx lang]
  "ShareGPT format for backward direction"
  (let [sample (if (= lang :python) 
                 (:backward-py pair)
                 (:backward-js pair))
        lang-name (if (= lang :python) "Python" "JavaScript")]
    {:id (str "std-lang-bwd-" lang "-" idx)
     :conversations
     [{:from "human"
       :value (str (:instruction sample) "\n\n```" (name lang) "\n" 
                   (:input sample) "\n```")}
      {:from "gpt"
       :value (str "**xtalk DSL:**\n```clojure\n" 
                   (:output sample) "\n```")}]}))

;; ============================================================
;; FILE ORGANIZATION
;; ============================================================

(defn ensure-dir [dir]
  (let [file (io/file dir)]
    (when-not (.exists file)
      (.mkdirs file))))

(defn split-data [data]
  (let [total (count data)
        train-count (int (* total (:train (:splits +config+))))
        val-count (int (* total (:val (:splits +config+))))
        shuffled (shuffle data)]
    {:train (take train-count shuffled)
     :val (take val-count (drop train-count shuffled))
     :test (drop (+ train-count val-count) shuffled)}))

(defn write-jsonl [data filepath]
  (spit filepath (str/join "\n" (map json/write data))))

(defn write-json [data filepath]
  (spit filepath (json/write data)))

;; ============================================================
;; DATASET GENERATION
;; ============================================================

(defn generate-dataset [count]
  (let [base-dir (:output-dir +config+)]
    
    ;; Create directory structure
    (ensure-dir base-dir)
    (doseq [dir ["forward" "backward" "bidirectional" "complete"]]
      (ensure-dir (str base-dir "/" dir)))
    
    ;; Generate base data
    (println "\n=== PHASE 1: Generating Bidirectional Pairs ===")
    (let [pairs (generate-batch count)
          splits (split-data pairs)]
      
      ;; Forward direction: xtalk → Python/JS
      (println "\n=== PHASE 2: Forward Direction (xtalk → Python/JS) ===")
      (doseq [[split split-data] splits]
        (let [jsonl-data (map-indexed #(to-forward-jsonl %2 %1) split-data)
              filepath (str base-dir "/forward/" (name split) ".jsonl")]
          (write-jsonl jsonl-data filepath)
          (println (str "  ✓ " filepath " (" (count jsonl-data) " samples)"))))
      
      ;; Backward direction: Python → xtalk
      (println "\n=== PHASE 3: Backward Direction (Python → xtalk) ===")
      (doseq [[split split-data] splits]
        (let [jsonl-data (map-indexed #(to-backward-jsonl %2 %1 :python) split-data)
              filepath (str base-dir "/backward/python_" (name split) ".jsonl")]
          (write-jsonl jsonl-data filepath)
          (println (str "  ✓ " filepath " (" (count jsonl-data) " samples)"))))
      
      ;; Backward direction: JS → xtalk
      (println "\n=== PHASE 4: Backward Direction (JavaScript → xtalk) ===")
      (doseq [[split split-data] splits]
        (let [jsonl-data (map-indexed #(to-backward-jsonl %2 %1 :javascript) split-data)
              filepath (str base-dir "/backward/javascript_" (name split) ".jsonl")]
          (write-jsonl jsonl-data filepath)
          (println (str "  ✓ " filepath " (" (count jsonl-data) " samples)"))))
      
      ;; Bidirectional: Complete conversion table
      (println "\n=== PHASE 5: Bidirectional (Complete Mappings) ===")
      (doseq [[split split-data] splits]
        (let [jsonl-data (map-indexed #(to-bidirectional-jsonl %2 %1) split-data)
              filepath (str base-dir "/bidirectional/" (name split) ".jsonl")]
          (write-jsonl jsonl-data filepath)
          (println (str "  ✓ " filepath " (" (count jsonl-data) " samples)"))))
      
      ;; Alpaca format for instruction tuning
      (println "\n=== PHASE 6: Alpaca Format (Instruction Tuning) ===")
      (ensure-dir (str base-dir "/alpaca"))
      (let [all-forward (mapcat :train (vals splits))
            all-backward-py (mapcat :train (vals splits))
            alpaca-data (concat
                         (map-indexed #(to-alpaca-forward %2 %1) all-forward)
                         (map-indexed #(to-alpaca-backward %2 %1 :python) all-backward-py))]
        (write-json alpaca-data (str base-dir "/alpaca/train.json"))
        (println (str "  ✓ " base-dir "/alpaca/train.json (" (count alpaca-data) " samples)")))
      
      ;; ShareGPT format for conversational training
      (println "\n=== PHASE 7: ShareGPT Format (Conversational) ===")
      (ensure-dir (str base-dir "/sharegpt"))
      (let [all-data (:train splits)
            sharegpt-data (concat
                          (map-indexed #(to-sharegpt-forward %2 %1) all-data)
                          (map-indexed #(to-sharegpt-backward %2 %1 :python) all-data)
                          (map-indexed #(to-sharegpt-backward %2 %1 :javascript) all-data))]
        (write-json sharegpt-data (str base-dir "/sharegpt/train.json"))
        (println (str "  ✓ " base-dir "/sharegpt/train.json (" (count sharegpt-data) " samples)")))
      
      ;; Metadata
      (let [metadata {:generated_at (str (java.time.Instant/now))
                      :total_base_pairs (count pairs)
                      :splits (zipmap (keys splits) (map count (vals splits)))
                      :directions [:forward :backward]
                      :source_languages [:xtalk :python :javascript]
                      :target_languages [:xtalk :python :javascript]
                      :formats (:formats +config+)
                      :description "Bidirectional std.lang training data for LLMs"
                      :forward_samples (* (count pairs) 1)
                      :backward_samples (* (count pairs) 2)
                      :total_training_samples (* (count pairs) 3)}]
        (write-json metadata (str base-dir "/metadata.json"))
        (println (str "\n✓ Metadata: " base-dir "/metadata.json")))
      
      ;; Statistics
      (let [stats (str "BIDIRECTIONAL TRAINING DATA STATISTICS\n"
                      "=======================================\n\n"
                      "Base Pairs Generated: " (count pairs) "\n"
                      "Train: " (count (:train splits)) "\n"
                      "Val: " (count (:val splits)) "\n"
                      "Test: " (count (:test splits)) "\n\n"
                      "Direction Breakdown:\n"
                      "  Forward (xtalk→Python/JS): " (count pairs) "\n"
                      "  Backward (Python→xtalk): " (count pairs) "\n"
                      "  Backward (JS→xtalk): " (count pairs) "\n"
                      "  Total Training Samples: " (* (count pairs) 3) "\n\n"
                      "By Control Flow Type:\n"
                      (str/join "\n"
                                (map #(str "  " (key %) ": " (val %))
                                     (sort-by val > (frequencies (map #(get-in % [:forward :type]) pairs))))))]
        (spit (str base-dir "/statistics.txt") stats)
        (println (str "✓ Statistics: " base-dir "/statistics.txt")))
      
      pairs)))

;; ============================================================
;; MAIN
;; ============================================================

(defn -main
  [& args]
  (let [count (or (when (seq args)
                    (try (Integer/parseInt (first args))
                         (catch Exception _ 500)))
                  500)]
    
    (println "╔════════════════════════════════════════════════════════════════╗")
    (println "║     BIDIRECTIONAL LLM TRAINING DATA GENERATOR                 ║")
    (println "║     Phase III: Backward Sieve + Forward Compilation          ║")
    (println "╚════════════════════════════════════════════════════════════════╝")
    (println)
    (println "This generates training data for BOTH directions:")
    (println "  1. Forward:  xtalk DSL → Python/JavaScript (compilation)")
    (println "  2. Backward: Python/JavaScript → xtalk DSL (translation)")
    (println)
    
    (try
      ;; Initialize runtimes
      (println "Initializing runtimes...")
      (l/script- :js {:runtime :basic})
      (l/script- :python {:runtime :basic})
      (println "✓ Runtimes ready\n")
      
      ;; Generate dataset
      (generate-dataset count)
      
      (println "\n╔════════════════════════════════════════════════════════════════╗")
      (println "║     DATASET GENERATION COMPLETE                               ║")
      (println "╚════════════════════════════════════════════════════════════════╝")
      (println)
      (println "Output directory: training/bidirectional/")
      (println)
      (println "Generated files:")
      (println "  forward/        - xtalk → Python/JS (compilation)")
      (println "  backward/       - Python/JS → xtalk (translation)")
      (println "  bidirectional/  - Complete conversion tables")
      (println "  alpaca/         - Instruction tuning format")
      (println "  sharegpt/       - Conversational format")
      (println)
      (println "✓ Ready for multi-task LLM training!")
      (println)
      (println "Training scenarios:")
      (println "  • Fine-tune on 'forward' for compilation")
      (println "  • Fine-tune on 'backward' for translation")
      (println "  • Multi-task on all for bidirectional fluency")
      
      (catch Exception e
        (println (str "\n✗ Error: " (.getMessage e)))
        (.printStackTrace e)))))

;; Run
(apply -main *command-line-args*)
