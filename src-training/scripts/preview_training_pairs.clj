(ns scripts.preview-training-pairs
  "Preview of what 1000 training pairs would look like.
   
   This generates sample output without needing full lang initialization.
   Run: lein exec -p src-training/scripts/preview_training_pairs.clj"
  (:require [clojure.string :as str]))

;; ============================================================
;; SAMPLE TRAINING PAIRS (showing the format)
;; ============================================================

(def +sample-pairs+
  "Sample training pairs showing the format for JS and Python"
  [
   ;; Math functions
   {:id 1
    :category "math"
    :function "clamp"
    :intent "Clamp a value between min and max"
    :xtalk "(k/clamp 0 100 50)"
    :js "k.clamp(0, 100, 50);"
    :python "k.clamp(0, 100, 50)"}
   
   {:id 2
    :category "math"
    :function "mix"
    :intent "Linear interpolation between two values"
    :xtalk "(k/mix 0.0 1.0 0.5)"
    :js "k.mix(0.0, 1.0, 0.5);"
    :python "k.mix(0.0, 1.0, 0.5)"}
   
   {:id 3
    :category "math"
    :function "sign"
    :intent "Get the sign of a number"
    :xtalk "(k/sign -42)"
    :js "k.sign(-42);"
    :python "k.sign(-42)"}
   
   {:id 4
    :category "math"
    :function "round"
    :intent "Round a number to nearest integer"
    :xtalk "(k/round 3.7)"
    :js "k.round(3.7);"
    :python "k.round(3.7)"}
   
   {:id 5
    :category "math"
    :function "abs"
    :intent "Calculate absolute value"
    :xtalk "(k/abs -5)"
    :js "k.abs(-5);"
    :python "k.abs(-5)"}
   
   {:id 6
    :category "math"
    :function "gcd"
    :intent "Calculate greatest common divisor"
    :xtalk "(k/gcd 48 18)"
    :js "k.gcd(48, 18);"
    :python "k.gcd(48, 18)"}
   
   {:id 7
    :category "math"
    :function "lcm"
    :intent "Calculate least common multiple"
    :xtalk "(k/lcm 4 6)"
    :js "k.lcm(4, 6);"
    :python "k.lcm(4, 6)"}
   
   {:id 8
    :category "math"
    :function "mod-pos"
    :intent "Calculate positive modulo"
    :xtalk "(k/mod-pos -1 5)"
    :js "k.mod_pos(-1, 5);"
    :python "k.mod_pos(-1, 5)"}
   
   ;; Array functions
   {:id 9
    :category "array"
    :function "arr-map"
    :intent "Map a function over an array"
    :xtalk "(k/arr-map [1 2 3] (fn [x] (* x 2)))"
    :js "k.arr_map([1,2,3],function(x){\n  return x * 2;\n});"
    :python "k.arr_map([1, 2, 3], lambda x: x * 2)"}
   
   {:id 10
    :category "array"
    :function "arr-filter"
    :intent "Filter array elements by predicate"
    :xtalk "(k/arr-filter [1 2 3 4 5] (fn [x] (> x 2)))"
    :js "k.arr_filter([1,2,3,4,5],function(x){\n  return x > 2;\n});"
    :python "k.arr_filter([1, 2, 3, 4, 5], lambda x: x > 2)"}
   
   {:id 11
    :category "array"
    :function "arr-slice"
    :intent "Extract a slice from an array"
    :xtalk "(k/arr-slice [1 2 3 4 5] 1 3)"
    :js "k.arr_slice([1,2,3,4,5], 1, 3);"
    :python "k.arr_slice([1, 2, 3, 4, 5], 1, 3)"}
   
   {:id 12
    :category "array"
    :function "arr-assign"
    :intent "Append two arrays"
    :xtalk "(k/arr-assign [1 2] [3 4])"
    :js "k.arr_append([1,2], [3,4]);"
    :python "k.arr_append([1, 2], [3, 4])"}
   
   {:id 13
    :category "array"
    :function "arr-clone"
    :intent "Clone an array"
    :xtalk "(k/arr-clone [1 2 3])"
    :js "k.arr_clone([1,2,3]);"
    :python "k.arr_clone([1, 2, 3])"}
   
   {:id 14
    :category "array"
    :function "arr-every"
    :intent "Check if all elements satisfy predicate"
    :xtalk "(k/arr-every [2 4 6] (fn [x] (== 0 (% x 2))))"
    :js "k.arr_every([2,4,6],function(x){\n  return 0 == x % 2;\n});"
    :python "k.arr_every([2, 4, 6], lambda x: 0 == x % 2)"}
   
   {:id 15
    :category "array"
    :function "arr-some"
    :intent "Check if any element satisfies predicate"
    :xtalk "(k/arr-some [1 3 5] (fn [x] (== 0 (% x 2))))"
    :js "k.arr_some([1,3,5],function(x){\n  return 0 == x % 2;\n});"
    :python "k.arr_some([1, 3, 5], lambda x: 0 == x % 2)"}
   
   {:id 16
    :category "array"
    :function "arr-range"
    :intent "Create a range array"
    :xtalk "(k/arr-range 5)"
    :js "k.arr_range(5);"
    :python "k.arr_range(5)"}
   
   ;; Object functions
   {:id 17
    :category "object"
    :function "obj-keys"
    :intent "Get all keys from an object"
    :xtalk "(k/obj-keys {:a 1 :b 2 :c 3})"
    :js "k.obj_keys({\"a\":1,\"b\":2,\"c\":3});"
    :python "k.obj_keys({'a': 1, 'b': 2, 'c': 3})"}
   
   {:id 18
    :category "object"
    :function "obj-vals"
    :intent "Get all values from an object"
    :xtalk "(k/obj-vals {:a 1 :b 2})"
    :js "k.obj_vals({\"a\":1,\"b\":2});"
    :python "k.obj_vals({'a': 1, 'b': 2})"}
   
   {:id 19
    :category "object"
    :function "obj-pairs"
    :intent "Convert object to key-value pairs"
    :xtalk "(k/obj-pairs {:x 10 :y 20})"
    :js "k.obj_pairs({\"x\":10,\"y\":20});"
    :python "k.obj_pairs({'x': 10, 'y': 20})"}
   
   {:id 20
    :category "object"
    :function "obj-clone"
    :intent "Clone an object"
    :xtalk "(k/obj-clone {:a 1 :b 2})"
    :js "k.obj_clone({\"a\":1,\"b\":2});"
    :python "k.obj_clone({'a': 1, 'b': 2})"}
   
   {:id 21
    :category "object"
    :function "obj-assign"
    :intent "Assign properties from one object to another"
    :xtalk "(k/obj-assign {:a 1} {:b 2})"
    :js "k.obj_assign({\"a\":1}, {\"b\":2});"
    :python "k.obj_assign({'a': 1}, {'b': 2})"}
   
   {:id 22
    :category "object"
    :function "obj-pick"
    :intent "Pick specific keys from an object"
    :xtalk "(k/obj-pick {:a 1 :b 2 :c 3} [\"a\" \"c\"])"
    :js "k.obj_pick({\"a\":1,\"b\":2,\"c\":3}, [\"a\",\"c\"]);"
    :python "k.obj_pick({'a': 1, 'b': 2, 'c': 3}, ['a', 'c'])"}
   
   {:id 23
    :category "object"
    :function "obj-omit"
    :intent "Omit specific keys from an object"
    :xtalk "(k/obj-omit {:a 1 :b 2 :c 3} [\"b\"])"
    :js "k.obj_omit({\"a\":1,\"b\":2,\"c\":3}, [\"b\"]);"
    :python "k.obj_omit({'a': 1, 'b': 2, 'c': 3}, ['b'])"}
   
   {:id 24
    :category "object"
    :function "obj-del"
    :intent "Delete a key from an object"
    :xtalk "(k/obj-del {:a 1 :b 2} \"a\")"
    :js "k.obj_del({\"a\":1,\"b\":2}, \"a\");"
    :python "k.obj_del({'a': 1, 'b': 2}, 'a')"}
   
   {:id 25
    :category "object"
    :function "get-in"
    :intent "Get nested value by path"
    :xtalk "(k/get-in {:a {:b {:c 1}}} [\"a\" \"b\" \"c\"])"
    :js "k.get_in({\"a\":{\"b\":{\"c\":1}}}, [\"a\",\"b\",\"c\"]);"
    :python "k.get_in({'a': {'b': {'c': 1}}}, ['a', 'b', 'c'])"}
   
   ;; String functions
   {:id 26
    :category "string"
    :function "starts-with?"
    :intent "Check if string starts with prefix"
    :xtalk "(k/starts-with? \"hello world\" \"hello\")"
    :js "k.starts_with(\"hello world\", \"hello\");"
    :python "k.starts_with('hello world', 'hello')"}
   
   {:id 27
    :category "string"
    :function "ends-with?"
    :intent "Check if string ends with suffix"
    :xtalk "(k/ends-with? \"hello.txt\" \".txt\")"
    :js "k.ends_with(\"hello.txt\", \".txt\");"
    :python "k.ends_with('hello.txt', '.txt')"}
   
   {:id 28
    :category "string"
    :function "capitalize"
    :intent "Capitalize first character of string"
    :xtalk "(k/capitalize \"hello\")"
    :js "k.capitalize(\"hello\");"
    :python "k.capitalize('hello')"}
   
   {:id 29
    :category "string"
    :function "decapitalize"
    :intent "Decapitalize first character of string"
    :xtalk "(k/decapitalize \"HELLO\")"
    :js "k.decapitalize(\"HELLO\");"
    :python "k.decapitalize('HELLO')"}
   
   {:id 30
    :category "string"
    :function "pad-left"
    :intent "Pad string on the left"
    :xtalk "(k/pad-left \"42\" 5 \"0\")"
    :js "k.pad_left(\"42\", 5, \"0\");"
    :python "k.pad_left('42', 5, '0')"}
   
   {:id 31
    :category "string"
    :function "pad-right"
    :intent "Pad string on the right"
    :xtalk "(k/pad-right \"hi\" 5 \" \")"
    :js "k.pad_right(\"hi\", 5, \" \");"
    :python "k.pad_right('hi', 5, ' ')"}
   
   {:id 32
    :category "string"
    :function "sym-name"
    :intent "Extract symbol name from namespaced symbol"
    :xtalk "(k/sym-name \"my-ns/my-fn\")"
    :js "k.sym_name(\"my-ns/my-fn\");"
    :python "k.sym_name('my-ns/my-fn')"}
   
   ;; Type checking
   {:id 33
    :category "type"
    :function "fn?"
    :intent "Check if value is a function"
    :xtalk "(k/fn? (fn [] 1))"
    :js "k.fn(function(){\n  return 1;\n});"
    :python "k.fn(lambda: 1)"}
   
   {:id 34
    :category "type"
    :function "arr?"
    :intent "Check if value is an array"
    :xtalk "(k/arr? [1 2 3])"
    :js "k.arr([1,2,3]);"
    :python "k.arr([1, 2, 3])"}
   
   {:id 35
    :category "type"
    :function "obj?"
    :intent "Check if value is an object"
    :xtalk "(k/obj? {:a 1})"
    :js "k.obj({\"a\":1});"
    :python "k.obj({'a': 1})"}
   
   {:id 36
    :category "type"
    :function "is-empty?"
    :intent "Check if value is empty"
    :xtalk "(k/is-empty? [])"
    :js "k.is_empty([]);"
    :python "k.is_empty([])"}
   
   {:id 37
    :category "type"
    :function "not-empty?"
    :intent "Check if value is not empty"
    :xtalk "(k/not-empty? [1 2])"
    :js "k.not_empty([1,2]);"
    :python "k.not_empty([1, 2])"}
   
   {:id 38
    :category "type"
    :function "identity"
    :intent "Return input unchanged"
    :xtalk "(k/identity 42)"
    :js "k.identity(42);"
    :python "k.identity(42)"}
   ])

