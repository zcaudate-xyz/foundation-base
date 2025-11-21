# `std.lang.base.emit` Summary

The `std.lang.base.emit*` namespaces form the core of the `foundation-base` code generation and transpilation engine. Their primary responsibility is to take a Clojure-like data structure, referred to as a "form," and translate it into a string of code in a target language. This entire process is orchestrated by a `grammar` that specifies the syntax, semantics, and customization points for the target language.

**Core Concepts:**

*   **Emit Pipeline:** The process of code generation is a multi-stage pipeline that transforms the input form into the final output string. The main entry point is `std.lang.base.emit/emit`, which takes a form, a grammar, a namespace, and a map of options.
*   **Grammar:** The `grammar` is a rich data structure (a Clojure map) that defines how to emit different forms. It contains detailed information about reserved words, operators, data structures, control flow, function definitions, and more. The grammar is the primary mechanism for customizing the output for different target languages.
*   **Dispatch:** The emit process uses a multi-dispatch mechanism to determine how to emit a given form. The `std.lang.base.emit-common/form-key` function analyzes a form and returns a key that is used to look up the appropriate emit function in the grammar's `:reserved` map.
*   **Customization:** The emit pipeline is designed to be highly customizable. Developers can override default emit functions, define new language constructs through macros, and control the formatting and layout of the generated code.

**The Emit Pipeline in Detail:**

1.  **Preprocessing (`std.lang.base.emit-preprocess`):**
    *   **`to-input`:** This is the first step, where the raw Clojure form is converted into an "input form." This involves expanding special reader macros and other syntactic sugar into a more canonical representation. For example, `@` is expanded to a `!:deref` form.
    *   **`to-staging`:** This is the second and more complex preprocessing step. It takes the input form and prepares it for emission by:
        *   Resolving symbols and namespaces.
        *   Expanding macros defined in the grammar.
        *   Handling dependencies between different code modules.
        *   Processing inline function assignments.

2.  **Emission (`std.lang.base.emit`):**
    *   **`emit-main`:** The main entry point for the emission process. It sets up the dynamic environment and calls `emit-main-loop`.
    *   **`emit-main-loop`:** This is the core recursive function that traverses the preprocessed form. For each node in the form, it calls `emit-form`.
    *   **`emit-form`:** This function is the central dispatcher. It calls `form-key` to determine the type of the current form and then dispatches to the appropriate emit function (e.g., `emit-def`, `emit-fn`, `emit-block`).

3.  **Form-Specific Emitters:**
    *   The `std.lang.base.emit-*` namespaces provide the actual implementation for emitting different types of forms.
    *   **`emit-top-level`:** Handles top-level forms like `def`, `defn`, and `defclass`.
    *   **`emit-fn`:** Handles function definitions and calls, including argument lists and type hints.
    *   **`emit-block`:** Handles block-level constructs like `do`, `if`, `for`, and `while`.
    *   **`emit-data`:** Handles the emission of data literals like maps, vectors, lists, and sets.
    *   **`emit-assign`:** Handles assignment operations.
    *   **`emit-special`:** Handles special forms like `!:eval` (for evaluating code at compile time) and `!:lang` (for embedding code from another language).

**Customizing the Pipeline:**

The emit pipeline is designed for extensive customization, primarily through the `grammar` map.

*   **The Grammar:**
    *   The grammar is a nested map that contains all the information needed to emit code for a specific language.
    *   The `:default` key in the grammar contains default settings for things like comments, common syntax elements (e.g., statement terminators, namespace separators), and function/block structure.
    *   The `:token` key allows customization of how different token types (e.g., strings, symbols, keywords) are emitted.
    *   The `:data` key controls the emission of data structures.
    *   The `:define` key controls the emission of top-level definitions.
    *   The `:block` key controls the emission of block-level constructs.

*   **Reserved Words and Operators:**
    *   The `:reserved` map in the grammar is used to define how specific symbols are treated. For each symbol, you can specify:
        *   `:emit`: The type of emission to use (e.g., `:infix`, `:prefix`, `:macro`).
        *   `:raw`: The raw string to emit for the symbol.
        *   `:macro`: A macro function to be expanded during preprocessing.
        *   `:block`: A map defining the structure of a block-level construct.

*   **Macros:**
    *   Macros provide a powerful way to extend the language. A macro is a function that is called during preprocessing and returns a new form to be emitted.
    *   Macros are defined in the `:reserved` map with an `:emit` value of `:macro` and a `:macro` key pointing to the macro function.

*   **Custom Emit Functions:**
    *   You can provide a custom emit function for any form by adding an `:emit` key to its definition in the `:reserved` map. The value of the `:emit` key should be a function that takes the form, grammar, and options map as arguments and returns the emitted string.

*   **Dynamic Variables:**
    *   Several dynamic variables can be used to control the output:
        *   `*indent*`: The current indentation level.
        *   `*compressed*`: If true, emits the code in a compressed format without newlines or extra spaces.
        *   `*trace*`: If true, prints the form being emitted at each step of the recursion.
        *   `*explode*`: If true, prints a stack trace on error.

**Example: Customizing an Operator**

To customize the `+` operator to emit `add` instead, you would modify the grammar like this:

```clojure
(def +my-grammar+
  (h/merge-nested
   helper/+default+
   {:reserved {\+ {:emit :infix :raw "add"}}}))

(emit/emit '(+ 1 2) +my-grammar+ 'my.ns {})
;; => "1 add 2"
```

This detailed control over the emission process makes the `foundation-base` transpiler a flexible and powerful tool for code generation.