(ns js.cell.runtime-test
  (:use code.test)
  (:require [clojure.string :as str]
            [js.cell.runtime.env-node :as env-node]
            [js.cell.runtime.link :as runtime-link]
            [std.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.core :as j]
             [js.cell.kernel :as cl]
             [js.cell.runtime.env-node :as env-node]
             [js.cell.runtime.link :as runtime-link]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.cell.runtime.env-node/forms :added "4.0"}
(fact "returns the Node worker bootstrap forms"
  ^:hidden
  (!.js (env-node/forms))
  => vector?)

^{:refer js.cell.runtime.env-node/script :added "4.0"}
(fact "emits the Node worker bootstrap script"
  ^:hidden
  (str/includes? (!.js (env-node/script)) "worker_threads")
  => true
  (str/includes? (!.js (env-node/script)) "parentPort")
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
