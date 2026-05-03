(ns xtbench.dart.event.util-throttle-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-throttle :as throttle]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.util-throttle/throttle-create :added "4.1"}
(fact "creates a throttle"

  (set
   (!.dt
    (xt/x:obj-keys
     (throttle/throttle-create
      (fn [])
      nil))))
  => #{"handler" "queued" "now_fn" "active"})

^{:refer xt.event.util-throttle/throttle-run-async :added "4.1"}
(fact "runs a throttled handler once"

  (notify/wait-on :dart
    (var out [])
    (var handler
         (fn [i]
           (return
            (spec-promise/x:with-delay
             50
             (fn []
               (x:arr-push out i)
               (repl/notify out))))))
    (var instance (throttle/throttle-create handler nil))
    (throttle/throttle-run-async instance 1 nil))
  => [1])

^{:refer xt.event.util-throttle/throttle-run :added "4.1"}
(fact "queues a single rerun per id"

  (notify/wait-on :dart
    (var out [])
    (var queued nil)
    (var first-run nil)
    (var second-run nil)
    (var handler
         (fn [i]
           (return
            (spec-promise/x:with-delay
             100
             (fn []
               (x:arr-push out i)
               (when (== 2 (xt/x:len out))
                 (repl/notify {"same_promise" (== (. first-run ["promise"])
                                                  (. second-run ["promise"]))
                               "queued" queued
                               "runs" out})))))))
    (var instance (throttle/throttle-create handler nil))
    (:= first-run (throttle/throttle-run instance 1 nil))
    (:= second-run (throttle/throttle-run instance 1 nil))
    (throttle/throttle-run instance 1 nil)
    (throttle/throttle-run instance 1 nil)
    (:= queued (throttle/throttle-queued instance)))
  => {"same_promise" true
      "queued" ["1"]
      "runs" [1 1]})

^{:refer xt.event.util-throttle/throttle-waiting :added "4.1"}
(fact "returns the union of active and queued ids"

  (set
   (!.dt
    (throttle/throttle-waiting
     {"active" {"1" {} "2" {}}
      "queued" {"2" {} "3" {}}})))
  => #{"1" "2" "3"})

^{:refer xt.event.util-throttle/throttle-active :added "4.1"}
(fact "reports active and waiting ids"

  (notify/wait-on :dart
    (var instance)
    (var handler
         (fn [i]
           (return
            (spec-promise/x:with-delay
             (:? (== i 1) 100 300)
             (fn []
               (when (== i 1)
                 (repl/notify [(throttle/throttle-active instance)
                               (throttle/throttle-waiting instance)])))))))
    (:= instance (throttle/throttle-create handler nil))
    (throttle/throttle-run instance 1 nil)
    (throttle/throttle-run instance 1 nil)
    (throttle/throttle-run instance 1 nil)
    (throttle/throttle-run instance 2 nil)
    (throttle/throttle-run instance 3 nil))
  => [["1" "2" "3"]
      ["1" "2" "3"]])

^{:refer xt.event.util-throttle/throttle-queued :added "4.1"}
(fact "returns only queued ids"

  (set
   (!.dt
    (throttle/throttle-queued
     {"queued" {"1" {} "3" {}}})))
  => #{"1" "3"})

(comment
  (s/snapto)
  (s/run '[xt.event.util-throttle])
  
  (s/seedgen-benchadd '[xt.event.util-throttle] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.util-throttle]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.util-throttle]  {:lang [:lua :python] :write true}))
