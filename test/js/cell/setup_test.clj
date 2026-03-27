(ns js.cell.setup-test
  (:use code.test)
  (:require [js.cell.setup :as setup]
            [std.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell.kernel :as cl]
             [js.cell.setup.runtime :as runtime]]})

^{:refer js.cell.setup/webworker-script :added "4.0"}
(fact "emits the WebWorker bootstrap script"
  ^:hidden
  (setup/webworker-script)
  => string?

  (setup/sharedworker-script)
  => string?

  (setup/node-script)
  => string?)

^{:refer js.cell.setup/runtime-script :added "4.0"}
(fact "dispatches runtime bootstrap scripts"
  ^:hidden
  (setup/runtime-script :webworker)
  => string?

  (setup/runtime-script :sharedworker)
  => string?

  (setup/runtime-script :node)
  => string?)

^{:refer js.cell.setup.runtime/make-link :added "4.0"}
(fact "creates a mock worker"
  ^:hidden
  (!.js
   (var worker ((. (runtime/make-link "mock" nil {})
                   ["create-fn"])
                (fn [data] data)))
   (return (k/obj-keys worker)))
  => (contains ["::" "listeners" "postMessage" "postRequest"]))

^{:refer js.cell.kernel/make-cell :added "4.0"}
(fact "creates a kernel cell from the mock worker"
  ^:hidden
  (!.js
   (var cell (cl/make-cell (runtime/make-link "mock" nil {})))
   (return cell))
  => (contains-in {"::" "cell"
                   "link" {"::" "cell.link"}
                   "models" {}}))
