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
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.old.event-view :as base-view]
             [xt.cell.kernel.base-impl :as impl-common]
             [xt.cell.kernel.base-link-local :as link-fn]
             [xt.cell.kernel.inner-impl :as inner-impl]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.cell.kernel :as cl]
             [xt.lang.common-repl :as repl]]})

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

(defn.xt setup-annex-cell
  []
  (var cell (-/reset-cell))
  (return (. cell ["init"]
             (then (fn []
                     (cl/GX-set "p0" cell)
                     (return cell))))))

(defn.xt setup-hello-model
  []
  (return
   (. (-/setup-annex-cell)
      (then (fn []
              (return
               (. (cl/add-model "hello"
                                {:echo {:handler link-fn/echo
                                        :trigger {"hello" true}
                                        :defaultArgs ["HELLO"]}})
                  ["init"]
                  (then (fn []
                          (return (cl/GD)))))))))))

(defn.xt setup-hello-attach
  []
  (return (. (-/setup-annex-cell)
             (then (fn []
                     (cl/add-model-attach "hello"
                                          {:echo {:handler link-fn/echo
                                                  :trigger {"hello" true}
                                                  :defaultArgs ["HELLO"]}})
                     (return (cl/GD)))))))

(defn.xt setup-hello-remote-model
  []
  (return
   (. (-/setup-annex-cell)
      (then (fn []
              (return
               (. (cl/add-model "hello"
                                {:echo {:handler link-fn/echo
                                        :remoteHandler link-fn/echo-async
                                        :trigger {"hello" true}
                                        :defaultArgs ["HELLO" 25]}})
                  ["init"]
                  (then (fn []
                          (return (cl/GD)))))))))))

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
  => (contains ["pong" integer?]))

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
        "::" "view.run"}}))

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
  => "WORLD")

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

^{:refer xt.cell.kernel/GD :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "gets the current cell singleton"

  (!.js
   (cl/GD))
  => map?)

^{:refer xt.cell.kernel/GX :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "gets the current annex map"

  (!.js
   (xt/x:obj-keys (cl/GX)))
  => ["p0"])

^{:refer xt.cell.kernel/GX-val :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "gets a stored annex entry"

  (!.js
   (cl/GX-val "p0"))
  => map?)

^{:refer xt.cell.kernel/GX-set :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "stores a cell in the annex map"

  (!.js
   (cl/GX-set "p1" (cl/GD)))
  => map?)

^{:refer xt.cell.kernel/get-cell :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "resolves the current cell from context"

  (!.js
   (cl/get-cell "p0"))
  => map?)

^{:refer xt.cell.kernel/call :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "conducts a raw call against the current cell"

  (notify/wait-on :js
    (. (cl/call (cl/GD)
                {:op "call"
                 :action "@worker/ping.async"
                 :body [10]})
       (then (repl/>notify))))
  => (contains ["pong" integer?]))

^{:refer xt.cell.kernel/fn-call-model :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "calls a model helper using the current cell"

  (!.js
   (cl/fn-call-model impl-common/model-get "hello" []))
  => map?)

^{:refer xt.cell.kernel/fn-call-view :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "calls a view helper using the current cell"

  (!.js
   (xt/x:second (cl/fn-call-view impl-common/view-ensure ["hello" "echo"] [])))
  => map?)

^{:refer xt.cell.kernel/fn-access-cell :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "maps an accessor across all views in the current cell"

  (!.js
   (cl/fn-access-cell base-view/get-current))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}}))

^{:refer xt.cell.kernel/fn-access-model :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "maps an accessor across all views in a model"

  (!.js
   (cl/fn-access-model base-view/get-current "hello"))
  => (contains-in {"echo" ["HELLO" integer?]}))

^{:refer xt.cell.kernel/fn-access-view :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "applies an accessor to a single view"

  (!.js
   (cl/fn-access-view base-view/get-current ["hello" "echo"] []))
  => (contains ["HELLO" integer?]))

^{:refer xt.cell.kernel/list-models :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "lists all models in the current cell"

  (!.js
   (cl/list-models))
  => ["hello"])

^{:refer xt.cell.kernel/list-views :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "lists all views in a model"

  (!.js
   (cl/list-views "hello"))
  => ["echo"])

^{:refer xt.cell.kernel/get-model :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets a model in context"

  (!.js
   (cl/get-model "hello"))
  => map?)

^{:refer xt.cell.kernel/get-view :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets a view in context"

  (!.js
   (cl/get-view ["hello" "echo"]))
  => map?)

^{:refer xt.cell.kernel/cell-vals :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets all current view values in the cell"

  (!.js
   (cl/cell-vals))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}}))

^{:refer xt.cell.kernel/cell-outputs :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets all view outputs in the cell"

  (!.js
   (cl/cell-outputs))
  => (contains-in {"hello" {"echo" {"current" ["HELLO" integer?]
                                    "updated" integer?}}}))

