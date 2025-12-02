(ns js.cell.base-fn-mock-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [xt.lang.util-throttle :as th]
             [js.core :as j]
             [js.cell.base-fn :as base-fn]
             [js.cell.base-internal :as base-internal]
             [js.cell.link-fn :as link-fn]
             [js.cell.link-raw :as link-raw]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.base-fn/CELL_STATE :adopt true :added "4.0" :unchecked true
  :setup [(base-fn/CELL_STATE-reset {:eval true})]}
(fact "gets worker state"

  (base-fn/CELL_STATE)
  => map?)

^{:refer js.cell.base-fn/CELL_ROUTES :adopt true :added "4.0" :unchecked true}
(fact "gets worker routes"

  (base-fn/CELL_ROUTES)
  => map?)

^{:refer js.cell.base-fn/fn-trigger :adopt true :added "4.0" :unchecked true}
(fact "triggers an event"
  ^:hidden
  
  (notify/wait-on :js
    (base-fn/fn-trigger
     (base-internal/new-mock (repl/>notify))
     "stream"
     "hello"
     "ok"
     {:a 1}))
  => {"body" {"a" 1},
      "status" "ok",
      "op" "stream",
      "topic" "hello"})

^{:refer js.cell.base-fn/fn-trigger-async :adopt true :added "4.0" :unchecked true}
(fact "triggers an event after a delay"
  ^:hidden

  (notify/wait-on :js
    (base-fn/fn-trigger-async
     (base-internal/new-mock (repl/>notify))
     "stream"
     "hello"
     "ok"
     {:a 1}
     100))
  => {"body" {"a" 1}, "status" "ok", "op" "stream", "topic" "hello"})

^{:refer js.cell.base-fn/fn-set-state :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/CELL_STATE)
                     {:final false}))]}
(fact "helper to set the state and emit event"
  ^:hidden

  (notify/wait-on :js
    (base-fn/fn-set-state
     (base-internal/new-mock (repl/>notify))
     (base-fn/CELL_STATE)
     (fn [])
     false))
  => {"body" {"eval" true, "final" false},
      "status" "ok",
      "op" "stream",
      "topic" "@/::STATE"})

^{:refer js.cell.base-fn/fn-final-set :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/CELL_STATE)
                     {:final false}))]}
(fact "sets the worker state to final"
  ^:hidden
  
  (notify/wait-on :js
    (base-fn/fn-final-set
     (base-internal/new-mock (repl/>notify))))
  => {"body" {"eval" true, "final" true},
      "status" "ok",
      "op" "stream",
      "topic" "@/::STATE"})

^{:refer js.cell.base-fn/fn-final-status :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/CELL_STATE)
                     {:final false}))]}
(fact "gets the final status"
  ^:hidden

  (!.js (base-fn/fn-final-status))
  => false)

^{:refer js.cell.base-fn/fn-eval-enable :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/CELL_STATE)
                     {:eval true}))]}
(fact "enables eval"
  ^:hidden

  (notify/wait-on :js
    (base-fn/fn-eval-enable
     (base-internal/new-mock (repl/>notify))))
  => {"body" {"eval" true, "final" false},
      "status" "ok",
      "op" "stream",
      "topic" "@/::STATE"})

^{:refer js.cell.base-fn/fn-eval-disable :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/CELL_STATE)
                     {:eval true}))]}
(fact "disables eval"
  ^:hidden

  (notify/wait-on :js
    (base-fn/fn-eval-disable
     (base-internal/new-mock (repl/>notify))))
  => {"body" {"eval" false, "final" false},
      "status" "ok",
      "op" "stream",
      "topic" "@/::STATE"})

^{:refer js.cell.base-fn/fn-eval-status :adopt true :added "4.0" :unchecked true}
(fact "gets the eval status"
  ^:hidden
  
  (base-fn/fn-eval-status)
  => boolean?)

^{:refer js.cell.base-fn/fn-route-list :adopt true :added "4.0" :unchecked true}
(fact "gets the routes list"
  ^:hidden

  (base-fn/fn-route-list)
  => vector?)

^{:refer js.cell.base-fn/fn-route-entry :adopt true :added "4.0" :unchecked true}
(fact  "gets a route entry"
  ^:hidden

  (base-fn/fn-route-entry "hello")
  => nil)

^{:refer js.cell.base-fn/fn-ping :adopt true :added "4.0" :unchecked true}
(fact "pings the worker"
  ^:hidden
  
  (base-fn/fn-ping)
  => (contains ["pong" integer?]))

^{:refer js.cell.base-fn/fn-ping-async :adopt true :added "4.0" :unchecked true}
(fact "pings after a delay"
  ^:hidden

  (j/<! (base-fn/fn-ping-async 100))
  => (contains ["pong" integer?]))

