(ns xt.lang.common-repl-test
  (:use code.test)
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
    :require [[xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-repl/return-callbacks :added "4.0"}
(fact "constructs return callbacks")

^{:refer xt.lang.common-repl/socket-send :added "4.0"}
(fact "sends a message via the socket"

  (notify/wait-on-call
   (fn []
     (!.lua
      (k/socket-connect
       "127.0.0.1"
       (@! (:socket-port (l/default-notify)))
       {:success (fn [conn]
                   (k/socket-send conn
                                  (x:cat (k/return-encode "hello"
                                                          (@! notify/*override-id*)
                                                          "hello")
                                         "\n"))
                   (k/socket-close conn))}))))
  => "hello"

  (notify/wait-on-call
   (fn []
     (!.js
      (k/socket-connect
       "127.0.0.1"
       (@! (:socket-port (l/default-notify)))
       {:success (fn [conn]
                   (k/socket-send conn
                                  (x:cat (k/return-encode "hello"
                                                          (@! notify/*override-id*)
                                                          "hello")
                                         "\n"))
                   (k/socket-close conn))}))))
  => "hello"

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                (k/socket-connect
                 "127.0.0.1"
                 (@! (:socket-port (l/default-notify)))
                  {:success (fn [conn]
                              (k/socket-send conn
                                             (x:cat (k/return-encode "hello"
                                                                     (@! notify/*override-id*)
                                                                     "hello")
                                                    "\n"))
                              (k/socket-close conn))}))))}}}
  (notify/wait-on-call
   (fn []
     (!.py
      (k/socket-connect
       "127.0.0.1"
       (@! (:socket-port (l/default-notify)))
       {:success (fn [conn]
                   (k/socket-send conn
                                  (x:cat (k/return-encode "hello"
                                                          (@! notify/*override-id*)
                                                          "hello")
                                         "\n"))
                   (k/socket-close conn))}))))
  => "hello")

^{:refer xt.lang.common-repl/socket-close :added "4.0"}
(fact "closes the socket")

^{:refer xt.lang.common-repl/socket-connect-base :added "4.0"}
(fact "base connect call")

^{:refer xt.lang.common-repl/socket-connect :added "4.0"
  :setup [(l/rt:restart)]}
(fact "connects a a socket to port"

  (notify/wait-on :js
    (k/socket-connect
     "127.0.0.1"
     (@! (:socket-port (l/default-notify)))
     {:success (fn [conn]
                 (k/notify "OK")
                 (k/socket-close conn))}))
  => "OK"

  (notify/wait-on :lua
   (k/socket-connect
    "127.0.0.1"
    (@! (:socket-port (l/default-notify)))
    {:success (fn [conn]
                (k/notify "OK")
                (k/socket-close conn))}))
  => "OK"

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                (k/socket-connect
                 "127.0.0.1"
                 (@! (:socket-port (l/default-notify)))
                 {:success (fn [conn]
                             (k/socket-send
                              conn
                              (x:cat (k/return-encode "OK"
                                                      (@! notify/*override-id*)
                                                      nil)
                                     "\n"))
                             (k/socket-close conn))}))))}}}
  (notify/wait-on :python
    (k/socket-connect
     "127.0.0.1"
     (@! (:socket-port (l/default-notify)))
     {:success (fn [conn]
                 (k/notify "OK")
                 (k/socket-close conn))}))
  => "OK")

^{:refer xt.lang.common-repl/notify-socket-handler :added "4.0"}
(fact "helper function for `notify-socket`")

^{:refer xt.lang.common-repl/notify-socket :added "4.0"}
(fact "notifies the socket of a value"

  (notify/wait-on-call
   (fn [] (!.js
           (k/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
                            "hello"
                            (@! notify/*override-id*)
                            nil
                            {}))))
  => "hello"

  (notify/wait-on-call
   (fn [] (!.lua
           (k/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
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
                (k/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
                                 "hello"
                                 (@! notify/*override-id*)
                                 nil
                                 {}))))}}}
  (notify/wait-on-call
   (fn [] (!.py
           (k/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
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
           (k/notify-socket-http
            "127.0.0.1" (@! (:http-port (l/default-notify)))
            "hello"
            (@! notify/*override-id*)
            nil
            {}))))
  => "hello"

  (notify/wait-on-call
   (fn [] (!.lua
           (k/notify-socket-http
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
                (k/notify-socket-http
                 "127.0.0.1" (@! (:http-port (l/default-notify)))
                 "hello"
                 (@! notify/*override-id*)
                 nil
                 {}))))}}}
  (notify/wait-on-call
   (fn [] (!.py
           (k/notify-socket-http
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
            (k/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
                           "hello"
                           (@! notify/*override-id*)
                           nil
                           {}))))
  => "hello"

  (notify/wait-on-call
   (fn [] (!.lua
           (k/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
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
                (k/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
                               "hello"
                               (@! notify/*override-id*)
                               nil
                               {}))))}}}
  (notify/wait-on-call
   (fn []
     (!.py
      (k/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
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
    (k/notify 1))
  => 1

  (notify/wait-on :lua
    (k/notify 1))
  => 1

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                (k/notify 1))))}}}
  (notify/wait-on :python
    (k/notify 1))
  => 1)

^{:refer xt.lang.common-repl/>notify :added "4.0"}
(fact "creates a callback function")

^{:refer xt.lang.common-repl/<! :added "4.0"}
(fact "creates a callback map"

  (notify/wait-on :js
    ((. (k/<!)
       ["success"]) 1))
  => 1

  (notify/wait-on :lua
   ((. (k/<!)
       ["success"]) 1))
  => 1

  ^{:lang-exceptions
    {:dart
     {:form (notify/wait-on-call
             2000
             (fn []
               (!.dt
                ((. (k/<!)
                    ["success"]) 1))))}}}
  (notify/wait-on :python
   ((. (k/<!)
       ["success"]) 1))
  => 1)
