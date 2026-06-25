(ns xt.event.base-model-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/scaffold {:all true}}
(do 
  (l/script- :xtalk
    {:require [[xt.lang.common-lib :as k]
               [xt.lang.common-repl :as repl]
               [xt.lang.common-tree :as tree]
               [xt.lang.spec-base :as xt]
               [xt.lang.spec-promise :as promise]
               [xt.event.base-model :as model]]})
  
  (defn.xt make-basic-model []
    (return
     (model/create-model
      (fn:> [x] {:value x})
      {}
      [3]
      {:value 0}
      nil
      nil)))
  
  (defn.xt make-processed-model []
    (return
     (model/create-model
      (fn:> [x] x)
      {}
      [3]
      1
      (fn [x] (return (+ x 10)))
      nil)))
  
  (defn.xt make-remote-model []
    (return
     (model/create-model
      nil
      {:remote {:handler (fn:> [x] {:value x})}}
      [3]
      nil
      nil
      nil)))
  
  (defn.xt make-sync-model []
    (return
     (model/create-model
      nil
      {:sync {:handler (fn:> [x] {:value x})}}
      [3]
      nil
      nil
      nil)))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as model]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as model]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as model]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-model/pipeline-run.success :adopt true :added "4.0"}
(fact "lists all listeners"
  
  (!.js
    (var v (model/create-model
            (fn [x]
              (return
               (promise/x:with-delay 100
                                     (fn []
                                       (return {:value x})))))
            {}
            [3]
            {:value 0}))
    (model/add-listener v "a1" (fn []))
    (model/add-listener v "b2" (fn []))
    (model/add-listener v "c3" (fn []))
    (model/list-listeners v))
  => (contains ["a1" "b2" "c3"] :in-any-order)


  (!.js
    (var v (model/create-model
            (fn [x]
              (return
               (promise/x:with-delay 100
                                     (fn []
                                       (return {:value x})))))
            {}
            [3]
            {:value 0}))
    (model/init-model v))
  => (contains-in {"current" {"data" [3]}, "updated" number?})

  (!.js
    (var v (model/create-model
            (fn [x]
              (return
               (promise/x:with-delay 100
                                     (fn []
                                       (return {:value x})))))
            {}
            [3]
            {:value 0}))
    (model/init-model v)
    (model/pipeline-prep v))
  => (contains-in
      [{"model"
        {"output"
         {"elapsed" nil, "current" nil, "type" "output", "updated" nil},
         "::" "event.model",
         "pipeline" {"remote" {}, "main" {}, "sync" {}},
         "input" {"current" {"data" [3]}, "updated" number?},
         "options" {},
         "listeners" {}},
        "args" [3],
        "input" {"data" [3]},
        "acc" {"::" "model.run"}}
       false])
  
  (notify/wait-on :js
    (var v (model/create-model
            (fn [x]
              (return
               (promise/x:with-delay 100
                                     (fn []
                                       (return {:value x})))))
            {}
            [3]
            {:value 0}))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v))
    (-> (model/pipeline-run context
                            nil
                            model/async-fn-promise
                            (fn [])
                            (fn [x] (return x)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify context.acc)))))
  => {"post" [false], "::" "model.run", "main" [true {"value" 3}], "pre" [false]}

  (notify/wait-on :js
    (var v (model/create-model
            nil
            {:remote {:handler
                      (fn [x]
                        (return
                         (promise/x:with-delay 100
                                               (fn []
                                                 (return {:value x})))))}}
            {}
            [3]
            {:value 0}))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v))
    (-> (model/pipeline-run-remote
         context
         nil
         model/async-fn-promise
         (fn [])
         (fn [x] (return x)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify context.acc)))))
  => {"error" true, "remote" [true {} true], "post" [false], "::" "model.run", "pre" [false]})


^{:refer xt.lang.event-model/pipeline-run-remote.errored :adopt true :added "4.0"}
(fact "runs the pipeline"
  
  (notify/wait-on :js
    (var v (model/create-model
            nil
            {:remote {:handler
                      (fn [x]
                        (return
                         (promise/x:with-delay
                          100
                          (fn []
                            (xt/x:throw "ERRORED")))))}}
            [3]
            ["BLAH"]
            xt/x:first))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v))
    (-> (model/pipeline-run-remote context
                                   nil
                                   model/async-fn-promise
                                   (fn [])
                                   (fn [x] (return x)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify context.acc)))))
  => {"error" true, "remote" [true "ERRORED" true], "post" [false], "::" "model.run", "pre" [false]})

