## std.text: A Comprehensive Summary

The `std.text` module in `foundation-base` provides functionalities for text processing, specifically focusing on text differencing and full-text indexing. It integrates with external libraries (like `difflib` for Java) and offers tools for comparing text, applying/reverting patches, and creating searchable text indexes with stemming and persistence capabilities.

The module is organized into two main sub-namespaces:

### `std.text.diff`

This namespace provides utilities for computing and applying differences between text sequences, often referred to as "diffing" and "patching." It leverages the `difflib` Java library.

*   **`create-patch [arr]`**: Creates a `difflib.Patch` object from a collection of delta maps.
*   **`object/vector-like Patch`**: Extends `difflib.Patch` to be treated as a vector-like object, allowing conversion to/from data maps.
*   **`object/string-like Delta$TYPE`**: Extends `difflib.Delta$TYPE` to be treated as a string-like object, allowing conversion to/from string representations.
*   **`create-delta [map]`**: Creates a `difflib.Delta` object (e.g., `InsertDelta`, `ChangeDelta`, `DeleteDelta`) from a map representation.
*   **`object/map-like Delta`**: Extends `difflib.Delta` to be treated as a map-like object, allowing conversion to/from data maps.
*   **`create-chunk [map]`**: Creates a `difflib.Chunk` object from a map representation.
*   **`object/map-like Chunk`**: Extends `difflib.Chunk` to be treated as a map-like object, allowing conversion to/from data maps.
*   **`diff [original revised]`**: Computes the differences between two text strings (or sequences of lines) and returns a collection of delta maps.
*   **`patch [original diff]`**: Applies a series of `diff` deltas to an `original` text, returning the `revised` text.
*   **`unpatch [revised diff]`**: Reverts a series of `diff` deltas from a `revised` text, restoring the `original` text.
*   **`->string [deltas]`**: Converts a collection of delta maps into a human-readable string representation, often with ANSI color coding for added/removed lines.
*   **`summary [deltas]`**: Creates a summary of the diffs, counting the number of deletions and insertions.

### `std.text.index`

This namespace provides functionalities for creating and querying a full-text search index, including text stemming and disk persistence. It uses `std.text.index.stemmer` and `std.text.index.porter` for stemming.

*   **`Index` Record**: A Clojure record to hold the index data (a map from stem to a map of keys to weights) and the stemmer function.
*   **`make-index [& [stemmer-func]]`**: Creates a new search index, optionally specifying a custom stemmer function (defaults to Porter stemmer). The index is managed as an agent for concurrent updates.
*   **`add-entry [index stem key weight]`**: Adds an entry to the index for a given stem, associating it with a `key` and `weight`.
*   **`remove-entries [index key stems]`**: Removes entries associated with a `key` for a given set of `stems`.
*   **`index-text [index key data & [weight]]`**: Adds text to the index. It processes text blocks, stems words, calculates frequencies, and updates the index with associated keys and weights.
*   **`unindex-text [index key txt]`**: Removes text associated with a `key` from the index.
*   **`unindex-all [index key]`**: Clears all entries associated with a specific `key` from the index.
*   **`query [index query-string]`**: Queries the index with a `query-string`, returning a map of stems to their associated key-weight maps.
*   **`merge-and [query-results]`**: Merges query results using an "AND" logic, returning a map of IDs to their combined scores (multiplied weights).
*   **`merge-or [query-results]`**: Merges query results using an "OR" logic, returning a map of IDs to their combined scores (summed weights).
*   **`search [index term & [merge-style]]`**: Performs a search on the index for a given `term`, using either "AND" (default) or "OR" merge style, and returns sorted results.
*   **`save-index [index & [filename-or-file]]`**: Saves the index data to a file, optionally specifying the filename.
*   **`load-index [index filename-or-file]`**: Loads index data from a file into the index.

**Overall Importance:**

The `std.text` module is a valuable component for applications requiring text analysis, comparison, and search capabilities within the `foundation-base` project.

*   **Text Differencing:** The `std.text.diff` namespace is crucial for tasks like version control, code review tools, or any application that needs to highlight changes between text documents. Its integration with `difflib` provides robust diffing algorithms, and the `->string` function offers user-friendly output.
*   **Full-Text Search:** The `std.text.index` namespace provides a lightweight yet powerful full-text indexing solution. This is essential for applications that need to search through large volumes of text efficiently, such as documentation systems, knowledge bases, or code search tools. The use of stemming improves search relevance, and agent-based concurrency ensures safe updates.
*   **Foundation for Text-Based Tools:** Both sub-modules provide foundational building blocks for creating more sophisticated text-based tools and features within the `foundation-base` ecosystem, supporting its broader goals of code generation and language processing.

By offering these specialized text processing functionalities, `std.text` enhances the project's ability to manage and interact with textual data effectively.
