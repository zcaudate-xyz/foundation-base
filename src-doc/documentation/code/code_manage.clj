(ns documentation.code-manage
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

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.manage"}]]
[[:api {:namespace "code.manage.var"}]]
[[:api {:namespace "code.manage.unit"}]]
