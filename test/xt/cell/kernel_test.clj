(ns xt.cell.kernel-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.old.event-view :as base-view]
             [xt.cell.kernel.base-impl :as impl-common]
             [xt.cell.kernel.base-link-local :as link-fn]
             [xt.cell.kernel.inner-impl :as inner-impl]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.cell.kernel :as cl]]})

(l/script- :js
  {:require [[xt.lang.spec-base :as xt] [xt.lang.common-lib :as k] [xt.old.event-view :as base-view] [xt.cell.kernel.base-impl :as impl-common] [xt.cell.kernel.base-link-local :as link-fn] [xt.cell.kernel.inner-impl :as inner-impl] [xt.cell.kernel.inner-mock :as inner-mock] [xt.cell.kernel :as cl] [xt.lang.common-repl :as repl]] :runtime :basic})

(l/script- :lua
  {:require [[xt.lang.spec-base :as xt] [xt.lang.common-lib :as k] [xt.old.event-view :as base-view] [xt.cell.kernel.base-impl :as impl-common] [xt.cell.kernel.base-link-local :as link-fn] [xt.cell.kernel.inner-impl :as inner-impl] [xt.cell.kernel.inner-mock :as inner-mock] [xt.cell.kernel :as cl] [xt.lang.common-repl :as repl]] :runtime :basic})

(l/script- :python
  {:require [[xt.lang.spec-base :as xt] [xt.lang.common-lib :as k] [xt.old.event-view :as base-view] [xt.cell.kernel.base-impl :as impl-common] [xt.cell.kernel.base-link-local :as link-fn] [xt.cell.kernel.inner-impl :as inner-impl] [xt.cell.kernel.inner-mock :as inner-mock] [xt.cell.kernel :as cl] [xt.lang.common-repl :as repl]] :runtime :basic})

(defn.xt reset-cell
  []
  (var cell
       (cl/make-cell
        {:create-fn
         (fn:> [listener]
           (inner-mock/create-worker listener {} true))}))
  (cl/GD-reset cell)
  (cl/GX-reset {})
  (inner-impl/worker-init-signal (. cell ["link"] ["worker"]) {:done true})
  (return cell))

(fact:global
  {:setup    [(do (l/rt:restart)
                  (l/rt:scaffold-imports :js))]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel/make-cell :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "manages the current cell context"

  (!.js
   (cl/GD))
  => map?

  (!.js
   (cl/GX-set "p0" (cl/GD))
   (xt/x:obj-keys (cl/GX)))
  => ["p0"]

  (!.js
   (cl/GX-val "p0"))
  => map?

  (!.js
   (cl/get-cell))
  => map?

  (!.js
   (cl/get-cell "p0"))
  => map?

  (notify/wait-on :js
    (. (cl/call (cl/GD)
                {:op "call"
                 :action "@worker/ping.async"
                 :body [50]})
       (then (repl/>notify))))
  => (contains ["pong" integer?])

  (!.lua
   (cl/GD))
  => map?

  (!.lua
   (cl/GX-set "p0" (cl/GD))
   (xt/x:obj-keys (cl/GX)))
  => ["p0"]

  (!.lua
   (cl/GX-val "p0"))
  => map?

  (!.lua
   (cl/get-cell))
  => map?

  (!.lua
   (cl/get-cell "p0"))
  => map?

  (notify/wait-on :lua
    (. (cl/call (cl/GD)
                {:op "call"
                 :action "@worker/ping.async"
                 :body [50]})
       (then (repl/>notify))))
  => (contains ["pong" integer?])

  (!.py
   (cl/GD))
  => map?

  (!.py
   (cl/GX-set "p0" (cl/GD))
   (xt/x:obj-keys (cl/GX)))
  => ["p0"]

  (!.py
   (cl/GX-val "p0"))
  => map?

  (!.py
   (cl/get-cell))
  => map?

  (!.py
   (cl/get-cell "p0"))
  => map?

  (notify/wait-on :python
    (. (cl/call (cl/GD)
                {:op "call"
                 :action "@worker/ping.async"
                 :body [50]})
       (then (repl/>notify))))
  => (contains ["pong" integer?]))

^{:refer xt.cell.kernel/fn-call-cell :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (fn []
                                (. (cl/add-model "hello"
                                                 {:echo {:handler link-fn/echo
                                                         :defaultArgs ["HELLO"]}})
                                   ["init"]
                                   (then (repl/>notify)))))))]}
