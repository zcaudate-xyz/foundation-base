## std.lang: A Comprehensive Summary (including submodules)

The `std.lang` module is the core of the `foundation-base` ecosystem, providing a powerful and extensible framework for defining, transpiling, and managing multiple programming languages. It enables developers to write code in a Clojure-like DSL and then generate equivalent code in various target languages (JavaScript, Lua, Python, C, Rust, GLSL, Bash, Scheme, JQ, PostgreSQL, etc.). This module is central to the project's goal of creating a polyglot development environment with unified tooling and runtime management.

### `std.lang` (Main Namespace)

This namespace orchestrates the functionality of its submodules, providing a high-level interface for language definition, code generation, and runtime interaction. It re-exports key functions from its sub-namespaces, making it a convenient entry point for language-oriented programming.

**Key Re-exported Functions:**

*   From `std.lang.base.util`: `sym-full`, `sym-id`, `sym-module`, `sym-pair`, `sym-default-str`, `ptr`.
*   From `std.lang.base.emit-common`: `with:explode`, `with-trace`.
*   From `std.lang.base.emit`: `with:emit`, `emit*`.
*   From `std.lang.base.emit-helper`: `basic-typed-args`, `emit-type-record`.
*   From `std.lang.base.emit-preprocess`: `macro-form`, `macro-opts`, `macro-grammar`, `with:macro-opts`.
*   From `std.lang.base.impl`: `emit-script`, `emit-str`, `emit-as`, `emit-symbol`, `default-library`, `default-library:reset`, `runtime-library`, `with:library`, `grammar`.
*   From `std.lang.base.impl-entry`: `emit-entry`, `with:cache-none`, `with:cache-force`.
*   From `std.lang.base.impl-deps`: `emit-entry-deps`.
*   From `std.lang.base.pointer`: `with:print`, `with:print-all`, `with:clip`, `with:input`, `with:raw`, `with:rt`, `with:rt-wrap`, `rt:macro-opts`.
*   From `std.lang.base.script`: `script`, `script-`, `script+`, `!`, `annex:get`, `annex:start`, `annex:stop`, `annex:restart-all`, `annex:start-all`, `annex:stop-all`, `annex:list`.
*   From `std.lang.base.script-def`: `tmpl-entry`, `tmpl-macro`.
*   From `std.lang.base.library`: `get-book-raw`, `get-book`, `get-module`, `get-snapshot`, `delete-module!`, `delete-modules!`, `delete-entry!`, `purge-book!`.
*   From `std.lang.base.script-lint`: `lint-set`, `lint-clear`.
*   From `std.lang.base.util`: `rt`, `rt:list`, `rt:default`.
*   From `std.lang.base.script-control`: `rt:restart`, `rt:stop`.
*   From `std.lang.base.workspace`: `sym-entry`, `module-entries`, `emit-ptr`, `emit-module`, `print-module`, `ptr-clip`, `ptr-print`, `ptr-setup`, `ptr-teardown`, `ptr-setup-deps`, `ptr-teardown-deps`, `rt:module`, `rt:module-purge`, `rt:inner`, `rt:restart`, `rt:setup`, `rt:setup-to`, `rt:setup-single`, `rt:scaffold`, `rt:scaffold-to`, `rt:scaffold-imports`, `rt:teardown`, `rt:teardown-at`, `rt:teardown-single`, `rt:teardown-to`, `intern-macros`.
*   From `std.lang.base.manage`: `lib:overview`, `lib:module`, `lib:entries`, `lib:purge`, `lib:unused`.

**Key Functions:**

*   **`rt:space`**: Retrieves the runtime for a given language and namespace.
*   **`get-entry`**: Retrieves a book entry from a pointer or map.
*   **`as-lua`**: Transforms Clojure vectors to Lua tables (empty vectors to empty maps).
*   **`rt:invoke`**: Invokes code in a specified runtime.
*   **`force-reload`**: Forces reloading of a namespace and its dependents.

### `std.lang.base.book` (Language Book Management)

This sub-namespace defines the core data structures for storing and managing language definitions, including books, modules, and entries. It provides functions for accessing and manipulating these structures.

**Core Concepts:**

*   **`Book` Record:** Represents a language definition, containing its `lang`, `meta` (metadata), `grammar`, `modules`, `parent` (for inheritance), and `referenced` modules.
*   **`BookModule` Record:** Represents a single source file or a collection of related code within a language, containing `alias`, `link`, `native` imports, `code` entries, `fragment` (macros), and `static` metadata.
*   **`BookEntry` Record:** Represents a single piece of code (function, macro, variable), containing its `id`, `module`, `section` (code, fragment, header), `form`, `form-input`, `deps`, `namespace`, and `static` metadata.

**Key Functions:**

*   **`get-base-entry`, `get-code-entry`, `get-entry`, `get-module`**: Functions for retrieving entries and modules from a book.
*   **`get-code-deps`, `get-deps`**: Functions for retrieving dependencies of code entries and modules.
*   **`list-entries`**: Lists entries within a book.
*   **`book-string`**: Returns a string representation of a book.
*   **`book?`, `book`**: Predicate and constructor for `Book` records.
*   **`book-merge`**: Merges a book with its parent book (for language inheritance).
*   **`book-from`**: Returns a merged book from a snapshot.
*   **`check-compatible-lang`, `assert-compatible-lang`**: Checks and asserts language compatibility.
*   **`set-module`, `put-module`, `delete-module`, `delete-modules`, `has-module?`, `assert-module`**: Functions for managing modules within a book.
*   **`set-entry`, `put-entry`, `delete-entry`, `has-entry?`, `assert-entry`**: Functions for managing entries within a module.
*   **`module-create-bundled`**: Creates bundled packages for modules.
*   **`module-create-filename`**: Generates a filename for a module.
*   **`module-create-check`**: Checks for bundle availability.
*   **`module-create-requires`**: Creates a map for module requirements.
*   **`module-create`**: Creates a `BookModule` record.
*   **`module-deps`**: Gets dependencies for a module.

### `std.lang.base.compile` (Code Compilation and Output)

This sub-namespace provides functions for compiling and writing generated code to files, supporting various output formats and module structures.

