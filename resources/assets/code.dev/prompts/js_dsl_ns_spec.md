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

