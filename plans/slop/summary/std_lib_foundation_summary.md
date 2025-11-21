## std.lib.foundation: A Comprehensive Summary

The `std.lib.foundation` namespace is a foundational utility library within the `foundation-base` ecosystem, providing a wide array of functions and macros that serve as core building blocks for other modules. It extends, re-implements, or introduces new functionalities for common programming tasks, with a strong emphasis on low-level operations, type checking, meta-programming, and interoperability.

**Key Features and Concepts (Summarizing all public symbols):**

1.  **Core Constants and Basic Utilities:**
    *   `+init+`: A var holding `find-ns`, likely used for initialization or namespace lookup.
    *   `*sep*`: A dynamic var for a separator string, defaulting to "/".
    *   `T`: A function that always returns `true`, regardless of arguments.
    *   `F`: A function that always returns `false`, regardless of arguments.
    *   `NIL`: A function that always returns `nil`, regardless of arguments.
    *   `xor`: A macro for performing an exclusive OR comparison between two boolean values.

2.  **Unique Identifiers and Time:**
    *   `sid`: Generates a short, unique ID string.
    *   `sid-tag`: A memoized function to generate a short ID string based on a tag.
    *   `uuid`: Creates a `java.util.UUID` object from various inputs (random, string, byte array, keyword, or two `Long`s).
    *   `uuid-nil`: Constructs a nil UUID (all zeros).
    *   `instant`: Returns a `java.util.Date` object, either current or from a `Long` timestamp.
    *   `uri`: Creates a `java.net.URI` object from a path string.
    *   `url`: Creates a `java.net.URL` object from a path string.
    *   `date`: Creates a `java.util.Date` object, either current or from a `long` timestamp.
    *   `flake`: Returns a unique, time-incremental ID string using `hara.lib.foundation.Flake`.

3.  **String and Data Manipulation:**
    *   `strn`: Converts various types (bytes, keyword, string) to a string, or `pr-str` for others. Can concatenate multiple arguments.
    *   `lsubs`: Returns a substring from the beginning, excluding `n` characters from the end.
    *   `keyword`: Converts a string or keyword to a keyword.
    *   `string`: Converts a byte array to a string, or `str` for other types.
    *   `concatv`: Concatenates multiple sequences into a single vector.
    *   `edn`: Prints an object to a string and then reads it back, useful for serialization/deserialization testing.

4.  **Combinators:**
    *   `U`: The U combinator, enabling recursive function definitions.
    *   `Z`: The Z combinator, also for recursive function definitions.

5.  **Counters:**
    *   `counter`: Creates a mutable counter object (`hara.lib.foundation.Counter`).
    *   `inc!`: Increments a counter by 1 or a specified amount.
    *   `dec!`: Decrements a counter by 1 or a specified amount.
    *   `reset!`: Resets a counter to a given value.

6.  **Function Invocation and Macros:**
    *   `invoke`: Invokes a `clojure.lang.IFn` with arguments.
    *   `call`: Similar to `invoke`, but reverses the function and first argument, allowing for a threading-like style.
    *   `const`: A macro that evaluates its body at compile time, effectively creating a constant.
    *   `applym`: A macro that allows applying other macros to arguments, similar to `apply` for functions.

7.  **Code Context and Threading Macros:**
    *   `code-ns`: Returns the symbol of the current namespace where the code is located.
    *   `code-line`: Returns the line number of the current code form.
    *   `code-column`: Returns the column number of the current code form.
    *   `thread-form`: A helper function for `->` and `->>` macros.
    *   `->`: A threading macro similar to `clojure.core/->`, but uses `%` as a placeholder for the threaded value, offering more flexibility.
    *   `->>`: A threading macro similar to `clojure.core/->>`, also using `%` as a placeholder.

8.  **Var and Symbol Management:**
    *   `var-sym`: Converts a `clojure.lang.Var` to its fully qualified symbol.
    *   `unbound?`: Checks if a var is currently unbound.
    *   `set!`: A macro to set the value of a var without altering its metadata, providing a controlled way to re-define vars.

9.  **Error Handling and Debugging:**
    *   `suppress`: A macro that executes a body of code and suppresses any `Throwable` exceptions, optionally returning a default value or applying a handler function to the exception.
    *   `with-thrown`: A macro that executes a body and returns any `Throwable` caught, otherwise returns the result of the body.
    *   `with-ex`: A macro that executes a body and returns the `ex-data` of any `Throwable` caught, otherwise returns the result of the body.
    *   `with-retry-fn`: A function that retries a given function `f` a specified `limit` number of times, with a `sleep` interval between retries, until it succeeds or the limit is reached.
    *   `with-retry`: A macro wrapper around `with-retry-fn` for convenient retry logic.
    *   `error`: A macro to throw an `ex-info` with a message and optional data.
    *   `trace`: A macro that returns a `Throwable` containing a stack trace, useful for debugging code paths.
    *   `throwable?`: A predicate to check if an object is an instance of `java.lang.Throwable`.

