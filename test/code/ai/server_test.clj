(ns code.ai.server-test
  (:use code.test)
  (:require [code.ai.server :refer :all]))

^{:refer code.ai.server/get-channels :added "4.0"}
(fact "gets the active channels")

^{:refer code.ai.server/send-to-clients :added "4.0"}
(fact "sends a message to all clients")

^{:refer code.ai.server/handle-relay-output :added "4.0"}
(fact "handles output from a relay")

^{:refer code.ai.server/create-relay-instance :added "4.0"}
(fact "creates a relay instance")

^{:refer code.ai.server/stop-relay-instance :added "4.0"}
(fact "stops a relay instance")

^{:refer code.ai.server/get-relay-info :added "4.0"}
(fact "gets information about a relay")

^{:refer code.ai.server/ws-handler :added "4.0"}
(fact "handles websocket connections")

^{:refer code.ai.server/serve-resource :added "4.0"}
(fact "serves a static resource")

^{:refer code.ai.server/handler :added "4.0"}
(fact "handles http requests")

^{:refer code.ai.server/start-server :added "4.0"}
(fact "Starts the HTTP server")

^{:refer code.ai.server/stop-server :added "4.0"}
(fact "Stops the HTTP server")