^{:refer xt.cell.kernel/cell-inputs :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets all view inputs in the cell"

  (!.js
   (cl/cell-inputs))
  => (contains-in {"hello" {"echo" {"current" {"data" ["HELLO"]}
                                    "updated" integer?}}}))

^{:refer xt.cell.kernel/cell-trigger :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "triggers all matching views in the cell"

  (!.js
   (cl/cell-trigger "hello" {}))
  => {"hello" ["echo"]})

^{:refer xt.cell.kernel/model-outputs :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets all outputs for a model"

  (!.js
   (cl/model-outputs "hello"))
  => (contains-in {"echo" {"current" ["HELLO" integer?]
                           "updated" integer?}}))

^{:refer xt.cell.kernel/model-vals :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets all values for a model"

  (!.js
   (cl/model-vals "hello"))
  => (contains-in {"echo" ["HELLO" integer?]}))

^{:refer xt.cell.kernel/model-is-errored :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "checks if any model view has errored"

  (!.js
   (cl/model-is-errored "hello"))
  => false)

^{:refer xt.cell.kernel/model-is-pending :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "checks if any model view is pending"

  (!.js
   (cl/model-is-pending "hello"))
  => false)

^{:refer xt.cell.kernel/add-model-attach :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "adds a model without triggering initialization"

  (!.js
   (cl/add-model-attach "hello"
                        {:echo {:handler link-fn/echo
                                :defaultArgs ["HELLO"]}})
   (cl/list-models))
  => ["hello"])

^{:refer xt.cell.kernel/add-model :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "adds a model and runs its init refresh"

  (notify/wait-on :js
    (. (cl/add-model "hello"
                     {:echo {:handler link-fn/echo
                             :defaultArgs ["HELLO"]}})
       ["init"]
       (then (repl/>notify))))
  => (contains-in
      [{"path" ["hello" "echo"]
        "post" [false]
        "main" [true ["HELLO" integer?]]
        "pre" [false]
        "::" "view.run"}]))

^{:refer xt.cell.kernel/remove-model :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-attach)
               (then (repl/>notify))))]}
(fact "removes a model from the current cell"

  (!.js
   (cl/remove-model "hello")
   (cl/list-models))
  => [])

^{:refer xt.cell.kernel/model-trigger :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "triggers matching views on a model"

  (!.js
   (cl/model-trigger "hello" "hello" {}))
  => ["echo"])

^{:refer xt.cell.kernel/view-success :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets the successful output value for a view"

  (!.js
   (cl/view-success ["hello" "echo"]))
  => (contains ["HELLO" integer?]))

^{:refer xt.cell.kernel/view-get-input :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets the current input state for a view"

  (!.js
   (cl/view-get-input ["hello" "echo"]))
  => map?)

^{:refer xt.cell.kernel/view-get-output :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets the current output state for a view"

  (!.js
   (cl/view-get-output ["hello" "echo"]))
  => (contains-in {"current" ["HELLO" integer?]
                   "updated" integer?}))

^{:refer xt.cell.kernel/view-set-val :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "sets the current output value for a view"

  (!.js
   (do (cl/view-set-val ["hello" "echo"] 1)
       (cl/view-val ["hello" "echo"])))
  => 1)

^{:refer xt.cell.kernel/view-get-time-updated :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets the last update time for a view"

  (!.js
   (cl/view-get-time-updated ["hello" "echo"]))
  => integer?)

^{:refer xt.cell.kernel/view-is-errored :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "checks whether a view is errored"

  (!.js
   (cl/view-is-errored ["hello" "echo"]))
  => false)

^{:refer xt.cell.kernel/view-is-pending :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "checks whether a view is pending"

  (!.js
   (cl/view-is-pending ["hello" "echo"]))
  => false)

^{:refer xt.cell.kernel/view-get-time-elapsed :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "gets the elapsed time for the last run"

  (notify/wait-on :js
    (. (cl/view-refresh ["hello" "echo"])
       (then (fn []
               (repl/notify
                (cl/view-get-time-elapsed ["hello" "echo"]))))))
  => integer?)

^{:refer xt.cell.kernel/view-set-input :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "sets new input data and refreshes the view"

  (notify/wait-on :js
    (. (xt/x:first (cl/view-set-input ["hello" "echo"] {:data ["WORLD"]}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"}))

^{:refer xt.cell.kernel/view-refresh :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "refreshes a view through the kernel"

  (notify/wait-on :js
    (. (cl/view-refresh ["hello" "echo"])
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["HELLO" integer?]]
       "pre" [false]
       "::" "view.run"}))

^{:refer xt.cell.kernel/view-update :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "updates a view and returns the legacy throttle entry"

  (notify/wait-on :js
    (. (xt/x:first (cl/view-update ["hello" "echo"]))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["HELLO" integer?]]
       "pre" [false]
       "::" "view.run"}))

^{:refer xt.cell.kernel/view-ensure :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "returns the model and view records for a path"

  (!.js
   (cl/view-ensure ["hello" "echo"]))
  => (contains [map? map?]))

^{:refer xt.cell.kernel/view-call-remote :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-remote-model)
               (then (repl/>notify))))]}
