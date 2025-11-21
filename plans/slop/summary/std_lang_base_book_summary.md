# `std.lang.base.book` and `std.lang.base.library` Summary

The `std.lang.base.book*` and `std.lang.base.library*` namespaces define the data structures for storing, managing, and accessing code within the `foundation-base` ecosystem. A "library" is a collection of "books," each of which represents a language and contains the code and grammar for that language.

**Core Concepts:**

*   **Library (`std.lang.base.library`):** A `Library` is the top-level container for all language definitions. It holds a "snapshot" of the current state of all books.
*   **Snapshot (`std.lang.base.library-snapshot`):** A `Snapshot` is an immutable map of language IDs to book definitions. It represents a specific version of the library's content.
*   **Book (`std.lang.base.book`):** A `Book` holds all the code for a particular language. It contains a map of modules, a grammar, and metadata about the language.
*   **Module (`std.lang.base.book-module`):** A `Module` represents a single source file or a collection of related code. It contains a map of code entries, as well as information about the module's dependencies, exports, and other properties.
*   **Entry (`std.lang.base.book-entry`):** An `Entry` represents a single piece of code, such as a function, a variable, or a macro. It contains the code itself (as a Clojure form), as well as metadata about the code.

**How Forms are Stored as Code:**

The `BookEntry` record is the key to understanding how forms are stored as code. Here's a breakdown of its most important fields:

*   `:form`: This field holds the actual Clojure form that represents the code. This is the raw, unevaluated code that will be processed by the emit pipeline.
*   `:form-input`: This field holds the "input form," which is the result of the first stage of preprocessing (`to-input`).
*   `:deps`: This field holds a set of symbols that represent the dependencies of the code. These dependencies are resolved during the `to-staging` phase of preprocessing.
*   `:namespace`: This field holds the namespace of the code.
*   `:template` and `:standalone`: These fields are used for macros and templates.

**The Library and Book Data Structures:**

*   **`Library`:** A record with an `:instance` field that holds an atom containing the current `Snapshot`.
*   **`Snapshot`:** An immutable map where keys are language keywords (e.g., `:lua`, `:js`) and values are maps containing a `:book` key.
*   **`Book`:** A record with the following fields:
    *   `:lang`: The language of the book.
    *   `:meta`: A `BookMeta` record with language-specific metadata.
    *   `:grammar`: The grammar for the language.
    *   `:modules`: A map of `BookModule` records.
*   **`BookModule`:** A record with fields for `:code`, `:fragment` (macros), `:alias`, `:link`, `:export`, etc.
*   **`BookEntry`:** A record with fields for `:form`, `:deps`, `:namespace`, etc.

**Interaction with the Emit Pipeline:**

The `library` and `book` are essential for the emit pipeline.

*   The `emit` function takes a `library` (or a `snapshot`) as an argument, which it uses to get the `book` for the target language.
*   The `book` provides the `grammar` that is used to emit the code.
*   The `modules` in the `book` are used to resolve dependencies and to find the code for other functions and macros that are used in the form being emitted.

**Example: Creating and Using a Library**

```clojure
(require '[std.lang.base.library :as lib])
(require '[std.lang.base.book :as book])
(require '[std.lang.base.book-module :as module])
(require '[std.lang.base.book-entry :as entry])

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
```

This example shows how to create a library, add a book to it, and then use the library to emit code. The library provides the necessary context (grammar and modules) for the emit pipeline to do its work.

By providing a structured way to store and manage code, the `std.lang.base.book*` and `std.lang.base.library*` namespaces play a crucial role in the `foundation-base` transpiler.