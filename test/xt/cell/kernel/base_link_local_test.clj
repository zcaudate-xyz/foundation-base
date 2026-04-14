(ns xt.cell.kernel.base-link-local-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [js.core :as j]
             [xt.cell.kernel.base-link :as base-link]
             [xt.cell.kernel.base-link-local :as base-link-local]]})

(fact:global
  {:setup     [(l/rt:restart)
               (l/rt:scaffold-imports :js)]
   :teardown  [(l/rt:stop)]})

(defn.js make-link
  [handler]
  (return
   (base-link/link-create
    {:create-fn
     (fn:> [listener]
       {"::" "worker.fake"
        "postRequest"
        (fn [input]
          (return (handler listener input)))})})))

^{:refer xt.cell.kernel.base-link-local/trigger :added "4.0"}
(fact "performs trigger call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "stream"
                       "signal" "hello"
                       "status" "ok"
                       "body" "hello"})
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" nil}))))
    (base-link/add-callback link "test" "hello" (repl/>notify))
    (base-link-local/trigger link "stream" "hello" "ok" "hello"))
  => {"body" "hello",
      "status" "ok",
      "op" "stream",
      "signal" "hello"})

^{:refer xt.cell.kernel.base-link-local/trigger-async :added "4.0"}
(fact "performs trigger-async call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (j/future-delayed [10]
              (listener {"op" "stream"
                         "signal" "hello"
                         "status" "ok"
                         "body" "hello"})
              (listener {"op" "call"
                         "id" input.id
                         "status" "ok"
                         "body" nil})))))
    (base-link/add-callback link "test" "hello" (repl/>notify))
    (base-link-local/trigger-async link "stream" "hello" "ok" "hello" 100))
  => {"body" "hello",
      "status" "ok",
      "op" "stream",
      "signal" "hello"})

^{:refer xt.cell.kernel.base-link-local/set-final-status :added "4.0"}
(fact "performs set-final-status call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" {"eval" true "final" true}}))))
    (. (base-link-local/set-final-status link true)
       (then (repl/>notify))))
  => {"eval" true, "final" true})

^{:refer xt.cell.kernel.base-link-local/get-final-status :added "4.0"}
(fact "performs get-final-status call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" true}))))
    (. (base-link-local/get-final-status link)
       (then (repl/>notify))))
  => true)

^{:refer xt.cell.kernel.base-link-local/set-eval-status :added "4.0"}
(fact "performs set-eval-status call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" {"eval" false}}))))
    (. (base-link-local/set-eval-status link false true)
       (then (repl/>notify))))
  => {"eval" false})

^{:refer xt.cell.kernel.base-link-local/get-eval-status :added "4.0"}
(fact "performs get-eval-status call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" false}))))
    (. (base-link-local/get-eval-status link)
       (then (repl/>notify))))
  => false)

^{:refer xt.cell.kernel.base-link-local/get-action-list :added "4.0"}
(fact "performs get-action-list call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" ["@worker/ping"
                               "@worker/ping.async"
                               "@worker/echo"
                               "@worker/error.async"]}))))
    (. (base-link-local/get-action-list link)
       (then (repl/>notify))))
  => ["@worker/ping"
      "@worker/ping.async"
      "@worker/echo"
      "@worker/error.async"])

^{:refer xt.cell.kernel.base-link-local/get-action-entry :added "4.0"}
(fact "performs get-action-entry call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" {"args" ["arg"] "is_async" false}}))))
    (. (base-link-local/get-action-entry link "@worker/echo")
       (then (repl/>notify))))
  => {"args" ["arg"], "is_async" false})

^{:refer xt.cell.kernel.base-link-local/ping :added "4.0"}
(fact "performs ping call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" ["pong" 1]}))))
    (. (base-link-local/ping link)
       (then (repl/>notify))))
  => ["pong" 1])

^{:refer xt.cell.kernel.base-link-local/ping-async :added "4.0"}
(fact "performs ping-async call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (j/future-delayed [10]
              (listener {"op" "call"
                         "id" input.id
                         "status" "ok"
                         "body" ["pong" 1]})))))
    (. (base-link-local/ping-async link 100)
       (then (repl/>notify))))
  => ["pong" 1])

^{:refer xt.cell.kernel.base-link-local/echo :added "4.0"}
(fact "performs echo call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" ["hello" 1]}))))
    (. (base-link-local/echo link "hello")
       (then (repl/>notify))))
  => ["hello" 1])

^{:refer xt.cell.kernel.base-link-local/echo-async :added "4.0"}
(fact "performs echo-async call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (j/future-delayed [10]
              (listener {"op" "call"
                         "id" input.id
                         "status" "ok"
                         "body" ["hello" 1]})))))
    (. (base-link-local/echo-async link "hello" 100)
       (then (repl/>notify))))
  => ["hello" 1])

^{:refer xt.cell.kernel.base-link-local/error :added "4.0"}
(fact "performs error call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "error"
                       "body" ["error" 1]
                       "action" "@worker/error"}))))
    (. (base-link-local/error link)
       (catch (repl/>notify))))
  => (contains-in
      {"body" ["error" 1],
       "action" "@worker/error",
       "status" "error",
       "op" "call"}))

^{:refer xt.cell.kernel.base-link-local/error-async :added "4.0"}
(fact "performs error-async call"
  ^:hidden
  
  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (j/future-delayed [10]
              (listener {"op" "call"
                         "id" input.id
                         "status" "error"
                         "body" ["error" 1]
                         "action" "@worker/error.async"})))))
    (. (base-link-local/error-async link 100)
       (catch (repl/>notify))))
  => (contains-in
      {"body" ["error" 1],
       "action" "@worker/error.async",
       "status" "error",
       "op" "call"}))

^{:refer xt.cell.kernel.base-link-local/tmpl-link-action :added "4.0"}
(fact "performs a template"
  ^:hidden
  
  (base-link-local/tmpl-link-action
   '[trigger xt.cell.kernel.worker-state/fn-trigger])
  => (contains '(defn.xt trigger)))
