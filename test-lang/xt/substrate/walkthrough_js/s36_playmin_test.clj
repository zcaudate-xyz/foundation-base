^{:seedgen/skip true}
(ns xt.substrate.walkthrough-js.s35-playmin-test
  "Walkthrough test demonstrating js.react.ext-page against a substrate node
   running inside a browser page served by the `:playground` runtime.

   The `:playground` runtime starts an http-kit server that serves an HTML
   page with an embedded WebSocket eval client. A headless Chrome instance is
   navigated to that page, the substrate/React modules are scaffolded into the
   browser over the WebSocket, and then `!.js` forms drive substrate page
   models through `js.react.ext-page`."
  (:use code.test)
  (:require [hara.lang :as l]
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
             [js.react :as r]
             [hara.runtime.js-playground.client :as client]]
   :emit {:lang/jsx false}})



(fact:global
 {:setup [(l/rt:restart :js)
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




(comment
  
  
  (!.js
    (window.PLAYGROUND.setStage
     [:div "hello world"]))
  
  (!.js
    (+ 1 2 3))
  
  )
