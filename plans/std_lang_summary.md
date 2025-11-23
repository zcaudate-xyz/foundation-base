# std.lang Summary

`std.lang` is the core transpilation and multi-language support engine of the `foundation-base` project. It allows writing code in a Lisp-like DSL (Clojure-based) and transpiling it to various target languages such as JavaScript, Lua, Python, R, and Solidity.

## 1. Core Architecture

The architecture revolves around a few key concepts:

### 1.1. Book
A "Book" (`std.lang.base.book`) is the central data structure that holds the definition of a language environment. It contains:
*   **`:lang`**: The target language identifier (e.g., `:js`, `:lua`).
*   **`:grammar`**: The grammar specification used for emission.
*   **`:modules`**: A collection of modules, which in turn contain entries (functions, variables).
*   **`:parent`**: Inheritance from other languages (e.g., `:js` inherits from `:xtalk`).

### 1.2. Grammar
The grammar (`std.lang.base.grammar`) defines how Lisp forms are translated into the target language's syntax. It includes:
*   **Reserved words**: Mappings for operators (`+`, `-`, `===`), control flow (`if`, `for`), and declarations.
*   **Emission rules**: Custom functions to handle specific data structures (vectors, maps) or constructs (regex).
*   **Templates**: Configuration for code formatting (indentation, separators).

### 1.3. Emission
The emission process (`std.lang.base.emit`) takes a Lisp form and converts it to a string string based on the grammar.
*   **`emit/emit`**: Top-level function.
*   **`emit-main-loop`**: Recursively processes forms.
*   **Pre-processing**: Macros and syntactic sugar are expanded before emission.

### 1.4. Scripting
The `std.lang` DSL allows defining code within Clojure namespaces using `l/script`.
*   **`l/script`**: Defines a module for a specific language.
*   **`defn.<lang>` / `def.<lang>`**: Defines functions and variables for that language.
*   **Interoperability**: Code can import dependencies from other modules within the "Book".

## 2. Supported Languages

Languages are defined in `src/std/lang/model/`. Key implementations include:

*   **JavaScript (`:js`)**: Defined in `spec_js.clj`. Supports JSX (via `std.lang.model.spec-js.jsx`), QML, and standard JS constructs.
*   **Lua (`:lua`)**: Defined in `spec_lua.clj`.
*   **Python (`:python`)**: Defined in `spec_python.clj`.
*   **Solidity (`:solidity`)**: Used for Web3 development.
*   **XTalk (`:xtalk`)**: A base "cross-talk" language that others can inherit from, providing common functionality.

## 3. Standard JS Library (`src/js`)

The `src/js` directory contains the standard library definitions for the JavaScript target. These are **not** raw JavaScript files but Clojure files defining JS modules via the DSL.

### 3.1. Key Components (`src/js/`)
*   **Core (`src/js/core/`)**:
    *   `core.clj`: Fundamental runtime utilities (macros for `future`, `timeout`, `assignNew`, etc.).
    *   `dom.clj`, `fetch.clj`, `impl.clj`: DOM manipulation, Fetch API wrappers, and implementation details.
*   **React (`src/js/react/`)**:
    *   `react.clj`: Main React bindings.
    *   `layout.clj`, `ext_*.clj`: Layout components and extensions (box, cell, form, route, view).
    *   `compile_*.clj`: Compiler helpers for React components, directives, and states.
*   **React Native (`src/js/react_native/`)**:
    *   `react_native.clj`: Main RN bindings.
    *   `ui_*.clj`: UI components (button, input, frame, slider, etc.).
    *   `helper_*.clj`: Helpers for theming, mobile interactions, and animations.
*   **Libraries (`src/js/lib/`)**:
    *   Wide range of bindings for external libraries: `three` (Three.js), `valtio` (State), `lucide` (Icons), `d3`, `p5`, `supabase`.
    *   `webpack.clj`: Webpack configuration helpers.
*   **UI Kits**:
    *   `tamagui` (`src/js/tamagui/`): Bindings for the Tamagui UI kit.
    *   `blessed` (`src/js/blessed/`): Bindings for the Blessed (TUI) library.

### 3.2. Role in Transpilation
These files act as "headers" or "standard libraries" for the transpiler. When you write `(l/script :js ...)` in your own code, you often implicitly or explicitly rely on these definitions (e.g., using `js.core/future` or `js.react/useState`). They provide the "bridge" between the DSL constructs and the underlying JS ecosystem.

## 4. Workflow

1.  **Definition**: Code is written in `.clj` files using `l/script` blocks.
2.  **Loading**: The `std.lang` runtime loads these definitions into the global registry (or a specific Book).
3.  **Transpilation**: When needed (e.g., for deployment or hot-reloading), the code is transpiled to the target language string.
4.  **Execution**:
    *   **Static**: Transpiled files are saved to disk.
    *   **Dynamic**: Code can be executed live against a runtime (e.g., sending JS to a browser or Node.js process, Lua to Nginx).

## 5. Key Files

*   `src/std/lang.clj`: Public API.
*   `src/std/lang/base/book.clj`: Book data structure and management.
*   `src/std/lang/base/emit.clj`: Core emission logic.
*   `src/std/lang/base/grammar.clj`: Grammar definition.
*   `src/std/lang/model/spec_*.clj`: Language-specific implementations.

## 6. Usage for Jules

When working with `std.lang`:
*   **Read the Spec**: Check `spec_<lang>.clj` to understand available macros and custom syntax (e.g., how `#{}` or `[]` are handled).
*   **Use `l/script`**: Isolate DSL code in `l/script` blocks.
*   **Debug Emission**: Use `(emit/emit form grammar ...)` to verify how a form is transpiled if unsure.
