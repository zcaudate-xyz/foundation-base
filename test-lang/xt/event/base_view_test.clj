(ns xt.event.base-view-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do 
  (l/script- :xtalk
    {:require [[xt.event.base-view :as view]
               [xt.lang.spec-base :as xt]]})
  
  (defn.xt make-basic-view []
    (return
     (view/create-view
      (fn:> [x] {:value x})
      {}
      [3]
      {:value 0}
      nil
      nil)))
  
  (defn.xt make-processed-view []
    (return
     (view/create-view
      (fn:> [x] x)
      {}
      [3]
      1
      (fn [x] (return (+ x 10)))
      nil)))
  
  (defn.xt make-remote-view []
    (return
     (view/create-view
      nil
      {:remote {:handler (fn:> [x] {:value x})}}
      [3]
      nil
      nil
      nil)))
  
  (defn.xt make-sync-view []
    (return
     (view/create-view
      nil
      {:sync {:handler (fn:> [x] {:value x})}}
      [3]
      nil
      nil
      nil)))

  (defn.xt success-async
    [handler-fn context cb]
    (return ((xt/x:get-key cb "success") (handler-fn context)))))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.event.base-view :as view]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.event.base-view :as view]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.event.base-view :as view]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-view/wrap-args :added "4.1"}
(fact "provides the core view helpers"

  (!.js
    [((view/wrap-args k/identity)
      {:args [1]})
     (view/check-disabled {})
     (view/check-disabled {:input {:data [3]}})
     (view/check-disabled {:input {:data [3]
                                   :disabled true}})
     (view/parse-args {:input {:data [1 2 3]}})])
  => [1 true false true [1 2 3]]

  (!.lua
    [((view/wrap-args k/identity)
      {:args [1]})
     (view/check-disabled {})
     (view/check-disabled {:input {:data [3]}})
     (view/check-disabled {:input {:data [3]
                                   :disabled true}})
     (view/parse-args {:input {:data [1 2 3]}})])
  => [1 true false true [1 2 3]]

  (!.py
    [((view/wrap-args k/identity)
      {:args [1]})
     (view/check-disabled {})
     (view/check-disabled {:input {:data [3]}})
     (view/check-disabled {:input {:data [3]
                                   :disabled true}})
     (view/parse-args {:input {:data [1 2 3]}})])
  => [1 true false true [1 2 3]])

