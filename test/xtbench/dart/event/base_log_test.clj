(ns xtbench.dart.event.base-log-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.event.base-log :as log]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-log/queue-entry :added "4.1"}
(fact "queues and slices log entries"

  (!.dt
   (var l (log/new-log {}))
   (log/queue-entry l {:id "id-0"}
                    (fn [x _]
                      (return (xt/x:get-key x "id")))
                    k/identity
                    1)
   (log/queue-entry l {:id "id-1"}
                    (fn [x _]
                      (return (xt/x:get-key x "id")))
                    k/identity
                    1)
   (log/queue-entry l {:id "id-2"}
                    (fn [x _]
                      (return (xt/x:get-key x "id")))
                    k/identity
                    1)
   [(log/get-count l)
    (log/get-last l)
    (log/get-head l 2)
    (log/get-tail l 2)
    (log/get-slice l 1 3)
    (log/clear l)
    (log/get-count l)])
  => [3
      {"id" "id-2"}
      [{"id" "id-0"}
       {"id" "id-1"}]
      [{"id" "id-1"}
       {"id" "id-2"}]
      [{"id" "id-1"}
       {"id" "id-2"}]
      [{"id" "id-0"}
       {"id" "id-1"}
       {"id" "id-2"}]
      0])

^{:refer xt.event.base-log/add-listener :added "4.1"}
(fact "adds and removes log listeners"

  (!.dt
   (var l (log/new-log {}))
   (log/add-listener l "a1" (fn:> [id data t meta] nil) nil)
   (log/add-listener l "b2" (fn:> [id data t meta] nil) nil)
   [(log/list-listeners l)
    (. (log/remove-listener l "b2") ["meta"])
    (log/list-listeners l)])
  => (just-in
      [(just ["a1" "b2"] :in-any-order)
       {"listener/id" "b2"
        "listener/type" "log"}
       ["a1"]]))

^{:refer xt.event.base-log/new-log :added "4.1"}
(fact "creates a new log container"

  (!.dt
   (var l (log/new-log {:interval 10
                        :maximum 5}))
   [(. l ["::"])
    (. l ["interval"])
    (. l ["maximum"])
    (. l ["callback"])
    (log/get-count l)])
  => ["event.log" 10 5 nil 0])

^{:refer xt.event.base-log/get-count :added "4.1"}
(fact "returns the processed count"

  (!.dt
   (log/get-count
    (log/new-log {:processed [{"id" "a"}
                              {"id" "b"}]})))
  => 2)

^{:refer xt.event.base-log/get-last :added "4.1"}
(fact "returns the last processed entry"

  (!.dt
   (log/get-last
    (log/new-log {:processed [{"id" "a"}
                              {"id" "b"}]})))
  => {"id" "b"})

^{:refer xt.event.base-log/get-head :added "4.1"}
(fact "returns the first n entries"

  (!.dt
   (log/get-head
    (log/new-log {:processed [{"id" "a"}
                              {"id" "b"}
                              {"id" "c"}]})
    2))
  => [{"id" "a"}
      {"id" "b"}])

^{:refer xt.event.base-log/get-filtered :added "4.1"}
(fact "filters processed entries"

  (!.dt
   (log/get-filtered
    (log/new-log {:processed [{"id" "a" "keep" true}
                              {"id" "b" "keep" false}
                              {"id" "c" "keep" true}]})
    (fn [entry]
      (return (xt/x:get-key entry "keep")))))
  => [{"id" "a" "keep" true}
      {"id" "c" "keep" true}])

^{:refer xt.event.base-log/get-tail :added "4.1"}
(fact "returns the last n entries"

  (!.dt
   (log/get-tail
    (log/new-log {:processed [{"id" "a"}
                              {"id" "b"}
                              {"id" "c"}]})
    2))
  => [{"id" "b"}
      {"id" "c"}])

^{:refer xt.event.base-log/get-slice :added "4.1"}
(fact "returns a bounded slice"

  (!.dt
   (log/get-slice
    (log/new-log {:processed [{"id" "a"}
                              {"id" "b"}
                              {"id" "c"}]})
    1
    3))
  => [{"id" "b"}
      {"id" "c"}])

^{:refer xt.event.base-log/clear :added "4.1"}
(fact "clears processed entries"

  (!.dt
   (var l (log/new-log {:processed [{"id" "a"}
                                    {"id" "b"}]}))
   [(log/clear l)
    (log/get-count l)])
  => [[{"id" "a"}
       {"id" "b"}]
      0])

^{:refer xt.event.base-log/clear-cache :added "4.1"}
(fact "evicts stale cache keys"

  (!.dt
   (var l (log/new-log {:interval 10
                        :cache {"a" 0
                                "b" 15}}))
   [(log/clear-cache l 20)
    (xt/x:obj-keys (. l ["cache"]))
    (. l ["last"])])
  => [["a"]
      ["b"]
      20])

(comment
  (s/snapto '[xt.event.base-log])
  
  (s/seedgen-benchadd '[xt.event.base-log] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.base-log]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-log]  {:lang [:lua :python] :write true}))
