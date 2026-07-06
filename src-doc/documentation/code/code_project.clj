(ns documentation.code-project
  (:use code.test))

[[:hero {:title "code.project"
         :subtitle "Project metadata and file lookup."
         :lead "`code.project` normalizes project files, source paths, lookup tables, and dependency metadata so tools can work across source, test, and language directories."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Most higher-level tools need to know where namespaces live. `code.project` builds that lookup from project metadata and abstracts over Leiningen and Shadow project shapes."

[[:chapter {:title "Internal usage" :link "internal"}]]

"`code.framework`, `code.manage`, `code.doc`, and build tooling all use project lookups to resolve namespaces to files. This is also what lets documentation pages reference APIs from `src`, `src-lang`, and tests consistently."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "code.project"}]]
[[:api {:namespace "code.project.common"}]]
[[:api {:namespace "code.project.lein"}]]
[[:api {:namespace "code.project.shadow"}]]