**Key Functions:**

*   **`compile-script`**: Compiles a single script entry.
*   **`compile-module-single`**: Compiles a single module.
*   **`compile-module-graph-rel`**: Extracts the relative path for a module in a graph.
*   **`compile-module-graph-single`**: Compiles a single module file within a graph.
*   **`compile-module-graph`**: Compiles a graph of modules.
*   **`compile-module-directory-single`**: Compiles a single module file within a directory structure.
*   **`compile-module-directory`**: Compiles modules from a directory structure.
*   **`compile-module-schema`**: Compiles all modules into a single schema file (e.g., for SQL).

### `std.lang.base.emit` (Code Emission Pipeline)

This sub-namespace defines the core code emission pipeline, responsible for transforming Clojure forms into target language strings based on a grammar.

**Core Concepts:**

*   **`default-grammar`**: Provides a base grammar with common settings.
*   **`emit-main-loop`**: The recursive function that traverses forms and dispatches to appropriate emitters.
*   **`emit-main`**: The main entry point for emitting a single form.
*   **`emit`**: Emits a form to an output string, handling grammar and options.
*   **`with:emit` (macro)**: Binds the top-level emit function.
*   **`prep-options`**: Prepares options for the emit pipeline.
*   **`prep-form`**: Preprocesses a form through different stages (raw, input, staging).

### `std.lang.base.emit-assign` (Assignment Emission)

This sub-namespace handles the emission of assignment-related forms, including inline function assignments and variable declarations.

**Key Functions:**

*   **`emit-def-assign-inline`**: Emits an inline function assignment.
*   **`emit-def-assign`**: Emits a variable declaration or assignment.
*   **`test-assign-loop`, `test-assign-emit`**: Test functions for assignment emission.

### `std.lang.base.emit-block` (Block Emission)

This sub-namespace handles the emission of control flow blocks (do, if, for, while, try, switch) and their associated parameters and bodies.

**Key Functions:**

*   **`emit-statement`**: Emits a single statement.
*   **`emit-do`, `emit-do*`**: Emits `do` blocks.
*   **`block-options`**: Retrieves options for a block.
*   **`emit-block-body`**: Emits the body of a block.
*   **`parse-params`**: Parses parameters for a block.
*   **`emit-params-statement`, `emit-params`**: Emits parameters for a block.
*   **`emit-block-control`, `emit-block-controls`**: Emits control flow constructs within a block.
*   **`emit-block-setup`**: Prepares a block for emission.
*   **`emit-block-inner`**: Emits the inner content of a block.
*   **`emit-block-standard`**: Emits a generic block.
*   **`emit-block`**: The main function for emitting block expressions.
*   **`test-block-loop`, `test-block-emit`**: Test functions for block emission.

### `std.lang.base.emit-common` (Common Emission Utilities)

This sub-namespace provides fundamental utilities and dynamic variables used across the entire emission pipeline, such as indentation, tracing, and generic emission functions.

**Core Concepts:**

*   **Dynamic Variables:** `*explode*`, `*trace*`, `*indent*`, `*compressed*`, `*multiline*`, `*max-len*`, `*emit-internal*`, `*emit-fn*`.
*   **Emission Types:** `:discard`, `:free`, `:squash`, `:comment`, `:indent`, `:token`, `:alias`, `:unit`, `:internal`, `:internal-str`, `:pre`, `:post`, `:prefix`, `:postfix`, `:infix`, `:infix-`, `:infix*`, `:infix-if`, `:bi`, `:between`, `:assign`, `:invoke`, `:new`, `:static-invoke`, `:index`, `:return`, `:macro`, `:template`, `:with-global`, `:with-decorate`, `:with-uuid`, `:with-rand`.

**Key Functions:**

*   **`with:explode`, `with-trace`, `with-compressed`, `with-indent` (macros)**: Control dynamic emission settings.
*   **`newline-indent`**: Returns a newline with appropriate indentation.
*   **`emit-reserved-value`**: Emits a reserved value.
*   **`emit-free-raw`, `emit-free`**: Emits free-form text.
*   **`emit-comment`**: Emits a comment.
*   **`emit-indent`**: Emits an indented form.
*   **`emit-macro`**: Emits a macro.
*   **`emit-array`**: Emits an array of forms.
*   **`emit-wrappable?`**: Checks if a form is wrappable.
*   **`emit-squash`**: Emits a squashed representation.
*   **`emit-wrapping`**: Emits a potentially wrapped form.
*   **`wrapped-str`**: Wraps a string with start/end delimiters.
*   **`emit-unit`**: Emits a unit.
*   **`emit-internal`, `emit-internal-str`**: Emits internal forms or strings.
*   **`emit-pre`, `emit-post`, `emit-prefix`, `emit-postfix`**: Emits operators before/after/prefix/postfix to arguments.
*   **`emit-infix`, `emit-infix-default`, `emit-infix-pre`, `emit-infix-if-single`, `emit-infix-if`**: Emits infix expressions.
*   **`emit-between`**: Emits a raw symbol between two elements.
*   **`emit-bi`**: Emits a binary infix operator.
*   **`emit-assign`**: Emits an assignment.
*   **`emit-return-do`, `emit-return-base`, `emit-return`**: Emits return statements.
*   **`emit-with-global`**: Emits a global variable.
*   **`emit-symbol-classify`**: Classifies a symbol.
*   **`emit-symbol-standard`**: Emits a standard symbol.
*   **`emit-symbol`**: Emits a symbol.
*   **`emit-token`**: Emits a token.
*   **`emit-with-decorate`**: Emits a decorated form.
*   **`emit-with-uuid`, `emit-with-rand`**: Emits UUIDs or random numbers.
*   **`invoke-kw-parse`**: Parses keyword arguments for invocation.
*   **`emit-invoke-kw-pair`, `emit-invoke-args`, `emit-invoke-layout`, `emit-invoke-raw`, `emit-invoke-static`, `emit-invoke-typecast`, `emit-invoke`**: Emits function invocations.
*   **`emit-new`**: Emits a constructor call.
*   **`emit-class-static-invoke`**: Emits a static class invocation.
*   **`emit-index-entry`, `emit-index`**: Emits indexed expressions.
*   **`emit-op`**: Dispatches to the appropriate emitter based on the operation.
*   **`form-key`**: Returns the key associated with a form.
*   **`emit-common-loop`**: The core emission loop.
*   **`emit-common`**: Emits a string based on grammar.

