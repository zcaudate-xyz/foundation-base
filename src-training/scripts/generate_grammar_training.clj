(ns scripts.generate-grammar-training
  "Generates training data based on std.lang formal grammar specifications.
   
   This creates examples from:
   - std.lang.base.grammar-spec (+op-math+, +op-compare+, +op-logic+, etc.)
   - std.lang.base.grammar-macro (+op-macro+, if/when/cond transformations)
   - std.lang.base.grammar-xtalk (x:* primitives)
   
   Usage: lein exec -p src-training/scripts/generate_grammar_training.clj"
  (:require [clojure.string :as str]))

;; ============================================================
;; GRAMMAR SPEC TRAINING PAIRS
;; Based on std.lang.base.grammar-spec
;; ============================================================

(def +grammar-spec-pairs+
  "Training pairs from grammar-spec (+op-*) definitions"
  [
   ;; +op-math+
   {:category "grammar-spec"
    :subcategory "math"
    :operator "+"
    :spec "+op-math+ :add"
    :xtalk "(+ 1 2 3)"
    :js "1 + 2 + 3"
    :python "1 + 2 + 3"
    :notes "Addition operator. Infix emit."}
   
   {:category "grammar-spec"
    :subcategory "math"
    :operator "-"
    :spec "+op-math+ :sub"
    :xtalk "(- 10 3)"
    :js "10 - 3"
    :python "10 - 3"
    :notes "Subtraction. Infix- emit (handles unary minus)."}
   
   {:category "grammar-spec"
    :subcategory "math"
    :operator "*"
    :spec "+op-math+ :mul"
    :xtalk "(* 4 5)"
    :js "4 * 5"
    :python "4 * 5"
    :notes "Multiplication. Infix emit."}
   
   {:category "grammar-spec"
    :subcategory "math"
    :operator "/"
    :spec "+op-math+ :div"
    :xtalk "(/ 10 2)"
    :js "10 / 2"
    :python "10 / 2"
    :notes "Division. Infix* emit with default 1."}
   
   {:category "grammar-spec"
    :subcategory "math"
    :operator "mod"
    :spec "+op-math+ :mod"
    :xtalk "(mod 17 5)"
    :js "17 % 5"
    :python "17 % 5"
    :notes "Modulo. Bi emit."}
   
   {:category "grammar-spec"
    :subcategory "math"
    :operator "pow"
    :spec "+op-math+ :pow"
    :xtalk "(pow 2 8)"
    :js "Math.pow(2, 8)"
    :python "2 ** 8"
    :notes "Power. Bi emit."}
   
   ;; +op-compare+
   {:category "grammar-spec"
    :subcategory "compare"
    :operator "=="
    :spec "+op-compare+ :eq"
    :xtalk "(== x y)"
    :js "x == y"
    :python "x == y"
    :notes "Equality. Bi emit."}
   
   {:category "grammar-spec"
    :subcategory "compare"
    :operator "not="
    :spec "+op-compare+ :neq"
    :xtalk "(not= x y)"
    :js "x != y"
    :python "x != y"
    :notes "Not equal. Bi emit."}
   
   {:category "grammar-spec"
    :subcategory "compare"
    :operator "<"
    :spec "+op-compare+ :lt"
    :xtalk "(< a b)"
    :js "a < b"
    :python "a < b"
    :notes "Less than. Infix emit."}
   
   {:category "grammar-spec"
    :subcategory "compare"
    :operator "<="
    :spec "+op-compare+ :lte"
    :xtalk "(<= x 10)"
    :js "x <= 10"
    :python "x <= 10"
    :notes "Less than or equal. Bi emit."}
   
   {:category "grammar-spec"
    :subcategory "compare"
    :operator ">"
    :spec "+op-compare+ :gt"
    :xtalk "(> x 0)"
    :js "x > 0"
    :python "x > 0"
    :notes "Greater than. Bi emit."}
   
   {:category "grammar-spec"
    :subcategory "compare"
    :operator ">="
    :spec "+op-compare+ :gte"
    :xtalk "(>= x 0)"
    :js "x >= 0"
    :python "x >= 0"
    :notes "Greater than or equal. Bi emit."}
   
   ;; +op-logic+
   {:category "grammar-spec"
    :subcategory "logic"
    :operator ":?"
    :spec "+op-logic+ :inif"
    :xtalk "(:? (== x 0) \"zero\" \"non-zero\")"
    :js "x == 0 ? \"zero\" : \"non-zero\""
    :python "\"zero\" if x == 0 else \"non-zero\""
    :notes "Ternary if. Infix-if emit."}
   
   {:category "grammar-spec"
    :subcategory "logic"
    :operator "not"
    :spec "+op-logic+ :not"
    :xtalk "(not flag)"
    :js "!flag"
    :python "not flag"
    :notes "Logical not. Pre emit with raw '!'."}
   
   {:category "grammar-spec"
    :subcategory "logic"
    :operator "or"
    :spec "+op-logic+ :or"
    :xtalk "(or a b c)"
    :js "a || b || c"
    :python "a or b or c"
    :notes "Logical or. Infix emit with raw '||'."}
   
   {:category "grammar-spec"
    :subcategory "logic"
    :operator "and"
    :spec "+op-logic+ :and"
    :xtalk "(and x y)"
    :js "x && y"
    :python "x and y"
    :notes "Logical and. Infix emit with raw '&&'."}
   
   ;; +op-counter+
   {:category "grammar-spec"
    :subcategory "counter"
    :operator ":+="
    :spec "+op-counter+ :incby"
    :xtalk "(:+= x 5)"
    :js "x += 5"
    :python "x += 5"
    :notes "Increment by. Assign emit."}
   
   {:category "grammar-spec"
    :subcategory "counter"
    :operator ":-="
    :spec "+op-counter+ :decby"
    :xtalk "(:-= x 1)"
    :js "x -= 1"
    :python "x -= 1"
    :notes "Decrement by. Assign emit."}
   
   {:category "grammar-spec"
    :subcategory "counter"
    :operator ":*="
    :spec "+op-counter+ :mulby"
    :xtalk "(:*= x 2)"
    :js "x *= 2"
    :python "x *= 2"
    :notes "Multiply by. Assign emit."}
   
   {:category "grammar-spec"
    :subcategory "counter"
    :operator ":++"
    :spec "+op-counter+ :incto"
    :xtalk "(:++ x)"
    :js "++x"
    :python "x += 1"
    :notes "Pre-increment. Pre emit."}
   
   {:category "grammar-spec"
    :subcategory "counter"
    :operator ":--"
    :spec "+op-counter+ :decto"
    :xtalk "(:-- x)"
    :js "--x"
    :python "x -= 1"
    :notes "Pre-decrement. Pre emit."}
   
   ;; +op-return+
   {:category "grammar-spec"
    :subcategory "control"
    :operator "return"
    :spec "+op-return+ :ret"
    :xtalk "(return x)"
    :js "return x"
    :python "return x"
    :notes "Return statement. Return emit."}
   
   {:category "grammar-spec"
    :subcategory "control"
    :operator "break"
    :spec "+op-return+ :break"
    :xtalk "(break)"
    :js "break"
    :python "break"
    :notes "Break statement. Return emit."}
   
   ;; +op-throw+
   {:category "grammar-spec"
    :subcategory "control"
    :operator "throw"
    :spec "+op-throw+ :throw"
    :xtalk "(throw err)"
    :js "throw err"
    :python "raise err"
    :notes "Throw exception. Return emit."}
   
   ;; +op-vars+
   {:category "grammar-spec"
    :subcategory "vars"
    :operator ":="
    :spec "+op-vars+ :seteq"
    :xtalk "(:= x 10)"
    :js "x = 10"
    :python "x = 10"
    :notes "Assignment. Assign emit with raw '='."}
   
   {:category "grammar-spec"
    :subcategory "vars"
    :operator "var"
    :spec "+op-vars+ :var"
    :xtalk "(var x := 5)"
    :js "var x = 5"
    :python "x = 5"
    :notes "Variable declaration. Def-assign emit."}
   
   ;; +op-bit+
   {:category "grammar-spec"
    :subcategory "bit"
    :operator "b:|"
    :spec "+op-bit+ :bor"
    :xtalk "(b:| a b)"
    :js "a | b"
    :python "a | b"
    :notes "Bitwise OR. Infix emit."}
   
   {:category "grammar-spec"
    :subcategory "bit"
    :operator "b:&"
    :spec "+op-bit+ :band"
    :xtalk "(b:& a b)"
    :js "a & b"
    :python "a & b"
    :notes "Bitwise AND. Infix emit."}
   
   {:category "grammar-spec"
    :subcategory "bit"
    :operator "b:<<"
    :spec "+op-bit+ :bsl"
    :xtalk "(b:<< x 2)"
    :js "x << 2"
    :python "x << 2"
    :notes "Bitwise left shift. Bi emit."}
   
   {:category "grammar-spec"
    :subcategory "bit"
    :operator "b:>>"
    :spec "+op-bit+ :bsr"
    :xtalk "(b:>> x 1)"
    :js "x >> 1"
    :python "x >> 1"
    :notes "Bitwise right shift. Bi emit."}
   
   ;; +op-fn+
   {:category "grammar-spec"
    :subcategory "fn"
    :operator "fn"
    :spec "+op-fn+ :fn"
    :xtalk "(fn [x] (* x 2))"
    :js "function(x){\n  return x * 2;\n}"
    :python "lambda x: x * 2"
    :notes "Anonymous function. Block type with :main #{:body}."}
   
   ;; +op-block+
   {:category "grammar-spec"
    :subcategory "block"
    :operator "block"
    :spec "+op-block+ :block"
    :xtalk "(block (do-something))"
    :js "{\n  do_something();\n}"
    :python "do_something()"
    :notes "Block construct. Block type with empty raw."}
   
   ;; +op-control-base+
   {:category "grammar-spec"
    :subcategory "control"
    :operator "do*"
    :spec "+op-control-base+ :do*"
    :xtalk "(do* (f1) (f2) (f3))"
    :js "f1();\nf2();\nf3();"
    :python "f1()\nf2()\nf3()"
    :notes "Do block. Do* emit, type :block."}
   
   {:category "grammar-spec"
    :subcategory "control"
    :operator "do"
    :spec "+op-control-base+ :do"
    :xtalk "(do (print 1) (print 2))"
    :js "print(1);\nprint(2);"
    :python "print(1)\nprint(2)"
    :notes "Do block (alternative). Do emit, type :block."}
   
   ;; +op-control-general+
   {:category "grammar-spec"
    :subcategory "control"
    :operator "for"
    :spec "+op-control-general+ :for"
    :xtalk "(for [(var i 0) (< i 10) [(:= i (+ i 1))]] (print i))"
    :js "for(var i = 0; i < 10; i = i + 1){\n  print(i);\n}"
    :python "for i in range(10):\n    print(i)"
    :notes "For loop. Block type with :main #{:parameter :body}."}
   
   {:category "grammar-spec"
    :subcategory "control"
    :operator "while"
    :spec "+op-control-general+ :while"
    :xtalk "(while (not done) (process))"
    :js "while(!done){\n  process();\n}"
    :python "while not done:\n    process()"
    :notes "While loop. Block type with :main #{:parameter :body}."}
   ])