(fact "wraps cell, model and view access"

  (!.js
   (cl/fn-call-cell k/identity []))
  => map?

  (!.js
   (cl/fn-call-model impl-common/model-get "hello" []))
  => map?

  (!.js
   (xt/x:second (cl/fn-call-view impl-common/view-ensure ["hello" "echo"] [])))
  => map?

  (!.js
   (cl/fn-access-cell base-view/get-current))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (!.js
   (cl/fn-access-model base-view/get-current "hello"))
  => (contains-in {"echo" ["HELLO" integer?]})

  (!.js
   (cl/fn-access-view base-view/get-current ["hello" "echo"] []))
  => (contains ["HELLO" integer?])

  (!.js
   (cl/list-models))
  => ["hello"]

  (!.js
   (cl/list-views "hello"))
  => ["echo"]

  (!.js
   (cl/get-model "hello"))
  => map?

  (!.js
   (cl/get-view ["hello" "echo"]))
  => map?

  (!.lua
   (cl/fn-call-cell k/identity []))
  => map?

  (!.lua
   (cl/fn-call-model impl-common/model-get "hello" []))
  => map?

  (!.lua
   (xt/x:second (cl/fn-call-view impl-common/view-ensure ["hello" "echo"] [])))
  => map?

  (!.lua
   (cl/fn-access-cell base-view/get-current))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (!.lua
   (cl/fn-access-model base-view/get-current "hello"))
  => (contains-in {"echo" ["HELLO" integer?]})

  (!.lua
   (cl/fn-access-view base-view/get-current ["hello" "echo"] []))
  => (contains ["HELLO" integer?])

  (!.lua
   (cl/list-models))
  => ["hello"]

  (!.lua
   (cl/list-views "hello"))
  => ["echo"]

  (!.lua
   (cl/get-model "hello"))
  => map?

  (!.lua
   (cl/get-view ["hello" "echo"]))
  => map?

  (!.py
   (cl/fn-call-cell k/identity []))
  => map?

  (!.py
   (cl/fn-call-model impl-common/model-get "hello" []))
  => map?

  (!.py
   (xt/x:second (cl/fn-call-view impl-common/view-ensure ["hello" "echo"] [])))
  => map?

  (!.py
   (cl/fn-access-cell base-view/get-current))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (!.py
   (cl/fn-access-model base-view/get-current "hello"))
  => (contains-in {"echo" ["HELLO" integer?]})

  (!.py
   (cl/fn-access-view base-view/get-current ["hello" "echo"] []))
  => (contains ["HELLO" integer?])

  (!.py
   (cl/list-models))
  => ["hello"]

  (!.py
   (cl/list-views "hello"))
  => ["echo"]

  (!.py
   (cl/get-model "hello"))
  => map?

  (!.py
   (cl/get-view ["hello" "echo"]))
  => map?)

^{:refer xt.cell.kernel/model-update :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (fn []
                                (. (cl/add-model "hello"
                                                 {:echo {:handler link-fn/echo
                                                         :defaultArgs ["HELLO"]
                                                         :trigger {"hello" true}}})
                                   ["init"]
                                   (then (repl/>notify)))))))]}
