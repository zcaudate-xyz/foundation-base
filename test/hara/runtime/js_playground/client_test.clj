(ns hara.runtime.js-playground.client-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[hara.runtime.js-playground.client :as client]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]
   :test-mode true
   :emit {:lang/jsx false}})

(fact:global
 {:setup [(l/rt:restart :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})


^{:refer hara.runtime.js-playground.client/format-body :added "4.1"}
(fact "TODO"
  
  (!.js
    (client/format-body "hello")))

^{:refer hara.runtime.js-playground.client/send-response :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/eval-body :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/make-add-message :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/make-tab-id :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/find-tab-index :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/has-tab? :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/run-eval :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/make-websocket :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/MessageItem :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/MessageList :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/TopMenu :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/TabBar :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/ActiveTabPanel :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/get-config :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/App :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.js-playground.client/mount! :added "4.1"}
(fact "TODO")
