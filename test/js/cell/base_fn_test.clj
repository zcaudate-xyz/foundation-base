(ns js.cell.base-fn-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [js.cell.playground :as browser]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [xt.lang.util-throttle :as th]
             [js.core :as j]
             [js.cell.base-fn :as base-fn]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

(defmacro eval-worker
  [body & [timeout no-post]]
  (h/$ (notify/wait-on [:js ~(or timeout 1000)]
         (var worker (new Worker
                          (fn []
                            (eval (@! (browser/play-script
                                       '[~(if no-post
                                            body
                                            (list 'js.core/settle 'postMessage body))]
                                       true))))))
         (. worker (addEventListener
                    "message"
                    (fn [e]
                      (repl/notify e.data))
                    false)))))

^{:refer js.cell.base-fn/CELL_STATE :added "4.0" :unchecked true}
(fact "gets worker state"

  (base-fn/CELL_STATE)
  => map?)

^{:refer js.cell.base-fn/CELL_ROUTES :added "4.0" :unchecked true}
(fact "gets worker routes"

  (base-fn/CELL_ROUTES)
  => map?)

^{:refer js.cell.base-fn/get-state :added "4.0" :unchecked true}
(fact "gets cell state")

^{:refer js.cell.base-fn/get-routes :added "4.0" :unchecked true}
(fact "gets cell routes")

