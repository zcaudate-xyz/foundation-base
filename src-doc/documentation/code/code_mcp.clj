(ns documentation.code-mcp
  (:use code.test))

[[:hero {:title "code.mcp"
         :subtitle "Agent-facing MCP tool surface."
         :lead "`code.mcp` exposes foundation maintenance actions as MCP tools, including code management, test execution, documentation helpers, Maven helpers, JVM namespace helpers, and hara.lang probes."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Agents need structured operations instead of ad hoc shell commands. The MCP layer packages safe, named capabilities around the existing `code.*`, `jvm.*`, and `hara.*` toolchains."

[[:chapter {:title "Internal usage" :link "internal"}]]

"`code.mcp.server/default-instructions` advertises code-manage, code-test, hara-lang, and code-maven capabilities. Individual tool namespaces wrap the same functions that maintainers can run from the REPL or Leiningen."

[[:chapter {:title "API" :link "api"}]]

