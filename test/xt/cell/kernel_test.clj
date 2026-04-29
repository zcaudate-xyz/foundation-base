(ns xt.cell.kernel-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.old.event-view :as base-view]
             [xt.cell.kernel.base-impl :as impl-common]
             [xt.cell.kernel.base-link-local :as link-fn]
             [xt.cell.kernel.inner-impl :as inner-impl]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.cell.kernel :as cl]]})

(l/script- :js
  {:require [[xt.lang.spec-base :as xt] [xt.lang.common-lib :as k] [xt.old.event-view :as base-view] [xt.cell.kernel.base-impl :as impl-common] [xt.cell.kernel.base-link-local :as link-fn] [xt.cell.kernel.inner-impl :as inner-impl] [xt.cell.kernel.inner-mock :as inner-mock] [xt.cell.kernel :as cl] [xt.lang.common-repl :as repl]] :runtime :basic})

(defn.xt reset-cell
  []
  (var cell
       (cl/make-cell
        {:create-fn
         (fn:> [listener]
           (inner-mock/create-worker listener {} true))}))
  (cl/GD-reset cell)
  (cl/GX-reset {})
  (inner-impl/worker-init-signal (. cell ["link"] ["worker"]) {:done true})
  (return cell))

(fact:global
 {:setup [(do (l/rt:restart)
        (l/rt:scaffold-imports :js))]
 :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel/make-cell :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "manages the current cell context"

  (!.js
   (cl/GD))
  => map?

  (!.js
   (cl/GX-set "p0" (cl/GD))
   (xt/x:obj-keys (cl/GX)))
  => ["p0"]

  (!.js
   (cl/GX-val "p0"))
  => map?

  (!.js
   (cl/get-cell))
  => map?

  (!.js
   (cl/get-cell "p0"))
  => map?

  (notify/wait-on :js
    (. (cl/call (cl/GD)
                {:op "call"
                 :action "@worker/ping.async"
                 :body [50]})
       (then (repl/>notify))))
  => (contains ["pong" integer?])

    )

^{:refer xt.cell.kernel/fn-call-cell :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (fn []
                                (. (cl/add-model "hello"
                                                 {:echo {:handler link-fn/echo
                                                         :defaultArgs ["HELLO"]}})
                                   ["init"]
                                   (then (repl/>notify)))))))]}
(fact "wraps cell, model and view access"

  (!.js
   (cl/fn-call-cell k/identity []))
  => map?

  (!.js
   (cl/fn-call-model impl-common/model-get "hello" []))
  => map?

  (!.js
   (xt/x:second (cl/fn-call-view impl-common/view-ensure ["hello" "echo"] [])))
  => map?

  (!.js
   (cl/fn-access-cell base-view/get-current))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (!.js
   (cl/fn-access-model base-view/get-current "hello"))
  => (contains-in {"echo" ["HELLO" integer?]})

  (!.js
   (cl/fn-access-view base-view/get-current ["hello" "echo"] []))
  => (contains ["HELLO" integer?])

  (!.js
   (cl/list-models))
  => ["hello"]

  (!.js
   (cl/list-views "hello"))
  => ["echo"]

  (!.js
   (cl/get-model "hello"))
  => map?

  (!.js
   (cl/get-view ["hello" "echo"]))
  => map?)

^{:refer xt.cell.kernel/model-update :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (fn []
                                (. (cl/add-model "hello"
                                                 {:echo {:handler link-fn/echo
                                                         :defaultArgs ["HELLO"]
                                                         :trigger {"hello" true}}})
                                   ["init"]
                                   (then (repl/>notify)))))))]}
(fact "updates and triggers models through the kernel"

  (!.js
   (cl/cell-vals))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (!.js
   (cl/cell-outputs))
  => (contains-in {"hello" {"echo" {"current" ["HELLO" integer?]
                                    "updated" integer?}}})

  (!.js
   (cl/cell-inputs))
  => (contains-in {"hello" {"echo" {"current" {"data" ["HELLO"]}
                                    "updated" integer?}}})

  (!.js
   (cl/model-vals "hello"))
  => (contains-in {"echo" ["HELLO" integer?]})

  (!.js
   (cl/model-outputs "hello"))
  => (contains-in {"echo" {"current" ["HELLO" integer?]
                           "updated" integer?}})

  (!.js
   (cl/model-is-errored "hello"))
  => false

  (!.js
   (cl/model-is-pending "hello"))
  => false

  (!.js
   (cl/model-trigger "hello" "hello" {}))
  => ["echo"]

  (!.js
   (cl/cell-trigger "hello" {}))
  => {"hello" ["echo"]}

  (notify/wait-on :js
    (. (cl/model-update "hello")
       (then (repl/>notify))))
  => (contains-in
      {"echo"
       {"path" ["hello" "echo"]
        "post" [false]
        "main" [true ["HELLO" integer?]]
        "pre" [false]
        "::" "view.run"}})

    )

