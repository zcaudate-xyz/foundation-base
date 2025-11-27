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