^{:refer xt.event.base-view/create-view :added "4.1"}
(fact "manages view listeners"

  ^{:seedgen/base {:lua {:expect (just-in
                                  [{"type" "output"}
                                   (just ["a1" "b2"] :in-any-order)
                                   {"listener/id" "b2"
                                    "listener/type" "view"}
                                   ["a1"]])}}}
  (!.js
    (var v (view/create-view
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (view/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (view/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(view/get-output v)
     (view/list-listeners v)
     (. (view/remove-listener v "b2") ["meta"])
     (view/list-listeners v)])
  => (just-in
      [{"current" nil
        "elapsed" nil
        "type" "output"
        "updated" nil}
       (just ["a1" "b2"] :in-any-order)
       {"listener/id" "b2"
        "listener/type" "view"}
       ["a1"]])

  (!.lua
    (var v (view/create-view
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (view/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (view/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(view/get-output v)
     (view/list-listeners v)
     (. (view/remove-listener v "b2") ["meta"])
     (view/list-listeners v)])
  => (just-in [{"type" "output"} (just ["a1" "b2"] :in-any-order) {"listener/id" "b2", "listener/type" "view"} ["a1"]])

  (!.py
    (var v (view/create-view
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (view/add-listener v "a1" (fn:> [id data t meta] nil) nil nil)
    (view/add-listener v "b2" (fn:> [id data t meta] nil) nil nil)
    [(view/get-output v)
     (view/list-listeners v)
     (. (view/remove-listener v "b2") ["meta"])
     (view/list-listeners v)])
  => (just-in
      [{"current" nil
        "elapsed" nil
        "type" "output"
        "updated" nil}
       (just ["a1" "b2"] :in-any-order)
       {"listener/id" "b2"
        "listener/type" "view"}
       ["a1"]]))

^{:refer xt.event.base-view/check-disabled :added "4.1"}
(fact "checks disabled state from view context"

  (!.js
    [(view/check-disabled {})
     (view/check-disabled {:input {:data [3]}})
     (view/check-disabled {:input {:data [3]
                                   :disabled true}})])
  => [true false true]

  (!.lua
    [(view/check-disabled {})
     (view/check-disabled {:input {:data [3]}})
     (view/check-disabled {:input {:data [3]
                                   :disabled true}})])
  => [true false true]

  (!.py
    [(view/check-disabled {})
     (view/check-disabled {:input {:data [3]}})
     (view/check-disabled {:input {:data [3]
                                   :disabled true}})])
  => [true false true])

^{:refer xt.event.base-view/parse-args :added "4.1"}
(fact "parses arguments from input data"

  (!.js
    (view/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3]

  (!.lua
    (view/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3]

  (!.py
    (view/parse-args {:input {:data [1 2 3]}}))
  => [1 2 3])

^{:refer xt.event.base-view/view-context :added "4.1"}
(fact "builds a view context with view and input"

  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    [(xt/x:has-key? (view/view-context v) "view")
     (xt/x:has-key? (view/view-context v) "input")])
  => [true true]

  (!.lua
    (var v (-/make-basic-view))
    (view/init-view v)
    [(xt/x:has-key? (view/view-context v) "view")
     (xt/x:has-key? (view/view-context v) "input")])
  => [true true]

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    [(xt/x:has-key? (view/view-context v) "view")
     (xt/x:has-key? (view/view-context v) "input")])
  => [true true])

^{:refer xt.event.base-view/add-listener :added "4.1"}
(fact "adds a view listener"

  ^{:seedgen/base {:lua {:expect {"id" "a1"
                                  "data" {"data" {"value" 0}
                                          "type" "output"}
                                  "meta" {"listener/id" "a1"
                                          "listener/type" "view"}}}}}
  (!.js
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/trigger-listeners v "output" {:value 0})
    out)
  => {"id" "a1"
      "data" {"data" {"value" 0}
              "type" "output"}
      "t" nil
      "meta" {"listener/id" "a1"
              "listener/type" "view"}}

  (!.lua
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/trigger-listeners v "output" {:value 0})
    out)
  => {"id" "a1", "meta" {"listener/id" "a1", "listener/type" "view"}, "data" {"type" "output", "data" {"value" 0}}}

  (!.py
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/trigger-listeners v "output" {:value 0})
    out)
  => {"id" "a1"
      "data" {"data" {"value" 0}
              "type" "output"}
      "t" nil
      "meta" {"listener/id" "a1"
              "listener/type" "view"}})

^{:refer xt.event.base-view/trigger-listeners :added "4.1"
  :setup [(def +out+
            (just-in
             [(just ["a1" "b2"] :in-any-order)
              (just ["a1" "b2"] :in-any-order)]))]}
(fact "triggers all registered view listeners"

  (!.js
    (var v (-/make-basic-view))
    (var calls [])
    (view/add-listener v "a1" (fn [id data t meta] (xt/x:arr-push calls "a1")) nil nil)
    (view/add-listener v "b2" (fn [id data t meta] (xt/x:arr-push calls "b2")) nil nil)
    [(view/trigger-listeners v "output" {:value 0})
     calls])
  => +out+

  (!.lua
    (var v (-/make-basic-view))
    (var calls [])
    (view/add-listener v "a1" (fn [id data t meta] (xt/x:arr-push calls "a1")) nil nil)
    (view/add-listener v "b2" (fn [id data t meta] (xt/x:arr-push calls "b2")) nil nil)
    [(view/trigger-listeners v "output" {:value 0})
     calls])
  => +out+

  (!.py
    (var v (-/make-basic-view))
    (var calls [])
    (view/add-listener v "a1" (fn [id data t meta] (xt/x:arr-push calls "a1")) nil nil)
    (view/add-listener v "b2" (fn [id data t meta] (xt/x:arr-push calls "b2")) nil nil)
    [(view/trigger-listeners v "output" {:value 0})
     calls])
  => +out+)

^{:refer xt.event.base-view/get-input :added "4.1"}
(fact "gets the current input record"

  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    (view/get-input v))
  => (contains-in {"current" {"data" [3]}
                   "updated" integer?})

  (!.lua
    (var v (-/make-basic-view))
    (view/init-view v)
    (view/get-input v))
  => (contains-in {"current" {"data" [3]}
                   "updated" integer?})

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    (view/get-input v))
  => (contains-in {"current" {"data" [3]}
                   "updated" integer?}))

^{:refer xt.event.base-view/get-output :added "4.1"}
(fact "gets the output record"

  ^{:seedgen/base {:lua {:expect {"type" "output"}}}}
  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    (view/get-output v))
  => {"current" nil
      "elapsed" nil
      "type" "output"
      "updated" nil}

  (!.lua
    (var v (-/make-basic-view))
    (view/init-view v)
    (view/get-output v))
  => {"type" "output"}

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    (view/get-output v))
  => {"current" nil
      "elapsed" nil
      "type" "output"
      "updated" nil})

^{:refer xt.event.base-view/get-current :added "4.1"}
(fact "gets the current output value"

  (!.js
    (var v (-/make-basic-view))
    (view/set-output v 1 nil nil nil nil)
    (view/get-current v))
  => 1

  (!.lua
    (var v (-/make-basic-view))
    (view/set-output v 1 nil nil nil nil)
    (view/get-current v))
  => 1

  (!.py
    (var v (-/make-basic-view))
    (view/set-output v 1 nil nil nil nil)
    (view/get-current v))
  => 1)

^{:refer xt.event.base-view/is-disabled :added "4.1"}
(fact "checks whether the view is disabled"

  (!.js
    (var v (-/make-basic-view))
    (var before (view/is-disabled v))
    (view/init-view v)
    [before
     (view/is-disabled v)])
  => [true false]

  (!.lua
    (var v (-/make-basic-view))
    (var before (view/is-disabled v))
    (view/init-view v)
    [before
     (view/is-disabled v)])
  => [true false]

  (!.py
    (var v (-/make-basic-view))
    (var before (view/is-disabled v))
    (view/init-view v)
    [before
     (view/is-disabled v)])
  => [true false])

^{:refer xt.event.base-view/is-errored :added "4.1"}
(fact "checks whether the output is errored"

  (!.js
    (var v (-/make-basic-view))
    (view/set-output v 1 true nil nil nil)
    (view/is-errored v))
  => true

  (!.lua
    (var v (-/make-basic-view))
    (view/set-output v 1 true nil nil nil)
    (view/is-errored v))
  => true

  (!.py
    (var v (-/make-basic-view))
    (view/set-output v 1 true nil nil nil)
    (view/is-errored v))
  => true)

^{:refer xt.event.base-view/is-pending :added "4.1"}
(fact "checks whether the output is pending"

  (!.js
    (var v (-/make-basic-view))
    (view/set-pending v true nil)
    (view/is-pending v))
  => true

  (!.lua
    (var v (-/make-basic-view))
    (view/set-pending v true nil)
    (view/is-pending v))
  => true

  (!.py
    (var v (-/make-basic-view))
    (view/set-pending v true nil)
    (view/is-pending v))
  => true)

^{:refer xt.event.base-view/get-time-elapsed :added "4.1"}
(fact "gets elapsed time for the output"

  (!.js
    (var v (-/make-basic-view))
    (view/set-elapsed v 20 nil)
    (view/get-time-elapsed v))
  => 20

  (!.lua
    (var v (-/make-basic-view))
    (view/set-elapsed v 20 nil)
    (view/get-time-elapsed v))
  => 20

  (!.py
    (var v (-/make-basic-view))
    (view/set-elapsed v 20 nil)
    (view/get-time-elapsed v))
  => 20)

^{:refer xt.event.base-view/get-time-updated :added "4.1"}
(fact "gets the last output update time"

  (!.js
    (var v (-/make-basic-view))
    (view/set-output v 1 nil nil nil nil)
    (view/get-time-updated v))
  => integer?

  (!.lua
    (var v (-/make-basic-view))
    (view/set-output v 1 nil nil nil nil)
    (view/get-time-updated v))
  => integer?

  (!.py
    (var v (-/make-basic-view))
    (view/set-output v 1 nil nil nil nil)
    (view/get-time-updated v))
  => integer?)

^{:refer xt.event.base-view/get-success :added "4.1"}
(fact "returns current output or processed default value"

  (!.js
    (var v (-/make-processed-view))
    (var initial (view/get-success v nil))
    (view/set-output v 3 nil nil nil nil)
    (var current (view/get-success v nil))
    (view/set-output v 20 true nil nil nil)
    [initial
     current
     (view/get-success v nil)])
  => [11 3 11]

  (!.lua
    (var v (-/make-processed-view))
    (var initial (view/get-success v nil))
    (view/set-output v 3 nil nil nil nil)
    (var current (view/get-success v nil))
    (view/set-output v 20 true nil nil nil)
    [initial
     current
     (view/get-success v nil)])
  => [11 3 11]

  (!.py
    (var v (-/make-processed-view))
    (var initial (view/get-success v nil))
    (view/set-output v 3 nil nil nil nil)
    (var current (view/get-success v nil))
    (view/set-output v 20 true nil nil nil)
    [initial
     current
     (view/get-success v nil)])
  => [11 3 11])

^{:refer xt.event.base-view/set-input :added "4.1"}
(fact "sets view input and notifies listeners"

  (!.js
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/set-input v {:data [1]})
    [out
     (. (view/get-input v) ["current"])])
  => (just-in
      [(contains-in
        {"id" "a1"
         "t" nil
         "meta" {"listener/id" "a1"
                 "listener/type" "view"}
         "data" {"type" "view.input"
                 "data" {"current" {"data" [1]}
                         "updated" integer?}}})
       {"data" [1]}])

  (!.lua
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/set-input v {:data [1]})
    [out
     (. (view/get-input v) ["current"])])
  => (just-in
      [(contains-in
        {"id" "a1"
         "t" nil
         "meta" {"listener/id" "a1"
                 "listener/type" "view"}
         "data" {"type" "view.input"
                 "data" {"current" {"data" [1]}
                         "updated" integer?}}})
       {"data" [1]}])

  (!.py
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/set-input v {:data [1]})
    [out
     (. (view/get-input v) ["current"])])
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

