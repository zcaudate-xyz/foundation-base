(ns xt.lang.base-thread-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-lib/thread-spawn :adopt true :added "4.0"}
(fact "spawns a thread"
  ^:hidden
  
  (notify/wait-on :js
    (k/thread-spawn (fn []
                      (repl/notify "hello"))))
  => "hello"
  
  (notify/wait-on :lua
    (k/thread-spawn (fn []
                       (repl/notify "hello"))))
  => "hello"

  (notify/wait-on :python
    (var thread := (k/thread-spawn (fn []
                                     (repl/notify "hello"))))
    true)
  
  => "hello")

^{:refer xt.lang.base-lib/with-delay :adopt true :added "4.0"}
(fact "creates a delayed promise"
  ^:hidden
  
  (notify/wait-on :js
    (k/with-delay (fn []
                    (repl/notify "hello")
                    true)
      100))
  => "hello"
  
  (notify/wait-on :lua
    (k/with-delay (fn []
                    (return (repl/notify "hello")))
      100))
  => "hello"

  (notify/wait-on :python
    (k/with-delay (fn []
                         (repl/notify "hello"))
      100))
  => "hello")
