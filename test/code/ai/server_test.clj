(ns code.ai.server-test
  (:use code.test)
  (:require [code.ai.server :as server]
            [code.ai.server.tool.basic :as basic]
            [code.ai.server.tool.std-lang :as std-lang]
            [std.lang :as l]
            [mcp-clj.mcp-server.core :as mcp-server]))

^{:refer code.ai.server.tool.basic/echo-fn :added "4.0"}
(fact "echoes input text"
  (basic/echo-fn nil {:text "hello"})
  => {:content [{:type "text" :text "hello"}]
      :isError false})

^{:refer code.ai.server.tool.basic/ping-fn :added "4.0"}
(fact "returns ping"
  (basic/ping-fn nil nil)
  => {:content [{:type "text" :text "ping"}]
      :isError false})

^{:refer code.ai.server.tool.std-lang/lang-emit-as-safe :added "4.0"}
(fact "safely emits code"
  (std-lang/lang-emit-as-safe :lua "(+ 1 2)")
  => "1 + 2"

  (std-lang/lang-emit-as-safe :js "(+ 1 2)")
  => "1 + 2")

^{:refer code.ai.server.tool.std-lang/lang-emit-as-fn :added "4.0"}
(fact "tool wrapper for emit"
  (std-lang/lang-emit-as-fn nil {:type "lua" :code "(+ 1 2)"})
  => {:content [{:type "text" :text "1 + 2"}]
      :isError false})

^{:refer code.ai.server/create-server :added "4.0"}
(fact "creates a server instance"
  (server/create-server)
  => map?)

^{:refer code.ai.server/start-server :added "4.0"}
(fact "starts the server"
  (with-redefs [mcp-server/create-server (constantly {:stop (fn [])})]
    (server/start-server)
    => map?
    @server/*server* => map?))

^{:refer code.ai.server/stop-server :added "4.0"}
(fact "stops the server"
  (with-redefs [mcp-server/create-server (constantly {:stop (fn [])})]
    (server/start-server)
    (server/stop-server)
    @server/*server* => nil))
