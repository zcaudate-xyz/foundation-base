# Critique of `code.doc`

## 1. Executive Summary

`code.doc` is a highly specialized, custom-built documentation engine that treats Clojure source code as the primary source of truth. Unlike standard tools that generate documentation from docstrings (Codox) or Markdown files (Docusaurus, Hugo), `code.doc` parses the Clojure AST directly to extract structure, tests, and documentation "directives".

**Recommendation:**
*   **Short-term:** Maintain the status quo if the team is comfortable with the "Literate Clojure" workflow. The system is tightly integrated and functional.
*   **Long-term:** Consider migrating to a standard SSG (like Docusaurus) *only if* the team wants to lower the "barrier to entry" for documentation or requires features like client-side search, versioning, and a wider ecosystem of plugins. The maintenance cost of a custom AST parser (`code.doc.parse`) is non-trivial.

## 2. Comparison Matrix

| Feature | `code.doc` | Codox | Docusaurus / VitePress | Hugo / Jekyll |
| :--- | :--- | :--- | :--- | :--- |
| **Input Source** | Clojure Source (`.clj`) | Clojure Source (docstrings) | Markdown / MDX | Markdown |
| **Paradigm** | Literate Programming (Directives) | API Reference Generation | Static Site Generation (SSG) | Static Site Generation (SSG) |
| **Templating** | Custom (String Replace + Hiccup) | Hiccup / Selmer | React / Vue | Go Templates / Liquid |
| **Search** | None / Custom | Basic (client-side) | Algolia / Local Search | Algolia / FlexSearch |
| **Extensibility** | High (It's just Clojure code) | Moderate (Plugins) | Very High (React Ecosystem) | High (Theme Ecosystem) |
| **Maintenance** | High (Custom parser & engine) | Low (Maintained by community) | Low (Maintained by Meta/Vue team) | Low (Maintained by community) |
| **Aesthetics** | Custom Themes (`bolton`, `stark`) | Standard / Functional | Modern / Polished | Variable (Thousands of themes) |

## 3. Deep Dive: `code.doc` Internals

### 3.1 The Parser (`src/code/doc/parse.clj`)
The heart of `code.doc` is a custom parser that uses `code.query` and `code.edit` (likely zipper-based navigation) to walk the Clojure concrete syntax tree (CST).

*   **Unique approach:** It doesn't just read comments. It parses `(fact ...)`, `(deftest ...)`, and custom vectors like `[[:chapter {:title "..."}]]`.
*   **Risk:** This code is complex (`parse-loop`, `parse-single`, stateful recursion). Any changes to the Clojure syntax or the desire to support new forms requires modifying this parser. It is brittle compared to standard Markdown parsers.

### 3.2 The Collection & Reference System (`src/code/doc/collect/`)
*   It builds a graph of "articles" and "references" by manually linking namespaces.
*   It allows for fine-grained control over what gets documented, but it requires manual wiring in `all-pages` configuration.

### 3.3 The Engine & Theming (`src/code/doc/theme/`)
*   **Theming as Code:** Themes are Clojure namespaces (`bolton`, `stark`). This provides ultimate power but couples the content presentation tightly to the codebase.
*   **Rendering:** It uses `std.html` (Hiccup-style) to generate HTML. This is a solid choice for a Clojure project, avoiding the need for an external templating language.

## 4. Pros & Cons

### Pros
*   **Single Source of Truth:** Documentation lives *inside* the code structure, not just in docstrings but as structural elements (Chapters defined as vectors in the file).
*   **Test Integration:** It treats `fact` and `deftest` forms as first-class documentation elements. This is its **killer feature**. It effectively forces documentation to be accurate because the documentation *is* the test code.
*   **Total Control:** You have 100% control over the output HTML and logic.

### Cons
*   **High Maintenance:** You own the parser, the renderer, the theme engine, and the build process.
*   **Non-Standard Workflow:** New developers must learn "how to write docs in vector directives" rather than just writing Markdown.
*   **Limited Features:** Missing out-of-the-box features like:
    *   Full-text search.
    *   Dark mode toggle (unless manually built).
    *   Versioning (v1.0 vs v2.0 docs).
    *   Internationalization (i18n).
    *   Mobile-optimized navigation (likely custom implemented).
*   **Fragility:** The parser relies on specific code structures. Refactoring code might break documentation generation if not careful.

## 5. Specific Recommendations

### If keeping `code.doc`:
1.  **Document the Directives:** Ensure there is a cheat sheet for the `[[:chapter ...]]` syntax, as this is non-standard.
2.  **Simplify `parse.clj`:** If possible, leverage `rewrite-clj` or a standard CST tool if not already doing so, to reduce the maintenance burden of the custom zipper logic.
3.  **Decouple Themes:** Move CSS/JS assets out of the code structure if possible, or use a standard utility class library (Tailwind is mentioned in `std.tailwind`, maybe leverage that more).

### If migrating:
1.  **Docusaurus** is the strongest contender for a "Docs-as-Code" replacement.
2.  **Migration Path:**
    *   Write a script to use the existing `parse.clj` logic to export content to `.md` or `.mdx` files.
    *   Use the exported Markdown as the source for Docusaurus.
    *   This preserves the existing content while moving the infrastructure to a maintained platform.

## 6. Conclusion
`code.doc` is an impressive piece of engineering that implements a specific vision of "Literate Testing/Documentation". It is valuable if the team heavily relies on the "Tests as Documentation" feature. However, if the goal is to have a standard, low-maintenance documentation portal, it is "over-engineered" compared to modern off-the-shelf solutions.