### `std.lang.base.emit-data` (Data Structure Emission)

This sub-namespace handles the emission of various Clojure data structures (maps, vectors, sets, tuples) into their target language equivalents.

**Key Functions:**

*   **`default-map-key`**: Emits a default map key.
*   **`emit-map-key`**: Emits a map key.
*   **`emit-map-entry`**: Emits a map entry.
*   **`emit-singleline-array?`**: Checks if an array can be emitted on a single line.
*   **`emit-maybe-multibody`**: Emits a multi-line body if necessary.
*   **`emit-coll-layout`**: Lays out a collection.
*   **`emit-coll`**: Emits a collection.
*   **`emit-data-standard`**: Emits standard data.
*   **`emit-data`**: The main function for emitting data forms.
*   **`emit-quote`**: Emits a quoted form.
*   **`emit-table-group`**: Groups table arguments.
*   **`emit-table`**: Emits a table.
*   **`test-data-loop`, `test-data-emit`**: Test functions for data emission.

### `std.lang.base.emit-fn` (Function Emission)

This sub-namespace handles the emission of function definitions and related constructs, including argument lists, type hints, and function bodies.

**Key Functions:**

*   **`emit-input-default`**: Emits a default input argument string.
*   **`emit-hint-type`**: Emits a type hint.
*   **`emit-def-type`**: Emits a definition type.
*   **`emit-fn-type`**: Emits a function type.
*   **`emit-fn-block`**: Retrieves block options for a function.
*   **`emit-fn-preamble-args`**: Emits preamble arguments for a function.
*   **`emit-fn-preamble`**: Emits the preamble of a function.
*   **`emit-fn`**: The main function for emitting function templates.
*   **`test-fn-loop`, `test-fn-emit`**: Test functions for function emission.

### `std.lang.base.emit-helper` (Emission Helper Functions)

This sub-namespace provides various helper functions and constants used throughout the emission pipeline, such as default grammar settings, symbol replacement rules, and argument parsing.

**Key Functions:**

*   **`default-emit-fn`**: The default emit function.
*   **`pr-single`**: Prints a single-quoted string.
*   **`+sym-replace+`**: Symbol replacement rules.
*   **`+default+`**: Default grammar settings.
*   **`get-option`, `get-options`**: Retrieves grammar options.
*   **`form-key-base`**: Gets the base key for a form.
*   **`basic-typed-args`**: Parses basic typed arguments.
*   **`emit-typed-allowed-args`**: Emits allowed typed arguments.
*   **`emit-typed-args`**: Emits typed arguments.
*   **`emit-symbol-full`**: Emits a full symbol.
*   **`emit-type-record`**: Formats a type record.

### `std.lang.base.emit-preprocess` (Emission Preprocessing)

This sub-namespace handles the preprocessing of Clojure forms before they are emitted, including macro expansion, symbol resolution, and dependency collection.

**Core Concepts:**

*   **Dynamic Variables:** `*macro-form*`, `*macro-grammar*`, `*macro-opts*`, `*macro-splice*`, `*macro-skip-deps*`.
*   **Preprocessing Stages:** `to-input` (raw to input forms), `to-staging` (input to staged forms).

**Key Functions:**

*   **`macro-form`, `macro-opts`, `macro-grammar`**: Accessors for macro context.
*   **`with:macro-opts` (macro)**: Binds macro options.
*   **`to-input-form`**: Processes a raw form into an input form.
*   **`to-input`**: Converts a raw form to an input form.
*   **`get-fragment`**: Retrieves a fragment (macro) from modules.
*   **`process-namespaced-resolve`**: Resolves a symbol in the current namespace.
*   **`process-namespaced-symbol`**: Processes namespaced symbols.
*   **`process-inline-assignment`**: Prepares a form for inline assignment.
*   **`to-staging-form`**: Processes different staging forms.
*   **`to-staging`**: Converts an input form to a staged form, collecting dependencies.
*   **`to-resolve`**: Resolves code symbols without macroexpansion.

### `std.lang.base.emit-special` (Special Form Emission)

This sub-namespace handles the emission of special forms, such as `!:module` (for emitting module contents), `!:eval` (for evaluating Clojure code at emit time), and `!:lang` (for embedding code from another language).

**Key Functions:**

*   **`emit-with-module-all-ids`**: Emits all IDs from a module.
*   **`emit-with-module`**: Emits a module.
*   **`emit-with-preprocess`**: Emits a preprocessed form.
*   **`emit-with-eval`**: Emits an evaluated form.
*   **`emit-with-deref`**: Emits a dereferenced var.
*   **`emit-with-lang`**: Emits an embedded language form.
*   **`test-special-loop`, `test-special-emit`**: Test functions for special form emission.

### `std.lang.base.emit-top-level` (Top-Level Form Emission)

This sub-namespace handles the emission of top-level forms like `defn`, `def`, `defglobal`, `defrun`, and `defclass`.

**Key Functions:**

*   **`transform-defclass-inner`**: Transforms the body of a `defclass`.
*   **`emit-def`**: Emits a `def` statement.
*   **`emit-declare`**: Emits a `declare` statement.
*   **`emit-top-level`**: The main function for emitting top-level forms.
*   **`emit-form`**: Emits a customisable form.

### `std.lang.base.grammar` (Language Grammar Definition)

This sub-namespace defines the structure and semantics of a target language's grammar, including its reserved words, operators, and emission rules.

**Core Concepts:**

*   **`Grammer` Record:** Represents a language grammar, containing its `tag`, `emit` function, `structure`, `reserved` words, `banned` forms, `highlight` keywords, and `macros`.
*   **Operators (`+op-all+`):** A comprehensive collection of predefined operators (math, compare, logic, control flow, data structures, macros, cross-language `xtalk` operations) that can be included in a grammar.