^{:refer xt.event.base-model/async-fn-basic :added "4.1"}
(fact "executes a synchronous handler and dispatches callbacks"

  (!.js
    (var out nil)
    (model/async-fn-basic (fn:> [ctx] {:value 3})
                          {}
                          {"success" (fn [res] (:= out res))
                           "error"   (fn [err] (:= out err))})
    out)
  => {"value" 3}

  (!.lua
    (var out nil)
    (model/async-fn-basic (fn:> [ctx] {:value 3})
                          {}
                          {"success" (fn [res] (:= out res))
                           "error"   (fn [err] (:= out err))})
    out)
  => {"value" 3}

  (!.py
    (var out nil)
    (model/async-fn-basic (fn:> [ctx] {:value 3})
                          {}
                          {"success" (fn [res] (:= out res))
                           "error"   (fn [err] (:= out err))})
    out)
  => {"value" 3})

^{:refer xt.event.base-model/async-fn-promise :added "4.1"}
(fact "executes handlers through promise resolution"

  (notify/wait-on :js
    (var out nil)
    (-> (model/async-fn-promise
         (fn [ctx]
           (return (promise/x:with-delay 10 (fn:> [] {:value 3}))))
         {}
         {"success" (fn [res] (:= out res))
          "error"   (fn [err] (:= out err))})
        (promise/x:promise-then
         (fn [] (repl/notify out)))))
  => {"value" 3})
=> {:value 3}

  (notify/wait-on :lua
    (var out nil)
    (-> (model/async-fn-promise
         (fn [ctx]
           (return (promise/x:with-delay 10 (fn:> [] {:value 3}))))
         {}
         {"success" (fn [res] (:= out res))
          "error"   (fn [err] (:= out err))})
        (promise/x:promise-then
         (fn [] (repl/notify out)))))
  => {"value" 3})

^{:refer xt.event.base-model/wrap-args :added "4.1"}
(fact "provides the core model helpers"

  (!.js
    [((model/wrap-args k/identity)
      {:args [1]})
     (model/check-disabled {})
     (model/check-disabled {:input {:data [3]}})
     (model/check-disabled {:input {:data [3]
                                    :disabled true}})
     (model/parse-args {:input {:data [1 2 3]}})])
  => [1 true false true [1 2 3]]

  (!.lua
    [((model/wrap-args k/identity)
      {:args [1]})
     (model/check-disabled {})
     (model/check-disabled {:input {:data [3]}})
     (model/check-disabled {:input {:data [3]
                                    :disabled true}})
     (model/parse-args {:input {:data [1 2 3]}})])
  => [1 true false true [1 2 3]]

  (!.py
    [((model/wrap-args k/identity)
      {:args [1]})
     (model/check-disabled {})
     (model/check-disabled {:input {:data [3]}})
     (model/check-disabled {:input {:data [3]
                                    :disabled true}})
     (model/parse-args {:input {:data [1 2 3]}})])
  => [1 true false true [1 2 3]])

^{:refer xt.event.base-model/check-disabled :added "4.1"}
(fact "checks disabled state from model context"

  (!.js
    [(model/check-disabled {})
     (model/check-disabled {:input {:data [3]}})
     (model/check-disabled {:input {:data [3]
                                    :disabled true}})])
  => [true false true]

  (!.lua
    [(model/check-disabled {})
     (model/check-disabled {:input {:data [3]}})
     (model/check-disabled {:input {:data [3]
                                    :disabled true}})])
  => [true false true]

  (!.py
    [(model/check-disabled {})
     (model/check-disabled {:input {:data [3]}})
     (model/check-disabled {:input {:data [3]
                                    :disabled true}})])
  => [true false true])

