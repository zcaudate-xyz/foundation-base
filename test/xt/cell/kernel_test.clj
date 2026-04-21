(ns xt.cell.kernel-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-spec :as xt]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-runtime :as rt :with [defvar.js]]
              [xt.lang.common-repl :as repl]
              [js.core :as j]
             [xt.cell.kernel :as kernel]
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
  (kernel/GD-reset cell)
  (kernel/GX-reset {"named" cell})
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
   "async"
   {"delay" {:handler base-link-local/echo-async
             :defaultArgs ["ASYNC" 20]
             :trigger "refresh"}})
  (return cell))

(defn.js make-link
  [handler]
  (return
   (base-link/link-create
    {:create-fn
     (fn:> [listener]
       (return
        {"postRequest"
         (fn [input]
           (return (handler listener input)))}))})))

^{:refer xt.cell.kernel/task-all :added "4.1"}
(fact "is exposed as the top-level task helper"
  (resolve 'xt.cell.kernel/task-all)
  => var?)

^{:refer xt.cell.kernel/make-cell :added "4.1"}
(fact "creates a cell and registers the raw callback"

  (!.js
   (var cell
        (kernel/make-cell
         {:create_fn
          (fn:> [listener]
            (worker-mock/create-worker listener {} true))}))
   [(. cell ["::"])
    (kernel/list-raw-callbacks cell)])
  => ["cell" ["@worker/::INIT" "@/raw"]])

^{:refer xt.cell.kernel/GD :added "4.1"}
(fact "returns the current cell binding"

  (!.js
   (kernel/GD-reset {"::" "cell"}))
  => [true {"watch" {}
            "value" {"::" "cell"}}])

^{:refer xt.cell.kernel/GX :added "4.1"}
(fact "returns the current annex binding"

  (!.js
   (kernel/GX-reset {"named" {"::" "cell"}}))
  => [true {"watch" {}
            "value" {"named" {"::" "cell"}}}])

^{:refer xt.cell.kernel/GX-val :added "4.1"}
(fact "gets a named entry from the annex"

  (!.js
   (kernel/GX-reset {"named" {"::" "cell"}})
   (kernel/GX-val "named"))
  => {"::" "cell"})

^{:refer xt.cell.kernel/GX-set :added "4.1"}
(fact "sets a named entry on the annex"

  (!.js
   (kernel/GX-reset {})
   (kernel/GX-set "named" {"::" "cell"}))
  => {"::" "cell"})

^{:refer xt.cell.kernel/get-cell :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "resolves the current cell from nil, name, or object contexts"

  (!.js
   [(. (kernel/get-cell nil) ["::"])
    (. (kernel/get-cell "named") ["::"])
    (. (kernel/get-cell {"cell" (-/CELL)}) ["::"])])
  => ["cell" "cell" "cell"])

^{:refer xt.cell.kernel/call :added "4.1"}
(fact "delegates raw calls to the underlying link"

  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" input.body}))))
    (. (kernel/call
        link
        {"op" "call"
         "action" "@worker/echo"
         "body" ["hello" 1]})
       (then (repl/>notify))))
  => ["hello" 1])

^{:refer xt.cell.kernel/fn-call-cell :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "calls a handler with the resolved cell"

  (!.js
   (kernel/fn-call-cell
    (fn [cell a b]
      (return [(. cell ["::"]) a b]))
    ["x" "y"]
    nil))
  => ["cell" "x" "y"])

^{:refer xt.cell.kernel/fn-call-model :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "calls a handler with the resolved model id"

  (!.js
   (kernel/fn-call-model
    (fn [cell model-id arg]
      (return [(. cell ["::"]) model-id arg]))
    "hello"
    ["x"]
    nil))
  => ["cell" "hello" "x"])

^{:refer xt.cell.kernel/fn-call-view :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "calls a handler with the resolved model and view ids"

  (!.js
   (kernel/fn-call-view
    (fn [cell model-id view-id arg]
      (return [(. cell ["::"]) model-id view-id arg]))
    ["hello" "echo"]
    ["x"]
    nil))
  => ["cell" "hello" "echo" "x"])

