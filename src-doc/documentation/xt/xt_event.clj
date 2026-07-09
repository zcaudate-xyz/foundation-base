(ns documentation.xt-event
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-notify :as notify]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event]
             [xt.event.base-box :as box]
             [xt.event.base-form :as form]
             [xt.event.base-route :as route]
             [xt.event.base-model :as model]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.event"
         :subtitle "Event, model, route, form, log, and validation layers."
         :lead "`xt.event` is a portable event/application layer for boxes, forms, models, routes, listeners, logs, animation, validation, decoration, tasks, and throttling."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Application state and UI flows need common event structures across runtimes. The event layer gives generated systems a shared model for routing, forms, logs, listeners, animation, and validation."

[[:chapter {:title "Internal usage" :link "internal"}]]

"Tests under `test-lang/xt/event` cover each base and utility namespace. React and substrate examples use these concepts when coordinating routes, models, and UI events."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Listener containers"}]]

"`xt.event.base-listener` provides the shared machinery used by every other event namespace: containers that hold data plus a map of listeners, and functions to add, remove, list, and trigger those listeners."

(fact "create a container and manage listeners"
  ^{:refer xt.event.base-listener/blank-container :added "4.0"}
  (!.js
    (var c (event/blank-container "demo" {:count 0}))
    (event/add-listener c
                        "a1"
                        "demo"
                        (fn [id data t meta]
                          (return data))
                        nil
                        nil)
    (var before (event/list-listeners c))
    (event/remove-listener c "a1")
    [before (event/list-listeners c)])
  => [["a1"] []])

(fact "trigger listeners with a payload"
  ^{:refer xt.event.base-listener/trigger-listeners :added "4.0"}
  (!.js
    (var c (event/blank-container "demo" {}))
    (var calls [])
    (event/add-listener c
                        "a1"
                        "demo"
                        (fn [id data t meta]
                          (xt/x:arr-push calls data))
                        nil
                        nil)
    (event/trigger-listeners c {:value 1})
    calls)
  => [{"value" 1}])

[[:section {:title "Observable boxes"}]]

"`xt.event.base-box` wraps a nested data structure in a container. Setting, merging, appending, or deleting values triggers listeners whose path predicates match the changed location."

(fact "boxes hold and update nested data"
  ^{:refer xt.event.base-box/make-box :added "4.1"}
  ^{:refer xt.event.base-box/get-data :added "4.1"}
  (!.js
    (var b (box/make-box (fn:> {:a 1 :items []})))
    (box/merge-data b [] {:b 2})
    (box/set-data b ["a"] 3)
    (box/append-data b ["items"] "hello")
    [(box/get-data b [])
     (box/get-data b ["a"])
     (box/get-data b ["b"])
     (box/get-data b ["items"])])
  => [{"a" 3 "b" 2 "items" ["hello"]}
      3
      2
      ["hello"]])

(fact "box listeners fire for matching paths"
  ^{:refer xt.event.base-box/add-listener :added "4.1"}
  (!.js
    (var b (box/make-box (fn:> {:a {:b 1}})))
    (var calls [])
    (box/add-listener b
                      "a1"
                      ["a"]
                      (fn [id data t meta]
                        (xt/x:arr-push calls (. data ["value"])))
                      nil)
    (box/set-data b ["a" "b"] 2)
    calls)
  => [2])

(fact "reset a box back to its initial value"
  ^{:refer xt.event.base-box/reset-data :added "4.1"}
  (!.js
    (var b (box/make-box (fn:> {:a 1})))
    (box/set-data b ["b"] 2)
    (box/reset-data b)
    (box/get-data b []))
  => {"a" 1})

[[:section {:title "Forms and validation"}]]

"`xt.event.base-form` stores field values and validator guards. Fields can be set individually or in bulk, and validation runs asynchronously so guards can perform remote checks."

(fact "forms track field values"
  ^{:refer xt.event.base-form/make-form :added "4.0"}
  ^{:refer xt.event.base-form/set-field :added "4.0"}
  ^{:refer xt.event.base-form/toggle-field :added "4.0"}
  (!.js
    (var f (form/make-form
            (fn:> {:email "" :active false})
            {:email [] :active []}))
    (form/set-field f "email" "a@b.com")
    (form/toggle-field f "active")
    [(form/get-field f "email")
     (form/get-field f "active")
     (form/get-data f)])
  => ["a@b.com"
      true
      {"email" "a@b.com" "active" true}])

