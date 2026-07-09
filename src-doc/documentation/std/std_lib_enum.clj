(ns documentation.std-lib-enum
  (:require [std.lib.enum :refer :all])
  (:use code.test)
  (:import java.lang.annotation.ElementType))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.enum` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Inspecting Java enums"}]]

"`std.lib.enum` makes it easy to work with Java enums from Clojure. Check if a class is an enum, list its values, and convert between strings, keywords, and enum instances."

(fact "check and list enum values"
  ^{:refer std.lib.enum/enum? :added "3.0"}
  (enum? java.lang.annotation.ElementType)
  => true

  ^{:refer std.lib.enum/enum-values :added "3.0"}
  (->> (enum-values ElementType)
       (map str)
       (set))
  => (contains #{"TYPE" "FIELD" "METHOD" "PARAMETER" "CONSTRUCTOR"}))

(fact "convert to and from enum instances"
  ^{:refer std.lib.enum/create-enum :added "3.0"}
  (create-enum "TYPE" ElementType)
  => ElementType/TYPE

  ^{:refer std.lib.enum/to-enum :added "3.0"}
  (to-enum :field ElementType)
  => ElementType/FIELD

  ^{:refer std.lib.enum/enum-map :added "3.0"}
  (enum-map ElementType)
  => (satisfies [:annotation-type
                 :constructor
                 :field
                 :local-variable
                 :method
                 :module
                 :package
                 :parameter
                 :record-component
                 :type
                 :type-parameter
                 :type-use]
                (comp vec sort keys)))

[[:chapter {:title "API" :link "std.lib.enum"}]]

[[:api {:namespace "std.lib.enum"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_enum_summary.md
;; sha256: c2baa11bcb1ede442bd70ae3fad9253953eb97231742b0128add597d00b0b75f
[[:chapter {:title "std.lib.enum: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-enum-summary-md"}]]

"The `std.lib.enum` namespace provides a set of utility functions for working with Java enum types in Clojure. It simplifies common tasks such as checking if a class is an enum, retrieving its values, converting between string/keyword representations and enum instances, and generating maps of enum values. This module is particularly useful for enhancing interoperability with Java APIs that extensively use enums."

"**Key Features and Concepts:**"

"1.  **Enum Type Checking:**\n    *   **`enum? [type]`**: A predicate that returns `true` if the given `type` (a `java.lang.Class`) is a Java enum, and `false` otherwise. It checks if the type's ancestor list includes `java.lang.Enum`.\n\n2.  **Enum Value Retrieval:**\n    *   **`enum-values [type]`**: Returns a sequence of all enum instances for a given enum `type`. It achieves this by reflectively invoking the static `values()` method available on all Java enum classes.\n\n3.  **Enum Instance Creation:**\n    *   **`create-enum [s type]`**: Creates an enum instance of the specified `type` from its string representation `s`. It iterates through the enum's values to find a match, throwing an exception if no match is found.\n\n4.  **Enum Mapping and Conversion:**\n    *   **`enum-map [type]`**: A memoized function that returns a map where keys are kebab-cased keywords (derived from the enum's string name) and values are the corresponding enum instances. This provides a convenient way to look up enum values using Clojure keywords.\n    *   **`enum-map-form [type]`**: A helper function that generates the Clojure form for `enum-map`, useful for macro expansion.\n    *   **`enum-map> [type]`**: A macro that expands to the result of `enum-map-form`, allowing enum maps to be generated at compile time.\n    *   **`to-enum [s type]`**: Converts a string or keyword `s` into an enum instance of the specified `type`. It uses `enum-map` for efficient lookup, handling both string and keyword inputs by converting them to kebab-cased keywords."

"**Usage and Importance:**"

"The `std.lib.enum` module is crucial for applications within the `foundation-base` project that need to interact with Java APIs that utilize enums. It provides:"

"*   **Simplified Interoperability:** Bridges the gap between Clojure's dynamic nature and Java's static enum types, making it easier to work with Java enums in a Clojure-idiomatic way.\n*   **Type Safety and Validation:** Functions like `create-enum` and `to-enum` ensure that attempts to create enum instances from invalid strings or keywords are caught, preventing runtime errors.\n*   **Readability and Convenience:** The `enum-map` and `enum-map>` functions provide a convenient keyword-based lookup mechanism, improving code readability and reducing boilerplate when dealing with enum values.\n*   **Performance:** The use of memoization in `enum-map` ensures that the mapping of enum values is computed only once, improving performance for repeated lookups."

"By offering these specialized utilities for Java enums, `std.lib.enum` enhances the `foundation-base` project's ability to seamlessly integrate with and leverage the broader Java ecosystem."
;; END merged documentation: plans/slop/summary/std_lib_enum_summary.md