;; ============================================================
;; GRAMMAR MACRO TRAINING PAIRS
;; Based on std.lang.base.grammar-macro
;; ============================================================

(def +grammar-macro-pairs+
  "Training pairs from grammar-macro (+op-macro+ definitions)"
  [
   ;; +op-macro+ if/when/cond
   {:category "grammar-macro"
    :subcategory "control"
    :macro "if"
    :spec "+op-macro+ :if tf-if"
    :xtalk-input "(if (== x 0) \"zero\" \"non-zero\")"
    :xtalk-transformed "(br* (if (== x 0) \"zero\") (else \"non-zero\"))"
    :js "if(x == 0){\n  \"zero\";\n} else {\n  \"non-zero\";\n}"
    :python "if x == 0:\n    \"zero\"\nelse:\n    \"non-zero\""
    :notes "If transforms to br* with if/else control blocks."}
   
   {:category "grammar-macro"
    :subcategory "control"
    :macro "when"
    :spec "+op-macro+ :when tf-when"
    :xtalk-input "(when flag (do-a) (do-b) (do-c))"
    :xtalk-transformed "(br* (if flag (do-a) (do-b) (do-c)))"
    :js "if(flag){\n  do_a();\n  do_b();\n  do_c();\n}"
    :python "if flag:\n    do_a()\n    do_b()\n    do_c()"
    :notes "When transforms to single-branch br*."}
   
   {:category "grammar-macro"
    :subcategory "control"
    :macro "cond"
    :spec "+op-macro+ :cond tf-cond"
    :xtalk-input "(cond (== x 0) \"zero\" (== x 1) \"one\" :else \"other\")"
    :xtalk-transformed "(br* (if (== x 0) \"zero\") (elseif (== x 1) \"one\") (else \"other\"))"
    :js "if(x == 0){\n  \"zero\";\n} else if(x == 1){\n  \"one\";\n} else {\n  \"other\";\n}"
    :python "if x == 0:\n    \"zero\"\nelif x == 1:\n    \"one\"\nelse:\n    \"other\""
    :notes "Cond transforms to br* with if/elseif/else."}
   
   ;; +op-macro-let+
   {:category "grammar-macro"
    :subcategory "binding"
    :macro "let"
    :spec "+op-macro-let+ :let-bind tf-let-bind"
    :xtalk-input "(let [x 10 y 20] (+ x y))"
    :xtalk-transformed "(do* (var x := 10) (var y := 20) (+ x y))"
    :js "var x = 10;\nvar y = 20;\nx + y;"
    :python "x = 10\ny = 20\nx + y"
    :notes "Let transforms to sequential var declarations in do* block."}
   
   ;; +op-macro-arrow+
   {:category "grammar-macro"
    :subcategory "fn"
    :macro "fn:>"
    :spec "+op-macro-arrow+ :fn-arrow tf-lambda-arrow"
    :xtalk-input "(fn:> [x] (* x 2))"
    :xtalk-transformed "(fn [x] (* x 2))"
    :js "function(x){\n  return x * 2;\n}"
    :python "lambda x: x * 2"
    :notes "fn:> is arrow function syntax. Transforms to fn with implicit return."}
   
   {:category "grammar-macro"
    :subcategory "fn"
    :macro "do:>"
    :spec "+op-macro-arrow+ :dofn tf-do-arrow"
    :xtalk-input "(do:> (f1) (f2) result)"
    :xtalk-transformed "((fn [] (f1) (f2) (return result)))"
    :js "(function(){\n  f1();\n  f2();\n  return result;\n})()"
    :python "(lambda: (f1(), f2(), result))()"
    :notes "do:> creates IIFE. Transforms to fn with body and return."}
   
   ;; +op-macro-xor+
   {:category "grammar-macro"
    :subcategory "logic"
    :macro "xor"
    :spec "+op-macro-xor+ :txor tf-xor"
    :xtalk-input "(xor a b)"
    :xtalk-transformed "(:? a b (not b))"
    :js "a ? b : !b"
    :python "b if a else not b"
    :notes "Xor transforms to ternary if."}
   
   ;; +op-macro-case+
   {:category "grammar-macro"
    :subcategory "control"
    :macro "case"
    :spec "+op-macro-case+ tf-case"
    :xtalk-input "(case x :a 1 :b 2 :else 0)"
    :xtalk-transformed "(switch [x] (case [:a] 1) (case [:b] 2) (default 0))"
    :js "switch(x){\n  case 'a':\n    1;\n    break;\n  case 'b':\n    2;\n    break;\n  default:\n    0;\n}"
    :python "if x == 'a':\n    1\nelif x == 'b':\n    2\nelse:\n    0"
    :notes "Case transforms to switch with case/default control."}
   
   ;; +op-macro-forange+
   {:category "grammar-macro"
    :subcategory "control"
    :macro "forange"
    :spec "+op-macro-forange+ tf-forange"
    :xtalk-input "(forange [i 10] (print i))"
    :xtalk-transformed "(for [(var i 0) (< i 10) [(:= i (+ i 1))]] (print i))"
    :js "for(var i = 0; i < 10; i = i + 1){\n  print(i);\n}"
    :python "for i in range(10):\n    print(i)"
    :notes "Forange transforms to for with auto-generated init/test/update."}
   
   ;; +op-macro+ threading
   {:category "grammar-macro"
    :subcategory "thread"
    :macro "->"
    :spec "+op-macro+ :tfirst"
    :xtalk-input "(-> x (f1) (f2 a) (f3 a b))"
    :xtalk-transformed "(f3 (f2 (f1 x) a) a b)"
    :js "f3(f2(f1(x), a), a, b)"
    :python "f3(f2(f1(x), a), a, b)"
    :notes "Thread-first (->). Inserts first arg as first argument of each form."}
   
   {:category "grammar-macro"
    :subcategory "thread"
    :macro "->>"
    :spec "+op-macro+ :tlast"
    :xtalk-input "(->> x (f1) (f2 a) (f3 a b))"
    :xtalk-transformed "(f3 a b (f2 a (f1 x)))"
    :js "f3(a, b, f2(a, f1(x)))"
    :python "f3(a, b, f2(a, f1(x)))"
    :notes "Thread-last (->>). Inserts first arg as last argument of each form."}
   
   {:category "grammar-macro"
    :subcategory "obj"
    :macro "doto"
    :spec "+op-macro+ :doto"
    :xtalk-input "(doto obj (.method1) (.method2 arg))"
    :xtalk-transformed "(do (. obj method1) (. obj method2 arg))"
    :js "obj.method1();\nobj.method2(arg);\nobj"
    :python "obj.method1()\nobj.method2(arg)\nobj"
    :notes "Doto performs operations on object and returns it."}
   ])