^{:refer xt.cell.kernel/fn-access-cell :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "maps an accessor across every view in the cell"

  (!.js
   (kernel/fn-access-cell
    (fn [view]
      (return (. view ["::"])))
    nil))
  => {"hello" {"echo" "event.view"}
      "async" {"delay" "event.view"}})

^{:refer xt.cell.kernel/fn-access-model :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "maps an accessor across every view in a model"

  (!.js
   (kernel/fn-access-model
    (fn [view]
      (return (. view ["::"])))
    "hello"
    nil))
  => {"echo" "event.view"})

^{:refer xt.cell.kernel/fn-access-view :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "accesses a single resolved view"

  (!.js
   (kernel/fn-access-view
    (fn [view]
      (return (. view ["::"])))
    ["hello" "echo"]
    []
    nil))
  => "event.view")

^{:refer xt.cell.kernel/list-models :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "lists model ids in the current cell"

  (!.js
   (kernel/list-models nil))
  => ["hello" "async"])

^{:refer xt.cell.kernel/list-views :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "lists view ids in a model"

  (!.js
   (kernel/list-views "hello" nil))
  => ["echo"])

^{:refer xt.cell.kernel/get-model :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets a model from the current cell"

  (!.js
   (kernel/get-model "hello" nil))
  => (contains {"name" "hello"}))

^{:refer xt.cell.kernel/get-view :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets a view from the current cell"

  (!.js
   (kernel/get-view ["hello" "echo"] nil))
  => (contains {"::" "event.view"}))

^{:refer xt.cell.kernel/cell-vals :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets current values across all views"

  (!.js
   (kernel/cell-vals nil))
  => {"hello" {"echo" nil}
      "async" {"delay" nil}})

^{:refer xt.cell.kernel/cell-outputs :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets output records across all views"

  (!.js
   [(. (kernel/cell-outputs nil) ["hello"] ["echo"] ["type"])
    (. (kernel/cell-outputs nil) ["async"] ["delay"] ["type"])])
  => ["output" "output"])

^{:refer xt.cell.kernel/cell-inputs :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets input records across all views"

  (!.js
   [(. (kernel/cell-inputs nil) ["hello"] ["echo"] ["current"] ["data"])
    (. (kernel/cell-inputs nil) ["async"] ["delay"] ["current"] ["data"])])
  => [["HELLO"]
      ["ASYNC" 20]])

^{:refer xt.cell.kernel/cell-trigger :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "triggers matching views across the whole cell"

  (!.js
   (kernel/cell-trigger "refresh" {} nil))
  => {"hello" ["echo"]
      "async" ["delay"]})

^{:refer xt.cell.kernel/model-outputs :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets output records for a model"

  (!.js
   (. (kernel/model-outputs "hello" nil) ["echo"] ["type"]))
  => "output")

^{:refer xt.cell.kernel/model-vals :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets current values for a model"

  (!.js
   (kernel/model-vals "hello" nil))
  => {"echo" nil})

^{:refer xt.cell.kernel/model-is-errored :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "detects errored views in a model"

  (!.js
   (kernel/view-set-val ["hello" "echo"] "ERR" true nil)
   (kernel/model-is-errored "hello" nil))
  => true)

^{:refer xt.cell.kernel/model-is-pending :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "reports whether any view in a model is pending"

  (!.js
   (kernel/model-is-pending "hello" nil))
  => false)

^{:refer xt.cell.kernel/add-model-attach :added "4.1"
  :setup [(!.js (-/reset-cell) true)]}
(fact "attaches a model through the wrapper"

  (!.js
   (kernel/add-model-attach
    "hello"
    {"echo" {:handler base-link-local/echo
             :defaultArgs ["HELLO"]}}
    nil))
  => (contains {"name" "hello"}))

^{:refer xt.cell.kernel/add-model :added "4.1"
  :setup [(!.js (-/reset-cell) true)]}