^{:refer xt.event.base-view/set-output :added "4.1"}
(fact "sets output and notifies listeners"

  (!.js
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/set-output v 1 nil nil nil nil)
    [out
     (view/get-current v)])
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
       1])

  (!.lua
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/set-output v 1 nil nil nil nil)
    [out
     (view/get-current v)])
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
       1])

  (!.py
    (var v (-/make-basic-view))
    (var out nil)
    (view/add-listener v "a1" (fn [id data t meta] (:= out {"id" id "data" data "t" t "meta" meta})) nil nil)
    (view/set-output v 1 nil nil nil nil)
    [out
     (view/get-current v)])
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

^{:refer xt.event.base-view/set-output-disabled :added "4.1"}
(fact "sets the output disabled flag"

  (!.js
    (var v (-/make-basic-view))
    [(. (view/set-output-disabled v true nil) ["disabled"])
     (. (view/get-output v) ["disabled"])])
  => [true true]

  (!.lua
    (var v (-/make-basic-view))
    [(. (view/set-output-disabled v true nil) ["disabled"])
     (. (view/get-output v) ["disabled"])])
  => [true true]

  (!.py
    (var v (-/make-basic-view))
    [(. (view/set-output-disabled v true nil) ["disabled"])
     (. (view/get-output v) ["disabled"])])
  => [true true])

