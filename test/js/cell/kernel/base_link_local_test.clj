(ns js.cell.kernel.base-link-local-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.cell.kernel.base-link-local :as base-link-local]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.base-link-local/trigger :added "4.0"}
(fact "performs trigger call")

^{:refer js.cell.kernel.base-link-local/trigger-async :added "4.0"}
(fact "performs trigger-async call")

^{:refer js.cell.kernel.base-link-local/set-final-status :added "4.0"}
(fact "performs set-final-status call")

^{:refer js.cell.kernel.base-link-local/get-final-status :added "4.0"}
(fact "performs get-final-status call")

^{:refer js.cell.kernel.base-link-local/set-eval-status :added "4.0"}
(fact "performs set-eval-status call")

^{:refer js.cell.kernel.base-link-local/get-eval-status :added "4.0"}
(fact "performs get-eval-status call")

^{:refer js.cell.kernel.base-link-local/get-action-list :added "4.0"}
(fact "performs get-action-list call")

^{:refer js.cell.kernel.base-link-local/get-action-entry :added "4.0"}
(fact "performs get-action-entry call")

^{:refer js.cell.kernel.base-link-local/ping :added "4.0"}
(fact "performs ping call")

^{:refer js.cell.kernel.base-link-local/ping-async :added "4.0"}
(fact "performs ping-async call")

^{:refer js.cell.kernel.base-link-local/echo :added "4.0"}
(fact "performs echo call")

^{:refer js.cell.kernel.base-link-local/echo-async :added "4.0"}
(fact "performs echo-async call")

^{:refer js.cell.kernel.base-link-local/error :added "4.0"}
(fact "performs error call")

^{:refer js.cell.kernel.base-link-local/error-async :added "4.0"}
(fact "performs error-async call")

^{:refer js.cell.kernel.base-link-local/tmpl-link-action :added "4.0"}
(fact "performs a template"
  ^:hidden
  
  (base-link-local/tmpl-link-action
   '[trigger js.cell.kernel.worker-state/fn-trigger])
  => (contains '(defn.js trigger)))