^{:refer xt.cell.kernel/view-val :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (fn []
                                (. (cl/add-model "hello"
                                                 {:echo {:handler link-fn/echo
                                                         :defaultArgs ["HELLO"]
                                                         :trigger {"hello" true}}})
                                   ["init"]
                                   (then (repl/>notify)))))))]}
(fact "reads and updates views through the kernel"

  (!.js
   (cl/view-success ["hello" "echo"]))
  => (contains ["HELLO" integer?])

  (!.js
   (cl/view-val ["hello" "echo"]))
  => (contains ["HELLO" integer?])

  (!.js
   (cl/view-get-input ["hello" "echo"]))
  => map?

  (!.js
   (cl/view-get-output ["hello" "echo"]))
  => (contains-in {"current" ["HELLO" integer?]
                   "updated" integer?})

  (!.js
   (do (cl/view-set-val ["hello" "echo"] 1)
       (cl/view-val ["hello" "echo"])))
  => 1

  (!.js
   (cl/view-get-time-updated ["hello" "echo"]))
  => integer?

  (!.js
   (cl/view-is-errored ["hello" "echo"]))
  => false

  (!.js
   (cl/view-is-pending ["hello" "echo"]))
  => false

  (notify/wait-on :js
    (. (xt/x:first (cl/view-set-input ["hello" "echo"] {:data ["WORLD"]}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"})

  (notify/wait-on :js
    (. (xt/x:first (cl/view-trigger ["hello" "echo"] "hello" {}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"})

  (notify/wait-on :js
    (. (cl/view-for ["hello" "echo"])
       (then (repl/>notify))))
  => (contains ["WORLD" integer?])

  (!.js
   (cl/get-val ["hello" "echo"] [0]))
  => "WORLD"

  (notify/wait-on :js
    (. (cl/get-for ["hello" "echo"] [0])
       (then (repl/>notify))))
  => "WORLD"

                )

^{:refer xt.cell.kernel/add-listener :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (fn []
                                (. (cl/add-model "hello"
                                                 {:echo {:handler link-fn/echo
                                                         :defaultArgs ["HELLO"]}})
                                   ["init"]
                                   (then (repl/>notify)))))))]}
(fact "manages listeners and raw callbacks"

  (notify/wait-on :js
    (cl/add-listener ["hello" "echo"]
                     "@react/1234"
                     (fn [event]
                       (var #{type} event)
                       (when (== "view.output" type)
                         (repl/notify event)))
                     nil
                     nil)
    (cl/view-update ["hello" "echo"]))
  => (contains-in
      {"path" ["hello" "echo"]
       "type" "view.output"
       "meta" {"listener/id" "@react/1234"
               "listener/type" "cell"}
       "data" {"current" ["HELLO" integer?]
               "updated" integer?
               "pending" true}})

  (!.js
   (cl/list-listeners ["hello" "echo"]))
  => ["@react/1234"]

  (!.js
   (cl/list-all-listeners))
  => {"hello" {"echo" ["@react/1234"]}}

  (!.js
   (cl/remove-listener ["hello" "echo"] "@react/1234"))
  => (contains-in {"meta" {"listener/id" "@react/1234"
                           "listener/type" "cell"}})

  (notify/wait-on :js
    (cl/add-raw-callback "@/TEST" true (repl/>notify) nil)
    (link-fn/trigger (. (cl/GD) ["link"])
                     "stream"
                     "hello"
                     "ok"
                     {:data 123}))
  => {"body" {"data" 123}
      "status" "ok"
      "op" "stream"
      "topic" "hello"}

  (set
   (!.js
    (cl/list-raw-callbacks)))
  => #{"@/raw" "@/TEST"}

  (!.js
   (cl/remove-raw-callback "@/TEST"))
  => vector?)


^{:refer xt.cell.kernel/GD :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/GX :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/GX-val :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/GX-set :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/get-cell :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/call :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/fn-call-model :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/fn-call-view :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/fn-access-cell :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/fn-access-model :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/fn-access-view :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/list-models :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/list-views :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/get-model :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/get-view :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/cell-vals :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/cell-outputs :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/cell-inputs :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/cell-trigger :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/model-outputs :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/model-vals :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/model-is-errored :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/model-is-pending :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/add-model-attach :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/add-model :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/remove-model :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/model-trigger :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-success :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-get-input :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-get-output :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-set-val :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-get-time-updated :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-is-errored :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-is-pending :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-get-time-elapsed :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-set-input :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-refresh :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-update :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-ensure :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-call-remote :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-refresh-remote :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-trigger :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-for :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/view-for-input :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/get-val :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/get-for :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/nil-view :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/nil-model :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/clear-listeners :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/remove-listener :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/list-listeners :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/list-all-listeners :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/add-raw-callback :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/remove-raw-callback :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel/list-raw-callbacks :added "4.1"}
(fact "TODO")