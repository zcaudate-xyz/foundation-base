# **Javascript (JS) DSL Specification**

**Objective:** This document defines the syntax, conventions, and constraints for writing code using the Clojure-based Javascript DSL (JS DSL), based on the std.lang transpiler

### **1\. Core Concepts & File Structure**

All JS DSL code must be written in .clj files and wrapped in an (l/script ...) block.

* **(l/script :js ...)**: Top-level form defining a module. Configures requirements, imports, exports, and runtime behavior.  
  * **Namespace Naming:** Use concise names, preferably using kebab-case (e.g., ui-button, ui-common). **Avoid using / in namespace names** as it may cause issues with the underlying module system.  
  * **Example:**  
    (ns ui-common ;; Prefer kebab-case names  
      (:require \[std.lang :as l\] ...))

    (l/script :js  
      {:require \[\[js.tamagui :as tm\]  
                 \[js.react :as r\]\]  
       :import  \[\["@tamagui/lucide-icons" :as \#{House Menu}\]\]  
       :static {:flags {:nextjs {:use-client true}}}})

* **(defn.js ...)**: Defines a Javascript function or **React component**. Requires an explicit (return ...) to yield a value.  
  * **Syntax:** (defn.js FunctionName \[props\] ... (return value))  
  * **Props Destructuring:** \[\#{\[prop1 (:= prop2 defaultVal) (:.. props)\]}\]  
* **(defmacro.js ...)**: Defines a DSL macro for compile-time code transformation.  
* **(def.js ...)**: Defines a top-level Javascript variable.  
* **Comments**: Use standard Lisp comments (;).

### **2\. Basic Syntax & Data Structures**

Accuracy is critical. Pay close attention to the syntax for Javascript Objects.

| Javascript | JS DSL Equivalent | Notes & Examples |
| :---- | :---- | :---- |
| null | nil | Transpiles to null. |
| undefined | undefined | Use the symbol undefined. |
| true / false | true / false |  |
| "string" | "string" |  |
| 123, 45.6 | 123, 45.6 |  |
| \[...\] (Array) | \[...\] (Vector) | Standard Lisp vector becomes a JS array. **Exception:** See JSX syntax below. |
| {...} (Object) | {...} **or** \#{...} | CRITICAL Distinction: 1\. Simple Key-Value: Prefer standard Clojure map literal {...} when only defining explicit key-value pairs. Keys can be keywords (:key), strings ("key"), or evaluated symbols. \-\> {:key1 val1 "key-2" val2} \-\> {key1: val1, "key-2": val2} 2\. Shorthand Symbols, Spreads, or Mixed: Use the Lisp set literal \#{...} only when the definition involves shorthand symbols (variables in scope) or spread syntax (:..). \-\> \#{a b} \-\> {a: a, b: b} \-\> \#{(:.. props)} \-\> {...props} \-\> \#{\[a :b 2 (:.. rest)\]} \-\> {a: a, b: 2, ...rest} |
| obj.prop | (. obj prop) | Dotted property access. |
| obj\["key"\] | (. obj \[key\]) | Bracket property access (for variables or string keys). |
| obj.prop \= val | (:= (. obj prop) val) | Assignment operator. |
| regex | \#"pattern" | e.g., \#"^\[a-z\]" |
| return value; | (return value) | **CRITICAL:** Explicit return is **required** in functions (defn.js, fn, fn:\>) and inside block constructs (if, cond, for, k/for:\*) to yield a value. The last expression is *not* implicitly returned. |

### **3\. Variables & Control Flow (Blocks vs Expressions)**

**Key Distinction:** Unlike Clojure/Script where if, cond, for, etc., are *expressions* that return values, in this JS DSL, they are primarily **statement blocks**. They execute code but do not inherently return a value unless an explicit (return ...) is used within their branches. The ternary operator (:? ...) *is* an expression.

| Javascript | JS DSL Equivalent | Notes & Examples |
| :---- | :---- | :---- |
| let x \= 1; | (var x 1\) | var macro for let. |
| const x \= 1; | (const x 1\) | const macro. |
| let \[a,b\] \= arr; | (var \[a b\] arr) | Array destructuring. |
| let {a,b} \= obj; | (var \#{a b} obj) | Object destructuring. |
| fn(a, b) | (fn a b) | Standard function call. |
| (err, res) \=\> { return ... } | (fn \[err res\] ... (return ...)) | Anonymous function. Requires (return ...). |
| () \=\> { return ... } | (fn \[\] ... (return ...)) or (fn:\> ... (return ...)) | fn:\> shorthand. Requires (return ...). |
| if (x) { A } | (if x A) | Block statement. Does not return a value itself. Use (return ...) inside A if needed. |
| if (x) { A } else { B } | (if x A B) | Block statement. Use (return ...) inside A or B. |
| x ? A : B | (:? x A B) | **Expression:** This ternary operator *returns* A or B. Suitable for inlining where a value is needed (e.g., JSX attributes, variable assignment). |
| switch (x) { case 1: ... } | (cond (== x 1\) ... (== x 2\) ... :else ...) | Block statement. Each clause executes code. Use (return ...) within clauses to yield a value from the containing function/block. |
| for (...) { ... } | (for \[...\] ...) | Block statement (JS for loop). Use (return ...) *outside* the loop or modify variables within the loop. Does not yield a collection. |
| (JS for...of) | (k/for:array \[\[i e\] arr\] ...) | **Block Macro:** Iterates but does *not* return a collection directly. Primarily for side effects or building a collection manually (e.g., using k/arr-push). Requires (return ...) *outside* the loop if the containing function needs to return something. |
| (JS for...in) | (k/for:object \[\[k v\] obj\] ...) | **Block Macro:** Similar to k/for:array, iterates for side effects. Does not return a value. Requires (return ...) *outside*. |
| x \=== y | (=== x y) | Expression. Triple equals. (== ...), (\!== ...), (\!= ...) also exist. |
| try { ... } catch (e) { ... } | (try ... (catch e ...)) | Block statement. |
| new Promise(...) | (new Promise (fn \[resolve reject\] ...)) | Expression. |
| await fn() | (await (fn)) | Expression. |

* When refering to a previously defined <element> in the same namespace, explicity link to it with -/<element>, using '-' for the current namespace.
  (def.js Hello "hello")
  
  ;; CORRECT  
  (defn.js HelloFn [] (return -/Hello))
  
  ;; INCORRECT  
  (defn.js HelloFn [] (return Hello))

* Append using '+' not 'str'
  ;; CORRECT  
  (+ "hello" "world")
  
  ;; INCORRECT
  (str "hello" "world")
  
* Anonymous function definitions need explicit return
  ;; CORRECT  
  (fn [] (return 1))
  
  ;; INCORRECT
  (fn [] 1)
  
        
- do not use let form, instead the convention for `(let [a 1 b 2])` is `(var a 1) (var b 2)`

- destructuring is not `(let [{:keys [a b]} m])` but `(var #{a b} m)`

- `(. notation can be ["done"] (like 1) [0] thisway)` to produce `notation.can.be["done"](like 1)[0].thisway`

- don't import the 'js.core' namespace, instead use javascript interop functions.

- async anonymous functions always need a name: `(fn ^{:- [async]} check [] (return 1))`
