(ns lib.docker.ryuk-test
  (:require [lib.docker.common :as common]
             [lib.docker.ryuk :refer :all]
             [std.lib.component :as component]
             [std.lib.env :as env]
             [std.lib.foundation :as f])
  (:use code.test))

^{:refer lib.docker.common/CANARY :guard true :adopt true :added "4.0"}
(fact "executes a shell command"

  (common/raw-exec (concat ["docker" "ps"]
                           (when common/*host* ["--host" common/*host*])
                           ["--format" "{{json .}}"])
                   {})
  => coll?)


^{:refer lib.docker.ryuk/start-ryuk :added "4.0"}
(fact "starts the reaper"

  (start-ryuk)
  => map?)

^{:refer lib.docker.ryuk/stop-ryuk :added "4.0"}
(fact "stops the reaper"

  (let [stopped  (atom [])
        original *ryuk*]
    (try
      (alter-var-root #'*ryuk* (constantly {:relay :relay :socket :socket}))
      (with-redefs [common/stop-container (fn [_] :stopped)
                    component/stop (fn [relay] (swap! stopped conj [:relay relay]))
                    env/close (fn [socket] (swap! stopped conj [:socket socket]))]
        [(stop-ryuk) @stopped *ryuk*])
      (finally
        (alter-var-root #'*ryuk* (constantly original)))))
  => [nil [[:relay :relay] [:socket :socket]] nil])

^{:refer lib.docker.ryuk/start-reaped :added "4.0"}
(fact "starts a reaped container"

  (start-reaped {:id     "test"
                 :image  "node:16"
                 :ports  [6379]
                 :cmd    ["node"]})
  => map?)

^{:refer lib.docker.ryuk/stop-all-reaped :added "4.0"}
(fact "stops all reaped"

  (stop-all-reaped)
  => (any nil? f/wrapped?))
