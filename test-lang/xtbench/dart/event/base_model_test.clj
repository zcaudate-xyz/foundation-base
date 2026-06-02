(ns xtbench.dart.event.base-model-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do 
  (l/script- :xtalk
    {:require [[xt.event.base-model :as model]
               [xt.lang.spec-base :as xt]]})
  
  (defn.xt make-basic-view []
    (return
     (model/create-view
      (fn:> [x] {:value x})
      {}
      [3]
      {:value 0}
      nil
      nil)))
  
  (defn.xt make-processed-view []
    (return
     (model/create-view
      (fn:> [x] x)
      {}
      [3]
      1
      (fn [x] (return (+ x 10)))
      nil)))
  
  (defn.xt make-remote-view []
    (return
     (model/create-view
      nil
      {:remote {:handler (fn:> [x] {:value x})}}
      [3]
      nil
      nil
      nil)))
  
  (defn.xt make-sync-view []
    (return
     (model/create-view
      nil
      {:sync {:handler (fn:> [x] {:value x})}}
      [3]
      nil
      nil
      nil)))

  (defn.xt success-async
    [handler-fn context cb]
    (return ((xt/x:get-key cb "success") (handler-fn context)))))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.event.base-model :as model]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-model/wrap-args :added "4.1"}
(fact "provides the core view helpers"

  (!.dt
    [((model/wrap-args k/identity)
      {:args [1]})
     (model/check-disabled {})
     (model/check-disabled {:input {:data [3]}})
     (model/check-disabled {:input {:data [3]
                                   :disabled true}})
     (model/parse-args {:input {:data [1 2 3]}})])
  => [1 true false true [1 2 3]])

^{:refer xt.event.base-model/check-disabled :added "4.1"}
(fact "checks disabled state from view context"

  (!.dt
    [(model/check-disabled {})
     (model/check-disabled {:input {:data [3]}})
     (model/check-disabled {:input {:data [3]
                                   :disabled true}})])
  => [true false true])

^{:refer xt.event.base-model/parse-args :added "4.1"}
(fact "parses arguments from input data"

  (!.dt
    (model/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3])

^{:refer xt.event.base-model/create-view :added "4.1"}
(fact "manages view listeners"

  (!.dt
    (var v (model/create-view
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
        "listener/type" "view"}
       ["a1"]]))

^{:refer xt.event.base-model/view-context :added "4.1"}
(fact "builds a view context with view and input"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    [(xt/x:has-key? (model/view-context v) "view")
     (xt/x:has-key? (model/view-context v) "input")])
  => [true true])

^{:refer xt.event.base-model/add-listener :added "4.1"}
(fact "adds a view listener"

  (!.dt
    (var v (-/make-basic-view))
    (var out nil)
    (model/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (model/trigger-listeners v "output" {:value 0})
    out)
  => {"id" "a1"
      "data" {"data" {"value" 0}
              "type" "output"}
      "t" nil
      "meta" {"listener/id" "a1"
              "listener/type" "view"}})

^{:refer xt.event.base-model/trigger-listeners :added "4.1"
  :setup [(def +out+
            (just-in
             [(just ["a1" "b2"] :in-any-order)
              (just ["a1" "b2"] :in-any-order)]))]}
(fact "triggers all registered view listeners"

  (!.dt
    (var v (-/make-basic-view))
    (var calls [])
    (model/add-listener v "a1" (fn [id data t meta] (xt/x:arr-push calls "a1")) nil nil)
    (model/add-listener v "b2" (fn [id data t meta] (xt/x:arr-push calls "b2")) nil nil)
    [(model/trigger-listeners v "output" {:value 0})
     calls])
  => +out+)

^{:refer xt.event.base-model/get-input :added "4.1"}
(fact "gets the current input record"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    (model/get-input v))
  => (contains-in {"current" {"data" [3]}
                   "updated" integer?}))

^{:refer xt.event.base-model/get-output :added "4.1"}
(fact "gets the output record"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    (model/get-output v))
  => {"current" nil
      "elapsed" nil
      "type" "output"
      "updated" nil})

^{:refer xt.event.base-model/get-current :added "4.1"}
(fact "gets the current output value"

  (!.dt
    (var v (-/make-basic-view))
    (model/set-output v 1 nil nil nil nil)
    (model/get-current v))
  => 1)

^{:refer xt.event.base-model/is-disabled :added "4.1"}
(fact "checks whether the view is disabled"

  (!.dt
    (var v (-/make-basic-view))
    (var before (model/is-disabled v))
    (model/init-view v)
    [before
     (model/is-disabled v)])
  => [true false])

^{:refer xt.event.base-model/is-errored :added "4.1"}
(fact "checks whether the output is errored"

  (!.dt
    (var v (-/make-basic-view))
    (model/set-output v 1 true nil nil nil)
    (model/is-errored v))
  => true)

^{:refer xt.event.base-model/is-pending :added "4.1"}
(fact "checks whether the output is pending"

  (!.dt
    (var v (-/make-basic-view))
    (model/set-pending v true nil)
    (model/is-pending v))
  => true)

