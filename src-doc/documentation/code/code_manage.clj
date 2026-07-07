(ns documentation.code-manage
  (:require [code.manage :as manage])
  (:use code.test))

[[:hero {:title "code.manage"
         :subtitle "Analysis, scaffolding, location, and refactoring tasks."
         :lead "`code.manage` is the operational interface for maintaining this codebase. It wraps source analysis, grep-like location, usage discovery, test import, scaffolding, formatting, and structural refactors behind task-style commands."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"The codebase is large enough that plain text search is not always enough. `code.manage` understands namespaces, test files, fact metadata, and parsed Clojure blocks, making it suitable for documentation discovery and source maintenance."

[[:chapter {:title "Common workflows" :link "workflows"}]]

"Use `locate-code` to find structural forms, `find-usages` to identify where a var is referenced through aliases or direct names, `import` to pull examples from tests, and `scaffold` to create missing tests. For docs work, `find-usages` and `locate-code` should be used to collect the internal usage summaries on each page."

(comment
  (code.manage/find-usages '[code]
                           {:var 'code.framework/analyse})
  (code.manage/locate-code '[hara]
                           {:query ['l/script]}))

[[:chapter {:title "Internal usage" :link "internal"}]]

"The repository exposes `code.manage` through Leiningen tasks, JVM tool helpers, and MCP tools. It is also the safest way to gather examples for documentation without inventing usage narratives by hand."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Analysing namespaces"}]]

"`code.manage` tasks operate on namespaces, sets of namespaces, or `:all`. `analyse` returns structured metadata about source or test files, and `vars` lists the public vars."

^{:refer code.manage/analyse :added "3.0"}
(fact "analyse a single namespace"
  (manage/analyse 'code.manage)
  => code.framework.common.Entry)

^{:refer code.manage/vars :added "3.0"}
(fact "list vars in a namespace"
  (manage/vars 'code.manage)
  => vector?)

[[:section {:title "Finding code"}]]

"`find-usages` discovers where a var is referenced, while `locate-code` and `grep` search for structural patterns and raw text."

^{:refer code.manage/find-usages :added "3.0"}
(fact "find usages of a var"
  (manage/find-usages 'code.manage
                      {:var 'code.framework/analyse
                       :print {:result false :summary false}})
  => map?)

^{:refer code.manage/locate-code :added "3.0"}
(fact "locate forms matching a structural query"
  (manage/locate-code 'code.manage
                      {:query '[ns | {:first :require}]
                       :print {:result false :summary false}})
  => seq?)

^{:refer code.manage/grep :added "3.0"}
(fact "grep for text in files"
  (manage/grep 'code.manage
               {:query "analyse"
                :print {:result false :summary false}})
  => seq?)

[[:section {:title "Test hygiene"}]]

"`missing`, `orphaned`, and `incomplete` help keep tests in sync with source code. They report source vars without tests, tests without source, and both problems together."

^{:refer code.manage/missing :added "3.0"}
(fact "find source vars without tests"
  (manage/missing 'code.manage
                  {:print {:result false :summary false}})
  => map?)

^{:refer code.manage/orphaned :added "3.0"}
(fact "find tests without corresponding source"
  (manage/orphaned 'code.manage
                   {:print {:result false :summary false}})
  => map?)

[[:section {:title "End-to-end: preview a safe refactor"}]]

"`grep-replace` performs text replacement across files. Set `:write false` to preview diffs before committing changes."

^{:refer code.manage/grep-replace :added "3.0"}
(fact "preview a no-op replacement"
  (manage/grep-replace 'code.manage
                       {:query "definvoke"
                        :replace "definvoke"
                        :write false
                        :print {:result false :summary false}})
  => (contains {:updated false}))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.manage"}]]
[[:api {:namespace "code.manage.var"}]]
[[:api {:namespace "code.manage.unit"}]]

;; BEGIN merged documentation: guides/code.manage.md
;; sha256: 6c73b0c9dd63628555fa78d92d8920c11594459bc929d65c9c3d045516d4ff56
[[:chapter {:title "code.manage Guide" :link "merged-guides-code-manage-md"}]]

"`code.manage` provides a suite of tasks for maintaining code quality, managing tests, and refactoring. It is typically invoked via `lein manage` or from the REPL."

[[:section {:title "Core Concepts" :link "merged-guides-code-manage-md-core-concepts"}]]

"- **Tasks**: Operations that run over a set of namespaces (e.g., `analyse`, `grep`).\n- **Templates**: Preset configurations for tasks (e.g., `:code.transform`, `:code.locate`).\n- **Selectors**: Arguments to target specific namespaces (e.g., vector of symbols `['my.ns]`)."

[[:section {:title "Usage" :link "merged-guides-code-manage-md-usage"}]]

[[:subsection {:title "Invocation" :link "merged-guides-code-manage-md-invocation"}]]

[[:code {:lang "bash"} "# From CLI\nlein manage <task> <namespaces> <options>\n\n# Example\nlein manage analyse \"['code.manage]\" \"{:print {:summary true}}\""]]

[[:code {:lang "clojure"} ";; From REPL\n(require '[code.manage :as manage])\n(manage/analyse ['code.manage] {:print {:summary true}})"]]

[[:subsection {:title "Scenarios" :link "merged-guides-code-manage-md-scenarios"}]]

[[:subsubsection {:title "1. Codebase Cleanup Workflow" :link "merged-guides-code-manage-md-1-codebase-cleanup-workflow"}]]

"A typical cleanup session might involve identifying messy code and then standardizing it."

"**Step A: Identify \"Unclean\" Code**\nFind files with top-level comment blocks (often used for debugging/scratch) that shouldn't be committed."

[[:code {:lang "clojure"} "(manage/unclean ['my.project] {:print {:item true}})"]]

"**Step B: Remove Docstrings from Source**\nIf you prefer keeping docstrings in tests or external docs, you can purge them."

[[:code {:lang "clojure"} "(manage/purge ['my.project] {:write true})"]]

"**Step C: Standardize Namespace Declarations**\nEnsure `ns` forms are formatted consistently (requires sorting, indentation)."

[[:code {:lang "clojure"} "(manage/ns-format ['my.project] {:write true})"]]

[[:subsubsection {:title "2. Test Coverage & Management" :link "merged-guides-code-manage-md-2-test-coverage-management"}]]

"`code.manage` integrates tightly with `code.test` to ensure coverage."

"**Step A: Find Missing Tests**\nList functions that have no corresponding `fact`."

[[:code {:lang "clojure"} "(manage/missing ['my.project])"]]

"**Step B: Scaffold New Tests**\nGenerate test files and stubs for the missing functions."

[[:code {:lang "clojure"} "(manage/scaffold ['my.project] {:write true})"]]

"**Step C: Identify \"Orphaned\" Tests**\nFind tests that refer to non-existent functions (e.g., after a rename/delete)."

[[:code {:lang "clojure"} "(manage/orphaned ['my.project])"]]

[[:subsubsection {:title "3. Large Scale Refactoring" :link "merged-guides-code-manage-md-3-large-scale-refactoring"}]]

"Use `refactor-code` or `grep-replace` for bulk changes."

"**Scenario: Renaming a function across the codebase**"

"If simple grep isn't enough (e.g., context sensitive), you can write a custom transform script. However, for simple string replacement:"

[[:code {:lang "clojure"} "(manage/grep-replace ['my.project]\n                     {:query \"old-fn-name\"\n                      :replace \"new-fn-name\"\n                      :write true})"]]

"**Scenario: Custom AST Modification**"

"You can use `refactor-code` with a custom edit function that operates on the zipper."

[[:code {:lang "clojure"} "(require '[code.query :as query]\n         '[std.block.navigate :as edit])\n\n(manage/refactor-code ['my.project]\n  {:edits [(fn [zloc]\n             ;; Use code.query to find/modify\n             (query/modify zloc\n                           '[defn old-name]\n                           (fn [node]\n                             (edit/set-value node 'new-name))))]\n   :write true})"]]

[[:subsubsection {:title "4. Search and Analysis" :link "merged-guides-code-manage-md-4-search-and-analysis"}]]

"**Scenario: Find all usages of a specific var**"

"Useful when checking impact before a change."

[[:code {:lang "clojure"} "(manage/find-usages ['my.project]\n                    {:var 'my.project.core/my-func})"]]

"**Scenario: Grep with highlighting**"

"Quickly scan for a pattern."

[[:code {:lang "clojure"} "(manage/grep ['my.project]\n             {:query \"TODO\"\n              :highlight true})"]]
;; END merged documentation: guides/code.manage.md
