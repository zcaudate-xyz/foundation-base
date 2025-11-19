
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