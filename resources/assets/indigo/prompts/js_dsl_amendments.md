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
    
    
