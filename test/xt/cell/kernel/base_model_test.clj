(ns xt.cell.kernel.base-model-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-runtime :as rt :with [defvar.js]]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-data :as xtd]
              [js.core :as j]
              [xt.cell.kernel.base-link :as base-link]
             [xt.cell.kernel.base-link-local :as base-link-local]
             [xt.cell.kernel.base-impl :as base-impl]
             [xt.cell.kernel.base-model :as base-model]
             [xt.cell.kernel.worker-impl :as worker-impl]
             [xt.cell.kernel.worker-mock :as worker-mock]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

(defvar.js CELL
  []
  (return nil))

(defn.js reset-cell
  []
  (var link
       (base-link/link-create
        {:create-fn
         (fn:> [listener]
           (worker-mock/create-worker listener {} true))}))
  (var cell (base-impl/new-cell link))
  (-/CELL-reset cell)
  (worker-impl/worker-init-signal (. link ["worker"]) {:done true})
  (return cell))

(defn.js seed-cell
  []
  (var cell (-/reset-cell))
  (base-model/add-model-attach
   cell
   "hello"
   {"echo" {:handler base-link-local/echo
            :remoteHandler base-link-local/echo
            :defaultArgs ["HELLO"]
            :trigger "refresh"}})
  (base-model/add-model-attach
   cell
   "watch"
   {"echo" {:handler base-link-local/echo
            :defaultArgs ["WATCH"]
            :deps [["hello" "echo"]]
            :trigger "refresh"}})
  (base-model/add-model-attach
   cell
   "async"
   {"delay" {:handler base-link-local/echo-async
             :remoteHandler base-link-local/echo-async
             :defaultArgs ["ASYNC" 20]
             :trigger "refresh"}})
  (return cell))

^{:refer xt.cell.kernel.base-model/wrap-cell-args :added "4.1"}
(fact "prepends the cell link to handler args"

  (!.js
   ((base-model/wrap-cell-args
     (fn [link args]
       (return [(. link ["::"]) args])))
    {"cell" {"link" {"::" "cell.link"}}
     "args" ["a" "b"]}))
  => ["cell.link" "a"])

