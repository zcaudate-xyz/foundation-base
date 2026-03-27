(ns js.cell.kernel.base-impl-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.cell.kernel.base-impl :as base-impl]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.base-impl/new-cell-init :added "4.0"}
(fact "creates a record for asynchronous resolve")

^{:refer js.cell.kernel.base-impl/new-cell :added "4.0"}
(fact "makes the core link")

^{:refer js.cell.kernel.base-impl/list-models :added "4.0"}
(fact "lists all models")

^{:refer js.cell.kernel.base-impl/call :added "4.0"}
(fact "conducts a call, either for a link or cell")

^{:refer js.cell.kernel.base-impl/model-get :added "4.0"}
(fact "gets a model")

^{:refer js.cell.kernel.base-impl/model-ensure :added "4.0"}
(fact "throws an error if model is not present")

^{:refer js.cell.kernel.base-impl/list-views :added "4.0"}
(fact "lists views in the model")

^{:refer js.cell.kernel.base-impl/view-ensure :added "4.0"}
(fact "gets the view")

^{:refer js.cell.kernel.base-impl/view-access :added "4.0"}
(fact "acts as the view access function")

^{:refer js.cell.kernel.base-impl/add-listener :added "4.0"}
(fact "add listener to cell")

^{:refer js.cell.kernel.base-impl/remove-listener :added "4.0"}
(fact "remove listeners from cell")

^{:refer js.cell.kernel.base-impl/list-listeners :added "4.0"}
(fact "lists listeners in a cell path")

^{:refer js.cell.kernel.base-impl/list-all-listeners :added "4.0"}
(fact "lists all listeners in cell")

^{:refer js.cell.kernel.base-impl/trigger-listeners :added "4.0"}
(fact "triggers listeners")
