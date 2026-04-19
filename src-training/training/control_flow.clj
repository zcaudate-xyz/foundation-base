(ns training.control-flow
  "Control flow construct generators for training data.

   Provides:
   - Individual generators for if, when, cond, for, while, let, defn, fn, :=, var
   - Combined random control flow form generator
   - Type-consistent generation using training.type-system"
  (:require [training.type-system :as ts]))

;; ============================================================
;; INDIVIDUAL CONTROL FLOW GENERATORS
;; ============================================================

(defn gen-if
  "Generate if expression: (if cond then else)
   
   Returns quoted form."
  []
  (let [cond-expr (ts/generate-simple-expr :boolean 0)
        then-expr (ts/generate-simple-expr :any 1)
        else-expr (ts/generate-simple-expr :any 1)]
    `(if ~cond-expr ~then-expr ~else-expr)))

(defn gen-when
  "Generate when expression: (when cond body)
   
   Returns quoted form."
  []
  (let [cond-expr (ts/generate-simple-expr :boolean 0)
        body-expr (ts/generate-simple-expr :any 1)]
    `(when ~cond-expr ~body-expr)))

(defn gen-cond
  "Generate cond expression: (cond test1 result1 test2 result2 :else default)
   
   Returns quoted form."
  []
  (let [test1 (ts/generate-simple-expr :boolean 0)
        result1 (ts/generate-simple-expr :any 1)
        test2 (ts/generate-simple-expr :boolean 0)
        result2 (ts/generate-simple-expr :any 1)
        default (ts/generate-simple-expr :any 1)]
    `(cond ~test1 ~result1 ~test2 ~result2 :else ~default)))

(defn gen-for
  "Generate for loop: (for [(var i := 0) (< i 10) [(:= i (+ i 1))]] body)
   
   Returns quoted form."
  []
  (let [var-sym (rand-nth (:symbol ts/+type-system+))
        init 0
        test `(< ~var-sym 10)
        update `[(:= ~var-sym (+ ~var-sym 1))]
        body (ts/generate-simple-expr :any 1)]
    `(for [(var ~var-sym := ~init) ~test ~update] ~body)))

(defn gen-while
  "Generate while loop: (while cond body)
   
   Returns quoted form."
  []
  (let [cond-expr (ts/generate-simple-expr :boolean 0)
        body-expr (ts/generate-simple-expr :any 1)]
    `(while ~cond-expr ~body-expr)))

(defn gen-let
  "Generate let binding: (let [var expr] body)
   
   Returns quoted form."
  []
  (let [bindings [(rand-nth (:symbol ts/+type-system+))
                  (ts/generate-simple-expr :any 0)]
        body (ts/generate-simple-expr :any 1)]
    `(let ~(vec bindings) ~body)))

(defn gen-defn
  "Generate function definition: (defn name [args...] (return body))
   
   Returns quoted form."
  []
  (let [fn-name (symbol (str "fn" (rand-int 1000)))
        args (vec (take (rand-int 3) (repeatedly #(rand-nth (:symbol ts/+type-system+)))))
        body `(return ~(ts/generate-simple-expr :any 1))]
    `(defn ~fn-name ~args ~body)))

(defn gen-fn
  "Generate anonymous function: (fn [args...] (return body))
   
   Returns quoted form."
  []
  (let [args (vec (take (rand-int 3) (repeatedly #(rand-nth (:symbol ts/+type-system+)))))
        body `(return ~(ts/generate-simple-expr :any 1))]
    `(fn ~args ~body)))

(defn gen-assign
  "Generate assignment: (:= var expr)
   
   Returns quoted form."
  []
  (let [var (rand-nth (:symbol ts/+type-system+))
        val (ts/generate-simple-expr :any 0)]
    `(:= ~var ~val)))

(defn gen-var
  "Generate variable declaration: (var var := expr)
   
   Returns quoted form."
  []
  (let [var (rand-nth (:symbol ts/+type-system+))
        val (ts/generate-simple-expr :any 0)]
    `(var ~var := ~val)))

;; ============================================================
;; RANDOM CONTROL FLOW GENERATOR
;; ============================================================

(def +generators+
  "Map of generator names to functions"
  {:if    gen-if
   :when  gen-when
   :cond  gen-cond
   :for   gen-for
   :while gen-while
   :let   gen-let
   :defn  gen-defn
   :fn    gen-fn
   :=     gen-assign
   :var   gen-var})

(defn generate-control-flow-form
  "Generate a random control flow form with type consistency.
   
   Options:
   - :types (coll) - restrict to specific form types (keywords from +generators+)
   - :weights (map) - relative weights for each generator type
   
   Returns quoted form."
  [& {:keys [types weights]}]
  (let [allowed-types (or types (keys +generators+))
        generators (map +generators+ allowed-types)
        weighted (if weights
                   (map (fn [type] (get weights type 1)) allowed-types)
                   (repeat 1))]
    (when (seq generators)
      (let [chosen (rand-nth (mapcat (fn [gen weight] (repeat weight gen)) generators weighted))]
        (chosen)))))

(defn generate-control-flow-pair
  "Generate a control flow pair ready for training.
   
   Uses emit-to-both from training.util to produce translations.
   
   Returns map with keys:
   - :type - form type keyword
   - :form - quoted form
   - :xtalk - string representation of form
   - :js - JavaScript translation (if emission succeeded)
   - :python - Python translation (if emission succeeded)
   - :valid - boolean indicating successful emission
   - :error - error message if emission failed"
  [& opts]
  (let [form (apply generate-control-flow-form opts)
        type (some (fn [[k f]] (when (= f (get +generators+ k)) k)) +generators+)]
    {:type type
     :form form
     :xtalk (pr-str form)}))