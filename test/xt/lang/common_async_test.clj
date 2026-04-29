(ns xt.lang.common-async-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
              [xt.lang.common-async :as async]
              [xt.lang.spec-promise :as spec-promise]
              [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
   :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-async/promise-run :added "4.1"}
(fact "runs a thunk inside a host promise"

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (async/promise-run
      (fn []
        (return "hello")))
     (repl/>notify)))
  => "hello"

  (notify/wait-on :js
    (spec-promise/x:promise-catch
     (async/promise-run
      (fn []
        (xt/x:err "boom")))
     (fn [_]
       (repl/notify "boom"))))
  => "boom"
  )

^{:refer xt.lang.common-async/promise-all :added "4.1"}
(fact "waits for all input values and promises"

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (async/promise-all
      ["a"
       (async/promise-run (fn [] (return "b")))])
     (repl/>notify)))
  => ["a" "b"])

^{:refer xt.lang.common-async/promise-delay :added "4.1"}
(fact "runs a thunk after a timeout"

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (async/promise-delay
      10
      (fn []
        (return "later")))
     (repl/>notify)))
  => "later")

^{:refer xt.lang.common-async/promise-next :added "4.1"}
(fact "schedules work on the next tick"

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (async/promise-next
      (fn []
        (return "next")))
     (repl/>notify)))
  => "next")

^{:refer xt.lang.common-async/async-fn :added "4.1"}
(fact "adapts a handler to success and error callbacks"

  (notify/wait-on :js
    (async/async-fn
     (fn [context]
       (return (. context ["value"])))
      {"value" "ok"}
      {"success" (repl/>notify)
       "error" (repl/>notify)}))
  => "ok"

  (notify/wait-on :js
    (async/async-fn
     (fn [_]
       (xt/x:err "boom"))
      {}
      {"success" (repl/>notify)
       "error" (fn [_]
                  (repl/notify "boom"))}))
  => "boom")
