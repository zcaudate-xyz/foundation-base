(ns js.worker.emit-test
  (:use code.test)
  (:require [clojure.string :as str]
            [js.worker.emit :as emit]))

^{:refer js.worker.emit/emit-worker-script :added "4.1"}
(fact "emits worker scripts from forms"
  (emit/emit-worker-script '[(+ 1 2)] :flat)
  => "1 + 2;")

^{:refer js.worker.emit/webworker-forms :added "4.1"}
(fact "returns the WebWorker runtime entry form"
  (emit/webworker-forms {"node" {"id" "worker-a"}})
  => '[(:= (. globalThis ["__JS_WORKER_WEBWORKER_OPTS"]) {"node" {"id" "worker-a"}})
       (var config (or (. globalThis ["__JS_WORKER_WEBWORKER_OPTS"]) {}))
       (var nodeOpts (or (. config ["node"]) {}))
       (var dbOpts (or (. config ["db-node"])
                       (. config ["db_node"])
                       {}))
       (var transportId (or (. config ["transport_id"])
                            "host"))
       (var node (xt.event.node/node-create nodeOpts))
       (xt.db.node/install node dbOpts)
       (xt.event.node/attach-transport
        node
        transportId
        (xt.event.node-transport-browser/self-endpoint self))
       node])

^{:refer js.worker.emit/webworker-script :added "4.1"}
(fact "emits the WebWorker bootstrap script with runtime config"
  (str/includes? (emit/webworker-script {"node" {"id" "worker-a"}
                                         "db-node" {"schema" {"Order" {}}}})
                 "worker-a")
  => true

  (str/includes? (emit/webworker-script {"node" {"id" "worker-a"}})
                 "attach_transport")
  => true)

^{:refer js.worker.emit/sharedworker-forms :added "4.1"}
(fact "returns the SharedWorker runtime entry form"
  (emit/sharedworker-forms)
  => '[(js.worker.env-sharedworker/runtime-init)])

^{:refer js.worker.emit/sharedworker-script :added "4.1"}
(fact "emits the SharedWorker bootstrap script"
  (str/includes? (emit/sharedworker-script) "onconnect")
  => true)

^{:refer js.worker.emit/node-forms :added "4.1"}
(fact "returns the Node runtime entry form"
  (emit/node-forms)
  => '[(js.worker.env-node/runtime-init)])

^{:refer js.worker.emit/node-script :added "4.1"}
(fact "emits the Node bootstrap script"
  (str/includes? (emit/node-script) "worker_threads")
  => true

  (str/includes? (emit/node-script) "parentPort")
  => true)
