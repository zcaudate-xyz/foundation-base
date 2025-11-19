

# **std.lang (JS) DSL Specification**
**Objective:** This document defines the syntax, conventions, and
 constraints for writing code using the Clojure-based Javascript DSL (JS DSL),
 based on the std.lang transpiler
The dsl format is given as <DESC>\n %JS ^: <E1> ^: <E2> %DSL ^: <T1> ^: <T2>
where ^: is the start of a new line with a colon. This gives the spec for how to translate
JS code to std.lang DSL. please follow the patterns, including precidence of 
For each of the code patterns in the JS section, translate to the DSL pattern, if
there are multiple variations of DSL code, pick the DSL first over the rest.
In general, always pick the most succinct and closest translation to the original and
make sure that there are the least possible parens in the final form.
When it is possible to follow the interop, follow the interop. Don't use library
code.

### Primitives

%JS
: null
%DSL
: nil


%JS
: undefined
%DSL
: undefined


%JS
: true
%DSL
: true


%JS
: false
%DSL
: false


%JS
: "hello"
%DSL
: "hello"


%JS
: 123
%DSL
: 123


%JS
: 45.6
%DSL
: 45.6


%JS
: NaN
%DSL
: NaN


%JS
: /^he.*llo$/
%DSL
: #"^he.*llo$"


### Comparison

%JS
: a <= b
%DSL
: (<= a b)


%JS
: a == b
%DSL
: (== a b)


%JS
: a === b
%DSL
: (=== a b)


%JS
: a != b
%DSL
: (not= a b)


%JS
: a !== b
%DSL
: (not== a b)


%JS
: a > b
%DSL
: (> a b)


%JS
: a >= b
%DSL
: (>= a b)


### Assignment

%JS
: a = 10
%DSL
: (:= a 10)


%JS
: ++a
%DSL
: (:++ a)


%JS
: --a
%DSL
: (:-- a)


%JS
: a += 2
%DSL
: (:+= a 2)


%JS
: a -= 2
%DSL
: (:-= a 2)


%JS
: a *= 2
%DSL
: (:*= a 2)


### Logical

%JS
: !condition
%DSL
: (not condition)


%JS
: a || b || c
%DSL
: (or a b c)


%JS
: a && b && c
%DSL
: (and a b c)


%JS
: condition ? true_val : false_val
%DSL
: (:? condition
      true-val
      false-val)


### Math

%JS
: a + b
%DSL
: (+ a b)


%JS
: a + b + c
%DSL
: (+ a b c)


%JS
: -a
%DSL
: (- a)


%JS
: a - b
%DSL
: (- a b)


%JS
: a - b - c
%DSL
: (- a b c)


%JS
: a * b
%DSL
: (* a b)


%JS
: a * b * c
%DSL
: (* a b c)


%JS
: a / b
%DSL
: (/ a b)


%JS
: a / b / c
%DSL
: (/ a b c)


%JS
: base ^ exp
%DSL
: (pow base exp)


%JS
: a % b
%DSL
: (mod a b)


### Bit-wise

%JS
: a & b
%DSL
: (b:& a b)


%JS
: a << b
%DSL
: (b:<< a b)


%JS
: a >> b
%DSL
: (b:>> a b)


%JS
: a ^ b
%DSL
: (b:xor a b)


%JS
: a | b
%DSL
: (b:| a b)


### Arrays

%JS
: [a,b,c]
%DSL
: [a b c]


%JS
: [a,b,c,...more]
%DSL
: [a b c (:.. more)]


### Objects

%JS
: {"a":a,"b":b,"c":coll}
%DSL
: {:a a :b b :c coll}


%JS
: delete obj.prop
%DSL
: (del (. obj prop))


%JS
: this.abc
%DSL
: this.abc


%JS
: super.method()
%DSL
: (super.method)


### Assignment

%JS
: let x = 1
: var x = 1
: const x = 1
%DSL
: (var x 1)


