(ns
 xtbench.lua.lang.util-throttle-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :config {:program :resty},
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.spec-base :as xt]
   [xt.lang.util-throttle :as throttle]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-throttle/throttle-create, :added "4.0"}
(fact
 "creates a throttle"
 ^{:hidden true}
 (set (!.lua (xt/x:obj-keys (throttle/throttle-create (fn []) nil))))
 =>
 #{"handler" "queued" "now_fn" "active"})

^{:refer xt.lang.util-throttle/throttle-run-async, :added "4.0"}
(fact
 "runs an async throttle"
 ^{:hidden true}
 (notify/wait-on
  :lua
  (var out [])
  (var
   handler
   (fn [i] (ngx.sleep 0.1) (x:arr-push out i) (repl/notify out)))
  (var throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run-async throttle 1))
 =>
 [1])

^{:refer xt.lang.util-throttle/throttle-run, :added "4.0"}
(fact
 "throttles a function so that it only runs a single thread"
 ^{:hidden true}
 (notify/wait-on
  :lua
  (:= (!:G OUT) [])
  #'throttle
  (var
   handler
   (fn
    [i]
    (ngx.sleep 0.1)
    (x:arr-push (!:G OUT) i)
    (repl/notify (!:G OUT))))
  (:= throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1))
 =>
 [1]
 (do (Thread/sleep 500) (!.lua (!:G OUT)))
 =>
 [1 1])

^{:refer xt.lang.util-throttle/throttle-active, :added "4.0"}
(fact
 "gets the active ids in a throttle"
 ^{:hidden true}
 (notify/wait-on
  :lua
  #'throttle
  (var
   handler
   (fn
    [i]
    (ngx.sleep 0.1)
    (repl/notify
     [(throttle/throttle-active throttle)
      (throttle/throttle-waiting throttle)])))
  (:= throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 2)
  (throttle/throttle-run throttle 3))
 =>
 [[1 2 3] [1 2 3]])
