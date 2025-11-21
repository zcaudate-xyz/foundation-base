# Plan for Generating cljfmt and clj-kondo Rules for Custom Top-Level Forms

## Objective
To systematically identify custom top-level forms within the `/src` directory of the `foundation-base` project and propose methods for generating appropriate `cljfmt` and `clj-kondo` rules.

## Phase 1: Identification of Custom Top-Level Forms

The goal of this phase is to identify all unique custom top-level forms. This will involve more than just `defmacro` as other constructs might also define custom forms (e.g., `def`, `defn` with specific metadata, `deftype`, `defrecord`, `defprotocol`, `defmulti`, `defmethod` when used in a DSL-like manner).

### Steps:
1.  **List all `.clj` files in `/src`:** Obtain a comprehensive list of all Clojure source files.
2.  **Parse each file for top-level forms:** For each `.clj` file, read its content and identify all top-level forms. This will require a more sophisticated approach than simple regex, possibly involving a basic Clojure parser or by looking for common patterns of top-level definitions.
3.  **Filter for custom forms:**
    *   Exclude standard Clojure forms (e.g., `ns`, `require`, `def`, `defn`, `let`, `if`, `do`, `comment`).
    *   Identify forms that are likely custom macros or DSL constructs. This will involve looking for:
        *   `defmacro` and its variants (`defmacro.js`, `defmacro.lua`, etc.).
        *   `def` forms that define functions or macros (e.g., `(def my-macro (macro/macro-fn ...))`).
        *   Forms with specific metadata that indicates custom behavior (e.g., `^{:static/lang :bash}`).
        *   `deftype`, `defrecord`, `defprotocol`, `defmulti`, `defmethod` if they are part of a custom DSL.
4.  **Categorize custom forms:** Group identified custom forms by their type and behavior (e.g., function-like, variable-like, block-like, DSL-specific). This categorization will inform the type of `cljfmt` and `clj-kondo` rules needed.
5.  **Collect metadata:** Extract any relevant metadata associated with these forms, especially `^:style/indent` for `cljfmt` and any `lint-as` hints for `clj-kondo`.

## Phase 2: Generation of clj-kondo Rules

Based on the identified and categorized custom forms, generate `clj-kondo` `lint-as` rules.

### Steps:
1.  **Default `lint-as` mappings:** For each category of custom forms, propose a default `lint-as` mapping to a standard Clojure form (e.g., `clojure.core/def`, `clojure.core/defn`, `clojure.core/do`).
2.  **Automate `lint-as` generation:** Develop a script or a systematic process to generate the `lint-as` entries for the `.clj-kondo/config.edn` file. This script would:
    *   Read the identified custom forms and their categories.
    *   Generate the corresponding `lint-as` entries.
    *   Merge these new entries with the existing `.clj-kondo/config.edn` without overwriting existing custom rules.
3.  **Identify potential custom hooks:** For complex custom forms that cannot be adequately handled by `lint-as`, identify them as candidates for custom `clj-kondo` hooks. This would require manual inspection and development of Clojure code for the hooks.

## Phase 3: Generation of cljfmt Rules

Address `cljfmt` rules, focusing on indentation and formatting.

### Steps:
1.  **Review existing `^:style/indent`:** For custom forms that already have `^:style/indent` metadata, verify if the indentation is correct.
2.  **Identify missing `^:style/indent`:** For custom forms lacking `^:style/indent` metadata, propose adding appropriate metadata based on their structure (e.g., `{:style/indent 1}` for forms with a body).
3.  **Generate `.cljfmt.edn` entries (if necessary):** If `^:style/indent` is insufficient or if specific formatting rules are required (e.g., for complex DSLs), generate entries for a `.cljfmt.edn` file. This would involve:
    *   Identifying the custom forms that need explicit `cljfmt` rules.
    *   Defining the appropriate indentation rules (e.g., `[[:block 1]]`, `[[:inner 0]]`).
    *   Creating or updating the `.cljfmt.edn` file with these rules.

## Phase 4: Verification and Refinement

After generating the rules, verify their effectiveness and refine as needed.

### Steps:
1.  **Run `clj-kondo`:** Execute `clj-kondo` across the entire codebase with the updated configuration and analyze the output for new warnings or errors related to the custom forms.
2.  **Run `cljfmt`:** Apply `cljfmt` to the codebase and visually inspect the formatting of the custom forms.
3.  **Iterative refinement:** Adjust `lint-as` rules, add custom hooks, or refine `cljfmt` rules based on the verification results.

## Tools to be used:
*   `default_api.glob`: To list `.clj` files.
*   `default_api.read_file`: To read file contents.
*   `default_api.search_file_content`: To find patterns like `defmacro`.
*   `default_api.write_file`: To create the plan file and update config files.
*   `default_api.replace`: To modify existing config files.
*   `clj_eval`: Potentially for parsing Clojure forms if a simple regex is insufficient.
