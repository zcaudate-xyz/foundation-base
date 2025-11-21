# **std.lang (JS) DSL Specification - Amendments**

This document outlines specific corrections and clarifications for translating JavaScript/TypeScript code to the Clojure-based Javascript DSL (JS DSL), based on common patterns and errors encountered during recent translation tasks. Adhering to these rules is critical for accurate and idiomatic translations, complementing the existing `translate_dsl.md` specification.

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
*   **`:require`**: Use for internal project file dependencies (paths starting with `./` or referring to other project namespaces, e.g., `smalltalkinterfacedesign.components.ui.utils`). Also use for core symbolic libraries like `js.react`, `js.lib.figma`, `js.lib.lucide`, etc.
*   **`:import`**: Use for external library dependencies (paths that do **not** start with `./`, e.g., `@radix-ui/react-accordion`, `react-dnd`).
*   This is done in the `l/script` form, NOT the `ns` form.

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

## 6. `React.ComponentProps<"tag">` vs `React.ComponentProps<typeof Component>` Translation

**Rule:** TypeScript-specific type constructs like `React.ComponentProps<"tag">` or `React.ComponentProps<typeof Component>` are **ignored** in the DSL function signature. The DSL argument destructuring should only reflect the actual destructured props.

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

## 8. `React.createContext` and `React.useContext`

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

## 9. `React.useMemo`, `React.useCallback`, `React.useEffect`

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

## 10. Standard JS Functions and Global Objects (Direct Interop)

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

## 11. `as` Type Assertion

**Rule:** TypeScript's `as Type` assertion translates to `(as Type value)` in the DSL.

**Example:**

```
%FROM
: theme as ToasterProps["theme"]

%TO
: (as ToasterProps.theme theme)
```

## 12. `typeof` Operator

**Rule:** The `typeof` operator translates to `(typeof value)`.

**Example:**

```
%FROM
: typeof value === "function"

%TO
: (=== (typeof value) "function")
```

## 13. `instanceof` Operator

**Rule:** The `instanceof` operator translates to `(instanceof obj Type)`.

**Example:**

```
%FROM
: obj instanceof MyClass

%TO
: (instanceof obj MyClass)
```

## 14. `new` Keyword

**Rule:** The `new` keyword translates to `(new Constructor args...)`.

**Example:**

```
%FROM
: new Error("message")

%TO
: (new Error "message")
```

## 15. `null` and `undefined`

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

## 16. Conditional Statements (`if/else`, `cond`, `(:? ...)`)

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

## 17. `for` Loops

**Rule:** Standard JavaScript `for` loops translate directly to `(for [(var i 0) (< i count) (:++ i)] ...)`.

**Example:**

```
%FROM
: for (let i = 0; i < arr.length; i++) { console.log(arr[i]); }

%TO
: (for [(var i 0) (< i (. arr -length)) (:++ i)] (console.log (. arr [i])))
```

## 18. `const` vs `var`

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

## 19. Missing Explicit `return` in Anonymous Functions within `map`/`filter`/`reduce` etc.

**Rule:** All anonymous functions (`fn [...] ...`) that are intended to return a value, especially when used in higher-order functions like `map`, `filter`, `reduce`, `forEach`, etc., **must** have an explicit `(return ...)` statement.

**Example:**

```
%FROM
: (map (fn [child] (renderTreeNode child (+ depth 1))) component.children)
: (. components (forEach collectStates))

%TO
: (. component.children (map (fn [child] (return (renderTreeNode child (+ depth 1))))))
: (. components (forEach (fn [collectStates]) ...)) ;; If collectStates is a function, otherwise (forEach collectStates)
```

## 20. Incorrect Property Access on Forms (`. form property`)

**Rule:** The `.` access can **only** be applied to symbols (variables). When attempting to access a property (e.g., `.length`) on the result of a function call or another form, the correct syntax is `(. form property)`.

**Example:**

```
%FROM
: (Object.keys component.inputs).length
: (. message.timestamp (toLocaleTimeString [] #js {:hour "2-digit" :minute "2-digit"}))

%TO
: (. (Object.keys component.inputs) length)
: (. message.timestamp (toLocaleTimeString [] {:hour "2-digit" :minute "2-digit"}))
```

## 21. Standardize Object Merging/Updating

**Rule:** When merging or updating objects, use `Object.assign({}, target, source1, source2, ...)` without the `#js` prefix. Direct property assignment should also be preferred where applicable. For JSX props, always use the `:..` spread operator.

**Example:**