(fact "validation results update after checking"
  ^{:refer xt.event.base-form/validate-all :added "4.0"}
  ^{:refer xt.event.base-form/check-all-passed :added "4.0"}
  (notify/wait-on :js
    (var f (form/make-form
            (fn:> {:email ""})
            {:email [["required"
                      {:check (fn [v rec]
                                (return (and (k/not-nil? v)
                                             (< 0 (xt/x:len v)))))}]]}))
    (form/set-field f "email" "a@b.com")
    (form/validate-all
     f
     nil
     (fn [ok res]
       (repl/notify (form/check-all-passed f)))))
  => true)

[[:section {:title "Routes and URLs"}]]

"`xt.event.base-route` parses URLs into path and parameter trees, lets you update them programmatically, and notifies listeners when the URL, path, or params change."

(fact "parse and inspect a route"
  ^{:refer xt.event.base-route/make-route :added "4.0"}
  ^{:refer xt.event.base-route/get-url :added "4.0"}
  ^{:refer xt.event.base-route/get-param :added "4.0"}
  (!.js
    (var r (route/make-route "users/123?tab=profile"))
    [(route/get-url r)
     (route/get-segment r [])
     (route/get-segment r ["users"])
     (route/get-param r "tab" nil)])
  => ["users/123?tab=profile"
      "users"
      "123"
      "profile"])

(fact "update a route and notify listeners"
  ^{:refer xt.event.base-route/set-url :added "4.0"}
  ^{:refer xt.event.base-route/add-url-listener :added "4.0"}
  (!.js
    (var r (route/make-route "hello?auth=sign_in"))
    (var calls [])
    (route/add-url-listener
     r
     "url"
     (fn [id data t meta]
       (xt/x:arr-push calls (. data ["type"])))
     nil)
    (route/set-url r "hello/world?auth=sign_out" nil)
    [(route/get-url r) calls])
  => ["hello/world?auth=sign_out" ["route.url"]])

[[:section {:title "Async models"}]]

"`xt.event.base-model` wraps a handler pipeline with input/output records, pending and elapsed tracking, and support for sync, remote, and main execution paths."

(fact "models expose input and output state"
  ^{:refer xt.event.base-model/create-model :added "4.0"}
  ^{:refer xt.event.base-model/init-model :added "4.0"}
  ^{:refer xt.event.base-model/get-success :added "4.0"}
  ^{:refer xt.event.base-model/is-disabled :added "4.0"}
  (!.js
    (var m (model/create-model
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (model/init-model m)
    [(model/is-disabled m)
     (model/get-success m nil)])
  => [false {"value" 0}])

(fact "models can be disabled by input"
  ^{:refer xt.event.base-model/set-input :added "4.0"}
  (!.js
    (var m (model/create-model
            (fn:> [x] {:value x})
            {}
            [3]
            {:value 0}
            nil
            nil))
    (model/set-input m {:data [3]})
    (model/is-disabled m))
  => false)

(fact "run a model pipeline asynchronously"
  ^{:refer xt.event.base-model/pipeline-run :added "4.0"}
  (notify/wait-on :js
    (var m (model/create-model
            (fn [x]
              (return (promise/x:with-delay
                       10
                       (fn:> [] {:value x}))))
            {}
            [3]
            {:value 0}
            nil
            nil))
    (model/init-model m)
    (var [context disabled] (model/pipeline-prep m))
    (-> (model/pipeline-run context
                            false
                            model/async-fn-promise
                            nil
                            nil
                            nil)
        (promise/x:promise-then
         (fn [out]
           (repl/notify (model/get-current m nil))))))
  => {"value" 3})

[[:section {:title "End-to-end: a tiny login flow"}]]

"Combining a form, validation, and a box gives a concise login flow. The form holds the fields, validation guards enforce constraints, and a box can hold the resulting session state."

(fact "validate login fields and store the result"
  (notify/wait-on :js
    (var f (form/make-form
            (fn:> {:email "" :password ""})
            {:email [["required"
                      {:check (fn [v rec]
                                (return (and (k/not-nil? v)
                                             (< 0 (xt/x:len v)))))}]]
             :password [["required"
                         {:check (fn [v rec]
                                   (return (and (k/not-nil? v)
                                                (< 0 (xt/x:len v)))))}]]}))
    (form/set-data f {:email "a@b.com" :password "secret"})
    (form/validate-all
     f
     nil
     (fn [ok res]
       (repl/notify [(form/check-all-passed f)
                     (form/check-any-errored f)
                     (form/get-data f)]))))
  => [true false {"email" "a@b.com" "password" "secret"}])

[[:chapter {:title "API" :link "api"}]]

