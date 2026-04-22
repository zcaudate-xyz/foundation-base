(ns xt.lang.event-view-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.event-view :as view]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-spec :as xt]
               [xt.lang.common-data :as xtd]
                [xt.lang.common-repl :as repl]
                [xt.lang.event-view :as view]
                [lua.nginx :as n]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
              [xt.lang.common-data :as xtd]
               [xt.lang.common-repl :as repl]
               [xt.lang.event-view :as view]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [xt.lang.event-view :as view]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-view/list-listeners :adopt true :added "4.0"}
(fact "lists all listeners"

  ^{:lang-exceptions
    {:dart
     {:form (!.dt
              (var v (view/create-view
                      (fn:> [x] {:value x})
                      {}
                      [3]
                      {:value 0}
                      nil
                      nil))
              (view/add-listener v "a1" (fn:>) nil nil)
              (view/add-listener v "b2" (fn:>) nil nil)
              (view/add-listener v "c3" (fn:>) nil nil)
              (view/list-listeners v))
      :expect ["a1" "b2" "c3"]}}}
  (set (!.js
        (var v (view/create-view
                (fn:> [x] {:value x})
                {}
                [3]
                {:value 0}))
        (view/add-listener v "a1" (fn:>))
        (view/add-listener v "b2" (fn:>))
        (view/add-listener v "c3" (fn:>))
        (view/list-listeners v)))
  => #{"c3" "a1" "b2"}

  (set (!.lua
        (var v (view/create-view
                (fn:> [x] {:value x})
                {}
                [3]
                {:value 0}))
        (view/add-listener v "a1" (fn:>))
        (view/add-listener v "b2" (fn:>))
        (view/add-listener v "c3" (fn:>))
        (view/list-listeners v)))
  => #{"c3" "a1" "b2"})

^{:refer xt.lang.event-view/pipeline-run-remote.errored :adopt true :added "4.0"}
(fact "runs the pipeline"

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on :dart
              (var v (view/create-view
                      nil
                      {:remote {:handler (fn:> [x] (throw "ERRORED"))}}
                      [3]
                      ["BLAH"]
                      xtd/first
                      nil))
                (view/init-view v)
                (var [context disabled] (view/pipeline-prep v nil))
                (var async-fn
                     (fn [handler-fn context cb]
                       (var out (handler-fn context))
                       (if (== out "ERRORED")
                         (return ((xt/x:get-key cb "error") out))
                         (return ((xt/x:get-key cb "success") out)))))
                (view/pipeline-run-remote context
                                          true
                                          async-fn
                                          nil
                                          nil)
                (xt/x:get-key context "acc"))}}}
  (!.js
    (var v (view/create-view
            nil
            {:remote {:handler (fn:> [x] "ERRORED")}}
            [3]
            ["BLAH"]
            xtd/first))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (var async-fn
         (fn [handler-fn context cb]
           (var out (handler-fn context))
           (if (== out "ERRORED")
             (return ((xt/x:get-key cb "error") out))
             (return ((xt/x:get-key cb "success") out)))))
    (view/pipeline-run-remote context
                              true
                              async-fn
                              nil
                              nil)
    (xt/x:get-key context "acc"))
  => {"error" true,
      "remote" [true "ERRORED" true],
      "post" [false],
      "pre" [false],
      "::" "view.run"}

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on :dart
              (var v (view/create-view
                      nil
                      {:remote {:handler (fn:> [x] nil)}}
                      [3]
                      ["BLAH"]
                      xtd/first
                      nil))
               (view/init-view v)
               (var [context disabled] (view/pipeline-prep v nil))
               (var async-fn
                    (fn [handler-fn context cb]
                      (try
                        (return ((xt/x:get-key cb "success")
                                 (handler-fn context)))
                        (catch err
                          (return ((xt/x:get-key cb "error") err))))))
                 (view/pipeline-run-remote context
                                           true
                                           async-fn
                                           nil
                                           nil)
                 (view/get-output v))}}}
  (!.js
    (var v (view/create-view
            nil
            {:remote {:handler (fn:> [x] nil)}}
            [3]
            ["BLAH"]
            xtd/first))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (var async-fn
         (fn [handler-fn context cb]
           (try
             (return ((xt/x:get-key cb "success")
                      (handler-fn context)))
             (catch err
               (return ((xt/x:get-key cb "error") err))))))
    (view/pipeline-run-remote context
                              true
                              async-fn
                              nil
                              nil)
    (view/get-output v)))

