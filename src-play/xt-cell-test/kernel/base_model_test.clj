(ns xt.cell.kernel.base-model-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :xtalk
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-resource :as rt :with [defsingleton.xt]]
             [xt.event.base-model :as base-model]
             [xt.cell.kernel.base-link :as base-link]
             [xt.cell.kernel.base-link-local :as base-link-local]
             [xt.cell.kernel.base-model :as base-model]
             [xt.cell.kernel.base-impl :as base-impl]
             [xt.cell.kernel.inner-impl :as inner-impl]
             [xt.cell.kernel.inner-mock :as inner-mock]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
              [xt.lang.common-lib :as k]
               [xt.lang.common-data :as xtd]
               [xt.lang.common-resource :as rt :with [defsingleton.js]]
               [xt.event.base-model :as base-model]
              [xt.cell.kernel.base-link :as base-link]
              [xt.cell.kernel.base-link-local :as base-link-local]
              [xt.cell.kernel.base-model :as base-model]
             [xt.cell.kernel.base-impl :as base-impl]
             [xt.cell.kernel.inner-impl :as inner-impl]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(do (l/rt:restart)
          (l/rt:scaffold-imports :js))]
 :teardown [(l/rt:stop)]})

(defsingleton.xt CELL
  []
  (return nil))

(defn.xt reset-cell
  []
  (var link (base-link/link-create
             {:create-fn
              (fn:> [listener]
                (inner-mock/create-worker listener {} true))}))
  (var cell (base-impl/new-cell link))
  (-/CELL-reset cell)
  (inner-impl/worker-init-signal (. link ["worker"]) {:done true})
  (return cell))

(defn.xt get-cell
  []
  (var cell (-/CELL))
  (when cell
    (return cell))
  (return (-/reset-cell)))

^{:refer xt.cell.kernel.base-model/wrap-cell-args :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "puts the cell as first argument"

  (notify/wait-on :js
    (. ((base-model/wrap-cell-args
         base-link-local/echo)
        {:cell (-/get-cell)
         :args ["hello"]})
       (then (repl/>notify))))
  => (contains ["hello" integer?]))

^{:refer xt.cell.kernel.base-model/throttle-entry-promise :added "4.1"}
(fact "normalises legacy and map throttle entries to their promise"

  (notify/wait-on :js
    (. (base-model/throttle-entry-promise
        [(. Promise (resolve "legacy")) 10])
       (then (repl/>notify))))
  => "legacy"

  (notify/wait-on :js
    (. (base-model/throttle-entry-promise
        {"promise" (. Promise (resolve "mapped"))
         "started" 20})
       (then (repl/>notify))))
  => "mapped")

^{:refer xt.cell.kernel.base-model/throttle-entry :added "4.1"}
(fact "normalises throttle entries to the legacy [promise started] shape"

  (notify/wait-on :js
    (. (xt/x:first
        (base-model/throttle-entry
         {"promise" (. Promise (resolve "mapped"))
          "started" 20}))
       (then (repl/>notify))))
  => "mapped"

  (!.js
   (xt/x:second
    (base-model/throttle-entry
     {"promise" (. Promise (resolve "mapped"))
      "started" 20})))
  => 20)

^{:refer xt.cell.kernel.base-model/async-fn :added "4.1"}
(fact "TODO")

^{:refer xt.cell.kernel.base-model/prep-view :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/ping
                                                         :defaultArgs []}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "prepares params of views"

  (!.js
   (base-model/prep-view (-/CELL) "hello" "ping" {}))
  => vector?)

^{:refer xt.cell.kernel.base-model/get-view-dependents :added "4.0"}
(fact "gets all dependents for a view"

  (!.js
   (base-model/get-view-dependents
    {:models {"test/common" {:deps {"test/common" {"b2" {"a1" true}}}}
              "test/util"   {:deps {"test/common" {"b2" {"c3" true}}}}}}
    "test/common" "b2"))
  => {"test/common" ["a1"],
      "test/util" ["c3"]})

^{:refer xt.cell.kernel.base-model/get-model-dependents :added "4.0"}
(fact "gets all dependents for a model"

  (!.js
   (base-model/get-model-dependents
    {:models {"test/common" {:deps {"test/common" {"b2" {"a1" true}}}}
              "test/util"   {:deps {"test/common" {"b2" {"c3" true}}}}}}
    "test/common"))
  => {"test/common" true,
      "test/util" true})

^{:refer xt.cell.kernel.base-model/run-tail-call :added "4.0"}
(fact "helper function for tail calls on run commands"

  (!.js
   (var called [])
   (var acc {"ok" true})
   (var context {:acc acc
                 :cell {:id "cell"}
                 :path ["hello" "ping"]})
   [(base-model/run-tail-call
     context
     (fn [cell model-id view-id refresh]
       (called.push [model-id view-id])))
    called])
  => [{"ok" true} [["hello" "ping"]]])

