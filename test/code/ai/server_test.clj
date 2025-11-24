(ns code.ai.server-test
  (:use code.test)
  (:require [code.ai.server :as server]
            [mcp-clj.mcp-server.core :as mcp-server]
            [mcp-clj.in-memory-transport.shared :as shared]))

^{:refer code.ai.server/create-server :added "4.0"}
(fact "creates and manages a server instance"
  (let [shared (shared/create-shared-transport)]
    (server/start-server {:type :in-memory :shared shared})
    @server/*server* => map?
    (server/stop-server)
    @server/*server* => nil))

^{:refer code.ai.server/echo-fn :added "4.0"}
(fact "echo function"
  (server/echo-fn nil {:text "hello"})
  => {:content [{:type "text" :text "hello"}]
      :isError false})

^{:refer code.ai.server/ping-fn :added "4.0"}
(fact "ping function"
  (server/ping-fn nil {:text "any"})
  => {:content [{:type "text" :text "ping"}]
      :isError false})

^{:refer code.ai.server/lang-emit-as-fn :added "4.0"}
(fact "lang emit as function"
  (let [res (server/lang-emit-as-fn nil {:type "lua" :code "(+ 1 2 3)"})]
    res => (contains {:isError false})
    (-> res :content first :text) => "1 + 2 + 3"))