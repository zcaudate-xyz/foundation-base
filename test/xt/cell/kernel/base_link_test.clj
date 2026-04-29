(ns xt.cell.kernel.base-link-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :xtalk
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.cell.kernel.base-link :as base-link]
             [xt.cell.kernel.base-link-local :as base-link-local]
             [xt.cell.kernel.inner-mock :as inner-mock]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.cell.kernel.base-link :as base-link]
             [xt.cell.kernel.base-link-local :as base-link-local]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)
                 (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

(defn.xt make-link
  []
  (return
   (base-link/link-create
    {:create-fn
     (fn:> [listener]
       (inner-mock/create-worker listener {} true))})))

^{:refer xt.cell.kernel.base-link/link-listener-call :added "4.0"}
(fact "resolves a call to the link"

  (!.js
   (base-link/link-listener-call {:op "eval"
                                  :id "hello"
                                  :status "ok"
                                  :body "1"}
                                 {:hello {:resolve k/identity}}))
  => 1

  (!.js
   (base-link/link-listener-call {:op "eval"
                                  :id "hello"
                                  :status "ok"}
                                 {:hello {:resolve k/identity
                                          :reject  k/identity}}))
  => (contains {"message" "Format Invalid"
                "id" "hello"
                "status" "error"
                "op" "eval"})

  (!.js
   (base-link/link-listener-call {:op "eval"
                                  :id "hello"
                                  :status "error"
                                  :message "Server Error"}
                                 {:hello {:resolve k/identity
                                          :reject  k/identity}}))
  => (contains {"message" "Server Error"
                "id" "hello"
                "status" "error"
                "op" "eval"}))

^{:refer xt.cell.kernel.base-link/link-listener-event :added "4.0"}
(fact "notifies all registered callbacks"

  (!.js
   (base-link/link-listener-event {:op "stream"
                                   :signal "hello"}
                                  {:hello {:pred true
                                           :handler (fn:> true)}
                                   :world {:handler (fn:> true)}
                                   :again {:pred (fn:> [signal event] (not= signal "hello"))
                                           :handler (fn:> true)}}))
  => ["hello" "world"])

^{:refer xt.cell.kernel.base-link/link-listener :added "4.0"}
(fact "constructs a link listener"

  (!.js
   (base-link/link-listener {:data {:op "eval"
                                    :id "hello"
                                    :status "ok"
                                    :body "1"}}
                            {:hello {:resolve k/identity}}
                            {}))
  => 1)

^{:refer xt.cell.kernel.base-link/link-create-worker :added "4.0"}
(fact "creates a worker from a create-fn"

  (!.js
   (xt/x:get-key
    (base-link/link-create-worker
     {:create-fn
      (fn:> [listener]
        (inner-mock/create-worker listener {} true))}
     {}
     {})
    "::"))
  => "worker.mock")

^{:refer xt.cell.kernel.base-link/link-create :added "4.0"}
(fact "creates a link from a worker descriptor"

  (!.js
   (xt/x:get-key (-/make-link) "::"))
  => "cell.link"

  (notify/wait-on :js
    (var link (-/make-link))
    (. (base-link/call link {:op "eval"
                             :body "1+1"})
       (then (repl/>notify))))
  => 2

  (notify/wait-on :js
    (var link (-/make-link))
    (. (base-link/call link {:op "call"
                             :action "@worker/ping.async"
                             :body [100]})
       (then (repl/>notify))))
  => (contains ["pong" integer?]))

^{:refer xt.cell.kernel.base-link/link-active :added "4.0"}
(fact "tracks active calls on the link"

  (vals
   (!.js
    (var link (-/make-link))
    (base-link/call link {:op "call"
                          :action "@worker/ping.async"
                          :body [100]})
    (base-link/link-active link)))
  => (contains-in
      [{"input" {"body" [100] "action" "@worker/ping.async" "op" "call"}}])

  (vals
   )
  => (contains-in
      [{"input" {"body" [100] "action" "@worker/ping.async" "op" "call"}}])

  (vals
   )
  => (contains-in
      [{"input" {"body" [100] "action" "@worker/ping.async" "op" "call"}}]))

^{:refer xt.cell.kernel.base-link/add-callback :added "4.0"}
(fact "adds a callback to the link"

  (!.js
   (var link (-/make-link))
   (base-link/add-callback link "hello" true (fn:> true))
   (. link ["callbacks"] ["hello"]))
  => (contains {"key" "hello"
                "pred" true}))

^{:refer xt.cell.kernel.base-link/list-callbacks :added "4.0"}
(fact "lists callbacks on the link"

  (!.js
   (var link (-/make-link))
   (base-link/add-callback link "a" true (fn:> true))
   (base-link/add-callback link "b" true (fn:> true))
   (base-link/list-callbacks link))
  => ["a" "b"])

^{:refer xt.cell.kernel.base-link/remove-callback :added "4.0"}
(fact "removes a callback from the link"

  (!.js
   (var link (-/make-link))
   (base-link/add-callback link "a" true (fn:> true))
   (base-link/remove-callback link "a"))
  => (contains-in [{"key" "a" "pred" true}]))

^{:refer xt.cell.kernel.base-link/call-id :added "4.0"}
(fact "creates a call id"

  (!.js
   (base-link/call-id (-/make-link)))
  => string?)

^{:refer xt.cell.kernel.base-link/call :added "4.0"}
(fact "calls the link with a request frame"

  (notify/wait-on :js
    (var link (-/make-link))
    (. (base-link/call link {:op "eval"
                             :body "1+1"})
       (then (repl/>notify))))
  => 2

  (notify/wait-on :js
    (var link (-/make-link))
    (. (base-link-local/error-async link 100)
       (catch (repl/>notify))))
  => (contains-in
      {"body" ["error" integer?]
       "action" "@worker/error.async"
       "status" "error"
       "op" "call"}))