**Key Functions:**

*   **`gen-ops`**: Generates operators from a namespace.
*   **`collect-ops`**: Collects all operators.
*   **`ops-list`, `ops-symbols`, `ops-summary`, `ops-detail`**: Functions for inspecting operators.
*   **`build`**: Selects operators for a grammar.
*   **`build-min`**: Builds a minimal grammar.
*   **`build-xtalk`**: Builds a grammar with cross-language operators.
*   **`build:override`, `build:extend`**: Modifies an existing grammar.
*   **`to-reserved`**: Converts an operator map to a reserved word map.
*   **`grammar-structure`, `grammar-sections`, `grammar-macros`**: Extracts structural information from a grammar.
*   **`grammar?`, `grammar`**: Predicate and constructor for `Grammer` records.

### `std.lang.base.grammar-macro` (Macro Transformations)

This sub-namespace provides macro transformations for common Clojure forms (e.g., `->`, `->>`, `when`, `if`, `cond`, `let`, `case`, `doto`, `fn:>`) into more basic forms suitable for emission.

**Key Functions:**

*   **`tf-macroexpand`**: Macroexpands a form.
*   **`tf-when`**: Transforms `when` to a branch.
*   **`tf-if`**: Transforms `if` to a branch.
*   **`tf-cond`**: Transforms `cond` to a branch.
*   **`tf-let-bind`**: Transforms `let` bindings.
*   **`tf-case`**: Transforms `case` to a switch.
*   **`tf-lambda-arrow`**: Transforms lambda arrows.
*   **`tf-tcond`**: Transforms ternary conditions.
*   **`tf-xor`**: Transforms `xor` to a ternary if.
*   **`tf-doto`**: Transforms `doto` to a sequence of `do` operations.
*   **`tf-do-arrow`**: Transforms `do:>` to a function.
*   **`tf-forange`**: Transforms `forange` to a `for` loop.
*   **`+op-macro+`, `+op-macro-arrow+`, `+op-macro-let+`, `+op-macro-xor+`, `+op-macro-case+`, `+op-macro-forange+`**: Collections of macro operators.

### `std.lang.base.grammar-spec` (Grammar Specification)

This sub-namespace defines the core set of operators and their properties that form the basis of most language grammars. It includes operators for built-in functions, math, comparisons, logic, control flow, and top-level definitions.

**Key Functions:**

*   **`get-comment`**: Retrieves the comment prefix for a language.
*   **`format-fargs`**: Formats function arguments.
*   **`format-defn`**: Formats `defn` forms.
*   **`tf-for-index`**: Transforms `for:index` loops.
*   **`+op-builtin+`, `+op-builtin-global+`, `+op-builtin-module+`, `+op-builtin-helper+`**: Built-in operators.
*   **`+op-free-control+`, `+op-free-literal+`**: Free control and literal operators.
*   **`+op-math+`, `+op-compare+`, `+op-logic+`, `+op-counter+`**: Math, comparison, logic, and counter operators.
*   **`+op-return+`, `+op-throw+`, `+op-await+`**: Return, throw, and await operators.
*   **`+op-data-table+`, `+op-data-shortcuts+`, `+op-data-range+`**: Data table, shortcuts, and range operators.
*   **`+op-vars+`, `+op-bit+`, `+op-pointer+`**: Variable, bit, and pointer operators.
*   **`+op-fn+`, `+op-block+`**: Function and block operators.
*   **`+op-control-base+`, `+op-control-general+`, `+op-control-try-catch+`**: Control flow operators.
*   **`+op-top-base+`, `+op-top-global+`**: Top-level operators.
*   **`+op-class+`**: Class-related operators.
*   **`+op-for+`, `+op-coroutine+`**: For loop and coroutine operators.

### `std.lang.base.grammar-xtalk` (Cross-Language Operators)

This sub-namespace defines a set of "cross-talk" (xtalk) operators that provide a common interface for language-agnostic operations, such as object manipulation, type checking, and I/O. These operators are designed to be implemented differently in each target language.

**Key Functions:**

*   **`tf-throw`**: Transforms `throw`.
*   **`tf-eq-nil?`, `tf-not-nil?`**: Transforms nil checks.
*   **`tf-proto-create`**: Transforms prototype creation.
*   **`tf-has-key?`**: Transforms key existence checks.
*   **`tf-get-path`, `tf-get-key`**: Transforms property access.
*   **`tf-set-key`, `tf-del-key`**: Transforms property setting/deletion.
*   **`tf-copy-key`**: Transforms key copying.
*   **`tf-grammar-offset`, `tf-grammar-end-inclusive`**: Retrieves grammar-specific offset/inclusive settings.
*   **`tf-offset-base`, `tf-offset`, `tf-offset-rev`, `tf-offset-len`, `tf-offset-rlen`**: Transforms offset calculations.
*   **`tf-global-set`, `tf-global-has?`, `tf-global-del`**: Transforms global variable operations.
*   **`tf-lu-eq`**: Transforms lookup equality.
*   **`tf-bit-and`, `tf-bit-or`, `tf-bit-lshift`, `tf-bit-rshift`, `tf-bit-xor`**: Transforms bitwise operations.
*   **`+op-xtalk-core+`, `+op-xtalk-proto+`, `+op-xtalk-global+`, `+op-xtalk-custom+`, `+op-xtalk-math+`, `+op-xtalk-type+`, `+op-xtalk-bit+`, `+op-xtalk-lu+`, `+op-xtalk-obj+`, `+op-xtalk-arr+`, `+op-xtalk-str+`, `+op-xtalk-js+`, `+op-xtalk-return+`, `+op-xtalk-socket+`, `+op-xtalk-iter+`, `+op-xtalk-cache+`, `+op-xtalk-thread+`, `+op-xtalk-file+`, `+op-xtalk-b64+`, `+op-xtalk-uri+`, `+op-xtalk-special+`**: Collections of cross-language operators.

### `std.lang.base.impl` (Core Implementation Utilities)

This sub-namespace provides core implementation details and helper functions for the language system, including managing the global library, emit options, and direct code emission.

**Key Functions:**

