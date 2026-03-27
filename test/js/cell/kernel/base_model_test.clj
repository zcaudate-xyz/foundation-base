(ns js.cell.kernel.base-model-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.cell.kernel.base-model :as base-model]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.base-model/wrap-cell-args :added "4.0"}
(fact "puts the cell as first argument")

^{:refer js.cell.kernel.base-model/prep-view :added "4.0"}
(fact "prepares params of views")

^{:refer js.cell.kernel.base-model/get-view-dependents :added "4.0"}
(fact "gets all dependents for a view")

^{:refer js.cell.kernel.base-model/get-model-dependents :added "4.0"}
(fact "gets all dependents for a model")

^{:refer js.cell.kernel.base-model/run-tail-call :added "4.0"}
(fact "helper function for tail calls on run commands")

^{:refer js.cell.kernel.base-model/run-remote :added "4.0"}
(fact "runs the remote function")

^{:refer js.cell.kernel.base-model/remote-call :added "4.0"}
(fact "runs the remote call")

^{:refer js.cell.kernel.base-model/run-refresh :added "4.0"}
(fact "helper function for refresh")

^{:refer js.cell.kernel.base-model/refresh-view-dependents :added "4.0"}
(fact "refreshes view dependents")

^{:refer js.cell.kernel.base-model/refresh-view :added "4.0"}
(fact "refreshes a view")

^{:refer js.cell.kernel.base-model/refresh-view-remote :added "4.0"}
(fact "refreshes view remotely")

^{:refer js.cell.kernel.base-model/refresh-view-dependents-unthrottled :added "4.0"}
(fact "refreshes view dependents unthrottled")

^{:refer js.cell.kernel.base-model/refresh-model :added "4.0"}
(fact "refreshes a model")

^{:refer js.cell.kernel.base-model/get-model-deps :added "4.0"}
(fact "gets model dependencies")

^{:refer js.cell.kernel.base-model/get-unknown-deps :added "4.0"}
(fact "gets unknown dependencies")

^{:refer js.cell.kernel.base-model/create-throttle :added "4.0"}
(fact "creates a throttle")

^{:refer js.cell.kernel.base-model/create-view :added "4.0"}
(fact "creates a view")

^{:refer js.cell.kernel.base-model/add-model-attach :added "4.0"}
(fact "attaches a model")

^{:refer js.cell.kernel.base-model/add-model :added "4.0"}
(fact "adds a model")

^{:refer js.cell.kernel.base-model/remove-model :added "4.0"}
(fact "removes a model")

^{:refer js.cell.kernel.base-model/remove-view :added "4.0"}
(fact "removes a view")

^{:refer js.cell.kernel.base-model/model-update :added "4.0"}
(fact "updates a model")

^{:refer js.cell.kernel.base-model/view-update :added "4.0"}
(fact "updates a view")

^{:refer js.cell.kernel.base-model/view-set-input :added "4.0"}
(fact "sets view input")

^{:refer js.cell.kernel.base-model/trigger-model-raw :added "4.0"}
(fact "triggers model raw")

^{:refer js.cell.kernel.base-model/trigger-model :added "4.0"}
(fact "triggers a model")

^{:refer js.cell.kernel.base-model/trigger-view :added "4.0"}
(fact "triggers a view")

^{:refer js.cell.kernel.base-model/trigger-all :added "4.0"}
(fact "triggers all")

^{:refer js.cell.kernel.base-model/add-raw-callback :added "4.0"}
(fact "adds a raw callback")

^{:refer js.cell.kernel.base-model/remove-raw-callback :added "4.0"}
(fact "removes a raw callback")
