(ns xtbench.dart.event.base-listener-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.event.base-listener :as event]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-listener/blank-container :added "4.1"}
(fact "creates a blank listener container"

  (!.dt
    (event/blank-container
     "custom.container"
     {:info {:name "hello"}}))
  => {"::" "custom.container"
      "info" {"name" "hello"}
      "listeners" {}})

^{:refer xt.event.base-listener/make-container :added "4.1"}
(fact "creates a container with data and initial function"

  (!.dt
    (var c (event/make-container
            (fn:> {:hello "world"})
            "custom.container"
            {:info {:name "hello"}}))
    [(. c ["::"])
     (. c ["data"])
     (xt/x:is-function? (. c ["initial"]))
     (. c ["info"])])
  => ["custom.container"
      {"hello" "world"}
      true
      {"name" "hello"}])

^{:refer xt.event.base-listener/make-listener-entry :added "4.1"}
(fact "creates listener entry metadata"

  (!.dt
    (var entry
         (event/make-listener-entry
          "a1"
          "custom"
          (fn [id data t meta]
            (return data))
          {:label "hello"}
          (fn [e]
            (return (xt/x:get-key e "ok")))))
    [(xt/x:is-function? (. entry ["callback"]))
     (xt/x:is-function? (. entry ["pred"]))
     (. entry ["meta"])])
  => [true
      true
      {"label" "hello"
       "listener/id" "a1"
       "listener/type" "custom"}])

^{:refer xt.event.base-listener/arrayify-path :added "4.1"}
(fact "normalizes listener paths"

  (!.dt
    [(event/arrayify-path nil)
     (event/arrayify-path {})
     (event/arrayify-path "a")
     (event/arrayify-path ["a"])])
  => [[] [] ["a"] ["a"]])

^{:refer xt.event.base-listener/clear-listeners :added "4.1"}
(fact "clears non-keyed listeners"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (event/add-listener c "a1" "custom" (fn:> [id data t meta] "a1") nil nil)
    (event/add-listener c "b2" "custom" (fn:> [id data t meta] "b2") nil nil)
    [(xt/x:obj-keys (event/clear-listeners c))
     (event/list-listeners c)])
  => (just-in [(just ["a1" "b2"] :in-any-order)
               []]))

^{:refer xt.event.base-listener/add-listener :added "4.1"
  :setup [(def +out+
            (just-in
             ["custom.container"
              {"hello" "world"}
              (just ["a1" "b2"] :in-any-order)
              {"custom" (just ["a1" "b2"] :in-any-order)}
              ["k1"]
              {"group" ["k1"]}
              (just ["a1" "b2"] :in-any-order)
              ["k1"]
              ["a1" "b2" "k1"]
              {"listener/id" "a1"
               "listener/type" "custom"
               "label" "one"}
              {"listener/id" "k1"
               "listener/type" "custom"
               "label" "group"}
              ["b2"]
              empty?]))]}
(fact "manages listeners and keyed listeners"

  (!.dt
    (var c (event/make-container
            (fn:> {:hello "world"})
            "custom.container"
            {:info {:name "hello"}}))
    (var calls [])
    (event/add-listener c
                        "a1"
                        "custom"
                        (fn [id data t meta]
                          (xt/x:arr-push calls "a1"))
                        {:label "one"}
                        nil)
    (event/add-listener c
                        "b2"
                        "custom"
                        (fn [id data t meta]
                          (xt/x:arr-push calls "b2"))
                        nil
                        (fn [e]
                          (return (xt/x:get-key e "ok"))))
    (event/add-keyed-listener c
                              "group"
                              "k1"
                              "custom"
                              (fn [id data t meta]
                                (xt/x:arr-push calls "k1"))
                              {:label "group"}
                              nil)
    [(. c ["::"])
     (. c ["data"])
     (event/list-listeners c)
     (event/list-listener-types c)
     (event/list-keyed-listeners c "group")
     (event/all-keyed-listeners c)
     (event/trigger-listeners c {:ok true :data "hello"})
     (event/trigger-keyed-listeners c "group" {:data "world"})
     calls
     (. (event/remove-listener c "a1") ["meta"])
     (. (event/remove-keyed-listener c "group" "k1") ["meta"])
     (xt/x:obj-keys (event/clear-listeners c))
     (event/list-listeners c)])
  => +out+)

^{:refer xt.event.base-listener/remove-listener :added "4.1"}
(fact "removes a listener by id"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (event/add-listener c "a1" "custom" (fn:> [id data t meta] "a1") {:label "one"} nil)
    [(. (event/remove-listener c "a1") ["meta"])
     (event/remove-listener c "missing")
     (event/list-listeners c)])
  => [{"label" "one"
       "listener/id" "a1"
       "listener/type" "custom"}
      nil
      []])

^{:refer xt.event.base-listener/list-listeners :added "4.1"}
(fact "lists all non-keyed listeners"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (event/add-listener c "a1" "custom" (fn:> [id data t meta] "a1") nil nil)
    (event/add-listener c "b2" "custom" (fn:> [id data t meta] "b2") nil nil)
    (event/list-listeners c))
  => (just ["a1" "b2"] :in-any-order))

