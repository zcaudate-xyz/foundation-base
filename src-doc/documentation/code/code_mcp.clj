(ns documentation.code-mcp
  (:require [code.mcp.server :as server]
            [code.mcp.tool.basic :as basic]
            [code.mcp.tool.common :as common])
  (:use code.test))

[[:hero {:title "code.mcp"
         :subtitle "Agent-facing MCP tool surface."
         :lead "`code.mcp` exposes foundation maintenance actions as MCP tools, including code management, test execution, documentation helpers, Maven helpers, JVM namespace helpers, and hara.lang probes."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Agents need structured operations instead of ad hoc shell commands. The MCP layer packages safe, named capabilities around the existing `code.*`, `jvm.*`, and `hara.*` toolchains."

[[:chapter {:title "Internal usage" :link "internal"}]]

"`code.mcp.server/default-instructions` advertises code-manage, code-test, hara-lang, and code-maven capabilities. Individual tool namespaces wrap the same functions that maintainers can run from the REPL or Leiningen."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Basic connectivity tools"}]]

"Every MCP server exposes small tools for health checks. `echo-fn` and `ping-fn` return simple responses and are useful when wiring up a new client."

^{:refer code.mcp.tool.basic/echo-fn :added "4.0"}
(fact "echo returns the input text"
  (basic/echo-fn nil {:text "hello"})
  => {:content [{:type "text" :text "hello"}]
      :isError false})

^{:refer code.mcp.tool.basic/ping-fn :added "4.0"}
(fact "ping returns a ping response"
  (basic/ping-fn nil nil)
  => {:content [{:type "text" :text "ping"}]
      :isError false})

[[:section {:title "Server setup"}]]

"`default-tools` lists the tools advertised by the server, and `default-instructions` tells clients which project tools to prefer."

^{:refer code.mcp.server/default-tools :added "4.1"}
(fact "default tools include expected names"
  (->> (server/default-tools)
       (map :name)
       set)
  => #{"echo" "ping"
       "clj-eval" "code-test" "code-manage" "jvm-namespace"
       "lang-emit-as" "hara.lang-list" "hara.lang-modules"
       "code-doc-init" "code-doc-deploy" "code-doc-publish"
       "code-maven"
       "form-heal-list-edits" "form-heal-get-dsl-deps" "form-heal-refactor-directory"})

^{:refer code.mcp.server/default-instructions :added "4.1"}
(fact "instructions advertise project-specific tools"
  [(re-find #"code-test" (server/default-instructions))
   (re-find #"code-manage" (server/default-instructions))]
  => ["code-test" "code-manage"])

[[:section {:title "Tool responses"}]]

"`common/response` wraps raw results in the MCP text-content shape, and `error-response` builds an error payload."

^{:refer code.mcp.tool.common/response :added "4.0"}
(fact "wrap a result in MCP content format"
  (common/response {:result 42})
  => {:content [{:type "text" :text "{:result 42}\n"}]
      :isError false})

^{:refer code.mcp.tool.common/error-response :added "4.0"}
(fact "wrap an error message"
  (common/error-response "something went wrong")
  => {:content [{:type "text" :text "something went wrong"}]
      :isError true})

[[:section {:title "End-to-end: create a server instance"}]]

"`create-server` assembles the configured server without starting network transport. It is useful for inspecting the advertised tool set in tests."

^{:refer code.mcp.server/create-server :added "4.0"}
(fact "create a server map"
  (let [s (server/create-server)]
    [(contains? s :provider)
     (contains? s :server)
     (contains? s :tool-specs)])
  => [true true true])

[[:chapter {:title "API" :link "api"}]]

