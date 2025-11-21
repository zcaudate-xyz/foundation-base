# Main Translation Guidelines for JS DSL (std.lang)

This document consolidates the essential guidelines for translating JavaScript/TypeScript code into the Clojure-based Javascript DSL (JS DSL) using `std.lang`. It incorporates rules from `translate_dsl.md` and further clarifications based on practical application.

## 1. Core Concepts & File Structure

*   All JS DSL code must be in `.clj` files.
*   The top-level form for a module is `(l/script :js {...})`.
    *   **Namespace Naming:** Use kebab-case (e.g., `my-module`, `ui-button`). **Avoid `/` in namespace names.**
    *   **Top-level forms:** `(def.js ...)` for variables, `(defn.js ...)` for functions/React components.
*   Comments use standard Lisp syntax (`;`).
*   Match delimiters and indentation meticulously.

## 2. Basic Syntax & Data Structures

| Javascript               | JS DSL Equivalent      | Notes                                                    |
| :----------------------- | :--------------------- | :------------------------------------------------------- |
| `null`                   | `nil`                  |                                                          |
| `undefined`              | `undefined`            | Use the symbol `undefined`.                              |
| `true` / `false`         | `true` / `false`       |                                                          |
| `"string"`               | `"string"`             |                                                          |
| `123`, `45.6`            | `123`, `45.6`          |                                                          |
| `NaN`                    | `NaN`                  |                                                          |
| `new RegExp('...')`      | `#"..."`               |                                                          |
| `[...]` (Array)          | `[...]` (Vector)       | Standard Lisp vector becomes a JS array.                 |
| `{...}` (Object literal) | `{...}` (Hash Map)     | **CRITICAL:** Use hash maps `{}` for JS objects.         |
| `obj.prop`               | `obj.prop` or `(. obj prop)` | Prefer `obj.prop` where possible for brevity.            |
| `obj["key"]`             | `(. obj [key])`        | Use for variable keys or string literal keys.            |
| `obj.prop = val`         | `(:= obj.prop val)`    |                                                          |
| `++a`, `--a`, `a+=b`     | `(:++ a)`, `(:-- a)`, `(:+= a b)` |                                                    |

## 3. Variables & Control Flow

*   **Variable Declaration:** Always use `(var ...)` for `let`, `const`, or `var`.
    *   `let x = 1;` -> `(var x 1)`
    *   Object destructuring: `let {a,b} = obj;` -> `(var {:# [a b]} obj)`
    *   Array destructuring: `let [a,b] = arr;` -> `(var [a b] arr)`
*   **Functions:**
    *   `function () { return 1; }` or `() => 1` -> `(fn [] (return 1))` (explicit `return` is mandatory).
    *   `function (a=1) {}` -> `(fn [(:= a 1)] ...)`
    *   `async function () {}` -> `(async (fn [] ...))`
    *   `await fn()` -> `(await (fn))`
*   **Property Access:** Prefer `obj.prop` over `(. obj prop)` when `obj` is a simple symbol. Use `(. form property)` when accessing a property on the result of another form (e.g., `(. (Object.keys obj) length)`).
*   **Control Flow:**
    *   `if (c) {A}` -> `(if c A)` or `(when c A)` (for multiple statements)
    *   `if (c) {A} else {B}` -> `(if c A B)`
    *   `c ? A : B` -> `(:? c A B)` (preferred in JSX)
    *   `switch` -> `(cond ...)`
    *   `for (let i=0; i<N; ++i)` -> `(for [(var i 0) (< i N) (:++ i)] ...)`
    *   `while (c)` -> `(while c ...)`
    *   `try/catch` -> `(try ... (catch e ...))`
    *   `throw new Error(...)` -> `(throw (new Error ...))`

## 4. React & JSX Syntax

*   **Defining Components:** Use `(defn.js MyComponent [{:# [prop1] :.. props}] (return ...))`.
*   **JSX Element Syntax:**
    *   HTML tags: `[:tag {:attr "val"} ...children]`
    *   React Components: `[:% Component {:prop val} ...children]` (The `:%` prefix is mandatory).
    *   React Fragment: `[:<> ...children]`
*   **Embedding Logic in JSX:**
    *   Use `(:? ...)` for ternary logic within attributes or as children.
    *   Use array `map` (with explicit `return`) for rendering lists: `(. myArr (map (fn [item] (return [:div {:key item.id} item.name]))))`.

## 5. React Hooks DSL (`js.react :as r`)

*   `useState` -> `(var [val setVal] (r/useState initial))`
*   `useEffect` -> `(r/useEffect (fn [] ...cleanup...) [deps])`
*   `useRef` -> `(var myRef (r/useRef initial))`
*   `useContext` -> `(var ctx (r/useContext -/MyContext))`

## 6. Common Library Imports & Conventions

*   **`js.react :as r`**: For all React hooks and JSX transpilation.
*   **`js.lib.figma :as fg`**: For all `src/components/ui/` components (e.g., `Button`, `Card`, `Input`, `Tabs`, `Dialog`, `DropdownMenu`, `Avatar`, `Progress`, `Separator`, `ScrollArea`, `Alert`).
*   **`js.lib.lucide :as lc`**: For all `lucide-react` icons (e.g., `lc/User`, `lc/Bell`).
*   **Internal Project Files:** Use `[szncampaigncenter.my-module :as mm]` in `:require` (kebab-case namespace).
*   **Referencing Top-level Forms:** Use `-/MyVar` or `-/myFunction` for forms defined in the same namespace.
*   **No `j/` helpers or `js.core`**: Avoid.
*   **No `k/` helpers or `xt.lang.base-lib`**: Avoid.
*   **JSON.stringify/parse**: Use directly with interop.
*   **No sugar syntax**: Translate code as is, keeping parens count low.
*   **No `aget` or Clojure/ClojureScript-specific functions**: Stick strictly to JS equivalents.
*   **String Concatenation:** Use `(+ "string" var "string")` instead of template literals.

## 7. Key Self-Correction & Clarification Rules

*   **Explicit `return` in all anonymous functions**: If an anonymous `(fn [...] ...)` is meant to yield a value, it *must* have an explicit `(return ...)`.
*   **Property Access on Forms**: `(. (functionCall args) property)` or `(. (object.property) method)` not `(functionCall args).property`.
*   **Object Literals**: Use `{}` exclusively for JavaScript object literals and JSX props. **NEVER** use `#js {}` or `#{}` for object literals.
*   **Internal Function Calls**: Functions defined locally within a `defn.js` block are called directly by name (e.g., `(myLocalFunc arg)`), not with `./` or `-/`.
*   **JSX Props Hashmaps**: All JSX element properties must be in a hashmap `{}`.
*   **JSX Prop Spreading**: Always use `:..` for spreading props in JSX (e.g., `{:.. props}`). Do not use `(Object.assign ...)`.
*   **Filename Naming**: `.clj` files should be `snake_case`. (e.g., `my_file.clj`).
*   **Namespace Naming**: Namespaces should be `kebab-case`. (e.g., `my-project.my-module`).
*   **`l/script` import structure**:
    *   `:require` for internal project files and symbolic library references (`js.react`, `js.lib.figma`).
    *   `:import` for string-based external package imports (`"react-dnd"`).
*   **Long interop method calls**: For readability, place method calls and arguments on a new line.

This summary aims to guide future translations for accuracy and consistency with the std.lang JS DSL.