^{:refer xt.event.base-view/set-pending :added "4.1"}
(fact "sets the pending flag"

  (!.js
    (var v (-/make-basic-view))
    [(. (view/set-pending v true nil) ["pending"])
     (view/is-pending v)])
  => [true true]

  (!.lua
    (var v (-/make-basic-view))
    [(. (view/set-pending v true nil) ["pending"])
     (view/is-pending v)])
  => [true true]

  (!.py
    (var v (-/make-basic-view))
    [(. (view/set-pending v true nil) ["pending"])
     (view/is-pending v)])
  => [true true])

^{:refer xt.event.base-view/set-elapsed :added "4.1"}
(fact "sets elapsed time on the output"

  (!.js
    (var v (-/make-basic-view))
    [(. (view/set-elapsed v 25 nil) ["elapsed"])
     (view/get-time-elapsed v)])
  => [25 25]

  (!.lua
    (var v (-/make-basic-view))
    [(. (view/set-elapsed v 25 nil) ["elapsed"])
     (view/get-time-elapsed v)])
  => [25 25]

  (!.py
    (var v (-/make-basic-view))
    [(. (view/set-elapsed v 25 nil) ["elapsed"])
     (view/get-time-elapsed v)])
  => [25 25])

^{:refer xt.event.base-view/init-view :added "4.1"}
(fact "initialises default input data"

  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    (. (view/get-input v) ["current"]))
  => {"data" [3]}

  (!.lua
    (var v (-/make-basic-view))
    (view/init-view v)
    (. (view/get-input v) ["current"]))
  => {"data" [3]}

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    (. (view/get-input v) ["current"]))
  => {"data" [3]})

^{:refer xt.event.base-view/pipeline-prep :added "4.1"}
(fact "prepares a context and accumulator for execution"

  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    [(. context ["args"])
     disabled
     (. (. context ["acc"]) ["::"])])
  => [[3] false "view.run"]

  (!.lua
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    [(. context ["args"])
     disabled
     (. (. context ["acc"]) ["::"])])
  => [[3] false "view.run"]

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    [(. context ["args"])
     disabled
     (. (. context ["acc"]) ["::"])])
  => [[3] false "view.run"])

