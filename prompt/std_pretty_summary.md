## std.pretty: A Comprehensive Summary (including submodules)

The `std.pretty` module provides a powerful and extensible pretty-printing framework for Clojure, designed to render complex data structures in a human-readable and customizable format. It supports features like intelligent line wrapping, syntax highlighting with ANSI colors, and custom handlers for various data types. This module is crucial for debugging, logging, and generating formatted output in Clojure applications.

### `std.pretty` (Main Namespace)

This namespace serves as the primary entry point for pretty-printing, aggregating and re-exporting key functionalities from its submodules. It defines the core pretty-printing options and provides high-level functions for rendering data.

**Core Concepts:**

*   **`+defaults+`**: A map defining default pretty-printing options, including width, key sorting, collection delimiters, color settings, and fallback behaviors.
*   **`CanonicalPrinter`**: A printer implementation that produces a canonical, uncolored representation of data.
*   **`PrettyPrinter`**: A printer implementation that applies all pretty-printing options, including coloring and custom handlers.

**Key Functions:**

*   **`format-unknown`**: Formats unknown data types.
*   **`format-doc-edn`**: Formats EDN-like data.
*   **`format-doc`**: The main function for formatting data into a document.
*   **`pr-handler`**: A print handler for printing strings.
*   **`unknown-handler`**: A handler for unknown objects.
*   **`tagged-handler`**: A handler for tagged literals.
*   **`java-handlers`, `clojure-handlers`, `clojure-interface-handlers`, `common-handlers`**: Collections of predefined print handlers for Java and Clojure types.
*   **`canonical-printer`**: Creates a canonical printer.
*   **`pretty-printer`**: Creates a pretty printer.
*   **`render-out`**: Renders a document to an output stream.
*   **`pprint`**: Pretty-prints a value to `*out*`.
*   **`pprint-str`**: Returns the pretty-printed string.
*   **`pprint-cc`**: Pretty-prints using the `std.concurrent.print` framework.

### `std.pretty.color` (Coloring Utilities)

This sub-namespace provides utilities for applying color and other markup to text, supporting ANSI escape codes, inline HTML styles, and HTML classes.

**Key Functions:**

*   **`-document` (multimethod)**: Constructs a pretty-print document with optional coloring.
*   **`-text` (multimethod)**: Produces colored text.

### `std.pretty.compare` (Comparison Utilities)

This sub-namespace provides a custom comparison function for various Clojure data types, used for sorting keys in maps and sets during pretty-printing.

**Key Functions:**

*   **`type-priority`**: Assigns a priority to different data types for comparison.
*   **`compare-seqs`**: Compares two sequences.
*   **`compare`**: Compares any two values, handling different types and collections.

### `std.pretty.deque` (Deque Implementation)

This sub-namespace provides a simple deque (double-ended queue) implementation using `clojure.core.rrb-vector`, used internally by the pretty-printing engine.

**Key Functions:**

*   **`pop-left`, `peek-left`**: Removes or peeks at the leftmost element.
*   **`conj-right`, `conj-left`, `conj-both`**: Adds elements to either end.
*   **`update-left`, `update-right`**: Updates elements at either end.

### `std.pretty.dispatch` (Dispatch Mechanisms)

This sub-namespace provides mechanisms for dispatching print handlers based on class inheritance, allowing for flexible and extensible type-based printing.

**Key Functions:**

*   **`chained-lookup`**: Chains multiple lookup functions together.
*   **`inheritance-lookup`**: Creates a lookup function that considers class inheritance.

### `std.pretty.edn` (EDN Printing)

This sub-namespace provides the core logic for visiting and formatting EDN (Extensible Data Notation) data structures, including primitive types, collections, and tagged literals.

**Key Functions:**

*   **`override?`**: Checks if an object implements `protocol.pretty/IOverride`.
*   **`edn`**: Converts an object to an EDN representation.
*   **`class->edn`**: Converts a `Class` object to its EDN representation.
*   **`tagged-object`**: Converts an object to a tagged literal.
*   **`format-date`**: Formats a date object.
*   **`visit-seq`, `visit-tagged`, `visit-unknown`, `visit-meta`, `visit-edn`, `visit`**: Functions for visiting and formatting different EDN data types.

### `std.pretty.engine` (Pretty-Printing Engine)

This sub-namespace implements the core pretty-printing algorithm, which converts a document (a hierarchical data structure representing the formatted output) into a flat sequence of operations, and then renders these operations to a string.

**Key Functions:**

*   **`serialize`**: Converts a document into a sequence of operations.
*   **`serialize-node-text`, `serialize-node-pass`, `serialize-node-escaped`, `serialize-node-span`, `serialize-node-line`, `serialize-node-break`, `serialize-node-group`, `serialize-node-nest`, `serialize-node-align`**: Functions for serializing different types of document nodes.
*   **`annotate-right`**: Annotates nodes with their rightmost position.
*   **`annotate-begin`**: Recalculates the rightmost position of `begin` nodes.
*   **`format-nodes`**: Formats nodes given a line width.
*   **`pprint-document`**: Pretty-prints a document to an output stream.

### `std.pretty.protocol` (Pretty-Printing Protocols)

This sub-namespace defines the core protocols and multimethods that enable extensible pretty-printing, allowing custom handlers for various data types and output formats.

**Key Protocols:**

*   **`IEdn`**: Defines the `-edn` method for converting an object to an EDN representation.
*   **`IOverride`**: A marker protocol for types that override default printing behavior.
*   **`IVisitor`**: Defines methods for visiting and formatting different EDN data types.

**Key Multimethods:**

*   **`-serialize-node`**: Serializes a document node.
*   **`-document`**: Constructs a pretty-print document.
*   **`-text`**: Produces colored text.

### Usage Pattern:

The `std.pretty` module is essential for:
*   **Debugging:** Making complex data structures easier to understand in logs and console output.
*   **User Interfaces:** Generating formatted text for display in terminal-based or HTML-based applications.
*   **Code Generation:** Producing human-readable code output.
*   **Documentation:** Formatting code examples and data structures.

By providing a flexible and powerful pretty-printing solution, `std.pretty` enhances the developer experience and improves the readability of Clojure applications.