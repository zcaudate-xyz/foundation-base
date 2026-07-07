(ns documentation.std-lib-class
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.class` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "API" :link "std.lib.class"}]]

[[:api {:namespace "std.lib.class"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_class_summary.md
;; sha256: ed68349d4d36ed4a8c878a07a3c28db93128919ba1e5943d6581032b12a93cdb
[[:chapter {:title "std.lib.class: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-class-summary-md"}]]

"The `std.lib.class` namespace provides a collection of utility functions for introspecting and manipulating Java classes and their hierarchies within Clojure. It offers tools to query class properties, determine inheritance relationships, and work with primitive types and arrays. This module is fundamental for tasks involving reflection, type checking, and dynamic class analysis."

[[:section {:title "Core Concepts:" :link "merged-plans-slop-summary-std-lib-class-summary-md-core-concepts"}]]

"*   **Class Introspection:** Functions to examine various characteristics of a `java.lang.Class` object, such as whether it's an array, a primitive, an interface, or an abstract class.\n*   **Type Hierarchy Traversal:** Utilities to list ancestors (superclasses and interfaces) of a given class, providing a comprehensive view of its type hierarchy.\n*   **Primitive Type Mapping:** A robust system for mapping between different representations of Java primitive types (e.g., keyword, symbol, `Class` object, internal JVM raw type string).\n*   **Class Matching:** A function to find the \"best match\" for a given class from a set of candidate classes or interfaces, useful for type resolution."

[[:section {:title "Key Functions:" :link "merged-plans-slop-summary-std-lib-class-summary-md-key-functions"}]]

"*   **`class:array?`**:\n    *   **Purpose:** Checks if a given `Class` object represents an array type.\n    *   **Usage:** `(class:array? (type (int-array 0)))`\n*   **`primitive?`**:\n    *   **Purpose:** Checks if a given `Class` object represents a primitive Java type (e.g., `int`, `boolean`).\n    *   **Usage:** `(primitive? Integer/TYPE)`\n*   **`primitive:array?`**:\n    *   **Purpose:** Checks if a given `Class` object represents an array of primitive types.\n    *   **Usage:** `(primitive:array? (type (int-array 0)))`\n*   **`class:array-component`**:\n    *   **Purpose:** Returns the `Class` object representing the component type of an array class.\n    *   **Usage:** `(class:array-component (type (int-array 0)))`\n*   **`class:interface?`**:\n    *   **Purpose:** Checks if a given `Class` object represents an interface.\n    *   **Usage:** `(class:interface? java.util.Map)`\n*   **`class:abstract?`**:\n    *   **Purpose:** Checks if a given `Class` object represents an abstract class.\n    *   **Usage:** `(class:abstract? java.util.AbstractMap)`\n*   **`primitive`**:\n    *   **Purpose:** A powerful lookup function that converts between various representations of primitive types. It can take a primitive's keyword, symbol, raw JVM string, or `Class` object and return its corresponding `:raw`, `:symbol`, `:string`, `:class`, or `:container` representation.\n    *   **Usage:** `(primitive Boolean/TYPE :symbol)`, `(primitive \"Z\" :class)`\n*   **`create-lookup`**:\n    *   **Purpose:** A helper function used internally to build efficient lookup maps for primitive type conversions.\n*   **`ancestor:list`**:\n    *   **Purpose:** Returns a list of all direct superclasses (ancestors) of a given class, starting from the class itself up to `java.lang.Object`.\n    *   **Usage:** `(ancestor:list clojure.lang.PersistentHashMap)`\n*   **`class:interfaces`**:\n    *   **Purpose:** Returns a set of all interfaces implemented by a given class, including interfaces inherited from superclasses.\n    *   **Usage:** `(class:interfaces clojure.lang.AFn)`\n*   **`ancestor:tree`**:\n    *   **Purpose:** Returns a detailed hierarchy of a class, including its superclasses and the interfaces implemented at each level.\n    *   **Usage:** `(ancestor:tree Class)`\n*   **`ancestor:all`**:\n    *   **Purpose:** Returns a set of all classes and interfaces that a given class inherits from or implements, including the class itself.\n    *   **Usage:** `(ancestor:all String)`\n*   **`class:inherits?`**:\n    *   **Purpose:** Checks if one class (the `cls`) inherits from or implements another class or interface (the `ancestor`).\n    *   **Usage:** `(class:inherits? clojure.lang.ILookup clojure.lang.APersistentMap)`\n*   **`class:match`**:\n    *   **Purpose:** Given a set of candidate classes/interfaces and a target class, it finds the \"best\" matching candidate in the target class's hierarchy. \"Best\" typically means the most specific common ancestor.\n    *   **Usage:** `(class:match #{Object Number} Long)`"

[[:section {:title "Usage Pattern:" :link "merged-plans-slop-summary-std-lib-class-summary-md-usage-pattern"}]]

"This namespace is crucial for:"

"*   **Dynamic Type Checking:** Determining the exact nature of an object's class at runtime.\n*   **Reflection-based Operations:** Building tools that need to understand and navigate class hierarchies.\n*   **Code Generation/Metaprogramming:** When generating code that interacts with specific Java types or needs to ensure type compatibility.\n*   **Library Development:** Providing robust type-related utilities for other parts of the `foundation-base` ecosystem."

"By centralizing these class-related utilities, `std.lib.class` promotes consistent and reliable handling of Java types within Clojure applications."
;; END merged documentation: plans/slop/summary/std_lib_class_summary.md