(fact "runs the remote handler for a view"

  (notify/wait-on :js
    (. (cl/view-call-remote ["hello" "echo"] ["WORLD" 10] true)
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "pre" [false]
       "remote" [true ["WORLD" integer?]]
       "post" [false]
       "::" "view.run"}))

^{:refer xt.cell.kernel/view-refresh-remote :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-remote-model)
               (then (repl/>notify))))]}
(fact "refreshes the remote output for a view"

  (notify/wait-on :js
    (. (cl/view-refresh-remote ["hello" "echo"])
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "pre" [false]
       "remote" [true ["HELLO" integer?]]
       "post" [false]
       "::" "view.run"}))

^{:refer xt.cell.kernel/view-trigger :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "triggers a view when the topic matches"

  (notify/wait-on :js
    (. (xt/x:first (cl/view-trigger ["hello" "echo"] "hello" {}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["HELLO" integer?]]
       "pre" [false]
       "::" "view.run"}))

^{:refer xt.cell.kernel/view-for :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "returns the updated view value"

  (notify/wait-on :js
    (. (cl/view-for ["hello" "echo"])
       (then (repl/>notify))))
  => (contains ["HELLO" integer?]))

^{:refer xt.cell.kernel/view-for-input :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "returns the view value after updating input"

  (notify/wait-on :js
    (. (cl/view-for-input ["hello" "echo"] {:data ["WORLD"]})
       (then (repl/>notify))))
  => (contains ["WORLD" integer?]))

^{:refer xt.cell.kernel/get-val :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "reads a nested subpath from the current view value"

  (!.js
   (cl/get-val ["hello" "echo"] [0]))
  => "HELLO")

^{:refer xt.cell.kernel/get-for :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "reads a nested subpath after updating the view"

  (notify/wait-on :js
    (. (cl/get-for ["hello" "echo"] [0])
       (then (repl/>notify))))
  => "HELLO")

^{:refer xt.cell.kernel/nil-view :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "sets a single view input to nil"

  (notify/wait-on :js
    (. (cl/nil-view ["hello" "echo"])
       (then (repl/>notify))))
  => (contains ["HELLO" integer?]))

^{:refer xt.cell.kernel/nil-model :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "sets all model view inputs to nil"

  (notify/wait-on :js
    (. (cl/nil-model "hello")
       (then (fn [arr]
               (repl/notify
                (xt/x:first arr))))))
  => (contains ["HELLO" integer?]))

^{:refer xt.cell.kernel/clear-listeners :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "clears all keyed listeners on the current cell"

  (!.js
   (cl/add-listener ["hello" "echo"] "@react/1234" (fn [event] (return event)) nil nil)
   (cl/clear-listeners)
   (cl/list-all-listeners))
  => {})

^{:refer xt.cell.kernel/remove-listener :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "removes a keyed listener from a view path"

  (!.js
   (cl/add-listener ["hello" "echo"] "@react/1234" (fn [event] (return event)) nil nil)
   (cl/remove-listener ["hello" "echo"] "@react/1234")
   (cl/list-listeners ["hello" "echo"]))
  => [])

^{:refer xt.cell.kernel/list-listeners :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "lists listener ids for a single view path"

  (!.js
   (cl/add-listener ["hello" "echo"] "@react/1234" (fn [event] (return event)) nil nil)
   (cl/list-listeners ["hello" "echo"]))
  => ["@react/1234"])

^{:refer xt.cell.kernel/list-all-listeners :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-hello-model)
               (then (repl/>notify))))]}
(fact "lists all listener ids grouped by model and view"

  (!.js
   (cl/add-listener ["hello" "echo"] "@react/1234" (fn [event] (return event)) nil nil)
   (cl/add-listener ["hello" "echo"] "@react/5678" (fn [event] (return event)) nil nil)
   (cl/list-all-listeners))
  => {"hello" {"echo" ["@react/1234" "@react/5678"]}})

^{:refer xt.cell.kernel/add-raw-callback :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "adds a raw link callback for all events"

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
      "topic" "hello"})

^{:refer xt.cell.kernel/remove-raw-callback :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "removes a raw link callback by key"

  (!.js
   (cl/add-raw-callback "@/TEST" true (fn [event signal] (return event)) nil)
   (cl/remove-raw-callback "@/TEST"))
  => vector?)

^{:refer xt.cell.kernel/list-raw-callbacks :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/setup-annex-cell)
               (then (repl/>notify))))]}
(fact "lists all registered raw link callbacks"

  (set
   (!.js
    (cl/add-raw-callback "@/TEST" true (fn [event signal] (return event)) nil)
    (cl/list-raw-callbacks)))
  => #{"@/raw" "@/TEST"})