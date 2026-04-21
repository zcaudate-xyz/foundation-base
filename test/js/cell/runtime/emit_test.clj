(ns js.cell.runtime.emit-test
  (:use code.test)
  (:require [clojure.string :as str]
            [js.cell.runtime.emit :as emit]))

^{:refer js.cell.runtime.emit/emit-worker-script :added "4.1"}
(fact "emits worker scripts from forms"
  (emit/emit-worker-script '[(+ 1 2)] :flat)
  => "1 + 2;")

^{:refer js.cell.runtime.emit/webworker-forms :added "4.1"}
(fact "returns the WebWorker runtime entry form"
  (emit/webworker-forms)
  => '[(js.cell.runtime.env-webworker/runtime-init)])

^{:refer js.cell.runtime.emit/webworker-script :added "4.1"}
(fact "emits the WebWorker bootstrap script"
  (str/includes? (emit/webworker-script) "self")
  => true)

^{:refer js.cell.runtime.emit/sharedworker-forms :added "4.1"}
(fact "returns the SharedWorker runtime entry form"
  (emit/sharedworker-forms)
  => '[(js.cell.runtime.env-sharedworker/runtime-init)])

^{:refer js.cell.runtime.emit/sharedworker-script :added "4.1"}
(fact "emits the SharedWorker bootstrap script"
  (str/includes? (emit/sharedworker-script) "onconnect")
  => true)

^{:refer js.cell.runtime.emit/node-forms :added "4.1"}
(fact "returns the Node runtime entry form"
  (emit/node-forms)
  => '[(js.cell.runtime.env-node/runtime-init)])

^{:refer js.cell.runtime.emit/node-script :added "4.1"}
(fact "emits the Node bootstrap script"
  (str/includes? (emit/node-script) "worker_threads")
  => true

  (str/includes? (emit/node-script) "parentPort")
  => true)
