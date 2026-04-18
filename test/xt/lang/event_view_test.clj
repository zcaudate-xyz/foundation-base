(ns xt.lang.event-view-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.event-view :as view]
             [js.core :as j]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
               [xt.lang.common-repl :as repl]
               [xt.lang.event-view :as view]
               [lua.nginx :as n]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [xt.lang.event-view :as view]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [xt.lang.event-view :as view]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-view/list-listeners :adopt true :added "4.0"}
(fact "lists all listeners"
  ^:hidden
  
  (set (!.js
        (var v (view/create-view
                (fn:> [x] (j/future-delayed [100]
                            (return {:value x})))
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
  ^:hidden
  
  (notify/wait-on :js
    (var v (view/create-view
            nil
            {:remote {:handler (fn:> [x] (j/future-delayed [100]
                                           (throw "ERRORED")))}}
            [3]
            ["BLAH"]
            xtd/first))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v))
    (var async-fn
         (fn [handler-fn context #{success error}]
           (return (. (new Promise
                           (fn [resolve reject]
                             (resolve (handler-fn context))))
                      (then success)
                      (catch error)))))
    (. (view/pipeline-run-remote context
                                 true
                                 async-fn
                                 (fn:>)
                                 k/identity)
       (then (fn:> (repl/notify context.acc)))))
  => {"error" true,
      "remote" [true "ERRORED" true],
      "post" [false],
      "pre" [false],
      "::" "view.run"}
  
  (notify/wait-on :js
    (var v (view/create-view
            nil
            {:remote {:handler (fn:> [x] (j/future-delayed [100]
                                           (return nil)))}}
            [3]
            ["BLAH"]
            xtd/first))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v))
    (var async-fn
         (fn [handler-fn context #{success error}]
           (return (. (new Promise
                           (fn [resolve reject]
                             (resolve (handler-fn context))))
                      (then success)
                      (catch error)))))
    (. (view/pipeline-run-remote context
                                 true
                                 async-fn
                                 (fn:>)
                                 k/identity)
       (then (fn:> (repl/notify (view/get-output v)))))))

^{:refer xt.lang.event-view/wrap-args :added "4.0"}
(fact "wraps handler for context args"
  ^:hidden
  
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
  ^:hidden
  
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
  ^:hidden
  
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
  ^:hidden
  
  (!.js
   (xtd/tree-get-data
    (view/create-view
     (fn:> [x] (j/future-delayed [100]
                 (return {:value x})))
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
      "listeners" {}})

^{:refer xt.lang.event-view/view-context :added "4.0"}
(fact "gets the view-context"
  ^:hidden
  
  (!.js
   (var v (view/create-view
           (fn:> [x] (j/future-delayed [100]
                       (return {:value x})))
           {}
           [3]
           {:value 0}))
   (view/init-view v)
   (xtd/obj-keys (view/view-context v)))
  => ["view" "input"]

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
  ^:hidden
  
  (notify/wait-on :js
    (var v (view/create-view
            (fn:> [x] (j/future-delayed [100]
                        (return {:value x})))
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
  ^:hidden
  
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
  ^:hidden
  
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
  ^:hidden
  
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
  ^:hidden
  
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
  ^:hidden
  
  (notify/wait-on :js
    (var v (view/create-view
            (fn:> [x] (j/future-delayed [100]
                        (return {:value x})))
            {}
            [3]
            {}))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v))
    (var async-fn
         (fn [handler-fn context #{success error}]
           (return (. (new Promise
                           (fn [resolve reject]
                             (resolve (handler-fn context))))
                      (then success)
                      (catch error)))))
    (. (view/pipeline-run context
                          disabled
                          async-fn
                          (fn:>)
                          k/identity)
       (then (fn:> (repl/notify context.acc)))))
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
   (var [context disabled] (view/pipeline-prep v))
   (var async-fn
        (fn [handler-fn context cb]
          (return (cb.success (handler-fn context)))))
   (view/pipeline-run context
                      disabled
                      async-fn
                      (fn:>)
                      (fn:>))
   context.acc)
  => {"::" "view.run"
      "pre" [false],
      "main" [true {"value" 3}]
      "post" [false]})

^{:refer xt.lang.event-view/pipeline-run-force :added "4.0"}
(fact "runs the pipeline via sync or remote paths")

^{:refer xt.lang.event-view/pipeline-run-remote :added "4.0"}
(fact "runs the remote pipeline"
  ^:hidden
  
  (notify/wait-on :js
    (var v (view/create-view
            nil
            {:remote {:handler (fn:> [x] (j/future-delayed [100]
                                           (return {:value x})))}}
            [3]))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v))
    (var async-fn
         (fn [handler-fn context #{success error}]
           (return (. (new Promise
                           (fn [resolve reject]
                             (resolve (handler-fn context))))
                      (then success)
                      (catch error)))))
    (. (view/pipeline-run-remote context
                                 true
                                 async-fn
                                 (fn:>)
                                 k/identity)
       (then (fn:> (repl/notify context.acc)))))
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
   (var [context disabled] (view/pipeline-prep v))
   (var async-fn
        (fn [handler-fn context cb]
          (return (cb.success (handler-fn context)))))
   (view/pipeline-run-remote context
                             true
                             async-fn
                             (fn:>)
                             (fn:>))
   context.acc)
  => {"::" "view.run"
      "pre" [false]
      "remote" [true {"value" 3}],
      "post" [false],})

^{:refer xt.lang.event-view/pipeline-run-sync :added "4.0"}
(fact "runs the sync pipeline"
  ^:hidden
  
  (notify/wait-on :js
    (var v (view/create-view
            nil
            {:sync {:handler (fn:> [x] (j/future-delayed [100]
                                           (return {:value x})))}}
            [3]))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v))
    (var async-fn
         (fn [handler-fn context #{success error}]
           (return (. (new Promise
                           (fn [resolve reject]
                             (resolve (handler-fn context))))
                      (then success)
                      (catch error)))))
    (. (view/pipeline-run-sync context
                               true
                               async-fn
                               (fn:>)
                               k/identity)
       (then (fn:> (repl/notify context.acc)))))
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
   (var [context disabled] (view/pipeline-prep v))
   (var async-fn
        (fn [handler-fn context cb]
          (return (cb.success (handler-fn context)))))
   (view/pipeline-run-sync context
                             true
                             async-fn
                             (fn:>)
                             (fn:>))
   context.acc)
  => {"::" "view.run"
      "pre" [false]
      "sync" [true {"value" 3}],
      "post" [false]})

^{:refer xt.lang.event-view/get-with-lookup :added "0.1"}
(fact "creates a results vector and a lookup table"
  ^:hidden

  (!.js
   (view/get-with-lookup
    [{:id "A"}
     {:id "B"}
     {:id "C"}]))
  => {"results" [{"id" "A"} {"id" "B"} {"id" "C"}],
      "lookup" {"C" {"id" "C"}, "B" {"id" "B"}, "A" {"id" "A"}}}

  (!.lua
   (view/get-with-lookup
    [{:id "A"}
     {:id "B"}
     {:id "C"}]))
  => {"results" [{"id" "A"} {"id" "B"} {"id" "C"}],
      "lookup" {"C" {"id" "C"}, "B" {"id" "B"}, "A" {"id" "A"}}})

^{:refer xt.lang.event-view/sorted-lookup :added "0.1"}
(fact "sorted lookup for region data"
  ^:hidden
  
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
  ^:hidden
  
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