*   **`with:library` (macro)**: Binds a library as the default.
*   **`default-library`, `default-library:reset`**: Manages the default library.
*   **`runtime-library`**: Retrieves the current runtime library.
*   **`grammar`**: Retrieves the grammar for a language.
*   **`emit-options-raw`, `emit-options`**: Prepares emit options.
*   **`to-form`**: Converts input to a form.
*   **`%.form` (macro)**: Converts to a form.
*   **`emit-bulk?`**: Checks if a form is a bulk form.
*   **`emit-direct`**: Emits a form directly.
*   **`emit-str`**: Converts a form to an output string.
*   **`%.str` (macro)**: Converts to an output string.
*   **`emit-as`**: Emits multiple forms.
*   **`emit-symbol`**: Emits a symbol.
*   **`get-entry`**: Retrieves an entry.
*   **`emit-entry`**: Emits an entry.
*   **`emit-entry-deps-collect`**: Collects entry dependencies.
*   **`emit-entry-deps`**: Emits entry dependencies.
*   **`emit-script-imports`**: Emits script imports.
*   **`emit-script-deps`**: Emits script dependencies.
*   **`emit-script-join`**: Joins script parts.
*   **`emit-script`**: Emits a script with all dependencies.
*   **`emit-scaffold-raw-imports`**: Emits scaffold raw imports.
*   **`emit-scaffold-raw`**: Creates scaffold raw entries.
*   **`emit-scaffold-for`**: Creates scaffold for a module.
*   **`emit-scaffold-to`**: Creates scaffold up to a module.
*   **`emit-scaffold-imports`**: Creates scaffold to expose native imports.

### `std.lang.base.impl-deps` (Dependency Management Implementation)

This sub-namespace provides the implementation details for managing dependencies between modules and entries, including collecting native imports and resolving module links.

**Key Functions:**

*   **`module-import-form`, `module-export-form`, `module-link-form`**: Generates import/export/link forms.
*   **`has-module-form`, `setup-module-form`, `teardown-module-form`**: Generates module lifecycle forms.
*   **`has-ptr-form`, `setup-ptr-form`, `teardown-ptr-form`**: Generates pointer lifecycle forms.
*   **`collect-script-natives`**: Collects native imported modules.
*   **`collect-script-entries`**: Collects all entries for a script.
*   **`collect-script`**: Collects dependencies for a script.
*   **`collect-script-summary`**: Summarizes script dependencies.
*   **`collect-module-check-options`**: Checks module options.
*   **`collect-module-ns-select`**: Selects module namespaces.
*   **`collect-module-directory-form`**: Collects module directory forms.
*   **`collect-module`**: Collects information for an entire module.

### `std.lang.base.impl-entry` (Entry Implementation)

This sub-namespace provides the implementation details for creating and emitting book entries, including handling metadata, preprocessing, and caching.

**Key Functions:**

*   **`create-common`**: Creates common entry keys from metadata.
*   **`create-code-raw`**: Creates a raw code entry.
*   **`create-code-base`**: Creates a base code entry.
*   **`create-code-hydrate`**: Hydrates code entries.
*   **`create-code`**: Creates a code entry.
*   **`create-fragment`**: Creates a fragment entry.
*   **`create-macro`**: Creates a macro entry.
*   **`with:cache-none`, `with:cache-force` (macros)**: Control entry caching.
*   **`emit-entry-raw`**: Emits a raw entry.
*   **`+cached-emit-keys+`, `+cached-keys+`**: Keys for cached emits.
*   **`emit-entry-cached`**: Emits a cached entry.
*   **`emit-entry-label`**: Emits an entry label.
*   **`emit-entry`**: Emits a given entry.

### `std.lang.base.impl-lifecycle` (Lifecycle Implementation)

This sub-namespace provides the implementation details for managing the lifecycle of modules, including emitting setup and teardown scripts.

**Key Functions:**

*   **`emit-module-prep`**: Prepares a module for emission.
*   **`emit-module-setup-concat`, `emit-module-setup-join`, `emit-module-setup-native-arr`, `emit-module-setup-link-arr`, `emit-module-setup-raw`, `emit-module-setup`**: Functions for emitting module setup code.
*   **`emit-module-teardown-concat`, `emit-module-teardown-join`, `emit-module-teardown-raw`, `emit-module-teardown`**: Functions for emitting module teardown code.

### `std.lang.base.library` (Library Management)

This sub-namespace defines the core `Library` record and provides functions for managing a collection of language books and their modules/entries. It handles snapshot management and bulk operations.

**Key Functions:**

*   **`wait-snapshot`**: Waits for the current snapshot to be ready.
*   **`wait-apply`**: Applies a function to the library state when the task queue is empty.
*   **`wait-mutate!`**: Mutates the library state once the task queue is empty.
*   **`get-snapshot`**: Retrieves the current snapshot.
*   **`get-book`, `get-book-raw`**: Retrieves a book from the library.
*   **`get-module`, `get-entry`**: Retrieves a module or entry from the library.
*   **`add-book!`, `delete-book!`**: Adds or deletes a book.
*   **`reset-all!`**: Resets the library.
*   **`list-modules`, `list-entries`**: Lists modules or entries.
*   **`add-module!`, `delete-module!`, `delete-modules!`**: Adds or deletes modules.
*   **`library-string`**: Returns a string representation of the library.
*   **`library?`, `library:create`, `library`**: Predicate and constructors for `Library` records.
*   **`add-entry!`, `add-entry-single!`, `delete-entry!`**: Adds or deletes entries.
*   **`install-module!`, `install-book!`, `purge-book!`**: Installs modules or books.

### `std.lang.base.library-snapshot` (Library Snapshot Management)

This sub-namespace defines the `Snapshot` record and provides functions for managing immutable snapshots of the library's state, enabling efficient versioning and merging of language definitions.

**Key Functions:**

