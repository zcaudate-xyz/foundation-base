(ns xt.cell.kernel.base-async-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :xtalk
  {:require [[xt.cell.kernel.base-async :as base-async]]})

(l/script- :js
  {:require [[xt.cell.kernel.base-async :as base-async]
             [xt.lang.common-repl :as repl]
             [js.core :as j]]
   :runtime :basic})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.base-async/promise-run :added "4.1"}
(fact "runs a thunk inside a host promise"

  (notify/wait-on :js
    (. (base-async/promise-run
        (fn []
          (return "hello")))
       (then (repl/>notify))))
  => "hello"

  (j/<! (. (base-async/promise-run
            (fn []
              (throw "boom")))
           (catch j/identity)))
  => "boom")

^{:refer xt.cell.kernel.base-async/promise-all :added "4.1"}
(fact "waits for all input promises"

  (j/<! (base-async/promise-all
         [(. Promise (resolve "a"))
          (. Promise (resolve "b"))]))
  => ["a" "b"])

^{:refer xt.cell.kernel.base-async/promise-delay :added "4.1"}
(fact "runs a thunk after a timeout"

  (j/<! (base-async/promise-delay
         10
         (fn []
           (return "later"))))
  => "later")

^{:refer xt.cell.kernel.base-async/promise-next :added "4.1"}
(fact "schedules work on the next tick"

  (j/<! (base-async/promise-next
         (fn []
           (return "next"))))
  => "next")

^{:refer xt.cell.kernel.base-async/async-fn :added "4.1"}
(fact "adapts a handler to success and error callbacks"

  (notify/wait-on :js
    (base-async/async-fn
     (fn [context]
       (return (. context ["value"])))
     {"value" "ok"}
     {:success (repl/>notify)
      :error j/identity}))
  => "ok"

  (j/<! (base-async/async-fn
         (fn [_]
           (throw "boom"))
         {}
         {:success j/identity
          :error j/identity}))
  => "boom")
