(ns xtbench.dart.lang.common-repl-test
  (:use code.test)
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-lib :as xtl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-repl/socket-connect :added "4.0"
  :setup [(l/rt:restart)]}
(fact "connects a a socket to port"

  (notify/wait-on :dart
    (repl/socket-connect
     "127.0.0.1"
     (@! (:socket-port (l/default-notify)))
     {:success (fn [conn]
                 (repl/notify "OK"))}))
  => "OK")

^{:refer xt.lang.common-repl/notify-socket-handler :added "4.0"}
(fact "helper function for `notify-socket`"

  (notify/wait-on-call
   (fn []
     (!.dt
       (repl/socket-connect
        "127.0.0.1"
        (@! (:socket-port (l/default-notify)))
        {:success (fn [conn]
                    (repl/notify-socket-handler conn
                                                (xtl/return-encode "hello"
                                                                   (@! notify/*override-id*)
                                                                   "hello")))}))))
  => "hello")

^{:refer xt.lang.common-repl/notify-socket :added "4.0"}
(fact "notifies the socket of a value"

  (notify/wait-on-call
   (fn [] (!.dt
           (repl/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
                            "hello"
                            (@! notify/*override-id*)
                            nil
                            {}))))
  => "hello")

^{:refer xt.lang.common-repl/notify-socket-http :added "4.0"}
(fact "using the base socket implementation to notify on http protocol"

  (notify/wait-on-call
   (fn [] (!.dt
           (repl/notify-socket-http
            "127.0.0.1" (@! (:http-port (l/default-notify)))
            "hello"
            (@! notify/*override-id*)
            nil
            {}))))
  => "hello")

^{:refer xt.lang.common-repl/notify-http :added "4.0"
  :setup [(!.dt
  (:= (!:G fetch)
      (. (require "node-fetch") default)))]}
(fact "call a http notify function."

  (notify/wait-on-call
   (fn [] (!.dt
            (repl/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
                              "hello"
                              (@! notify/*override-id*)
                              nil
                              {}))))
  => "hello")

^{:refer xt.lang.common-repl/notify :added "4.0"}
(fact "sends a message to the notify server"

  (notify/wait-on :dart
    (repl/notify 1))
  => 1)

^{:refer xt.lang.common-repl/<! :added "4.0"}
(fact "creates a callback map"

  (notify/wait-on :dart
    ((. (repl/<!)
       ["success"]) 1))
  => 1)

(comment
  (s/seedgen-langadd 'xt.lang.common-repl {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-repl {:lang [:lua :python] :write true}))