^{:refer js.cell.base-fn/fn-self :added "4.0" :unchecked true}
(fact "applies arguments along with `self`"
  ^:hidden
  
  (set 
   (eval-worker ((js.cell.base-fn/fn-self
                  xt.lang.base-lib/obj-keys))))
  => #{"onerror" "close" "postMessage" "addEventListener" "onmessage"})

^{:refer js.cell.base-fn/fn-trigger :added "4.0" :unchecked true}
(fact "triggers an event"
  ^:hidden
  
  (eval-worker (js.cell.base-fn/fn-trigger
                self
                "stream"
                "hello"
                "ok"
                {:a 1})
               nil true)
  => {"body" {"a" 1}, "status" "ok", "op" "stream", "topic" "hello"})

^{:refer js.cell.base-fn/fn-trigger-async :added "4.0" :unchecked true}
(fact "triggers an event after a delay"
  ^:hidden
  
  (eval-worker (js.cell.base-fn/fn-trigger-async
                self
                "stream"
                "hello"
                "ok"
                {:a 1}
                100)
               nil true)
  => {"body" {"a" 1}, "status" "ok", "op" "stream", "topic" "hello"})

^{:refer js.cell.base-fn/fn-set-state :added "4.0" :unchecked true}
(fact "helper to set the state and emit event"
  ^:hidden

  (eval-worker (js.cell.base-fn/fn-set-state
                self
                (js.cell.base-fn/CELL_STATE)
                (fn [])
                true))
  => {"eval" true}
  
  (eval-worker (js.cell.base-fn/fn-set-state
                self
                (js.cell.base-fn/CELL_STATE)
                (fn []))
               nil true)
  => {"body" {"eval" true},
      "status" "ok",
      "op" "stream",
      "topic" "@/::STATE"})

^{:refer js.cell.base-fn/fn-final-set :added "4.0" :unchecked true}
(fact "sets the worker state to final"
  ^:hidden
  
  (eval-worker (js.cell.base-fn/fn-final-set
                self true))
  => {"eval" true, "final" true}
  
  (eval-worker (js.cell.base-fn/fn-final-set
                self)
               nil true)
  => {"body" {"eval" true, "final" true},
      "status" "ok",
      "op" "stream",
      "topic" "@/::STATE"})

^{:refer js.cell.base-fn/fn-final-status :added "4.0" :unchecked true}
(fact "gets the final status"
  ^:hidden

  (eval-worker (js.cell.base-fn/fn-final-status
                self))
  => nil
  
  (eval-worker (do:> (js.cell.base-fn/fn-final-set
                      self true)
                     (return (js.cell.base-fn/fn-final-status
                              self))))
  => true)

^{:refer js.cell.base-fn/fn-eval-enable :added "4.0" :unchecked true}
(fact "enables eval"
  ^:hidden
  
  (eval-worker (js.cell.base-fn/fn-eval-enable
                self true))
  => {"eval" true}
  
  (eval-worker (js.cell.base-fn/fn-eval-enable
                self)
               nil true)
  => {"body" {"eval" true}, "status" "ok", "op" "stream", "topic" "@/::STATE"})

^{:refer js.cell.base-fn/fn-eval-disable :added "4.0" :unchecked true}
(fact "disables eval"
  ^:hidden
  
  (eval-worker (js.cell.base-fn/fn-eval-disable
                self true))
  => {"eval" false}
  
  (eval-worker (js.cell.base-fn/fn-eval-disable
                self)
               nil true)
  => {"body" {"eval" false}, "status" "ok", "op" "stream", "topic" "@/::STATE"})

^{:refer js.cell.base-fn/fn-eval-status :added "4.0" :unchecked true}
(fact "gets the eval status"
  ^:hidden
  
  (eval-worker (js.cell.base-fn/fn-eval-status
                self))
  => true
  
  (eval-worker (do:> (js.cell.base-fn/fn-eval-disable
                      self true)
                     (return (js.cell.base-fn/fn-eval-status
                              self))))
  => false)

^{:refer js.cell.base-fn/fn-route-list :added "4.0" :unchecked true}
(fact "gets the routes list"
  ^:hidden

  (js.cell.base-fn/fn-route-list)
  => vector?
  
  (eval-worker (js.cell.base-fn/fn-route-list))
  => vector?)

^{:refer js.cell.base-fn/fn-route-entry :added "4.0" :unchecked true}
(fact  "gets a route entry"
  ^:hidden

  (js.cell.base-fn/fn-route-entry "hello")
  => nil
  
  (eval-worker (js.cell.base-fn/fn-route-entry "hello"))
  => nil)

^{:refer js.cell.base-fn/fn-ping :added "4.0" :unchecked true}
(fact "pings the worker"
  ^:hidden
  
  (base-fn/fn-ping)
  => (contains ["pong" integer?])
  
  (eval-worker (js.cell.base-fn/fn-ping))
  => (contains ["pong" integer?]))

^{:refer js.cell.base-fn/fn-ping-async :added "4.0" :unchecked true}
(fact "pings after a delay"
  ^:hidden

  (j/<! (base-fn/fn-ping-async 100))
  => (contains ["pong" integer?])
  
  (eval-worker (js.cell.base-fn/fn-ping-async 100))
  => (contains ["pong" integer?]))

^{:refer js.cell.base-fn/fn-echo :added "4.0" :unchecked true}
(fact  "echos the first arg"
  ^:hidden
  
  (base-fn/fn-echo "hello")
  => (contains ["hello" integer?])
  
  (eval-worker (js.cell.base-fn/fn-echo "hello"))
  => (contains ["hello" integer?]))

^{:refer js.cell.base-fn/fn-echo-async :added "4.0" :unchecked true}
(fact "echos the first arg after delay"
  ^:hidden

  (j/<! (base-fn/fn-echo-async "hello" 100))
  => (contains ["hello" integer?])
  
  (eval-worker (js.cell.base-fn/fn-echo-async "hello" 100))
  => (contains ["hello" integer?]))

^{:refer js.cell.base-fn/fn-error :added "4.0" :unchecked true}
(fact "throws an error"
  ^:hidden
  
  (base-fn/fn-error)
  => (throws)

  (eval-worker (js.cell.base-fn/fn-error))
  => :timeout)

^{:refer js.cell.base-fn/fn-error-async :added "4.0" :unchecked true}
(fact  "throws an error after delay"
  ^:hidden
  
  (j/<! (. (base-fn/fn-error-async)
           (catch k/identity)))
  => (contains ["error"])
  
  (eval-worker (js.cell.base-fn/fn-error)
               300)
  => :timeout)

^{:refer js.cell.base-fn/tmpl-local-route :added "4.0" :unchecked true}
(fact "templates a local function"
  ^:hidden
  
  (base-fn/tmpl-local-route @base-fn/fn-echo)
  => '["@/echo" {:handler js.cell.base-fn/fn-echo,
                      :async false
                      :args ["arg"]}]

  (base-fn/tmpl-local-route @base-fn/fn-trigger-async)
  => '["@/trigger-async" {:handler (js.cell.base-fn/fn-self
                                         js.cell.base-fn/fn-trigger-async),
                               :async true
                               :args ["op" "topic" "status" "body" "ms"]}])

^{:refer js.cell.base-fn/routes-base :added "4.0" :unchecked true}
(fact "returns the base routes"
  ^:hidden
  
  (base-fn/routes-base)
  => map?)

^{:refer js.cell.base-fn/routes-init :added "4.0" :unchecked true}
(fact "initiates the base routes"
  ^:hidden
  
  (base-fn/routes-init {})
  => (contains [true]))