^{:refer xt.event.base-model/parse-args :added "4.1"}
(fact "parses arguments from input data"

  (!.js
    (model/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3]

  (!.lua
    (model/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3]

  (!.py
    (model/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3])

^{:refer xt.event.base-model/create-model :added "4.1"}
(fact "manages model listeners"

  ^{:seedgen/base {:lua {:expect (just-in
                                  [{"type" "output"}
                                   (just ["a1" "b2"] :in-any-order)
                                   {"listener/id" "b2"
                                    "listener/type" "model"}
                                   ["a1"]])}}}
  (!.js
    (var v (model/create-model
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(model/get-output v)
     (model/list-listeners v)
     (. (model/remove-listener v "b2") ["meta"])
     (model/list-listeners v)])
  => (just-in
      [{"current" nil
        "elapsed" nil
        "type" "output"
        "updated" nil}
       (just ["a1" "b2"] :in-any-order)
       {"listener/id" "b2"
        "listener/type" "model"}
       ["a1"]])

  (!.lua
    (var v (model/create-model
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(model/get-output v)
     (model/list-listeners v)
     (. (model/remove-listener v "b2") ["meta"])
     (model/list-listeners v)])
  => (just-in [{"type" "output"} (just ["a1" "b2"] :in-any-order) {"listener/id" "b2", "listener/type" "model"} ["a1"]])

  (!.py
    (var v (model/create-model
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(model/get-output v nil)
     (model/list-listeners v)
     (. (model/remove-listener v "b2") ["meta"])
     (model/list-listeners v)])
  => (just-in
      [{"current" nil
        "elapsed" nil
        "type" "output"
        "updated" nil}
       (just ["a1" "b2"] :in-any-order)
       {"listener/id" "b2"
        "listener/type" "model"}
       ["a1"]]))

^{:refer xt.event.base-model/model-context :added "4.1"}
(fact "builds a model context with model and input"

  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    [(xt/x:has-key? (model/model-context v) "model")
     (xt/x:has-key? (model/model-context v) "input")])
  => [true true]

  (!.lua
    (var v (-/make-basic-model))
    (model/init-model v)
    [(xt/x:has-key? (model/model-context v) "model")
     (xt/x:has-key? (model/model-context v) "input")])
  => [true true]

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    [(xt/x:has-key? (model/model-context v) "model")
     (xt/x:has-key? (model/model-context v) "input")])
  => [true true])

^{:refer xt.event.base-model/add-listener :added "4.1"}
(fact "adds a model listener"

  ^{:seedgen/base {:lua {:expect {"id" "a1"
                                  "data" {"data" {"value" 0}
                                          "type" "output"}
                                  "meta" {"listener/id" "a1"
                                          "listener/type" "model"}}}}}
  (!.js
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/trigger-listeners v "output" {:value 0})
    out)
  => {"id" "a1"
      "data" {"data" {"value" 0}
              "type" "output"}
      "t" nil
      "meta" {"listener/id" "a1"
              "listener/type" "model"}}

  (!.lua
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/trigger-listeners v "output" {:value 0})
    out)
  => {"id" "a1", "meta" {"listener/id" "a1", "listener/type" "model"}, "data" {"type" "output", "data" {"value" 0}}}

  (!.py
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/trigger-listeners v "output" {:value 0})
    out)
  => {"id" "a1"
      "data" {"data" {"value" 0}
              "type" "output"}
      "t" nil
      "meta" {"listener/id" "a1"
              "listener/type" "model"}})

^{:refer xt.event.base-model/remove-listener :added "4.1"}
(fact "removes a model listener by id"

  (!.js
    (var v (-/make-basic-model))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(model/list-listeners v)
     (. (model/remove-listener v "b2") ["meta"])
     (model/list-listeners v)])
  => (just-in
      [(just ["a1" "b2"] :in-any-order)
       {"listener/id" "b2"
        "listener/type" "model"}
       ["a1"]])

  (!.lua
    (var v (-/make-basic-model))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(model/list-listeners v)
     (. (model/remove-listener v "b2") ["meta"])
     (model/list-listeners v)])
  => (just-in
      [(just ["a1" "b2"] :in-any-order)
       {"listener/id" "b2"
        "listener/type" "model"}
       ["a1"]])

  (!.py
    (var v (-/make-basic-model))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(model/list-listeners v)
     (. (model/remove-listener v "b2") ["meta"])
     (model/list-listeners v)])
  => (just-in
      [(just ["a1" "b2"] :in-any-order)
       {"listener/id" "b2"
        "listener/type" "model"}
       ["a1"]]))

