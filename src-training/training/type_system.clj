(ns training.type-system
  "Type system for semantically valid expression generation.

   Provides:
   - Type definitions for xtalk values
   - Operator type signatures
   - Type compatibility checking
   - Type-consistent random expression generation"
  (:require [clojure.set :as set]))

;; ============================================================
;; TYPE DEFINITIONS
;; ============================================================

(def +type-system+
  "Values organized by type for consistent expression generation"
  {:number   [0 1 2 5 10 42 100 -1 -10 3.14 0.5]
   :boolean  [true false]
   :string   ["hello" "world" "test" "foo" "bar" ""]
   :symbol   ['x 'y 'n 'i 'val 'result 'acc 'item 'count 'total]
   :array    ['[] '[1 2 3] '["a" "b"]]
   :object   ['{} '{:a 1} '{:x 10 :y 20}]})

(def +operator-type-signatures+
  "Type signatures for grammar operators"
  {
   ;; Math operators
   '+  {:args [:number :number] :returns :number}
   '-  {:args [:number :number] :returns :number}
   '*  {:args [:number :number] :returns :number}
   '/  {:args [:number :number] :returns :number}
   'pow {:args [:number :number] :returns :number}
   'mod {:args [:number :number] :returns :number}
   
   ;; Comparison operators (comparable types)
   '==  {:args [:comparable :comparable] :returns :boolean}
   'not= {:args [:comparable :comparable] :returns :boolean}
   '<   {:args [:comparable :comparable] :returns :boolean}
   '<=  {:args [:comparable :comparable] :returns :boolean}
   '>   {:args [:comparable :comparable] :returns :boolean}
   '>=  {:args [:comparable :comparable] :returns :boolean}
   
   ;; Logic operators
   'and {:args [:boolean :boolean] :returns :boolean}
   'or  {:args [:boolean :boolean] :returns :boolean}
   'not {:args [:boolean] :returns :boolean}
   
   ;; Counter operators
   ':+   {:args [:symbol :number] :returns nil :side-effect true}
   ':-   {:args [:symbol :number] :returns nil :side-effect true}
   ':+=  {:args [:symbol :number] :returns nil :side-effect true}
   ':-=  {:args [:symbol :number] :returns nil :side-effect true}
   ':++  {:args [:symbol] :returns nil :side-effect true}
   ':--  {:args [:symbol] :returns nil :side-effect true}
   
   ;; Assignment
   ':=  {:args [:symbol :any] :returns nil :side-effect true}
   'var {:args [:symbol :any] :returns nil :side-effect true}
   
   ;; Control flow (special handling)
   'if    {:args [:boolean :any :any] :returns :any}
   'when  {:args [:boolean :any] :returns :any}
   'cond  {:args [:boolean :any :boolean :any :any] :returns :any :variadic true}
   'for   {:args [:vector :any] :returns nil :side-effect true}
   'while {:args [:boolean :any] :returns nil :side-effect true}
   })

(def +comparable-types+
  "Types that can be compared with ==, <, >, etc."
  #{:number :boolean :string})

;; ============================================================
;; TYPE HELPERS
;; ============================================================

(defn get-value-by-type
  "Get a random value of specified type"
  [type]
  (let [values (get +type-system+ type)]
    (when values
      (rand-nth values))))

(defn infer-type
  "Infer the type of a value.
   Returns keyword type or nil if unknown."
  [value]
  (cond
    (number? value) :number
    (boolean? value) :boolean
    (string? value) :string
    (symbol? value) :symbol
    (or (vector? value) (list? value) (seq? value)) :array
    (map? value) :object
    :else nil))

(defn compatible-type?
  "Check if a value is compatible with expected type"
  [value expected-type]
  (cond
    (contains? +comparable-types+ expected-type)
    (contains? +comparable-types+ (infer-type value))
    
    (= expected-type :any)
    true
    
    (= expected-type :comparable)
    (contains? +comparable-types+ (infer-type value))
    
    :else
    (case expected-type
      :number (number? value)
      :boolean (boolean? value)
      :string (string? value)
      :symbol (symbol? value)
      :array (or (vector? value) (list? value) (seq? value))
      :object (map? value)
      false)))

;; ============================================================
;; TYPE-CONSISTENT EXPRESSION GENERATION
;; ============================================================

(defn generate-simple-expr
  "Generate a simple expression of specified type.
   
   Args:
   - expected-type: keyword type (:number, :boolean, :string, :any, etc.)
   - depth: current nesting depth (default 0)
   - max-depth: maximum nesting depth (default 3)
   - compound-prob: probability of generating compound expression (default 0.3)
   
   Returns a quoted xtalk expression."
  ([expected-type]
   (generate-simple-expr expected-type 0))
  ([expected-type depth]
   (generate-simple-expr expected-type depth 3 0.3))
  ([expected-type depth max-depth compound-prob]
   (if (>= depth max-depth)
     ;; At max depth, return primitive value
     (get-value-by-type expected-type)
     ;; Otherwise, maybe generate a compound expression
     (if (< (rand) compound-prob)
       (let [ops (filter (fn [[op sig]] 
                           (= (:returns sig) expected-type))
                         +operator-type-signatures+)]
         (if (seq ops)
           (let [[op sig] (rand-nth ops)
                 args (map (fn [arg-type]
                             (generate-simple-expr arg-type (inc depth) max-depth compound-prob))
                           (:args sig))]
             (cons op args))
           (get-value-by-type expected-type)))
       (get-value-by-type expected-type)))))

(defn generate-typed-expr
  "Generate a random expression with type consistency.
   
   Options:
   - :type (default :any) - expected type of expression
   - :max-depth (default 3) - maximum nesting depth
   - :compound-prob (default 0.3) - probability of compound expression
   
   Returns a quoted xtalk expression."
  [& {:keys [type max-depth compound-prob]
      :or {type :any max-depth 3 compound-prob 0.3}}]
  (generate-simple-expr type 0 max-depth compound-prob))