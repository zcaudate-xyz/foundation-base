## std.block: A Comprehensive Summary (including submodules)

The `std.block` module is a foundational component of the `foundation-base` ecosystem, providing an Abstract Syntax Tree (AST) or "code-as-data" abstraction for representing Clojure code. It allows for the parsing, manipulation, and structured representation of code as a hierarchical collection of "blocks." This module is critical for metaprogramming, code analysis, and especially for transpilation processes where code needs to be understood and transformed systematically.

### `std.block` (Main Namespace)

This namespace orchestrates the functionality of its submodules, providing a unified interface for working with code blocks. It re-exports key functions from its sub-namespaces, making it a convenient entry point for block manipulation.

**Key Re-exported Functions:**

*   From `std.block.base`: `block?`, `expression?`, `type`, `tag`, `string`, `length`, `width`, `height`, `prefixed`, `suffixed`, `verify`, `value`, `value-string`, `children`, `info`.
*   From `std.block.construct`: `block`, `void`, `space`, `spaces`, `newline`, `newlines`, `tab`, `tabs`, `comment`, `uneval`, `cursor`, `contents`, `container`, `root`.
*   From `std.block.parse`: `parse-string`, `parse-root`.
*   From `std.block.type`: `void?`, `space?`, `linebreak?`, `linespace?`, `eof?`, `comment?`, `token?`, `container?`, `modifier?`.

### `std.block.base` (Core Block Definitions and Protocols)

This sub-namespace defines the fundamental protocols and basic operations that all code blocks adhere to. It establishes the common interface for querying block properties.

**Core Concepts:**

*   **`IBlock` Protocol:** The base protocol for all code blocks, defining methods like `_type`, `_tag`, `_string`, `_length`, `_width`, `_height`, `_prefixed`, `_suffixed`, `_verify`.
*   **`IBlockExpression` Protocol:** For blocks that have an associated Clojure value, defining `_value` and `_value_string`.
*   **`IBlockModifier` Protocol:** For blocks that modify an accumulator (e.g., `#_` for unevaluated forms), defining `_modify`.
*   **`IBlockContainer` Protocol:** For blocks that contain other blocks (e.g., lists, vectors), defining `_children` and `_replace_children`.
*   **Block Types and Tags:** Blocks are categorized by `:type` (e.g., `:void`, `:token`, `:comment`, `:collection`, `:modifier`) and more specific `:tag` (e.g., `:eof`, `:symbol`, `:list`, `:meta`, `:hash-uneval`).
*   **`*container-limits*`:** Defines the start and end delimiters and properties for various container types (e.g., `(`, `)`, `[`, `]`, `{`, `}`).

**Key Functions:**

*   **`block?`**: Checks if an object is an `IBlock`.
*   **`block-*` functions**: Accessors for block properties (`block-type`, `block-tag`, `block-string`, `block-length`, `block-width`, `block-height`, `block-prefixed`, `block-suffixed`, `block-verify`).
*   **`expression?`**: Checks if a block has a value.
*   **`block-value`**: Returns the Clojure value of an expression block.
*   **`block-value-string`**: Returns the `pr-str` representation of the block's value.
*   **`modifier?`**: Checks if a block is a modifier.
*   **`block-modify`**: Applies a modifier's logic.
*   **`container?`**: Checks if a block is a container.
*   **`block-children`**: Returns the child blocks of a container.
*   **`replace-children`**: Replaces the children of a container.
*   **`block-info`**: Returns a map of common block information.

### `std.block.check` (Character and Token Classification)

This sub-namespace provides utilities for classifying characters and Clojure forms, which is essential for the parsing process.

**Core Concepts:**

*   **Character Properties:** Functions to determine if a character is a boundary, whitespace, comma, linebreak, or delimiter.
*   **Form Tags:** Functions to categorize Clojure forms as void, token, or collection types.

**Key Functions:**

*   **`boundary?`, `whitespace?`, `comma?`, `linebreak?`, `delimiter?`, `voidspace?`, `linetab?`, `linespace?`, `voidspace-or-boundary?`**: Predicates for character classification.
*   **`tag`**: Generic function to find a tag based on a set of checks.
*   **`void-tag`, `void?`**: Classifies and checks for void forms (whitespace, newlines).
*   **`token-tag`, `token?`**: Classifies and checks for token forms (numbers, symbols, keywords, strings).
*   **`collection-tag`, `collection?`**: Classifies and checks for collection forms (lists, vectors, maps, sets).
*   **`comment?`**: Checks if a string is a comment.

### `std.block.construct` (Block Construction)

This sub-namespace provides functions for programmatically creating various types of code blocks.

**Core Concepts:**

*   **Direct Block Creation:** Functions to create instances of `VoidBlock`, `CommentBlock`, `TokenBlock`, and `ContainerBlock`.
*   **Special Blocks:** `uneval` (for `#_`) and `cursor` (for `|`) as modifier blocks.

**Key Functions:**