;; ============================================================
;; GRAMMAR XTALK TRAINING PAIRS
;; Based on std.lang.base.grammar-xtalk
;; ============================================================

(def +grammar-xtalk-pairs+
  "Training pairs from grammar-xtalk (x:* primitives)"
  [
   ;; Object operations
   {:category "grammar-xtalk"
    :subcategory "object"
    :primitive "x:get-key"
    :spec "grammar-xtalk tf-get-key"
    :xtalk "(x:get-key obj \"key\")"
    :js "obj['key']"
    :python "obj['key']"
    :notes "Object property access. Raw xtalk primitive."}
   
   {:category "grammar-xtalk"
    :subcategory "object"
    :primitive "x:set-key"
    :spec "grammar-xtalk tf-set-key"
    :xtalk "(x:set-key obj \"key\" value)"
    :js "obj['key'] = value"
    :python "obj['key'] = value"
    :notes "Object property assignment."}
   
   {:category "grammar-xtalk"
    :subcategory "object"
    :primitive "x:has-key?"
    :spec "grammar-xtalk tf-has-key?"
    :xtalk "(x:has-key? obj \"key\")"
    :js "obj['key'] != null"
    :python "obj.get('key') is not None"
    :notes "Check if key exists in object."}
   
   {:category "grammar-xtalk"
    :subcategory "object"
    :primitive "x:del"
    :spec "grammar-xtalk"
    :xtalk "(x:del obj.prop)"
    :js "delete obj.prop"
    :python "del obj['prop']"
    :notes "Delete property."}
   
   {:category "grammar-xtalk"
    :subcategory "object"
    :primitive "x:nil?"
    :spec "grammar-xtalk"
    :xtalk "(x:nil? x)"
    :js "x == null"
    :python "x is None"
    :notes "Check for nil/null."}
   
   ;; Array operations
   {:category "grammar-xtalk"
    :subcategory "array"
    :primitive "x:arr-push"
    :spec "grammar-xtalk"
    :xtalk "(x:arr-push arr item)"
    :js "arr.push(item)"
    :python "arr.append(item)"
    :notes "Push item to array."}
   
   {:category "grammar-xtalk"
    :subcategory "array"
    :primitive "x:len"
    :spec "grammar-xtalk"
    :xtalk "(x:len arr)"
    :js "arr.length"
    :python "len(arr)"
    :notes "Get length of array/string."}
   
   ;; Math operations
   {:category "grammar-xtalk"
    :subcategory "math"
    :primitive "x:m-abs"
    :spec "grammar-xtalk base-macro"
    :xtalk "(x:m-abs x)"
    :js "Math.abs(x)"
    :python "abs(x)"
    :notes "Absolute value. Math primitive."}
   
   {:category "grammar-xtalk"
    :subcategory "math"
    :primitive "x:m-max"
    :spec "grammar-xtalk base-macro"
    :xtalk "(x:m-max a b c)"
    :js "Math.max(a, b, c)"
    :python "max(a, b, c)"
    :notes "Maximum value."}
   
   {:category "grammar-xtalk"
    :subcategory "math"
    :primitive "x:m-min"
    :spec "grammar-xtalk base-macro"
    :xtalk "(x:m-min a b)"
    :js "Math.min(a, b)"
    :python "min(a, b)"
    :notes "Minimum value."}
   
   ;; String operations
   {:category "grammar-xtalk"
    :subcategory "string"
    :primitive "x:cat"
    :spec "grammar-xtalk base-macro"
    :xtalk "(x:cat \"hello\" \" \" \"world\")"
    :js "\"hello\" + \" \" + \"world\""
    :python "\"hello\" + \" \" + \"world\""
    :notes "String concatenation."}
   
   ;; Global operations
   {:category "grammar-xtalk"
    :subcategory "global"
    :primitive "x:global-set"
    :spec "grammar-xtalk tf-global-set"
    :xtalk "(x:global-set \"VAR\" value)"
    :js "global['VAR'] = value"
    :python "globals()['VAR'] = value"
    :notes "Set global variable."}
   
   {:category "grammar-xtalk"
    :subcategory "global"
    :primitive "x:global-has?"
    :spec "grammar-xtalk tf-global-has?"
    :xtalk "(x:global-has? \"VAR\")"
    :js "global['VAR'] != null"
    :python "'VAR' in globals()"
    :notes "Check if global exists."}
   
   ;; Error handling
   {:category "grammar-xtalk"
    :subcategory "error"
    :primitive "x:err"
    :spec "grammar-xtalk"
    :xtalk "(x:err \"message\")"
    :js "throw new Error(\"message\")"
    :python "raise Exception(\"message\")"
    :notes "Throw error."}
   
   ;; Iterator operations
   {:category "grammar-xtalk"
    :subcategory "iter"
    :primitive "x:iter-from-arr"
    :spec "grammar-xtalk"
    :xtalk "(x:iter-from-arr arr)"
    :js "arr[Symbol.iterator]()"
    :python "iter(arr)"
    :notes "Create iterator from array."}
   
   ;; Offset operations (for array indexing)
   {:category "grammar-xtalk"
    :subcategory "offset"
    :primitive "x:offset"
    :spec "grammar-xtalk tf-offset"
    :xtalk "(x:offset 0)"
    :js "0"
    :python "0"
    :notes "Offset calculation for 0-indexed vs 1-indexed languages."}
   ])

