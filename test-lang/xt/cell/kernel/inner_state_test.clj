(ns xt.cell.kernel.inner-state-test
  (:require [hara.lang              :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(do (l/script- :xtalk
      {:require [[xt.lang.spec-base :as xt]
                 [xt.lang.common-data :as xtd]
                 [xt.cell.kernel.inner-state :as inner-state]
                 [xt.cell.kernel.inner-local :as inner-local]
                 [xt.cell.kernel.inner-mock :as inner-mock]]})
    (defn.xt reset-inner-state
      []
      (inner-state/INNER_STATE-reset {"eval" true})
      (inner-state/INNER_ACTIONS-reset {})
      (return true)))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.cell.kernel.inner-state :as inner-state]
             [xt.cell.kernel.inner-local :as inner-local]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.cell.kernel.inner-state :as inner-state]
             [xt.cell.kernel.inner-local :as inner-local]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.cell.kernel.inner-state :as inner-state]
             [xt.cell.kernel.inner-local :as inner-local]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.inner-state/INNER_STATE :added "4.0"}
(fact "gets inner state"

  (!.js
    (-/reset-inner-state)
    (inner-state/INNER_STATE))
  => {"eval" true}

  (!.lua
    (-/reset-inner-state)
    (inner-state/INNER_STATE))
  => {"eval" true}

  (!.py
    (-/reset-inner-state)
    (inner-state/INNER_STATE))
  => {"eval" true})

^{:refer xt.cell.kernel.inner-state/INNER_ACTIONS :added "4.0"}
(fact "gets inner actions"

  (!.js
    (-/reset-inner-state)
    (inner-state/INNER_ACTIONS))
  => {}

  (!.lua
    (-/reset-inner-state)
    (inner-state/INNER_ACTIONS))
  => {}

  (!.py
    (-/reset-inner-state)
    (inner-state/INNER_ACTIONS))
  => {})

^{:refer xt.cell.kernel.inner-state/get-state :added "4.0"}
(fact "gets cell state"

  (!.js
    (-/reset-inner-state)
    (inner-state/get-state nil))
  => {"eval" true}

  (!.js
    (-/reset-inner-state)
    (inner-state/get-state {}))
  => {"eval" true}

  (!.lua
    (-/reset-inner-state)
    (inner-state/get-state nil))
  => {"eval" true}

  (!.lua
    (-/reset-inner-state)
    (inner-state/get-state {}))
  => {"eval" true}

  (!.py
    (-/reset-inner-state)
    (inner-state/get-state nil))
  => {"eval" true}

  (!.py
    (-/reset-inner-state)
    (inner-state/get-state {}))
  => {"eval" true})

^{:refer xt.cell.kernel.inner-state/get-actions :added "4.0"}
(fact "gets inner actions from the inner or singleton"

  (!.js
    (-/reset-inner-state)
    (inner-state/get-actions nil))
  => {}

  (!.js
    (inner-state/get-actions {"actions" {"@test/action" {}}}))
  => {"@test/action" {}}

  (!.lua
    (-/reset-inner-state)
    (inner-state/get-actions nil))
  => {}

  (!.lua
    (inner-state/get-actions {"actions" {"@test/action" {}}}))
  => {"@test/action" {}}

  (!.py
    (-/reset-inner-state)
    (inner-state/get-actions nil))
  => {}

  (!.py
    (inner-state/get-actions {"actions" {"@test/action" {}}}))
  => {"@test/action" {}})

