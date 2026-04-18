(ns xt.lang.util-throttle-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.util-throttle :as throttle]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.util-throttle :as throttle]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.util-throttle :as throttle]
             [xt.lang.common-repl :as repl]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.util-throttle :as throttle]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-throttle/throttle-create :added "4.0"}
(fact "creates a throttle"
  ^:hidden
  
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
  ^:hidden
  
  (notify/wait-on :js
    (var out [])
    (var throttle (throttle/throttle-create
                   (fn [i]
                     (return (new Promise
                                  (fn [resolve reject]
                                    (setTimeout (fn []
                                                  (x:arr-push out i)
                                                  (resolve (repl/notify out)))
                                                100)))))
                   nil))
    (throttle/throttle-run-async throttle 1))
  => [1]
  
  (notify/wait-on :lua
    (var out [])
    (var throttle (throttle/throttle-create
                   (fn [i]
                     (ngx.sleep 0.1)
                     (x:arr-push out i)
                     (repl/notify out))
                   nil))
    (throttle/throttle-run-async throttle 1))
  => [1])

^{:refer xt.lang.util-throttle/throttle-run :added "4.0"}
(fact "throttles a function so that it only runs a single thread"
  ^:hidden
  
  ;;
  ;; JS
  ;;

  (notify/wait-on :js
    (:= (!:G OUT) [])
    (var throttle (throttle/throttle-create
                   (fn [i]
                     (return (new Promise
                                  (fn [resolve reject]
                                    (setTimeout (fn []
                                                  (x:arr-push (!:G OUT) i)
                                                  (resolve (repl/notify (!:G OUT))))
                                                100)))))
                   nil))
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
    (:= throttle (throttle/throttle-create
                  (fn [i]
                    (ngx.sleep 0.1)
                    (x:arr-push (!:G OUT) i)
                    (repl/notify (!:G OUT)))
                  nil))
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1))
  => [1]
  
  (do (Thread/sleep 500)
      (!.lua (!:G OUT)))
  => [1 1])

^{:refer xt.lang.util-throttle/throttle-waiting :added "4.0"}
(fact "gets all the waiting ids")

^{:refer xt.lang.util-throttle/throttle-active :added "4.0"}
(fact "gets the active ids in a throttle"
  ^:hidden

  (notify/wait-on :js
    (var throttle (throttle/throttle-create
                   (fn [i]
                     (return (new Promise
                                  (fn [resolve reject]
                                    (setTimeout (fn []
                                                  (resolve (repl/notify
                                                            [(throttle/throttle-active throttle)
                                                             (throttle/throttle-waiting throttle)])))
                                                100)))))
                   nil))
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 2)
    (throttle/throttle-run throttle 3))
  => [["1" "2" "3"]
      ["1" "2" "3"]]
  

  (notify/wait-on :lua
    (var throttle)
    (:= throttle (throttle/throttle-create
                  (fn [i]
                    (ngx.sleep 0.1)
                    (repl/notify [(throttle/throttle-active throttle)
                                  (throttle/throttle-waiting throttle)]))
                  nil))
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 1)
    (throttle/throttle-run throttle 2)
    (throttle/throttle-run throttle 3))
  => [[1 2 3]
      [1 2 3]])

^{:refer xt.lang.util-throttle/throttle-queued :added "4.0"}
(fact "gets all the queued ids")
