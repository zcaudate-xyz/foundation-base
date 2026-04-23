(ns xt.lang.util-throttle-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.lang.util-throttle :as throttle]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.lang.util-throttle :as throttle]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.lang.util-throttle :as throttle]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-throttle/throttle-create :added "4.0"}
(fact "creates a throttle"

  (set (!.js
        (xt/x:obj-keys
         (throttle/throttle-create
          (fn [])
          nil))))
  => #{"handler" "queued" "now_fn" "active"}

  (set (!.lua
        (xt/x:obj-keys
         (throttle/throttle-create
          (fn [])
          nil))))
  => #{"handler" "queued" "now_fn" "active"})

^{:refer xt.lang.util-throttle/throttle-run-async :added "4.0"}
(fact "runs an async throttle"
  (notify/wait-on :js
    (var out [])
    (var handler
         (fn [i]
           (var promise-fn
                (fn [resolve reject]
                  (var timeout-fn
                       (fn []
                         (x:arr-push out i)
                         (resolve (repl/notify out))))
                  (setTimeout timeout-fn 100)))
           (return (new Promise promise-fn))))
    (var throttle (throttle/throttle-create handler nil))
    (throttle/throttle-run-async throttle 1))
  => [1]

  (notify/wait-on :lua
    (var out [])
    (var handler
         (fn [i]
           (ngx.sleep 0.1)
           (x:arr-push out i)
           (repl/notify out)))
    (var throttle (throttle/throttle-create handler nil))
    (throttle/throttle-run-async throttle 1))
  => [1]

  (!.py
   (:= (!:G THROTTLE_OUT) [])
   (var handler
        (fn [i]
          (var delayed-fn
               (fn []
                 (x:arr-push (!:G THROTTLE_OUT) i)))
          (xt/x:with-delay delayed-fn 100)))
    (var throttle (throttle/throttle-create handler nil))
    (throttle/throttle-run-async throttle 1 nil))

  (do (Thread/sleep 200)
      (!.py (!:G THROTTLE_OUT)))
  => [1])

^{:refer xt.lang.util-throttle/throttle-run :added "4.0"}
(fact "throttles a function so that it only runs a single thread"
  ;;
  ;; JS
  ;;

  (notify/wait-on :js
    (:= (!:G OUT) [])
    (var handler
         (fn [i]
           (var promise-fn
                (fn [resolve reject]
                  (var timeout-fn
                       (fn []
                         (x:arr-push (!:G OUT) i)
                         (resolve (repl/notify (!:G OUT)))))
                  (setTimeout timeout-fn 100)))
           (return (new Promise promise-fn))))
    (var throttle (throttle/throttle-create handler nil))
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1))
  => [1]

  (do (Thread/sleep 500)
      (!.js (!:G OUT)))
  => [1 1]

  ;;
  ;; LUA
  ;;

  (notify/wait-on :lua
    (:= (!:G OUT) [])
    (var throttle)
    (var handler
         (fn [i]
           (ngx.sleep 0.1)
           (x:arr-push (!:G OUT) i)
           (repl/notify (!:G OUT))))
    (:= throttle (throttle/throttle-create handler nil))
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1))
  => [1]

  (do (Thread/sleep 500)
      (!.lua (!:G OUT)))
  => [1 1]

  (!.py
   (:= (!:G THROTTLE_OUT) [])
   (var handler
        (fn [i]
          (var delayed-fn
               (fn []
                 (x:arr-push (!:G THROTTLE_OUT) i)))
          (xt/x:with-delay delayed-fn 100)))
    (var throttle (throttle/throttle-create handler nil))
    (throttle/throttle-run throttle 1 nil)
   (throttle/throttle-run throttle 1 nil)
   (throttle/throttle-run throttle 1 nil)
   (throttle/throttle-run throttle 1 nil))

  (do (Thread/sleep 120)
      (!.py (!:G THROTTLE_OUT)))
  => [1 1]

  (do (Thread/sleep 500)
      (!.py (!:G THROTTLE_OUT)))
  => [1 1])

^{:refer xt.lang.util-throttle/throttle-waiting :added "4.0"}
(fact "gets all the waiting ids")

^{:refer xt.lang.util-throttle/throttle-active :added "4.0"}
(fact "gets the active ids in a throttle"
  (notify/wait-on :js
    (var throttle)
    (var handler
         (fn [i]
           (var promise-fn
                (fn [resolve reject]
                  (var timeout-fn
                       (fn []
                         (resolve (repl/notify
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
  => [["1" "2" "3"]
      ["1" "2" "3"]]


  (notify/wait-on :lua
    (var throttle)
    (var handler
         (fn [i]
           (ngx.sleep 0.1)
           (repl/notify [(throttle/throttle-active throttle)
                         (throttle/throttle-waiting throttle)])))
    (:= throttle (throttle/throttle-create handler nil))
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 2)
    (throttle/throttle-run throttle 3))
  => [[1 2 3]
      [1 2 3]]

  (!.py
   (var throttle)
   (var handler
        (fn [i]
          (var delayed-fn
               (fn [] nil))
          (xt/x:with-delay delayed-fn (:? (== i 1) 100 300))))
    (:= throttle (throttle/throttle-create handler nil))
    (:= (!:G THROTTLE_STATE) throttle)
   (throttle/throttle-run throttle 1 nil)
   (throttle/throttle-run throttle 1 nil)
   (throttle/throttle-run throttle 1 nil)
   (throttle/throttle-run throttle 2 nil)
   (throttle/throttle-run throttle 3 nil))

  (do (Thread/sleep 50)
      (let [state (!.py [(throttle/throttle-active (!:G THROTTLE_STATE))
                         (throttle/throttle-waiting (!:G THROTTLE_STATE))])]
        (and (vector? state)
             (= 2 (count state))
             (= (first state) (second state))
             (every? vector? state))))
  => true)

^{:refer xt.lang.util-throttle/throttle-queued :added "4.0"}
(fact "gets all the queued ids")
