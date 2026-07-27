(ns documentation.std-lib-foundation
  (:require [std.lib.foundation :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.foundation` provides basic predicates, constructors, and helpers used across the foundation libraries."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Constants and combinators"}]]

"`T`, `F`, and `NIL` are constant functions. `U` and `Z` are classic fixed-point combinators, useful for anonymous recursion."

(fact "constant functions and combinators"
  ^{:refer std.lib.foundation/T :added "3.0"}
  (T 1 2 3)
  => true

  ^{:refer std.lib.foundation/F :added "3.0"}
  (F 1 2 3)
  => false

  ^{:refer std.lib.foundation/Z :added "3.0"}
  (let [factorial (fn [f]
                    (fn [n]
                      (if (zero? n)
                        1
                        (* n (f (dec n))))))]
    ((Z factorial) 5))
  => 120)

[[:section {:title "Identifiers and time"}]]

"Generate short IDs, UUIDs, and timestamps."

(fact "create identifiers and instants"
  ^{:refer std.lib.foundation/sid :added "3.0"}
  (sid)
  => string?

  ^{:refer std.lib.foundation/uuid :added "3.0"}
  (uuid)
  => #(instance? java.util.UUID %)

  ^{:refer std.lib.foundation/instant :added "3.0"}
  (instant 0)
  => #inst "1970-01-01T00:00:00.000-00:00")

[[:section {:title "Coercion helpers"}]]

"`strn`, `keyword`, and `string` coerce values to common types."

(fact "coerce values"
  ^{:refer std.lib.foundation/strn :added "3.0"}
  (strn :hello)
  => "hello"

  ^{:refer std.lib.foundation/keyword :added "3.0"}
  (keyword "hello")
  => :hello

  ^{:refer std.lib.foundation/string :added "3.0"}
  (string (.getBytes "Hello"))
  => "Hello")

[[:chapter {:title "Constants and Combinators" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [T F NIL U Z xor]}]]

[[:chapter {:title "Identifiers" :link "std.lib.foundation"}]]

"`sid`, `uuid`, and `flake` generate short and unique identifiers."

[[:api {:namespace "std.lib.foundation"
        :only [sid uuid uuid-nil flake]}]]

[[:chapter {:title "Constructors" :link "std.lib.foundation"}]]

"Construct common Java and Clojure values."

[[:api {:namespace "std.lib.foundation"
        :only [instant date uri url counter]}]]

[[:chapter {:title "Coercion" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [strn keyword string edn edn?]}]]

[[:chapter {:title "Type Predicates" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [byte? short? long? bigint? bigdec? regexp? iobj? iref? ideref? thread? url? atom? comparable? array?]}]]

[[:chapter {:title "Parsing" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [parse-long parse-double]}]]

[[:chapter {:title "Invocation" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [invoke call]}]]

[[:chapter {:title "Errors" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [error suppress with-ex with-thrown throwable?]}]]

[[:chapter {:title "Utilities" :link "std.lib.foundation"}]]

[[:api {:namespace "std.lib.foundation"
        :only [hash-id hash-code demunge aget var-sym unbound?]}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_foundation_recommendations.md
;; sha256: cf5428ddf23afa14a7cf70230813d746b4b77f6cdc474deeb262981dab5e4f67
[[:chapter {:title "std.lib.foundation Recommendations" :link "merged-plans-slop-summary-std-lib-foundation-recommendations-md"}]]

"`std.lib.foundation` provides the most basic utility functions for the entire `foundation-base` ecosystem. Its goal is to be a lightweight, universal \"prelude\" for all other modules. Here are some recommendations for new functionality that would align with this goal and further enhance the developer experience."

"*   **More String Utilities:**\n    *   **Justification:** String manipulation is a very common task. While `std.string.common` exists, `std.lib.foundation` would benefit from having a few core, frequently used string functions directly available. This would reduce the need for other modules to import `std.string.common`, simplifying dependencies and improving code readability.\n    *   **Recommendations:**\n        *   `trim`: Removes leading and trailing whitespace from a string.\n        *   `split`: Splits a string into a sequence of strings based on a delimiter.\n        *   `join`: Joins a sequence of strings into a single string with a separator.\n        *   `replace`: Replaces all occurrences of a substring or pattern with another string.\n        *   `starts-with?`, `ends-with?`: Checks if a string starts or ends with a given prefix or suffix.\n    *   **Example Usage:**\n        ```clojure\n        (-> \"  hello world  \" h/trim (h/split #\" \") (h/join \"-\"))\n        ;; => \"hello-world\"\n        ```\n\n*   **Enhanced Type Checking:**\n    *   **Justification:** The codebase already has predicates like `string?`, `keyword?`, and `symbol?`. Expanding this set would provide a more complete and consistent way to perform type checks, which is especially useful for validation and multi-method dispatch.\n    *   **Recommendations:**\n        *   `mapp?`: Checks if an object is a map.\n        *   `vector?`: Checks if an object is a vector.\n        *   `set?`: Checks if an object is a set.\n        *   `fn?`: Checks if an object is a function.\n        *   `coll?`: Checks if an object is a collection (list, vector, set, or map).\n    *   **Example Usage:**\n        ```clojure\n        (when (h/mapp? my-data)\n          (h/map-vals process-value my-data))\n        ```\n\n*   **More Math Functions:**\n    *   **Justification:** While the project has a `math` namespace, some very basic math functions are so common that they would be a good fit for `std.lib.foundation`.\n    *   **Recommendations:**\n        *   `abs`: Returns the absolute value of a number.\n        *   `min`, `max`: Returns the minimum or maximum of a set of numbers.\n        *   `round`, `floor`, `ceil`: For rounding numbers.\n    *   **Example Usage:**\n        ```clojure\n        (h/round 3.14159) ;; => 3\n        (h/max 1 5 2 8 3) ;; => 8\n        ```\n\n*   **`identity` function:**\n    *   **Justification:** The `identity` function is a fundamental building block in functional programming. It's often used as a default function or in higher-order functions. Its absence is a noticeable omission.\n    *   **Recommendation:** Add `(defn identity [x] x)`.\n    *   **Example Usage:**\n        ```clojure\n        (get-in my-map [:a :b] :default-value)\n        ;; vs\n        (-> (get-in my-map [:a :b]) (or :default-value))\n        ;; with identity\n        (-> (get-in my-map [:a :b]) (h/call identity) (or :default-value))\n        ```\n\n*   **`constantly` function:**\n    *   **Justification:** `constantly` is another useful higher-order function that creates a function that always returns the same value. It's great for stubbing out functions or for use with functions like `map`.\n    *   **Recommendation:** Add `(defn constantly [x] (fn [& args] x))`.\n    *   **Example Usage:**\n        ```clojure\n        (map (h/constantly 0) (range 5)) ;; => (0 0 0 0 0)\n        ```\n\n*   **`juxt` function:**\n    *   **Justification:** `juxt` is a powerful function for applying multiple functions to the same arguments and collecting the results. It can simplify code and make it more expressive.\n    *   **Recommendation:** Add a `juxt` implementation.\n    *   **Example Usage:**\n        ```clojure\n        (let [stats (h/juxt min max count)]\n          (stats [1 5 2 8 3])) ;; => [1 8 5]\n        ```"
;; END merged documentation: plans/slop/summary/std_lib_foundation_recommendations.md

;; BEGIN merged documentation: plans/slop/summary/std_lib_foundation_summary.md
;; sha256: 1dbcf91075933f41afab031977c8b7b0c21d0e27101af89a2291c3211f55d548
[[:chapter {:title "std.lib.foundation: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-foundation-summary-md"}]]

"The `std.lib.foundation` namespace is a foundational utility library within the `foundation-base` ecosystem, providing a wide array of functions and macros that serve as core building blocks for other modules. It extends, re-implements, or introduces new functionalities for common programming tasks, with a strong emphasis on low-level operations, type checking, meta-programming, and interoperability."

"**Key Features and Concepts (Summarizing all public symbols):**"

"1.  **Core Constants and Basic Utilities:**\n    *   `+init+`: A var holding `find-ns`, likely used for initialization or namespace lookup.\n    *   `*sep*`: A dynamic var for a separator string, defaulting to \"/\".\n    *   `T`: A function that always returns `true`, regardless of arguments.\n    *   `F`: A function that always returns `false`, regardless of arguments.\n    *   `NIL`: A function that always returns `nil`, regardless of arguments.\n    *   `xor`: A macro for performing an exclusive OR comparison between two boolean values.\n\n2.  **Unique Identifiers and Time:**\n    *   `sid`: Generates a short, unique ID string.\n    *   `sid-tag`: A memoized function to generate a short ID string based on a tag.\n    *   `uuid`: Creates a `java.util.UUID` object from various inputs (random, string, byte array, keyword, or two `Long`s).\n    *   `uuid-nil`: Constructs a nil UUID (all zeros).\n    *   `instant`: Returns a `java.util.Date` object, either current or from a `Long` timestamp.\n    *   `uri`: Creates a `java.net.URI` object from a path string.\n    *   `url`: Creates a `java.net.URL` object from a path string.\n    *   `date`: Creates a `java.util.Date` object, either current or from a `long` timestamp.\n    *   `flake`: Returns a unique, time-incremental ID string using `hara.lib.foundation.Flake`.\n\n3.  **String and Data Manipulation:**\n    *   `strn`: Converts various types (bytes, keyword, string) to a string, or `pr-str` for others. Can concatenate multiple arguments.\n    *   `lsubs`: Returns a substring from the beginning, excluding `n` characters from the end.\n    *   `keyword`: Converts a string or keyword to a keyword.\n    *   `string`: Converts a byte array to a string, or `str` for other types.\n    *   `concatv`: Concatenates multiple sequences into a single vector.\n    *   `edn`: Prints an object to a string and then reads it back, useful for serialization/deserialization testing.\n\n4.  **Combinators:**\n    *   `U`: The U combinator, enabling recursive function definitions.\n    *   `Z`: The Z combinator, also for recursive function definitions.\n\n5.  **Counters:**\n    *   `counter`: Creates a mutable counter object (`hara.lib.foundation.Counter`).\n    *   `inc!`: Increments a counter by 1 or a specified amount.\n    *   `dec!`: Decrements a counter by 1 or a specified amount.\n    *   `reset!`: Resets a counter to a given value.\n\n6.  **Function Invocation and Macros:**\n    *   `invoke`: Invokes a `clojure.lang.IFn` with arguments.\n    *   `call`: Similar to `invoke`, but reverses the function and first argument, allowing for a threading-like style.\n    *   `const`: A macro that evaluates its body at compile time, effectively creating a constant.\n    *   `applym`: A macro that allows applying other macros to arguments, similar to `apply` for functions.\n\n7.  **Code Context and Threading Macros:**\n    *   `code-ns`: Returns the symbol of the current namespace where the code is located.\n    *   `code-line`: Returns the line number of the current code form.\n    *   `code-column`: Returns the column number of the current code form.\n    *   `thread-form`: A helper function for `->` and `->>` macros.\n    *   `->`: A threading macro similar to `clojure.core/->`, but uses `%` as a placeholder for the threaded value, offering more flexibility.\n    *   `->>`: A threading macro similar to `clojure.core/->>`, also using `%` as a placeholder.\n\n8.  **Var and Symbol Management:**\n    *   `var-sym`: Converts a `clojure.lang.Var` to its fully qualified symbol.\n    *   `unbound?`: Checks if a var is currently unbound.\n    *   `set!`: A macro to set the value of a var without altering its metadata, providing a controlled way to re-define vars.\n\n9.  **Error Handling and Debugging:**\n    *   `suppress`: A macro that executes a body of code and suppresses any `Throwable` exceptions, optionally returning a default value or applying a handler function to the exception.\n    *   `with-thrown`: A macro that executes a body and returns any `Throwable` caught, otherwise returns the result of the body.\n    *   `with-ex`: A macro that executes a body and returns the `ex-data` of any `Throwable` caught, otherwise returns the result of the body.\n    *   `with-retry-fn`: A function that retries a given function `f` a specified `limit` number of times, with a `sleep` interval between retries, until it succeeds or the limit is reached.\n    *   `with-retry`: A macro wrapper around `with-retry-fn` for convenient retry logic.\n    *   `error`: A macro to throw an `ex-info` with a message and optional data.\n    *   `trace`: A macro that returns a `Throwable` containing a stack trace, useful for debugging code paths.\n    *   `throwable?`: A predicate to check if an object is an instance of `java.lang.Throwable`.\n\n10. **Type Predicates:**\n    *   `byte?`: Checks if an object is a `java.lang.Byte`.\n    *   `short?`: Checks if an object is a `java.lang.Short`.\n    *   `long?`: Checks if an object is a `java.lang.Long`.\n    *   `bigint?`: Checks if an object is a `clojure.lang.BigInt`.\n    *   `bigdec?`: Checks if an object is a `java.math.BigDecimal`.\n    *   `regexp?`: Checks if an object is a `java.util.regex.Pattern`.\n    *   `iobj?`: Checks if an object implements `clojure.lang.IObj`.\n    *   `iref?`: Checks if an object implements `clojure.lang.IRef` (e.g., `Atom`, `Ref`, `Agent`).\n    *   `ideref?`: Checks if an object implements `clojure.lang.IDeref` (e.g., `volatile!`, `promise`).\n    *   `thread?`: Checks if an object is a `java.lang.Thread`.\n    *   `url?`: Checks if an object is a `java.net.URL`.\n    *   `atom?`: Checks if an object is a `clojure.lang.Atom`.\n    *   `comparable?`: Checks if two objects are both `Comparable` and of the same type.\n    *   `array?`: Checks if an object is a primitive array, optionally checking its component type.\n    *   `edn?`: Checks if an object is a valid EDN (Extensible Data Notation) value.\n\n11. **Parsing and Hashing:**\n    *   `parse-long`: Parses a string into a `Long`.\n    *   `parse-double`: Parses a string into a `Double`.\n    *   `hash-id`: Returns the identity hash code of an object (memory address-based).\n    *   `hash-code`: Returns the standard `hashCode` of an object.\n\n12. **Array Access:**\n    *   `aget`: A type-safe version of `clojure.core/aget` for primitive arrays (e.g., `longs`, `ints`, `bytes`).\n\n13. **Namespace and Var Interning (Meta-programming):**\n    *   `demunge`: Demunges a Java-mangled name back to its original Clojure form.\n    *   `re-create`: A memoized function to create a `java.util.regex.Pattern` from a string, escaping special characters.\n    *   `intern-var`: Interns a var into a specified namespace, optionally merging metadata.\n    *   `intern-form`: A helper function to create the form for `intern-var`.\n    *   `intern-in`: A macro to intern specific vars from other namespaces into the current one.\n    *   `intern-all`: A macro to intern all public vars from one or more namespaces into the current one.\n\n14. **Templating Macros (Advanced Meta-programming):**\n    *   `*template-meta*`: A dynamic var for binding template metadata.\n    *   `with:template-meta`: A macro to bind `*template-meta*` for a block of code.\n    *   `template-meta`: Returns the currently bound template metadata.\n    *   `template-vars`: A macro to generate multiple var definitions using a template function and a list of symbols/arguments.\n    *   `template-entries`: A macro to generate entries using a template function and a list of data entries, supporting resolution of symbols and lists.\n    *   `template-bulk`: Similar to `template-entries` but optimized for heavy usage, applying `eval` to the template function's output.\n    *   `template-ensure`: A helper function to verify that templated entries match the actual interned vars, useful for consistency checks.\n\n15. **Wrapped Objects:**\n    *   `Wrapped`: A `deftype` for wrapping values, primarily for display purposes, allowing custom `toString` behavior.\n    *   `wrapped`: Creates a `Wrapped` object.\n    *   `wrapped?`: Checks if an object is a `Wrapped` instance.\n\n16. **Namespaced Symbol Resolution:**\n    *   `resolve-namespaced`: Resolves a namespaced symbol to its fully qualified var symbol.\n\n17. **Multiple Var Definition:**\n    *   `def.m`: A macro to define multiple vars from the elements of a sequence returned by a single call."

"**Usage and Importance:**"

"`std.lib.foundation` is a critical component of the `foundation-base` project, serving as its low-level utility belt. Its functions and macros are designed to:"

"*   **Enhance Clojure Core:** Provide more robust or specialized versions of common Clojure functionalities.\n*   **Facilitate Interoperability:** Offer seamless integration with Java types and features.\n*   **Support Meta-programming:** Enable powerful code generation, introspection, and transformation, which is essential for the project's transpilation and runtime management goals.\n*   **Improve Debugging and Development:** Provide tools for unique ID generation, precise timing, error handling, and code context retrieval.\n*   **Ensure Type Safety:** Offer a comprehensive set of type predicates and type-aware operations."

"By consolidating these fundamental capabilities, `std.lib.foundation` provides a stable and extensible base upon which the rest of the `foundation-base` ecosystem is built, contributing significantly to its flexibility, performance, and maintainability."
;; END merged documentation: plans/slop/summary/std_lib_foundation_summary.md
