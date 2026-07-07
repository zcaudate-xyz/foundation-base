(ns documentation.hara.lang-introduction
  (:require [hara.lang :as l])
  (:use code.test))

[[:hero {:title "Introduction"
         :subtitle "Understanding the book, the grammar, and the runtime model."
         :lead "`hara.lang` is best understood as a **language-oriented templating system** built for real multi-language applications. It stores code in a reusable intermediate form, emits that form through a target grammar, and can then hand the result to a matching runtime."
         :badges ["Book model" "Grammar model" "Shared DSL"]}]]

[[:chapter {:title "The core idea"}]]

"Instead of writing and maintaining every target language by hand, `hara.lang` lets you author code in a Lisp DSL and describe each target language as a grammar. That grammar controls **how forms are emitted**, **which macros expand**, **which operators exist**, and **what dependencies or native fragments are required**."

[[:card-grid {:items [{:meta "Book"
                       :title "A language is a structured library"
                       :text "A `Book` stores the target language identity, grammar, parent relationship, modules, and code entries. That means emitted code is traceable back to named modules and functions instead of being anonymous text."}
                      {:meta "Grammar"
                       :title "Syntax is data"
                       :text "Reserved operators, blocks, data literals, top-level forms, and special emit hooks are declared in the grammar, which makes a new target language mostly a matter of defining conventions."}
                      {:meta "Script"
                       :title "Namespaces become language modules"
                       :text "Using `l/script` installs a module into the current language book, imports relevant macros, and connects the namespace to a runtime configuration for further evaluation."}]}]]

[[:section {:title "Walkthrough"}]]

"The fastest way to see the book and grammar model is to emit the same small function to two targets. `l/emit-as` takes a language keyword and a quoted hara.lang form, and returns the emitted string."

(fact "emit a function to JavaScript"
  ^{:refer hara.lang/emit-as :added "4.0"}
  (l/emit-as :js '[(defn add [a b]
                     (return (+ a b)))
                   (add 1 2)])
  => "function add(a,b){\n  return a + b;\n}\n\nadd(1,2)")

(fact "emit the same function to Lua"
  ^{:refer hara.lang/emit-as :added "4.0"}
  (l/emit-as :lua '[(defn add [a b]
                      (return (+ a b)))
                    (add 1 2)])
  => "local function add(a,b)\n  return a + b\nend\n\nadd(1,2)")

"`l/script-` installs a script context into the current namespace. Once installed, `!.js` evaluates expressions in place, and `defn.js` stores functions as book entries that can be called like Clojure functions."

(fact "install a script context and call a generated function"
  ^{:refer hara.lang/script- :added "4.0"}
  (do
    (l/script- :js
      {:require [[xt.lang.spec-base :as xt]]})
    (defn.js add [a b]
      (return (+ a b)))
    (add 1 2))
  => "add(1,2)")

[[:chapter {:title "Why not just transpile?"}]]

"Straight transpilation is usually about **converting one source language into one target language**. `hara.lang` aims at a broader problem: **shared authoring and maintenance across many targets and runtimes**."

[[:callout {:tone :success
            :title "The leverage point"
            :content "Once code is stored as reusable entries inside a language book, you can do more than print it. You can **track dependencies**, **inherit grammars**, **re-stage templates per target**, **inspect modules**, and **run the result inside different runtime adapters**."}]]

[[:chapter {:title "Where it fits well"}]]

[[:card-grid {:items [{:meta "Polyglot systems"
                       :title "Shared logic across JS, Lua, Python, and more"
                       :text "A single conceptual model can be emitted to different environments when those environments share enough structure."}
                      {:meta "Infrastructure"
                       :title "Tight feedback loops"
                       :text "Because authoring, testing, and maintenance stay in Clojure, teams can iterate on generated code without jumping across unrelated toolchains for every edit."}
                      {:meta "Exploration"
                       :title "New targets are incremental"
                       :text "Adding a target language does not require rebuilding the world. A grammar, a runtime adapter, and a set of useful library modules are often enough to make it productive."}]}]]