^{:refer xt.event.base-model/list-listeners :added "4.1"}
(fact "lists all registered listener ids"

  (!.js
    (var v (-/make-basic-model))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    (model/list-listeners v))
  => (just ["a1" "b2"] :in-any-order)

  (!.lua
    (var v (-/make-basic-model))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    (model/list-listeners v))
  => (just ["a1" "b2"] :in-any-order)

  (!.py
    (var v (-/make-basic-model))
    (model/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (model/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    (model/list-listeners v))
  => (just ["a1" "b2"] :in-any-order))

^{:refer xt.event.base-model/trigger-listeners :added "4.1"
  :setup [(def +out+
            (just-in
             [(just ["a1" "b2"] :in-any-order)
              (just ["a1" "b2"] :in-any-order)]))]}
(fact "triggers all registered model listeners"

  (!.js
    (var v (-/make-basic-model))
    (var calls [])
    (model/add-listener v "a1" (fn [id data t meta] (xt/x:arr-push calls "a1")) nil nil)
    (model/add-listener v "b2" (fn [id data t meta] (xt/x:arr-push calls "b2")) nil nil)
    [(model/trigger-listeners v "output" {:value 0})
     calls])
  => +out+

  (!.lua
    (var v (-/make-basic-model))
    (var calls [])
    (model/add-listener v "a1" (fn [id data t meta] (xt/x:arr-push calls "a1")) nil nil)
    (model/add-listener v "b2" (fn [id data t meta] (xt/x:arr-push calls "b2")) nil nil)
    [(model/trigger-listeners v "output" {:value 0})
     calls])
  => +out+

  (!.py
    (var v (-/make-basic-model))
    (var calls [])
    (model/add-listener v "a1" (fn [id data t meta] (xt/x:arr-push calls "a1")) nil nil)
    (model/add-listener v "b2" (fn [id data t meta] (xt/x:arr-push calls "b2")) nil nil)
    [(model/trigger-listeners v "output" {:value 0})
     calls])
  => +out+)

^{:refer xt.event.base-model/get-input :added "4.1"}
(fact "gets the current input record"

  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    (model/get-input v))
  => (contains-in {"current" {"data" [3]}
                   "updated" integer?})

  (!.lua
    (var v (-/make-basic-model))
    (model/init-model v)
    (model/get-input v))
  => (contains-in {"current" {"data" [3]}
                   "updated" integer?})

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    (model/get-input v))
  => (contains-in {"current" {"data" [3]}
                   "updated" integer?}))

^{:refer xt.event.base-model/get-output :added "4.1"}
(fact "gets the output record"

  ^{:seedgen/base {:lua {:expect {"type" "output"}}}}
  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    (model/get-output v))
  => {"current" nil
      "elapsed" nil
      "type" "output"
      "updated" nil}

  (!.lua
    (var v (-/make-basic-model))
    (model/init-model v)
    (model/get-output v))
  => {"type" "output"}

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    (model/get-output v nil))
  => {"current" nil
      "elapsed" nil
      "type" "output"
      "updated" nil})

^{:refer xt.event.base-model/get-current :added "4.1"}
(fact "gets the current output value"

  (!.js
    (var v (-/make-basic-model))
    (model/set-output v 1 nil nil nil nil)
    (model/get-current v))
  => 1

  (!.lua
    (var v (-/make-basic-model))
    (model/set-output v 1 nil nil nil nil)
    (model/get-current v))
  => 1

  (!.py
    (var v (-/make-basic-model))
    (model/set-output v 1 nil nil nil nil)
    (model/get-current v nil))
  => 1)

^{:refer xt.event.base-model/is-disabled :added "4.1"}
(fact "checks whether the model is disabled"

  (!.js
    (var v (-/make-basic-model))
    (var before (model/is-disabled v))
    (model/init-model v)
    [before
     (model/is-disabled v)])
  => [true false]

  (!.lua
    (var v (-/make-basic-model))
    (var before (model/is-disabled v))
    (model/init-model v)
    [before
     (model/is-disabled v)])
  => [true false]

  (!.py
    (var v (-/make-basic-model))
    (var before (model/is-disabled v))
    (model/init-model v)
    [before
     (model/is-disabled v)])
  => [true false])

^{:refer xt.event.base-model/is-errored :added "4.1"}
(fact "checks whether the output is errored"

  (!.js
    (var v (-/make-basic-model))
    (model/set-output v 1 true nil nil nil)
    (model/is-errored v))
  => true

  (!.lua
    (var v (-/make-basic-model))
    (model/set-output v 1 true nil nil nil)
    (model/is-errored v))
  => true

  (!.py
    (var v (-/make-basic-model))
    (model/set-output v 1 true nil nil nil)
    (model/is-errored v nil))
  => true)

