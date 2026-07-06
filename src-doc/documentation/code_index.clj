(ns documentation.code-index)

[[:hero {:title "code"
         :subtitle "Development tools for foundation."
         :lead "The `code.*` namespaces are the maintenance layer for this repository: tests, source analysis, structural queries, documentation publishing, project metadata, build tools, and agent-facing MCP commands."
         :badges ["Testing" "Analysis" "Docs" "Tools"]
         :actions [{:label "Back to home" :href "../index.html" :variant :primary}]}]]

[[:card-grid {:title "Tooling Areas"
              :lead "Use these pages when maintaining source, tests, generated documentation, or project metadata."
              :items [{:meta "Testing"
                       :title "code.test"
                       :text "Fact-based custom tests, arrow assertions, metadata, skips, and targeted runs."
                       :href "code-test.html"}
                      {:meta "Management"
                       :title "code.manage"
                       :text "Analyse, import, scaffold, locate, find usages, refactor, snap, and inspect code."
                       :href "code-manage.html"}
                      {:meta "Queries"
                       :title "code.query"
                       :text "Structural selectors and traversal tools for Clojure source trees."
                       :href "code-query.html"}
                      {:meta "Analysis"
                       :title "code.framework"
                       :text "Source and test analysis used by manage, doc, and refactor tooling."
                       :href "code-framework.html"}
                      {:meta "Docs"
                       :title "code.doc"
                       :text "Parse, collect, link, render, theme, and publish static documentation."
                       :href "code-doc.html"}
                      {:meta "Project"
                       :title "code.project"
                       :text "Project metadata, file lookup, Leiningen, and Shadow project support."
                       :href "code-project.html"}
                      {:meta "Build"
                       :title "code.tool"
                       :text "Build, Java, Maven, and measurement utilities."
                       :href "code-tool.html"}
                      {:meta "Agents"
                       :title "code.mcp"
                       :text "MCP server and tool layer for code-manage, code-test, code-doc, and hara-lang actions."
                       :href "code-mcp.html"}]}]]

[[:callout {:tone :info
            :title "How this section is organized"
            :content "The pages follow the same split as the repository maintenance workflow: `code.test` runs facts, `code.manage` finds and rewrites code, `code.framework` extracts source/test metadata, and `code.doc` turns that metadata into generated documentation."}]]
