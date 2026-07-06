(ns documentation.std-lib-system
  (:require [std.lib.system :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.system` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Primitives and system predicates"}]]

"`primitive?` recognises basic scalar values, and `system?` checks whether a value satisfies the `ISystem` protocol."

(fact "detect primitive values"
  (primitive? 1)
  => true

  (primitive? "hello")
  => true

  (primitive? {:a 1})
  => false)

(fact "a plain map is not a system"
  (system? {})
  => false)

[[:section {:title "System arrays"}]]

"`array` builds a component array from a vector of configs. `array?` checks the result, and `info-array` summarises the array for display."

(fact "construct and inspect a component array"
  (let [arr (array {:constructor identity} [{:id 1} {:id 2}])]
    (array? arr)
    => true

    (count arr)
    => 2

    (vec arr)
    => [{:id 1} {:id 2}]))

[[:section {:title "Topology helpers"}]]

"Topologies can be written in short form and expanded with `long-form`. From the expanded form you can extract dependencies and exposed keys."

(fact "expand a topology to long form"
  (long-form {:db    [identity]
              :cache [identity :db]})
  => (contains {:db    (contains {:type :build
                                  :constructor identity
                                  :dependencies []})
                :cache (contains {:type :build
                                  :constructor identity
                                  :dependencies [:db]})}))

(fact "extract dependencies and exposed keys"
  (let [topo (long-form {:db     [identity]
                         :cache  [identity :db]
                         :public {:expose identity :in :cache}})]
    (get-dependencies topo)
    => {:db [] :cache [:db] :public [:cache]}

    (get-exposed topo)
    => [:public]))

[[:section {:title "Partial systems"}]]

"`valid-subcomponents` and `subsystem` let you work with a subset of a larger system. These are useful for tests and for starting only the components required by a particular feature."

(fact "find valid subcomponents for a partial system"
  (let [topo {:a [identity]
              :b [identity :a]
              :c [identity :b]
              :d [identity]}]
    (valid-subcomponents topo [:c])
    => (contains [:a :b :c] :in-any-order)))

[[:section {:title "End-to-end: build, start, and inspect a system"}]]

"A complete workflow defines a topology, constructs a system, starts it, checks its health, inspects its info, and stops it."

(fact "build and lifecycle a small system"
  (let [topo  {:db    [identity]
               :cache [identity :db]}
        sys   (system topo
                      {:db    {:host "localhost"}
                       :cache {:ttl 60}})
        sys   (start-system sys)]
    (keys sys)
    => (contains [:db :cache] :in-any-order)

    (health-system sys)
    => {:status :ok}

    (:db (info-system sys))
    => {:host "localhost"}

    (keys (stop-system sys))
    => (contains [:db :cache] :in-any-order)))

[[:chapter {:title "API" :link "std.lib.system"}]]

[[:api {:namespace "std.lib.system"}]]
