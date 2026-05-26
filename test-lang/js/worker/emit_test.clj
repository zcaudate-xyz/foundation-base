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
       (var node (xt.substrate/node-create nodeOpts))
       (xt.db.node/install node dbOpts)
       (xt.substrate/attach-transport
        node
        transportId
        (xt.substrate.transport-browser/self-endpoint self))
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