*   **`get-deps`**: Retrieves dependencies from a snapshot.
*   **`snapshot-string`**: Returns a string representation of a snapshot.
*   **`snapshot?`, `snapshot`**: Predicate and constructor for `Snapshot` records.
*   **`snapshot-reset`**: Resets a snapshot.
*   **`snapshot-merge`**: Merges two snapshots.
*   **`get-book-raw`, `get-book`**: Retrieves a book from a snapshot.
*   **`add-book`**: Adds a book to a snapshot.
*   **`set-module`, `delete-module`, `delete-modules`**: Manages modules in a snapshot.
*   **`list-modules`, `list-entries`**: Lists modules or entries in a snapshot.
*   **`set-entry`, `set-entries`, `delete-entry`, `delete-entries`**: Manages entries in a snapshot.
*   **`install-check-merged`**: Checks for merged books.
*   **`install-module-update`, `install-module`**: Updates or installs a module.
*   **`install-book-update`, `install-book`**: Updates or installs a book.

### `std.lang.base.manage` (Language Management Tasks)

This sub-namespace provides high-level tasks for managing language definitions, modules, and entries, including overviews, purging, and linting.

**Key Functions:**

*   **`lib-overview-format`, `lib-overview`**: Formats and displays a library overview.
*   **`lib-module-env`**: Compiles the module task environment.
*   **`lib-module-filter`**: Filters modules.
*   **`lib-module-overview-format`, `lib-module-overview`**: Formats and displays a module overview.
*   **`lib-module-entries-format-section`, `lib-module-entries-format`, `lib-module-entries`**: Formats and displays module entries.
*   **`lib-module-purge-fn`, `lib-module-purge`**: Purges modules.
*   **`lib-module-unused-fn`, `lib-module-unused`**: Lists unused modules.
*   **`lib-module-missing-line-number-fn`, `lib-module-missing-line-number`**: Lists modules with missing line numbers.
*   **`lib-module-incorrect-alias-fn`, `lib-module-incorrect-alias`**: Lists modules with incorrect aliases.

### `std.lang.base.pointer` (Language Pointers)

This sub-namespace defines the `Pointer` record, which acts as a reference to a code entry within a specific language runtime. Pointers enable dynamic invocation and introspection of code across different languages.

**Key Functions:**

*   **`with:clip`, `with:print`, `with:print-all`, `with:rt-wrap`, `with:rt`, `with:input`, `with:raw` (macros)**: Control pointer behavior.
*   **`get-entry`**: Retrieves the library entry for a pointer.
*   **`ptr-tag`**: Creates a tag for a pointer.
*   **`ptr-deref`**: Dereferences a pointer.
*   **`ptr-display`**: Displays a pointer.
*   **`ptr-invoke-meta`**: Prepares metadata for pointer invocation.
*   **`rt-macro-opts`**: Creates macro options for a runtime.
*   **`ptr-invoke-string`**: Emits the invocation string for a pointer.
*   **`ptr-invoke-script`**: Emits a script for a pointer.
*   **`ptr-intern`**: Interns a pointer into the workspace.
*   **`ptr-output-json`, `ptr-output`**: Handles pointer output.
*   **`ptr-invoke`**: Invokes a pointer.

### `std.lang.base.registry` (Language Registry)

This sub-namespace defines a global registry (`+registry+`) that maps language contexts and runtime keys to their corresponding Clojure namespaces, enabling dynamic loading and instantiation of language runtimes.

**Core Concepts:**

*   **`+registry+`**: An atom storing a map of `[lang runtime-key]` to Clojure namespace symbols.

### `std.lang.base.runtime` (Language Runtimes)

This sub-namespace defines the `RuntimeDefault` record, which serves as a base implementation for language runtimes. It provides default behaviors for code evaluation, pointer handling, and lifecycle management.

**Core Concepts:**

*   **`RuntimeDefault` Record:** A base runtime implementation that can be extended or proxied.
*   **`IContext` Protocol:** Implemented by runtimes for raw code evaluation, pointer initialization, tag retrieval, dereferencing, display, and invocation.
*   **`IComponent` Protocol:** Implemented by runtimes for lifecycle management (start, stop, kill).

**Key Functions:**

*   **`default-tags-ptr`, `default-deref-ptr`, `default-invoke-ptr`, `default-init-ptr`, `default-display-ptr`, `default-raw-eval`, `default-transform-in-ptr`, `default-transform-out-ptr`**: Default implementations for `IContext` methods.
*   **`rt-default-string`**: Returns a string representation of a default runtime.
*   **`rt-default?`, `rt-default`**: Predicate and constructor for `RuntimeDefault` records.
*   **`install-lang!`, `install-type!`**: Installs language definitions and runtime types.
*   **`return-format-simple`, `return-format`, `return-wrap-invoke`, `return-transform`**: Functions for formatting return values.
*   **`default-invoke-script`**: Default script invocation.
*   **`default-lifecycle-prep`**: Prepares options for lifecycle management.
*   **`default-scaffold-array`, `default-scaffold-setup-for`, `default-scaffold-setup-to`, `default-scaffold-imports`**: Functions for scaffolding.
*   **`default-lifecycle-fn`**: Constructs a lifecycle function.
*   **`default-has-module?`, `default-has-ptr?`, `default-setup-ptr`, `default-teardown-ptr`**: Default lifecycle functions for modules and pointers.
*   **`default-setup-module-emit`, `default-setup-module-basic`, `default-teardown-module-basic`, `default-setup-module`, `default-teardown-module`**: Default module setup/teardown functions.
*   **`multistage-invoke`, `multistage-setup-for`, `multistage-setup-to`, `multistage-teardown-for`, `multistage-teardown-at`, `multistage-teardown-to`**: Multistage lifecycle functions.

### `std.lang.base.runtime-proxy` (Runtime Proxy)

This sub-namespace defines a proxy mechanism for language runtimes, allowing one runtime to delegate its operations to another. This is useful for creating aliases or for adding layers of functionality.

**Key Functions:**

*   **`rt-proxy-string`**: Returns a string representation of a runtime proxy.
*   **`proxy-get-rt`**: Retrieves the redirected runtime.
*   **`proxy-raw-eval`, `proxy-init-ptr`, `proxy-tags-ptr`, `proxy-deref-ptr`, `proxy-display-ptr`, `proxy-invoke-ptr`, `proxy-transform-in-ptr`, `proxy-transform-out-ptr`**: Proxy implementations for `IContext` methods.
*   **`proxy-started?`, `proxy-stopped?`, `proxy-remote?`, `proxy-info`, `proxy-health`**: Proxy implementations for `IComponent` methods.

