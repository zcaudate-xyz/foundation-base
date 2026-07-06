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