^{:refer xt.cell.kernel.inner-state/set-actions :added "4.0"}
(fact "sets actions on a inner or in global state"

  (!.js
    (-/reset-inner-state)
    (var inner {})
    (inner-state/set-actions {"@test/action" {}} inner)
    (xt/x:get-key inner "actions"))
  => {"@test/action" {}}

  (!.js
    (-/reset-inner-state)
    (inner-state/set-actions {"@global/action" {}} nil)
    (xt/x:has-key? (inner-state/INNER_ACTIONS) "@global/action"))
  => true

  (!.lua
    (-/reset-inner-state)
    (var inner {})
    (inner-state/set-actions {"@test/action" {}} inner)
    (xt/x:get-key inner "actions"))
  => {"@test/action" {}}

  (!.lua
    (-/reset-inner-state)
    (inner-state/set-actions {"@global/action" {}} nil)
    (xt/x:has-key? (inner-state/INNER_ACTIONS) "@global/action"))
  => true

  (!.py
    (-/reset-inner-state)
    (var inner {})
    (inner-state/set-actions {"@test/action" {}} inner)
    (xt/x:get-key inner "actions"))
  => {"@test/action" {}}

  (!.py
    (-/reset-inner-state)
    (inner-state/set-actions {"@global/action" {}} nil)
    (xt/x:has-key? (inner-state/INNER_ACTIONS) "@global/action"))
  => true)

^{:refer xt.cell.kernel.inner-state/fn-trigger :added "4.0"}
(fact "triggers an event on a inner"

  (!.js
    (var messages [])
    (inner-state/fn-trigger
     (inner-mock/mock-worker (fn [msg] (messages.push msg)))
     "stream"
     "hello"
     "ok"
     {:a 1})
    [(xt/x:get-key (xtd/first messages) "signal")
     (xt/x:get-key (xt/x:get-key (xtd/first messages) "body") "a")])
  => ["hello" 1]

  (!.lua
    (var messages [])
    (inner-state/fn-trigger
     (inner-mock/mock-worker (fn [msg] (xt/x:arr-push messages msg)))
     "stream"
     "hello"
     "ok"
     {:a 1})
    [(xt/x:get-key (xtd/first messages) "signal")
     (xt/x:get-key (xt/x:get-key (xtd/first messages) "body") "a")])
  => ["hello" 1]

  (!.py
    (var messages [])
    (inner-state/fn-trigger
     (inner-mock/mock-worker (fn [msg] (xt/x:arr-push messages msg)))
     "stream"
     "hello"
     "ok"
     {:a 1})
    [(xt/x:get-key (xtd/first messages) "signal")
     (xt/x:get-key (xt/x:get-key (xtd/first messages) "body") "a")])
  => ["hello" 1])

^{:refer xt.cell.kernel.inner-state/fn-trigger-async :added "4.0"}
(fact "triggers an event after a delay"

  (notify/wait-on :js
    (inner-state/fn-trigger-async
     (inner-mock/mock-worker
      (fn [msg]
        (repl/notify [(xt/x:get-key msg "signal")
                      (xt/x:get-key (xt/x:get-key msg "body") "a")])))
     "stream"
     "hello"
     "ok"
     {:a 1}
     50))
  => ["hello" 1]

  (notify/wait-on :lua
    (inner-state/fn-trigger-async
     (inner-mock/mock-worker
      (fn [msg]
        (repl/notify [(xt/x:get-key msg "signal")
                      (xt/x:get-key (xt/x:get-key msg "body") "a")])))
     "stream"
     "hello"
     "ok"
     {:a 1}
     50))
  => ["hello" 1]

  (notify/wait-on :python
    (inner-state/fn-trigger-async
     (inner-mock/mock-worker
      (fn [msg]
        (repl/notify [(xt/x:get-key msg "signal")
                      (xt/x:get-key (xt/x:get-key msg "body") "a")])))
     "stream"
     "hello"
     "ok"
     {:a 1}
     50))
  => ["hello" 1])

