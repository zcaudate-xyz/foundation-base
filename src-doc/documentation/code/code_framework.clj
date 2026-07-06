(ns documentation.code-framework
  (:use code.test))

[[:hero {:title "code.framework"
         :subtitle "Source and test analysis foundation."
         :lead "`code.framework` extracts namespace, function, docstring, line, and test metadata from source files. It is the analysis substrate below code management and documentation generation."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Documentation and maintenance both need an index of what exists. `code.framework` creates that index by analysing source and test files and by connecting implementation forms with facts and docstrings."

[[:chapter {:title "How it is used" :link "usage"}]]

"Use `analyse` for namespace-level source/test metadata, `extract` for function-level entries, and the link/cache helpers when repeated project-wide scans would be too expensive."

[[:chapter {:title "Internal usage" :link "internal"}]]

"`code.doc` relies on framework-style analysis to build API tables. `code.manage` uses the same metadata to find missing tests, incomplete examples, orphaned references, and importable fact bodies."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.framework"}]]
[[:api {:namespace "code.framework.common"}]]
[[:api {:namespace "code.framework.link"}]]
[[:api {:namespace "code.framework.cache"}]]
[[:api {:namespace "code.framework.docstring"}]]
