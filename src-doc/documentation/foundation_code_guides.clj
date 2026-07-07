(ns documentation.code-guides)

[[:chapter {:title "Authoring guides"}]]

"The revived theme works best when pages combine narrative sections with generated technical blocks."

[[:card-grid {:items [{:meta "Landing"
                       :title "Use `:hero`, `:callout`, and `:card-grid`"
                       :text "These directives give you a high-level, product-style landing page without leaving the Clojure authoring flow."}
                      {:meta "Narrative"
                       :title "Mix markdown and Clojure pages"
                       :text "Markdown is now better for onboarding and prose, while Clojure pages remain ideal for generated API sections and code references."}
                      {:meta "Reference"
                       :title "Keep generated blocks close to prose"
                       :text "Generated `:api` and `:reference` blocks work best when they support a concept page instead of replacing it."}]}]]

[[:quote {:text "A good docs page should read like a guide, but stay anchored to the source."
           :source "code authoring rule"}]]

;; BEGIN merged documentation: plans/slop/summary/documentation_helper.md
;; sha256: f605bf34f1ac0a7ce6070dfb79376d5ea3da3b9a3d0ebbf5473f3f962e078d1a
[[:chapter {:title "Documentation Helper" :link "merged-plans-slop-summary-documentation-helper-md"}]]

[[:code {:lang "json"} "{\n  \"file_path\": \"/path/to/your/test/file.clj\",\n  \"task\": \"Refactor Clojure Test Docstrings Using Source Context\",\n  \"instructions\": [\n    \"**Scan** the file at `file_path` for all Clojure forms.\",\n    \"**Identify** every form that starts with the symbol `fact`.\",\n    \"For each `fact` form, **extract the function symbol** from the preceding metadata (look for the `:refer` key, e.g., `script.css/generate-style`).\",\n    \"**Locate and analyze the source code** of this function in the corresponding `src` directory (e.g., `src/script/css.clj`) to understand the function's full contract (inputs, outputs, purpose, and existing docstring).\",\n    \"The **docstring** to be modified is the first argument to the `fact` form (a string literal).\",\n    \"**Modify** the existing docstring to be more descriptive and informative, synthesizing information from the test body and the source function's context.\",\n    \"The new docstring must clarify *what* the test is verifying about the function's behavior, especially in relation to the specific input and expected output of the test case.\",\n    \"**Example Transformation for `generate-css` (Source-Aware):**\",\n    \"  * If the source docstring says the function 'creates a stylesheet from a vector of rules', the test docstring should be refined to: `\\\"generates a complete stylesheet by correctly processing a vector containing multiple [selector map] rules into a formatted CSS string.\\\"`\",\n    \"**Preserve** all surrounding metadata (e.g., `^{:refer ...}`), all test code within the `fact` body, and file structure (`ns`, `use`, `require`).\",\n    \"**Output** the complete, modified file content.\"\n  ],\n  \"context\": \"The test file verifies functions in the `script.css` namespace, covering both CSS generation and parsing. The agent should use the source code to provide the most accurate and specific descriptions possible for each test.\"\n}"]]
;; END merged documentation: plans/slop/summary/documentation_helper.md
