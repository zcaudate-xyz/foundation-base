(ns xt.cell.kernel.base-impl-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.cell.kernel.base-link :as base-link]
             [xt.cell.kernel.base-link-local :as base-link-local]
             [xt.cell.kernel.base-model :as base-model]
             [xt.cell.kernel.base-impl :as base-impl]
             [xt.cell.kernel.inner-impl :as inner-impl]
             [xt.cell.kernel.inner-mock :as inner-mock]]})

(l/script- :js
  {:require [[xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd] [xt.cell.kernel.base-link :as base-link] [xt.cell.kernel.base-link-local :as base-link-local] [xt.cell.kernel.base-model :as base-model] [xt.cell.kernel.base-impl :as base-impl] [xt.cell.kernel.inner-impl :as inner-impl] [xt.cell.kernel.inner-mock :as inner-mock] [xt.lang.common-repl :as repl] [js.core :as j]] :runtime :basic})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

(defn.xt make-link
  []
  (return
   (base-link/link-create
    {:create-fn
     (fn:> [listener]
       (inner-mock/create-worker listener {} true))})))

(defn.xt make-cell
  []
  (var link (-/make-link))
  (var cell (base-impl/new-cell link))
  (inner-impl/worker-init-signal (. link ["worker"]) {:done true})
  (return cell))

(defn.xt make-cell-with-model
  [views]
  (var cell (-/make-cell))
  (return (. (base-model/add-model cell "hello" views)
             ["init"]
             (then (fn []
                     (return cell))))))

^{:refer xt.cell.kernel.base-impl/new-cell-init :added "4.0"}
(fact "creates a record for asynchronous resolve"

  (set
   (!.js
    (xtd/obj-keys
     (base-impl/new-cell-init))))
  => #{"resolve" "current" "reject"}

  (notify/wait-on :js
                  (:= (!:G INIT) (base-impl/new-cell-init))
                  ((. INIT ["resolve"]) true)
                  (. INIT ["current"]
                     (then (repl/>notify))))
  => true)

^{:refer xt.cell.kernel.base-impl/new-cell :added "4.0"}
(fact "creates a cell record and resolves init on worker init"

  (notify/wait-on :js
    (var cell (-/make-cell))
    (. cell ["init"]
       (then (repl/>notify))))
  => true

  (!.js
   (xt/x:get-key (-/make-cell) "::"))
  => "cell")

^{:refer xt.cell.kernel.base-impl/list-models :added "4.0"}
(fact "lists models on a fresh cell"

  (!.js
   (base-impl/list-models (-/make-cell)))
  => [])

^{:refer xt.cell.kernel.base-impl/call :added "4.0"}
(fact "conducts a call against a cell or link"

  (j/<!
   (base-impl/call (-/make-cell)
                   {:op "call"
                    :action "@worker/echo"
                    :body ["hello"]}))
  => (contains ["hello" integer?])

  (j/<!
   (base-impl/call (-/make-link)
                   {:op "call"
                    :action "@worker/echo"
                    :body ["hello"]}))
  => (contains ["hello" integer?]))

^{:refer xt.cell.kernel.base-impl/model-get :added "4.0"}
(fact "returns nil for a missing model"

  (!.js
   (base-impl/model-get (-/make-cell) "missing"))
  => nil)

^{:refer xt.cell.kernel.base-impl/model-ensure :added "4.0"}
(fact "throws for a missing model"

  (!.js
   (base-impl/model-ensure (-/make-cell) "missing"))
  => (throws))

^{:refer xt.cell.kernel.base-impl/view-access :added "4.0"}
(fact "returns nil for a missing view"

  (!.js
   (base-impl/view-access (-/make-cell)
                          "missing"
                          "echo"
                          (fn [view]
                            (return true))
                          []))
  => nil)

^{:refer xt.cell.kernel.base-impl/add-listener :added "4.0"}
(fact "adds, lists, removes, and triggers keyed listeners on a cell"

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell
                           ["hello" "echo"]
                           "@react/1234"
                           (fn:> [event]
                             (. event ["path"]))
                           nil
                           nil)
   (base-impl/list-listeners cell ["hello" "echo"]))
  => ["@react/1234"]

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell ["hello" "echo"] "@react/1234" (fn:>) nil nil)
   (base-impl/add-listener cell ["hello" "echo"] "@react/5678" (fn:>) nil nil)
   (base-impl/list-all-listeners cell))
  => {"hello" {"echo" ["@react/1234" "@react/5678"]}}

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell ["hello" "echo"] "@react/1234" (fn:>) nil nil)
   (base-impl/remove-listener cell ["hello" "echo"] "@react/1234"))
  => map?

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell ["hello" "echo"] "@react/1234" (fn:>) nil nil)
   (base-impl/trigger-listeners cell ["hello" "echo"] {}))
  => ["@react/1234"])

^{:refer xt.cell.kernel.base-impl/list-views :added "4.1"}
(fact "lists views attached to a model"

  (notify/wait-on :js
    (. (-/make-cell-with-model
        {:echo {:handler base-link-local/echo
                :defaultArgs ["hello"]}})
       (then (fn [cell]
               (repl/notify
                (base-impl/list-views cell "hello"))))))
  => ["echo"])

^{:refer xt.cell.kernel.base-impl/view-ensure :added "4.1"}
(fact "returns the model and view pair for an existing view"

  (notify/wait-on :js
    (. (-/make-cell-with-model
        {:echo {:handler base-link-local/echo
                :defaultArgs ["hello"]}})
       (then (fn [cell]
               (repl/notify
                (base-impl/view-ensure cell "hello" "echo"))))))
  => (contains [map? map?]))

^{:refer xt.cell.kernel.base-impl/remove-listener :added "4.1"}
(fact "removes a keyed listener from a path"

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell ["hello" "echo"] "@react/1234" (fn:>) nil nil)
   (base-impl/remove-listener cell ["hello" "echo"] "@react/1234"))
  => (contains-in {"meta" {"listener/id" "@react/1234"
                           "listener/type" "cell"}})

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell ["hello" "echo"] "@react/1234" (fn:>) nil nil)
   (base-impl/remove-listener cell ["hello" "echo"] "@react/1234")
   (base-impl/list-listeners cell ["hello" "echo"]))
  => [])

^{:refer xt.cell.kernel.base-impl/list-listeners :added "4.1"}
(fact "lists listener ids for a single view path"

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell ["hello" "echo"] "@react/1234" (fn:>) nil nil)
   (base-impl/list-listeners cell ["hello" "echo"]))
  => ["@react/1234"])

^{:refer xt.cell.kernel.base-impl/list-all-listeners :added "4.1"}
(fact "lists all listener ids grouped by model and view"

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell ["hello" "echo"] "@react/1234" (fn:>) nil nil)
   (base-impl/add-listener cell ["hello" "echo"] "@react/5678" (fn:>) nil nil)
   (base-impl/list-all-listeners cell))
  => {"hello" {"echo" ["@react/1234" "@react/5678"]}})

^{:refer xt.cell.kernel.base-impl/trigger-listeners :added "4.1"}
(fact "triggers listener callbacks registered on a path"

  (!.js
   (var cell (-/make-cell))
   (base-impl/add-listener cell ["hello" "echo"] "@react/1234" (fn:>) nil nil)
   (base-impl/trigger-listeners cell ["hello" "echo"] {}))
  => ["@react/1234"])
