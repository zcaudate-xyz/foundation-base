(ns documentation.code-guides
  (:require [code.doc.parse :as parse]
            [std.block.navigate :as nav])
  (:use code.test))

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

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Directives become element maps"}]]

"Authoring a `code.doc` page means writing data structures that the parser turns into element maps. `parse-directive` converts vectors like `[:chapter ...]` into plain maps, and `parse-attribute` converts attribute vectors."

^{:refer code.doc.parse/parse-directive :added "3.0"}
(fact "chapters, sections, and cards are directives"
  (-> (nav/parse-string "[[:chapter {:title \"Introduction\"}]]")
      (parse/parse-directive))
  => {:type :chapter :title "Introduction"}

  (-> (nav/parse-string "[[:card-grid {:items [{:title \"A\"}]}]]")
      (parse/parse-directive))
  => {:type :card-grid :items [{:title "A"}]})

^{:refer code.doc.parse/parse-attribute :added "3.0"}
(fact "attribute vectors merge into the next element"
  (-> (nav/parse-string "[[{:class \"lead\"}]]")
      (parse/parse-attribute))
  => {:type :attribute :class "lead"})

[[:section {:title "Prose and markdown"}]]

"Plain strings become paragraphs, and markdown headers are converted to chapter or section directives. Embedded directive lines are parsed the same way as Clojure directives."

^{:refer code.doc.parse/parse-paragraph :added "3.0"}
(fact "string literals become paragraphs"
  (-> (nav/parse-string "\"this is a paragraph\"")
      (parse/parse-paragraph))
  => {:type :paragraph :text "this is a paragraph"})

^{:refer code.doc.parse/parse-header :added "4.1"}
(fact "markdown headers map to document structure"
  (parse/parse-header "# Getting Started")
  => {:type :chapter :title "Getting Started"}

  (parse/parse-header "## First Steps {#first-steps}")
  => {:type :section :title "First Steps" :tag "first-steps"})

^{:refer code.doc.parse/parse-markdown-directive :added "4.1"}
(fact "directives can live inside markdown"
  (parse/parse-markdown-directive "[[:callout {:tone :info :title \"Hello\" :content \"World\"}]]")
  => {:type :callout :tone :info :title "Hello" :content "World"})

[[:section {:title "Facts and code blocks"}]]

"Pages often include runnable examples. `parse-fact-form` extracts the caption and assertion code from a `fact` form, while `parse-code-directive` captures fenced code blocks."

^{:refer code.doc.parse/parse-fact-form :added "3.0"}
(fact "fact forms become test elements"
  (-> (nav/parse-string "(fact \"addition works\" (+ 1 1) \n => 2)")
      (parse/parse-fact-form))
  => {:type :test :indentation 2 :caption "addition works" :code "(+ 1 1) \n => 2"})

^{:refer code.doc.parse/parse-code-directive :added "3.0"}
(fact "code directives become block elements"
  (-> (nav/parse-string "[[:code {:language :clojure} \"(+ 1 1)\"]]")
      (parse/parse-code-directive))
  => {:type :block :indentation 0 :code "(+ 1 1)" :language :clojure})

[[:section {:title "End-to-end: parse a short guide"}]]

"`parse-markdown` turns a complete markdown string into a sequence of elements, including front matter, headers, embedded directives, and fenced code blocks."

^{:refer code.doc.parse/parse-markdown :added "4.1"}
(fact "parse a complete markdown guide"
  (parse/parse-markdown (str "---\n{:title \"Hello\"}\n---\n"
                             "# Heading {#heading}\n"
                             "[[:callout {:tone :success :title \"Nice\" :content \"Yep\"}]]\n"
                             "```clojure\n(+ 1 1)\n```"))
  => [{:type :article :title "Hello"}
      {:type :chapter :title "Heading" :tag "heading"}
      {:type :callout :tone :success :title "Nice" :content "Yep"}
      {:type :block :indentation 0 :lang "clojure" :code "(+ 1 1)"}])

;; BEGIN merged documentation: plans/slop/summary/documentation_helper.md
;; sha256: f605bf34f1ac0a7ce6070dfb79376d5ea3da3b9a3d0ebbf5473f3f962e078d1a
[[:chapter {:title "Documentation Helper" :link "merged-plans-slop-summary-documentation-helper-md"}]]

[[:code {:lang "json"} "{\n  \"file_path\": \"/path/to/your/test/file.clj\",\n  \"task\": \"Refactor Clojure Test Docstrings Using Source Context\",\n  \"instructions\": [\n    \"**Scan** the file at `file_path` for all Clojure forms.\",\n    \"**Identify** every form that starts with the symbol `fact`.\",\n    \"For each `fact` form, **extract the function symbol** from the preceding metadata (look for the `:refer` key, e.g., `script.css/generate-style`).\",\n    \"**Locate and analyze the source code** of this function in the corresponding `src` directory (e.g., `src/script/css.clj`) to understand the function's full contract (inputs, outputs, purpose, and existing docstring).\",\n    \"The **docstring** to be modified is the first argument to the `fact` form (a string literal).\",\n    \"**Modify** the existing docstring to be more descriptive and informative, synthesizing information from the test body and the source function's context.\",\n    \"The new docstring must clarify *what* the test is verifying about the function's behavior, especially in relation to the specific input and expected output of the test case.\",\n    \"**Example Transformation for `generate-css` (Source-Aware):**\",\n    \"  * If the source docstring says the function 'creates a stylesheet from a vector of rules', the test docstring should be refined to: `\\\"generates a complete stylesheet by correctly processing a vector containing multiple [selector map] rules into a formatted CSS string.\\\"`\",\n    \"**Preserve** all surrounding metadata (e.g., `^{:refer ...}`), all test code within the `fact` body, and file structure (`ns`, `use`, `require`).\",\n    \"**Output** the complete, modified file content.\"\n  ],\n  \"context\": \"The test file verifies functions in the `script.css` namespace, covering both CSS generation and parsing. The agent should use the source code to provide the most accurate and specific descriptions possible for each test.\"\n}"]]
;; END merged documentation: plans/slop/summary/documentation_helper.md
