

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
  * match delimiters and indentation
  * **IGNORE** everything you know about clojure/clojurescript forms. It is only applicable in the first `ns` call. **FOLLOW THE std.lang SPEC**
  * **DO NOT** be smart about replacement or take liberties with replacements. DO NOT try to replace with clojure idioms. **FOLLOW THE std.lang SPEC**
  * String templating is not supported in the DSL. Use standard `+` notation for string concatentation.
  * **ALL** clojure.core and cljs.core functions and macro are not valid. do not use them. stick to the javascript interop and **FOLLOW THE std.lang SPEC**
  * **Example:**
  
```
(ns example-project.webapp.index-page   ;; namespace, in clojure convention
  (:require [std.lib :as h]
            [std.lang :as l]))

;; AFTER THIS IS DSL
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



** (ns ..) and (l/script ...) forms serve different purposes. ns loads std.lang
whilst l/script is responsible for setting up the js dependencies. Top level forms go OUTSIDE of the l/script form.

%FROM
: (ns smalltalkinterfacedesign.components.ui.toggle
    (:require [std.lang :as l]
              [js.react :as r]
              [smalltalkinterfacedesign.components.ui.utils :as u])
    (:import  [["@radix-ui/react-toggle@1.1.2" :as [* TogglePrimitive]]
               ["class-variance-authority@0.7.1" :as #{cva VariantProps}]]))
  
  (l/script :js
    (def.js toggleVariants
      ... )
  
    (defn.js Toggle [{:# [className variant size] :.. props}]
     ...)
%TO
: (ns smalltalkinterfacedesign.components.ui.toggle
    (:require [std.lang :as l]))
    
  
  (l/script :js
   {:require [[js.react :as r]
              [smalltalkinterfacedesign.components.ui.utils :as u]]
    :import  [["@radix-ui/react-toggle@1.1.2" :as [* TogglePrimitive]]
              ["class-variance-authority@0.7.1" :as #{cva VariantProps}]]})
  
  (def.js toggleVariants
    ... )
  
  (defn.js Toggle [{:# [className variant size] :.. props}]
    ...)


* Functions *ALWAYS* need an explicit return:
%FROM
: (fn [brand] (=== (. brand category) category))
%TO
: (fn [brand] (return (=== (. brand category) category)))

* String templating is not supported in the DSL. Use standard `+` notation for string concatentation.

%JS
: `flex ${isUser ? 'justify-end' : 'justify-start'}`
%DSL
: (+ "flex " (:? isUser "justify-end" "justify-start"))

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



** save on parens when possible: 
  - `(. e target value)` should be written as e.target.value
  - `(. (. e target) value)` should be written as e.target.value

%FROM
: (. comp.type (toLowerCase))
%TO
: (comp.type.toLowerCase)

%FROM
: (. (. Math (random)) (toString 36) (substr 2 9))
%TO
: (. (Math.random) (toString 36) (substr 2 9))

%FROM
: (. Date (now))
%TO
: (Date.now)

%FROM
: (. type (toLowerCase))
%TO
: (type.toLowerCase)

%FROM
: (. comps (splice i 0 draggedComponent))
%TO
: (comps.splice i 0 draggedComponent)

%FROM
: (. (. (. badge name) (toLowerCase)) (includes (. searchQuery (toLowerCase))))
%TO
: (. (badge.name.toLowerCase) (includes (searchQuery.toLowerCase)))

%FROM
: (. (. badge criteria) type)
%TO
: badge.criteria.type
  

** any reference to a top level form (a form defined using def.js or defn.js) within the name namespace needs to have `-/` namespace.
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




** When importing from ./**/ui/<component>, translate to a single library component `js.lib.figma`
``
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { Input } from './ui/input';
```

%FROM
: (l/script :js
  {:require [[szncampaigncenter.components.ui.card :as c]
             [szncampaigncenter.components.ui.badge :as bg]
             [szncampaigncenter.components.ui.button :as b]
             [szncampaigncenter.components.ui.input :as i]]})
  
  (defn.js Example
    []
    (return
    [:% c/Card
      [:% bg/Badge]
      [:% b/Button]
      [:% i/Input]]))
