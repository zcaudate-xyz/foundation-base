(ns code.mcp.server-test
  (:require [code.mcp.base.server :as base-server]
            [code.mcp.server :as server]
            [code.mcp.tool.basic :as basic]
            [code.mcp.tool.std-lang :as std-lang])
  (:use code.test))

^{:refer code.mcp.tool.basic/echo-fn :added "4.0"}
(fact "echoes input text"
  (basic/echo-fn nil {:text "hello"})
  => {:content [{:type "text" :text "hello"}]
      :isError false})

^{:refer code.mcp.tool.basic/ping-fn :added "4.0"}
(fact "returns ping"
  (basic/ping-fn nil nil)
  => {:content [{:type "text" :text "ping"}]
      :isError false})

^{:refer code.mcp.tool.std-lang/lang-emit-as-safe :added "4.0"}
(fact "safely emits code"
  (std-lang/lang-emit-as-safe :lua "(+ 1 2)")
  => "1 + 2"

  (std-lang/lang-emit-as-safe :js "(+ 1 2)")
  => "1 + 2")

^{:refer code.mcp.tool.std-lang/lang-emit-as-fn :added "4.0"}
(fact "tool wrapper for emit"
  (std-lang/lang-emit-as-fn nil {:type "lua" :code "(+ 1 2)"})
  => {:content [{:type "text" :text "1 + 2"}]
      :isError false})

^{:refer code.mcp.server/default-tools :added "4.1"}
(fact "default server tools include expected tool names"
  (->> (server/default-tools)
       (map :name)
       set)
  => #{"echo" "ping"
       "clj-eval"
       "code-test"
       "code-manage"
       "jvm-namespace"
       "std-lang-manage"
       "lang-emit-as" "std-lang-list" "std-lang-modules"
       "code-doc-init" "code-doc-deploy" "code-doc-publish"
       "code-maven"
       "form-heal-list-edits" "form-heal-get-dsl-deps" "form-heal-refactor-directory"})

^{:refer code.mcp.server/default-instructions :added "4.1"}
(fact "default server instructions advertise the project-specific autopilot tools"
  [(re-find #"code-test" (server/default-instructions))
   (re-find #"code-manage" (server/default-instructions))
   (re-find #"std-lang-manage" (server/default-instructions))
   (re-find #"code-maven" (server/default-instructions))]
  => ["code-test" "code-manage" "std-lang-manage" "code-maven"])

^{:refer code.mcp.server/create-server :added "4.0"}
(fact "creates a server instance through base server"
  (with-redefs [base-server/create-server (fn [opts] {:opts opts})]
    (server/create-server)
    => (contains {:opts map?})))

^{:refer code.mcp.server/start-server :added "4.0"}
(fact "starts the server and memoizes active instance"
  (let [calls (atom 0)]
    (with-redefs [base-server/create-server (fn [_]
                                              (swap! calls inc)
                                              {:server :mock})]
      (server/stop-server)
      (server/start-server)
      => {:server :mock}
      (server/start-server)
      => {:server :mock}
      @calls => 1)))

^{:refer code.mcp.server/stop-server :added "4.0"}
(fact "stops the server via base close"
  (let [closed (atom nil)]
    (with-redefs [base-server/create-server (fn [_] {:server :mock})
                  base-server/close! (fn [server]
                                       (reset! closed server)
                                       nil)]
      (server/stop-server)
      (server/start-server)
      (server/stop-server)
      @closed => {:server :mock}
      @server/*server* => nil)))