*   **`void`, `space`, `spaces`, `tab`, `tabs`, `newline`, `newlines`**: Create void blocks (whitespace, newlines, tabs).
*   **`comment`**: Creates a comment block.
*   **`string-token`, `token`, `token-from-string`**: Create token blocks for various literal types.
*   **`container`**: Creates a container block (list, vector, map, set, root), handling delimiters and children.
*   **`uneval`**: Creates a modifier block for `#_` (unevaluated forms).
*   **`cursor`**: Creates a modifier block for `|` (used as a cursor in editing contexts).
*   **`construct-collection` (multimethod)**: A multimethod for constructing blocks from Clojure collections (lists, vectors, maps, sets).
*   **`construct-children`**: Helper to convert a sequence of Clojure forms into a sequence of blocks, inserting correct spacing.
*   **`block`**: A generic function to construct a block from a Clojure form (token or collection).
*   **`add-child`**: Adds a child block to a container block.
*   **`empty`**: Creates an empty list container block.
*   **`root`**: Creates a special container block representing the root of a code structure.
*   **`contents`**: Extracts the value representation of a container block's children, ignoring void blocks.

### `std.block.grid` (Code Formatting and Layout)

This sub-namespace provides algorithms for formatting and arranging code blocks into a readable grid-like structure, handling indentation and line breaks.

**Core Concepts:**

*   **Line-based Formatting:** Operations for splitting blocks into lines, removing extra spaces/linebreaks, and adjusting comments.
*   **Indentation Rules:** `grid-rules` defines how different container types and symbols should be indented.
*   **Scope-aware Indentation:** Indentation can be adjusted based on the nesting level and specific rules for forms like `let` or `if`.

**Key Functions:**

*   **`trim-left`, `trim-right`**: Remove void blocks from the ends of sequences.
*   **`split-lines`**: Splits a sequence of blocks into a sequence of lines based on linebreak blocks.
*   **`remove-starting-spaces`**: Removes leading space blocks from lines.
*   **`adjust-comments`**: Ensures comments are followed by newlines.
*   **`remove-extra-linebreaks`**: Collapses multiple consecutive linebreak blocks into a single one.
*   **`grid-scope`**: Calculates indentation scope for child nodes.
*   **`grid-rules`**: Determines indentation and binding rules for a given block type and symbol.
*   **`indent-bind`**: Calculates the number of lines to bind for indentation.
*   **`indent-lines`**: Applies indentation to a sequence of lines based on rules.
*   **`grid`**: The main function for formatting a container block into a grid, using the defined rules for indentation and line breaks.

### `std.block.parse` (Code Parsing)

This sub-namespace defines the core logic for parsing raw input strings into a tree of `std.block` objects. It leverages `clojure.tools.reader` for low-level character reading.

**Core Concepts:**

*   **`tools.reader` Integration:** Uses `clojure.tools.reader.reader-types/IndexingPushbackReader` for character-by-character input processing, allowing for peeking, reading, and unreading characters.
*   **Dispatch Mechanism:** Uses `read-dispatch` and a multimethod `-parse` to determine the type of block to parse based on the leading character.
*   **Recursive Descent Parsing:** Parses complex structures like collections and forms recursively.
*   **Error Handling:** Throws informative exceptions for parsing errors (e.g., unmatched delimiters, unexpected EOF).

**Key Functions:**

*   **`read-dispatch`**: Determines the block type (`:void`, `:hash`, `:list`, `:token`, etc.) based on the next character.
*   **`-parse` (multimethod)**: The main dispatch function for parsing different block types.
    *   **`parse-void`**: Parses whitespace, newlines, commas.
    *   **`parse-comment`**: Parses a comment line.
    *   **`parse-token`**: Parses numbers, symbols, booleans, ratios.
    *   **`parse-keyword`**: Parses keywords (`:key`, `::key`).
    *   **`parse-reader`**: Parses reader forms (e.g., `\c`).
    *   **`read-string-data`**: Parses string literals, handling newlines and escaped characters.
    *   **`parse-collection`**: Parses collections (`()`, `[]`, `{}`, `#{}`).
    *   **`parse-cons`**: Parses forms with prefixes (e.g., `'`, `~`, `@`, `#`).
    *   **`parse-unquote`, `parse-select`, `parse-hash-uneval`, `parse-hash-cursor`, `parse-hash`**: Specific parsers for reader macros and prefixed forms.
*   **`parse-string`**: Parses an entire string into a single block.
*   **`parse-root`**: Parses a string into a root block, representing the entire input structure.
*   **`eof-block?`, `delimiter-block?`**: Check for EOF or delimiter blocks.
*   **`read-whitespace`**: Reads a sequence of whitespace blocks.
*   **`parse-non-expressions`**: Parses blocks up to the next expression.
*   **`read-start`**: Verifies starting delimiters for collections/forms.
*   **`read-collection`**: Reads all child blocks within a collection, respecting delimiters.
*   **`read-cons`**: Reads child blocks for cons-like forms (e.g., `'x`, `@y`).

### `std.block.reader` (Low-Level Character Reading)

