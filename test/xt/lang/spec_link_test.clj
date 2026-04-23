(ns xt.lang.spec-link-test
  (:use code.test)
  (:require [clojure.set :as set]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-link :as spec-link]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-link/x:socket-connect :added "4.1"}
(fact "connects sockets and forwards the connection to callbacks"

  (notify/wait-on :js
    (do (var net (require "net"))
        (var port 18182)
        (var connect-fn
             (fn [host port opts cb]
               (spec-link/x:socket-connect host port opts cb)))
        (var server
             (. net (createServer (fn [conn]
                                    (. conn (end))))))
        (. server (listen port "127.0.0.1"))
        (connect-fn "127.0.0.1"
                    port
                    {}
                    (fn [err conn]
                      (. server (close))
                      (repl/notify [(xt/x:nil? err)
                                    (xt/x:is-function? (. conn ["write"]))])))))
  => [true true])

^{:refer xt.lang.spec-link/x:socket-send :added "4.1"}
(fact "sends socket messages through write"

  (!.js
    (var out nil)
    (var conn {:write (fn [s]
                        (:= out s))})
    (spec-link/x:socket-send conn "PING")
    out)
  => "PING")

^{:refer xt.lang.spec-link/x:socket-close :added "4.1"}
(fact "closes sockets through end"

  (!.js
    (var out nil)
    (var conn {:end (fn []
                      (:= out "closed"))})
    (spec-link/x:socket-close conn)
    out)
  => "closed")

^{:refer xt.lang.spec-link/x:notify-http :added "4.1"}
(fact "posts encoded values through fetch")
