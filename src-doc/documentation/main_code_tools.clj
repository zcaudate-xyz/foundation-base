(ns documentation.main-code-tools)

[[:hero {:title "Code Tools"
          :subtitle "Testing, documentation, source queries, management, and project tooling."
          :lead "The `code.*` namespaces are the repository's development and maintenance layer. This root page provides reliable links even when the full `code/` subsite has not yet been published."
          :actions [{:label "Open full code site" :href "code/index.html" :variant :primary}
                    {:label "Browse source" :href "https://github.com/zcaudate-xyz/foundation-base/tree/main/src/code"}]}]]

[[:card-grid {:items [{:meta "Testing"
                       :title "code.test"
                       :text "Fact-based tests, arrow assertions, checkers, skips, and targeted runs."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/documentation/code/code_test.clj"}
                      {:meta "Management"
                       :title "code.manage"
                       :text "Analysis, scaffolding, location, refactoring, and test-management workflows."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/documentation/code/code_manage.clj"}
                      {:meta "Queries"
                       :title "code.query"
                       :text "Structural matching, traversal, and transformations for Clojure source."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/documentation/code/code_query.clj"}
                      {:meta "Documentation"
                       :title "code.doc"
                       :text "Parse, collect, link, render, theme, and publish static documentation."
                       :href "code/code-doc.html"}
                      {:meta "Project"
                       :title "code.project"
                       :text "Project metadata, namespace lookup, and source-file discovery."
                       :href "code/code-project.html"}
                      {:meta "Build tools"
                       :title "code.tool"
                       :text "Build, Java, Maven, and measurement utilities."
                       :href "code/code-tool.html"}]}]]

[[:chapter {:title "First interaction"}]]

"Start by running one targeted test namespace or loading project metadata in the REPL."

[[:code {:lang "bash"}
  "lein test :only code.doc-test\nlein test :only std.lib.collection-test"]]

[[:callout {:tone :info
             :title "Fallback navigation"
             :content "Direct GitHub guide and source links are kept on this page so readers are not blocked if a generated subsite is temporarily missing or stale."}]]