^{:refer xt.event.base-model/is-pending :added "4.1"}
(fact "checks whether the output is pending"

  (!.js
    (var v (-/make-basic-model))
    (model/set-pending v true nil)
    (model/is-pending v))
  => true

  (!.lua
    (var v (-/make-basic-model))
    (model/set-pending v true nil)
    (model/is-pending v))
  => true

  (!.py
    (var v (-/make-basic-model))
    (model/set-pending v true nil)
    (model/is-pending v nil))
  => true)

^{:refer xt.event.base-model/get-time-elapsed :added "4.1"}
(fact "gets elapsed time for the output"

  (!.js
    (var v (-/make-basic-model))
    (model/set-elapsed v 20 nil)
    (model/get-time-elapsed v))
  => 20

  (!.lua
    (var v (-/make-basic-model))
    (model/set-elapsed v 20 nil)
    (model/get-time-elapsed v))
  => 20

  (!.py
    (var v (-/make-basic-model))
    (model/set-elapsed v 20 nil)
    (model/get-time-elapsed v nil))
  => 20)

^{:refer xt.event.base-model/get-time-updated :added "4.1"}
(fact "gets the last output update time"

  (!.js
    (var v (-/make-basic-model))
    (model/set-output v 1 nil nil nil nil)
    (model/get-time-updated v))
  => integer?

  (!.lua
    (var v (-/make-basic-model))
    (model/set-output v 1 nil nil nil nil)
    (model/get-time-updated v))
  => integer?

  (!.py
    (var v (-/make-basic-model))
    (model/set-output v 1 nil nil nil nil)
    (model/get-time-updated v nil))
  => integer?)

^{:refer xt.event.base-model/get-success :added "4.1"}
(fact "returns current output or processed default value"

  (!.js
    (var v (-/make-processed-model))
    (var initial (model/get-success v nil))
    (model/set-output v 3 nil nil nil nil)
    (var current (model/get-success v nil))
    (model/set-output v 20 true nil nil nil)
    [initial
     current
     (model/get-success v nil)])
  => [11 3 11]

  (!.lua
    (var v (-/make-processed-model))
    (var initial (model/get-success v nil))
    (model/set-output v 3 nil nil nil nil)
    (var current (model/get-success v nil))
    (model/set-output v 20 true nil nil nil)
    [initial
     current
     (model/get-success v nil)])
  => [11 3 11]

  (!.py
    (var v (-/make-processed-model))
    (var initial (model/get-success v nil))
    (model/set-output v 3 nil nil nil nil)
    (var current (model/get-success v nil))
    (model/set-output v 20 true nil nil nil)
    [initial
     current
     (model/get-success v nil)])
  => [11 3 11])

^{:refer xt.event.base-model/set-input :added "4.1"}
(fact "sets model input and notifies listeners"

  (!.js
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/set-input v {:data [1]})
    [out
     (. (model/get-input v) ["current"])])
  => (just-in
      [(contains-in
        {"id" "a1"
         "t" nil
         "meta" {"listener/id" "a1"
                 "listener/type" "model"}
         "data" {"type" "model.input"
                 "data" {"current" {"data" [1]}
                         "updated" integer?}}})
       {"data" [1]}])

  (!.lua
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/set-input v {:data [1]})
    [out
     (. (model/get-input v) ["current"])])
  => (just-in
      [(contains-in
        {"id" "a1"
         "t" nil
         "meta" {"listener/id" "a1"
                 "listener/type" "model"}
         "data" {"type" "model.input"
                 "data" {"current" {"data" [1]}
                         "updated" integer?}}})
       {"data" [1]}])

  (!.py
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/set-input v {:data [1]})
    [out
     (. (model/get-input v) ["current"])])
  => (just-in
      [(contains-in
        {"id" "a1"
         "t" nil
         "meta" {"listener/id" "a1"
                 "listener/type" "model"}
         "data" {"type" "model.input"
                 "data" {"current" {"data" [1]}
                         "updated" integer?}}})
       {"data" [1]}]))