(fact "updates and triggers models through the kernel"

  (!.js
   (cl/cell-vals))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (!.js
   (cl/cell-outputs))
  => (contains-in {"hello" {"echo" {"current" ["HELLO" integer?]
                                    "updated" integer?}}})

  (!.js
   (cl/cell-inputs))
  => (contains-in {"hello" {"echo" {"current" {"data" ["HELLO"]}
                                    "updated" integer?}}})

  (!.js
   (cl/model-vals "hello"))
  => (contains-in {"echo" ["HELLO" integer?]})

  (!.js
   (cl/model-outputs "hello"))
  => (contains-in {"echo" {"current" ["HELLO" integer?]
                           "updated" integer?}})

  (!.js
   (cl/model-is-errored "hello"))
  => false

  (!.js
   (cl/model-is-pending "hello"))
  => false

  (!.js
   (cl/model-trigger "hello" "hello" {}))
  => ["echo"]

  (!.js
   (cl/cell-trigger "hello" {}))
  => {"hello" ["echo"]}

  (notify/wait-on :js
    (. (cl/model-update "hello")
       (then (repl/>notify))))
  => (contains-in
      {"echo"
       {"path" ["hello" "echo"]
        "post" [false]
        "main" [true ["HELLO" integer?]]
        "pre" [false]
        "::" "view.run"}})

  (!.lua
   (cl/cell-vals))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (!.lua
   (cl/cell-outputs))
  => (contains-in {"hello" {"echo" {"current" ["HELLO" integer?]
                                    "updated" integer?}}})

  (!.lua
   (cl/cell-inputs))
  => (contains-in {"hello" {"echo" {"current" {"data" ["HELLO"]}
                                    "updated" integer?}}})

  (!.lua
   (cl/model-vals "hello"))
  => (contains-in {"echo" ["HELLO" integer?]})

  (!.lua
   (cl/model-outputs "hello"))
  => (contains-in {"echo" {"current" ["HELLO" integer?]
                           "updated" integer?}})

  (!.lua
   (cl/model-is-errored "hello"))
  => false

  (!.lua
   (cl/model-is-pending "hello"))
  => false

  (!.lua
   (cl/model-trigger "hello" "hello" {}))
  => ["echo"]

  (!.lua
   (cl/cell-trigger "hello" {}))
  => {"hello" ["echo"]}

  (notify/wait-on :lua
    (. (cl/model-update "hello")
       (then (repl/>notify))))
  => (contains-in
      {"echo"
       {"path" ["hello" "echo"]
        "post" [false]
        "main" [true ["HELLO" integer?]]
        "pre" [false]
        "::" "view.run"}})

  (!.py
   (cl/cell-vals))
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (!.py
   (cl/cell-outputs))
  => (contains-in {"hello" {"echo" {"current" ["HELLO" integer?]
                                    "updated" integer?}}})

  (!.py
   (cl/cell-inputs))
  => (contains-in {"hello" {"echo" {"current" {"data" ["HELLO"]}
                                    "updated" integer?}}})

  (!.py
   (cl/model-vals "hello"))
  => (contains-in {"echo" ["HELLO" integer?]})

  (!.py
   (cl/model-outputs "hello"))
  => (contains-in {"echo" {"current" ["HELLO" integer?]
                           "updated" integer?}})

  (!.py
   (cl/model-is-errored "hello"))
  => false

  (!.py
   (cl/model-is-pending "hello"))
  => false

  (!.py
   (cl/model-trigger "hello" "hello" {}))
  => ["echo"]

  (!.py
   (cl/cell-trigger "hello" {}))
  => {"hello" ["echo"]}

  (notify/wait-on :python
    (. (cl/model-update "hello")
       (then (repl/>notify))))
  => (contains-in
      {"echo"
       {"path" ["hello" "echo"]
        "post" [false]
        "main" [true ["HELLO" integer?]]
        "pre" [false]
        "::" "view.run"}}))

^{:refer xt.cell.kernel/view-val :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (fn []
                                (. (cl/add-model "hello"
                                                 {:echo {:handler link-fn/echo
                                                         :defaultArgs ["HELLO"]
                                                         :trigger {"hello" true}}})
                                   ["init"]
                                   (then (repl/>notify)))))))]}
