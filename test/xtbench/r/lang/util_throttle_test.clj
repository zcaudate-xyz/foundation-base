(ns
 xtbench.r.lang.util-throttle-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-throttle/throttle-create, :added "4.0"}
(fact
 "creates a throttle"
 ^{:hidden true}
 (set (!.R (xt/x:obj-keys (throttle/throttle-create (fn []) nil))))
 =>
 #{"handler" "queued" "now_fn" "active"})

^{:refer xt.lang.util-throttle/throttle-run-async, :added "4.0"}
(fact
 "runs an async throttle"
 ^{:hidden true}
 (notify/wait-on
  :r
  (var out [])
  (var
   throttle
   (throttle/throttle-create
    (fn
     [i]
     (return
      (new
       Promise
       (fn
        [resolve reject]
        (setTimeout
         (fn [] (x:arr-push out i) (resolve (repl/notify out)))
         100)))))
    nil))
  (throttle/throttle-run-async throttle 1))
 =>
 [1])

^{:refer xt.lang.util-throttle/throttle-run, :added "4.0"}
(fact
 "throttles a function so that it only runs a single thread"
 ^{:hidden true}
 (notify/wait-on
  :r
  (:= (!:G OUT) [])
  (var
   throttle
   (throttle/throttle-create
    (fn
     [i]
     (return
      (new
       Promise
       (fn
        [resolve reject]
        (setTimeout
         (fn
          []
          (x:arr-push (!:G OUT) i)
          (resolve (repl/notify (!:G OUT))))
         100)))))
    nil))
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1))
 =>
 [1]
 (do (Thread/sleep 500) (!.R (!:G OUT)))
 =>
 [1 1])

^{:refer xt.lang.util-throttle/throttle-active, :added "4.0"}
(fact
 "gets the active ids in a throttle"
 ^{:hidden true}
 (notify/wait-on
  :r
  (var
   throttle
   (throttle/throttle-create
    (fn
     [i]
     (return
      (new
       Promise
       (fn
        [resolve reject]
        (setTimeout
         (fn
          []
          (resolve
           (repl/notify
            [(throttle/throttle-active throttle)
             (throttle/throttle-waiting throttle)])))
         100)))))
    nil))
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 2)
  (throttle/throttle-run throttle 3))
 =>
 [["1" "2" "3"] ["1" "2" "3"]])