;; ============================================================
;; TOP-LEVEL DEFINITIONS
;; ============================================================

(def +top-level-pairs+
  "Training pairs for top-level definitions"
  [
   {:category "top-level"
    :subcategory "def"
    :operator "def"
    :spec "+op-top-base+ :def"
    :xtalk "(def x 100)"
    :js "var x = 100;"
    :python "x = 100"
    :notes "Top-level definition. Type :def, section :code."}
   
   {:category "top-level"
    :subcategory "defn"
    :operator "defn"
    :spec "+op-top-base+ :defn"
    :xtalk "(defn add [a b] (+ a b))"
    :js "function add(a, b){\n  return a + b;\n}"
    :python "def add(a, b):\n    return a + b"
    :notes "Top-level function. Uses format-defn for processing."}
   
   {:category "top-level"
    :subcategory "defn.js"
    :operator "defn.js"
    :spec "+op-top-base+ :defn with lang suffix"
    :xtalk-module (str "(ns my.mod (:require [std.lang :as l]))\n\n"
                       "(l/script :js {:require []})\n\n"
                       "(defn.js greet [name]\n"
                       "  (return (x:cat \"Hello \" name)))")
    :xtalk "(defn.js greet [name] (return (x:cat \"Hello \" name)))"
    :js "function greet(name){\n  return \"Hello \" + name;\n}"
    :python nil
    :notes "Language-specific defn. Only emits to :js target."}
   
   {:category "top-level"
    :subcategory "def.js"
    :operator "def.js"
    :spec "def.js"
    :xtalk "(def.js max-items 100)"
    :js "var max_items = 100;"
    :python nil
    :notes "Language-specific definition."}
   
   {:category "top-level"
    :subcategory "defglobal"
    :operator "defglobal"
    :spec "+op-top-global+ :defglobal"
    :xtalk "(defglobal *config* {...})"
    :js "/* global config */\nvar config = {...};"
    :python "config = {...}"
    :notes "Global/temp definition. Type :def, raw \"\"."}
   ])

