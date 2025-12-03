(ns xt.lang.base-repl-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.json :as json]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-repl :as k]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-repl :as k]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.base-repl :as k]]})

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.base-repl :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-repl/return-encode :added "4.0"}
(fact "returns the encoded "
  ^:hidden

  (json/read
   (!.js
    (k/return-encode {:data [1 2 3]} "<id>" "<key>")))
  => { "key" "<key>", "id" "<id>", "value" {"data" [1 2 3]}, "type" "data"}
  
  (json/read
   (!.lua
    (k/return-encode {:data [1 2 3]} "<id>" "<key>")))
  => {"key" "<key>", "id" "<id>", "value" {"data" [1 2 3]}, "type" "data"}

  (json/read
   (!.py
    (k/return-encode {:data [1 2 3]} "<id>" "<key>")))
  => {"key" "<key>", "id" "<id>", "value" {"data" [1 2 3]}, "type" "data"}

  (json/read
   (!.R
    (k/return-encode {:data [1 2 3]} "<id>" "<key>")))
  => {"key" "<key>", "id" "<id>", "value" {"data" [1 2 3]}, "type" "data"})

^{:refer xt.lang.base-repl/return-wrap :added "4.0"}
(fact "returns a wrapped call"
  ^:hidden
  
  (json/read
   (!.js
    (k/return-wrap (fn:> 1))))
  => {"value" 1, "type" "data", "return" "number"}
  
  (json/read
   (!.lua
    (k/return-wrap (fn:> 1))))
  => {"value" 1, "type" "data"}

  (json/read
   (!.py
    (k/return-wrap (fn:> 1))))
  => {"key" nil, "id" nil, "value" 1, "type" "data"}

  (json/read
   (!.R
    (k/return-wrap (fn:> 1))))
  => {"key" nil, "id" nil, "value" 1, "type" "data"})

^{:refer xt.lang.base-repl/return-eval :added "4.0"}
(fact "evaluates a returns a string"
  ^:hidden

  (json/read
   (!.js
    (k/return-eval "1")))
  => {"return" "number", "value" 1, "type" "data"}

  (json/read
   (!.lua
    (k/return-eval "return 1")))
  => {"value" 1, "type" "data"}

  (json/read
   (!.py
    (k/return-eval "globals()[\"OUT\"] = 1")))
  => {"key" nil, "id" nil, "value" 1, "type" "data"}

  (json/read
   (!.R
    (k/return-eval "1")))
  => {"key" nil, "id" nil, "value" 1, "type" "data"})

^{:refer xt.lang.base-repl/return-callbacks :added "4.0"}
(fact "constructs return callbacks")

^{:refer xt.lang.base-repl/socket-send :added "4.0"}
(fact "sends a message via the socket"
  ^:hidden
  
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

^{:refer xt.lang.base-repl/socket-close :added "4.0"}
(fact "closes the socket")

^{:refer xt.lang.base-repl/socket-connect-base :added "4.0"}
(fact "base connect call")

^{:refer xt.lang.base-repl/socket-connect :added "4.0"
  :setup [(l/rt:restart)]}
(fact "connects a a socket to port"
  ^:hidden

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

  (notify/wait-on :python
   (k/socket-connect
    "127.0.0.1"
    (@! (:socket-port (l/default-notify)))
    {:success (fn [conn]
                (k/notify "OK")
                (k/socket-close conn))}))
  => "OK")

^{:refer xt.lang.base-repl/notify-socket-handler :added "4.0"}
(fact "helper function for `notify-socket`")

^{:refer xt.lang.base-repl/notify-socket :added "4.0"}
(fact "notifies the socket of a value"
  ^:hidden
  
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

  (notify/wait-on-call
   (fn [] (!.py
           (k/notify-socket "127.0.0.1" (@! (:socket-port (l/default-notify)))
                            "hello"
                            (@! notify/*override-id*)
                            nil
                            {}))))
  => "hello")

^{:refer xt.lang.base-repl/notify-socket-http-handler :added "4.0"}
(fact "helper function for `notify-socket-http`")

^{:refer xt.lang.base-repl/notify-socket-http :added "4.0"}
(fact "using the base socket implementation to notify on http protocol"
  ^:hidden
  
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

  (notify/wait-on-call
   (fn [] (!.py
           (k/notify-socket-http
            "127.0.0.1" (@! (:http-port (l/default-notify)))
            "hello"
            (@! notify/*override-id*)
            nil
            {}))))
  => "hello")

^{:refer xt.lang.base-repl/notify-http :added "4.0"}
(fact "call a http notify function."
  ^:hidden

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

  (notify/wait-on-call
   (fn []
     (!.py
      (k/notify-http "127.0.0.1" (@! (:http-port (l/default-notify)))
                     "hello"
                     (@! notify/*override-id*)
                     nil
                     {}))))
  => "hello")

^{:refer xt.lang.base-repl/notify-form :added "4.0"}
(fact "creates the notify form")

^{:refer xt.lang.base-repl/print :added "4.0"}
(fact "creates the print op")

^{:refer xt.lang.base-repl/capture :added "4.0"}
(fact "creats the capture op")

^{:refer xt.lang.base-repl/notify :added "4.0"}
(fact "sends a message to the notify server"
  ^:hidden

  (notify/wait-on :js
    (k/notify 1))
  => 1

  (notify/wait-on :lua
    (k/notify 1))
  => 1
  
  (notify/wait-on :python
    (k/notify 1))
  => 1)

^{:refer xt.lang.base-repl/>notify :added "4.0"}
(fact "creates a callback function")

^{:refer xt.lang.base-repl/<! :added "4.0"}
(fact "creates a callback map"
  ^:hidden
  
  (notify/wait-on :js
    ((. (k/<!)
       ["success"]) 1))
  => 1
  
  (notify/wait-on :lua
   ((. (k/<!)
       ["success"]) 1))
  => 1
 
  (notify/wait-on :python
   ((. (k/<!)
       ["success"]) 1))
  => 1)
