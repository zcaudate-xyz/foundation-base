(ns scripts.rosetta-forge
  "Phase II: The Rosetta Forge - Generates synthetic training data pairs
   from xtalk specs using l/emit-as for multi-target code generation.
   
   Usage: lein exec -p src-training/scripts/rosetta_forge.clj"
  (:require [std.lang :as l]
            [std.lib.json :as json]
            [clojure.string :as str])
  (:use code.test))

;; Initialize runtime contexts
(l/script- :js
  {:require [[xt.lang.base-lib :as k]]})

(l/script- :python
  {:require [[xt.lang.base-lib :as k]]})

;; ============================================================
;; ATOM EXTRACTION
;; ============================================================

(def +atom-registry+
  "Registry of xtalk atoms extracted from xt.lang namespaces"
  (atom {}))

(defn register-atom!
  [category name spec args docstring]
  (swap! +atom-registry+ assoc-in [category name]
         {:name name
          :spec spec
          :args args
          :docstring docstring
          :category category}))

;; Manually define core atoms from xt.lang.base-lib analysis
(def +base-lib-atoms+
  "Core atoms from xt.lang.base-lib"
  {:math
   [{:name 'clamp :args '[min max v] :spec '[:fn [:xt/num :xt/num :xt/num] :xt/num] :doc "Clamps value between min and max"}
    {:name 'mix :args '[x0 x1 v] :spec '[:fn [:xt/num :xt/num :xt/num :xt/num] :xt/num] :doc "Linear interpolation between x0 and x1"}
    {:name 'sign :args '[x] :spec '[:fn [:xt/num] :xt/num] :doc "Returns sign of number (-1, 0, or 1)"}
    {:name 'round :args '[x] :spec '[:fn [:xt/num] :xt/num] :doc "Rounds number to nearest integer"}
    {:name 'abs :args '[x] :spec '[:fn [:xt/num] :xt/num] :doc "Absolute value"}
    {:name 'gcd :args '[a b] :spec '[:fn [:xt/num :xt/num] :xt/num] :doc "Greatest common divisor"}
    {:name 'lcm :args '[a b] :spec '[:fn [:xt/num :xt/num] :xt/num] :doc "Least common multiple"}
    {:name 'mod-pos :args '[x m] :spec '[:fn [:xt/num :xt/num] :xt/num] :doc "Positive modulo"}
    {:name 'bit-count :args '[x] :spec '[:fn [:xt/num] :xt/num] :doc "Count of set bits"}]
   
   :array
   [{:name 'arr-map :args '[arr f] :spec '[:fn [AnyArray UnaryFn] AnyArray] :doc "Maps function over array"}
    {:name 'arr-filter :args '[arr pred] :spec '[:fn [AnyArray Predicate] AnyArray] :doc "Filters array by predicate"}
    {:name 'arr-slice :args '[arr start end] :spec '[:fn [AnyArray :xt/num :xt/num] AnyArray] :doc "Extracts slice of array"}
    {:name 'arr-assign :args '[arr1 arr2] :spec '[:fn [AnyArray AnyArray] AnyArray] :doc "Appends two arrays"}
    {:name 'arr-clone :args '[arr] :spec '[:fn [AnyArray] AnyArray] :doc "Clones an array"}
    {:name 'arr-every :args '[arr pred] :spec '[:fn [AnyArray Predicate] :xt/bool] :doc "Tests if all elements satisfy predicate"}
    {:name 'arr-some :args '[arr pred] :spec '[:fn [AnyArray Predicate] :xt/bool] :doc "Tests if any element satisfies predicate"}
    {:name 'arr-keep :args '[arr f] :spec '[:fn [AnyArray MaybeUnaryFn] AnyArray] :doc "Maps and filters out nils"}
    {:name 'arr-range :args '[n] :spec '[:fn [:xt/any] AnyArray] :doc "Creates range array"}]
   
   :object
   [{:name 'obj-keys :args '[obj] :spec '[:fn [AnyDict] [:xt/array :xt/str]] :doc "Gets object keys as array"}
    {:name 'obj-vals :args '[obj] :spec '[:fn [AnyDict] AnyArray] :doc "Gets object values as array"}
    {:name 'obj-pairs :args '[obj] :spec '[:fn [AnyDict] [:xt/array Pair]] :doc "Gets object as key-value pairs"}
    {:name 'obj-clone :args '[obj] :spec '[:fn [AnyDict] AnyDict] :doc "Clones an object"}
    {:name 'obj-assign :args '[obj1 obj2] :spec '[:fn [AnyDict AnyDict] AnyDict] :doc "Assigns properties from obj2 to obj1"}
    {:name 'obj-pick :args '[obj keys] :spec '[:fn [AnyDict [:xt/array :xt/str]] AnyDict] :doc "Picks specified keys from object"}
    {:name 'obj-omit :args '[obj keys] :spec '[:fn [AnyDict [:xt/array :xt/str]] AnyDict] :doc "Omits specified keys from object"}
    {:name 'obj-del :args '[obj key] :spec '[:fn [AnyDict :xt/str] AnyDict] :doc "Deletes key from object"}
    {:name 'get-in :args '[obj path] :spec '[:fn [AnyDict AnyArray] :xt/any] :doc "Gets nested value by path"}]
   
   :string
   [{:name 'starts-with? :args '[s prefix] :spec '[:fn [:xt/str :xt/str] :xt/bool] :doc "Checks if string starts with prefix"}
    {:name 'ends-with? :args '[s suffix] :spec '[:fn [:xt/str :xt/str] :xt/bool] :doc "Checks if string ends with suffix"}
    {:name 'capitalize :args '[s] :spec '[:fn [:xt/str] :xt/str] :doc "Capitalizes first character"}
    {:name 'decapitalize :args '[s] :spec '[:fn [:xt/str] :xt/str] :doc "Lowercases first character"}
    {:name 'pad-left :args '[s n pad] :spec '[:fn [:xt/str :xt/num :xt/str] :xt/str] :doc "Pads string on left"}
    {:name 'pad-right :args '[s n pad] :spec '[:fn [:xt/str :xt/num :xt/str] :xt/str] :doc "Pads string on right"}
    {:name 'split-long :args '[s n] :spec '[:fn [:xt/str :xt/num] [:xt/array :xt/str]] :doc "Splits string into chunks"}
    {:name 'sym-name :args '[s] :spec '[:fn [:xt/str] :xt/str] :doc "Extracts symbol name"}
    {:name 'sym-ns :args '[s] :spec '[:fn [:xt/str] [:xt/maybe :xt/str]] :doc "Extracts symbol namespace"}]
   
   :type
   [{:name 'fn? :args '[x] :spec '[:fn [:xt/any] :xt/bool] :doc "Checks if value is a function"}
    {:name 'arr? :args '[x] :spec '[:fn [:xt/any] :xt/bool] :doc "Checks if value is an array"}
    {:name 'obj? :args '[x] :spec '[:fn [:xt/any] :xt/bool] :doc "Checks if value is an object"}
    {:name 'is-empty? :args '[x] :spec '[:fn [:xt/any] :xt/bool] :doc "Checks if value is empty"}
    {:name 'not-empty? :args '[x] :spec '[:fn [:xt/any] :xt/bool] :doc "Checks if value is not empty"}
    {:name 'identity :args '[x] :spec '[:fn [:xt/any] :xt/any] :doc "Returns input unchanged"}
    {:name 'noop :args '[] :spec '[:fn [] :xt/nil] :doc "No operation"}
    {:name 'id-fn :args '[x] :spec '[:fn [AnyDict] [:xt/maybe :xt/any]] :doc "Gets id field from object"}
    {:name 'key-fn :args '[k] :spec '[:fn [:xt/any] UnaryFn] :doc "Creates key access function"}
    {:name 'eq-fn :args '[k v] :spec '[:fn [:xt/any :xt/any] Predicate] :doc "Creates equality predicate"}
    {:name 'inc-fn :args '[n] :spec '[:fn [[:xt/maybe :xt/num]] CounterFn] :doc "Creates incrementing function"}]})

;; ============================================================
;; TRAINING PAIR GENERATION
;; ============================================================

(defn generate-arg-values
  "Generate sample argument values based on type specs"
  [arg-names]
  (mapv (fn [arg]
          (case arg
            (min max v x0 x1 x a b n m) (rand-int 100)
            (arr arr1 arr2) [1 2 3 4 5]
            (obj obj1 obj2) {:a 1 :b 2 :c 3}
            (s prefix suffix pad) "hello"
            (keys path) ["a" "b"]
            (f pred) '(fn [x] (> x 0))
            (k) "key"
            (v) "value"
            nil))
        arg-names))

(defn generate-xtalk-function
  "Generates an xtalk function definition for a given atom"
  [atom-def lang-ns]
  (let [{:keys [name args spec doc]} atom-def
        fn-name (symbol (str name "-example"))
        x-prefix (symbol (str "x:" (str/replace (str name) #"-" "")))
        body (if (empty? args)
               (list x-prefix)
               (cons x-prefix (map symbol args)))]
    {:intent (str doc " using xtalk " name)
     :category (:category atom-def :unknown)
     :function-name fn-name
     :args args
     :xtalk-form (list 'defn.js fn-name args
                       (list 'return body))
     :raw-atom name}))

(defn emit-training-pair
  "Emits xtalk code to both JavaScript and Python"
  [xtalk-fn]
  (try
    (let [js-code (l/emit-as :js [(:xtalk-form xtalk-fn)])
          py-code (l/emit-as :python [(:xtalk-form xtalk-fn)])]
      (assoc xtalk-fn
             :js js-code
             :python py-code
             :valid true))
    (catch Exception e
      (assoc xtalk-fn
             :error (.getMessage e)
             :valid false))))

(defn generate-pair-batch
  "Generate a batch of training pairs for a category"
  [category atoms n-per-atom]
  (for [atom-def atoms
        i (range n-per-atom)]
    (-> atom-def
        (assoc :category category)
        (generate-xtalk-function 'xt.lang.base-lib)
        emit-training-pair)))

;; ============================================================
;; MAIN GENERATION
;; ============================================================

(defn generate-rosetta-bible
  "Generate the full Rosetta Bible with specified number of pairs"
  [target-count]
  (let [atoms-per-category (Math/ceil (/ target-count (count +base-lib-atoms+)))
        all-pairs (doall
                   (mapcat (fn [[category atoms]]
                             (println (str "Generating " category " atoms..."))
                             (generate-pair-batch category atoms atoms-per-category))
                           +base-lib-atoms+))
        valid-pairs (filter :valid all-pairs)
        trimmed-pairs (take target-count valid-pairs)]
    (println (str "\nGenerated " (count valid-pairs) " valid pairs"))
    (println (str "Trimmed to " (count trimmed-pairs) " pairs"))
    trimmed-pairs))

(defn pairs->jsonl
  "Convert pairs to JSONL format"
  [pairs]
  (str/join "\n"
            (map (fn [pair]
                   (json/write-str
                    {:instruction (:intent pair)
                     :category (str (:category pair))
                     :xtalk (pr-str (:xtalk-form pair))
                     :js (:js pair)
                     :python (:python pair)
                     :function_name (str (:function-name pair))}))
                 pairs)))

(defn -main
  [& args]
  (let [target-count (or (when (seq args)
                          (try (Integer/parseInt (first args))
                               (catch Exception _ 1000)))
                        1000)
        output-file (or (second args)
                        "training/ROSETTA_BIBLE.jsonl")]
    
    (println "=== ROSSETTA FORGE ===")
    (println (str "Generating " target-count " training pairs...\n"))
    
    (let [pairs (generate-rosetta-bible target-count)
          jsonl (pairs->jsonl pairs)]
      
      (spit output-file jsonl)
      
      (println (str "\n✓ Generated " (count pairs) " training pairs"))
      (println (str "✓ Written to: " output-file))
      
      ;; Print sample
      (println "\n=== SAMPLE PAIRS ===")
      (doseq [pair (take 3 pairs)]
        (println "\n---")
        (println (str "Intent: " (:intent pair)))
        (println (str "Category: " (:category pair)))
        (println (str "XTalk: " (pr-str (:xtalk-form pair))))
        (println (str "JS: " (subs (:js pair) 0 (min 80 (count (:js pair)))) "..."))
        (println (str "Python: " (subs (:python pair) 0 (min 80 (count (:python pair)))) "...")))
      
      ;; Category breakdown
      (println "\n=== CATEGORY BREAKDOWN ===")
      (doseq [[cat pairs] (group-by :category pairs)]
        (println (str cat ": " (count pairs) " pairs"))))))

;; Run if executed directly
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
