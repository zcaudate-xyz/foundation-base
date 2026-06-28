^{:seedgen/skip true}
(ns xt.substrate.walkthrough-js.s35-playground-test
  "Walkthrough test demonstrating js.react.ext-page against a substrate node
   running inside a browser page served by the `:playground` runtime.

   The `:playground` runtime starts an http-kit server that serves an HTML
   page with an embedded WebSocket eval client. A headless Chrome instance is
   navigated to that page, the substrate/React modules are scaffolded into the
   browser over the WebSocket, and then `!.js` forms drive substrate page
   models through `js.react.ext-page`."
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.lang.pointer :as ptr]
            [std.lib.component :as component]
            [xt.lang.common-notify :as notify]))

(require '[hara.runtime.js-playground :as js-playground]
         '[hara.runtime.chromedriver :as chromedriver])

(l/script- :js
  {:runtime :playground
   :config {:port 0}
   :test-mode true
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [hara.runtime.js-playground.client :as client]
             [js.react.ext-page :as ext-page]]
   :emit {:lang/jsx false}})

(defn.js create-node
  "creates the substrate node spec"
  {:added "4.1"}
  []
  (return {"id" "s35-node"
           "spaces" {"space/a" {"state" {}}}}))

(defn.js setup-node
  "creates a substrate node with a single page model"
  {:added "4.1"}
  []
  (var node (substrate/node-create (-/create-node)))
  (page-core/add-group-attach
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
  "reads the current output value of the page model via ext-page"
  {:added "4.1"}
  [node]
  (var model (ext-page/get-model node "space/a" ["page" "greet"]))
  (var output (event-model/get-current model nil))
  (return (:? (xt/x:nil? output) nil (. output ["value"]))))

(defn- wait-for-channel
  "waits up to 5s for the playground websocket channel to be connected"
  [rt]
  (let [channel (:channel rt)]
    (loop [i 0]
      (when (and (< i 50) (not @channel))
        (Thread/sleep 100)
        (recur (inc i))))))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (def +url+ (js-playground/play-url (l/rt :js)))
          (def +browser+ (chromedriver/browser {}))
          (chromedriver/goto +url+ 5000 +browser+)
          (wait-for-channel (l/rt :js))]
  :teardown [(l/rt:stop)
             (component/stop +browser+)]})

^{:refer xt.substrate.walkthrough-js.s35-playground-test/CANARY :adopt true :added "4.1"}
(fact "basic eval reaches the playground-served browser"

  (!.js (+ 1 2 3))
  => 6)

^{:refer xt.substrate.walkthrough-js.s35-playground-test/ext-page-renders-initial-output
  :added "4.1"}
(fact "ext-page can read the substrate model in a playground-served browser"

  (notify/wait-on [:js 5000]
    (var node (-/setup-node))
    (var model (ext-page/get-model node "space/a" ["page" "greet"]))
    (repl/notify model))
  => map?)

^{:refer xt.substrate.walkthrough-js.s35-playground-test/ext-page-follows-model-updates
  :added "4.1"}
(fact "ext-page follows model updates in a playground-served browser"

  (notify/wait-on [:js 5000]
    (var node (-/setup-node))
    (page-core/model-set-input node "space/a" "page" "greet" {"data" ["world"]} {})
    (-> (promise/x:with-delay 200
          (fn [] (return (-/model-output-value node))))
        (promise/x:promise-then
         (fn [v]
           (repl/notify v)))))
  => "world")


(comment
  
  
  (!.js
    (window.PLAYGROUND.setStage
     [:div "hello world"]))
  
  (!.js
    (+ 1 2 3))
  
  )