This sub-namespace provides an enhanced character-level reader built on top of `clojure.tools.reader`, offering utility functions for detailed input stream manipulation.

**Core Concepts:**

*   **`IndexingPushbackReader`:** Leverages `clojure.tools.reader.reader-types/IndexingPushbackReader` for precise control over the input stream, including tracking line and column numbers.
*   **Character-level Operations:** Functions for peeking, reading, unreading, and skipping characters.
*   **Predicate-based Reading:** Functions to read characters until a predicate is met or while a predicate is true.

**Key Functions:**

*   **`create`**: Creates an `IndexingPushbackReader` from a string.
*   **`reader-position`**: Returns the current line and column of the reader.
*   **`throw-reader`**: Throws an exception with detailed position information.
*   **`step-char`**: Reads a character and advances the reader.
*   **`read-char`**: Reads the next character.
*   **`ignore-char`**: Skips the next character.
*   **`unread-char`**: Pushes a character back onto the reader.
*   **`peek-char`**: Returns the next character without advancing the reader.
*   **`read-while`**: Reads characters as long as a predicate is true.
*   **`read-until`**: Reads characters until a predicate is true.
*   **`read-times`**: Reads characters a specified number of times.
*   **`read-repeatedly`**: Reads characters repeatedly until a stop condition.
*   **`read-include`**: Reads characters while accumulating non-expression blocks.
*   **`slurp`**: Reads the rest of the reader's input as a string.
*   **`read-to-boundary`**: Reads characters until a boundary character is encountered.

### `std.block.type` (Concrete Block Implementations)

This sub-namespace defines the concrete `deftype` implementations for the various block types, adhering to the `std.protocol.block` protocols.

**Core Concepts:**

*   **`VoidBlock`**: Represents whitespace, newlines, tabs, and EOF.
*   **`CommentBlock`**: Represents comments.
*   **`TokenBlock`**: Represents atomic values like numbers, symbols, keywords, strings.
*   **`ContainerBlock`**: Represents collections (lists, vectors, maps, sets, root) and forms with prefixes (e.g., `'x`).
*   **`ModifierBlock`**: Represents reader macro modifiers (e.g., `#_`, `|`).
*   **Dimensions:** Tracks `width` and `height` of blocks for formatting.

**Key Functions:**

*   **`block-compare`**: A utility for comparing two blocks.
*   **`void-block?`, `void-block`**: Checks for and constructs `VoidBlock`.
*   **`space-block?`, `linebreak-block?`, `linespace-block?`, `eof-block?`, `nil-void?`**: Specific predicates for types of void blocks.
*   **`comment-block?`, `comment-block`**: Checks for and constructs `CommentBlock`.
*   **`token-block?`, `token-block`**: Checks for and constructs `TokenBlock`.
*   **`container-width`, `container-height`**: Calculates dimensions for `ContainerBlock`.
*   **`container-string` (multimethod)**: Generates the string representation for different container types.
*   **`container-value-string`**: Generates a string representation of the *value* of a container block (used for `block-value-string`).
*   **`container-block?`, `container-block`**: Checks for and constructs `ContainerBlock`.
*   **`modifier-block?`, `modifier-block`**: Checks for and constructs `ModifierBlock`.

### `std.block.value` (Extracting Values from Blocks)

This sub-namespace focuses on extracting the underlying Clojure value from code blocks, applying any modifiers in the process.

**Core Concepts:**

*   **Value Extraction:** Functions to convert a block representation back into a runnable Clojure value.
*   **Modifier Application:** Correctly applies the logic of modifier blocks (like `#_`) during value extraction.

**Key Functions:**

*   **`apply-modifiers`**: Applies `IBlockModifier` logic to a sequence of blocks.
*   **`child-values`**: Extracts the Clojure values of child blocks within a container, applying modifiers.
*   **`root-value`**, **`list-value`**, **`map-value`**, **`set-value`**, **`vector-value`**: Extract values from container blocks.
*   **`deref-value`**, **`meta-value`**, **`quote-value`**, **`var-value`**, **`hash-keyword-value`**, **`select-value`**, **`select-splice-value`**, **`unquote-value`**, **`unquote-splice-value`**: Extract values from various prefixed forms.
*   **`from-value-string`**: Uses `read-string` on the `block-value-string` to get the value.
*   **`*container-values*`**: A dynamic map mapping container tags to the functions that extract their Clojure values.

### Usage Pattern:

The `std.block` module and its sub-namespaces provide the backbone for:
*   **Transpilers and Compilers:** Representing and manipulating source code during the conversion to other languages.
*   **Code Editors and IDEs:** Providing structured editing, syntax highlighting, and formatting capabilities.
*   **Static Analysis Tools:** Analyzing code structure and properties.
*   **Metaprogramming and Code Generation:** Programmatically constructing and transforming Clojure code.

By offering a powerful and finely-grained abstraction over Clojure code structure, `std.block` enables sophisticated processing and transformation of code within the `foundation-base` ecosystem.