^{:refer xt.cell.kernel.base-model/run-remote :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:echo-async {:handler base-link-local/echo
                                                               :remoteHandler base-link-local/echo-async
                                                               :defaultArgs ["hello" 100]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "runs the remote function"

  (notify/wait-on :js
    (. (do:>
        (var [path context disabled]
             (base-model/prep-view (-/CELL) "hello" "echo_async" {}))
        (return (base-model/run-remote context true path nil)))
       (then (repl/>notify))))
  => (contains-in
      {"::" "view.run",
       "path" ["hello" "echo_async"],
       "pre" [false],
       "remote" [true ["hello" integer?]],
       "post" [false]}))

^{:refer xt.cell.kernel.base-model/remote-call :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:echo-async {:handler base-link-local/echo
                                                               :remoteHandler base-link-local/echo-async
                                                               :defaultArgs ["hello" 100]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "runs the remote call"

  (notify/wait-on :js
    (. (base-model/remote-call (-/CELL) "hello" "echo_async" ["hello" 100] true)
       (then (repl/>notify))))
  => (contains-in
      {"::" "view.run",
       "path" ["hello" "echo_async"],
       "pre" [false],
       "remote" [true ["hello" integer?]],
       "post" [false]}))

^{:refer xt.cell.kernel.base-model/run-refresh :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/ping
                                                         :defaultArgs []}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "helper function for refresh"

  (notify/wait-on :js
    (. (do:>
        (var [path context disabled]
             (base-model/prep-view (-/CELL) "hello" "ping" {:event {}}))
        (return (base-model/run-refresh context disabled path nil)))
       (then (repl/>notify))))
  => (contains-in
      {"::" "view.run",
       "path" ["hello" "ping"],
       "pre" [false],
       "main" [true ["pong" integer?]],
       "post" [false]}))

^{:refer xt.cell.kernel.base-model/refresh-view-dependents :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/ping
                                                         :defaultArgs []}
                                                  :ping1 {:handler base-link-local/ping
                                                          :defaultArgs []
                                                          :deps ["ping"]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "refreshes view dependents"

  (def +res+
    (!.js
     (var [model view] (base-impl/view-ensure (-/CELL) "hello" "ping1"))
     (. view ["output"] ["current"])))

  (!.js
   (base-model/refresh-view-dependents (-/CELL)
                                       "hello"
                                       "ping"))
  => {"hello" ["ping1"]}

  (def +res2+
    (!.js
     (var [model view] (base-impl/view-ensure (-/CELL) "hello" "ping1"))
     (. view ["output"] ["current"])))

  (def +res+
    )

  (def +res2+
    )

  (def +res+
    )

  (def +res2+
    ))

^{:refer xt.cell.kernel.base-model/refresh-view :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:echo-async {:handler base-link-local/echo-async
                                                               :defaultArgs ["hello" 100]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "refreshes a view"

  (notify/wait-on :js
    (. (base-model/refresh-view
        (-/CELL) "hello" "echo_async" {})
       (then (repl/>notify))))
  => (contains-in
      {"::" "view.run",
       "path" ["hello" "echo_async"],
       "post" [false],
       "main" [true ["hello" integer?]],
       "pre" [false]}))

^{:refer xt.cell.kernel.base-model/refresh-view-remote :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:echo-async {:handler base-link-local/echo
                                                               :remoteHandler base-link-local/echo-async
                                                               :defaultArgs ["hello" 100]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "refreshes view remotely"

  (notify/wait-on :js
    (. (base-model/refresh-view-remote
        (-/CELL) "hello" "echo_async" nil)
       (then (repl/>notify))))
  => (contains-in
      {"::" "view.run",
       "path" ["hello" "echo_async"],
       "pre" [false],
       "remote" [true ["hello" integer?]],
       "post" [false]}))

^{:refer xt.cell.kernel.base-model/refresh-view-dependents-unthrottled :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/ping
                                                         :defaultArgs []}
                                                  :ping1 {:handler base-link-local/ping
                                                          :defaultArgs []
                                                          :deps ["ping"]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "refreshes view dependents unthrottled"

  (def +res3+
    (!.js
     (var [model view] (base-impl/view-ensure (-/CELL) "hello" "ping1"))
     (. view ["output"] ["current"])))

  (def +res4+
    (!.js
     (var [model view] (base-impl/view-ensure (-/CELL) "hello" "ping1"))
     (. view ["output"] ["current"])))

  (def +res3+
    )

  (def +res4+
    )

  (def +res3+
    )

  (def +res4+
    ))

^{:refer xt.cell.kernel.base-model/refresh-model :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/ping
                                                         :defaultArgs []}
                                                  :ping1 {:handler base-link-local/ping
                                                          :defaultArgs []
                                                          :deps ["ping"]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "refreshes a model"

  (notify/wait-on :js
    (. (base-model/refresh-model
        (-/CELL)
        "hello"
        {}
        nil)
       (then (repl/>notify))))
  => (contains-in
      [{"path" ["hello" "ping"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"}
       {"path" ["hello" "ping1"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"}]))

^{:refer xt.cell.kernel.base-model/get-model-deps :added "4.0"}
(fact "gets model dependencies"

  (!.js
   (base-model/get-model-deps
    "hello"
    {:ping {}
     :ping1 {:deps ["ping"]}}))
  => {"hello" {"ping" {"ping1" true}}})

^{:refer xt.cell.kernel.base-model/get-unknown-deps :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "gets unknown dependencies"

  (!.js
   (base-model/get-unknown-deps
    "hello"
    {:ping {}
     :ping1 {:deps ["ping"]}}
    (base-model/get-model-deps
     "hello"
     {:ping {}
      :ping1 {:deps ["ping"]}})
    (-/CELL)))
  => []

  (!.js
   (base-model/get-unknown-deps
    "hello"
    {:ping {}
     :ping1 {:deps ["ping"]}}
    (base-model/get-model-deps
     "hello"
     {:ping {}
      :ping1 {:deps ["ping2"]}})
    (-/CELL)))
  => [["hello" "ping2"]])

^{:refer xt.cell.kernel.base-model/create-throttle :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "creates a throttle"

  (!.js
   (base-model/create-throttle
    (-/CELL)
    "hello"
    nil))
  => {"queued" {}, "active" {}})

^{:refer xt.cell.kernel.base-model/create-model :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "creates a view"

  (!.js
   (base-model/create-model
    (-/CELL)
    "hello"
    "ping"
    {:handler base-link-local/ping
     :defaultArgs []}))
  => (contains-in
      {"::" "event.view",
       "input" {"current" {"data" []}, "updated" integer?},
       "output" {"elapsed" nil, "current" nil, "updated" nil},
       "pipeline" {"remote" {}, "main" {}},
       "options" {},
       "listeners"
       {"@/cell"
        {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}))

^{:refer xt.cell.kernel.base-model/add-model-attach :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "attaches a model"

  (!.js
   (base-model/add-model-attach
    (-/CELL)
    "hello"
    {:echo-async {:handler base-link-local/echo-async
                  :defaultArgs ["hello" 100]}}))
  => (contains-in
      {"name" "hello",
       "views"
       {"echo_async"
        {"::" "event.view",
         "pipeline" {"remote" {}, "main" {}},
         "options" {},
         "input" {"current" {"data" ["hello" 100]},
                  "updated" integer?},
         "output" {"elapsed" nil, "current" nil, "updated" nil},
         "listeners"
         {"@/cell"
          {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}},
       "deps" {},
       "throttle" {"queued" {}, "active" {}}}))

^{:refer xt.cell.kernel.base-model/add-model :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "adds a model"

  (notify/wait-on :js
    (. (. (base-model/add-model
           (-/CELL)
           "hello"
           {:echo-async {:handler base-link-local/echo-async
                         :defaultArgs ["hello" 100]}})
          ["init"])
       (then (repl/>notify))))
  => (contains-in
      [{"::" "view.run",
        "path" ["hello" "echo_async"],
        "post" [false],
        "main" [true ["hello" integer?]],
        "pre" [false]}])

  (notify/wait-on :js
    (. (. (base-model/add-model
           (-/CELL)
           "hello2"
           {:error-async {:handler base-link-local/error-async
                          :defaultArgs [100]}})
          ["init"])
       (then (repl/>notify))))
  => (contains-in
      [{"::" "view.run",
        "path" ["hello2" "error_async"],
        "error" true,
        "pre" [false],
        "main" [true
                {"body" ["error" integer?],
                 "action" "@cell/error.async",
                 "id" string?,
                 "status" "error",
                 "input" [100],
                 "end_time" integer?,
                 "op" "call",
                 "start_time" integer?}
                true],
        "post" [false]}]))

^{:refer xt.cell.kernel.base-model/remove-model :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model
                            (-/CELL)
                            "hello"
                            {:echo-async {:handler base-link-local/echo-async
                                          :defaultArgs ["hello" 100]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "removes a model"

  (!.js
   (base-model/remove-model
    (-/CELL)
    "hello"))
  => (contains-in
      {"name" "hello",
       "views"
       {"echo_async"
        {"::" "event.view",
         "output"
         {"elapsed" integer?
          "current" ["hello" integer?]
          "updated" integer?},
         "pipeline" {"remote" {}, "main" {}},
         "input"
         {"current" {"data" ["hello" 100]}, "updated" integer?},
         "options" {},
         "listeners"
         {"@/cell"
          {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}},
       "deps" {},
       "init" {},
       "throttle" {"queued" {}, "active" {}}}))

^{:refer xt.cell.kernel.base-model/remove-view :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model
                            (-/CELL)
                            "hello"
                            {:echo-async {:handler base-link-local/echo-async
                                          :defaultArgs ["hello" 100]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "removes a view"

  (!.js
   (base-model/remove-view
    (-/CELL) "hello" "echo_async"))
  => (contains-in
      {"::" "event.view",
       "pipeline" {"remote" {}, "main" {}},
       "options" {},
       "input" {"current" {"data" ["hello" 100]}, "updated" integer?},
       "output" {"elapsed" integer?
                 "current" ["hello" integer?]
                 "updated" integer?},
       "listeners"
       {"@/cell"
        {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}))

^{:refer xt.cell.kernel.base-model/model-update :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/ping
                                                         :defaultArgs []}
                                                  :ping1 {:handler base-link-local/ping
                                                          :defaultArgs []
                                                          :deps ["ping"]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "updates a model"

  (notify/wait-on :js
    (. (base-model/model-update
        (-/CELL)
        "hello"
        {})
       (then (repl/>notify))))
  => (contains-in
      {"ping"
       {"path" ["hello" "ping"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"},
       "ping1"
       {"path" ["hello" "ping1"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"}}))

^{:refer xt.cell.kernel.base-model/view-update :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/ping
                                                         :defaultArgs []}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "updates a view"

  (notify/wait-on :js
    (. (xtd/first
        (base-model/view-update
         (-/CELL)
         "hello"
         "ping"))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "ping"],
       "post" [false],
       "main" [true ["pong" integer?]],
       "pre" [false],
       "::" "view.run"}))

^{:refer xt.cell.kernel.base-model/view-set-input :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/echo
                                                         :defaultArgs ["foo"]}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "sets view input"

  (notify/wait-on :js
    (. (xtd/first
        (base-model/view-set-input
         (-/CELL) "hello" "ping" {:data ["bar"]}))
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "ping"],
       "post" [false],
       "main" [true ["bar" integer?]],
       "pre" [false],
       "::" "view.run"}))

^{:refer xt.cell.kernel.base-model/trigger-model-raw :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/echo
                                                         :defaultArgs ["foo"]
                                                         :trigger {"hello" true}}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "triggers model raw"

  (!.js
   (base-model/trigger-model-raw (-/CELL)
                                 (. (-/CELL)
                                    ["models"]
                                    ["hello"])
                                 "hello"
                                 {}))
  => ["ping"])

^{:refer xt.cell.kernel.base-model/trigger-model :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/echo
                                                         :defaultArgs ["foo"]
                                                         :trigger {"hello" true}}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "triggers a model"

  (!.js
   (base-model/trigger-model (-/CELL)
                             "hello"
                             "hello"
                             {}))
  => ["ping"])

^{:refer xt.cell.kernel.base-model/trigger-view :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/echo
                                                         :defaultArgs ["foo"]
                                                         :trigger {"hello" true}}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "triggers a view"

  (notify/wait-on :js
    (. (base-model/trigger-view (-/CELL)
                                "hello"
                                "ping"
                                "hello"
                                {})
       [0]
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "ping"],
       "post" [false],
       "main" [true ["foo" integer?]],
       "pre" [false],
       "::" "view.run"}))

^{:refer xt.cell.kernel.base-model/trigger-all :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (notify/wait-on :js
                     (. (. (base-model/add-model (-/CELL)
                                                 "hello"
                                                 {:ping {:handler base-link-local/echo
                                                         :defaultArgs ["foo"]
                                                         :trigger {"hello" true}}})
                           ["init"])
                        (then (repl/>notify))))]}
(fact "triggers all"

  (!.js
   (base-model/trigger-all (-/CELL)
                           "hello"
                           {:a 1}))
  => {"hello" ["ping"]})

^{:refer xt.cell.kernel.base-model/add-raw-callback :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))]}
(fact "adds a raw callback"

  (!.js
   (base-model/add-raw-callback (-/CELL)))
  => vector?)

^{:refer xt.cell.kernel.base-model/remove-raw-callback :added "4.0"
  :setup [(fact:global :setup)
                   (notify/wait-on :js
                     (. (-/reset-cell) ["init"]
                        (then (repl/>notify))))
                   (!.js
                    (base-model/add-raw-callback (-/CELL)))]}
(fact "removes a raw callback"

  (!.js
   (base-model/remove-raw-callback (-/CELL)))
  => vector?)