(ns
 xtbench.dart.lang.util-throttle-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-spec :as xt]
   [xt.lang.util-throttle :as throttle]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-throttle/throttle-create, :added "4.0"}
(fact
 "creates a throttle"
 ^{:hidden true}
 (set (!.dt (xt/x:obj-keys (throttle/throttle-create (fn []) nil))))
 =>
 #{"handler" "queued" "now_fn" "active"})

^{:refer xt.lang.util-throttle/throttle-run-async, :added "4.0"}
(fact
 "runs an async throttle"
 ^{:hidden true}
 (do (Thread/sleep 200) (!.dt (!:G THROTTLE_OUT)))
 =>
 [1])

^{:refer xt.lang.util-throttle/throttle-run, :added "4.0"}
(fact
 "throttles a function so that it only runs a single thread"
 ^{:hidden true}
 (do (Thread/sleep 120) (!.dt (!:G THROTTLE_OUT)))
 =>
 [1 1]
 (do (Thread/sleep 500) (!.dt (!:G THROTTLE_OUT)))
 =>
 [1 1])

^{:refer xt.lang.util-throttle/throttle-active, :added "4.0"}
(fact
 "gets the active ids in a throttle"
 ^{:hidden true}
 (do
  (Thread/sleep 50)
  (let
   [state
    (!.dt
     [(throttle/throttle-active (!:G THROTTLE_STATE))
      (throttle/throttle-waiting (!:G THROTTLE_STATE))])]
   (and
    (vector? state)
    (= 2 (count state))
    (= (first state) (second state))
    (every? vector? state))))
 =>
 true)
