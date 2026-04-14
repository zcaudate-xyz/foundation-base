(ns js.cell.runtime-test
  (:use code.test)
  (:require [clojure.string :as str]
            [js.cell.runtime.emit :as emit]
            [js.cell.runtime.link :as runtime-link]
            [std.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [js.cell.runtime.link :as runtime-link]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.cell.runtime.emit/node-script :added "4.0"}
(fact "emits the Node worker bootstrap script"
  ^:hidden
  (str/includes? (emit/node-script) "worker_threads")
  => true
  (str/includes? (emit/node-script) "parentPort")
  => true)

^{:refer js.cell.runtime.link/make-link :added "4.0"}
(fact "creates a mock worker"
  ^:hidden
  (!.js
   (var worker ((. (runtime-link/make-link "mock" nil {})
                   ["create-fn"])
                (fn [data] data)))
   (return (k/obj-keys worker)))
  => (contains ["::" "listeners" "postMessage" "postRequest"]))
