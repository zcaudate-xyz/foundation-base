(ns xt.lang.common-repl-test
  (:use code.test)
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as xtl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as xtl]]})

(l/script- :python
  {:runtime :basic
    :require [[xt.lang.common-repl :as repl]
              [xt.lang.common-lib :as xtl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-repl/socket-send :added "4.0"}
(fact "sends a message via the socket"
  
  ^{:seedgen/base             {}}
  (notify/wait-on-call
   (fn []
     (!.js
       (repl/socket-connect
        "127.0.0.1"
        (@! (:socket-port (l/default-notify)))
        {:success (fn [conn]
                    (repl/notify-socket-handler conn
                                                (xtl/return-encode "hello"
                                                                   (@! notify/*override-id*)
                                                                   "hello")))}))))
  => "hello")

^{:refer xt.lang.common-repl/socket-close :added "4.0"}
(fact "closes the socket")

^{:refer xt.lang.common-repl/socket-connect-base :added "4.0"}
(fact "base connect call")

^{:refer xt.lang.common-repl/socket-connect :added "4.0"
  :setup [(l/rt:restart)]}
(fact "connects a a socket to port"

  (notify/wait-on :js
    (repl/socket-connect
     "127.0.0.1"
     (@! (:socket-port (l/default-notify)))
     {:success (fn [conn]
                 (repl/notify "OK"))}))
  => "OK")

^{:refer xt.lang.common-repl/notify-socket-handler :added "4.0"}
(fact "helper function for `notify-socket`"

  (notify/wait-on :js
    (repl/socket-connect
     "127.0.0.1"
     (@! (:socket-port (l/default-notify)))
     {:success (fn [conn]
                 (repl/notify-socket-handler conn
                                             "OK"))})))

^{:refer xt.lang.common-repl/notify-socket :added "4.0"}
(fact "notifies the socket of a value"

  (notify/wait-on-call
   (fn [] (!.js
           (repl/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
                            "hello"
                            (@! notify/*override-id*)
                            nil
                            {}))))
  => "hello"

  (notify/wait-on-call
   (fn [] (!.lua
           (repl/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
                            "hello"
                            (@! notify/*override-id*)
                            nil
                            {}))))
  => "hello"

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                (repl/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
                                 "hello"
                                 (@! notify/*override-id*)
                                 nil
                                 {}))))}}}
  (notify/wait-on-call
   (fn [] (!.py
           (repl/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
                             "hello"
                            (@! notify/*override-id*)
                            nil
                            {}))))
  => "hello")

^{:refer xt.lang.common-repl/notify-socket-http-handler :added "4.0"}
(fact "helper function for `notify-socket-http`")

^{:refer xt.lang.common-repl/notify-socket-http :added "4.0"}
(fact "using the base socket implementation to notify on http protocol"

  (notify/wait-on-call
   (fn [] (!.js
           (repl/notify-socket-http
            "127.0.0.1" (@! (:http-port (l/default-notify)))
            "hello"
            (@! notify/*override-id*)
            nil
            {}))))
  => "hello"

  (notify/wait-on-call
   (fn [] (!.lua
           (repl/notify-socket-http
            "127.0.0.1" (@! (:http-port (l/default-notify)))
            "hello"
            (@! notify/*override-id*)
            nil
            {}))))
  => "hello"

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                (repl/notify-socket-http
                 "127.0.0.1" (@! (:http-port (l/default-notify)))
                 "hello"
                 (@! notify/*override-id*)
                 nil
                 {}))))}}}
  (notify/wait-on-call
   (fn [] (!.py
           (repl/notify-socket-http
            "127.0.0.1" (@! (:http-port (l/default-notify)))
            "hello"
            (@! notify/*override-id*)
            nil
            {}))))
  => "hello")

^{:refer xt.lang.common-repl/notify-http :added "4.0"}
(fact "call a http notify function."

  (notify/wait-on-call
   (fn [] (!.js
            (:= (!:G fetch) (. (require "node-fetch") default))
            (repl/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
                           "hello"
                           (@! notify/*override-id*)
                           nil
                           {}))))
  => "hello"

  (notify/wait-on-call
   (fn [] (!.lua
           (repl/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
                          "hello"
                          (@! notify/*override-id*)
                          nil
                          {}))))
  => "hello"

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                (repl/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
                               "hello"
                               (@! notify/*override-id*)
                               nil
                               {}))))}}}
  (notify/wait-on-call
   (fn []
     (!.py
      (repl/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
                     "hello"
                     (@! notify/*override-id*)
                     nil
                     {}))))
  => "hello")

^{:refer xt.lang.common-repl/notify-form :added "4.0"}
(fact "creates the notify form")

^{:refer xt.lang.common-repl/print :added "4.0"}
(fact "creates the print op")

^{:refer xt.lang.common-repl/capture :added "4.0"}
(fact "creats the capture op")

^{:refer xt.lang.common-repl/notify :added "4.0"}
(fact "sends a message to the notify server"

  (notify/wait-on :js
    (repl/notify 1))
  => 1

  (notify/wait-on :lua
    (repl/notify 1))
  => 1

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                (repl/notify 1))))}}}
  (notify/wait-on :python
    (repl/notify 1))
  => 1)

^{:refer xt.lang.common-repl/>notify :added "4.0"}
(fact "creates a callback function")

^{:refer xt.lang.common-repl/<! :added "4.0"}
(fact "creates a callback map"

  (notify/wait-on :js
    ((. (repl/<!)
       ["success"]) 1))
  => 1

  (notify/wait-on :lua
   ((. (repl/<!)
       ["success"]) 1))
  => 1

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                ((. (repl/<!)
                    ["success"]) 1))))}}}
  (notify/wait-on :python
   ((. (repl/<!)
       ["success"]) 1))
  => 1)