### `std.lang.base.script` (Scripting and Runtime Control)

This sub-namespace provides high-level macros and functions for defining and controlling language scripts and their associated runtimes. It includes mechanisms for installing languages, managing annexes, and executing code in different contexts.

**Core Concepts:**

*   **`script` Macro:** The primary macro for defining a language script, installing its module, and setting up its runtime.
*   **Annexes:** A mechanism for extending a language with additional runtimes or configurations.
*   **`!` Macro:** A convenient macro for switching between active annex runtimes and executing code within them.

**Key Functions:**

*   **`install`**: Installs a language book and its runtime.
*   **`script-ns-import`**: Imports namespaces for a script.
*   **`script-macro-import`**: Imports macros for a script.
*   **`script-fn-base`, `script-fn`**: Base functions for script setup.
*   **`script` (macro)**: Defines a language script.
*   **`script-test-prep`, `script-test`**: Prepares and runs test scripts.
*   **`script-` (macro)**: Defines a test script.
*   **`script-ext`, `script+` (macro)**: Extends a script with additional runtimes (annexes).
*   **`script-ext-run`**: Executes code in an annex runtime.
*   **`!` (macro)**: Executes code in a specified annex runtime.
*   **`annex:start`, `annex:get`, `annex:stop`, `annex:start-all`, `annex:stop-all`, `annex:restart-all`, `annex:list`**: Functions for managing annexes.

### `std.lang.base.script-annex` (Script Annex Management)

This sub-namespace provides the implementation for managing "annexes," which are extensions to language scripts that allow for dynamic runtime switching and configuration.

**Key Functions:**

*   **`rt-annex-string`**: Returns a string representation of a runtime annex.
*   **`rt-annex?`, `rt-annex:create`**: Predicate and constructor for `RuntimeAnnex` records.
*   **`annex-current`, `annex-reset`**: Manages the current annex.
*   **`get-annex`**: Retrieves the current annex.
*   **`clear-annex`**: Clears all runtimes in an annex.
*   **`get-annex-library`, `get-annex-book`**: Retrieves the library or book from an annex.
*   **`add-annex-runtime`, `get-annex-runtime`, `remove-annex-runtime`**: Manages runtimes in an annex.
*   **`register-annex-tag`, `deregister-annex-tag`**: Registers or deregisters annex tags.
*   **`start-runtime`**: Starts a runtime in an annex.
*   **`same-runtime?`**: Checks if two runtimes are the same.

### `std.lang.base.script-control` (Script Runtime Control)

This sub-namespace provides functions for controlling language runtimes, including getting, stopping, and restarting them, as well as executing one-shot evaluations.

**Key Functions:**

*   **`script-rt-get`**: Retrieves a language runtime.
*   **`script-rt-stop`, `script-rt-restart`**: Stops or restarts a runtime.
*   **`script-rt-oneshot-eval`, `script-rt-oneshot`**: Executes one-shot evaluations.

### `std.lang.base.script-def` (Script Definition Helpers)

This sub-namespace provides helper functions for defining script-related templates and macros.

**Key Functions:**

*   **`tmpl-entry`**: Creates templates for various argument types.
*   **`tmpl-macro`**: Creates templates for macros.

### `std.lang.base.script-lint` (Script Linting)

This sub-namespace provides a basic linter for language scripts, checking for unused variables, unknown symbols, and other potential issues.

**Key Functions:**

*   **`get-reserved-raw`, `get-reserved`**: Retrieves reserved symbols from a grammar.
*   **`collect-vars`**: Collects all variables in a form.
*   **`collect-module-globals`**: Collects global symbols from a module.
*   **`collect-sym-vars`**: Collects symbols and variables.
*   **`sym-check-linter`**: Checks the linter.
*   **`lint-set`, `lint-clear`, `lint-needed?`**: Manages linting settings.
*   **`lint-entry`**: Lints a single entry.

### `std.lang.base.script-macro` (Script Macro Interning)

This sub-namespace provides functions for interning language-specific macros and top-level forms into the Clojure environment, making them available for use in scripts.

**Key Functions:**

*   **`body-arglists`**: Extracts arglists from a function body.
*   **`intern-in`**: Interns a macro.
*   **`intern-prep`**: Prepares module and form metadata for interning.
*   **`intern-def$-fn`, `intern-def$`**: Interns `def$` fragments.
*   **`intern-defmacro-fn`, `intern-defmacro`**: Interns `defmacro` forms.
*   **`call-thunk`**: Calls a thunk with pointer output control.
*   **`intern-!-fn`, `intern-!`**: Interns free pointer macros.
*   **`intern-free-fn`, `intern-free`**: Interns free pointers.
*   **`intern-top-level-fn`, `intern-top-level`**: Interns top-level functions.
*   **`intern-macros`**: Interns all macros from a grammar.
*   **`intern-highlights`**: Interns highlight macros.
*   **`intern-grammar`**: Interns a grammar's macros into the namespace.
*   **`intern-defmacro-rt-fn`, `defmacro.!` (macro)**: Defines runtime language macros.

### `std.lang.base.util` (Language Utilities)

This sub-namespace provides various utility functions for working with language-related symbols, contexts, and pointers.

**Key Functions:**

*   **`sym-id`, `sym-module`, `sym-pair`, `sym-full`, `sym-default-str`, `sym-default-inverse-str`**: Functions for manipulating symbols.
*   **`hashvec?`, `doublevec?`**: Predicates for vector types.
*   **`lang-context`**: Creates a language context keyword.
*   **`lang-rt-list`, `lang-rt`**: Lists and retrieves language runtimes.
*   **`lang-rt-default`**: Retrieves the default runtime function.
*   **`lang-pointer`**: Creates a language pointer.

### `std.lang.base.workspace` (Workspace Management)

This sub-namespace provides functions for managing the language workspace, including emitting modules, printing module contents, and controlling runtime lifecycles.

**Key Functions:**

