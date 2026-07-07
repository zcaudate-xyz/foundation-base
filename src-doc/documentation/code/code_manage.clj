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
"# `code.manage` Guide\n\n`code.manage` provides a suite of tasks for maintaining code quality, managing tests, and refactoring. It is typically invoked via `lein manage` or from the REPL.\n\n## Core Concepts\n\n- **Tasks**: Operations that run over a set of namespaces (e.g., `analyse`, `grep`).\n- **Templates**: Preset configurations for tasks (e.g., `:code.transform`, `:code.locate`).\n- **Selectors**: Arguments to target specific namespaces (e.g., vector of symbols `['my.ns]`).\n\n## Usage\n\n### Invocation\n\n```bash\n# From CLI\nlein manage <task> <namespaces> <options>\n\n# Example\nlein manage analyse \"['code.manage]\" \"{:print {:summary true}}\"\n```\n\n```clojure\n;; From REPL\n(require '[code.manage :as manage])\n(manage/analyse ['code.manage] {:print {:summary true}})\n```\n\n### Scenarios\n\n#### 1. Codebase Cleanup Workflow\n\nA typical cleanup session might involve identifying messy code and then standardizing it.\n\n**Step A: Identify \"Unclean\" Code**\nFind files with top-level comment blocks (often used for debugging/scratch) that shouldn't be committed.\n\n```clojure\n(manage/unclean ['my.project] {:print {:item true}})\n```\n\n**Step B: Remove Docstrings from Source**\nIf you prefer keeping docstrings in tests or external docs, you can purge them.\n\n```clojure\n(manage/purge ['my.project] {:write true})\n```\n\n**Step C: Standardize Namespace Declarations**\nEnsure `ns` forms are formatted consistently (requires sorting, indentation).\n\n```clojure\n(manage/ns-format ['my.project] {:write true})\n```\n\n#### 2. Test Coverage & Management\n\n`code.manage` integrates tightly with `code.test` to ensure coverage.\n\n**Step A: Find Missing Tests**\nList functions that have no corresponding `fact`.\n\n```clojure\n(manage/missing ['my.project])\n```\n\n**Step B: Scaffold New Tests**\nGenerate test files and stubs for the missing functions.\n\n```clojure\n(manage/scaffold ['my.project] {:write true})\n```\n\n**Step C: Identify \"Orphaned\" Tests**\nFind tests that refer to non-existent functions (e.g., after a rename/delete).\n\n```clojure\n(manage/orphaned ['my.project])\n```\n\n#### 3. Large Scale Refactoring\n\nUse `refactor-code` or `grep-replace` for bulk changes.\n\n**Scenario: Renaming a function across the codebase**\n\nIf simple grep isn't enough (e.g., context sensitive), you can write a custom transform script. However, for simple string replacement:\n\n```clojure\n(manage/grep-replace ['my.project]\n                     {:query \"old-fn-name\"\n                      :replace \"new-fn-name\"\n                      :write true})\n```\n\n**Scenario: Custom AST Modification**\n\nYou can use `refactor-code` with a custom edit function that operates on the zipper.\n\n```clojure\n(require '[code.query :as query]\n         '[std.block.navigate :as edit])\n\n(manage/refactor-code ['my.project]\n  {:edits [(fn [zloc]\n             ;; Use code.query to find/modify\n             (query/modify zloc\n                           '[defn old-name]\n                           (fn [node]\n                             (edit/set-value node 'new-name))))]\n   :write true})\n```\n\n#### 4. Search and Analysis\n\n**Scenario: Find all usages of a specific var**\n\nUseful when checking impact before a change.\n\n```clojure\n(manage/find-usages ['my.project]\n                    {:var 'my.project.core/my-func})\n```\n\n**Scenario: Grep with highlighting**\n\nQuickly scan for a pattern.\n\n```clojure\n(manage/grep ['my.project]\n             {:query \"TODO\"\n              :highlight true})\n```\n"
;; END merged documentation: guides/code.manage.md
