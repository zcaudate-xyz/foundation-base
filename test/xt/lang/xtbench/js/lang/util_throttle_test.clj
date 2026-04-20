(ns
 xtbench.js.lang.util-throttle-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :js
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
 (set (!.js (xt/x:obj-keys (throttle/throttle-create (fn []) nil))))
 =>
 #{"handler" "queued" "now_fn" "active"})

^{:refer xt.lang.util-throttle/throttle-run-async, :added "4.0"}
(fact
 "runs an async throttle"
 ^{:hidden true}
 (notify/wait-on
  :js
  (var out [])
  (var
   handler
   (fn
    [i]
    (var
     promise-fn
     (fn
      [resolve reject]
      (var
       timeout-fn
       (fn [] (x:arr-push out i) (resolve (repl/notify out))))
      (setTimeout timeout-fn 100)))
    (return (new Promise promise-fn))))
  (var throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run-async throttle 1))
 =>
 [1])

^{:refer xt.lang.util-throttle/throttle-run, :added "4.0"}
(fact
 "throttles a function so that it only runs a single thread"
 ^{:hidden true}
 (notify/wait-on
  :js
  (:= (!:G OUT) [])
  (var
   handler
   (fn
    [i]
    (var
     promise-fn
     (fn
      [resolve reject]
      (var
       timeout-fn
       (fn
        []
        (x:arr-push (!:G OUT) i)
        (resolve (repl/notify (!:G OUT)))))
      (setTimeout timeout-fn 100)))
    (return (new Promise promise-fn))))
  (var throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1))
 =>
 [1]
 (do (Thread/sleep 500) (!.js (!:G OUT)))
 =>
 [1 1])

^{:refer xt.lang.util-throttle/throttle-active, :added "4.0"}
(fact
 "gets the active ids in a throttle"
 ^{:hidden true}
 (notify/wait-on
  :js
  #'throttle
  (var
   handler
   (fn
    [i]
    (var
     promise-fn
     (fn
      [resolve reject]
      (var
       timeout-fn
       (fn
        []
        (resolve
         (repl/notify
          [(throttle/throttle-active throttle)
           (throttle/throttle-waiting throttle)]))))
      (setTimeout timeout-fn (:? (== i 1) 100 300))))
    (return (new Promise promise-fn))))
  (:= throttle (throttle/throttle-create handler nil))
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 1)
  (throttle/throttle-run throttle 2)
  (throttle/throttle-run throttle 3))
 =>
 [["1" "2" "3"] ["1" "2" "3"]])