^{:refer xt.event.base-view/pipeline-set :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "writes pipeline output back to the view"

  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-set context "main" {"main" [true {"value" 3}]} nil)
    (view/get-current v))
  => {"value" 3}

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-set context "main" {"main" [true {"value" 3}]} nil)
    (view/get-current v))
  => {"value" 3})

^{:refer xt.event.base-view/pipeline-call :added "4.1"}
(fact "invokes a pipeline stage through the async adapter"

  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-call context "main" disabled -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "main" [true {"value" 3}]}

  (!.lua
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-call context "main" disabled -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "main" [true {"value" 3}]}

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-call context "main" disabled -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "main" [true {"value" 3}]})

^{:refer xt.event.base-view/pipeline-run-impl :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "runs an explicit list of pipeline stages"

  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-impl
     context
     ["main"]
     0
     -/success-async
     nil
     (fn [ctx] (return (. ctx ["acc"])))
     nil))
  => {"::" "view.run"
      "main" [true {"value" 3}]}

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-impl
     context
     ["main"]
     0
     -/success-async
     nil
     (fn [ctx] (return (. ctx ["acc"])))
     nil))
  => {"::" "view.run"
      "main" [true {"value" 3}]})

^{:refer xt.event.base-view/pipeline-run :added "4.1"}
(fact "runs the default main pipeline"

  (!.js
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run context disabled -/success-async nil nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "main" [true {"value" 3}]
      "post" [false]}

  (!.lua
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run context disabled -/success-async nil nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "main" [true {"value" 3}]
      "post" [false]}

  (!.py
    (var v (-/make-basic-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run context disabled -/success-async nil nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "main" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-view/pipeline-run-force :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "runs a forced remote or sync pipeline and can save to output"

  (!.js
    (var v (-/make-remote-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-force context true -/success-async nil nil "remote")
    [(. context ["acc"])
     (view/get-current v "remote")
     (view/get-current v)])
  => [{"::" "view.run"
       "pre" [false]
       "remote" [true {"value" 3}]
       "post" [false]}
      {"value" 3}
      {"value" 3}]

  (!.py
    (var v (-/make-remote-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-force context true -/success-async nil nil "remote")
    [(. context ["acc"])
     (view/get-current v "remote")
     (view/get-current v)])
  => [{"::" "view.run"
       "pre" [false]
       "remote" [true {"value" 3}]
       "post" [false]}
      {"value" 3}
      {"value" 3}])

^{:refer xt.event.base-view/pipeline-run-remote :added "4.1"}
(fact "runs the remote pipeline"

  (!.js
    (var v (-/make-remote-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-remote context true -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "remote" [true {"value" 3}]
      "post" [false]}

  (!.lua
    (var v (-/make-remote-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-remote context true -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "remote" [true {"value" 3}]
      "post" [false]}

  (!.py
    (var v (-/make-remote-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-remote context true -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "remote" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-view/pipeline-run-sync :added "4.1"}
(fact "runs the sync pipeline"

  (!.js
    (var v (-/make-sync-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-sync context true -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "sync" [true {"value" 3}]
      "post" [false]}

  (!.lua
    (var v (-/make-sync-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-sync context true -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "sync" [true {"value" 3}]
      "post" [false]}

  (!.py
    (var v (-/make-sync-view))
    (view/init-view v)
    (var [context disabled] (view/pipeline-prep v nil))
    (view/pipeline-run-sync context true -/success-async nil nil)
    (. context ["acc"]))
  => {"::" "view.run"
      "pre" [false]
      "sync" [true {"value" 3}]
      "post" [false]})

^{:refer xt.event.base-view/get-with-lookup :added "4.1"}
(fact "creates results with an id lookup"

  (!.js
    (view/get-with-lookup
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
    (view/get-with-lookup
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
    (view/get-with-lookup
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

^{:refer xt.event.base-view/sorted-lookup :added "4.1"}
(fact "sorts results before building a lookup"

  (!.js
    ((view/sorted-lookup "name")
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
    ((view/sorted-lookup "name")
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
    ((view/sorted-lookup "name")
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

^{:refer xt.event.base-view/group-by-lookup :added "4.1"}
(fact "groups results into lookup buckets"

  (!.js
    ((view/group-by-lookup "name")
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
    ((view/group-by-lookup "name")
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
    ((view/group-by-lookup "name")
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
  (s/snapto '[xt.event.base-view])
  
  (s/seedgen-benchadd '[xt.event.base-view] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.base-view]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-view]  {:lang [:lua :python] :write true}))