^{:refer js.cell.base-fn/fn-echo :adopt true :added "4.0" :unchecked true}
(fact  "echos the first arg"
  ^:hidden
  
  (base-fn/fn-echo "hello")
  => (contains ["hello" integer?]))

^{:refer js.cell.base-fn/fn-echo-async :adopt true :added "4.0" :unchecked true}
(fact "echos the first arg after delay"
  ^:hidden

  (j/<! (base-fn/fn-echo-async "hello" 100))
  => (contains ["hello" integer?]))

^{:refer js.cell.base-fn/fn-error :adopt true :added "4.0" :unchecked true}
(fact "throws an error"
  ^:hidden
  
  (base-fn/fn-error)
  => (throws))

^{:refer js.cell.base-fn/fn-error-async :adopt true :added "4.0" :unchecked true}
(fact  "throws an error after delay"
  ^:hidden
  
  (j/<! (. (base-fn/fn-error-async)
           (catch k/identity)))
  => (contains ["error"]))


^{:refer js.cell.base-fn/routes-base :adopt true :added "4.0" :unchecked true}
(fact "returns the base routes"
  ^:hidden
  
  (base-fn/routes-base)
  => map?)

^{:refer js.cell.base-fn/routes-init :adopt true :added "4.0" :unchecked true}
(fact "initiates the base routes"
  ^:hidden
  
  (base-fn/routes-init {})
  => (contains [true]))


^{:refer js.cell.base-internal/create-mock :adopt true :added "4.0" :unchecked true}
(fact "initiates the base routes"
  ^:hidden

  (notify/wait-on :js
    (base-internal/mock-init (repl/>notify)
                             {}))
  => {"body" {"done" true}, "status" "ok", "op" "stream", "topic" "@/::INIT"})

^{:refer js.cell.base-internal/worker-process :adopt true :added "4.0" :unchecked true
  :setup [(l/rt:restart)]}
(fact "initiates the base routes"
  ^:hidden
  
  (notify/wait-on :js
    (base-internal/mock-init (repl/>notify)
                             {}))
  => {"body" {"done" true}, "status" "ok", "op" "stream", "topic" "@/::INIT"}

  (notify/wait-on :js
    (var mock (base-internal/mock-init (repl/>notify)
                                       {}
                                       true))
    (base-internal/mock-send mock "1+1"))
  => {"body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",
      "id" nil,
      "status" "ok",
      "op" "eval"}
  
  (notify/wait-on :js
    (var mock (base-internal/mock-init (repl/>notify)
                                       {}
                                       true))
    (base-internal/mock-send mock {:op "eval"
                                   :id "A"
                                   :body "1+1"}))
  => {"body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",
      "id" "A",
      "status" "ok",
      "op" "eval"}

  (notify/wait-on :js
    (var mock (base-internal/mock-init (repl/>notify)
                                       {}
                                       true))
    (base-internal/mock-send mock {:op "route"
                                   :id "id-route"
                                   :route "@/ping-async"
                                   :body [100]}))
  => (contains-in
      {"body" ["pong" integer?],
       "id" "id-route",
       "status" "ok",
       "op" "route"}))


^{:refer js.cell.link-raw/link-create-mock :adopt true :added "4.0" :unchecked true
  :setup [(l/rt:restart)]}
(fact "creates a mock link for testing purposes"

  (notify/wait-on :js
    (var link (link-raw/link-create
              {:create-fn
               (fn:> [listener]
                 (base-internal/mock-init
                  listener
                  {} true))}))
   (j/notify (link-fn/ping-async link 300)))
  => (contains ["pong" integer?]) )




(comment
  ^*(!.js
 (link-raw/link-create
  {:create-fn (fn:> [listener] (base-internal/mock-init listener {}))}))
  
  (l/rt:restart)
  
  (!.js
   (k/trace-log-clear)
   (:= (!:G LK)
       ))
  
  (j/<! (link-fn/echo LK "hello"))
  
  (j/<! )
  (!.js
   (. (link-fn/echo LK "hello")
      (catch k/identity)))
  
  (!.js
   LK)
  (k/trace-log)
  
  (do 
    (k/trace-log-clear)
    (h/suppress
     (notify/wait-on :js
       (var link )
       (j/notify (link-fn/ping link))))
    (k/trace-log))
  
  [{"tag" "s10l40aeuvky",
    "time" 1646974384441,
    "line" 78,
    "column" 3,
    "data"
    [{"body" [], "route" "@/error", "id" "8g6-oof", "op" "route"}
     {"8g6-oof"
      {"time" 1646974384440,
       "input"
       {"body" [],
        "route" "@/error",
        "id" "8g6-oof",
        "op" "route"}}}],
    "ns" "js.cell.link-raw"}]
  
  
  (link-fn/echo)

  )
