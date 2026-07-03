^{:seedgen/skip true}
(ns xt.substrate.walkthrough-js.s33-react-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [js.react.ext-page :as ext-page]]})

(defn.js create-node
  []
  (return {"id" "s33-node"
           "spaces" {"space/a" {"state" {}}}}))

(defn.js setup-node
  []
  (var node (substrate/node-create (-/create-node)))
  (page-core/group-add-attach
   node
   "space/a"
   "page"
   {"greet" {"handler" (fn [ctx]
                          (var args (. ctx ["args"]))
                          (return {"value" (xt/x:get-idx args 0)}))
             "defaults" {"args" ["hello"]}
             "options" {"trigger" true}}})
  (return node))

(defn.js model-output-value
  [node]
  (var model (ext-page/get-model node "space/a" ["page" "greet"]))
  (var output (event-model/get-current model nil))
  (return (. output ["value"])))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.walkthrough-js.s33-react-test/ext-page-renders-initial-output
  :added "4.1"}
(fact "ext-page can read the substrate model and render the initial output"

  (notify/wait-on [:js 5000]
    (var node (-/setup-node))
    (-> (page-core/model-set-input node "space/a" "page" "greet" {"data" ["hello"]} {})
        (promise/x:promise-then
         (fn [_]
           (repl/notify (-/model-output-value node))))))
  => "hello")

^{:refer xt.substrate.walkthrough-js.s33-react-test/ext-page-follows-model-updates
  :added "4.1"}
(fact "ext-page follows model updates from substrate"

  (notify/wait-on [:js 5000]
    (var node (-/setup-node))
    (-> (page-core/model-set-input node "space/a" "page" "greet" {"data" ["world"]} {})
        (promise/x:promise-then
         (fn [_]
           (repl/notify (-/model-output-value node))))))
  => "world")