(fact "reads and updates views through the kernel"

  (!.js
   (cl/view-success ["hello" "echo"]))
  => (contains ["HELLO" integer?])

  (!.js
   (cl/view-val ["hello" "echo"]))
  => (contains ["HELLO" integer?])

  (!.js
   (cl/view-get-input ["hello" "echo"]))
  => map?

  (!.js
   (cl/view-get-output ["hello" "echo"]))
  => (contains-in {"current" ["HELLO" integer?]
                   "updated" integer?})

  (!.js
   (do (cl/view-set-val ["hello" "echo"] 1)
       (cl/view-val ["hello" "echo"])))
  => 1

  (!.js
   (cl/view-get-time-updated ["hello" "echo"]))
  => integer?

  (!.js
   (cl/view-is-errored ["hello" "echo"]))
  => false

  (!.js
   (cl/view-is-pending ["hello" "echo"]))
  => false

  (notify/wait-on :js
    (. (xt/x:first (cl/view-set-input ["hello" "echo"] {:data ["WORLD"]}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"})

  (notify/wait-on :js
    (. (xt/x:first (cl/view-trigger ["hello" "echo"] "hello" {}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"})

  (notify/wait-on :js
    (. (cl/view-for ["hello" "echo"])
       (then (repl/>notify))))
  => (contains ["WORLD" integer?])

  (!.js
   (cl/get-val ["hello" "echo"] [0]))
  => "WORLD"

  (notify/wait-on :js
    (. (cl/get-for ["hello" "echo"] [0])
       (then (repl/>notify))))
  => "WORLD"

  (!.lua
   (cl/view-success ["hello" "echo"]))
  => (contains ["HELLO" integer?])

  (!.lua
   (cl/view-val ["hello" "echo"]))
  => (contains ["HELLO" integer?])

  (!.lua
   (cl/view-get-input ["hello" "echo"]))
  => map?

  (!.lua
   (cl/view-get-output ["hello" "echo"]))
  => (contains-in {"current" ["HELLO" integer?]
                   "updated" integer?})

  (!.lua
   (do (cl/view-set-val ["hello" "echo"] 1)
       (cl/view-val ["hello" "echo"])))
  => 1

  (!.lua
   (cl/view-get-time-updated ["hello" "echo"]))
  => integer?

  (!.lua
   (cl/view-is-errored ["hello" "echo"]))
  => false

  (!.lua
   (cl/view-is-pending ["hello" "echo"]))
  => false

  (notify/wait-on :lua
    (. (xt/x:first (cl/view-set-input ["hello" "echo"] {:data ["WORLD"]}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"})

  (notify/wait-on :lua
    (. (xt/x:first (cl/view-trigger ["hello" "echo"] "hello" {}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"})

  (notify/wait-on :lua
    (. (cl/view-for ["hello" "echo"])
       (then (repl/>notify))))
  => (contains ["WORLD" integer?])

  (!.lua
   (cl/get-val ["hello" "echo"] [0]))
  => "WORLD"

  (notify/wait-on :lua
    (. (cl/get-for ["hello" "echo"] [0])
       (then (repl/>notify))))
  => "WORLD"

  (!.py
   (cl/view-success ["hello" "echo"]))
  => (contains ["HELLO" integer?])

  (!.py
   (cl/view-val ["hello" "echo"]))
  => (contains ["HELLO" integer?])

  (!.py
   (cl/view-get-input ["hello" "echo"]))
  => map?

  (!.py
   (cl/view-get-output ["hello" "echo"]))
  => (contains-in {"current" ["HELLO" integer?]
                   "updated" integer?})

  (!.py
   (do (cl/view-set-val ["hello" "echo"] 1)
       (cl/view-val ["hello" "echo"])))
  => 1

  (!.py
   (cl/view-get-time-updated ["hello" "echo"]))
  => integer?

  (!.py
   (cl/view-is-errored ["hello" "echo"]))
  => false

  (!.py
   (cl/view-is-pending ["hello" "echo"]))
  => false

  (notify/wait-on :python
    (. (xt/x:first (cl/view-set-input ["hello" "echo"] {:data ["WORLD"]}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"})

  (notify/wait-on :python
    (. (xt/x:first (cl/view-trigger ["hello" "echo"] "hello" {}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "echo"]
       "post" [false]
       "main" [true ["WORLD" integer?]]
       "pre" [false]
       "::" "view.run"})

  (notify/wait-on :python
    (. (cl/view-for ["hello" "echo"])
       (then (repl/>notify))))
  => (contains ["WORLD" integer?])

  (!.py
   (cl/get-val ["hello" "echo"] [0]))
  => "WORLD"

  (notify/wait-on :python
    (. (cl/get-for ["hello" "echo"] [0])
       (then (repl/>notify))))
  => "WORLD")

^{:refer xt.cell.kernel/add-listener :added "4.1"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (fn []
                                (. (cl/add-model "hello"
                                                 {:echo {:handler link-fn/echo
                                                         :defaultArgs ["HELLO"]}})
                                   ["init"]
                                   (then (repl/>notify)))))))]}
(fact "manages listeners and raw callbacks"

  (notify/wait-on :js
    (cl/add-listener ["hello" "echo"]
                     "@react/1234"
                     (fn [event]
                       (var #{type} event)
                       (when (== "view.output" type)
                         (repl/notify event)))
                     nil
                     nil)
    (cl/view-update ["hello" "echo"]))
  => (contains-in
      {"path" ["hello" "echo"]
       "type" "view.output"
       "meta" {"listener/id" "@react/1234"
               "listener/type" "cell"}
       "data" {"current" ["HELLO" integer?]
               "updated" integer?
               "pending" true}})

  (!.js
   (cl/list-listeners ["hello" "echo"]))
  => ["@react/1234"]

  (!.js
   (cl/list-all-listeners))
  => {"hello" {"echo" ["@react/1234"]}}

  (!.js
   (cl/remove-listener ["hello" "echo"] "@react/1234"))
  => (contains-in {"meta" {"listener/id" "@react/1234"
                           "listener/type" "cell"}})

  (notify/wait-on :js
    (cl/add-raw-callback "@/TEST" true (repl/>notify) nil)
    (link-fn/trigger (. (cl/GD) ["link"])
                     "stream"
                     "hello"
                     "ok"
                     {:data 123}))
  => {"body" {"data" 123}
      "status" "ok"
      "op" "stream"
      "topic" "hello"}

  (set
   (!.js
    (cl/list-raw-callbacks)))
  => #{"@/raw" "@/TEST"}

  (!.js
   (cl/remove-raw-callback "@/TEST"))
  => vector?

  (notify/wait-on :lua
    (cl/add-listener ["hello" "echo"]
                     "@react/1234"
                     (fn [event]
                       (var #{type} event)
                       (when (== "view.output" type)
                         (repl/notify event)))
                     nil
                     nil)
    (cl/view-update ["hello" "echo"]))
  => (contains-in
      {"path" ["hello" "echo"]
       "type" "view.output"
       "meta" {"listener/id" "@react/1234"
               "listener/type" "cell"}
       "data" {"current" ["HELLO" integer?]
               "updated" integer?
               "pending" true}})

  (!.lua
   (cl/list-listeners ["hello" "echo"]))
  => ["@react/1234"]

  (!.lua
   (cl/list-all-listeners))
  => {"hello" {"echo" ["@react/1234"]}}

  (!.lua
   (cl/remove-listener ["hello" "echo"] "@react/1234"))
  => (contains-in {"meta" {"listener/id" "@react/1234"
                           "listener/type" "cell"}})

  (notify/wait-on :lua
    (cl/add-raw-callback "@/TEST" true (repl/>notify) nil)
    (link-fn/trigger (. (cl/GD) ["link"])
                     "stream"
                     "hello"
                     "ok"
                     {:data 123}))
  => {"body" {"data" 123}
      "status" "ok"
      "op" "stream"
      "topic" "hello"}

  (set
   (!.lua
    (cl/list-raw-callbacks)))
  => #{"@/raw" "@/TEST"}

  (!.lua
   (cl/remove-raw-callback "@/TEST"))
  => vector?

  (notify/wait-on :python
    (cl/add-listener ["hello" "echo"]
                     "@react/1234"
                     (fn [event]
                       (var #{type} event)
                       (when (== "view.output" type)
                         (repl/notify event)))
                     nil
                     nil)
    (cl/view-update ["hello" "echo"]))
  => (contains-in
      {"path" ["hello" "echo"]
       "type" "view.output"
       "meta" {"listener/id" "@react/1234"
               "listener/type" "cell"}
       "data" {"current" ["HELLO" integer?]
               "updated" integer?
               "pending" true}})

  (!.py
   (cl/list-listeners ["hello" "echo"]))
  => ["@react/1234"]

  (!.py
   (cl/list-all-listeners))
  => {"hello" {"echo" ["@react/1234"]}}

  (!.py
   (cl/remove-listener ["hello" "echo"] "@react/1234"))
  => (contains-in {"meta" {"listener/id" "@react/1234"
                           "listener/type" "cell"}})

  (notify/wait-on :python
    (cl/add-raw-callback "@/TEST" true (repl/>notify) nil)
    (link-fn/trigger (. (cl/GD) ["link"])
                     "stream"
                     "hello"
                     "ok"
                     {:data 123}))
  => {"body" {"data" 123}
      "status" "ok"
      "op" "stream"
      "topic" "hello"}

  (set
   (!.py
    (cl/list-raw-callbacks)))
  => #{"@/raw" "@/TEST"}

  (!.py
   (cl/remove-raw-callback "@/TEST"))
  => vector?)