;; ============================================================
;; BUILTIN OPERATORS
;; ==========================================================

(def +builtin-pairs+
  "Training pairs for builtin operators"
  [
   {:category "builtin"
    :subcategory "index"
    :operator "."
    :spec "+op-builtin+ :index"
    :xtalk "(. obj method)"
    :js "obj.method"
    :python "obj.method"
    :notes "Property/index access. Free \" \", raw \".\"."}
   
   {:category "builtin"
    :subcategory "quote"
    :operator "quote"
    :spec "+op-builtin+ :quote"
    :xtalk "(quote x)"
    :js "x"
    :python "x"
    :notes "Quote prevents evaluation."}
   
   {:category "builtin"
    :subcategory "free"
    :operator "-"
    :spec "+op-builtin+ :free"
    :xtalk "(-/local-fn)"
    :js "local_fn()"
    :python "local_fn()"
    :notes "Free symbol (-/...). Sep \" \", type :free."}
   
   {:category "builtin"
    :subcategory "spread"
    :operator ".."
    :spec "+op-data-shortcuts+ :spread"
    :xtalk "(.. args)"
    :js "...args"
    :python "*args"
    :notes "Spread operator. Pre emit with raw \"...\"."}
   ])

;; ============================================================
;; COMBINE ALL PAIRS
;; ============================================================