%TO
: (l/script :js
  {:require [[js.lib.figma :as fg]]})
  
  (defn.js Example
    []
    (return
    [:% fg/Card
      [:% fg/Badge]
      [:% fg/Button]

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
  
** var destructuring should only involve :# and :.. syms and the hashmap notation:
%FROM
: (var #{:char char :hasFakeCaret hasFakeCaret :isActive isActive} (or (. inputOTPContext.slots [index]) {}))
%TO
: (var #{:# [char hasFakeCaret isActive]} (or (. inputOTPContext.slots [index]) {}))

%FROM
: (var #{:error error :formItemId formItemId :formDescriptionId formDescriptionId :formMessageId formMessageId} (-/useFormField)) 
%TO
: (var {:# [error formItemId formDescriptionId formMessageId]} (-/useFormField)) 


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


** var destructuring should be consistent as function destructuring
%FROM
: (var #{:theme (:= theme "system")} (useTheme))
%TO
: (var {:# [(:= theme "system")]} (useTheme))

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
    
    


# Self-Correction Specification for JS DSL Translation

This document outlines specific corrections and clarifications for translating JavaScript/TypeScript code to the Clojure-based Javascript DSL (JS DSL), based on recent errors and user feedback. Adhering to these rules is critical for accurate and idiomatic translations.

## 1. JSX Props Syntax: Use Hash Maps `{}` for Component Properties

**Rule:** When defining properties for JSX elements (both HTML tags and React components), always use a hash map `{}`. **Never use a hash set `#{}` for this purpose.**

**Rationale:** Hash sets (`#{}`) are used for object destructuring in the DSL, not for constructing literal objects or property maps. Hash maps (`{}`) are the correct syntax for defining key-value pairs that represent component props.

**Example:**

```
%FROM
: (defn.js MyComponent [props]
  (return
   [:% MyOtherComponent #{[:prop1 val1]
                         [:prop2 val2]
                         (:.. props)}]))

%TO
: (defn.js MyComponent [props]
  (return
   [:% MyOtherComponent {:prop1 val1
                         :prop2 val2
                         :.. props}]))
```

## 2. Nested Data Structure Syntax: Use Hash Maps `{}` for Object Literals

**Rule:** For any JavaScript object literal, especially when used in `useState` initial values, `getDefaultProperties` functions, or other data structures, always translate to a hash map `{}`. **Never use a hash set `#{}` for object literals.**

**Rationale:** Similar to JSX props, hash sets are for destructuring, while hash maps are for literal object construction.

**Example:**

```
%FROM
: (var [myState setMyState]
  (r/useState
   [#{[:id "item-1"]
      [:properties #{[:color "blue"]
                     [:size 10]}]}]))

: (var getDefaultProperties
  (fn [type]
    (var defaults
      (#{[:Button #{[:children "Button"]}]}))
    (return (or (. defaults [type]) #{}))))

%TO
: (var [myState setMyState]
  (r/useState
   [{:id "item-1"
     :properties {:color "blue"
                  :size 10}}]))

: (var getDefaultProperties
  (fn [type]
    (var defaults
      {:Button {:children "Button"}})
    (return (or (. defaults [type]) {}))))
```

## 3. Import Categorization: Differentiate between `:require` and `:import`

**Rule:**
*   **`:require`**: Use for internal project file dependencies (paths starting with `./` or referring to other project namespaces, e.g., `smalltalkinterfacedesign.components.ui.utils`).
*   **`:import`**: Use for external library dependencies (paths that do **not** start with `./`, e.g., `@radix-ui/react-accordion`, `react-dnd`).
*   This is done in the `l/script` form, NOT the `ns` form
**Rationale:** This distinction is crucial for the `std.lang` transpiler to correctly resolve module paths and dependencies.

**Example:**

```
%FROM
: (l/script :js
  {:require  [[js.react :as r]
              ["@radix-ui/react-toggle-group@1.1.2" :as ToggleGroupPrimitive]
              ["class-variance-authority@0.7.1" :as #{VariantProps}]
              [smalltalkinterfacedesign.components.ui.utils :as u]
              [smalltalkinterfacedesign.components.ui.toggle :as toggle]]})

%TO
: (l/script :js
  {:require  [[js.react :as r]
              [smalltalkinterfacedesign.components.ui.utils :as u]
              [smalltalkinterfacedesign.components.ui.toggle :as toggle]]
   :import   [["@radix-ui/react-toggle-group@1.1.2" :as ToggleGroupPrimitive]
              ["class-variance-authority@0.7.1" :as #{VariantProps}]]})
```

## 4. `import * as Name from "package"` Translation

**Rule:** When translating `import * as Name from "package"`, use the `[* Name]` syntax within the `:import` vector.

**Rationale:** This specific syntax correctly maps the JavaScript `import * as` behavior in the DSL.

**Example:**

```
%FROM
: import * as AccordionPrimitive from "@radix-ui/react-accordion@1.2.3";

%TO
: (l/script :js
  {:import   [["@radix-ui/react-accordion@1.2.3" :as [* AccordionPrimitive]]]})
```

## 5. Function Argument Destructuring: Use Hashmap Notation with `:#` and `:..`

**Rule:** For function arguments that involve object destructuring, the destructuring pattern itself should be a hashmap `{}`. Within this hashmap, use `:#` to collect all non-keyed symbols into a vector, and `:..` for the rest/spread operator.

**Rationale:** This is the idiomatic way to represent JavaScript object destructuring in the DSL.

**Example:**

```
%FROM
: (defn.js ToggleGroup [{:className className :variant variant :size size :children children :.. props}]
  ...)

%TO
: (defn.js ToggleGroup [{:# [className variant size children] :.. props}]
  ...)
```

## 5.1. `var` Destructuring: Use Hashmap Notation with `:#` and `:..`

**Rule:** When performing object destructuring with `var`, the destructuring pattern itself should be a hashmap `{}`. Within this hashmap, use `:#` to collect all non-keyed symbols into a vector, and `:..` for the rest/spread operator.

**Rationale:** This ensures consistency with how object destructuring is handled in function arguments and aligns with the DSL's syntax for representing JavaScript object destructuring.

**Example:**

```
%FROM
: (var #{:char char :hasFakeCaret hasFakeCaret :isActive isActive} (or (. inputOTPContext.slots [index]) {}))
: (var #{:error error :formItemId formItemId :formDescriptionId formDescriptionId :formMessageId formMessageId} (-/useFormField))

%TO
: (var {:# [char hasFakeCaret isActive]} (or (. inputOTPContext.slots [index]) {}))
: (var {:# [error formItemId formDescriptionId formMessageId]} (-/useFormField))
```

## 6. `React.ComponentProps<"tag">` vs `React.ComponentProps<typeof Component>`

**Rule:**
*   For `React.ComponentProps<"tag">` (e.g., `"div"`, `"button"`), the corresponding DSL function argument destructuring should only include the explicitly destructured props and the `:.. props` for the rest. The type information is not directly translated into the DSL function signature.
*   For `React.ComponentProps<typeof Component>` (e.g., `typeof SliderPrimitive.Root`), the same rule applies. The `typeof` part is a TypeScript construct and doesn't have a direct DSL equivalent in the function signature.

**Example:**

```
%FROM
: function Skeleton({ className, ...props }: React.ComponentProps<"div">) { ... }
: function Slider({ className, defaultValue, ...props }: React.ComponentProps<typeof SliderPrimitive.Root>) { ... }

%TO
: (defn.js Skeleton [{:# [className] :.. props}] ...)
: (defn.js Slider [{:# [className defaultValue] :.. props}] ...)
```

## 7. `as` Keyword in Imports (Aliasing)

**Rule:** When an import uses the `as` keyword for aliasing a named import (e.g., `import { Original as Alias } from "package";`), translate it by including the alias in the named import list. If the original name is not used elsewhere, it can be omitted from the list.

**Rationale:** This ensures that the correct symbol is available in the DSL and aligns with JavaScript's aliasing behavior.

**Example:**

```
%FROM
: import { Drawer as DrawerPrimitive } from "vaul@1.1.2";
: // ... only DrawerPrimitive is used in the code ...

%TO
: (l/script :js
  {:import   [["vaul@1.1.2" :as #{DrawerPrimitive}]]})
```

```
%FROM
: import { Toaster as Sonner, ToasterProps } from "sonner@2.0.3";
: // ... both Sonner and ToasterProps are used ...

%TO
: (l/script :js
  {:import   [["sonner@2.0.3" :as #{Sonner ToasterProps}]]})
```
## 9. `React.createContext` and `React.useContext`

**Rule:**
*   `React.createContext(defaultValue)` translates to `(r/createContext defaultValue)`.
*   `React.useContext(ContextObject)` translates to `(r/useContext -/ContextObject)`.

**Example:**

```
%FROM
: const MyContext = React.createContext(null);
: const context = React.useContext(MyContext);

%TO
: (def.js MyContext (r/createContext nil))
: (var context (r/useContext -/MyContext))
```

## 10. `React.useMemo`, `React.useCallback`, `React.useEffect`

**Rule:** Translate these React hooks directly using their `r/` prefixed DSL equivalents.

**Example:**

```
%FROM
: React.useMemo(() => { ... }, [deps]);
: React.useCallback(() => { ... }, [deps]);
: React.useEffect(() => { ... }, [deps]);

%TO
: (r/useMemo (fn [] ...) [deps])
: (r/useCallback (fn [] ...) [deps])
: (r/useEffect (fn [] ...) [deps])
```

## 11. Standard JS Functions and Global Objects (Direct Interop)

**Rule:** For standard JavaScript functions and global objects like `Array`, `Math`, `Date`, `String.prototype` methods, `document`, `window`, use direct interop.

**Example:**

```
%FROM
: Array.isArray(value)
: Math.random()
: Date.now()
: value.toLowerCase()
: value.substr(2, 9)
: document.cookie = ...
: window.addEventListener(...)

%TO
: (. Array (isArray value))
: (. Math (random))
: (Date.now)
: (. value (toLowerCase))
: (. value (substr 2 9))
: (:= document.cookie ...)
: (. window (addEventListener ...))
```

## 12. `as` Type Assertion

**Rule:** TypeScript's `as Type` assertion translates to `(as Type value)` in the DSL.

**Example:**

```
%FROM
: theme as ToasterProps["theme"]

%TO
: (as ToasterProps.theme theme)
```

## 13. `typeof` Operator

**Rule:** The `typeof` operator translates to `(typeof value)`.

**Example:**

```
%FROM
: typeof value === "function"

%TO
: (=== (typeof value) "function")
```

## 14. `instanceof` Operator

**Rule:** The `instanceof` operator translates to `(instanceof obj Type)`.

**Example:**

```
%FROM
: obj instanceof MyClass

%TO
: (instanceof obj MyClass)
```

## 15. `new` Keyword

**Rule:** The `new` keyword translates to `(new Constructor args...)`.

**Example:**

```
%FROM
: new Error("message")

%TO
: (new Error "message")
```

## 16. `null` and `undefined`

**Rule:**
*   `null` translates to the `nil` symbol.
*   `undefined` translates to the `undefined` symbol.

**Example:**

```
%FROM
: const myVar = null;
: let anotherVar = undefined;

%TO
: (var myVar nil)
: (var anotherVar undefined)
```

## 17. Conditional Statements (`if/else`, `cond`, `(:? ...)`)

**Rule:**
*   Simple `if` statements with a single expression in the body can use `(if condition expr)`.
*   `if/else` statements translate to `(if condition true-expr false-expr)`.
*   `if` statements with multiple expressions in the body should use `(when condition expr1 expr2 ...)`.
*   Complex `if/else if/else` chains translate to `(cond (== x 1) (do ...) :else (do ...))`.
*   Ternary operator `condition ? true_val : false_val` translates to `(:? condition true-val false-val)`. This is preferred for inline JSX logic.

**Example:**

```
%FROM
: if (x) { console.log("true"); }
: if (x) { A; } else { B; }
: if (x === 1) { a = 1; } else if (x === 2) { b = 1; } else { c = 1; }
: const val = condition ? "A" : "B";

%TO
: (if x (console.log "true"))
: (if x A B)
: (cond (=== x 1) (:= a 1)
        (=== x 2) (:= b 1)
        :else (:= c 1))
: (var val (:? condition "A" "B"))
```

## 18. `for` Loops

**Rule:** Standard JavaScript `for` loops translate directly.

**Example:**

```
%FROM
: for (let i = 0; i < arr.length; i++) { console.log(arr[i]); }

%TO
: (for [(var i 0) (< i (. arr -length)) (:++ i)] (console.log (. arr [i])))
```

## 19. `const` vs `var`

**Rule:** Always use `var` for variable declarations in the DSL, even if the original JavaScript uses `const` or `let`.

**Rationale:** The DSL's `var` macro handles both mutable and immutable declarations appropriately during transpilation.

**Example:**

```
%FROM
: const myConst = 1;
: let myLet = 2;

%TO
: (var myConst 1)
: (var myLet 2)
```


By strictly adhering to these rules, the translation process will be more accurate and consistent with the JS DSL specification.

1.  **Unnecessary `((:? ...))` wrapping:** Conditional expressions using `(:? ...)` were incorrectly wrapped in an additional set of parentheses.
    *   **Correction:** Remove the outer parentheses.
    *   **Example:** `((:? condition true-val false-val))` -> `(:? condition true-val false-val)`

2.  **Incorrect object literal syntax (`#js {}`):** JavaScript object literals were translated using `#js {}` instead of the specified `{}`.
    *   **Correction:** Replace all instances of `#js {}` with `{}`.

3.  **Incorrect object merging/updating:** Object merging and updating logic used `Object.assign` with `#js {}` or similar verbose constructs.
    *   **Correction:** Use `Object.assign({}, target, source)` for merging, ensuring no `#js` prefix is used. Direct property assignment should also be preferred where applicable.
    
## Missing Explicit `return` in Anonymous Functions within `map`

**Error Description:**
The DSL specification mandates that all anonymous functions (`fn [...] ...`) must have an explicit `(return ...)` statement if they are intended to return a value. This was overlooked in `map` functions where the result of an inner function call was implicitly returned.

**Correction Rule:**
Ensure every anonymous function, especially those used in `map` or `filter` operations, explicitly uses `(return ...)` for its return value.

**Example of Error:**
```clojure
(map (fn [child] (renderTreeNode child (+ depth 1))) component.children)
```

**Corrected Example:**
```clojure
(map (fn [child] (return (renderTreeNode child (+ depth 1)))) component.children)
```

## Incorrect Property Access on Forms (`. form property`)

**Error Description:**
The DSL specification states that `.` access can *only* be applied to symbols. When attempting to access a property (e.g., `.length`) on the result of a function call or another form, the correct syntax is `(. form property)`. The incorrect syntax `(form).property` was used.

**Correction Rule:**
For property access on the result of a form, use the `(. form property)` syntax.

**Example of Error:**
```clojure
(> (Object.keys component.inputs).length 0)
```

**Corrected Example:**
```clojure
(> (. (Object.keys component.inputs) length) 0)
```

## Incorrect Object Merging/Updating Logic

**Error Description:**
Initial attempts at object merging and updating used `Object.assign` with `#js {}` or other verbose constructs that were not idiomatic for the DSL or contained the forbidden `#js` prefix.

**Correction Rule:**
When merging or updating objects, use `Object.assign({}, target, source1, source2, ...)` without the `#js` prefix. Ensure that the target object is an empty `{}` for a shallow copy, or use direct property assignment for specific updates.

**Example of Error (conceptual, as specific instances were corrected inline):**
```clojure
(Object.assign #js {} theme #js {:colors ...})
```

**Corrected Example:**
```clojure
(Object.assign {} theme {:colors ...})
```

## Incorrect `name` Function Usage

**Error Description:**
The Clojure `name` function was used to convert keywords to strings for object keys. This function is not available in the `std.lang` JS DSL.

**Correction Rule:**
If a variable `key` is intended to be a string for property access, use it directly. Do not use `(name key)`.

**Example of Error:**
```clojure
(Object.assign {} theme.colors {(name key) value})
```

**Corrected Example:**
```clojure
(Object.assign {} theme.colors {key value})
```

---



## Proposed Amendments:

### 1. Clarify Object Literal Syntax

**Current Spec Implication:**
The spec shows `#{...}` for object destructuring and `{:a a :b b}` for object literals. However, the use of `#js {}` was a recurring error.

**Amendment Proposal:**
Explicitly state that **only `{}` should be used for JavaScript object literals**. Add a clear warning against using `#js {}` for this purpose, as it is a ClojureScript-specific literal not applicable to the `std.lang` JS DSL.

**Reasoning:**
This was a frequent source of error, indicating a need for stronger emphasis and explicit prohibition of `#js {}` for object literals.

**Example to Add to Spec:**

```markdown
**DO NOT** use `#js {}` for object literals.

%FROM
: (. message.timestamp (toLocaleTimeString [] #js {:hour "2-digit" :minute "2-digit"}))
%TO
: (. message.timestamp (toLocaleTimeString [] {:hour "2-digit" :minute "2-digit"}))
```

### 2. Emphasize Explicit `return` in Anonymous Functions

**Current Spec Implication:**
The spec mentions "Anonymous function definitions need explicit return". However, this was sometimes overlooked, especially within `map` or `forEach` callbacks.

**Amendment Proposal:**
Strengthen the emphasis on explicit `(return ...)` for *all* anonymous functions that are expected to yield a value. Provide examples specifically within higher-order functions like `map`.

**Reasoning:**
This was a recurring error, suggesting the current wording might not be strong enough or examples are insufficient.

**Example to Add to Spec:**

```markdown
*   Anonymous function definitions *always* need an explicit return if they are meant to return a value.

%FROM
: (map (fn [child] (renderTreeNode child (+ depth 1))) component.children)
%TO
: (map (fn [child] (return (renderTreeNode child (+ depth 1)))) component.children)
```

### 3. Clarify Property Access on Forms vs. Symbols

**Current Spec Implication:**
The spec states "`obj.prop` -> `(. obj prop)`" and "`this.prop` -> `this.prop`". It also mentions "`(. notation.can.be["done"].like(1)[0].thisway)`". However, the rule "`.` access can ONLY be applied to symbols, not forms" needs to be more prominent and clearly exemplified.

**Amendment Proposal:**
Add a dedicated section or a prominent note clarifying that direct dot-chaining (`.obj.prop`) is only for symbols. For accessing properties on the *result* of a form (function call, expression), the `(. form property)` syntax must be used.

**Reasoning:**
This was a source of error, particularly with `.length` on `Object.keys()` results.

**Example to Add to Spec:**

```markdown
**CRITICAL:** `.` access can ONLY be applied to symbols, not forms. When accessing a property on the result of a function call or another expression, use the `(. form property)` syntax.

%FROM
: (Object.keys component.inputs).length
%TO
: (. (Object.keys component.inputs) length)
```

### 4. Clarify Internal Function Calls within the Same `defn.js` Block

**Current Spec Implication:**
The spec has examples for `-/` for top-level forms and no prefix for forms created within a top-level form. However, the incorrect use of `./` for internal function calls was observed.

**Amendment Proposal:**
Explicitly state that functions defined *within the same `defn.js` or `def.js` block* should be called directly by their name, without any prefix (`./` or `-/`). Reiterate that `-/` is for top-level forms.

**Reasoning:**
The `./` prefix was incorrectly applied to locally defined helper functions.

**Example to Add to Spec:**

```markdown
*   Functions defined *within the same `defn.js` or `def.js` block* should be called directly by their name, without any prefix.

%FROM
: (var parent (./findComponentById updated parentId))
: (var newId (./importComponent component))
: (var newComponent (generateNewIds component)) ;; Correct, as generateNewIds is defined within importComponent
%TO
: (var parent (findComponentById updated parentId))
: (var newId (importComponent component))
: (var newComponent (generateNewIds component))
```

### 5. Standardize Object Merging/Updating

**Current Spec Implication:**
The spec doesn't explicitly detail how to perform object merging or updating in a JS-idiomatic way within the DSL, leading to verbose or incorrect constructs.

**Amendment Proposal:**
Provide clear guidance and examples for object merging and updating using `Object.assign({}, target, source)` or the spread operator (`:..`) within object literals, ensuring no `#js` prefix is used.

**Reasoning:**
This was a common pattern in the source code and led to verbose or incorrect translations.

**Example to Add to Spec:**

```markdown
*   For object merging or updating, use `Object.assign` or the spread operator (`:..`) within object literals.

%FROM
: (Object.assign #js {} theme #js {:colors (Object.assign #js {} theme.colors #js {(name key) value})})
%TO
: (Object.assign {} theme {:colors (Object.assign {} theme.colors {key value})})
```

### 6. Clarify `Object.keys().length` vs `Object.keys().forEach()`

**Current Spec Implication:**
The spec shows `(. obj item (doSomething 1 2))` for method calls. However, the distinction between accessing a property (`.length`) and calling a method (`.forEach`) on the result of `Object.keys()` was a source of error.

**Amendment Proposal:**
Add examples demonstrating property access (`.length`) and method calls (`.forEach`) on the result of `Object.keys()` to reinforce the `(. form property)` and `(. form (method args))` syntax.

**Reasoning:**
This specific pattern caused errors due to incorrect dot-chaining.

**Example to Add to Spec:**

```markdown
*   Accessing properties or calling methods on the result of a function call:

%FROM
: (Object.keys component.inputs).length
: (Object.entries component.inputs).map(...)
%TO
: (. (Object.keys component.inputs) length)
: (. (Object.entries component.inputs) (map ...))
```

### 7. Explicitly Prohibit Clojure/ClojureScript Specific Functions

**Current Spec Implication:**
The spec mentions "DO NOT use `aget` or any other clojurescript type syntax, stick to the spec." and "DO NOT import xt.lang.base-lib or the k/<> helpers!". However, the `name` function (Clojure) was still used.

**Amendment Proposal:**
Add a more comprehensive list of explicitly prohibited Clojure/ClojureScript functions (e.g., `name`, `last`, `count`, `range`, `merge`) and reiterate that only standard JavaScript functions and idioms are allowed unless explicitly defined in the DSL.

**Reasoning:**
To prevent the accidental introduction of Clojure/ClojureScript specific functions that do not transpile correctly or are not part of the JS DSL.

**Example to Add to Spec:**

```markdown
**CRITICAL:** Only standard JavaScript functions and idioms are allowed. Explicitly prohibited Clojure/ClojureScript functions include, but are not limited to: `name`, `last`, `count`, `range`, `merge`, `aget`.

%FROM
: (name key)
: (last my-array)
%TO
: key ;; assuming key is already a string
: (. my-array [(- my-array.length 1)])
```

### 8. Prefer Native JS Interop for Array Functions

**Error Description:**
Array functions like `map`, `filter`, `reduce`, `forEach` were translated using the `(map ...)` form, which is more akin to Clojure's functional style, instead of directly using JavaScript's native array methods via interop (e.g., `array.map(...)`).

**Correction Rule:**
When performing array operations such as `map`, `filter`, `reduce`, `forEach`, always use native JavaScript array method interop. This means `(. array-variable (method-name (fn [...] (return ...)) ...))`.

**Reasoning:**
This aligns with the spec's directive to "follow the interop" and "Don't use library code" (referring to non-native JS library code for common operations already available natively) and to keep the "least possible parens".

**Example to Add to Spec:**

```markdown
*   **Prefer Native JS Interop for Array Functions:** Use native JavaScript array methods directly.

%FROM
: (map (fn [child] (return ...)) component.children)
: (. components (forEach collectStates))
%TO
: (. component.children (map (fn [child] (return ...))))
: (. components (forEach (fn [collectStates]) ...))

### 9. JSX Props Must Always Be a Hashmap `{}`

**Correction Rule:**
For all JSX elements, when props needs to be there it must always be represented as a hashmap `{}`. This applies to both HTML tags and React components. The hash set `#{}` is reserved for object destructuring in function arguments or `var` declarations, not for constructing literal prop objects. (Object.assign ...) forms are not allowed for props representation.

**Reasoning:**
This ensures consistency with the DSL's representation of JavaScript objects and avoids syntax errors during transpilation.

**Example to Add to Spec:**

```markdown
*   **JSX Props Must Always Be a Hashmap `{}`:**

%FROM
: [:% fg/Button {:size "sm" :variant "outline" :onClick handleDoIt} "Do it (Ctrl+D)"]
: [:% fg/Select {:value newTriggerEvent :onValueChange setNewTriggerEvent}
   [:% fg/SelectTrigger {:className "w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
    [:% fg/SelectValue]]
   [:% fg/SelectContent
    [:% fg/SelectItem {:value "click"} "click"]
    [:% fg/SelectItem {:value "change"} "change"]
    [:% fg/SelectItem {:value "submit"} "submit"]]]
%TO
: [:% fg/Button {:size "sm" :variant "outline" :onClick handleDoIt} "Do it (Ctrl+D)"]
: [:% fg/Select {:value newTriggerEvent :onValueChange setNewTriggerEvent}
   [:% fg/SelectTrigger {:className "w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
    [:% fg/SelectValue]] ;; Note the empty hashmap for SelectValue
   [:% fg/SelectContent
    [:% fg/SelectItem {:value "click"} "click"]
    [:% fg/SelectItem {:value "change"} "change"]
    [:% fg/SelectItem {:value "submit"} "submit"]]]
    
```

### 10. ALWAYS use `:..` Spread Operator for Merging Props in JSX

**Error Description:**
`Object.assign` was used to merge props in JSX elements, which is prohibited in the DSL. ALWAYS use `:..` spread operator is available for hashmaps.

**Correction Rule:**
When merging properties (props) in JSX elements, always use the `:..` spread operator within the hashmap.

**Reasoning:**
This aligns with the DSL's concise syntax for object manipulation and reduces verbosity.

**Example to Add to Spec:**

```markdown
*   **Prefer `:..` Spread Operator for Merging Props in JSX:**

%FROM
: [:div (Object.assign {} allProps moreProps {:className (+ allProps.className " " (or resolvedProperties.className "p-4 border border-gray-700 rounded bg-[#2b2b2b]"))})]
%TO
: [:div {:className (+ allProps.className " " (or resolvedProperties.className "p-4 border border-gray-700 rounded bg-[#2b2b2b]"))
       :.. [allProps moreProps]}]
```
### 11. Component Props as Renamed Local Variables

**Correction Rule:**
If a prop `originalName` receives a component and is destructured as `originalName: RenamedComponent` (or just `RenamedComponent` if `originalName` is not needed), the `RenamedComponent` should be declared locally (e.g., `(var RenamedComponent originalName)`) if `originalName` is part of the destructuring, or used directly if it's the target of the destructuring. When used in JSX, it should be prefixed with `:%`.

**Reasoning:**
To clarify how to handle components passed as props and then used directly within JSX after destructuring and potential renaming.

**Example to Add to Spec:**

```markdown
%FROM
: function DraggableComponentItem({ icon: Icon, ...props }) {
    return <Icon className="..." />;
  }

: // In JSX:
: <DraggableComponentItem icon={Box} />
%TO
: (defn.js DraggableComponentItem [#{:# [icon] :.. props}]
    (var Icon icon) ;; if 'icon' needs to be preserved, otherwise 'icon' can directly be 'Icon'
    (return [:% Icon {:className "..."}]))

: ; In JSX:
: [:% -/DraggableComponentItem {:icon lc/Box}]
```

### 12. Direct Key Usage in Object Literals

**Correction Rule:**
When constructing object literals where the key is already a string variable, use the variable directly as the key without `(str key)`. The `str` function is not valid as are **ALL** clojure.core functions.

**Reasoning:**
The DSL handles string variables as keys directly within object literals. Using `(str key)` is redundant and can lead to unnecessary verbosity.

**Example to Add to Spec:**

```markdown
%FROM
: (Object.assign {} theme.colors {(str key) value})
%TO
: (Object.assign {} theme.colors {key value})
```

### 13. Unified `l/script` Import/Require Categorization

**Correction Rule:**
In the `l/script` form, all symbolic library references (e.g., `js.lib.figma`, `js.tamagui`, `js.react`, `js.lib.lucide`) must be placed in the `:require` section. The `:import` section is exclusively for string-based external package imports (e.g., `"@radix-ui/react-accordion"`, `"react-dnd"`).

**Reasoning:**
To ensure consistent and correct module resolution by the `std.lang` transpiler, distinguishing between directly managed symbolic dependencies and string-based package imports.

**Example to Add to Spec:**

```markdown
%FROM
: (l/script :js
   {:require [[js.react :as r]
              [js.lib.lucide :as lc]]
    :import  [[js.lib.figma :as fg]]})
%TO
: (l/script :js
   {:require [[js.react :as r]
              [js.lib.lucide :as lc]
              [js.lib.figma :as fg]]
    :import  []})
```
### 14. Long Interop Method Calls on New Line

**Correction Rule:**
For improved readability, when an interop call (`.`) involves a method with a long argument list or an anonymous function, place the method call and its arguments on a new line.

**Reasoning:**
Enhances code readability and maintainability, especially for complex functional compositions.

**Example to Add to Spec:**

```markdown
%FROM
: (. component.children (map (fn [child]
                               (return [:div {:key child.id} (renderComponent child)]))))
%TO
: (. component.children 
     (map (fn [child]
            (return [:div {:key child.id} (renderComponent child)]))))
```