*   **`rt-resolve`**: Resolves a runtime.
*   **`sym-entry`, `sym-pointer`**: Retrieves entries or pointers from symbols.
*   **`module-entries`**: Retrieves module entries.
*   **`emit-ptr`**: Emits a pointer as a string.
*   **`ptr-clip`, `ptr-print`**: Copies or prints pointer text.
*   **`ptr-setup`, `ptr-teardown`**: Sets up or tears down a pointer.
*   **`ptr-setup-deps`, `ptr-teardown-deps`**: Sets up or tears down pointer dependencies.
*   **`emit-module`**: Emits an entire module.
*   **`print-module`**: Prints a module.
*   **`rt:module`, `rt:module-purge`**: Manages module purging.
*   **`rt:inner`**: Retrieves the inner client for a shared runtime.
*   **`rt:restart`**: Restarts a shared runtime.
*   **`multistage-tmpl`**: Template for multistage functions.
*   **`intern-macros`**: Interns macros from one namespace to another.

### `std.lang.dev` (Development Utilities)

This sub-namespace provides utilities specifically for development, such as reloading language specifications.

**Key Functions:**

*   **`reload-specs`**: Reloads language specifications.

### `std.lang.interface.type-notify` (Notification Server)

This sub-namespace implements a notification server that allows language runtimes to send messages back to the Clojure host, enabling real-time feedback and event handling.

**Key Functions:**

*   **`has-sink?`, `get-sink`, `clear-sink`**: Manages notification sinks.
*   **`add-listener`, `remove-listener`**: Manages listeners for sinks.
*   **`get-oneshot-id`, `remove-oneshot-id`, `clear-oneshot-sinks`**: Manages one-shot notification IDs.
*   **`process-print`, `process-capture`, `process-message`**: Processes incoming messages.
*   **`handle-notify-http`, `start-notify-http`, `stop-notify-http`**: Manages HTTP notification server.
*   **`handle-notify-socket`, `start-notify-socket`, `stop-notify-socket`**: Manages Socket notification server.
*   **`start-notify`, `stop-notify`**: Starts or stops both notification servers.
*   **`notify-server-string`**: Returns a string representation of the notification server.
*   **`NotifyServer` (defimpl record)**: The concrete record type for the notification server.
*   **`notify-server:create`, `notify-server`**: Constructors for the notification server.
*   **`default-notify`, `default-notify:reset`**: Manages the default notification server.
*   **`watch-oneshot`**: Watches for a one-shot notification.

### `std.lang.interface.type-shared` (Shared Runtime Management)

This sub-namespace provides mechanisms for managing shared language runtimes, allowing multiple clients to share a single runtime instance.

**Key Functions:**

*   **`get-groups`**: Retrieves all shared groups.
*   **`get-group-count`, `update-group-count`**: Manages group counts.
*   **`get-group-instance`, `set-group-instance`, `update-group-instance`**: Manages group instances.
*   **`restart-group-instance`**: Restarts a group instance.
*   **`remove-group-instance`**: Removes a group instance.
*   **`start-shared`, `stop-shared`, `kill-shared`**: Lifecycle functions for shared runtimes.
*   **`wrap-shared`**: Wraps a function to operate on a shared runtime.
*   **`rt-shared-string`**: Returns a string representation of a shared runtime.
*   **`SharedRuntime` (defimpl record)**: The concrete record type for a shared runtime.
*   **`rt-shared:create`, `rt-shared`**: Constructors for shared runtimes.
*   **`rt-is-shared?`**: Checks if a runtime is shared.
*   **`rt-get-inner`**: Retrieves the inner runtime.

### `std.lang.model.*` (Language Specifications)

These sub-namespaces define the specific grammars and templates for various target languages, including Bash, C, GLSL, JQ, JavaScript, Lua, Python, R, Rust, and Scheme. They extend the base grammar with language-specific syntax, operators, and emission rules.

**Core Concepts:**

*   **Language-Specific Grammars:** Each `spec_<lang>.clj` file defines a `+grammar+` for its respective language, built upon `std.lang.base.grammar`.
*   **Language-Specific Templates:** Each `spec_<lang>.clj` file defines a `+template+` for its respective language, customizing emission rules for data structures, blocks, and tokens.
*   **Cross-Talk (XTalk) Operators:** The `spec_xtalk.clj` and `spec_xtalk/fn_<lang>.clj` files define cross-language operators that provide a common interface for operations that are implemented differently in each target language.

**Key Functions (examples from `spec_js.clj`, `spec_lua.clj`, `spec_python.clj`, `spec_r.clj`, `spec_rust.clj`, `spec_bash.clj`, `spec_glsl.clj`, `spec_scheme.clj`, `spec_jq.clj`):**

*   **`+features+`**: Defines language-specific operators and their emission rules.
*   **`+template+`**: Customizes the emission template for the language.
*   **`+grammar+`**: The final grammar for the language.
*   **`+meta+`**: Language-specific metadata (e.g., module import/export functions).
*   **`+book+`**: The language book, containing its grammar and meta.
*   **`+init+`**: Installs the language into the system.
*   **Language-specific transformation functions (e.g., `js-regex`, `lua-map-key`, `python-defn`, `r-token-boolean`, `rst-typesystem`, `bash-quote-item`)**: Implement custom emission logic for various forms and data types.

### `std.lang.strict.base_state` (Strict Mode State)

This sub-namespace likely provides state management for a "strict mode" within the language system, though its contents are minimal in the provided files.

### Usage Pattern:

The `std.lang` module is the cornerstone of the `foundation-base` project, enabling:
*   **Polyglot Development:** Write code once in Clojure and deploy it to multiple target languages.
*   **Unified Tooling:** Use a single set of tools for code generation, testing, and runtime management across different languages.
*   **Metaprogramming:** Programmatically generate and transform code for various platforms.
*   **Language Experimentation:** Easily define and experiment with new DSLs and language features.
*   **Runtime Agnosticism:** Abstract away the complexities of interacting with different execution environments.

By providing a powerful and extensible framework for language definition and code generation, `std.lang` empowers developers to build highly flexible and adaptable software systems.