^{:refer xt.event.base-listener/list-listener-types :added "4.1"}
(fact "indexes listeners by type"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (event/add-listener c "a1" "custom" (fn:> [id data t meta] "a1") nil nil)
    (event/add-listener c "b2" "route" (fn:> [id data t meta] "b2") nil nil)
    (event/list-listener-types c))
  => {"custom" ["a1"]
      "route" ["b2"]})

^{:refer xt.event.base-listener/trigger-entry :added "4.1"}
(fact "merges listener metadata into the event"

  (!.dt
    (var out nil)
    (event/trigger-entry
     (event/make-listener-entry
      "a1"
      "custom"
      (fn [id data t meta]
        (:= out {"id" id "data" data "t" t "meta" meta}))
      {:label "hello"}
      nil)
     {:meta {:base true}})
    out)
  => {"id" "a1"
      "data" {}
      "t" nil
      "meta" {"base" true
              "label" "hello"
              "listener/id" "a1"
              "listener/type" "custom"}})

^{:refer xt.event.base-listener/trigger-listeners :added "4.1"}
(fact "triggers all non-keyed listeners"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (var calls [])
    (event/add-listener
     c "a1" "custom"
     (fn [id data t meta]
       (xt/x:arr-push calls "a1"))
     nil
     nil)
    (event/add-listener
     c "b2" "custom"
     (fn [id data t meta]
       (xt/x:arr-push calls "b2"))
     nil
     (fn [e]
       (return (xt/x:get-key e "ok"))))
    [(event/trigger-listeners c {:ok true})
     calls])
  => (just-in [(just ["a1" "b2"] :in-any-order)
               ["a1" "b2"]]))

^{:refer xt.event.base-listener/add-keyed-listener :added "4.1"}
(fact "adds a keyed listener entry"

  (!.dt
    (. (event/add-keyed-listener
        (event/blank-container "custom.container" {})
        "group"
        "k1"
        "custom"
        (fn:> [id data t meta] "k1")
        {:label "hello"}
        nil)
       ["meta"]))
  => {"label" "hello"
      "listener/id" "k1"
      "listener/type" "custom"})

^{:refer xt.event.base-listener/remove-keyed-listener :added "4.1"}
(fact "removes keyed listeners and cleans empty groups"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (event/add-keyed-listener c "group" "k1" "custom" (fn:> [id data t meta] "k1") nil nil)
    [(. (event/remove-keyed-listener c "group" "k1") ["meta"])
     (event/remove-keyed-listener c "group" "missing")
     (event/all-keyed-listeners c)])
  => [{"listener/id" "k1"
       "listener/type" "custom"}
      nil
      {}])

^{:refer xt.event.base-listener/list-keyed-listeners :added "4.1"}
(fact "lists keyed listeners for a group"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (event/add-keyed-listener c "group" "k1" "custom" (fn:> [id data t meta] "k1") nil nil)
    (event/add-keyed-listener c "group" "k2" "custom" (fn:> [id data t meta] "k2") nil nil)
    [(event/list-keyed-listeners c "group")
     (event/list-keyed-listeners c "missing")])
  => (just-in [(just ["k1" "k2"] :in-any-order)
               []]))

^{:refer xt.event.base-listener/all-keyed-listeners :added "4.1"}
(fact "lists all keyed listener groups"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (event/add-keyed-listener c "group-a" "k1" "custom" (fn:> [id data t meta] "k1") nil nil)
    (event/add-keyed-listener c "group-b" "k2" "custom" (fn:> [id data t meta] "k2") nil nil)
    (event/all-keyed-listeners c))
  => {"group-a" ["k1"]
      "group-b" ["k2"]})

^{:refer xt.event.base-listener/trigger-keyed-listeners :added "4.1"}
(fact "triggers keyed listeners for a specific group"

  (!.dt
    (var c (event/blank-container "custom.container" {}))
    (var calls [])
    (event/add-keyed-listener
     c "group-a" "k1" "custom"
     (fn [id data t meta]
       (xt/x:arr-push calls "k1"))
     nil nil)
    (event/add-keyed-listener
     c "group-a" "k2" "custom"
     (fn [id data t meta]
       (xt/x:arr-push calls "k2"))
     nil nil)
    (event/add-keyed-listener
     c "group-b" "k3" "custom"
     (fn [id data t meta]
       (xt/x:arr-push calls "k3"))
     nil nil)
    [(event/trigger-keyed-listeners c "group-a" {:ok true})
     calls])
  => (just-in [(just ["k1" "k2"] :in-any-order)
               (just ["k1" "k2"] :in-any-order)]))

(comment
  (s/snapto '[xt.event.base-listener])

  (s/run '[xt.event.base-listener])
  
  (s/seedgen-benchadd '[xt.event.base-listener] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.base-listener]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-listener]  {:lang [:lua :python] :write true}))
