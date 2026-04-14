(ns js.cell.kernel.base-model-playground-test
  (:require [js.cell.playground :as browser]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-runtime :as rt :with [defvar.js]]
             [js.core :as j]
             [js.cell.kernel.base-link-local :as base-link-local]
             [js.cell.kernel.base-model :as base-model]
             [js.cell.kernel.base-impl :as base-impl]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup    [(do (l/rt:restart :js)
                 (l/rt:scaffold-imports :js))]
  :teardown [(browser/stop-playground)
             (l/rt:stop)]})

(defvar.js CELL
  []
  (return nil))

(defn.js reset-cell
  []
  (var cell (base-impl/new-cell
             (fn []
               (eval (@! (browser/play-worker true))))))
  (-/CELL-reset cell)
  (return cell))

(defn.js get-cell
  []
  (var cell (-/CELL))
  (when cell
    (return cell))
  (return (-/reset-cell)))

^{:refer js.cell.kernel.base-model/wrap-cell-args :added "4.0"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))]}
(fact "wraps cell args against the playground worker"
  ^:hidden

  (j/<! ((base-model/wrap-cell-args
          base-link-local/echo)
         {:cell (-/get-cell)
          :args ["hello"]}))
  => (contains ["hello" integer?]))

^{:refer js.cell.kernel.base-model/remote-call :added "4.0"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (j/<! (. (base-model/add-model (-/CELL)
                                         "hello"
                                         {:echo-async {:handler base-link-local/echo
                                                       :remoteHandler base-link-local/echo-async
                                                       :defaultArgs ["hello" 100]}})
                   ["init"]))]}
(fact "runs remote calls through the playground worker"
  ^:hidden

  (j/<! (base-model/remote-call (-/CELL) "hello" "echo_async" ["hello" 100] true))
  => (contains-in
      {"::" "view.run",
       "path" ["hello" "echo_async"],
       "pre" [false],
       "remote" [true ["hello" integer?]],
       "post" [false]}))

^{:refer js.cell.kernel.base-model/view-set-input :added "4.0"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (j/<! (. (base-model/add-model (-/CELL)
                                         "hello"
                                         {:ping {:handler base-link-local/echo
                                                 :defaultArgs ["foo"]}})
                   ["init"]))]}
(fact "updates view input against the playground worker"
  ^:hidden

  (j/<! (k/first
         (base-model/view-set-input
          (-/CELL) "hello" "ping" {:data ["bar"]})))
  => (contains-in
      {"path" ["hello" "ping"],
       "post" [false],
       "main" [true ["bar" integer?]],
       "pre" [false],
       "::" "view.run"}))

^{:refer js.cell.kernel.base-model/model-update :added "4.0"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (j/<! (. (base-model/add-model (-/CELL)
                                         "hello"
                                         {:ping {:handler base-link-local/ping
                                                 :defaultArgs []}
                                          :ping1 {:handler base-link-local/ping
                                                  :defaultArgs []
                                                  :deps ["ping"]}})
                   ["init"]))]}
(fact "updates playground-backed models"
  ^:hidden

  (j/<! (base-model/model-update
         (-/CELL)
         "hello"
         {}))
  => (contains-in
      {"ping1"
       {"path" ["hello" "ping1"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"},
       "ping"
       {"path" ["hello" "ping"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"}}))
