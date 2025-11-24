(ns code.ai.common-test
  (:use code.test)
  (:require [code.ai.common :refer :all]))

^{:refer code.ai.common/chat-string :added "4.0"}
(fact "string representation of a chat")

^{:refer code.ai.common/chat-start :added "4.0"}
(fact "starts the context runtime")

^{:refer code.ai.common/chat-stop :added "4.0"}
(fact "stops the context runtime")

^{:refer code.ai.common/chat? :added "4.0"}
(fact "checks that an object is of type chat")

^{:refer code.ai.common/chat-create :added "4.0"}
(fact "creates a chat")

^{:refer code.ai.common/make-plan :added "4.0"}
(fact "creates a plan")