(ns documentation.std-lib-resource
  (:require [std.lib.resource :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.resource` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Registering resource specs"}]]

"Resources are described by specs and variants. `res:spec-add` registers a new spec, `res:variant-add` adds a variant, and `res:spec-list` and `res:variant-list` query the registry."

(fact "register and inspect a resource spec"
  (res:spec-add {:type :demo/counter
                 :mode {:key :id
                        :allow #{:global :shared}
                        :default :shared}
                 :instance {:create (fn [config] (atom (:start config 0)))
                            :setup identity
                            :teardown identity}})
  => any?

  (res:spec-list)
  => (contains [:demo/counter])

  (res:variant-add :demo/counter {:id :default})
  => any?

  (res:variant-list :demo/counter)
  => (contains [:default]))

[[:section {:title "Inspecting specs and modes"}]]

"Retrieve the merged spec/variant with `res:variant-get`, and query the default mode with `res:mode`."

(fact "inspect a registered resource"
  (res:mode :demo/counter)
  => :shared

  (:mode (res:variant-get :demo/counter))
  => (contains {:default :shared}))

[[:section {:title "Resource keys and paths"}]]

"`res-key` computes the key used to identify an active resource, and `res-path` builds the full path used to store it."

(fact "compute resource keys and paths"
  (res-key :shared :demo/counter :default {:id :main})
  => :main

  (res-path :shared :demo/counter :default {:id :main})
  => '(:shared [:demo/counter :default] :main))

[[:section {:title "Starting and stopping resources"}]]

"The `res` function (and friends such as `res:start` and `res:stop`) manage the lifecycle of active resources."

(fact "start and stop a resource"
  (let [counter (res :demo/counter {:id :main :start 10})]
    @counter
    => 10

    (res:exists? :demo/counter {:id :main})
    => true

    (res:stop :demo/counter {:id :main})
    => any?))

[[:section {:title "End-to-end: register, start, query, and tear down"}]]

"A complete resource workflow: register a spec and variant, start an instance, verify it is active, then stop it and confirm it is gone."

(fact "lifecycle a counter resource"
  (res:spec-add {:type :demo/gauge
                 :mode {:key :id
                        :allow #{:shared}
                        :default :shared}
                 :instance {:create (fn [config] (atom (:value config 0)))
                            :setup identity
                            :teardown identity}})
  (res:variant-add :demo/gauge {:id :default})

  (let [g (res :demo/gauge {:id :primary :value 42})]
    @g
    => 42)

  (res:active :demo/gauge)
  => (contains {:shared (contains {:demo/gauge (contains {:default (contains [:primary])})})})

  (res:stop :demo/gauge {:id :primary})

  (res:exists? :demo/gauge {:id :primary})
  => false)

[[:chapter {:title "API" :link "std.lib.resource"}]]

[[:api {:namespace "std.lib.resource"}]]