[[:demo {:title "The mental model"
          :lang "text"
          :code "shared forms -> preprocess + stage -> grammar-driven emission -> target code -> runtime adapter"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lang_base_book_summary.md
;; sha256: 41c121101f24594e188676242ff16e9b8479f89681a47fe75ba82277ca798e39
[[:chapter {:title "hara.lang.base.book and hara.lang.base.library Summary" :link "merged-plans-slop-summary-std-lang-base-book-summary-md"}]]

"The `std.lang.base.book*` and `std.lang.base.library*` namespaces define the data structures for storing, managing, and accessing code within the `foundation-base` ecosystem. A \"library\" is a collection of \"books,\" each of which represents a language and contains the code and grammar for that language."

"**Core Concepts:**"

"*   **Library (`hara.lang.base.library`):** A `Library` is the top-level container for all language definitions. It holds a \"snapshot\" of the current state of all books.\n*   **Snapshot (`hara.lang.base.library-snapshot`):** A `Snapshot` is an immutable map of language IDs to book definitions. It represents a specific version of the library's content.\n*   **Book (`hara.lang.base.book`):** A `Book` holds all the code for a particular language. It contains a map of modules, a grammar, and metadata about the language.\n*   **Module (`std.lang.base.book-module`):** A `Module` represents a single source file or a collection of related code. It contains a map of code entries, as well as information about the module's dependencies, exports, and other properties.\n*   **Entry (`hara.lang.base.book-entry`):** An `Entry` represents a single piece of code, such as a function, a variable, or a macro. It contains the code itself (as a Clojure form), as well as metadata about the code."

"**How Forms are Stored as Code:**"

"The `BookEntry` record is the key to understanding how forms are stored as code. Here's a breakdown of its most important fields:"

"*   `:form`: This field holds the actual Clojure form that represents the code. This is the raw, unevaluated code that will be processed by the emit pipeline.\n*   `:form-input`: This field holds the \"input form,\" which is the result of the first stage of preprocessing (`to-input`).\n*   `:deps`: This field holds a set of symbols that represent the dependencies of the code. These dependencies are resolved during the `to-staging` phase of preprocessing.\n*   `:namespace`: This field holds the namespace of the code.\n*   `:template` and `:standalone`: These fields are used for macros and templates."

"**The Library and Book Data Structures:**"

"*   **`Library`:** A record with an `:instance` field that holds an atom containing the current `Snapshot`.\n*   **`Snapshot`:** An immutable map where keys are language keywords (e.g., `:lua`, `:js`) and values are maps containing a `:book` key.\n*   **`Book`:** A record with the following fields:\n    *   `:lang`: The language of the book.\n    *   `:meta`: A `BookMeta` record with language-specific metadata.\n    *   `:grammar`: The grammar for the language.\n    *   `:modules`: A map of `BookModule` records.\n*   **`BookModule`:** A record with fields for `:code`, `:fragment` (macros), `:alias`, `:link`, `:export`, etc.\n*   **`BookEntry`:** A record with fields for `:form`, `:deps`, `:namespace`, etc."

"**Interaction with the Emit Pipeline:**"

"The `library` and `book` are essential for the emit pipeline."

"*   The `emit` function takes a `library` (or a `snapshot`) as an argument, which it uses to get the `book` for the target language.\n*   The `book` provides the `grammar` that is used to emit the code.\n*   The `modules` in the `book` are used to resolve dependencies and to find the code for other functions and macros that are used in the form being emitted."

"**Example: Creating and Using a Library**"

^{:id merged-plans-slop-summary-std-lang-base-book-summary-md-example-1 :added "4.0"}
(fact "hara.lang.base.book and hara.lang.base.library Summary example"
  (require '[hara.lang.base.library :as lib])
  (require '[hara.lang.base.book :as book])
  (require '[hara.lang.base.book-module :as module])
  (require '[hara.lang.base.book-entry :as entry])

  ;; 1. Define a code entry
  (def my-entry
    (entry/book-entry
     {:lang :my-lang, :id 'my-fn, :module 'my-module, :section :code,
      :form '(defn my-fn [x] (+ x 1))}))

  ;; 2. Define a module
  (def my-module
    (module/book-module
     {:lang :my-lang, :id 'my-module, :code {'my-fn my-entry}}))

  ;; 3. Define a book
  (def my-book
    (book/book
     {:lang :my-lang, :grammar my-grammar, :modules {'my-module my-module}}))

  ;; 4. Create a library and add the book
  (def my-library (lib/library {}))
  (lib/add-book! my-library my-book)

  ;; 5. Emit code using the library
  (impl/emit-str '(my-module/my-fn 1) {:lang :my-lang :library my-library})
)

"This example shows how to create a library, add a book to it, and then use the library to emit code. The library provides the necessary context (grammar and modules) for the emit pipeline to do its work."

"By providing a structured way to store and manage code, the `hara.lang.base.book*` and `hara.lang.base.library*` namespaces play a crucial role in the `foundation-base` transpiler."
;; END merged documentation: plans/slop/summary/std_lang_base_book_summary.md

;; BEGIN merged documentation: plans/slop/summary/std_lang_base_emit_summary.md
;; sha256: 46c504a58f160f5d10e30709bdcbcbe8ab54e69471cd85563ebf781a29a419a1
[[:chapter {:title "hara.lang.base.emit Summary" :link "merged-plans-slop-summary-std-lang-base-emit-summary-md"}]]

"The `std.lang.base.emit*` namespaces form the core of the `foundation-base` code generation and transpilation engine. Their primary responsibility is to take a Clojure-like data structure, referred to as a \"form,\" and translate it into a string of code in a target language. This entire process is orchestrated by a `grammar` that specifies the syntax, semantics, and customization points for the target language."

"**Core Concepts:**"

"*   **Emit Pipeline:** The process of code generation is a multi-stage pipeline that transforms the input form into the final output string. The main entry point is `std.lang.base.emit/emit`, which takes a form, a grammar, a namespace, and a map of options.\n*   **Grammar:** The `grammar` is a rich data structure (a Clojure map) that defines how to emit different forms. It contains detailed information about reserved words, operators, data structures, control flow, function definitions, and more. The grammar is the primary mechanism for customizing the output for different target languages.\n*   **Dispatch:** The emit process uses a multi-dispatch mechanism to determine how to emit a given form. The `std.lang.base.emit-common/form-key` function analyzes a form and returns a key that is used to look up the appropriate emit function in the grammar's `:reserved` map.\n*   **Customization:** The emit pipeline is designed to be highly customizable. Developers can override default emit functions, define new language constructs through macros, and control the formatting and layout of the generated code."

"**The Emit Pipeline in Detail:**"

"1.  **Preprocessing (`hara.lang.base.emit-preprocess`):**\n    *   **`to-input`:** This is the first step, where the raw Clojure form is converted into an \"input form.\" This involves expanding special reader macros and other syntactic sugar into a more canonical representation. For example, `@` is expanded to a `!:deref` form.\n    *   **`to-staging`:** This is the second and more complex preprocessing step. It takes the input form and prepares it for emission by:\n        *   Resolving symbols and namespaces.\n        *   Expanding macros defined in the grammar.\n        *   Handling dependencies between different code modules.\n        *   Processing inline function assignments.\n\n2.  **Emission (`hara.lang.base.emit`):**\n    *   **`emit-main`:** The main entry point for the emission process. It sets up the dynamic environment and calls `emit-main-loop`.\n    *   **`emit-main-loop`:** This is the core recursive function that traverses the preprocessed form. For each node in the form, it calls `emit-form`.\n    *   **`emit-form`:** This function is the central dispatcher. It calls `form-key` to determine the type of the current form and then dispatches to the appropriate emit function (e.g., `emit-def`, `emit-fn`, `emit-block`).\n\n3.  **Form-Specific Emitters:**\n    *   The `hara.lang.base.emit-*` namespaces provide the actual implementation for emitting different types of forms.\n    *   **`emit-top-level`:** Handles top-level forms like `def`, `defn`, and `defclass`.\n    *   **`emit-fn`:** Handles function definitions and calls, including argument lists and type hints.\n    *   **`emit-block`:** Handles block-level constructs like `do`, `if`, `for`, and `while`.\n    *   **`emit-data`:** Handles the emission of data literals like maps, vectors, lists, and sets.\n    *   **`emit-assign`:** Handles assignment operations.\n    *   **`emit-special`:** Handles special forms like `!:eval` (for evaluating code at compile time) and `!:lang` (for embedding code from another language)."

"**Customizing the Pipeline:**"

"The emit pipeline is designed for extensive customization, primarily through the `grammar` map."

"*   **The Grammar:**\n    *   The grammar is a nested map that contains all the information needed to emit code for a specific language.\n    *   The `:default` key in the grammar contains default settings for things like comments, common syntax elements (e.g., statement terminators, namespace separators), and function/block structure.\n    *   The `:token` key allows customization of how different token types (e.g., strings, symbols, keywords) are emitted.\n    *   The `:data` key controls the emission of data structures.\n    *   The `:define` key controls the emission of top-level definitions.\n    *   The `:block` key controls the emission of block-level constructs.\n\n*   **Reserved Words and Operators:**\n    *   The `:reserved` map in the grammar is used to define how specific symbols are treated. For each symbol, you can specify:\n        *   `:emit`: The type of emission to use (e.g., `:infix`, `:prefix`, `:macro`).\n        *   `:raw`: The raw string to emit for the symbol.\n        *   `:macro`: A macro function to be expanded during preprocessing.\n        *   `:block`: A map defining the structure of a block-level construct.\n\n*   **Macros:**\n    *   Macros provide a powerful way to extend the language. A macro is a function that is called during preprocessing and returns a new form to be emitted.\n    *   Macros are defined in the `:reserved` map with an `:emit` value of `:macro` and a `:macro` key pointing to the macro function.\n\n*   **Custom Emit Functions:**\n    *   You can provide a custom emit function for any form by adding an `:emit` key to its definition in the `:reserved` map. The value of the `:emit` key should be a function that takes the form, grammar, and options map as arguments and returns the emitted string.\n\n*   **Dynamic Variables:**\n    *   Several dynamic variables can be used to control the output:\n        *   `*indent*`: The current indentation level.\n        *   `*compressed*`: If true, emits the code in a compressed format without newlines or extra spaces.\n        *   `*trace*`: If true, prints the form being emitted at each step of the recursion.\n        *   `*explode*`: If true, prints a stack trace on error."

"**Example: Customizing an Operator**"

"To customize the `+` operator to emit `add` instead, you would modify the grammar like this:"

^{:id merged-plans-slop-summary-std-lang-base-emit-summary-md-example-1 :added "4.0"}
(fact "hara.lang.base.emit Summary example"
  (def +my-grammar+
    (h/merge-nested
     helper/+default+
     {:reserved {\+ {:emit :infix :raw "add"}}}))

  (emit/emit '(+ 1 2) +my-grammar+ 'my.ns {})
  => "1 add 2"
)

"This detailed control over the emission process makes the `foundation-base` transpiler a flexible and powerful tool for code generation."
;; END merged documentation: plans/slop/summary/std_lang_base_emit_summary.md

;; BEGIN merged documentation: plans/slop/summary/std_lang_base_grammar_summary.md
;; sha256: a6f3844d6134f9b8828f17ed3269677ef5e2a527e6994c140b140c468f60a27f
[[:chapter {:title "hara.lang.base.grammar Summary" :link "merged-plans-slop-summary-std-lang-base-grammar-summary-md"}]]

"The `std.lang.base.grammar*` namespaces are responsible for defining the structure and semantics of a language that can be emitted by the `foundation-base` transpiler. The grammar is a key component of the emit pipeline, providing the necessary information to translate a Clojure-like form into a string of code in the target language."

"**Core Concepts:**"

"*   **Grammar:** A grammar is a Clojure map that defines the syntax and semantics of a target language. It is created using the `std.lang.base.grammar/grammar` function, which takes a tag, a map of reserved words, and a template as arguments.\n*   **Reserved Words:** The `:reserved` map in the grammar defines how specific symbols are treated by the emitter. For each symbol, you can specify its `op` (operation), `emit` type (e.g., `:infix`, `:prefix`, `:macro`), `raw` string representation, and other properties.\n*   **Operators (`+op-*`):** The `std.lang.base.grammar-spec`, `std.lang.base.grammar-macro`, and `std.lang.base.grammar-xtalk` namespaces define a large set of operators that can be used to build a grammar. These operators cover a wide range of functionalities, from basic arithmetic and logic to more advanced features like macros and cross-language interoperability (`xtalk`).\n*   **Building a Grammar:** The `std.lang.base.grammar/build` function is used to construct a grammar by selecting a set of operators from the available `+op-*` definitions. You can include or exclude specific operator groups to create a grammar that is tailored to your needs."

"**How the Grammar Fits into the Emit Pipeline:**"

"The grammar is a central component of the emit pipeline, and it is used at every stage of the process:"

"1.  **Preprocessing:**\n    *   During the `to-staging` phase, the grammar's `:reserved` map is used to identify and expand macros.\n    *   The grammar is also used to resolve symbols and namespaces.\n\n2.  **Emission:**\n    *   The `emit-main-loop` uses the `form-key` function to determine the key for a form, which is then used to look up the corresponding entry in the grammar's `:reserved` map.\n    *   The `emit-form` function uses the information in the grammar to dispatch to the appropriate emit function (e.g., `emit-def`, `emit-fn`, `emit-block`).\n    *   The form-specific emit functions use the grammar to get information about syntax, formatting, and other language-specific details. For example, `emit-infix` uses the `:raw` value from the grammar to get the string representation of the operator."

"**Customization:**"

"The grammar provides a powerful mechanism for customizing the emit pipeline:"

"*   **Defining a New Language:** You can define a new language by creating a new grammar that specifies the syntax and semantics of the language.\n*   **Extending an Existing Language:** You can extend an existing language by adding new operators and macros to its grammar.\n*   **Overriding Default Behavior:** You can override the default behavior of the emitter by providing your own emit functions in the grammar.\n*   **Controlling Formatting:** The grammar allows you to control the formatting and layout of the generated code by specifying options for indentation, spacing, and newlines."

"**Language-Specific Grammars in `hara.lang.model`:**"

"The `hara.lang.model.*` files provide concrete examples of how to define grammars for different languages. These files demonstrate how to:"

"*   **Select a set of features:** Each language-specific grammar starts by selecting a set of features (operators) from the `+op-*` definitions using `grammar/build`. For example, `spec_c.clj` excludes `:data-shortcuts`, `:control-try-catch`, and `:class`, which are not relevant for the C language.\n*   **Override and extend features:** The grammars then use `grammar/build:override` and `grammar/build:extend` to customize the selected features. For example, `spec_lua.clj` overrides the `:seteq` operator to emit `<-` instead of `=`, and it extends the grammar with Lua-specific operators like `:cat` (for string concatenation) and `:len` (for getting the length of a table).\n*   **Define a template:** Each grammar defines a `+template+` map that specifies the language-specific syntax and formatting rules. This includes things like comment prefixes, statement terminators, namespace separators, and how to emit data structures like maps and vectors.\n*   **Create a book:** Finally, each grammar is packaged into a `book` using `book/book`. The book contains the grammar, as well as metadata about the language, such as how to handle module imports and exports."

"**Example: The Lua Grammar (`spec_lua.clj`)**"

"The Lua grammar provides a good example of how to customize the emit pipeline for a specific language. Here are some key features of the Lua grammar:"

"*   **Custom `var` macro:** The `tf-local` macro provides a more flexible way to declare local variables in Lua.\n*   **Custom `for` loop macros:** The `tf-for-object`, `tf-for-array`, `tf-for-iter`, and `tf-for-index` macros provide different ways to iterate over data structures in Lua.\n*   **Custom map key emission:** The `lua-map-key` function provides custom logic for emitting map keys, taking into account Lua's syntax for table keys.\n*   **C FFI support:** The `tf-c-ffi` macro allows you to embed C code in your Lua code."

"By studying the language-specific grammars in `hara.lang.model`, you can get a good understanding of how to create your own grammars and customize the emit pipeline to support new languages or extend existing ones."
;; END merged documentation: plans/slop/summary/std_lang_base_grammar_summary.md

;; BEGIN merged documentation: plans/slop/summary/std_lang_base_runtime_summary.md
;; sha256: 2754a67bad98475541a1e08145d1aa46ca982fbf4f7746ce3d0f9c34b0b02525
[[:chapter {:title "hara.lang.base.runtime Summary" :link "merged-plans-slop-summary-std-lang-base-runtime-summary-md"}]]

"The `std.lang.base.runtime*` and `std.lang.base.impl*` namespaces, along with `rt.basic`, are responsible for defining, managing, and interacting with language runtimes in the `foundation-base` ecosystem. A runtime is an environment where code can be executed. The system is designed to be extensible, allowing new runtimes to be defined and integrated."

"**Core Concepts:**"

"*   **Runtime:** A runtime is a component that provides an execution environment for a specific language. It implements the `std.protocol.context/IContext` and `std.protocol.component/IComponent` protocols, which define the interface for interacting with the runtime.\n*   **`defimpl`:** The `defimpl` macro from `std.lib.impl` is the primary tool for creating new runtime types. It simplifies the process of defining a `defrecord` that implements one or more protocols.\n*   **Runtime Proxy:** A runtime proxy (`hara.lang.base.runtime-proxy`) is a runtime that forwards calls to another runtime. This is useful for creating aliases or for providing a different interface to an existing runtime.\n*   **Book:** A `book` (`hara.lang.base.book`) is a data structure that contains all the code and metadata for a specific language. Each runtime is associated with a book.\n*   **Pointer:** A `pointer` (`hara.lang.base.pointer`) is a reference to a piece of code in a book. Runtimes use pointers to execute code.\n*   **Lifecycle:** Runtimes have a lifecycle that is managed by the `std.protocol.component/IComponent` protocol. This includes `start`, `stop`, and `kill` functions."

"**Runtime Generation and Customization:**"

"The `foundation-base` ecosystem provides a flexible way to define and customize runtimes."

"**1. Defining a Runtime with `defimpl`:**"

"A new runtime is typically defined using the `defimpl` macro. This macro takes a name, a list of fields, and a set of protocol implementations."

^{:id merged-plans-slop-summary-std-lang-base-runtime-summary-md-example-1 :added "4.0"}
(fact "hara.lang.base.runtime Summary example"
  (defimpl MyRuntime [field1 field2]
    :protocols [std.protocol.context/IContext
                :body {-raw-eval (fn [this string]
                                   ;; implementation for evaluating a raw string
                                   )}
                std.protocol.component/IComponent
                :body {-start (fn [this]
                                ;; implementation for starting the runtime
                                this)
                       -stop (fn [this]
                               ;; implementation for stopping the runtime
                               this)}])
)

"*   **`IContext` Protocol:** This protocol defines the core interface for interacting with a runtime. Key functions include:\n    *   `-raw-eval`: Evaluates a raw string of code in the runtime's context.\n    *   `-invoke-ptr`: Invokes a function pointer with specified arguments.\n    *   `-deref-ptr`: Dereferences a pointer to get its value.\n    *   `-init-ptr`: Initializes a pointer in the runtime.\n\n*   **`IComponent` Protocol:** This protocol defines the component lifecycle for the runtime. Key functions include:\n    *   `-start`: Starts the runtime, preparing it for execution.\n    *   `-stop`: Stops the runtime, releasing any resources."

"**2. The `RuntimeDefault` Record:**"

"The `hara.lang.base.runtime` namespace defines a `RuntimeDefault` record using `defimpl`. This record provides a default implementation for the `IContext` and `IComponent` protocols."

"*   It serves as a base for many of the language-specific runtimes in `rt.basic`.\n*   It includes logic for proxying calls to another runtime via the `redirect` field.\n*   The `default-*` functions in `hara.lang.base.runtime` provide the actual implementations for the protocol functions. For example, `default-invoke-ptr` handles the logic for invoking a function pointer."

"**3. Customization:**"

"*   **Extending `RuntimeDefault`:** The easiest way to create a new runtime is to extend `RuntimeDefault` and override the functions that need to be customized.\n*   **Creating a New Runtime from Scratch:** For more advanced use cases, you can create a new runtime from scratch by implementing the `IContext` and `IComponent` protocols yourself.\n*   **Runtime Proxies:** The `hara.lang.base.runtime-proxy` namespace allows you to create a proxy for an existing runtime. This is useful for adding functionality or for creating a different interface to a runtime."

"**Example: A Simple Runtime Definition**"

"The following example from `rt.basic.type-basic` shows how a basic runtime is defined using `defimpl`:"

^{:id merged-plans-slop-summary-std-lang-base-runtime-summary-md-example-2 :added "4.0"}
(fact "hara.lang.base.runtime Summary example"
  (defimpl RuntimeBasic [id lang]
    :protocols [protocol.context/IContext
                :body {-raw-eval eval-string}
                protocol.component/IComponent
                :body {-start start-basic
                       -stop stop-basic}])
)

"This defines a `RuntimeBasic` record that implements the `IContext` and `IComponent` protocols. The `-raw-eval` function is implemented by `eval-string`, and the `-start` and `-stop` functions are implemented by `start-basic` and `stop-basic`, respectively."

"By using `defimpl` and the provided protocols, the `foundation-base` ecosystem makes it easy to create new runtimes and to extend the functionality of existing ones."
;; END merged documentation: plans/slop/summary/std_lang_base_runtime_summary.md

;; BEGIN merged documentation: plans/slop/summary/std_lang_base_script_summary.md
;; sha256: e07fa2455dfa8e1d40bdb15128236a844fd6b3da531043e669b7e61b659684f8
[[:chapter {:title "hara.lang.base.script Summary" :link "merged-plans-slop-summary-std-lang-base-script-summary-md"}]]

"The `std.lang.base.script*` namespaces provide a high-level interface for interacting with the `foundation-base` language ecosystem. They tie together the `grammar`, `emit`, `book`, and `runtime` components to provide a seamless experience for defining, compiling, and executing code in different languages."

"**Core Concepts:**"

"*   **Script:** A \"script\" is a self-contained unit of code that can be executed in a specific language runtime.\n*   **`script` macro:** The `script` macro is the main entry point for defining a script. It takes a language keyword, a module name, and a configuration map as arguments.\n*   **Runtime Management:** The `hara.lang.base.script-control` namespace provides functions for managing the lifecycle of language runtimes, including `script-rt-get`, `script-rt-stop`, and `script-rt-restart`.\n*   **Annex:** An \"annex\" (`hara.lang.base.script-annex`) is a way to extend an existing language with new functionality. It allows you to define new macros and functions that can be used in the extended language."

"**How `hara.lang.base.script` Ties Everything Together:**"

"The `script` macro is the glue that holds the `foundation-base` language ecosystem together. When you use the `script` macro, it performs the following steps:"

"1.  **Module Definition:** It defines a new module in the `book` for the specified language. This module contains the code and metadata for the script.\n2.  **Runtime Initialization:** It gets or creates a runtime for the specified language using `script-rt-get`.\n3.  **Macro and Highlight Interning:** It interns the macros and highlight symbols from the language's grammar into the current namespace, making them available for use in the script.\n4.  **Code Execution:** The code within the `script` macro is then executed in the context of the specified language runtime."

"**The `!` Macro:**"

"The `!` macro provides a convenient way to switch between different language runtimes within the same namespace. This is especially useful for testing and for writing polyglot scripts."

^{:id merged-plans-slop-summary-std-lang-base-script-summary-md-example-1 :added "4.0"}
(fact "hara.lang.base.script Summary example"
  (require '[hara.lang.base.script :as script])

  (script/script :lua my-lua-module)
  (script/script+ [:py :python] {})

  (!:lua (+ 1 2))
  => 3

  (!:py (+ 1 2))
  => 3
)

"In this example, the `script` macro is used to set up the `:lua` runtime, and the `script+` macro is used to set up the `:python` runtime as an annex. The `!` macro is then used to execute code in each of these runtimes."

"**Summary:**"

"The `std.lang.base.script*` namespaces provide a powerful and flexible way to work with multiple languages in the `foundation-base` ecosystem. By abstracting away the details of runtime management and code compilation, they allow developers to focus on writing code in the language of their choice. The `script` and `!` macros, in particular, provide a seamless and intuitive way to work with polyglot code."
;; END merged documentation: plans/slop/summary/std_lang_base_script_summary.md

;; BEGIN merged documentation: plans/slop/summary/std_lang_summary.md
;; sha256: 2abaa505197548af542d62431d7a5e9b884fb0756e14d91db097aad882b84f00
[[:chapter {:title "hara.lang: A Comprehensive Summary (including submodules)" :link "merged-plans-slop-summary-std-lang-summary-md"}]]

"The `std.lang` module is the core of the `foundation-base` ecosystem, providing a powerful and extensible framework for defining, transpiling, and managing multiple programming languages. It enables developers to write code in a Clojure-like DSL and then generate equivalent code in various target languages (JavaScript, Lua, Python, C, Rust, GLSL, Bash, Scheme, JQ, PostgreSQL, etc.). This module is central to the project's goal of creating a polyglot development environment with unified tooling and runtime management."

[[:section {:title "hara.lang (Main Namespace)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-main-namespace"}]]

"This namespace orchestrates the functionality of its submodules, providing a high-level interface for language definition, code generation, and runtime interaction. It re-exports key functions from its sub-namespaces, making it a convenient entry point for language-oriented programming."

"**Key Re-exported Functions:**"

"*   From `hara.lang.base.util`: `sym-full`, `sym-id`, `sym-module`, `sym-pair`, `sym-default-str`, `ptr`.\n*   From `hara.lang.base.emit-common`: `with:explode`, `with-trace`.\n*   From `hara.lang.base.emit`: `with:emit`, `emit*`.\n*   From `hara.lang.base.emit-helper`: `basic-typed-args`, `emit-type-record`.\n*   From `hara.lang.base.emit-preprocess`: `macro-form`, `macro-opts`, `macro-grammar`, `with:macro-opts`.\n*   From `hara.lang.base.impl`: `emit-script`, `emit-str`, `emit-as`, `emit-symbol`, `default-library`, `default-library:reset`, `runtime-library`, `with:library`, `grammar`.\n*   From `hara.lang.base.impl-entry`: `emit-entry`, `with:cache-none`, `with:cache-force`.\n*   From `hara.lang.base.impl-deps`: `emit-entry-deps`.\n*   From `hara.lang.base.pointer`: `with:print`, `with:print-all`, `with:clip`, `with:input`, `with:raw`, `with:rt`, `with:rt-wrap`, `rt:macro-opts`.\n*   From `hara.lang.base.script`: `script`, `script-`, `script+`, `!`, `annex:get`, `annex:start`, `annex:stop`, `annex:restart-all`, `annex:start-all`, `annex:stop-all`, `annex:list`.\n*   From `hara.lang.base.script-def`: `tmpl-entry`, `tmpl-macro`.\n*   From `hara.lang.base.library`: `get-book-raw`, `get-book`, `get-module`, `get-snapshot`, `delete-module!`, `delete-modules!`, `delete-entry!`, `purge-book!`.\n*   From `hara.lang.base.script-lint`: `lint-set`, `lint-clear`.\n*   From `hara.lang.base.util`: `rt`, `rt:list`, `rt:default`.\n*   From `hara.lang.base.script-control`: `rt:restart`, `rt:stop`.\n*   From `std.lang.base.workspace`: `sym-entry`, `module-entries`, `emit-ptr`, `emit-module`, `print-module`, `ptr-clip`, `ptr-print`, `ptr-setup`, `ptr-teardown`, `ptr-setup-deps`, `ptr-teardown-deps`, `rt:module`, `rt:module-purge`, `rt:inner`, `rt:restart`, `rt:setup`, `rt:setup-to`, `rt:setup-single`, `rt:scaffold`, `rt:scaffold-to`, `rt:scaffold-imports`, `rt:teardown`, `rt:teardown-at`, `rt:teardown-single`, `rt:teardown-to`, `intern-macros`.\n*   From `hara.lang.base.manage`: `lib:overview`, `lib:module`, `lib:entries`, `lib:purge`, `lib:unused`."

"**Key Functions:**"

"*   **`rt:space`**: Retrieves the runtime for a given language and namespace.\n*   **`get-entry`**: Retrieves a book entry from a pointer or map.\n*   **`as-lua`**: Transforms Clojure vectors to Lua tables (empty vectors to empty maps).\n*   **`rt:invoke`**: Invokes code in a specified runtime.\n*   **`force-reload`**: Forces reloading of a namespace and its dependents."

[[:section {:title "hara.lang.base.book (Language Book Management)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-book-language-book-management"}]]

"This sub-namespace defines the core data structures for storing and managing language definitions, including books, modules, and entries. It provides functions for accessing and manipulating these structures."

"**Core Concepts:**"

"*   **`Book` Record:** Represents a language definition, containing its `lang`, `meta` (metadata), `grammar`, `modules`, `parent` (for inheritance), and `referenced` modules.\n*   **`BookModule` Record:** Represents a single source file or a collection of related code within a language, containing `alias`, `link`, `native` imports, `code` entries, `fragment` (macros), and `static` metadata.\n*   **`BookEntry` Record:** Represents a single piece of code (function, macro, variable), containing its `id`, `module`, `section` (code, fragment, header), `form`, `form-input`, `deps`, `namespace`, and `static` metadata."

"**Key Functions:**"

"*   **`get-base-entry`, `get-code-entry`, `get-entry`, `get-module`**: Functions for retrieving entries and modules from a book.\n*   **`get-code-deps`, `get-deps`**: Functions for retrieving dependencies of code entries and modules.\n*   **`list-entries`**: Lists entries within a book.\n*   **`book-string`**: Returns a string representation of a book.\n*   **`book?`, `book`**: Predicate and constructor for `Book` records.\n*   **`book-merge`**: Merges a book with its parent book (for language inheritance).\n*   **`book-from`**: Returns a merged book from a snapshot.\n*   **`check-compatible-lang`, `assert-compatible-lang`**: Checks and asserts language compatibility.\n*   **`set-module`, `put-module`, `delete-module`, `delete-modules`, `has-module?`, `assert-module`**: Functions for managing modules within a book.\n*   **`set-entry`, `put-entry`, `delete-entry`, `has-entry?`, `assert-entry`**: Functions for managing entries within a module.\n*   **`module-create-bundled`**: Creates bundled packages for modules.\n*   **`module-create-filename`**: Generates a filename for a module.\n*   **`module-create-check`**: Checks for bundle availability.\n*   **`module-create-requires`**: Creates a map for module requirements.\n*   **`module-create`**: Creates a `BookModule` record.\n*   **`module-deps`**: Gets dependencies for a module."

[[:section {:title "hara.lang.base.compile (Code Compilation and Output)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-compile-code-compilation-and-output"}]]

"This sub-namespace provides functions for compiling and writing generated code to files, supporting various output formats and module structures."

"**Key Functions:**"

"*   **`compile-script`**: Compiles a single script entry.\n*   **`compile-module-single`**: Compiles a single module.\n*   **`compile-module-graph-rel`**: Extracts the relative path for a module in a graph.\n*   **`compile-module-graph-single`**: Compiles a single module file within a graph.\n*   **`compile-module-graph`**: Compiles a graph of modules.\n*   **`compile-module-directory-single`**: Compiles a single module file within a directory structure.\n*   **`compile-module-directory`**: Compiles modules from a directory structure.\n*   **`compile-module-schema`**: Compiles all modules into a single schema file (e.g., for SQL)."

[[:section {:title "hara.lang.base.emit (Code Emission Pipeline)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-code-emission-pipeline"}]]

"This sub-namespace defines the core code emission pipeline, responsible for transforming Clojure forms into target language strings based on a grammar."

"**Core Concepts:**"

"*   **`default-grammar`**: Provides a base grammar with common settings.\n*   **`emit-main-loop`**: The recursive function that traverses forms and dispatches to appropriate emitters.\n*   **`emit-main`**: The main entry point for emitting a single form.\n*   **`emit`**: Emits a form to an output string, handling grammar and options.\n*   **`with:emit` (macro)**: Binds the top-level emit function.\n*   **`prep-options`**: Prepares options for the emit pipeline.\n*   **`prep-form`**: Preprocesses a form through different stages (raw, input, staging)."

[[:section {:title "hara.lang.base.emit-assign (Assignment Emission)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-assign-assignment-emission"}]]

"This sub-namespace handles the emission of assignment-related forms, including inline function assignments and variable declarations."

"**Key Functions:**"

"*   **`emit-def-assign-inline`**: Emits an inline function assignment.\n*   **`emit-def-assign`**: Emits a variable declaration or assignment.\n*   **`test-assign-loop`, `test-assign-emit`**: Test functions for assignment emission."

[[:section {:title "hara.lang.base.emit-block (Block Emission)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-block-block-emission"}]]

"This sub-namespace handles the emission of control flow blocks (do, if, for, while, try, switch) and their associated parameters and bodies."

"**Key Functions:**"

"*   **`emit-statement`**: Emits a single statement.\n*   **`emit-do`, `emit-do*`**: Emits `do` blocks.\n*   **`block-options`**: Retrieves options for a block.\n*   **`emit-block-body`**: Emits the body of a block.\n*   **`parse-params`**: Parses parameters for a block.\n*   **`emit-params-statement`, `emit-params`**: Emits parameters for a block.\n*   **`emit-block-control`, `emit-block-controls`**: Emits control flow constructs within a block.\n*   **`emit-block-setup`**: Prepares a block for emission.\n*   **`emit-block-inner`**: Emits the inner content of a block.\n*   **`emit-block-standard`**: Emits a generic block.\n*   **`emit-block`**: The main function for emitting block expressions.\n*   **`test-block-loop`, `test-block-emit`**: Test functions for block emission."

[[:section {:title "hara.lang.base.emit-common (Common Emission Utilities)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-common-common-emission-utilities"}]]

"This sub-namespace provides fundamental utilities and dynamic variables used across the entire emission pipeline, such as indentation, tracing, and generic emission functions."

"**Core Concepts:**"

"*   **Dynamic Variables:** `*explode*`, `*trace*`, `*indent*`, `*compressed*`, `*multiline*`, `*max-len*`, `*emit-internal*`, `*emit-fn*`.\n*   **Emission Types:** `:discard`, `:free`, `:squash`, `:comment`, `:indent`, `:token`, `:alias`, `:unit`, `:internal`, `:internal-str`, `:pre`, `:post`, `:prefix`, `:postfix`, `:infix`, `:infix-`, `:infix*`, `:infix-if`, `:bi`, `:between`, `:assign`, `:invoke`, `:new`, `:static-invoke`, `:index`, `:return`, `:macro`, `:template`, `:with-global`, `:with-decorate`, `:with-uuid`, `:with-rand`."

"**Key Functions:**"

"*   **`with:explode`, `with-trace`, `with-compressed`, `with-indent` (macros)**: Control dynamic emission settings.\n*   **`newline-indent`**: Returns a newline with appropriate indentation.\n*   **`emit-reserved-value`**: Emits a reserved value.\n*   **`emit-free-raw`, `emit-free`**: Emits free-form text.\n*   **`emit-comment`**: Emits a comment.\n*   **`emit-indent`**: Emits an indented form.\n*   **`emit-macro`**: Emits a macro.\n*   **`emit-array`**: Emits an array of forms.\n*   **`emit-wrappable?`**: Checks if a form is wrappable.\n*   **`emit-squash`**: Emits a squashed representation.\n*   **`emit-wrapping`**: Emits a potentially wrapped form.\n*   **`wrapped-str`**: Wraps a string with start/end delimiters.\n*   **`emit-unit`**: Emits a unit.\n*   **`emit-internal`, `emit-internal-str`**: Emits internal forms or strings.\n*   **`emit-pre`, `emit-post`, `emit-prefix`, `emit-postfix`**: Emits operators before/after/prefix/postfix to arguments.\n*   **`emit-infix`, `emit-infix-default`, `emit-infix-pre`, `emit-infix-if-single`, `emit-infix-if`**: Emits infix expressions.\n*   **`emit-between`**: Emits a raw symbol between two elements.\n*   **`emit-bi`**: Emits a binary infix operator.\n*   **`emit-assign`**: Emits an assignment.\n*   **`emit-return-do`, `emit-return-base`, `emit-return`**: Emits return statements.\n*   **`emit-with-global`**: Emits a global variable.\n*   **`emit-symbol-classify`**: Classifies a symbol.\n*   **`emit-symbol-standard`**: Emits a standard symbol.\n*   **`emit-symbol`**: Emits a symbol.\n*   **`emit-token`**: Emits a token.\n*   **`emit-with-decorate`**: Emits a decorated form.\n*   **`emit-with-uuid`, `emit-with-rand`**: Emits UUIDs or random numbers.\n*   **`invoke-kw-parse`**: Parses keyword arguments for invocation.\n*   **`emit-invoke-kw-pair`, `emit-invoke-args`, `emit-invoke-layout`, `emit-invoke-raw`, `emit-invoke-static`, `emit-invoke-typecast`, `emit-invoke`**: Emits function invocations.\n*   **`emit-new`**: Emits a constructor call.\n*   **`emit-class-static-invoke`**: Emits a static class invocation.\n*   **`emit-index-entry`, `emit-index`**: Emits indexed expressions.\n*   **`emit-op`**: Dispatches to the appropriate emitter based on the operation.\n*   **`form-key`**: Returns the key associated with a form.\n*   **`emit-common-loop`**: The core emission loop.\n*   **`emit-common`**: Emits a string based on grammar."

[[:section {:title "hara.lang.base.emit-data (Data Structure Emission)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-data-data-structure-emission"}]]

"This sub-namespace handles the emission of various Clojure data structures (maps, vectors, sets, tuples) into their target language equivalents."

"**Key Functions:**"

"*   **`default-map-key`**: Emits a default map key.\n*   **`emit-map-key`**: Emits a map key.\n*   **`emit-map-entry`**: Emits a map entry.\n*   **`emit-singleline-array?`**: Checks if an array can be emitted on a single line.\n*   **`emit-maybe-multibody`**: Emits a multi-line body if necessary.\n*   **`emit-coll-layout`**: Lays out a collection.\n*   **`emit-coll`**: Emits a collection.\n*   **`emit-data-standard`**: Emits standard data.\n*   **`emit-data`**: The main function for emitting data forms.\n*   **`emit-quote`**: Emits a quoted form.\n*   **`emit-table-group`**: Groups table arguments.\n*   **`emit-table`**: Emits a table.\n*   **`test-data-loop`, `test-data-emit`**: Test functions for data emission."

[[:section {:title "hara.lang.base.emit-fn (Function Emission)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-fn-function-emission"}]]

"This sub-namespace handles the emission of function definitions and related constructs, including argument lists, type hints, and function bodies."

"**Key Functions:**"

"*   **`emit-input-default`**: Emits a default input argument string.\n*   **`emit-hint-type`**: Emits a type hint.\n*   **`emit-def-type`**: Emits a definition type.\n*   **`emit-fn-type`**: Emits a function type.\n*   **`emit-fn-block`**: Retrieves block options for a function.\n*   **`emit-fn-preamble-args`**: Emits preamble arguments for a function.\n*   **`emit-fn-preamble`**: Emits the preamble of a function.\n*   **`emit-fn`**: The main function for emitting function templates.\n*   **`test-fn-loop`, `test-fn-emit`**: Test functions for function emission."

[[:section {:title "hara.lang.base.emit-helper (Emission Helper Functions)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-helper-emission-helper-functions"}]]

"This sub-namespace provides various helper functions and constants used throughout the emission pipeline, such as default grammar settings, symbol replacement rules, and argument parsing."

"**Key Functions:**"

"*   **`default-emit-fn`**: The default emit function.\n*   **`pr-single`**: Prints a single-quoted string.\n*   **`+sym-replace+`**: Symbol replacement rules.\n*   **`+default+`**: Default grammar settings.\n*   **`get-option`, `get-options`**: Retrieves grammar options.\n*   **`form-key-base`**: Gets the base key for a form.\n*   **`basic-typed-args`**: Parses basic typed arguments.\n*   **`emit-typed-allowed-args`**: Emits allowed typed arguments.\n*   **`emit-typed-args`**: Emits typed arguments.\n*   **`emit-symbol-full`**: Emits a full symbol.\n*   **`emit-type-record`**: Formats a type record."

[[:section {:title "hara.lang.base.emit-preprocess (Emission Preprocessing)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-preprocess-emission-preprocessing"}]]

"This sub-namespace handles the preprocessing of Clojure forms before they are emitted, including macro expansion, symbol resolution, and dependency collection."

"**Core Concepts:**"

"*   **Dynamic Variables:** `*macro-form*`, `*macro-grammar*`, `*macro-opts*`, `*macro-splice*`, `*macro-skip-deps*`.\n*   **Preprocessing Stages:** `to-input` (raw to input forms), `to-staging` (input to staged forms)."

"**Key Functions:**"

"*   **`macro-form`, `macro-opts`, `macro-grammar`**: Accessors for macro context.\n*   **`with:macro-opts` (macro)**: Binds macro options.\n*   **`to-input-form`**: Processes a raw form into an input form.\n*   **`to-input`**: Converts a raw form to an input form.\n*   **`get-fragment`**: Retrieves a fragment (macro) from modules.\n*   **`process-namespaced-resolve`**: Resolves a symbol in the current namespace.\n*   **`process-namespaced-symbol`**: Processes namespaced symbols.\n*   **`process-inline-assignment`**: Prepares a form for inline assignment.\n*   **`to-staging-form`**: Processes different staging forms.\n*   **`to-staging`**: Converts an input form to a staged form, collecting dependencies.\n*   **`to-resolve`**: Resolves code symbols without macroexpansion."

[[:section {:title "hara.lang.base.emit-special (Special Form Emission)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-special-special-form-emission"}]]

"This sub-namespace handles the emission of special forms, such as `!:module` (for emitting module contents), `!:eval` (for evaluating Clojure code at emit time), and `!:lang` (for embedding code from another language)."

"**Key Functions:**"

"*   **`emit-with-module-all-ids`**: Emits all IDs from a module.\n*   **`emit-with-module`**: Emits a module.\n*   **`emit-with-preprocess`**: Emits a preprocessed form.\n*   **`emit-with-eval`**: Emits an evaluated form.\n*   **`emit-with-deref`**: Emits a dereferenced var.\n*   **`emit-with-lang`**: Emits an embedded language form.\n*   **`test-special-loop`, `test-special-emit`**: Test functions for special form emission."

[[:section {:title "hara.lang.base.emit-top-level (Top-Level Form Emission)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-emit-top-level-top-level-form-emission"}]]

"This sub-namespace handles the emission of top-level forms like `defn`, `def`, `defglobal`, `defrun`, and `defclass`."

"**Key Functions:**"

"*   **`transform-defclass-inner`**: Transforms the body of a `defclass`.\n*   **`emit-def`**: Emits a `def` statement.\n*   **`emit-declare`**: Emits a `declare` statement.\n*   **`emit-top-level`**: The main function for emitting top-level forms.\n*   **`emit-form`**: Emits a customisable form."

[[:section {:title "hara.lang.base.grammar (Language Grammar Definition)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-grammar-language-grammar-definition"}]]

"This sub-namespace defines the structure and semantics of a target language's grammar, including its reserved words, operators, and emission rules."

"**Core Concepts:**"

"*   **`Grammer` Record:** Represents a language grammar, containing its `tag`, `emit` function, `structure`, `reserved` words, `banned` forms, `highlight` keywords, and `macros`.\n*   **Operators (`+op-all+`):** A comprehensive collection of predefined operators (math, compare, logic, control flow, data structures, macros, cross-language `xtalk` operations) that can be included in a grammar."

"**Key Functions:**"

"*   **`gen-ops`**: Generates operators from a namespace.\n*   **`collect-ops`**: Collects all operators.\n*   **`ops-list`, `ops-symbols`, `ops-summary`, `ops-detail`**: Functions for inspecting operators.\n*   **`build`**: Selects operators for a grammar.\n*   **`build-min`**: Builds a minimal grammar.\n*   **`build-xtalk`**: Builds a grammar with cross-language operators.\n*   **`build:override`, `build:extend`**: Modifies an existing grammar.\n*   **`to-reserved`**: Converts an operator map to a reserved word map.\n*   **`grammar-structure`, `grammar-sections`, `grammar-macros`**: Extracts structural information from a grammar.\n*   **`grammar?`, `grammar`**: Predicate and constructor for `Grammer` records."

[[:section {:title "hara.lang.base.grammar-macro (Macro Transformations)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-grammar-macro-macro-transformations"}]]

"This sub-namespace provides macro transformations for common Clojure forms (e.g., `->`, `->>`, `when`, `if`, `cond`, `let`, `case`, `doto`, `fn:>`) into more basic forms suitable for emission."

"**Key Functions:**"

"*   **`tf-macroexpand`**: Macroexpands a form.\n*   **`tf-when`**: Transforms `when` to a branch.\n*   **`tf-if`**: Transforms `if` to a branch.\n*   **`tf-cond`**: Transforms `cond` to a branch.\n*   **`tf-let-bind`**: Transforms `let` bindings.\n*   **`tf-case`**: Transforms `case` to a switch.\n*   **`tf-lambda-arrow`**: Transforms lambda arrows.\n*   **`tf-tcond`**: Transforms ternary conditions.\n*   **`tf-xor`**: Transforms `xor` to a ternary if.\n*   **`tf-doto`**: Transforms `doto` to a sequence of `do` operations.\n*   **`tf-do-arrow`**: Transforms `do:>` to a function.\n*   **`tf-forange`**: Transforms `forange` to a `for` loop.\n*   **`+op-macro+`, `+op-macro-arrow+`, `+op-macro-let+`, `+op-macro-xor+`, `+op-macro-case+`, `+op-macro-forange+`**: Collections of macro operators."

[[:section {:title "hara.lang.base.grammar-spec (Grammar Specification)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-grammar-spec-grammar-specification"}]]

"This sub-namespace defines the core set of operators and their properties that form the basis of most language grammars. It includes operators for built-in functions, math, comparisons, logic, control flow, and top-level definitions."

"**Key Functions:**"

"*   **`get-comment`**: Retrieves the comment prefix for a language.\n*   **`format-fargs`**: Formats function arguments.\n*   **`format-defn`**: Formats `defn` forms.\n*   **`tf-for-index`**: Transforms `for:index` loops.\n*   **`+op-builtin+`, `+op-builtin-global+`, `+op-builtin-module+`, `+op-builtin-helper+`**: Built-in operators.\n*   **`+op-free-control+`, `+op-free-literal+`**: Free control and literal operators.\n*   **`+op-math+`, `+op-compare+`, `+op-logic+`, `+op-counter+`**: Math, comparison, logic, and counter operators.\n*   **`+op-return+`, `+op-throw+`, `+op-await+`**: Return, throw, and await operators.\n*   **`+op-data-table+`, `+op-data-shortcuts+`, `+op-data-range+`**: Data table, shortcuts, and range operators.\n*   **`+op-vars+`, `+op-bit+`, `+op-pointer+`**: Variable, bit, and pointer operators.\n*   **`+op-fn+`, `+op-block+`**: Function and block operators.\n*   **`+op-control-base+`, `+op-control-general+`, `+op-control-try-catch+`**: Control flow operators.\n*   **`+op-top-base+`, `+op-top-global+`**: Top-level operators.\n*   **`+op-class+`**: Class-related operators.\n*   **`+op-for+`, `+op-coroutine+`**: For loop and coroutine operators."

[[:section {:title "hara.lang.base.grammar-xtalk (Cross-Language Operators)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-grammar-xtalk-cross-language-operators"}]]

"This sub-namespace defines a set of \"cross-talk\" (xtalk) operators that provide a common interface for language-agnostic operations, such as object manipulation, type checking, and I/O. These operators are designed to be implemented differently in each target language."

"**Key Functions:**"

"*   **`tf-throw`**: Transforms `throw`.\n*   **`tf-eq-nil?`, `tf-not-nil?`**: Transforms nil checks.\n*   **`tf-proto-create`**: Transforms prototype creation.\n*   **`tf-has-key?`**: Transforms key existence checks.\n*   **`tf-get-path`, `tf-get-key`**: Transforms property access.\n*   **`tf-set-key`, `tf-del-key`**: Transforms property setting/deletion.\n*   **`tf-copy-key`**: Transforms key copying.\n*   **`tf-grammar-offset`, `tf-grammar-end-inclusive`**: Retrieves grammar-specific offset/inclusive settings.\n*   **`tf-offset-base`, `tf-offset`, `tf-offset-rev`, `tf-offset-len`, `tf-offset-rlen`**: Transforms offset calculations.\n*   **`tf-global-set`, `tf-global-has?`, `tf-global-del`**: Transforms global variable operations.\n*   **`tf-lu-eq`**: Transforms lookup equality.\n*   **`tf-bit-and`, `tf-bit-or`, `tf-bit-lshift`, `tf-bit-rshift`, `tf-bit-xor`**: Transforms bitwise operations.\n*   **`+op-xtalk-core+`, `+op-xtalk-proto+`, `+op-xtalk-global+`, `+op-xtalk-custom+`, `+op-xtalk-math+`, `+op-xtalk-type+`, `+op-xtalk-bit+`, `+op-xtalk-lu+`, `+op-xtalk-obj+`, `+op-xtalk-arr+`, `+op-xtalk-str+`, `+op-xtalk-js+`, `+op-xtalk-return+`, `+op-xtalk-socket+`, `+op-xtalk-iter+`, `+op-xtalk-cache+`, `+op-xtalk-thread+`, `+op-xtalk-file+`, `+op-xtalk-b64+`, `+op-xtalk-uri+`, `+op-xtalk-special+`**: Collections of cross-language operators."

[[:section {:title "hara.lang.base.impl (Core Implementation Utilities)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-impl-core-implementation-utilities"}]]

"This sub-namespace provides core implementation details and helper functions for the language system, including managing the global library, emit options, and direct code emission."

"**Key Functions:**"

"*   **`with:library` (macro)**: Binds a library as the default.\n*   **`default-library`, `default-library:reset`**: Manages the default library.\n*   **`runtime-library`**: Retrieves the current runtime library.\n*   **`grammar`**: Retrieves the grammar for a language.\n*   **`emit-options-raw`, `emit-options`**: Prepares emit options.\n*   **`to-form`**: Converts input to a form.\n*   **`%.form` (macro)**: Converts to a form.\n*   **`emit-bulk?`**: Checks if a form is a bulk form.\n*   **`emit-direct`**: Emits a form directly.\n*   **`emit-str`**: Converts a form to an output string.\n*   **`%.str` (macro)**: Converts to an output string.\n*   **`emit-as`**: Emits multiple forms.\n*   **`emit-symbol`**: Emits a symbol.\n*   **`get-entry`**: Retrieves an entry.\n*   **`emit-entry`**: Emits an entry.\n*   **`emit-entry-deps-collect`**: Collects entry dependencies.\n*   **`emit-entry-deps`**: Emits entry dependencies.\n*   **`emit-script-imports`**: Emits script imports.\n*   **`emit-script-deps`**: Emits script dependencies.\n*   **`emit-script-join`**: Joins script parts.\n*   **`emit-script`**: Emits a script with all dependencies.\n*   **`emit-scaffold-raw-imports`**: Emits scaffold raw imports.\n*   **`emit-scaffold-raw`**: Creates scaffold raw entries.\n*   **`emit-scaffold-for`**: Creates scaffold for a module.\n*   **`emit-scaffold-to`**: Creates scaffold up to a module.\n*   **`emit-scaffold-imports`**: Creates scaffold to expose native imports."

[[:section {:title "hara.lang.base.impl-deps (Dependency Management Implementation)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-impl-deps-dependency-management-implementation"}]]

"This sub-namespace provides the implementation details for managing dependencies between modules and entries, including collecting native imports and resolving module links."

"**Key Functions:**"

"*   **`module-import-form`, `module-export-form`, `module-link-form`**: Generates import/export/link forms.\n*   **`has-module-form`, `setup-module-form`, `teardown-module-form`**: Generates module lifecycle forms.\n*   **`has-ptr-form`, `setup-ptr-form`, `teardown-ptr-form`**: Generates pointer lifecycle forms.\n*   **`collect-script-natives`**: Collects native imported modules.\n*   **`collect-script-entries`**: Collects all entries for a script.\n*   **`collect-script`**: Collects dependencies for a script.\n*   **`collect-script-summary`**: Summarizes script dependencies.\n*   **`collect-module-check-options`**: Checks module options.\n*   **`collect-module-ns-select`**: Selects module namespaces.\n*   **`collect-module-directory-form`**: Collects module directory forms.\n*   **`collect-module`**: Collects information for an entire module."

[[:section {:title "hara.lang.base.impl-entry (Entry Implementation)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-impl-entry-entry-implementation"}]]

"This sub-namespace provides the implementation details for creating and emitting book entries, including handling metadata, preprocessing, and caching."

"**Key Functions:**"

"*   **`create-common`**: Creates common entry keys from metadata.\n*   **`create-code-raw`**: Creates a raw code entry.\n*   **`create-code-base`**: Creates a base code entry.\n*   **`create-code-hydrate`**: Hydrates code entries.\n*   **`create-code`**: Creates a code entry.\n*   **`create-fragment`**: Creates a fragment entry.\n*   **`create-macro`**: Creates a macro entry.\n*   **`with:cache-none`, `with:cache-force` (macros)**: Control entry caching.\n*   **`emit-entry-raw`**: Emits a raw entry.\n*   **`+cached-emit-keys+`, `+cached-keys+`**: Keys for cached emits.\n*   **`emit-entry-cached`**: Emits a cached entry.\n*   **`emit-entry-label`**: Emits an entry label.\n*   **`emit-entry`**: Emits a given entry."

[[:section {:title "hara.lang.base.impl-lifecycle (Lifecycle Implementation)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-impl-lifecycle-lifecycle-implementation"}]]

"This sub-namespace provides the implementation details for managing the lifecycle of modules, including emitting setup and teardown scripts."

"**Key Functions:**"

"*   **`emit-module-prep`**: Prepares a module for emission.\n*   **`emit-module-setup-concat`, `emit-module-setup-join`, `emit-module-setup-native-arr`, `emit-module-setup-link-arr`, `emit-module-setup-raw`, `emit-module-setup`**: Functions for emitting module setup code.\n*   **`emit-module-teardown-concat`, `emit-module-teardown-join`, `emit-module-teardown-raw`, `emit-module-teardown`**: Functions for emitting module teardown code."

[[:section {:title "hara.lang.base.library (Library Management)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-library-library-management"}]]

"This sub-namespace defines the core `Library` record and provides functions for managing a collection of language books and their modules/entries. It handles snapshot management and bulk operations."

"**Key Functions:**"

"*   **`wait-snapshot`**: Waits for the current snapshot to be ready.\n*   **`wait-apply`**: Applies a function to the library state when the task queue is empty.\n*   **`wait-mutate!`**: Mutates the library state once the task queue is empty.\n*   **`get-snapshot`**: Retrieves the current snapshot.\n*   **`get-book`, `get-book-raw`**: Retrieves a book from the library.\n*   **`get-module`, `get-entry`**: Retrieves a module or entry from the library.\n*   **`add-book!`, `delete-book!`**: Adds or deletes a book.\n*   **`reset-all!`**: Resets the library.\n*   **`list-modules`, `list-entries`**: Lists modules or entries.\n*   **`add-module!`, `delete-module!`, `delete-modules!`**: Adds or deletes modules.\n*   **`library-string`**: Returns a string representation of the library.\n*   **`library?`, `library:create`, `library`**: Predicate and constructors for `Library` records.\n*   **`add-entry!`, `add-entry-single!`, `delete-entry!`**: Adds or deletes entries.\n*   **`install-module!`, `install-book!`, `purge-book!`**: Installs modules or books."

[[:section {:title "hara.lang.base.library-snapshot (Library Snapshot Management)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-library-snapshot-library-snapshot-management"}]]

"This sub-namespace defines the `Snapshot` record and provides functions for managing immutable snapshots of the library's state, enabling efficient versioning and merging of language definitions."

"**Key Functions:**"

"*   **`get-deps`**: Retrieves dependencies from a snapshot.\n*   **`snapshot-string`**: Returns a string representation of a snapshot.\n*   **`snapshot?`, `snapshot`**: Predicate and constructor for `Snapshot` records.\n*   **`snapshot-reset`**: Resets a snapshot.\n*   **`snapshot-merge`**: Merges two snapshots.\n*   **`get-book-raw`, `get-book`**: Retrieves a book from a snapshot.\n*   **`add-book`**: Adds a book to a snapshot.\n*   **`set-module`, `delete-module`, `delete-modules`**: Manages modules in a snapshot.\n*   **`list-modules`, `list-entries`**: Lists modules or entries in a snapshot.\n*   **`set-entry`, `set-entries`, `delete-entry`, `delete-entries`**: Manages entries in a snapshot.\n*   **`install-check-merged`**: Checks for merged books.\n*   **`install-module-update`, `install-module`**: Updates or installs a module.\n*   **`install-book-update`, `install-book`**: Updates or installs a book."

[[:section {:title "hara.lang.base.manage (Language Management Tasks)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-manage-language-management-tasks"}]]

"This sub-namespace provides high-level tasks for managing language definitions, modules, and entries, including overviews, purging, and linting."

"**Key Functions:**"

"*   **`lib-overview-format`, `lib-overview`**: Formats and displays a library overview.\n*   **`lib-module-env`**: Compiles the module task environment.\n*   **`lib-module-filter`**: Filters modules.\n*   **`lib-module-overview-format`, `lib-module-overview`**: Formats and displays a module overview.\n*   **`lib-module-entries-format-section`, `lib-module-entries-format`, `lib-module-entries`**: Formats and displays module entries.\n*   **`lib-module-purge-fn`, `lib-module-purge`**: Purges modules.\n*   **`lib-module-unused-fn`, `lib-module-unused`**: Lists unused modules.\n*   **`lib-module-missing-line-number-fn`, `lib-module-missing-line-number`**: Lists modules with missing line numbers.\n*   **`lib-module-incorrect-alias-fn`, `lib-module-incorrect-alias`**: Lists modules with incorrect aliases."

[[:section {:title "hara.lang.base.pointer (Language Pointers)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-pointer-language-pointers"}]]

"This sub-namespace defines the `Pointer` record, which acts as a reference to a code entry within a specific language runtime. Pointers enable dynamic invocation and introspection of code across different languages."

"**Key Functions:**"

"*   **`with:clip`, `with:print`, `with:print-all`, `with:rt-wrap`, `with:rt`, `with:input`, `with:raw` (macros)**: Control pointer behavior.\n*   **`get-entry`**: Retrieves the library entry for a pointer.\n*   **`ptr-tag`**: Creates a tag for a pointer.\n*   **`ptr-deref`**: Dereferences a pointer.\n*   **`ptr-display`**: Displays a pointer.\n*   **`ptr-invoke-meta`**: Prepares metadata for pointer invocation.\n*   **`rt-macro-opts`**: Creates macro options for a runtime.\n*   **`ptr-invoke-string`**: Emits the invocation string for a pointer.\n*   **`ptr-invoke-script`**: Emits a script for a pointer.\n*   **`ptr-intern`**: Interns a pointer into the workspace.\n*   **`ptr-output-json`, `ptr-output`**: Handles pointer output.\n*   **`ptr-invoke`**: Invokes a pointer."

[[:section {:title "hara.lang.base.registry (Language Registry)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-registry-language-registry"}]]

"This sub-namespace defines a global registry (`+registry+`) that maps language contexts and runtime keys to their corresponding Clojure namespaces, enabling dynamic loading and instantiation of language runtimes."

"**Core Concepts:**"

"*   **`+registry+`**: An atom storing a map of `[lang runtime-key]` to Clojure namespace symbols."

[[:section {:title "hara.lang.base.runtime (Language Runtimes)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-runtime-language-runtimes"}]]

"This sub-namespace defines the `RuntimeDefault` record, which serves as a base implementation for language runtimes. It provides default behaviors for code evaluation, pointer handling, and lifecycle management."

"**Core Concepts:**"

"*   **`RuntimeDefault` Record:** A base runtime implementation that can be extended or proxied.\n*   **`IContext` Protocol:** Implemented by runtimes for raw code evaluation, pointer initialization, tag retrieval, dereferencing, display, and invocation.\n*   **`IComponent` Protocol:** Implemented by runtimes for lifecycle management (start, stop, kill)."

"**Key Functions:**"

"*   **`default-tags-ptr`, `default-deref-ptr`, `default-invoke-ptr`, `default-init-ptr`, `default-display-ptr`, `default-raw-eval`, `default-transform-in-ptr`, `default-transform-out-ptr`**: Default implementations for `IContext` methods.\n*   **`rt-default-string`**: Returns a string representation of a default runtime.\n*   **`rt-default?`, `rt-default`**: Predicate and constructor for `RuntimeDefault` records.\n*   **`install-lang!`, `install-type!`**: Installs language definitions and runtime types.\n*   **`return-format-simple`, `return-format`, `return-wrap-invoke`, `return-transform`**: Functions for formatting return values.\n*   **`default-invoke-script`**: Default script invocation.\n*   **`default-lifecycle-prep`**: Prepares options for lifecycle management.\n*   **`default-scaffold-array`, `default-scaffold-setup-for`, `default-scaffold-setup-to`, `default-scaffold-imports`**: Functions for scaffolding.\n*   **`default-lifecycle-fn`**: Constructs a lifecycle function.\n*   **`default-has-module?`, `default-has-ptr?`, `default-setup-ptr`, `default-teardown-ptr`**: Default lifecycle functions for modules and pointers.\n*   **`default-setup-module-emit`, `default-setup-module-basic`, `default-teardown-module-basic`, `default-setup-module`, `default-teardown-module`**: Default module setup/teardown functions.\n*   **`multistage-invoke`, `multistage-setup-for`, `multistage-setup-to`, `multistage-teardown-for`, `multistage-teardown-at`, `multistage-teardown-to`**: Multistage lifecycle functions."

[[:section {:title "hara.lang.base.runtime-proxy (Runtime Proxy)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-runtime-proxy-runtime-proxy"}]]

"This sub-namespace defines a proxy mechanism for language runtimes, allowing one runtime to delegate its operations to another. This is useful for creating aliases or for adding layers of functionality."

"**Key Functions:**"

"*   **`rt-proxy-string`**: Returns a string representation of a runtime proxy.\n*   **`proxy-get-rt`**: Retrieves the redirected runtime.\n*   **`proxy-raw-eval`, `proxy-init-ptr`, `proxy-tags-ptr`, `proxy-deref-ptr`, `proxy-display-ptr`, `proxy-invoke-ptr`, `proxy-transform-in-ptr`, `proxy-transform-out-ptr`**: Proxy implementations for `IContext` methods.\n*   **`proxy-started?`, `proxy-stopped?`, `proxy-remote?`, `proxy-info`, `proxy-health`**: Proxy implementations for `IComponent` methods."

[[:section {:title "hara.lang.base.script (Scripting and Runtime Control)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-script-scripting-and-runtime-control"}]]

"This sub-namespace provides high-level macros and functions for defining and controlling language scripts and their associated runtimes. It includes mechanisms for installing languages, managing annexes, and executing code in different contexts."

"**Core Concepts:**"

"*   **`script` Macro:** The primary macro for defining a language script, installing its module, and setting up its runtime.\n*   **Annexes:** A mechanism for extending a language with additional runtimes or configurations.\n*   **`!` Macro:** A convenient macro for switching between active annex runtimes and executing code within them."

"**Key Functions:**"

"*   **`install`**: Installs a language book and its runtime.\n*   **`script-ns-import`**: Imports namespaces for a script.\n*   **`script-macro-import`**: Imports macros for a script.\n*   **`script-fn-base`, `script-fn`**: Base functions for script setup.\n*   **`script` (macro)**: Defines a language script.\n*   **`script-test-prep`, `script-test`**: Prepares and runs test scripts.\n*   **`script-` (macro)**: Defines a test script.\n*   **`script-ext`, `script+` (macro)**: Extends a script with additional runtimes (annexes).\n*   **`script-ext-run`**: Executes code in an annex runtime.\n*   **`!` (macro)**: Executes code in a specified annex runtime.\n*   **`annex:start`, `annex:get`, `annex:stop`, `annex:start-all`, `annex:stop-all`, `annex:restart-all`, `annex:list`**: Functions for managing annexes."

[[:section {:title "hara.lang.base.script-annex (Script Annex Management)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-script-annex-script-annex-management"}]]

"This sub-namespace provides the implementation for managing \"annexes,\" which are extensions to language scripts that allow for dynamic runtime switching and configuration."

"**Key Functions:**"

"*   **`rt-annex-string`**: Returns a string representation of a runtime annex.\n*   **`rt-annex?`, `rt-annex:create`**: Predicate and constructor for `RuntimeAnnex` records.\n*   **`annex-current`, `annex-reset`**: Manages the current annex.\n*   **`get-annex`**: Retrieves the current annex.\n*   **`clear-annex`**: Clears all runtimes in an annex.\n*   **`get-annex-library`, `get-annex-book`**: Retrieves the library or book from an annex.\n*   **`add-annex-runtime`, `get-annex-runtime`, `remove-annex-runtime`**: Manages runtimes in an annex.\n*   **`register-annex-tag`, `deregister-annex-tag`**: Registers or deregisters annex tags.\n*   **`start-runtime`**: Starts a runtime in an annex.\n*   **`same-runtime?`**: Checks if two runtimes are the same."

[[:section {:title "hara.lang.base.script-control (Script Runtime Control)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-script-control-script-runtime-control"}]]

"This sub-namespace provides functions for controlling language runtimes, including getting, stopping, and restarting them, as well as executing one-shot evaluations."

"**Key Functions:**"

"*   **`script-rt-get`**: Retrieves a language runtime.\n*   **`script-rt-stop`, `script-rt-restart`**: Stops or restarts a runtime.\n*   **`script-rt-oneshot-eval`, `script-rt-oneshot`**: Executes one-shot evaluations."

[[:section {:title "hara.lang.base.script-def (Script Definition Helpers)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-script-def-script-definition-helpers"}]]

"This sub-namespace provides helper functions for defining script-related templates and macros."

"**Key Functions:**"

"*   **`tmpl-entry`**: Creates templates for various argument types.\n*   **`tmpl-macro`**: Creates templates for macros."

[[:section {:title "hara.lang.base.script-lint (Script Linting)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-script-lint-script-linting"}]]

"This sub-namespace provides a basic linter for language scripts, checking for unused variables, unknown symbols, and other potential issues."

"**Key Functions:**"

"*   **`get-reserved-raw`, `get-reserved`**: Retrieves reserved symbols from a grammar.\n*   **`collect-vars`**: Collects all variables in a form.\n*   **`collect-module-globals`**: Collects global symbols from a module.\n*   **`collect-sym-vars`**: Collects symbols and variables.\n*   **`sym-check-linter`**: Checks the linter.\n*   **`lint-set`, `lint-clear`, `lint-needed?`**: Manages linting settings.\n*   **`lint-entry`**: Lints a single entry."

[[:section {:title "hara.lang.base.script-macro (Script Macro Interning)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-script-macro-script-macro-interning"}]]

"This sub-namespace provides functions for interning language-specific macros and top-level forms into the Clojure environment, making them available for use in scripts."

"**Key Functions:**"

"*   **`body-arglists`**: Extracts arglists from a function body.\n*   **`intern-in`**: Interns a macro.\n*   **`intern-prep`**: Prepares module and form metadata for interning.\n*   **`intern-def$-fn`, `intern-def$`**: Interns `def$` fragments.\n*   **`intern-defmacro-fn`, `intern-defmacro`**: Interns `defmacro` forms.\n*   **`call-thunk`**: Calls a thunk with pointer output control.\n*   **`intern-!-fn`, `intern-!`**: Interns free pointer macros.\n*   **`intern-free-fn`, `intern-free`**: Interns free pointers.\n*   **`intern-top-level-fn`, `intern-top-level`**: Interns top-level functions.\n*   **`intern-macros`**: Interns all macros from a grammar.\n*   **`intern-highlights`**: Interns highlight macros.\n*   **`intern-grammar`**: Interns a grammar's macros into the namespace.\n*   **`intern-defmacro-rt-fn`, `defmacro.!` (macro)**: Defines runtime language macros."

[[:section {:title "hara.lang.base.util (Language Utilities)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-util-language-utilities"}]]

"This sub-namespace provides various utility functions for working with language-related symbols, contexts, and pointers."

"**Key Functions:**"

"*   **`sym-id`, `sym-module`, `sym-pair`, `sym-full`, `sym-default-str`, `sym-default-inverse-str`**: Functions for manipulating symbols.\n*   **`hashvec?`, `doublevec?`**: Predicates for vector types.\n*   **`lang-context`**: Creates a language context keyword.\n*   **`lang-rt-list`, `lang-rt`**: Lists and retrieves language runtimes.\n*   **`lang-rt-default`**: Retrieves the default runtime function.\n*   **`lang-pointer`**: Creates a language pointer."

[[:section {:title "hara.lang.base.workspace (Workspace Management)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-base-workspace-workspace-management"}]]

"This sub-namespace provides functions for managing the language workspace, including emitting modules, printing module contents, and controlling runtime lifecycles."

"**Key Functions:**"

"*   **`rt-resolve`**: Resolves a runtime.\n*   **`sym-entry`, `sym-pointer`**: Retrieves entries or pointers from symbols.\n*   **`module-entries`**: Retrieves module entries.\n*   **`emit-ptr`**: Emits a pointer as a string.\n*   **`ptr-clip`, `ptr-print`**: Copies or prints pointer text.\n*   **`ptr-setup`, `ptr-teardown`**: Sets up or tears down a pointer.\n*   **`ptr-setup-deps`, `ptr-teardown-deps`**: Sets up or tears down pointer dependencies.\n*   **`emit-module`**: Emits an entire module.\n*   **`print-module`**: Prints a module.\n*   **`rt:module`, `rt:module-purge`**: Manages module purging.\n*   **`rt:inner`**: Retrieves the inner client for a shared runtime.\n*   **`rt:restart`**: Restarts a shared runtime.\n*   **`multistage-tmpl`**: Template for multistage functions.\n*   **`intern-macros`**: Interns macros from one namespace to another."

[[:section {:title "hara.lang.interface.type-notify (Notification Server)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-interface-type-notify-notification-server"}]]

"This sub-namespace implements a notification server that allows language runtimes to send messages back to the Clojure host, enabling real-time feedback and event handling."

"**Key Functions:**"

"*   **`has-sink?`, `get-sink`, `clear-sink`**: Manages notification sinks.\n*   **`add-listener`, `remove-listener`**: Manages listeners for sinks.\n*   **`get-oneshot-id`, `remove-oneshot-id`, `clear-oneshot-sinks`**: Manages one-shot notification IDs.\n*   **`process-print`, `process-capture`, `process-message`**: Processes incoming messages.\n*   **`handle-notify-http`, `start-notify-http`, `stop-notify-http`**: Manages HTTP notification server.\n*   **`handle-notify-socket`, `start-notify-socket`, `stop-notify-socket`**: Manages Socket notification server.\n*   **`start-notify`, `stop-notify`**: Starts or stops both notification servers.\n*   **`notify-server-string`**: Returns a string representation of the notification server.\n*   **`NotifyServer` (defimpl record)**: The concrete record type for the notification server.\n*   **`notify-server:create`, `notify-server`**: Constructors for the notification server.\n*   **`default-notify`, `default-notify:reset`**: Manages the default notification server.\n*   **`watch-oneshot`**: Watches for a one-shot notification."

[[:section {:title "hara.lang.interface.type-shared (Shared Runtime Management)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-interface-type-shared-shared-runtime-management"}]]

"This sub-namespace provides mechanisms for managing shared language runtimes, allowing multiple clients to share a single runtime instance."

"**Key Functions:**"

"*   **`get-groups`**: Retrieves all shared groups.\n*   **`get-group-count`, `update-group-count`**: Manages group counts.\n*   **`get-group-instance`, `set-group-instance`, `update-group-instance`**: Manages group instances.\n*   **`restart-group-instance`**: Restarts a group instance.\n*   **`remove-group-instance`**: Removes a group instance.\n*   **`start-shared`, `stop-shared`, `kill-shared`**: Lifecycle functions for shared runtimes.\n*   **`wrap-shared`**: Wraps a function to operate on a shared runtime.\n*   **`rt-shared-string`**: Returns a string representation of a shared runtime.\n*   **`SharedRuntime` (defimpl record)**: The concrete record type for a shared runtime.\n*   **`rt-shared:create`, `rt-shared`**: Constructors for shared runtimes.\n*   **`rt-is-shared?`**: Checks if a runtime is shared.\n*   **`rt-get-inner`**: Retrieves the inner runtime."

[[:section {:title "hara.lang.model. (Language Specifications)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-model-language-specifications"}]]

"These sub-namespaces define the specific grammars and templates for various target languages, including Bash, C, GLSL, JQ, JavaScript, Lua, Python, R, Rust, and Scheme. They extend the base grammar with language-specific syntax, operators, and emission rules."

"**Core Concepts:**"

"*   **Language-Specific Grammars:** Each `spec_<lang>.clj` file defines a `+grammar+` for its respective language, built upon `hara.lang.base.grammar`.\n*   **Language-Specific Templates:** Each `spec_<lang>.clj` file defines a `+template+` for its respective language, customizing emission rules for data structures, blocks, and tokens.\n*   **Cross-Talk (XTalk) Operators:** The `spec_xtalk.clj` and `spec_xtalk/fn_<lang>.clj` files define cross-language operators that provide a common interface for operations that are implemented differently in each target language."

"**Key Functions (examples from `spec_js.clj`, `spec_lua.clj`, `spec_python.clj`, `spec_r.clj`, `spec_rust.clj`, `spec_bash.clj`, `spec_glsl.clj`, `spec_scheme.clj`, `spec_jq.clj`):**"

"*   **`+features+`**: Defines language-specific operators and their emission rules.\n*   **`+template+`**: Customizes the emission template for the language.\n*   **`+grammar+`**: The final grammar for the language.\n*   **`+meta+`**: Language-specific metadata (e.g., module import/export functions).\n*   **`+book+`**: The language book, containing its grammar and meta.\n*   **`+init+`**: Installs the language into the system.\n*   **Language-specific transformation functions (e.g., `js-regex`, `lua-map-key`, `python-defn`, `r-token-boolean`, `rst-typesystem`, `bash-quote-item`)**: Implement custom emission logic for various forms and data types."

[[:section {:title "hara.lang.strict.basestate (Strict Mode State)" :link "merged-plans-slop-summary-std-lang-summary-md-hara-lang-strict-basestate-strict-mode-state"}]]

"This sub-namespace likely provides state management for a \"strict mode\" within the language system, though its contents are minimal in the provided files."

[[:section {:title "Usage Pattern:" :link "merged-plans-slop-summary-std-lang-summary-md-usage-pattern"}]]

"The `hara.lang` module is the cornerstone of the `foundation-base` project, enabling:"

"*   **Polyglot Development:** Write code once in Clojure and deploy it to multiple target languages.\n*   **Unified Tooling:** Use a single set of tools for code generation, testing, and runtime management across different languages.\n*   **Metaprogramming:** Programmatically generate and transform code for various platforms.\n*   **Language Experimentation:** Easily define and experiment with new DSLs and language features.\n*   **Runtime Agnosticism:** Abstract away the complexities of interacting with different execution environments."

"By providing a powerful and extensible framework for language definition and code generation, `hara.lang` empowers developers to build highly flexible and adaptable software systems."
;; END merged documentation: plans/slop/summary/std_lang_summary.md
