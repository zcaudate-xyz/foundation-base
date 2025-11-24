(ns code.ai.server.tool.basic-test
  (:require [code.test :refer :all]
            [code.ai.server.tool.basic :as basic]))

^{:refer code.ai.server.tool.basic/echo-fn :added "0.1"}
(fact "echo function returns input text"
  (basic/echo-fn nil {:text "hello"})
  => {:content [{:type "text" :text "hello"}]
      :isError false})

^{:refer code.ai.server.tool.basic/ping-fn :added "0.1"}
(fact "ping function returns ping"
  (basic/ping-fn nil nil)
  => {:content [{:type "text" :text "ping"}]
      :isError false})
