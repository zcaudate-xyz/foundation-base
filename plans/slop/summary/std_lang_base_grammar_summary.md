# `std.lang.base.grammar` Summary

The `std.lang.base.grammar*` namespaces are responsible for defining the structure and semantics of a language that can be emitted by the `foundation-base` transpiler. The grammar is a key component of the emit pipeline, providing the necessary information to translate a Clojure-like form into a string of code in the target language.

**Core Concepts:**

*   **Grammar:** A grammar is a Clojure map that defines the syntax and semantics of a target language. It is created using the `std.lang.base.grammar/grammar` function, which takes a tag, a map of reserved words, and a template as arguments.
*   **Reserved Words:** The `:reserved` map in the grammar defines how specific symbols are treated by the emitter. For each symbol, you can specify its `op` (operation), `emit` type (e.g., `:infix`, `:prefix`, `:macro`), `raw` string representation, and other properties.
*   **Operators (`+op-*`):** The `std.lang.base.grammar-spec`, `std.lang.base.grammar-macro`, and `std.lang.base.grammar-xtalk` namespaces define a large set of operators that can be used to build a grammar. These operators cover a wide range of functionalities, from basic arithmetic and logic to more advanced features like macros and cross-language interoperability (`xtalk`).
*   **Building a Grammar:** The `std.lang.base.grammar/build` function is used to construct a grammar by selecting a set of operators from the available `+op-*` definitions. You can include or exclude specific operator groups to create a grammar that is tailored to your needs.

**How the Grammar Fits into the Emit Pipeline:**

The grammar is a central component of the emit pipeline, and it is used at every stage of the process:

1.  **Preprocessing:**
    *   During the `to-staging` phase, the grammar's `:reserved` map is used to identify and expand macros.
    *   The grammar is also used to resolve symbols and namespaces.

2.  **Emission:**
    *   The `emit-main-loop` uses the `form-key` function to determine the key for a form, which is then used to look up the corresponding entry in the grammar's `:reserved` map.
    *   The `emit-form` function uses the information in the grammar to dispatch to the appropriate emit function (e.g., `emit-def`, `emit-fn`, `emit-block`).
    *   The form-specific emit functions use the grammar to get information about syntax, formatting, and other language-specific details. For example, `emit-infix` uses the `:raw` value from the grammar to get the string representation of the operator.

**Customization:**

The grammar provides a powerful mechanism for customizing the emit pipeline:

*   **Defining a New Language:** You can define a new language by creating a new grammar that specifies the syntax and semantics of the language.
*   **Extending an Existing Language:** You can extend an existing language by adding new operators and macros to its grammar.
*   **Overriding Default Behavior:** You can override the default behavior of the emitter by providing your own emit functions in the grammar.
*   **Controlling Formatting:** The grammar allows you to control the formatting and layout of the generated code by specifying options for indentation, spacing, and newlines.

**Language-Specific Grammars in `std.lang.model`:**

The `std.lang.model.*` files provide concrete examples of how to define grammars for different languages. These files demonstrate how to:

*   **Select a set of features:** Each language-specific grammar starts by selecting a set of features (operators) from the `+op-*` definitions using `grammar/build`. For example, `spec_c.clj` excludes `:data-shortcuts`, `:control-try-catch`, and `:class`, which are not relevant for the C language.
*   **Override and extend features:** The grammars then use `grammar/build:override` and `grammar/build:extend` to customize the selected features. For example, `spec_lua.clj` overrides the `:seteq` operator to emit `<-` instead of `=`, and it extends the grammar with Lua-specific operators like `:cat` (for string concatenation) and `:len` (for getting the length of a table).
*   **Define a template:** Each grammar defines a `+template+` map that specifies the language-specific syntax and formatting rules. This includes things like comment prefixes, statement terminators, namespace separators, and how to emit data structures like maps and vectors.
*   **Create a book:** Finally, each grammar is packaged into a `book` using `book/book`. The book contains the grammar, as well as metadata about the language, such as how to handle module imports and exports.

**Example: The Lua Grammar (`spec_lua.clj`)**

The Lua grammar provides a good example of how to customize the emit pipeline for a specific language. Here are some key features of the Lua grammar:

*   **Custom `var` macro:** The `tf-local` macro provides a more flexible way to declare local variables in Lua.
*   **Custom `for` loop macros:** The `tf-for-object`, `tf-for-array`, `tf-for-iter`, and `tf-for-index` macros provide different ways to iterate over data structures in Lua.
*   **Custom map key emission:** The `lua-map-key` function provides custom logic for emitting map keys, taking into account Lua's syntax for table keys.
*   **C FFI support:** The `tf-c-ffi` macro allows you to embed C code in your Lua code.

By studying the language-specific grammars in `std.lang.model`, you can get a good understanding of how to create your own grammars and customize the emit pipeline to support new languages or extend existing ones.