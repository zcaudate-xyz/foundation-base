(ns xt.cell.kernel.inner-mock-test
  (:require [hara.lang              :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.inner-mock/mock-worker-send :added "4.0"}
(fact "sends a request to the mock worker"

  (!.js
   (var worker (inner-mock/mock-worker (fn [msg])))
   (inner-mock/mock-worker-send worker {"op" "eval" "body" "1 + 1"}))
  => nil?

  (!.lua
   (var worker (inner-mock/mock-worker (fn [msg])))
   (inner-mock/mock-worker-send worker {"op" "eval" "body" "1 + 1"}))
  => nil?

  (!.py
   (var worker (inner-mock/mock-worker (fn [msg])))
   (inner-mock/mock-worker-send worker {"op" "eval" "body" "1 + 1"}))
  => nil?)

^{:refer xt.cell.kernel.inner-mock/mock-worker :added "4.0"}
(fact "creates a mock worker"

  (!.js
   (var messages [])
   (var worker (inner-mock/mock-worker
                (fn [msg] (messages.push msg))))
   (worker.postMessage {"test" 1})
   messages)
  => [{"test" 1}]

  (!.lua
   (var messages [])
   (var worker (inner-mock/mock-worker
                (fn [msg] (xt/x:arr-push messages msg))))
    ((xt/x:get-key worker "postMessage") {"test" 1})
   messages)
  => [{"test" 1}]

  (!.py
   (var messages [])
   (var worker (inner-mock/mock-worker
                (fn [msg] (xt/x:arr-push messages msg))))
    ((xt/x:get-key worker "postMessage") {"test" 1})
   messages)
  => [{"test" 1}])

^{:refer xt.cell.kernel.inner-mock/create-worker :added "4.0"}
(fact "initialises the mock worker"

  (!.js
   (var worker (inner-mock/create-worker
                (fn [msg])
                {}
                true))
   (xt/x:get-key worker "::"))
  => "worker.mock"

  (!.js
   (var worker (inner-mock/create-worker k/identity {} true))
   (k/is-function? (xt/x:get-key worker "postMessage")))
  => true

  (notify/wait-on :js
    (inner-mock/create-worker (repl/>notify) {} false))
  => {"body" {"done" true}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::INIT"}

  (!.lua
   (var worker (inner-mock/create-worker
                (fn [msg])
                {}
                true))
   (xt/x:get-key worker "::"))
  => "worker.mock"

  (!.lua
   (var worker (inner-mock/create-worker k/identity {} true))
   (k/is-function? (xt/x:get-key worker "postMessage")))
  => true

  (notify/wait-on :lua
    (inner-mock/create-worker (repl/>notify) {} false))
  => {"body" {"done" true}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::INIT"}

  (!.py
   (var worker (inner-mock/create-worker
                (fn [msg])
                {}
                true))
   (xt/x:get-key worker "::"))
  => "worker.mock"

  (!.py
   (var worker (inner-mock/create-worker k/identity {} true))
   (k/is-function? (xt/x:get-key worker "postMessage")))
  => true

  (notify/wait-on :python
    (inner-mock/create-worker (repl/>notify) {} false))
  => {"body" {"done" true}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::INIT"})

(comment
  (s/snapto '[xt.cell.kernel.inner-mock])
  
  (s/seedgen-langadd '[xt.cell.kernel.inner-mock] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.cell.kernel.inner-mock] {:lang [:lua :python] :write true}))
