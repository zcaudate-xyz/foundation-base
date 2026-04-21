(ns xt.cell.kernel.base-impl-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [xt.lang.common-runtime :as rt :with [defvar.js]]
              [js.core :as j]
             [xt.cell.kernel.base-link :as base-link]
             [xt.cell.kernel.base-link-local :as base-link-local]
             [xt.cell.kernel.base-model :as base-model]
             [xt.cell.kernel.base-impl :as base-impl]
             [xt.cell.kernel.base-util :as base-util]
             [xt.cell.kernel.worker-impl :as worker-impl]
             [xt.cell.kernel.worker-mock :as worker-mock]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

(defvar.js CELL
  []
  (return nil))

(defn.js reset-cell
  []
  (var link (base-link/link-create
             {:create-fn
              (fn:> [listener]
                (worker-mock/create-worker listener {} true))}))
  (var cell (base-impl/new-cell link))
  (-/CELL-reset cell)
  (worker-impl/worker-init-signal (. link ["worker"]) {:done true})
  (return cell))

(defn.js get-cell
  []
  (var cell (-/CELL))
  (when cell
    (return cell))
  (return (-/reset-cell)))

^{:refer xt.cell.kernel.base-impl/new-cell-init :added "4.1"}
(fact "creates a record for asynchronous resolve"

  (set (!.js
        (xtd/obj-keys
         (base-impl/new-cell-init))))
  => #{"resolve" "current" "reject"}

  (notify/wait-on :js
    (:= (!:G INIT) (base-impl/new-cell-init))
    ((. INIT ["resolve"]) true)
    (. INIT ["current"]
       (then (repl/>notify))))
  => true

  (notify/wait-on :js
    (. INIT ["current"]
       (then (repl/>notify))))
  => true)

^{:refer xt.cell.kernel.base-impl/new-cell :added "4.1"
  :setup [(fact:global :setup)]}
(fact "makes the core link"

  (notify/wait-on :js
    (. (-/reset-cell) ["init"]
       (then (repl/>notify))))
  => true

  (notify/wait-on :js
    (var cell (-/reset-cell))
    (var #{link} cell)
    (base-link/add-callback link "test" "hello" (repl/>notify))
    (. link ["worker"]
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
      {"body" ["error" integer?],
       "action" "@worker/error",
       "status" "error",
       "op" "call"}))

^{:refer xt.cell.kernel.base-impl/list-models :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (!.js
           (base-model/add-model (-/CELL)
                                 "common/hello"
                                 {:echo {:handler base-link-local/echo}})
           (base-model/add-model (-/CELL)
                                 "common/hello1"
                                 {:echo {:handler base-link-local/echo}}))]}
(fact "lists all models"

  (set (!.js
        (base-impl/list-models (-/CELL))))
  => #{"common/hello" "common/hello1"})

^{:refer xt.cell.kernel.base-impl/call :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))]}
(fact "conducts a call, either for a link or cell"

  (j/<!
   (base-impl/call (-/CELL)
                   {:op "call"
                    :action "@worker/echo"
                    :body ["hello"]}))
  => (contains ["hello" integer?])

  (j/<!
   (base-impl/call (. (-/CELL) ["link"])
                   {:op "call"
                    :action "@worker/echo"
                    :body ["hello"]}))
  => (contains ["hello" integer?]))

^{:refer xt.cell.kernel.base-impl/model-get :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (notify/wait-on :js
            (base-model/add-model (-/CELL)
                                  "hello"
                                  {:echo {:handler base-link-local/echo
                                          :defaultArgs ["TEST"]}})
            (. (base-impl/model-get (-/CELL) "hello")
               ["init"]
               (then (repl/>notify))))]}
(fact "gets a model"

  (!.js
   (base-impl/model-get (-/CELL) "hello"))
  => (contains-in
      {"name" "hello",
       "views"
       {"echo"
        {"output"
         {"elapsed" integer?
          "current" ["TEST" integer?]
          "updated" integer?},
         "pipeline" {"remote" {}, "main" {}},
         "input" {"current" {"data" ["TEST"]}, "updated" integer?},
         "::" "event.view",
         "options" {},
         "listeners"
         {"@/cell"
          {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}},
       "deps" {},
       "init" {},
       "throttle" {"queued" {}, "active" {}}})

  (!.js
   (base-impl/model-get (-/CELL) "WRONG"))
  => nil)

^{:refer xt.cell.kernel.base-impl/model-ensure :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))]}
(fact "throws an error if model is not present"

  (!.js
   (base-impl/model-ensure (-/CELL) "WRONG"))
  => (throws))

^{:refer xt.cell.kernel.base-impl/list-views :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (notify/wait-on :js
            (base-model/add-model (-/CELL)
                                  "hello"
                                  {:echo {:handler base-link-local/echo
                                          :defaultArgs ["TEST"]}})
            (. (base-impl/model-get (-/CELL) "hello")
               ["init"]
               (then (repl/>notify))))]}
(fact "lists views in the model"

  (!.js
   (base-impl/list-views (-/CELL) "hello"))
  => ["echo"])

