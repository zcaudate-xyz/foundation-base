## std.string: A Comprehensive Summary

The `std.string` module in `foundation-base` provides a rich set of utilities for manipulating, transforming, and analyzing strings in Clojure. It extends core string functionalities, offers various casing conversions, path manipulation, pluralization/singularization, and prose formatting. A key feature is its `wrap` mechanism, which allows these string operations to work seamlessly across different "string-like" types (e.g., keywords, symbols, namespaces, byte arrays) by coercing them to strings, applying the operation, and then coercing them back to their original type.

The module is organized into several sub-namespaces:

### `std.string.case`

This namespace focuses on converting strings between various common casing conventions.

*   **`camel-case [value]`**: Converts to `camelCase` (e.g., "hello-world" -> "helloWorld").
*   **`upper-camel-case [value]`**: Converts to `UpperCamelCase` (e.g., "hello-world" -> "HelloWorld").
*   **`capital-sep-case [value]`**: Converts to `Capital Separated Case` (e.g., "hello world" -> "Hello World").
*   **`lower-sep-case [value]`**: Converts to `lower separated case` (e.g., "helloWorld" -> "hello world").
*   **`pascal-case [value]`**: Converts to `PascalCase` (same as `upper-camel-case`).
*   **`phrase-case [value]`**: Converts to `Phrase case` (e.g., "hello-world" -> "Hello world").
*   **`dot-case [value]`**: Converts to `dot.case` (e.g., "hello-world" -> "hello.world").
*   **`snake-case [value]`**: Converts to `snake_case` (e.g., "hello-world" -> "hello_world").
*   **`spear-case [value]`**: Converts to `spear-case` (e.g., "hello_world" -> "hello-world").
*   **`upper-sep-case [value]`**: Converts to `UPPER SEPARATED CASE` (e.g., "hello world" -> "HELLO WORLD").
*   **`typeless= [x y]`**: Compares two string-like objects for equality, ignoring casing and separators (e.g., "helloWorld" and "hello_world" are `true`).

### `std.string.coerce`

This namespace provides the core mechanisms for coercing various types to and from strings, and for defining how string operations should behave for different types.

*   **`from-string [string type & [opts]]`**: Converts a string to an object of a specified `type` (e.g., `clojure.lang.Symbol`, `clojure.lang.Namespace`).
*   **`to-string [string]`**: Converts an object to its string representation.
*   **`path-separator [type]`**: A multimethod that returns the default path separator for a given type (e.g., `.` for `Namespace`, `/` for `Keyword`).
*   **`str:op [f & [return]]`**: A higher-order function that wraps a string function `f`. It automatically coerces input arguments to strings, applies `f`, and then coerces the result back to the original input type (or `String` if `return` is `true`).
*   **`str:compare [f]`**: A higher-order function that wraps a comparison function `f`. It coerces two string-like inputs to strings before applying `f`.
*   **`protocol.string/IString` extensions**: Extends `nil`, `Object`, `String`, `byte[]`, `char[]`, `Class`, `clojure.lang.Keyword`, `clojure.lang.Symbol`, `clojure.lang.Namespace` to implement `IString` for `to-string` functionality.
*   **`protocol.string/-from-string` extensions**: Extends `String`, `byte[]`, `char[]`, `Class`, `clojure.lang.Keyword`, `clojure.lang.Symbol`, `clojure.lang.Namespace` for `from-string` functionality.
*   **`protocol.string/-path-separator` extensions**: Extends `Class` and `clojure.lang.Namespace` for `path-separator` functionality.

### `std.string.common`

This namespace offers a collection of general-purpose string utilities, including checks, splits, joins, case conversions, and replacements.

*   **`blank? [s]`**: Checks if a string is `nil` or contains only whitespace.
*   **`split [s re]`**: Splits a string by a regular expression into a vector.
*   **`split-lines [s]`**: Splits a string into a vector of lines.
*   **`joinl [coll & [separator]]`**: Joins a collection of strings with an optional separator.
*   **`join [separator coll]`**: Similar to `joinl`, but with arguments reordered for threading.
*   **`upper-case [s]`**: Converts a string to uppercase.
*   **`lower-case [s]`**: Converts a string to lowercase.
*   **`capital-case [s]`**: Capitalizes the first letter of a string and lowercases the rest.
*   **`reverse [s]`**: Reverses a string.
*   **`starts-with? [s substr]`**: Checks if a string starts with a substring.
*   **`ends-with? [s substr]`**: Checks if a string ends with a substring.
*   **`includes? [s substr]`**: Checks if a string contains a substring.
*   **`trim [s]`**: Trims leading and trailing whitespace.
*   **`trim-left [s]`**: Trims leading whitespace.
*   **`trim-right [s]`**: Trims trailing whitespace.
*   **`trim-newlines [s]`**: Trims trailing newlines.
*   **`escape [s cmap]`**: Escapes characters in a string using a character map.
*   **`replace [s match replacement]`**: Replaces occurrences of `match` with `replacement`. `match` can be a character, string, or `Pattern`.
*   **`caseless= [x y]`**: Compares two strings for equality, ignoring case.
*   **`truncate [s limit]`**: Truncates a string to a specified `limit`.
*   **`capitalize [s]`**: Capitalizes the first letter of a string.
*   **`decapitalize [s]`**: Lowercases the first letter of a string.
*   **`replace-all [s match re]`**: Shortcut for `String.replaceAll`.

### `std.string.path`

This namespace provides functions for manipulating string representations of paths, treating them as hierarchical structures.

