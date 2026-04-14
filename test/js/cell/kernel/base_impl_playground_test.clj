(ns js.cell.kernel.base-impl-playground-test
  (:require [js.cell.playground :as browser]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-runtime :as rt :with [defvar.js]]
             [js.core :as j]
              [js.cell.kernel.base-link :as base-link]
              [js.cell.kernel.base-link-local :as base-link-local]
              [js.cell.kernel.base-link-eval :as base-link-eval]
              [js.cell.kernel.base-model :as base-model]
              [js.cell.kernel.base-impl :as base-impl]]})

(def$.js Worker
  (require (+ (. process (cwd))
              "/node_modules/tiny-worker/lib/index.js")))

(fact:global
 {:setup     [(l/rt:restart :js)
              (l/rt:scaffold-imports :js)]
  :teardown  [(browser/stop-playground)
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

^{:refer js.cell.kernel.base-impl/new-cell :added "4.0"
  :setup [(fact:global :setup)]}
(fact "integrates new-cell with the playground worker"
  ^:hidden

  (notify/wait-on :js
    (. (-/reset-cell) ["init"]
       (then (repl/>notify))))
  => true

  (notify/wait-on :js
    (var cell (-/reset-cell))
    (var #{link} cell)
    (base-link/add-callback link "test" "hello" (repl/>notify))
    (base-link-eval/post-eval link
      (postMessage {:op "stream"
                    :signal "hello"
                    :status "ok"
                    :body {}})))
  => {"body" {}, "status" "ok", "op" "stream", "signal" "hello"}

  (notify/wait-on :js
    (var cell (-/reset-cell))
    (. (base-link-local/error (. cell ["link"]))
       (catch (repl/>notify))))
  => (contains-in
      {"body" ["error" integer?]
       "action" "@worker/error"
       "status" "error"
       "op" "call"}))

^{:refer js.cell.kernel.base-impl/call :added "4.0"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))]}
(fact "conducts calls against the playground worker"
  ^:hidden

  (j/<! (base-impl/call (-/CELL)
                        {:op "call"
                         :action "@worker/echo"
                         :body ["hello"]}))
  => (contains ["hello" integer?])

  (j/<! (base-impl/call (. (-/CELL) ["link"])
                        {:op "call"
                         :action "@worker/echo"
                         :body ["hello"]}))
  => (contains ["hello" integer?]))

^{:refer js.cell.kernel.base-impl/list-models :added "4.0"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (j/<! (. (base-model/add-model (-/CELL)
                                         "common/hello"
                                         {:echo {:handler base-link-local/echo
                                                 :defaultArgs ["TEST"]}})
                   ["init"]))
          (j/<! (. (base-model/add-model (-/CELL)
                                         "common/hello1"
                                         {:echo {:handler base-link-local/echo
                                                 :defaultArgs ["TEST"]}})
                   ["init"]))]}
(fact "lists playground-backed models"
  ^:hidden

  (set (!.js
        (base-impl/list-models (-/CELL))))
  => #{"common/hello" "common/hello1"})