^{:refer xt.cell.kernel.base-impl/view-ensure :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (notify/wait-on :js
            (base-model/add-model (-/CELL)
                                  "hello"
                                  {:echo {:handler base-link-local/echo
                                          :defaultArgs ["TEST"]}})
            (. (base-impl/model-get (-/CELL) "hello")
               ["init"]
               (then (repl/>notify))))]}
(fact "gets the view"

  (!.js
   (xtd/second (base-impl/view-ensure (-/CELL)
                                    "hello"
                                    "echo")))
  => (contains-in
      {"::" "event.view",
       "pipeline" {"remote" {}, "main" {}},
       "output" {"elapsed" integer?
                 "current" ["TEST" integer?]
                 "updated" integer?},
       "input" {"current" {"data" ["TEST"]}
                "updated" integer?},
       "options" {},
       "listeners"
       {"@/cell"
        {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}))

^{:refer xt.cell.kernel.base-impl/view-access :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (notify/wait-on :js
            (base-model/add-model (-/CELL)
                                  "hello"
                                  {:echo {:handler base-link-local/echo
                                          :defaultArgs ["TEST"]}})
            (. (base-impl/model-get (-/CELL) "hello")
               ["init"]
               (then (repl/>notify))))]}
(fact "acts as the view access function"

  (!.js
   (base-impl/view-access (-/CELL)
                          "hello"
                          "echo"
                          (fn [view]
                            (return (. view ["input"] ["current"] ["data"])))
                          []))
  => ["TEST"]

  (!.js
   (base-impl/view-access (-/CELL)
                          "hello"
                          "wrong"
                          (fn [view]
                            (return true))
                          []))
  => nil)

^{:refer xt.cell.kernel.base-impl/add-listener :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (j/<! (. (base-model/add-model (-/CELL)
                                         "hello"
                                         {:echo {:handler base-link-local/echo
                                                 :defaultArgs ["TEST"]}})
                   ["init"]))]}
(fact "add listener to cell"

  (notify/wait-on :js
    (base-impl/add-listener (-/CELL)
                            ["hello" "echo"]
                            "@react/1234"
                            (fn [event]
                              (var #{type} event)
                              (when (== type "view.output")
                                (repl/notify event))))
    (base-model/refresh-view (-/CELL)
                             "hello"
                             "echo"))
  => (contains-in
      {"path" ["hello" "echo"],
       "type" "view.output",
       "meta" {"listener/id" "@react/1234", "listener/type" "cell"},
       "data"
       {"current" ["TEST" integer?],
        "updated" integer?
        "pending" true}}))

^{:refer xt.cell.kernel.base-impl/remove-listener :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (!.js
           (base-impl/add-listener (-/CELL)
                                   ["hello" "echo"]
                                   "@react/1234"
                                   (fn:>)))]}
(fact "remove listeners from cell"

  (!.js
   (base-impl/remove-listener (-/CELL)
                              ["hello" "echo"]
                              "@react/1234"))
  => map?)

^{:refer xt.cell.kernel.base-impl/list-listeners :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (!.js
           (base-impl/add-listener (-/CELL)
                                   ["hello" "echo"]
                                   "@react/1234"
                                   (fn:>))
           (base-impl/add-listener (-/CELL)
                                   ["hello" "echo"]
                                   "@react/5678"
                                   (fn:>)))]}
(fact "lists listeners in a cell path"

  (!.js
   (base-impl/list-listeners (-/CELL)
                             ["hello" "echo"]))
  => ["@react/1234" "@react/5678"])

^{:refer xt.cell.kernel.base-impl/list-all-listeners :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))
          (!.js
           (base-impl/add-listener (-/CELL)
                                   ["hello" "echo"]
                                   "@react/1234"
                                   (fn:>))
           (base-impl/add-listener (-/CELL)
                                   ["hello" "echo"]
                                   "@react/5678"
                                   (fn:>)))]}
(fact "lists all listeners in cell"

  (!.js
   (base-impl/list-all-listeners (-/CELL)))
  => {"hello" {"echo" ["@react/1234" "@react/5678"]}})

^{:refer xt.cell.kernel.base-impl/trigger-listeners :added "4.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/reset-cell) ["init"]
               (then (repl/>notify))))]}
(fact "triggers listeners"

  (!.js
   (base-impl/add-listener (-/CELL)
                           ["hello" "echo"]
                           "@react/1234"
                           (fn:>))
   [(base-impl/trigger-listeners (-/CELL)
                                 ["hello" "echo"]
                                 {})
    (base-impl/trigger-listeners (-/CELL)
                                 ["hello" "WRONG"]
                                 {})])
  => [["@react/1234"] []]

  (notify/wait-on :js
    (base-impl/add-listener (-/CELL)
                            ["hello" "echo"]
                            "@react/1234"
                            (repl/>notify))
    (base-impl/trigger-listeners (-/CELL)
                                 ["hello" "echo"]
                                 {:data [1 2 3]
                                  :meta {:custom "hello"}}))
  => {"data" [1 2 3]
      "path" ["hello" "echo"],
      "meta"
      {"custom" "hello",
       "listener/id" "@react/1234",
       "listener/type" "cell"}})