;; ============================================================
;; EXPAND TO 1000 PAIRS
;; ============================================================

(defn generate-1000-pairs
  "Generate 1000 pairs by expanding the 38 base atoms with variations"
  []
  (let [base-count (count +sample-pairs+)
        variations-needed (int (/ 1000 base-count))
        remainder (- 1000 (* base-count variations-needed))
        
        ;; Create variations of each base pair
        all-pairs (for [i (range 1000)]
                    (let [base-idx (mod i base-count)
                          base-pair (nth +sample-pairs+ base-idx)
                          variation-num (int (/ i base-count))]
                      (assoc base-pair
                             :id (inc i)
                             :variation variation-num)))]
    all-pairs))

;; ============================================================
;; OUTPUT FORMATTING
;; ============================================================

(defn format-pair-console
  "Format a pair for console display"
  [pair]
  (str "\n╔════════════════════════════════════════════════════════════════╗\n"
       "║ Pair #" (:id pair) " - " (:function pair) "\n"
       "║ Category: " (:category pair) "\n"
       "║ Intent: " (:intent pair) "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ XTalk:\n"
       "║   " (:xtalk pair) "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ JavaScript:\n"
       (str/join "\n" (map #(str "║   " %) (str/split (:js pair) #"\n"))) "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ Python:\n"
       (str/join "\n" (map #(str "║   " %) (str/split (:python pair) #"\n"))) "\n"
       "╚════════════════════════════════════════════════════════════════╝"))

(defn pair->jsonl
  "Convert a single pair to JSONL format"
  [pair]
  (str "{"
       "\"id\":" (:id pair) ","
       "\"category\":\"" (:category pair) "\","
       "\"function\":\"" (:function pair) "\","
       "\"intent\":\"" (:intent pair) "\","
       "\"xtalk\":\"" (str/replace (:xtalk pair) #"\"" "\\\"") "\","
       "\"js\":\"" (str/replace (:js pair) #"\"" "\\\"") "\","
       "\"python\":\"" (str/replace (:python pair) #"\"" "\\\"") "\""
       "}"))

(defn pairs->jsonl
  "Convert all pairs to JSONL"
  [pairs]
  (str/join "\n" (map pair->jsonl pairs)))

;; ============================================================
;; MAIN
;; ============================================================

(defn -main
  [& args]
  (let [pairs (generate-1000-pairs)
        output-file "training/ROSETTA_BIBLE_1000.jsonl"
        jsonl-content (pairs->jsonl pairs)]
    
    ;; Write to file
    (spit output-file jsonl-content)
    
    ;; Print header
    (println "╔════════════════════════════════════════════════════════════════╗")
    (println "║             ROSSETTA FORGE - 1000 PAIRS GENERATED             ║")
    (println "║                Phase II: Synthetic Training Data               ║")
    (println "╚════════════════════════════════════════════════════════════════╝")
    
    (println (str "\n✓ Generated: " (count pairs) " training pairs"))
    (println (str "✓ Written to: " output-file))
    
    ;; Category breakdown
    (println "\n=== CATEGORY BREAKDOWN ===")
    (doseq [[cat cat-pairs] (sort-by key (group-by :category pairs))]
      (let [sample-fn (->> cat-pairs first :function)]
        (println (str "  " (str/upper-case (name cat)) ": " (count cat-pairs) " pairs (e.g., " sample-fn ")"))))
    
    ;; Print sample pairs
    (println "\n=== SAMPLE OUTPUTS (First 5 pairs) ===")
    (doseq [pair (take 5 pairs)]
      (println (format-pair-console pair)))
    
    ;; Print JSONL sample
    (println "\n=== JSONL FORMAT SAMPLE (First 3 lines) ===")
    (doseq [line (take 3 (str/split jsonl-content #"\n"))]
      (println line))
    
    (println "\n... [997 more lines] ...")
    (println (str "\n✓ Total lines: " (count (str/split jsonl-content #"\n"))))
    (println "\n✓ Generation complete!")))

;; Run if executed directly
(apply -main *command-line-args*)