^{:refer xt.event.base-model/get-time-elapsed :added "4.1"}
(fact "gets elapsed time for the output"

  (!.dt
    (var v (-/make-basic-view))
    (model/set-elapsed v 20 nil)
    (model/get-time-elapsed v))
  => 20)

^{:refer xt.event.base-model/get-time-updated :added "4.1"}
(fact "gets the last output update time"

  (!.dt
    (var v (-/make-basic-view))
    (model/set-output v 1 nil nil nil nil)
    (model/get-time-updated v))
  => integer?)

^{:refer xt.event.base-model/get-success :added "4.1"}
(fact "returns current output or processed default value"

  (!.dt
    (var v (-/make-processed-view))
    (var initial (model/get-success v nil))
    (model/set-output v 3 nil nil nil nil)
    (var current (model/get-success v nil))
    (model/set-output v 20 true nil nil nil)
    [initial
     current
     (model/get-success v nil)])
  => [11 3 11])

^{:refer xt.event.base-model/set-input :added "4.1"}
(fact "sets view input and notifies listeners"

  (!.dt
    (var v (-/make-basic-view))
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
                 "listener/type" "view"}
         "data" {"type" "view.input"
                 "data" {"current" {"data" [1]}
                         "updated" integer?}}})
       {"data" [1]}]))

^{:refer xt.event.base-model/set-output :added "4.1"}
(fact "sets output and notifies listeners"

  (!.dt
    (var v (-/make-basic-view))
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
                 "listener/type" "view"}
         "data" {"type" "view.output"
                 "data" {"current" 1
                         "elapsed" nil
                         "tag" nil
                         "type" "output"
                         "updated" integer?}}})
       1]))

^{:refer xt.event.base-model/set-output-disabled :added "4.1"}
(fact "sets the output disabled flag"

  (!.dt
    (var v (-/make-basic-view))
    [(. (model/set-output-disabled v true nil) ["disabled"])
     (. (model/get-output v) ["disabled"])])
  => [true true])

^{:refer xt.event.base-model/set-pending :added "4.1"}
(fact "sets the pending flag"

  (!.dt
    (var v (-/make-basic-view))
    [(. (model/set-pending v true nil) ["pending"])
     (model/is-pending v)])
  => [true true])

^{:refer xt.event.base-model/set-elapsed :added "4.1"}
(fact "sets elapsed time on the output"

  (!.dt
    (var v (-/make-basic-view))
    [(. (model/set-elapsed v 25 nil) ["elapsed"])
     (model/get-time-elapsed v)])
  => [25 25])

^{:refer xt.event.base-model/init-view :added "4.1"}
(fact "initialises default input data"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    (. (model/get-input v) ["current"]))
  => {"data" [3]})

^{:refer xt.event.base-model/pipeline-prep :added "4.1"}
(fact "prepares a context and accumulator for execution"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    (var [context disabled] (model/pipeline-prep v nil))
    [(. context ["args"])
     disabled
     (. (. context ["acc"]) ["::"])])
  => [[3] false "view.run"])

^{:refer xt.event.base-model/pipeline-set :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "writes pipeline output back to the view"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-set context "main" {"main" [true {"value" 3}]} nil)
    (model/get-current v))
  => {"value" 3})

^{:refer xt.event.base-model/pipeline-call :added "4.1"}
(fact "invokes a pipeline stage through the async adapter"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-call context "main" disabled -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "main" [true {"value" 3}]})

^{:refer xt.event.base-model/pipeline-run-impl :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "runs an explicit list of pipeline stages"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-impl
     context
     ["main"]
     0
     -/success-async
     nil
     (fn [ctx] (return (. ctx ["acc"])))
     nil))
  => {"::" "view.run"
      "main" [true {"value" 3}]})

^{:refer xt.event.base-model/pipeline-run :added "4.1"}
(fact "runs the default main pipeline"

  (!.dt
    (var v (-/make-basic-view))
    (model/init-view v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run context disabled -/success-async nil nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "main" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-model/pipeline-run-force :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "runs a forced remote or sync pipeline and can save to output"

  (!.dt
    (var v (-/make-remote-view))
    (model/init-view v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-force context true -/success-async nil nil "remote")
    [(. context ["acc"])
     (model/get-current v "remote")
     (model/get-current v)])
  => [{"::" "view.run"
       "pre" [false]
       "remote" [true {"value" 3}]
       "post" [false]}
      {"value" 3}
      {"value" 3}])

^{:refer xt.event.base-model/pipeline-run-remote :added "4.1"}
(fact "runs the remote pipeline"

  (!.dt
    (var v (-/make-remote-view))
    (model/init-view v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-remote context true -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "remote" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-model/pipeline-run-sync :added "4.1"}
(fact "runs the sync pipeline"

  (!.dt
    (var v (-/make-sync-view))
    (model/init-view v)
    (var [context disabled] (model/pipeline-prep v nil))
    (model/pipeline-run-sync context true -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "sync" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-model/get-with-lookup :added "4.1"}
(fact "creates results with an id lookup"

  (!.dt
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

  (!.dt
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

  (!.dt
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