(fact "adds and initializes a model through the wrapper"

  (!.js
   (kernel/add-model
    "hello"
    {"echo" {:handler base-link-local/echo
             :defaultArgs ["HELLO"]}}
    nil))
  => (contains {"name" "hello"}))

^{:refer xt.cell.kernel/remove-model :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "removes a model through the wrapper"

  (!.js
   (kernel/remove-model "async" nil))
  => (contains {"name" "async"}))

^{:refer xt.cell.kernel/model-update :added "4.1"}
(fact "is exposed as the model update wrapper"
  (resolve 'xt.cell.kernel/model-update)
  => var?)

^{:refer xt.cell.kernel/model-trigger :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "triggers a named model through the wrapper"

  (!.js
   (kernel/model-trigger "hello" "refresh" {} nil))
  => ["echo"])

^{:refer xt.cell.kernel/view-success :added "4.1"}
(fact "is exposed as the view success accessor"
  (resolve 'xt.cell.kernel/view-success)
  => var?)

^{:refer xt.cell.kernel/view-val :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets the current value of a view"

  (!.js
   (kernel/view-val ["hello" "echo"] nil))
  => nil)

^{:refer xt.cell.kernel/view-get-input :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets the input record of a view"

  (!.js
   (. (kernel/view-get-input ["hello" "echo"] nil) ["current"] ["data"]))
  => ["HELLO"])

^{:refer xt.cell.kernel/view-get-output :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets the output record of a view"

  (!.js
   (kernel/view-get-output ["hello" "echo"] nil))
  => (contains {"type" "output"}))

^{:refer xt.cell.kernel/view-set-val :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "sets the output value of a view"

  (!.js
   (kernel/view-set-val ["hello" "echo"] "VALUE" false nil)
   (kernel/view-val ["hello" "echo"] nil))
  => "VALUE")

^{:refer xt.cell.kernel/view-get-time-updated :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets the last updated timestamp for a view"

  (!.js
   (kernel/view-set-val ["hello" "echo"] "VALUE" false nil)
   (kernel/view-get-time-updated ["hello" "echo"] nil))
  => integer?)

^{:refer xt.cell.kernel/view-is-errored :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "checks whether a view is errored"

  (!.js
   (kernel/view-set-val ["hello" "echo"] "ERR" true nil)
   (kernel/view-is-errored ["hello" "echo"] nil))
  => true)

^{:refer xt.cell.kernel/view-is-pending :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "checks whether a view is pending"

  (!.js
   (kernel/view-is-pending ["hello" "echo"] nil))
  => false)

^{:refer xt.cell.kernel/view-get-time-elapsed :added "4.1"}
(fact "is exposed as the elapsed-time accessor"
  (resolve 'xt.cell.kernel/view-get-time-elapsed)
  => var?)

^{:refer xt.cell.kernel/view-set-input :added "4.1"}
(fact "is exposed as the set-input wrapper"
  (resolve 'xt.cell.kernel/view-set-input)
  => var?)

^{:refer xt.cell.kernel/view-refresh :added "4.1"}
(fact "is exposed as the view refresh wrapper"
  (resolve 'xt.cell.kernel/view-refresh)
  => var?)

^{:refer xt.cell.kernel/view-update :added "4.1"}
(fact "is exposed as the view update wrapper"
  (resolve 'xt.cell.kernel/view-update)
  => var?)

^{:refer xt.cell.kernel/view-ensure :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "ensures and returns a model/view pair"

  (!.js
   [(xt/x:get-key (xtd/first (kernel/view-ensure ["hello" "echo"] nil)) "name")
    (xt/x:get-key (xtd/second (kernel/view-ensure ["hello" "echo"] nil)) "::")])
  => ["hello" "event.view"])

^{:refer xt.cell.kernel/view-call-remote :added "4.1"}
(fact "is exposed as the remote call wrapper"
  (resolve 'xt.cell.kernel/view-call-remote)
  => var?)

^{:refer xt.cell.kernel/view-refresh-remote :added "4.1"}
(fact "is exposed as the remote refresh wrapper"
  (resolve 'xt.cell.kernel/view-refresh-remote)
  => var?)

^{:refer xt.cell.kernel/view-trigger :added "4.1"}
(fact "is exposed as the single-view trigger wrapper"
  (resolve 'xt.cell.kernel/view-trigger)
  => var?)

^{:refer xt.cell.kernel/view-for :added "4.1"}
(fact "is exposed as the post-update view accessor"
  (resolve 'xt.cell.kernel/view-for)
  => var?)

^{:refer xt.cell.kernel/view-for-input :added "4.1"}
(fact "is exposed as the post-input view accessor"
  (resolve 'xt.cell.kernel/view-for-input)
  => var?)

^{:refer xt.cell.kernel/get-val :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "gets a nested subpath from the current view value"

  (!.js
   (kernel/view-set-val ["hello" "echo"] {"nested" {"value" 1}} false nil)
   (kernel/get-val ["hello" "echo"] ["nested" "value"] nil))
  => 1)

^{:refer xt.cell.kernel/get-for :added "4.1"}
(fact "is exposed as the post-update subpath accessor"
  (resolve 'xt.cell.kernel/get-for)
  => var?)

^{:refer xt.cell.kernel/nil-view :added "4.1"}
(fact "is exposed as the nil-view helper"
  (resolve 'xt.cell.kernel/nil-view)
  => var?)

^{:refer xt.cell.kernel/nil-model :added "4.1"}
(fact "is exposed as the nil-model helper"
  (resolve 'xt.cell.kernel/nil-model)
  => var?)

^{:refer xt.cell.kernel/clear-listeners :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "clears all registered listeners"

  (!.js
   (kernel/add-listener ["hello" "echo"] "@r/1" (fn:>) nil nil nil)
   (kernel/clear-listeners nil)
   (kernel/list-all-listeners nil))
  => {})

^{:refer xt.cell.kernel/add-listener :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "adds a listener to a view path"

  (!.js
   (kernel/add-listener ["hello" "echo"] "@r/1" (fn:>) nil nil nil)
   (kernel/list-listeners ["hello" "echo"] nil))
  => ["@r/1"])

^{:refer xt.cell.kernel/remove-listener :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "removes a listener from a view path"

  (!.js
   (kernel/add-listener ["hello" "echo"] "@r/1" (fn:>) nil nil nil)
   (kernel/remove-listener ["hello" "echo"] "@r/1" nil)
   (kernel/list-listeners ["hello" "echo"] nil))
  => [])

^{:refer xt.cell.kernel/list-listeners :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "lists listeners registered on a view path"

  (!.js
   (kernel/add-listener ["hello" "echo"] "@r/1" (fn:>) nil nil nil)
   (kernel/add-listener ["hello" "echo"] "@r/2" (fn:>) nil nil nil)
   (kernel/list-listeners ["hello" "echo"] nil))
  => ["@r/1" "@r/2"])

^{:refer xt.cell.kernel/list-all-listeners :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "lists every listener grouped by model and view"

  (!.js
   (kernel/add-listener ["hello" "echo"] "@r/1" (fn:>) nil nil nil)
   (kernel/list-all-listeners nil))
  => {"hello" {"echo" ["@r/1"]}})

^{:refer xt.cell.kernel/add-raw-callback :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "adds a raw callback to the underlying link"

  (!.js
   (kernel/add-raw-callback "custom" true (fn:>) nil)
   (kernel/list-raw-callbacks nil))
  => ["custom"])

^{:refer xt.cell.kernel/remove-raw-callback :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "removes a raw callback from the underlying link"

  (!.js
   (kernel/add-raw-callback "custom" true (fn:>) nil)
   (kernel/remove-raw-callback "custom" nil)
   (kernel/list-raw-callbacks nil))
  => [])

^{:refer xt.cell.kernel/list-raw-callbacks :added "4.1"
  :setup [(!.js (-/seed-cell) true)]}
(fact "lists raw callbacks on the underlying link"

  (!.js
   (kernel/list-raw-callbacks nil))
  => [])