^{:refer xt.lang.event-view/wrap-args :added "4.0"}
(fact "wraps handler for context args"

  (!.js
   ((view/wrap-args k/identity)
    {:args [1]}))
  => 1

  (!.lua
   ((view/wrap-args k/identity)
    {:args [1]}))
  => 1)

^{:refer xt.lang.event-view/check-disabled :added "4.0"}
(fact "checks that view is disabled"

  (!.js
   [(view/check-disabled
     {})
    (view/check-disabled
     {:input {:data [3]}})
    (view/check-disabled
     {:input {:data [3]
              :disabled true}})])
  => [true false true]

  (!.lua
   [(view/check-disabled
     {})
    (view/check-disabled
     {:input {:data [3]}})
    (view/check-disabled
     {:input {:data [3]
              :disabled true}})])
  => [true false true])

^{:refer xt.lang.event-view/parse-args :added "4.0"}
(fact "parses args from context"

  (!.js
   (view/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3]

  (!.lua
   (view/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3])

^{:refer xt.lang.event-view/create-view :added "4.0"
  :setup [(def +out+
            (contains-in
             {"output" {"default" "<function>"},
              "pipeline"
              {"remote" {"wrapper" "<function>"},
               "check_disabled" "<function>",
               "check_args" "<function>",
               "main" {"handler" "<function>", "wrapper" "<function>"}},
              "input" {"default" "<function>"},
              "::" "event.view",
              "options" {},
              "listeners" {}}))]}
(fact "creates a view"

  ^{:lang-exceptions
    {:dart
      {:form (!.dt
              (xtd/tree-get-data
               (view/create-view
                (fn:> [x] {:value x})
                {}
                [3]
                {:value 0}
                nil
                nil)))
       :expect {"output"
                {"elapsed" nil,
                 "process" "<function>",
                 "current" nil,
                 "type" "output",
                 "updated" nil,
                 "default" "<function>"},
                "::" "event.view",
                "pipeline"
                {"remote" {"wrapper" "<function>"},
                 "check_disabled" "<function>",
                 "check_args" "<function>",
                 "main" {"handler" "<function>", "wrapper" "<function>"},
                 "sync" {"wrapper" "<function>"}},
                "input" {"current" nil, "updated" nil, "default" "<function>"},
                "options" {},
                "listeners" {}}}}}
  (!.js
   (xtd/tree-get-data
      (view/create-view
       (fn:> [x] {:value x})
       {}
       [3]
       {:value 0})))
  => {"output"
      {"elapsed" nil,
       "process" "<function>",
       "current" nil,
       "type" "output",
       "updated" nil,
       "default" "<function>"},
      "::" "event.view",
      "pipeline"
      {"remote" {"wrapper" "<function>"},
       "check_disabled" "<function>",
       "check_args" "<function>",
       "main" {"handler" "<function>", "wrapper" "<function>"},
       "sync" {"wrapper" "<function>"}},
      "input" {"current" nil, "updated" nil, "default" "<function>"},
      "options" {},
      "listeners" {}}


  (!.lua
   (xtd/tree-get-data
    (view/create-view
     (fn:> [x] {:value x})
     {}
     [3]
     {:value 0})))
  => {"output"
      {"process" "<function>", "type" "output", "default" "<function>"},
      "::" "event.view",
      "pipeline"
      {"remote" {"wrapper" "<function>"},
       "check_disabled" "<function>",
       "check_args" "<function>",
       "main" {"handler" "<function>", "wrapper" "<function>"},
       "sync" {"wrapper" "<function>"}},
      "input" {"default" "<function>"},
      "options" {},
      "listeners" {}}

  (!.py
   (xtd/tree-get-data
    (view/create-view
     (fn:> [x] {:value x})
     {}
     [3]
     {:value 0})))
  => {"output"
      {"elapsed" nil,
       "process" "<function>",
       "current" nil,
       "type" "output",
       "updated" nil,
       "default" "<function>"},
      "::" "event.view",
      "pipeline"
      {"remote" {"wrapper" "<function>"},
       "check_disabled" "<function>",
       "check_args" "<function>",
       "main" {"handler" "<function>", "wrapper" "<function>"},
       "sync" {"wrapper" "<function>"}},
      "input" {"current" nil, "updated" nil, "default" "<function>"},
      "options" {},
      "listeners" {}})

^{:refer xt.lang.event-view/view-context :added "4.0"}
(fact "gets the view-context"

    ^{:lang-exceptions
      {:dart
       {:form (!.dt
               (var v (view/create-view
                       (fn:> [x] {:value x})
                       {}
                       [3]
                       {:value 0}
                       nil
                       nil))
               (view/init-view v)
               [(xt/x:has-key? (view/view-context v) "view")
                (xt/x:has-key? (view/view-context v) "input")])
        :expect [true true]}}}
  (!.js
   (var v (view/create-view
           (fn:> [x] {:value x})
           {}
           [3]
           {:value 0}))
   (view/init-view v)
   [(xt/x:has-key? (view/view-context v) "view")
    (xt/x:has-key? (view/view-context v) "input")])
  => [true true]

  (set (!.lua
        (var v (view/create-view
                (fn:> [x] {:value x})
                {}
                [3]
                {:value 0}))
        (view/init-view v)
        (xtd/obj-keys (view/view-context v))))
  => #{"input" "view"})

^{:refer xt.lang.event-view/add-listener :added "4.0"}
(fact "adds a listener to the view"

   ^{:lang-exceptions
     {:dart
       {:form (!.dt
               (var out nil)
               (var v (view/create-view
                       (fn:> [x] {:value x})
                       {}
                       [3]
                       {:value 0}
                       nil
                       nil))
               (view/add-listener v "a1"
                                  (fn [res]
                                    (:= out (xtd/tree-get-data res))
                                    (return nil))
                                  nil
                                  nil)
               (view/trigger-listeners v "output" {:value 0})
               out)}}}
  (notify/wait-on :js
    (var v (view/create-view
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}))
    (view/add-listener v "a1" (repl/>notify))
    (view/trigger-listeners v "output" {:value 0}))
  => {"type" "output", "meta" {"listener/id" "a1", "listener/type" "view"}, "data" {"value" 0}}

  (notify/wait-on :lua
    (var v (view/create-view
    (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
   (view/add-listener v "a1" (repl/>notify))
   (view/trigger-listeners v "output" {:value 0}))
  => {"type" "output", "meta" {"listener/id" "a1", "listener/type" "view"}, "data" {"value" 0}})

^{:refer xt.lang.event-view/trigger-listeners :added "4.0"}
(fact "triggers listeners to activate")

^{:refer xt.lang.event-view/get-input :added "4.0"}
(fact "gets the view input record"

    ^{:lang-exceptions
      {:dart
       {:form (!.dt
               (do
                 (var v (view/create-view
                         (fn:> [x] {:value x})
                         {}
                         [3]
                         {:value 0}
                         nil
                         nil))
                 (view/init-view v)
                 (xtd/tree-get-data (view/get-input v))))
        :expect (contains-in {"current" {"data" [3]}, "updated" integer?})}}}
  (!.js
   (var v (view/create-view
     (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
   (view/init-view v)
   (view/get-input v))
  => (contains-in {"current" {"data" [3]}, "updated" integer?})

  (!.lua
   (var v (view/create-view
    (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
   (view/init-view v)
   (. (view/get-input v)
      ["current"]))
  => {"data" [3]})

^{:refer xt.lang.event-view/get-output :added "4.0"}
(fact "gets the view output record"

    ^{:lang-exceptions
      {:dart
       {:form (!.dt
               (do
                 (var v (view/create-view
                         (fn:> [x] {:value x})
                         {}
                         [3]
                         {:value 0}
                         nil
                         nil))
                 (view/init-view v)
                 (xtd/tree-get-data (view/get-output v nil))))
        :expect {"type" "output" "elapsed" nil, "current" nil, "updated" nil}}}}
  (!.js
   (var v (view/create-view
     (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
   (view/init-view v)
   (view/get-output v))
  => {"type" "output" "elapsed" nil, "current" nil, "updated" nil}

  (!.lua
   (var v (view/create-view
    (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
   (view/init-view v)
   (. (view/get-output v)
      ["current"]))
  => nil)

^{:refer xt.lang.event-view/get-current :added "4.0"}
(fact "gets the current view output")

^{:refer xt.lang.event-view/is-disabled :added "4.0"}
(fact "checks that the view is disabled")

^{:refer xt.lang.event-view/is-errored :added "4.0"}
(fact "checks that output is errored")

^{:refer xt.lang.event-view/is-pending :added "4.0"}
(fact "checks that output is pending")

^{:refer xt.lang.event-view/get-time-elapsed :added "4.0"}
(fact "gets time elapsed of output")

^{:refer xt.lang.event-view/get-time-updated :added "4.0"}
(fact "gets time updated of output")

^{:refer xt.lang.event-view/get-success :added "4.0"}
(fact "gets either the current or default value if errored")

^{:refer xt.lang.event-view/set-input :added "4.0"}
(fact "sets the input"

   ^{:lang-exceptions
     {:dart
      {:form (notify/wait-on-call
              2000
              (fn []
                (!.dt
                 (var out nil)
                 (var v (view/create-view
                         (fn:> [x] {:value x})
                         {}
                        [3]
                        {:value 0}
                        nil
                        nil))
                 (view/add-listener
                  v "a1"
                  (fn [res]
                    (:= out (xtd/tree-get-data res))
                    (return nil))
                   nil
                   nil)
                  (view/set-input v 1)
                  out)))}}}
  (notify/wait-on :js
    (var v (view/create-view
    (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
    (view/add-listener v "a1" (repl/>notify))
    (view/set-input v 1))
  => (contains-in
      {"type" "view.input",
       "meta" {"listener/id" "a1", "listener/type" "view"},
       "data" map?})

  (notify/wait-on :lua
    (var v (view/create-view
    (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
    (view/add-listener v "a1" (fn [res]
                                (repl/notify (xtd/tree-get-data res))))
    (view/set-input v 1))
  => (contains-in
      {"type" "view.input",
       "meta" {"listener/id" "a1", "listener/type" "view"},
       "data" map?}))

^{:refer xt.lang.event-view/set-output :added "4.0"}
(fact "sets the output"

   ^{:lang-exceptions
     {:dart
      {:form (notify/wait-on-call
              2000
              (fn []
                (!.dt
                 (var out nil)
                 (var v (view/create-view
                         (fn:> [x] {:value x})
                         {}
                         [3]
                         {:value 0}
                         nil
                         nil))
                 (view/add-listener
                  v "a1"
                  (fn [res]
                    (:= out (xtd/tree-get-data res))
                    (return nil))
                  nil
                  nil)
                 (view/set-output v 1 nil nil nil nil)
                 out)))}}}
  (notify/wait-on :js
    (var v (view/create-view
    (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
    (view/add-listener v "a1" (repl/>notify))
    (view/set-output v 1 nil))
  => (contains-in
      {"type" "view.output",
       "meta" {"listener/id" "a1", "listener/type" "view"},
       "data" map?})

  (notify/wait-on :lua
    (var v (view/create-view
    (fn:> [x] {:value x})
    {}
    [3]
    {:value 0}))
    (view/add-listener v "a1" (fn [res]
                                (repl/notify (xtd/tree-get-data res))))
    (view/set-output v 1))
  => (contains-in
      {"type" "view.output",
       "meta" {"listener/id" "a1", "listener/type" "view"},
       "data" map?}))

^{:refer xt.lang.event-view/set-output-disabled :added "4.0"}
(fact "sets the output disabled flag")

^{:refer xt.lang.event-view/set-pending :added "4.0"}
(fact "sets the output pending time")

^{:refer xt.lang.event-view/set-elapsed :added "4.0"}
(fact "sets the output elapsed time")

^{:refer xt.lang.event-view/init-view :added "4.0"}
(fact "initialises view")

^{:refer xt.lang.event-view/pipeline-prep :added "4.0"}
(fact "prepares the pipeline")

^{:refer xt.lang.event-view/pipeline-set :added "4.0"}
(fact "sets the pipeline")

^{:refer xt.lang.event-view/pipeline-call :added "4.0"}
(fact "calls the pipeline with async function")

^{:refer xt.lang.event-view/pipeline-run-impl :added "4.0"}
(fact "runs the pipeline")

^{:refer xt.lang.event-view/pipeline-run :added "4.0"}
(fact "runs the pipeline"

  ^{:lang-exceptions
    {:dart
      {:form (!.dt
              (var v (view/create-view
                      (fn:> [x] (return {:value x}))
                      {}
                      [3]
                      {}
                      nil
                      nil))
               (view/init-view v)
               (var [context disabled] (view/pipeline-prep v nil))
               (var async-fn
                    (fn [handler-fn context cb]
                      (try
                        (return ((xt/x:get-key cb "success")
                                 (handler-fn context)))
                        (catch err
                          (return ((xt/x:get-key cb "error") err))))))
                 (view/pipeline-run context
                                    disabled
                                    async-fn
                                    nil
                                    nil
                                    nil)
                 (xt/x:get-key context "acc"))}}}
  (!.js
    (var v (view/create-view
            (fn:> [x] {:value x})
            {}
            [3]
            {}))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (var async-fn
         (fn [handler-fn context cb]
           (return ((xt/x:get-key cb "success") (handler-fn context)))))
    (view/pipeline-run context
                       disabled
                       async-fn
                       nil
                       nil)
    (xt/x:get-key context "acc"))
  => {"::" "view.run"
      "pre" [false],
      "main" [true {"value" 3}]
      "post" [false]}

  (!.lua
   (var v (view/create-view
           (fn:> [x] {:value x})
           {}
           [3]
           {}))
   (view/init-view v)
   (var [context disabled] (view/pipeline-prep v nil))
   (var async-fn
        (fn [handler-fn context cb]
          (return ((. cb ["success"]) (handler-fn context)))))
   (view/pipeline-run context
                      disabled
                      async-fn
                      nil
                      nil)
   (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false],
      "main" [true {"value" 3}]
      "post" [false]})

^{:refer xt.lang.event-view/pipeline-run-force :added "4.0"}
(fact "runs the pipeline via sync or remote paths")

^{:refer xt.lang.event-view/pipeline-run-remote :added "4.0"}
(fact "runs the remote pipeline"

  ^{:lang-exceptions
    {:dart
      {:form (!.dt
              (var v (view/create-view
                      nil
                      {:remote {:handler (fn:> [x] (return {:value x}))}}
                      [3]
                      nil
                      nil
                      nil))
               (view/init-view v)
               (var [context disabled] (view/pipeline-prep v nil))
               (var async-fn
                    (fn [handler-fn context cb]
                      (try
                        (return ((xt/x:get-key cb "success")
                                 (handler-fn context)))
                        (catch err
                          (return ((xt/x:get-key cb "error") err))))))
                 (view/pipeline-run-remote context
                                           true
                                           async-fn
                                           nil
                                           nil)
                 (xt/x:get-key context "acc"))}}}
  (!.js
    (var v (view/create-view
            nil
            {:remote {:handler (fn:> [x] {:value x})}}
            [3]))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (var async-fn
         (fn [handler-fn context cb]
           (return ((xt/x:get-key cb "success") (handler-fn context)))))
    (view/pipeline-run-remote context
                              true
                              async-fn
                              nil
                              nil)
    (xt/x:get-key context "acc"))
  => {"::" "view.run"
      "pre" [false]
      "remote" [true {"value" 3}],
      "post" [false],}


  (!.lua
   (var v (view/create-view
            nil
            {:remote {:handler (fn:> [x] {:value x})}}
            [3]))
   (view/init-view v)
   (var [context disabled] (view/pipeline-prep v nil))
   (var async-fn
        (fn [handler-fn context cb]
          (return ((. cb ["success"]) (handler-fn context)))))
   (view/pipeline-run-remote context
                             true
                             async-fn
                             nil
                             nil)
   (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "remote" [true {"value" 3}],
      "post" [false],})

^{:refer xt.lang.event-view/pipeline-run-sync :added "4.0"}
(fact "runs the sync pipeline"

  ^{:lang-exceptions
    {:dart
      {:form (!.dt
              (var v (view/create-view
                      nil
                      {:sync {:handler (fn:> [x] (return {:value x}))}}
                      [3]
                      nil
                      nil
                      nil))
               (view/init-view v)
               (var [context disabled] (view/pipeline-prep v nil))
               (var async-fn
                    (fn [handler-fn context cb]
                      (try
                        (return ((xt/x:get-key cb "success")
                                 (handler-fn context)))
                        (catch err
                          (return ((xt/x:get-key cb "error") err))))))
                 (view/pipeline-run-sync context
                                         true
                                         async-fn
                                         nil
                                         nil)
                 (xt/x:get-key context "acc"))}}}
  (!.js
    (var v (view/create-view
            nil
            {:sync {:handler (fn:> [x] {:value x})}}
            [3]))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (var async-fn
         (fn [handler-fn context cb]
           (return ((xt/x:get-key cb "success") (handler-fn context)))))
    (view/pipeline-run-sync context
                            true
                            async-fn
                            nil
                            nil)
    (xt/x:get-key context "acc"))
  => {"::" "view.run"
      "pre" [false]
      "sync" [true {"value" 3}],
      "post" [false],}


  (!.lua
   (var v (view/create-view
            nil
            {:sync {:handler (fn:> [x] {:value x})}}
            [3]))
   (view/init-view v)
   (var [context disabled] (view/pipeline-prep v nil))
   (var async-fn
        (fn [handler-fn context cb]
          (return ((. cb ["success"]) (handler-fn context)))))
   (view/pipeline-run-sync context
                              true
                              async-fn
                              nil
                              nil)
   (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "sync" [true {"value" 3}],
      "post" [false]})

^{:refer xt.lang.event-view/get-with-lookup :added "0.1"}
(fact "creates a results vector and a lookup table"

  (!.js
   (view/get-with-lookup
    [{:id "A"}
     {:id "B"}
     {:id "C"}]
    nil))
  => {"results" [{"id" "A"} {"id" "B"} {"id" "C"}],
      "lookup" {"C" {"id" "C"}, "B" {"id" "B"}, "A" {"id" "A"}}}

  (!.lua
   (view/get-with-lookup
    [{:id "A"}
     {:id "B"}
     {:id "C"}]
    nil))
  => {"results" [{"id" "A"} {"id" "B"} {"id" "C"}],
      "lookup" {"C" {"id" "C"}, "B" {"id" "B"}, "A" {"id" "A"}}})

^{:refer xt.lang.event-view/sorted-lookup :added "0.1"}
(fact "sorted lookup for region data"

  (!.js
   ((view/sorted-lookup "name")
    [{:id "A" :name "a"}
     {:id "B" :name "b"}
     {:id "C" :name "c"}
     {:id "D" :name "d"}]))
  => {"results"
      [{"id" "A", "name" "a"}
       {"id" "B", "name" "b"}
       {"id" "C", "name" "c"}
       {"id" "D", "name" "d"}],
      "lookup"
      {"C" {"id" "C", "name" "c"},
       "B" {"id" "B", "name" "b"},
       "A" {"id" "A", "name" "a"},
       "D" {"id" "D", "name" "d"}}})

^{:refer xt.lang.event-view/group-by-lookup :added "0.1"}
(fact "creates group-by lookup"

  (!.js
   ((view/group-by-lookup "name")
    [{:id "A" :name "a"}
     {:id "B" :name "a"}
     {:id "C" :name "b"}
     {:id "D" :name "b"}]))
  => {"results"
      [{"id" "A", "name" "a"}
       {"id" "B", "name" "a"}
       {"id" "C", "name" "b"}
       {"id" "D", "name" "b"}],
      "lookup"
      {"a" [{"id" "A", "name" "a"} {"id" "B", "name" "a"}],
       "b" [{"id" "C", "name" "b"} {"id" "D", "name" "b"}]}})