*   **`path-join [arr & [sep]]`**: Joins a sequence of path elements into a path string using a separator (defaults to `h/*sep*`).
*   **`path-split [s & [sep]]`**: Splits a path string into a sequence of elements using a separator (defaults to `h/*sep*`).
*   **`path-ns-array [s & [sep]]`**: Returns all but the last element of a path as a vector (the "namespace" part).
*   **`path-ns [s & [sep]]`**: Returns the "namespace" part of a path as a joined string.
*   **`path-root [s & [sep]]`**: Returns the first element of a path (the "root").
*   **`path-stem-array [s & [sep]]`**: Returns all but the first element of a path as a vector (the "stem" part).
*   **`path-stem [s & [sep]]`**: Returns the "stem" part of a path as a joined string.
*   **`path-val [s & [sep]]`**: Returns the last element of a path (the "value").
*   **`path-nth [s n & [sep]]`**: Returns the `n`-th element of a path.
*   **`path-sub-array [s start num & [sep]]`**: Returns a sub-array of path elements.
*   **`path-sub [s start num & [sep]]`**: Returns a sub-section of a path as a joined string.
*   **`path-count [s & [sep]]`**: Counts the number of elements in a path.

### `std.string.plural`

This namespace provides functionalities for pluralizing and singularizing English words, handling irregular forms and uncountable nouns.

*   **`*uncountable*`, `*singular-irregular*`, `*plural-irregular*`, `*plural-rules*`, `*singular-rules*`**: Dynamic vars (atoms) holding the rules and exceptions for pluralization/singularization.
*   **`+base-uncountable+`, `+base-irregular+`, `+base-plural+`, `+base-singular+`**: Initial data for the rules.
*   **`uncountable? [word]`**: Checks if a word is in the list of uncountable nouns.
*   **`irregular? [word]`**: Checks if a word is an irregular plural/singular form.
*   **`resolve-rules [rules word]`**: Applies a set of rules to a word to find a matching pattern and replacement.
*   **`singular [s]`**: Converts a word to its singular form.
*   **`plural [s]`**: Converts a word to its plural form.
*   **`*initalized*`**: A var that ensures the base rules are loaded into the dynamic atoms upon namespace loading.

### `std.string.prose`

This namespace offers utilities for formatting and manipulating text as prose, including handling quotes, whitespace, line breaks, and indentation.

*   **`has-quotes? [s]`**: Checks if a string is enclosed in double quotes.
*   **`strip-quotes [s]`**: Removes leading and trailing double quotes from a string.
*   **`whitespace? [s]`**: Checks if a string consists entirely of whitespace.
*   **`escape-dollars [s]`**: Escapes dollar signs in a string for regex purposes.
*   **`escape-newlines [s]`**: Escapes newline characters (`\n`) to `\\n` for printable representation.
*   **`escape-escapes [s]`**: Escapes backslashes that precede letters.
*   **`escape-quotes [s]`**: Escapes double quotes in a string.
*   **`filter-empty-lines [s]`**: Removes empty or whitespace-only lines from a multi-line string.
*   **`single-line [s]`**: Replaces newlines with spaces, converting a multi-line string to a single line.
*   **`join-lines [arr]`**: Joins non-empty elements of an array with newlines.
*   **`spaces [n]`**: Creates a string with `n` spaces.
*   **`write-line [v]`**: Writes a single line based on a data structure, handling nested structures.
*   **`write-lines [lines]`**: Writes a block of strings, joining them with newlines.
*   **`indent [block n & [opts]]`**: Indents a block of text by `n` spaces, optionally with a custom prefix.
*   **`indent-rest [block n & [opts]]`**: Indents all lines of a block except the first.
*   **`multi-line? [s]`**: Checks if a string contains newline characters.
*   **`single-line? [s]`**: Checks if a string does not contain newline characters.
*   **`layout-lines [tokens & [max]]`**: Lays out a sequence of tokens into lines, respecting a maximum line length.

### `std.string.wrap`

This namespace provides the core `wrap` functionality, which is a powerful mechanism for extending string operations to work with various "string-like" types.

*   **`wrap-fn [f]`**: A multimethod that determines the appropriate wrapper function for a given string operation `f`.
    *   `:default` method uses `coerce/str:op` (coerces to string, applies `f`, coerces back).
    *   `:return` method uses `coerce/str:op` with `return` set to `true` (always returns a string).
    *   `:compare` method uses `coerce/str:compare` (coerces both inputs to string, applies `f`).
*   **`+defaults+`**: A map defining which string functions should use which `wrap-fn` method.
*   **`*lookup*`**: A dynamic var (atom) that maps string functions to their corresponding wrapper types (e.g., `:compare`, `:return`, `:default`).
*   **`join [arr & [sep]]`**: An extended version of `common/join` that works with string-like types.
*   **`wrap [f]`**: The main macro that takes a string function `f` and returns a new function that can operate on string-like types. It uses `*lookup*` and `wrap-fn` to determine the correct coercion and wrapping logic.

**Overall Importance:**

The `std.string` module is a fundamental utility within the `foundation-base` project. Its comprehensive set of string manipulation tools, combined with the powerful `wrap` mechanism, enables:

*   **Flexible String Handling:** Operations can be applied uniformly to various string-like data types, reducing boilerplate and improving code readability.
*   **Data Normalization:** Standardized casing and path manipulation functions are crucial for consistent data processing and interoperability with external systems.
*   **Code Generation and Transformation:** The ability to manipulate strings and symbols effectively is vital for meta-programming tasks, such as generating code in different target languages.
*   **Improved Readability and Formatting:** Prose-related functions help in presenting information clearly and consistently.
*   **Extensibility:** The protocol-based coercion and wrapping mechanisms allow for easy extension to new "string-like" types as needed.

By providing these versatile and extensible string utilities, `std.string` significantly contributes to the `foundation-base` project's ability to handle diverse data formats and support its multi-language development ecosystem.