%JS
: let {a,b,c} = opts
%DSL
: (var {:# [a b c]} opts)


%JS
: let [a,b,c] = arr
%DSL
: (var [a b c] arr)


### Functions

The map object syntax has two special keys `:#` and `:..``:#` collects all the non keyed symbol`:..` collects the spread operator for the map
%JS
: {a,b,"c":coll,...props}
%DSL
: {:# [a b] :.. props :c coll}


%JS
: {a,b,c,...props}
%DSL
: {:# [a b c] :.. props}


%JS
: async function ({a,b,"c":col,"d":dog,...props}){
    return a;
  }
%DSL
: (async (fn [{:# [a b]
               :c col
               :d dog
               :.. props}]
             (return a)))


%JS
: await function ({a,b,"c":col,"d":dog,...props}){
    return a;
  }
%DSL
: (await (fn [{:# [a b]
               :c col
               :d dog
               :.. props}]
             (return a)))


%JS
: function (){
    return 1;
  }
: () => 1
%DSL
: (fn [] (return 1))


%JS
: function (err,res){
    return res * 1;
  }
: (err, res) => res * 1
%DSL
: (fn [err res]
      (return (* res 1)))


%JS
: function (a = 1,b = 2){
    return a * b;
  }
%DSL
: (fn [(:= a 1) (:= b 2)]
      (return (* a b)))


%JS
: function ({a,b,c,d,e,...props}){
    return a * b * props.item.c;
  }
%DSL
: (fn [{:# [a b c d e] :.. props}]
      (return (* a b props.item.c)))


### Access

%JS
: this.prop
%DSL
: this.prop
: (. this prop)


%JS
: this.prop.long[1].call()
%DSL
: (. this prop long [1] (call))
: (. this.prop.long [1] (call))


%JS
: Array.from
%DSL
: Array.from


%JS
: Array.from
%DSL
: (. Array from)


%JS
: obj.item.doSomething(1,2)
%DSL
: (obj.item.doSomething 1 2)
: (. obj item (doSomething 1 2))


%JS
: notation.can.be["done"].like(1)[0].thisway
%DSL
: (. notation
     can
     be
     ["done"]
     (like 1)
     [0]
     thisway)


### Control-flow

%JS
: break
%DSL
: (break)


%JS
: return value
%DSL
: (return value)


%JS
: yield value
%DSL
: (yield value)


%JS
: for(let i = 0; i < 3; ++i){
    console.log(i);
  }
%DSL
: (for [(var i 0) (< i 3)]
    (console.log i))


%JS
: if(condition){
    console.log("true");
  }
  else{
    console.log("false");
  }
%DSL
: (if condition
    (console.log "true")
    (console.log "false"))


%JS
: if(condition){
    console.log("true");
    console.log("more");
  }
%DSL
: (when condition
    (console.log "true")
    (console.log "more"))


%JS
: if(x == 1){
    a = 1;
  }
  else{
    b = 1;
  }
%DSL
: (cond (== x 1)
        (do (:= a 1))
        :else
        (do (:= b 1)))


%JS
: while(condition){
    console.log("loop");
  }
%DSL
: (while condition
    (console.log "loop"))


%JS
: switch(val){
    case "a":
      return a;
    
    case "b":
      x = 1;
      break;
  }
%DSL
: (case val
    "a" (return a)
    "b" (do (:= x 1) (break)))


%JS
: throw new Error("message")
%DSL
: (throw (new Error "message"))


%JS
: try{
    do_something();
  }
  catch(e){
    console.log(e);
  }
%DSL
: (try (do-something)
       (catch e (console.log e)))


### Class

%JS
: obj instanceof Type
%DSL
: (instanceof obj Type)


%JS
: typeof v
%DSL
: (typeof v)


%JS
: new Constructor(a,b,c)
%DSL
: (new Constructor a b c)


### Async

%JS
: async function ({a,b,c,d,e,f,...more}){
    return a;
  }
%DSL
: (async (fn [{:# [a b c d e f] :.. more}]
             (return a)))


%JS
: await (async function ({a,b,c,d,e,f,...more}){
    return a;
  })({"a":1,"b":2})
%DSL
: (await ((quote ((async (fn [{:# [a b c d e f] :.. more}]
                             (return a)))))
          {:a 1 :b 2}))


### **1\. Core Concepts & File Structure**

All JS DSL code must be written in .clj files and wrapped in an (l/script ...) block.

* **(l/script :js ...)**: Top-level form defining a module. Configures requirements, imports, exports, and runtime behavior.  
  * **Namespace Naming:** Use concise names, preferably using kebab-case (e.g., ui-button, ui-common). **Avoid using / in namespace names** as it may cause issues with the underlying module system. 

  * **(defn.js ...)**: Defines a Javascript function or **React component**. Requires an explicit (return ...) to yield a value.  
    * **Syntax:** (defn.js FunctionName \[props\] ... (return value))  
    * **Props Destructuring:** \[\#{\[prop1 (:= prop2 defaultVal) (:.. props)\]}\]
  * **(def.js ...)**: Defines a top-level Javascript variable.  
  * **Comments**: Use standard Lisp comments (;).

  * **Example:**
  
```clojure
(ns example-project.webapp.index-page   ;; namespace, in clojure convention
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:import  [... <NATIVE LIBS> ...]  ;; more libraries
   :require [... <PACKAGE LIBS> ...
             ... <PROJECT LIBS> ...]
           })
```


<PACKAGE LIB> Examples
[js.react :as r]              ;; contains definitions for react
[js.lib.radix :as rx]         ;; contains definitions for @radix-ui/themes
[js.lib.lucide :as lr]        ;; contains definitions for lucide-react
             
<PROJECT LIB> Examples
[example-project.webapp.component-a :as c-a]  ;; internal project file a
[example-project.webapp.component-b :as c-b]  ;; internal project file b
[example-project.webapp.component-c :as c-c]] ;; internal project file c

<NATIVE LIB> Examples
["lucide-react" :as #{Input Button}]  
["react" :as React]
["react-native" :as [* ReactNative]]


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
  

## Non Clojure conventions
- do not use let form, instead the convention for `(let [a 1 b 2])` is `(var a 1) (var b 2)`

- destructuring is not `(let [{:keys [a b]} m])` but `(var {:# [a b]} m)`

- `(. notation can be ["done"] (like 1) [0] thisway)` to produce `notation.can.be["done"](like 1)[0].thisway`

### **2\. React & JSX Syntax**

* **Defining Components:** Use defn.js. **Must** include (return ...) with the JSX element.  
  (defn.js MyComponent \[props\]  
    (var color "$color1") ;; Logic here  
    (return ;; Explicit return is mandatory  
      \[:% tm/View {:padding "$4"} ;; Prefer map literal for simple props    
       \[:% tm/Text {:color color} "Hello"\]\]))

* **JSX Element Syntax:**  
  * **HTML Tags:** \[:tag {...props} ...children\] (e.g., \[:div {:class "..."} ...\])  
  * **React Components:** \[:% Component {...props} ...children\] (e.g., \[:% tm/XStack {:gap "$2"} ...\]). **:% prefix is mandatory.**  
    * **Props:** Use map literal {...} for component props unless spread (:..) or shorthand symbols are needed, in which case use the special keys 
  * **React Fragment:** \[:\<\> ...children\]  
* **Embedding Logic & Values in JSX:**  
  * **Attributes:** Use expressions directly or use the (:? ...) ternary operator. **Do NOT use if or cond blocks directly within attribute values.**  
    ;; CORRECT (Ternary Expression)  
    \[:% tm/Button {:icon (:? isLoading Spinner Icon)}\]

    ;; INCORRECT (Block) \- This will likely fail transpilation  
    ;; \[:% tm/Button {:icon (if isLoading Spinner Icon)}\]

  * **Children:**  
    * Simple expressions or variables can be placed directly: \[:% tm/Text count\]  
    * Ternary (:? ...) can be used for conditional rendering: \[:% tm/View (:? user user.name "Loading...")\] 



Given the spec in @translate_dsl.md, can you please translate src/App.jsx as well as all files in src/components/** to output files with the same directory structure in the /src-translated folder. 
  
** Please follow the spec and ignore any previous assumptions you have about std.lang. 

** DO NOT use the j/<> helper functions or import js.core. 
** use JSON.stringify an JSON.parse for functions as well!. 
** DO NOT use any sugar syntax. 
** DO NOT import xt.lang.base-lib or the k/<> helpers!. 
** translate the code as is and use interop as much as possible. keep the parens count low.
** make sure variable names are `obj.prop.item` instead of `(. (. obj prop) item)` this is very important!. 
** File names should be in snake-case. ie `code_viewer.clj` 
** Namespace name are in kebab-case. ie `smalltalkinterfacedesign.components.code-viewer`


** DO NOT use `aget` or any other clojurescript type syntax, stick to the spec. 
** function arguments and assign can be destructured using #{ setNotion } but it should not be used to construct standard maps. 
** js.react should not be required in the (ns ... ) form 
** In the `l/script` form, do not put :export or :static keys. These have been removed


** DO NOT translate exports as they are implicit
:JS
: export { Accordion, AccordionItem, AccordionTrigger, AccordionContent };
:DSL
: 

** Namespace references in the `l/script` form:
%FROM
: [smalltalkinterfacedesign.components.theme_editor :as te] 
%TO
: [smalltalkinterfacedesign.components.theme-editor :as te] 


** There is no need for :refer in the `l/script` :require map
%FROM
: [smalltalkinterfacedesign.components.theme-editor :as te :refer #{defaultTheme Theme}] 
%TO
: [smalltalkinterfacedesign.components.theme-editor :as te] 


** DO NOT export the `export interface <INTERFACE>` comment blocks. DELETE IT
%FROM
: ;; export interface ActionDefinition {
  ;;   type: 'setState' | 'toggleState' | 'incrementState' | 'customScript';
  ;;   .....
  ;; }
%TO
:

** `.` access can ONLY be applied to symbols, not forms. IMPORTANT!
%FROM
:  ((new Array (+ indent 1)).join "  ")
%TO
:  (. (new Array (+ indent 1)) (join "  "))

** `[<KEY>]` access CAN ONLY be in a `(. obj [<KEY>])` form IMPORTANT!
%FROM
:  (var tag (or tagMap[component.type] "div"))
%TO
:  (var tag (or (. tagMap [component.type]) "div"))

** :import forms need to be in vectors:
%FROM 
:  {:import (("./ui/scroll-area" :as #{ScrollArea}))}
%TO
:  {:import [["./ui/scroll-area" :as #{ScrollArea}]]}

** js.react contains all the functions in "react". use r/useState instead of importing it
%FROM
: (l/script :js
    {:import  [["react" :as #{useState}]]})
    
  (defn.js Example
    []
    (var [num setNum] (useState 0))
    (return [:button]))

%TO
: (l/script :js
    {:require  [[js.react :as r]]})
  
  (defn.js Example
    []
    (var [num setNum] (r/useState 0))
    (return [:button]))

** internal project files should be changed to a :require entry and referenced:
%FROM
: (l/script :js
    {:import  [["./components/OutlinerPanel" :as #{OutlinerPanel}]]})
    
  (defn.js Example
    []
    (return [:% OutlinerPanel]))


%TO
: (l/script :js
    {:require  [[<NAMESPACE>.<ROOT>.components.outliner-panel :as op]]})
    
  (defn.js Example
    []
    (return [:% op/OutlinerPanel]))



** DO NOT use #{[...]} for jsx use hashmaps for all properties: 
%FROM
: #{[:isVisible #{[:type "boolean"]
                  [:default true]
                  [:description "Controls visibility of description"]}]
    [:clickCount #{[:type "number"]
                   [:default 0]
                   [:description "Number of button clicks"]}]}
%TO
: {:isVisible {:type "boolean"
               :default true]
               :description "Controls visibility of description"}
   :clickCount {:type "number"
                :default 0
                :description "Number of button clicks"}}


** there should always be an explicit return:                
%FROM
: (fn [] {:type "LIBRARY_COMPONENT"
          :item {:libraryComponent comp.component}
          :collect (fn [monitor] {:isDragging (monitor.isDragging)}))
%TO
: (fn [] 
    (return {:type "LIBRARY_COMPONENT"
             :item {:libraryComponent comp.component}
             :collect (fn [monitor] {:isDragging (monitor.isDragging)})))

** any reference to a top level form within the name namespace needs to have `-/` namespace.
This means that any method that is is contained by the object (<TOPLEVEL>.method ...args) will not work and (. -/<TOPLEVEL> (method ...args))
should be used instead:
%FROM
: (def.js componentLibrary [....])

  (componentLibrary.filter
   (fn [comp] ...)
%TO
: (def.js componentLibrary [....])
  (. -/componentLibrary
   (filter ...)

** When the top level form is a function, it should be used as standard:
   
%FROM
: (defn.js someFunction [a b])

  (. -/someFunction ...)
%TO
: (defn.js someFunction [a b])

  (-/someFunction ...)

** when a form is created within the top-level form (def.js, defn.js), it is not referenced with the `-/` namespace. THIS IS IMPORTANT
%FROM
: (defn.js EXAMPLE
    []
    (var findComponentById
         (fn [components id]
           (var found (-/findComponentById component.children id))
           (return found))))
%TO
: (defn.js EXAMPLE
    []
    (var findComponentById
         (fn [components id]
           (var found (findComponentById component.children id))
           (return found))))
           
** ANOTHER EXAMPLE:
%FROM
:  (defn ... 
     (var generateStdLangCode (fn [] ...))
     (var fullCode (-/generateStdLangCode)) 
%TO
:  (defn ... 
     (var generateStdLangCode (fn [] ...))
     (var fullCode (generateStdLangCode)) ;; make sure it is not (. -/generateStdLangCode) or (. generateStdLangCode)

** all (var ... ) forms are NOT top-level ie. findComponentById, importComponent, importComponent, importAndEditComponent, addComponent, moveComponent  in app.clj and SHOULD NOT have the `-/` namespace

** the `for` does NOT behave like the clojure form. There is also NO `range`. Please stick to JS equivalents
%FROM
: (for [i (range 0 components.length)]
    (return i))
%TO   
: (for [(var i 0) (< i comps.length) (:++ i)]
    (return i)

** ["lucide-react" :as #{....}] can be replaced with the library package:
%FROM
: (l/script :js
    {:import  [["lucide-react" :as #{Search ...}]]})
    
  (defn.js Example
    []
    (return [:% Search]))


%TO
: (l/script :js
    {:require  [[js.lib.lucide :as lc]]})
    
  (defn.js Example
    []
    (return [:% lc/Search]))
    
    


