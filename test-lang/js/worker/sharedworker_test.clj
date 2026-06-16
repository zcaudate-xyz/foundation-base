(ns js.worker.sharedworker-test
  (:use code.test)
  (:require [clojure.string :as str]
            [js.worker.sharedworker :as sharedworker]))

^{:refer js.worker.sharedworker/script :added "4.1"}
(fact "emits a SharedWorker bootstrap script around a supplied node init form"
  (str/includes? (sharedworker/script
                  {"shared_key" "__demo_shared__"
                   "transport_prefix" "tab-"
                   "ready" {"signal" "ready"
                            "worker" "demo"}
                   "node_init" '(xt.db.node/create {"node_id" "demo"})})
                 "onconnect")
  => true

  (str/includes? (sharedworker/script
                  {"shared_key" "__demo_shared__"
                   "transport_prefix" "tab-"
                   "ready" {"signal" "ready"
                            "worker" "demo"}
                   "node_init" '(xt.db.node/create {"node_id" "demo"})})
                 "__demo_shared__")
  => true

  (str/includes? (sharedworker/script
                  {"shared_key" "__demo_shared__"
                   "transport_prefix" "tab-"
                   "ready" {"signal" "ready"
                            "worker" "demo"}
                   "node_init" '(xt.db.node/create {"node_id" "demo"})})
                 "tab-")
  => true)

^{:refer js.worker.sharedworker/script :added "4.1"}
(fact "requires a node init form"
  (sharedworker/script {})
  => (throws))


^{:refer js.worker.sharedworker/request :added "4.1"}
(fact "TODO")

^{:refer js.worker.sharedworker/connect :added "4.1"}
(fact "TODO")

^{:refer js.worker.sharedworker/disconnect :added "4.1"}
(fact "TODO")

^{:refer js.worker.sharedworker/ensure-model :added "4.1"}
(fact "TODO")

^{:refer js.worker.sharedworker/query-view :added "4.1"}
(fact "TODO")

^{:refer js.worker.sharedworker/open-tab :added "4.1"}
(fact "TODO")

^{:refer js.worker.sharedworker/node-summary :added "4.1"}
(fact "TODO")