^{:refer xt.cell.kernel.base-model/task-all :added "4.1"}
(fact "is exposed as the task aggregation helper"
  (resolve 'xt.cell.kernel.base-model/task-all)
  => var?)

^{:refer xt.cell.kernel.base-model/prep-view :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "prepares the view pipeline context"

  (!.js
   (xtd/first
    (base-model/prep-view (-/CELL) "hello" "echo" {})))
  => ["hello" "echo"])

^{:refer xt.cell.kernel.base-model/get-view-dependents :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "finds the views that depend on a source view"

  (!.js
   (base-model/get-view-dependents (-/CELL) "hello" "echo"))
  => {"watch" ["echo"]})

^{:refer xt.cell.kernel.base-model/get-model-dependents :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "finds the models that depend on a source model"

  (!.js
   (base-model/get-model-dependents (-/CELL) "hello"))
  => {"watch" true})

^{:refer xt.cell.kernel.base-model/run-tail-call :added "4.1"}
(fact "runs dependency refresh only for successful accumulators"

  (!.js
   (var out {})
   (base-model/run-tail-call
    {"acc" {"value" true}
     "cell" {}
     "path" ["hello" "echo"]}
    (fn [cell model-id view-id refresh-fn]
      (xt/x:set-key out "path" [model-id view-id])))
   [(. out ["path"])
    (base-model/run-tail-call
     {"acc" {"error" true}
      "cell" {}
      "path" ["hello" "echo"]}
     (fn [cell model-id view-id refresh-fn]
       (xt/x:set-key out "bad" true)))])
  => [["hello" "echo"]
      {"error" true}])

^{:refer xt.cell.kernel.base-model/run-remote :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for remote pipeline execution"
  (resolve 'xt.cell.kernel.base-model/run-remote)
  => var?)

^{:refer xt.cell.kernel.base-model/remote-call :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for direct remote view calls"
  (resolve 'xt.cell.kernel.base-model/remote-call)
  => var?)

^{:refer xt.cell.kernel.base-model/run-refresh :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for main pipeline execution"
  (resolve 'xt.cell.kernel.base-model/run-refresh)
  => var?)

^{:refer xt.cell.kernel.base-model/refresh-view-dependents :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "returns dependent views and queues them through the model throttle"

  (!.js
   (base-model/refresh-view-dependents (-/CELL) "hello" "echo"))
  => {"watch" ["echo"]})

^{:refer xt.cell.kernel.base-model/refresh-view :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for refreshing a single view"
  (resolve 'xt.cell.kernel.base-model/refresh-view)
  => var?)

^{:refer xt.cell.kernel.base-model/refresh-view-remote :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for remote refreshes"
  (resolve 'xt.cell.kernel.base-model/refresh-view-remote)
  => var?)

^{:refer xt.cell.kernel.base-model/refresh-view-dependents-unthrottled :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for unthrottled dependent refreshes"
  (resolve 'xt.cell.kernel.base-model/refresh-view-dependents-unthrottled)
  => var?)

^{:refer xt.cell.kernel.base-model/refresh-model :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for model-wide refreshes"
  (resolve 'xt.cell.kernel.base-model/refresh-model)
  => var?)

^{:refer xt.cell.kernel.base-model/get-model-deps :added "4.1"}
(fact "indexes view dependencies by model and view"

  (!.js
   (base-model/get-model-deps
    "hello"
    {"echo" {"deps" [["hello" "self"]
                     ["watch" "echo"]]}}))
  => {"hello" {"self" {"echo" true}}
      "watch" {"echo" {"echo" true}}})

^{:refer xt.cell.kernel.base-model/get-unknown-deps :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "reports dependency paths that are not present in the cell"

  (!.js
   (base-model/get-unknown-deps
    "hello"
    {"echo" {}}
    {"hello" {"missing" {"echo" true}}
     "ghost" {"echo" {"echo" true}}}
    (-/CELL)))
  => [["hello" "missing"]
      ["ghost" "echo"]])

^{:refer xt.cell.kernel.base-model/create-throttle :added "4.1"
  :setup [(!.js (-/reset-cell) true)]}
(fact "creates a throttle wrapper for model refreshes"

  (!.js
   (base-model/create-throttle (-/CELL) "hello" nil))
  => (contains {"queued" {}
                "active" {}}))

^{:refer xt.cell.kernel.base-model/create-view :added "4.1"
  :setup [(!.js (-/reset-cell) true)]}
(fact "creates and initializes a view record"

  (!.js
   (base-model/create-view
    (-/CELL)
    "hello"
    "echo"
    {:handler base-link-local/echo
     :defaultArgs ["HELLO"]
     :trigger "refresh"}))
  => (contains-in {"options" {"trigger" "refresh"}
                   "listeners"
                   {"@/cell"
                    {"meta"
                     {"listener/id" "@/cell"
                      "listener/type" "view"}}}}))

^{:refer xt.cell.kernel.base-model/add-model-attach :added "4.1"
  :setup [(!.js (-/reset-cell) true)]}
(fact "attaches a model without triggering initialization"

  (!.js
   (base-model/add-model-attach
    (-/CELL)
    "hello"
    {"echo" {:handler base-link-local/echo
             :defaultArgs ["HELLO"]}}))
  => (contains-in {"name" "hello"
                   "views" {"echo" {"::" "event.view"}}}))

^{:refer xt.cell.kernel.base-model/add-model :added "4.1"
  :setup [(!.js (-/reset-cell) true)]}
(fact "adds a model and starts its initialization task"

  (!.js
   (base-model/add-model
    (-/CELL)
    "hello"
    {"echo" {:handler base-link-local/echo
             :defaultArgs ["HELLO"]}}))
  => (contains {"name" "hello"
                "init" map?}))

^{:refer xt.cell.kernel.base-model/remove-model :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "removes a model when there are no dependents"

  (!.js
   (base-model/remove-model (-/CELL) "async"))
  => (contains {"name" "async"}))

^{:refer xt.cell.kernel.base-model/remove-view :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "removes a view when there are no dependents"

  (!.js
   (base-model/remove-view (-/CELL) "async" "delay"))
  => map?)

^{:refer xt.cell.kernel.base-model/model-update :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for model-wide updates"
  (resolve 'xt.cell.kernel.base-model/model-update)
  => var?)

^{:refer xt.cell.kernel.base-model/view-update :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for individual view updates"
  (resolve 'xt.cell.kernel.base-model/view-update)
  => var?)

^{:refer xt.cell.kernel.base-model/view-set-input :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for setting input before update"
  (resolve 'xt.cell.kernel.base-model/view-set-input)
  => var?)

^{:refer xt.cell.kernel.base-model/trigger-model-raw :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "triggers matching views on a model"

  (!.js
   (var model (base-impl/model-get (-/CELL) "hello"))
   (base-model/trigger-model-raw (-/CELL) model "refresh" {}))
  => ["echo"])

^{:refer xt.cell.kernel.base-model/trigger-model :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "triggers matching views for a named model"

  (!.js
   (base-model/trigger-model (-/CELL) "hello" "refresh" {}))
  => ["echo"])

^{:refer xt.cell.kernel.base-model/trigger-view :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "is exposed for single-view triggers"
  (resolve 'xt.cell.kernel.base-model/trigger-view)
  => var?)

^{:refer xt.cell.kernel.base-model/trigger-all :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "triggers matching views across all models in the cell"

  (!.js
   (base-model/trigger-all (-/CELL) "refresh" {}))
  => {"hello" ["echo"]
      "watch" ["echo"]
      "async" ["delay"]})

^{:refer xt.cell.kernel.base-model/add-raw-callback :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "registers the cell raw callback on the underlying link"

  (!.js
   [(base-model/remove-raw-callback (-/CELL))
    (base-model/add-raw-callback (-/CELL))
    (base-link/list-callbacks (. (-/CELL) ["link"]))])
  => [[nil]
      [nil]
      ["@/raw"]])

^{:refer xt.cell.kernel.base-model/remove-raw-callback :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "removes the cell raw callback from the underlying link"

  (!.js
   (base-model/remove-raw-callback (-/CELL)))
  => [nil])
