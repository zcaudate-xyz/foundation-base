(ns
 xtbench.python.lang.util-throttle-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
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
 (set (!.py (xt/x:obj-keys (throttle/throttle-create (fn []) nil))))
 =>
 #{"handler" "queued" "now_fn" "active"})

^{:refer xt.lang.util-throttle/throttle-run-async, :added "4.0"}
(fact
 "runs an async throttle"
 (!.py
  (:= (!:G THROTTLE_OUT) [])
  (var
   handler
   (fn
    [i]
    (var delayed-fn (fn [] (x:arr-push (!:G THROTTLE_OUT) i)))
    (xt/x:with-delay delayed-fn 100)))
  (var throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run-async throttle 1 nil))
 ^{:hidden true}
 (do (Thread/sleep 200) (!.py (!:G THROTTLE_OUT)))
 =>
 [1])

^{:refer xt.lang.util-throttle/throttle-run, :added "4.0"}
(fact
 "throttles a function so that it only runs a single thread"
 (!.py
  (:= (!:G THROTTLE_OUT) [])
  (var
   handler
   (fn
    [i]
    (var delayed-fn (fn [] (x:arr-push (!:G THROTTLE_OUT) i)))
    (xt/x:with-delay delayed-fn 100)))
  (var throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil))
 ^{:hidden true}
 (do (Thread/sleep 120) (!.py (!:G THROTTLE_OUT)))
 =>
 [1 1]
 (do (Thread/sleep 500) (!.py (!:G THROTTLE_OUT)))
 =>
 [1 1])

^{:refer xt.lang.util-throttle/throttle-active, :added "4.0"}
(fact
 "gets the active ids in a throttle"
 (!.py
  #'throttle
  (var
   handler
   (fn
    [i]
    (var delayed-fn (fn [] nil))
    (xt/x:with-delay delayed-fn (:? (== i 1) 100 300))))
  (:= throttle (throttle/throttle-create handler nil))
  (:= (!:G THROTTLE_STATE) throttle)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 2 nil)
  (throttle/throttle-run throttle 3 nil))
 ^{:hidden true}
 (do
  (Thread/sleep 50)
  (let
   [state
    (!.py
     [(throttle/throttle-active (!:G THROTTLE_STATE))
      (throttle/throttle-waiting (!:G THROTTLE_STATE))])]
   (and
    (vector? state)
    (= 2 (count state))
    (= (first state) (second state))
    (every? vector? state))))
 =>
 true)