^{:refer xt.event.base-model/set-output :added "4.1"}
(fact "sets output and notifies listeners"

  (!.js
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/set-output v 1 nil nil nil nil)
    [out
     (model/get-current v)])
  => (just-in
      [(contains-in
        {"id" "a1"
         "t" nil
         "meta" {"listener/id" "a1"
                 "listener/type" "model"}
         "data" {"type" "model.output"
                 "data" {"current" 1
                         "elapsed" nil
                         "tag" nil
                         "type" "output"
                         "updated" integer?}}})
       1])

  (!.lua
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/set-output v 1 nil nil nil nil)
    [out
     (model/get-current v)])
  => (just-in
      [(contains-in
        {"id" "a1"
         "t" nil
         "meta" {"listener/id" "a1"
                 "listener/type" "model"}
         "data" {"type" "model.output"
                 "data" {"current" 1
                         "elapsed" nil
                         "tag" nil
                         "type" "output"
                         "updated" integer?}}})
       1])

  (!.py
    (var v (-/make-basic-model))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/set-output v 1 nil nil nil nil)
    [out
     (model/get-current v nil)])
  => (just-in
      [(contains-in
        {"id" "a1"
         "t" nil
         "meta" {"listener/id" "a1"
                 "listener/type" "model"}
         "data" {"type" "model.output"
                 "data" {"current" 1
                         "elapsed" nil
                         "tag" nil
                         "type" "output"
                         "updated" integer?}}})
       1]))

^{:refer xt.event.base-model/set-output-disabled :added "4.1"}
(fact "sets the output disabled flag"

  (!.js
    (var v (-/make-basic-model))
    [(. (model/set-output-disabled v true nil) ["disabled"])
     (. (model/get-output v) ["disabled"])])
  => [true true]

  (!.lua
    (var v (-/make-basic-model))
    [(. (model/set-output-disabled v true nil) ["disabled"])
     (. (model/get-output v) ["disabled"])])
  => [true true]

  (!.py
    (var v (-/make-basic-model))
    [(. (model/set-output-disabled v true nil) ["disabled"])
     (. (model/get-output v nil) ["disabled"])])
  => [true true])

^{:refer xt.event.base-model/set-pending :added "4.1"}
(fact "sets the pending flag"

  (!.js
    (var v (-/make-basic-model))
    [(. (model/set-pending v true nil) ["pending"])
     (model/is-pending v)])
  => [true true]

  (!.lua
    (var v (-/make-basic-model))
    [(. (model/set-pending v true nil) ["pending"])
     (model/is-pending v)])
  => [true true]

  (!.py
    (var v (-/make-basic-model))
    [(. (model/set-pending v true nil) ["pending"])
     (model/is-pending v nil)])
  => [true true])

^{:refer xt.event.base-model/set-elapsed :added "4.1"}
(fact "sets elapsed time on the output"

  (!.js
    (var v (-/make-basic-model))
    [(. (model/set-elapsed v 25 nil) ["elapsed"])
     (model/get-time-elapsed v)])
  => [25 25]

  (!.lua
    (var v (-/make-basic-model))
    [(. (model/set-elapsed v 25 nil) ["elapsed"])
     (model/get-time-elapsed v)])
  => [25 25]

  (!.py
    (var v (-/make-basic-model))
    [(. (model/set-elapsed v 25 nil) ["elapsed"])
     (model/get-time-elapsed v nil)])
  => [25 25])

^{:refer xt.event.base-model/init-model :added "4.1"}
(fact "initialises default input data"

  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    (. (model/get-input v) ["current"]))
  => {"data" [3]}

  (!.lua
    (var v (-/make-basic-model))
    (model/init-model v)
    (. (model/get-input v) ["current"]))
  => {"data" [3]}

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    (. (model/get-input v) ["current"]))
  => {"data" [3]})

^{:refer xt.event.base-model/pipeline-prep :added "4.1"}
(fact "prepares a context and accumulator for execution"

  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    [(. context ["args"])
     disabled
     (. (. context ["acc"]) ["::"])])
  => [[3] false "model.run"]

  (!.lua
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    [(. context ["args"])
     disabled
     (. (. context ["acc"]) ["::"])])
  => [[3] false "model.run"]

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    [(. context ["args"])
     disabled
     (. (. context ["acc"]) ["::"])])
  => [[3] false "model.run"])