^{:refer xt.cell.kernel.inner-state/fn-set-state :added "4.0"}
(fact "updates inner state and can emit state events"

  (!.js
    (-/reset-inner-state)
    (inner-state/fn-set-state
     (inner-mock/mock-worker (fn [msg]))
     (inner-state/INNER_STATE)
     (fn [state]
       (xt/x:set-key state "final" false))
     true))
  => {"eval" true "final" false}

  (!.js
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-state
     (inner-mock/mock-worker (fn [msg] (messages.push msg)))
     (inner-state/INNER_STATE)
     (fn [state]
       (xt/x:set-key state "final" false))
     false)
    (xtd/first messages))
  => {"body" {"eval" true "final" false}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"}

  (!.lua
    (-/reset-inner-state)
    (inner-state/fn-set-state
     (inner-mock/mock-worker (fn [msg]))
     (inner-state/INNER_STATE)
     (fn [state]
       (xt/x:set-key state "final" false))
     true))
  => {"eval" true "final" false}

  (!.lua
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-state
     (inner-mock/mock-worker (fn [msg] (xt/x:arr-push messages msg)))
     (inner-state/INNER_STATE)
     (fn [state]
       (xt/x:set-key state "final" false))
     false)
    (xtd/first messages))
  => {"body" {"eval" true "final" false}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"}

  (!.py
    (-/reset-inner-state)
    (inner-state/fn-set-state
     (inner-mock/mock-worker (fn [msg]))
     (inner-state/INNER_STATE)
     (fn [state]
       (xt/x:set-key state "final" false))
     true))
  => {"eval" true "final" false}

  (!.py
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-state
     (inner-mock/mock-worker (fn [msg] (xt/x:arr-push messages msg)))
     (inner-state/INNER_STATE)
     (fn [state]
       (xt/x:set-key state "final" false))
     false)
    (xtd/first messages))
  => {"body" {"eval" true "final" false}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"})

^{:refer xt.cell.kernel.inner-state/fn-set-final-status :added "4.0"}
(fact "sets the inner state to final"

  (!.js
    (-/reset-inner-state)
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg]))
     true))
  => {"eval" true "final" true}

  (!.js
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg] (messages.push msg)))
     false)
    (xtd/first messages))
  => {"body" {"eval" true "final" true}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"}

  (!.lua
    (-/reset-inner-state)
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg]))
     true))
  => {"eval" true "final" true}

  (!.lua
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg] (xt/x:arr-push messages msg)))
     false)
    (xtd/first messages))
  => {"body" {"eval" true "final" true}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"}

  (!.py
    (-/reset-inner-state)
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg]))
     true))
  => {"eval" true "final" true}

  (!.py
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg] (xt/x:arr-push messages msg)))
     false)
    (xtd/first messages))
  => {"body" {"eval" true "final" true}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"})

^{:refer xt.cell.kernel.inner-state/fn-get-final-status :added "4.0"}
(fact "gets the final status"

  (!.js
    (-/reset-inner-state)
    (inner-state/fn-get-final-status nil))
  => nil

  (!.js
    (-/reset-inner-state)
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg]))
     true)
    (inner-state/fn-get-final-status nil))
  => true

  (!.lua
    (-/reset-inner-state)
    (inner-state/fn-get-final-status nil))
  => nil

  (!.lua
    (-/reset-inner-state)
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg]))
     true)
    (inner-state/fn-get-final-status nil))
  => true

  (!.py
    (-/reset-inner-state)
    (inner-state/fn-get-final-status nil))
  => nil

  (!.py
    (-/reset-inner-state)
    (inner-state/fn-set-final-status
     (inner-mock/mock-worker (fn [msg]))
     true)
    (inner-state/fn-get-final-status nil))
  => true)

^{:refer xt.cell.kernel.inner-state/fn-set-eval-status :added "4.0"}
(fact "sets the eval status"

  (!.js
    (-/reset-inner-state)
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg]))
     false
     true))
  => {"eval" false}

  (!.js
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg] (messages.push msg)))
     false
     false)
    (xtd/first messages))
  => {"body" {"eval" false}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"}

  (!.lua
    (-/reset-inner-state)
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg]))
     false
     true))
  => {"eval" false}

  (!.lua
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg] (xt/x:arr-push messages msg)))
     false
     false)
    (xtd/first messages))
  => {"body" {"eval" false}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"}

  (!.py
    (-/reset-inner-state)
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg]))
     false
     true))
  => {"eval" false}

  (!.py
    (-/reset-inner-state)
    (var messages [])
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg] (xt/x:arr-push messages msg)))
     false
     false)
    (xtd/first messages))
  => {"body" {"eval" false}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::STATE"})

^{:refer xt.cell.kernel.inner-state/fn-get-eval-status :added "4.0"}
(fact "gets the eval status"

  (!.js
    (-/reset-inner-state)
    (inner-state/fn-get-eval-status))
  => true

  (!.js
    (-/reset-inner-state)
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg]))
     false
     true)
    (inner-state/fn-get-eval-status))
  => false

  (!.lua
    (-/reset-inner-state)
    (inner-state/fn-get-eval-status))
  => true

  (!.lua
    (-/reset-inner-state)
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg]))
     false
     true)
    (inner-state/fn-get-eval-status))
  => false

  (!.py
    (-/reset-inner-state)
    (inner-state/fn-get-eval-status))
  => true

  (!.py
    (-/reset-inner-state)
    (inner-state/fn-set-eval-status
     (inner-mock/mock-worker (fn [msg]))
     false
     true)
    (inner-state/fn-get-eval-status))
  => false)

