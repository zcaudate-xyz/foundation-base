(ns lib.docker.ryuk-test
  (:use code.test)
  (:require [lib.docker.ryuk :refer :all]
            [lib.docker.common :as common]
            [std.lib :as h]))

^{:refer lib.docker.common/CANARY :guard true :adopt true :added "4.0"}
(fact "executes a shell command"
  ^:hidden
  
  (common/raw-exec (concat ["docker" "ps"]
                           (when common/*host* ["--host" common/*host*])
                           ["--format" "{{json .}}"])
                   {})
  => coll?)


^{:refer lib.docker.ryuk/start-ryuk :added "4.0"}
(fact "starts the reaper"
  ^:hidden
  
  (start-ryuk)
  => map?)

^{:refer lib.docker.ryuk/stop-ryuk :added "4.0"}
(fact "stops the reaper"
  ^:hidden
  
  (stop-ryuk))

^{:refer lib.docker.ryuk/start-reaped :added "4.0"}
(fact "starts a reaped container"
  ^:hidden

  (start-reaped {:id     "test"
                 :image  "node:16"
                 :ports  [6379]
                 :cmd    ["node"]})
  => map?)

^{:refer lib.docker.ryuk/stop-all-reaped :added "4.0"}
(fact "stops all reaped"
  ^:hidden
  
  (stop-all-reaped)
  => (any nil? h/wrapped?))