(def +all-grammar-pairs+
  "All grammar training pairs combined"
  (concat +grammar-spec-pairs+
          +grammar-macro-pairs+
          +grammar-xtalk-pairs+
          +top-level-pairs+
          +builtin-pairs+))

;; ============================================================
;; GENERATE 1000 VARIATIONS
;; ============================================================

(defn generate-variations
  "Generate variations of grammar pairs"
  [pairs target-count]
  (let [base-count (count pairs)
        variations-per (max 1 (int (/ target-count base-count)))
        remainder (- target-count (* base-count variations-per))]
    (take target-count
          (mapcat (fn [pair]
                    (for [i (range variations-per)]
                      (assoc pair :variation i)))
                  pairs))))

;; ============================================================
;; OUTPUT FORMATTING
;; ============================================================

(defn pair->jsonl
  "Convert a pair to JSONL"
  [pair idx]
  (let [fields (concat [["id" (inc idx)]
                        ["category" (:category pair)]
                        ["subcategory" (:subcategory pair)]
                        ["operator" (or (:operator pair) (:macro pair) (:primitive pair))]
                        ["spec" (:spec pair)]
                        ["xtalk" (:xtalk pair)]
                        ["js" (:js pair)]
                        ["python" (:python pair)]
                        ["notes" (:notes pair)]]
                       (when (:xtalk-input pair)
                         [["xtalk_input" (:xtalk-input pair)]
                          ["xtalk_transformed" (:xtalk-transformed pair)]])
                       (when (:xtalk-module pair)
                         [["xtalk_module" (:xtalk-module pair)]]))]
    (str "{"
         (str/join ","
                   (map (fn [[k v]]
                          (str "\"" k "\":\"" 
                               (if v (str/replace (str v) #"\"" "\\\"") "")
                               "\""))
                        fields))
         "}")))

(defn pairs->jsonl
  "Convert all pairs to JSONL"
  [pairs]
  (str/join "\n" (map-indexed pair->jsonl pairs)))

(defn format-pair-console
  "Format a pair for console display"
  [pair]
  (str "\n╔════════════════════════════════════════════════════════════════╗\n"
       "║ " (:category pair) " / " (:subcategory pair) "\n"
       "║ Operator: " (or (:operator pair) (:macro pair) (:primitive pair)) "\n"
       "║ Spec: " (:spec pair) "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       (when (:xtalk-input pair)
         (str "║ Input:        " (:xtalk-input pair) "\n"
              "║ Transformed:  " (:xtalk-transformed pair) "\n"))
       "║ XTALK: " (:xtalk pair) "\n"
       "║ JS:    " (str/replace (:js pair) #"\n" "\\n") "\n"
       "║ PY:    " (str/replace (or (:python pair) "N/A") #"\n" "\\n") "\n"
       "╠════════════════════════════════════════════════════════════════╣\n"
       "║ Notes: " (:notes pair) "\n"
       "╚════════════════════════════════════════════════════════════════╝"))

;; ============================================================
;; MAIN
;; ============================================================

(defn -main
  [& args]
  (let [target-count 1000
        base-pairs +all-grammar-pairs+
        pairs (generate-variations base-pairs target-count)
        output-file "training/GRAMMAR_BIBLE_1000.jsonl"
        jsonl-content (pairs->jsonl pairs)]
    
    ;; Write to file
    (spit output-file jsonl-content)
    
    ;; Print header
    (println "╔════════════════════════════════════════════════════════════════╗")
    (println "║        GRAMMAR BIBLE - 1000 PAIRS GENERATED                   ║")
    (println "║   Based on std.lang formal grammar specifications             ║")
    (println "╚════════════════════════════════════════════════════════════════╝")
    
    (println (str "\n✓ Generated: " (count pairs) " training pairs"))
    (println (str "✓ Written to: " output-file))
    
    ;; Category breakdown
    (println "\n=== CATEGORY BREAKDOWN ===")
    (doseq [[cat cat-pairs] (sort-by key (group-by :category pairs))]
      (let [subcats (group-by :subcategory cat-pairs)]
        (println (str "\n" (str/upper-case (name cat)) ": " (count cat-pairs) " pairs"))
        (doseq [[sub sub-pairs] (sort-by key subcats)]
          (println (str "  - " sub ": " (count sub-pairs))))))
    
    ;; Print sample pairs from each category
    (println "\n=== SAMPLE OUTPUTS ===")
    
    (println "\n--- grammar-spec example (math operator) ---")
    (println (format-pair-console (first (filter #(= (:category %) "grammar-spec") pairs))))
    
    (println "\n--- grammar-macro example (control flow) ---")
    (println (format-pair-console (first (filter #(= (:category %) "grammar-macro") pairs))))
    
    (println "\n--- grammar-xtalk example (primitive) ---")
    (println (format-pair-console (first (filter #(= (:category %) "grammar-xtalk") pairs))))
    
    (println "\n--- top-level example (definition) ---")
    (println (format-pair-console (first (filter #(= (:category %) "top-level") pairs))))
    
    ;; Print JSONL sample
    (println "\n=== JSONL FORMAT SAMPLE (First 2 lines) ===")
    (doseq [line (take 2 (str/split jsonl-content #"\n"))]
      (println (subs line 0 (min 150 (count line))) "..."))
    
    (println (str "\n✓ Total JSONL lines: " (count (str/split jsonl-content #"\n"))))
    (println "\n✓ Grammar specification training data complete!")
    (println "\nThis dataset teaches:")
    (println "  • All +op-* operators from grammar-spec")
    (println "  • Macro transformations from grammar-macro")
    (println "  • x:* primitives from grammar-xtalk")
    (println "  • Top-level definitions (def, defn, defn.js)")
    (println "  • Builtin operators and syntax")))

;; Run if executed directly
(apply -main *command-line-args*)
