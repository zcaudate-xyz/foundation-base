(ns
 xtbench.lua.lang.common-repl-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic, :require [[xt.lang.common-repl :as k]]})

(defn decode-output [x] (if (string? x) (json/read x) x))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-repl/return-encode, :added "4.0"}
(fact
 "returns the encoded "
 ^{:hidden true}
 (decode-output
  (!.lua (k/return-encode {:data [1 2 3]} "<id>" "<key>")))
 =>
 {"key" "<key>", "id" "<id>", "value" {"data" [1 2 3]}, "type" "data"})

^{:refer xt.lang.common-repl/return-wrap, :added "4.0"}
(fact
 "returns a wrapped call"
 ^{:hidden true}
 (decode-output (!.lua (k/return-wrap (fn:> 1))))
 =>
 {"value" 1, "type" "data"})

^{:refer xt.lang.common-repl/return-eval, :added "4.0"}
(fact
 "evaluates a returns a string"
 ^{:hidden true}
 (decode-output (!.lua (k/return-eval "return 1")))
 =>
 {"value" 1, "type" "data"})

^{:refer xt.lang.common-repl/socket-send, :added "4.0"}
(fact
 "sends a message via the socket"
 ^{:hidden true}
 (notify/wait-on-call
  (fn
   []
   (!.lua
    (k/socket-connect
     "127.0.0.1"
     (@! (:socket-port (l/default-notify)))
     {:success
      (fn
       [conn]
       (k/socket-send
        conn
        (x:cat
         (k/return-encode "hello" (@! notify/*override-id*) "hello")
         "\n"))
       (k/socket-close conn))}))))
 =>
 "hello")

^{:refer xt.lang.common-repl/socket-connect,
  :added "4.0",
  :setup [(l/rt:restart)]}
(fact
 "connects a a socket to port"
 ^{:hidden true}
 (notify/wait-on
  :lua
  (k/socket-connect
   "127.0.0.1"
   (@! (:socket-port (l/default-notify)))
   {:success (fn [conn] (k/notify "OK") (k/socket-close conn))}))
 =>
 "OK")

^{:refer xt.lang.common-repl/notify-socket, :added "4.0"}
(fact
 "notifies the socket of a value"
 ^{:hidden true}
 (notify/wait-on-call
  (fn
   []
   (!.lua
    (k/notify-socket
     "127.0.0.1"
     (@! (:socket-port (l/default-notify)))
     "hello"
     (@! notify/*override-id*)
     nil
     {}))))
 =>
 "hello")

^{:refer xt.lang.common-repl/notify-socket-http, :added "4.0"}
(fact
 "using the base socket implementation to notify on http protocol"
 ^{:hidden true}
 (notify/wait-on-call
  (fn
   []
   (!.lua
    (k/notify-socket-http
     "127.0.0.1"
     (@! (:http-port (l/default-notify)))
     "hello"
     (@! notify/*override-id*)
     nil
     {}))))
 =>
 "hello")

^{:refer xt.lang.common-repl/notify-http, :added "4.0"}
(fact
 "call a http notify function."
 ^{:hidden true}
 (notify/wait-on-call
  (fn
   []
   (!.lua
    (k/notify-http
     "127.0.0.1"
     (@! (:http-port (l/default-notify)))
     "hello"
     (@! notify/*override-id*)
     nil
     {}))))
 =>
 "hello")

^{:refer xt.lang.common-repl/notify, :added "4.0"}
(fact
 "sends a message to the notify server"
 ^{:hidden true}
 (notify/wait-on :lua (k/notify 1))
 =>
 1)

^{:refer xt.lang.common-repl/<!, :added "4.0"}
(fact
 "creates a callback map"
 ^{:hidden true}
 (notify/wait-on :lua ((. (k/<!) ["success"]) 1))
 =>
 1)