^{:refer xt.cell.kernel.inner-state/fn-get-action-list :added "4.0"}
(fact "lists registered actions"

  (!.js
    (-/reset-inner-state)
    (inner-local/actions-init {} nil)
    (xt/x:len (inner-state/fn-get-action-list)))
  => integer?

  (!.lua
    (-/reset-inner-state)
    (inner-local/actions-init {} nil)
    (xt/x:len (inner-state/fn-get-action-list)))
  => integer?

  (!.py
    (-/reset-inner-state)
    (inner-local/actions-init {} nil)
    (xt/x:len (inner-state/fn-get-action-list)))
  => integer?)

^{:refer xt.cell.kernel.inner-state/fn-get-action-entry :added "4.0"}
(fact "gets a registered action entry"

  (!.js
    (-/reset-inner-state)
    (inner-local/actions-init {} nil)
    (inner-state/fn-get-action-entry "@cell/ping"))
  => (contains {"static" true "is_async" false "args" []})

  (!.lua
    (-/reset-inner-state)
    (inner-local/actions-init {} nil)
    (xt/x:get-key (inner-state/fn-get-action-entry "@cell/ping") "is_async"))
  => false

  (!.py
    (-/reset-inner-state)
    (inner-local/actions-init {} nil)
    (xt/x:get-key (inner-state/fn-get-action-entry "@cell/ping") "args"))
  => [])

^{:refer xt.cell.kernel.inner-state/fn-ping :added "4.0"}
(fact "pings the inner"

  (!.js
    (inner-state/fn-ping))
  => (contains ["pong" integer?])

  (!.lua
    (inner-state/fn-ping))
  => (contains ["pong" integer?])

  (!.py
    (inner-state/fn-ping))
  => (contains ["pong" integer?]))

^{:refer xt.cell.kernel.inner-state/fn-ping-async :added "4.0"}
(fact "pings after a delay"

  (j/<! (inner-state/fn-ping-async 10))
  => (contains ["pong" integer?]))

^{:refer xt.cell.kernel.inner-state/fn-echo :added "4.0"}
(fact "echos the first arg"

  (!.js
    (inner-state/fn-echo "hello"))
  => (contains ["hello" integer?])

  (!.lua
    (inner-state/fn-echo "hello"))
  => (contains ["hello" integer?])

  (!.py
    (inner-state/fn-echo "hello"))
  => (contains ["hello" integer?]))

^{:refer xt.cell.kernel.inner-state/fn-echo-async :added "4.0"}
(fact "echos after a delay"

  (j/<! (inner-state/fn-echo-async "hello" 10))
  => (contains ["hello" integer?]))

^{:refer xt.cell.kernel.inner-state/fn-error :added "4.0"}
(fact "throws an error"

  (!.js
    (inner-state/fn-error))
  => (throws)

  (!.lua
    (inner-state/fn-error))
  => (throws)

  (!.py
    (inner-state/fn-error))
  => (throws))

^{:refer xt.cell.kernel.inner-state/fn-error-async :added "4.0"}
(fact "throws an async error"

  (j/<! (. (inner-state/fn-error-async 10)
           (catch j/identity)))
  => (contains ["error"]))

(comment
  (s/snapto '[xt.cell.kernel.inner-state])

  (s/run '[xt.cell.kernel.inner-state])
  
  (s/seedgen-langadd '[xt.cell.kernel.inner-state] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.cell.kernel.inner-state] {:lang [:lua :python] :write true}))
