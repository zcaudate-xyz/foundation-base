(ns js.cell-test
  (:require [std.lang :as l]
            [js.cell.runtime.emit :as emit]
            [std.lib.template :as template]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-repl :as repl]
             [js.cell :as cl]
             [js.cell.runtime.link :as runtime-link]]})

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:scaffold-imports :js)]
   :teardown [(l/rt:stop)]})

(defmacro node-worker-setup-check
  []
  (template/$
    (notify/wait-on :js
      (var cell
           (cl/make-cell
            (runtime-link/make-node-link ~(emit/node-script) {})))
      (. (. cell ["init"])
         (then
          (fn []
            (. (cl/setup-service cell
                                 {"dbs" {"main" {"kind" "cache"
                                                 "id" "main"}}})
               (then
                (fn []
                  (. (cl/get-service cell)
                     (then
                      (fn [service]
                        (. (cl/setup-bindings cell
                                              {"orders"
                                               {"views"
                                                {"all" {"type" "select"}}}})
                           (then
                            (fn []
                              (. (cl/get-bindings cell)
                                 (then
                                  (fn [bindings]
                                    (repl/notify {"service" service
                                                  "bindings" bindings})))))))))))))))
         (catch
          (fn [err]
            (repl/notify {"error" err})))))))

^{:refer js.cell/setup-service :added "4.1"}
(fact "sets worker service and bindings over a cell link"
  ^:hidden
  (node-worker-setup-check)
  => {"service" {"dbs" {"main" {"kind" "cache"
                                "id" "main"}}}
      "bindings" {"orders" {"views" {"all" {"type" "select"}}}}})


^{:refer js.cell/SERVICE :added "4.1"}
(fact "TODO")

^{:refer js.cell/BINDINGS :added "4.1"}
(fact "TODO")

^{:refer js.cell/fn-setup-service :added "4.1"}
(fact "TODO")

^{:refer js.cell/fn-get-service :added "4.1"}
(fact "TODO")

^{:refer js.cell/fn-setup-bindings :added "4.1"}
(fact "TODO")

^{:refer js.cell/fn-get-bindings :added "4.1"}
(fact "TODO")

^{:refer js.cell/actions-cell :added "4.1"}
(fact "TODO")

^{:refer js.cell/actions-baseline :added "4.1"}
(fact "TODO")

^{:refer js.cell/actions-init :added "4.1"}
(fact "TODO")

^{:refer js.cell/get-service :added "4.1"}
(fact "TODO")

^{:refer js.cell/setup-bindings :added "4.1"}
(fact "TODO")

^{:refer js.cell/get-bindings :added "4.1"}
(fact "TODO")