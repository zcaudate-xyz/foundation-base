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
 (!.dt
  (var out [])
  (var
   throttle
   (throttle/throttle-create
    (fn
     [i]
     (return (xt/x:with-delay 100 (do (x:arr-push out i) out))))
    nil))
  (throttle/throttle-run-async throttle 1 nil))
 =>
 [1])

^{:refer xt.lang.util-throttle/throttle-run, :added "4.0"}
(fact
 "throttles a function so that it only runs a single thread"
 ^{:hidden true}
 (!.dt
  (var out [])
  (var
   throttle
   (throttle/throttle-create
    (fn
     [i]
     (return (xt/x:with-delay 100 (do (x:arr-push out i) out))))
    nil))
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (xt/x:with-delay 150 out))
 =>
 [1]
 (!.dt
  (var out [])
  (var
   throttle
   (throttle/throttle-create
    (fn
     [i]
     (return (xt/x:with-delay 100 (do (x:arr-push out i) out))))
    nil))
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (xt/x:with-delay 500 out))
 =>
 [1 1])

^{:refer xt.lang.util-throttle/throttle-active, :added "4.0"}
(fact
 "gets the active ids in a throttle"
 ^{:hidden true}
 (!.dt
  (var
   throttle
   (throttle/throttle-create
    (fn
     [i]
     (return (xt/x:with-delay 100 i)))
    nil))
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 1 nil)
  (throttle/throttle-run throttle 2 nil)
  (throttle/throttle-run throttle 3 nil)
  (xt/x:with-delay 50 [(throttle/throttle-active throttle)
                       (throttle/throttle-waiting throttle)]))
 =>
 [[1 2 3] [1 2 3]])
