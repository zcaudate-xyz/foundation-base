(ns js.cell.kernel.base-link-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.core :as j]
             [js.cell.kernel.base-link :as base-link]
             [js.cell.kernel.worker-mock :as worker-mock]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.base-link/link-listener-call :added "4.0"}
(fact "resolves a call to the link")

^{:refer js.cell.kernel.base-link/link-listener-event :added "4.0"}
(fact "notifies all registered callbacks")

^{:refer js.cell.kernel.base-link/link-listener :added "4.0"}
(fact "constructs a link listener")

^{:refer js.cell.kernel.base-link/link-create-worker :added "4.0"}
(fact "helper function to create a worker")

^{:refer js.cell.kernel.base-link/link-create :added "4.0"}
(fact "creates a link from url")

^{:refer js.cell.kernel.base-link/link-active :added "4.0"}
(fact "gets the calls that are active")

^{:refer js.cell.kernel.base-link/add-callback :added "4.0"}
(fact "adds a callback to the link")

^{:refer js.cell.kernel.base-link/list-callbacks :added "4.0"}
(fact "lists all callbacks on the link")

^{:refer js.cell.kernel.base-link/remove-callback :added "4.0"}
(fact "removes a callback on the link")

^{:refer js.cell.kernel.base-link/call-id :added "4.0"}
(fact "gets the call id")

^{:refer js.cell.kernel.base-link/call :added "4.0"}
(fact "calls the link with an event")