10. **Type Predicates:**
    *   `byte?`: Checks if an object is a `java.lang.Byte`.
    *   `short?`: Checks if an object is a `java.lang.Short`.
    *   `long?`: Checks if an object is a `java.lang.Long`.
    *   `bigint?`: Checks if an object is a `clojure.lang.BigInt`.
    *   `bigdec?`: Checks if an object is a `java.math.BigDecimal`.
    *   `regexp?`: Checks if an object is a `java.util.regex.Pattern`.
    *   `iobj?`: Checks if an object implements `clojure.lang.IObj`.
    *   `iref?`: Checks if an object implements `clojure.lang.IRef` (e.g., `Atom`, `Ref`, `Agent`).
    *   `ideref?`: Checks if an object implements `clojure.lang.IDeref` (e.g., `volatile!`, `promise`).
    *   `thread?`: Checks if an object is a `java.lang.Thread`.
    *   `url?`: Checks if an object is a `java.net.URL`.
    *   `atom?`: Checks if an object is a `clojure.lang.Atom`.
    *   `comparable?`: Checks if two objects are both `Comparable` and of the same type.
    *   `array?`: Checks if an object is a primitive array, optionally checking its component type.
    *   `edn?`: Checks if an object is a valid EDN (Extensible Data Notation) value.

11. **Parsing and Hashing:**
    *   `parse-long`: Parses a string into a `Long`.
    *   `parse-double`: Parses a string into a `Double`.
    *   `hash-id`: Returns the identity hash code of an object (memory address-based).
    *   `hash-code`: Returns the standard `hashCode` of an object.

12. **Array Access:**
    *   `aget`: A type-safe version of `clojure.core/aget` for primitive arrays (e.g., `longs`, `ints`, `bytes`).

13. **Namespace and Var Interning (Meta-programming):**
    *   `demunge`: Demunges a Java-mangled name back to its original Clojure form.
    *   `re-create`: A memoized function to create a `java.util.regex.Pattern` from a string, escaping special characters.
    *   `intern-var`: Interns a var into a specified namespace, optionally merging metadata.
    *   `intern-form`: A helper function to create the form for `intern-var`.
    *   `intern-in`: A macro to intern specific vars from other namespaces into the current one.
    *   `intern-all`: A macro to intern all public vars from one or more namespaces into the current one.

14. **Templating Macros (Advanced Meta-programming):**
    *   `*template-meta*`: A dynamic var for binding template metadata.
    *   `with:template-meta`: A macro to bind `*template-meta*` for a block of code.
    *   `template-meta`: Returns the currently bound template metadata.
    *   `template-vars`: A macro to generate multiple var definitions using a template function and a list of symbols/arguments.
    *   `template-entries`: A macro to generate entries using a template function and a list of data entries, supporting resolution of symbols and lists.
    *   `template-bulk`: Similar to `template-entries` but optimized for heavy usage, applying `eval` to the template function's output.
    *   `template-ensure`: A helper function to verify that templated entries match the actual interned vars, useful for consistency checks.

15. **Wrapped Objects:**
    *   `Wrapped`: A `deftype` for wrapping values, primarily for display purposes, allowing custom `toString` behavior.
    *   `wrapped`: Creates a `Wrapped` object.
    *   `wrapped?`: Checks if an object is a `Wrapped` instance.

16. **Namespaced Symbol Resolution:**
    *   `resolve-namespaced`: Resolves a namespaced symbol to its fully qualified var symbol.

17. **Multiple Var Definition:**
    *   `def.m`: A macro to define multiple vars from the elements of a sequence returned by a single call.

**Usage and Importance:**

`std.lib.foundation` is a critical component of the `foundation-base` project, serving as its low-level utility belt. Its functions and macros are designed to:

*   **Enhance Clojure Core:** Provide more robust or specialized versions of common Clojure functionalities.
*   **Facilitate Interoperability:** Offer seamless integration with Java types and features.
*   **Support Meta-programming:** Enable powerful code generation, introspection, and transformation, which is essential for the project's transpilation and runtime management goals.
*   **Improve Debugging and Development:** Provide tools for unique ID generation, precise timing, error handling, and code context retrieval.
*   **Ensure Type Safety:** Offer a comprehensive set of type predicates and type-aware operations.

By consolidating these fundamental capabilities, `std.lib.foundation` provides a stable and extensible base upon which the rest of the `foundation-base` ecosystem is built, contributing significantly to its flexibility, performance, and maintainability.