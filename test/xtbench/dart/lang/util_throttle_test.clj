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
   [[xt.lang.common-spec :as xt]
    [xt.lang.common-repl :as repl]
    [xt.lang.util-throttle :as throttle]]})

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
 (notify/wait-on
  :dart
  (var out [])
  (var
   handler
   (fn
    [i]
    (var
     delayed-fn
     (fn [] (x:arr-push out i) (repl/notify out)))
    (xt/x:with-delay delayed-fn 100)))
  (var throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run-async throttle 1 nil))
  =>
  [1])

^{:refer xt.lang.util-throttle/throttle-run, :added "4.0"}
(fact
 "throttles a function so that it only runs a single thread"
 (notify/wait-on
  :dart
  (var out [])
  (var
   handler
   (fn
    [i]
    (var
     delayed-fn
     (fn
      []
      (x:arr-push out i)
      (when (== 1 (xt/x:len out)) (repl/notify out))))
    (xt/x:with-delay delayed-fn 100)))
  (var throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil))
  =>
  [1]
 (notify/wait-on
  :dart
  (var out [])
  (var
   handler
   (fn
    [i]
    (var
     delayed-fn
     (fn
      []
      (x:arr-push out i)
      (when (== 2 (xt/x:len out)) (repl/notify out))))
    (xt/x:with-delay delayed-fn 100)))
  (var throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil))
  =>
  [1 1])

^{:refer xt.lang.util-throttle/throttle-active, :added "4.0"}
(fact
 "gets the active ids in a throttle"
 (let
  [state
   (notify/wait-on
    :dart
    (var throttle)
    (var
     handler
     (fn
      [i]
      (var delayed-fn (fn [] nil))
      (xt/x:with-delay delayed-fn (:? (== i 1) 100 300))))
    (:= throttle (throttle/throttle-create handler nil))
    (throttle/throttle-run throttle 1 nil)
    (throttle/throttle-run throttle 1 nil)
    (throttle/throttle-run throttle 1 nil)
    (throttle/throttle-run throttle 2 nil)
    (throttle/throttle-run throttle 3 nil)
    (xt/x:with-delay
     (fn
      []
      (repl/notify
       [(throttle/throttle-active throttle)
        (throttle/throttle-waiting throttle)]))
     50))]
  (and
   (vector? state)
   (= 2 (count state))
   (= (first state) (second state))
   (every? vector? state)))
  =>
  true)