```
%FROM
: (Object.assign #js {} theme #js {:colors (Object.assign #js {} theme.colors #js {(name key) value})})

%TO
: (Object.assign {} theme {:colors (Object.assign {} theme.colors {key value})})
```

## 22. Clarify Internal Function Calls within the Same `defn.js` Block

**Rule:** Functions defined *within the same `defn.js` or `def.js` block* should be called directly by their name, without any prefix (`./` or `-/`). The `-/` prefix is reserved for top-level forms in the current namespace.

**Example:**

```
%FROM
: (var parent (./findComponentById updated parentId))
: (var newId (./importComponent component))

%TO
: (var parent (findComponentById updated parentId))
: (var newId (importComponent component))
```

## 23. Explicitly Prohibit Clojure/ClojureScript Specific Functions

**Rule:** Only standard JavaScript functions and idioms are allowed unless explicitly defined in the DSL. Explicitly prohibited Clojure/ClojureScript functions include, but are not limited to: `name`, `last`, `count`, `range`, `merge`, `aget`, `str`.

**Example:**

```
%FROM
: (name key)
: (last my-array)
: (str "hello" "world")

%TO
: key ;; assuming key is already a string
: (. my-array [(- my-array.length 1)])
: (+ "hello" "world")
```

## 24. JSX Props Must Always Be a Hashmap `{}`

**Rule:** For all JSX elements, the props object must always be represented as a hashmap `{}`. This applies to both HTML tags and React components. The hash set `#{}` is reserved for object destructuring in function arguments or `var` declarations, not for constructing literal prop objects. `Object.assign` forms are **not allowed** for props representation directly within JSX.

**Example:**

```
%FROM
: [:% fg/Button {:size "sm" :variant "outline" :onClick handleDoIt} "Do it (Ctrl+D)"]
: [:% fg/Select {:value newTriggerEvent :onValueChange setNewTriggerEvent}
   [:% fg/SelectTrigger {:className "w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
    [:% fg/SelectValue]]]
%TO
: [:% fg/Button {:size "sm" :variant "outline" :onClick handleDoIt} "Do it (Ctrl+D)"]
: [:% fg/Select {:value newTriggerEvent :onValueChange setNewTriggerEvent}
   [:% fg/SelectTrigger {:className "w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
    [:% fg/SelectValue {}]]] ;; Note the empty hashmap for SelectValue
```

## 25. ALWAYS use `:..` Spread Operator for Merging Props in JSX

**Rule:** When merging properties (props) in JSX elements, always use the `:..` spread operator within the hashmap. `Object.assign` should not be used for this purpose within JSX.

**Example:**

```
%FROM
: [:div (Object.assign {} allProps moreProps {:className (+ allProps.className " " (or resolvedProperties.className "p-4 border border-gray-700 rounded bg-[#2b2b2b]"))})]

%TO
: [:div {:className (+ allProps.className " " (or resolvedProperties.className "p-4 border border-gray-700 rounded bg-[#2b2b2b]"))
         :.. [allProps moreProps]}]
```

## 26. Component Props as Renamed Local Variables

**Rule:** If a prop `originalName` is received and needs to be used as a component (e.g., `icon: Icon`), the component should be referenced directly as `[:% Icon ...]` or `(var Icon icon)` and then `[:% Icon ...]`.

**Example:**

```
%FROM
: function DraggableComponentItem({ icon: Icon, ...props }) {
    return <Icon className="..." />;
  }
: // In JSX:
: <DraggableComponentItem icon={Box} />

%TO
: (defn.js DraggableComponentItem [{:# [icon] :.. props}]
    (var Icon icon)
    (return [:% Icon {:className "..."}]))

: ; In JSX:
: [:% -/DraggableComponentItem {:icon lc/Box}]
```

## 27. Direct Key Usage in Object Literals

**Rule:** When constructing object literals where the key is already a string variable, use the variable directly as the key without `(str key)`. The `str` function is not valid as are **ALL** clojure.core functions.

**Example:**

```
%FROM
: (Object.assign {} theme.colors {(str key) value})

%TO
: (Object.assign {} theme.colors {key value})
```

## 28. Long Interop Method Calls on New Line

**Rule:** For improved readability, when an interop call (`.`) involves a method with a long argument list or an anonymous function, place the method call and its arguments on a new line.

**Example:**

```
%FROM
: (. component.children (map (fn [child]
                               (return [:div {:key child.id} (renderComponent child)]))))

%TO
: (. component.children 
     (map (fn [child]
            (return [:div {:key child.id} (renderComponent child)]))))
```