^{:refer xt.event.base-model/pipeline-set :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "writes pipeline output back to the model"

  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-set context "main" {"main" [true {"value" 3}]} nil)
    (model/get-current v))
  => {"value" 3}

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-set context "main" {"main" [true {"value" 3}]} nil)
    (model/get-current v nil))
  => {"value" 3})

^{:refer xt.event.base-model/pipeline-call :added "4.1"}
(fact "invokes a pipeline stage through the async adapter"

  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-call context "main" disabled model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "main" [true {"value" 3}]}

  (!.lua
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-call context "main" disabled model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "main" [true {"value" 3}]}

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-call context "main" disabled model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "main" [true {"value" 3}]})

^{:refer xt.event.base-model/pipeline-run-impl :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "runs an explicit list of pipeline stages"

  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-impl
     context
     ["main"]
     0
     model/async-fn-basic
     nil
     (fn [ctx] (return (. ctx ["acc"])))
     nil))
  => {"::" "model.run"
      "main" [true {"value" 3}]}

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-impl
     context
     ["main"]
     0
     model/async-fn-basic
     nil
     (fn [ctx] (return (. ctx ["acc"])))
     nil))
  => {"::" "model.run"
      "main" [true {"value" 3}]})

^{:refer xt.event.base-model/pipeline-run :added "4.1"}
(fact "runs the default main pipeline"

  (!.js
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run context disabled model/async-fn-basic nil nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "main" [true {"value" 3}]
      "post" [false]}

  (!.lua
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run context disabled model/async-fn-basic nil nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "main" [true {"value" 3}]
      "post" [false]}

  (!.py
    (var v (-/make-basic-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run context disabled model/async-fn-basic nil nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "main" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-model/pipeline-run-force :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "runs a forced remote or sync pipeline and can save to output"

  (!.js
    (var v (-/make-remote-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-force context true model/async-fn-basic nil nil "remote")
    [(. context ["acc"])
     (model/get-current v "remote")
     (model/get-current v)])
  => [{"::" "model.run"
       "pre" [false]
       "remote" [true {"value" 3}]
       "post" [false]}
      {"value" 3}
      {"value" 3}]

  (!.py
    (var v (-/make-remote-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-force context true model/async-fn-basic nil nil "remote")
    [(. context ["acc"])
     (model/get-current v "remote")
     (model/get-current v nil)])
  => [{"::" "model.run"
       "pre" [false]
       "remote" [true {"value" 3}]
       "post" [false]}
      {"value" 3}
      {"value" 3}])

^{:refer xt.event.base-model/pipeline-run-remote :added "4.1"}
(fact "runs the remote pipeline"

  (!.js
    (var v (-/make-remote-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-remote context true model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "remote" [true {"value" 3}]
      "post" [false]}

  (!.lua
    (var v (-/make-remote-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-remote context true model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "remote" [true {"value" 3}]
      "post" [false]}

  (!.py
    (var v (-/make-remote-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-remote context true model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "remote" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-model/pipeline-run-sync :added "4.1"}
(fact "runs the sync pipeline"

  (!.js
    (var v (-/make-sync-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-sync context true model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "sync" [true {"value" 3}]
      "post" [false]}

  (!.lua
    (var v (-/make-sync-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-sync context true model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "sync" [true {"value" 3}]
      "post" [false]}

  (!.py
    (var v (-/make-sync-model))
    (model/init-model v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-sync context true model/async-fn-basic nil nil)
    (. context ["acc"]))
  => {"::" "model.run"
      "pre" [false]
      "sync" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-model/get-with-lookup :added "4.1"}
(fact "creates results with an id lookup"

  (!.js
    (model/get-with-lookup
     [{:id "A"}
      {:id "B"}
      {:id "C"}]
     nil))
  => {"lookup" {"A" {"id" "A"}
                "B" {"id" "B"}
                "C" {"id" "C"}}
      "results" [{"id" "A"}
                 {"id" "B"}
                 {"id" "C"}]}

  (!.lua
    (model/get-with-lookup
     [{:id "A"}
      {:id "B"}
      {:id "C"}]
     nil))
  => {"lookup" {"A" {"id" "A"}
                "B" {"id" "B"}
                "C" {"id" "C"}}
      "results" [{"id" "A"}
                 {"id" "B"}
                 {"id" "C"}]}

  (!.py
    (model/get-with-lookup
     [{:id "A"}
      {:id "B"}
      {:id "C"}]
     nil))
  => {"lookup" {"A" {"id" "A"}
                "B" {"id" "B"}
                "C" {"id" "C"}}
      "results" [{"id" "A"}
                 {"id" "B"}
                 {"id" "C"}]})

^{:refer xt.event.base-model/sorted-lookup :added "4.1"}
(fact "sorts results before building a lookup"

  (!.js
    ((model/sorted-lookup "name")
     [{:id "D" :name "d"}
      {:id "B" :name "b"}
      {:id "C" :name "c"}
      {:id "A" :name "a"}]))
  => {"lookup" {"A" {"id" "A" "name" "a"}
                "B" {"id" "B" "name" "b"}
                "C" {"id" "C" "name" "c"}
                "D" {"id" "D" "name" "d"}}
      "results" [{"id" "A" "name" "a"}
                 {"id" "B" "name" "b"}
                 {"id" "C" "name" "c"}
                 {"id" "D" "name" "d"}]}

  (!.lua
    ((model/sorted-lookup "name")
     [{:id "D" :name "d"}
      {:id "B" :name "b"}
      {:id "C" :name "c"}
      {:id "A" :name "a"}]))
  => {"lookup" {"A" {"id" "A" "name" "a"}
                "B" {"id" "B" "name" "b"}
                "C" {"id" "C" "name" "c"}
                "D" {"id" "D" "name" "d"}}
      "results" [{"id" "A" "name" "a"}
                 {"id" "B" "name" "b"}
                 {"id" "C" "name" "c"}
                 {"id" "D" "name" "d"}]}

  (!.py
    ((model/sorted-lookup "name")
     [{:id "D" :name "d"}
      {:id "B" :name "b"}
      {:id "C" :name "c"}
      {:id "A" :name "a"}]))
  => {"lookup" {"A" {"id" "A" "name" "a"}
                "B" {"id" "B" "name" "b"}
                "C" {"id" "C" "name" "c"}
                "D" {"id" "D" "name" "d"}}
      "results" [{"id" "A" "name" "a"}
                 {"id" "B" "name" "b"}
                 {"id" "C" "name" "c"}
                 {"id" "D" "name" "d"}]})

^{:refer xt.event.base-model/group-by-lookup :added "4.1"}
(fact "groups results into lookup buckets"

  (!.js
    ((model/group-by-lookup "name")
     [{:id "A" :name "a"}
      {:id "B" :name "a"}
      {:id "C" :name "b"}
      {:id "D" :name "b"}]))
  => {"lookup" {"a" [{"id" "A" "name" "a"}
                     {"id" "B" "name" "a"}]
                "b" [{"id" "C" "name" "b"}
                     {"id" "D" "name" "b"}]}
      "results" [{"id" "A" "name" "a"}
                 {"id" "B" "name" "a"}
                 {"id" "C" "name" "b"}
                 {"id" "D" "name" "b"}]}

  (!.lua
    ((model/group-by-lookup "name")
     [{:id "A" :name "a"}
      {:id "B" :name "a"}
      {:id "C" :name "b"}
      {:id "D" :name "b"}]))
  => {"lookup" {"a" [{"id" "A" "name" "a"}
                     {"id" "B" "name" "a"}]
                "b" [{"id" "C" "name" "b"}
                     {"id" "D" "name" "b"}]}
      "results" [{"id" "A" "name" "a"}
                 {"id" "B" "name" "a"}
                 {"id" "C" "name" "b"}
                 {"id" "D" "name" "b"}]}

  (!.py
    ((model/group-by-lookup "name")
     [{:id "A" :name "a"}
      {:id "B" :name "a"}
      {:id "C" :name "b"}
      {:id "D" :name "b"}]))
  => {"lookup" {"a" [{"id" "A" "name" "a"}
                     {"id" "B" "name" "a"}]
                "b" [{"id" "C" "name" "b"}
                     {"id" "D" "name" "b"}]}
      "results" [{"id" "A" "name" "a"}
                 {"id" "B" "name" "a"}
                 {"id" "C" "name" "b"}
                 {"id" "D" "name" "b"}]})

(comment
  (s/snapto '[xt.event.base-model])
  
  (s/seedgen-benchadd '[xt.event.base-model] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.base-model]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-model]  {:lang [:lua :python] :write true}))


