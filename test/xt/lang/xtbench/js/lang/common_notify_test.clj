(ns
 xtbench.js.lang.common-notify-test
 (:require
  [net.http :as http]
  [std.json :as json]
  [std.lang :as l]
  [std.lang.interface.type-notify :as interface]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require [[xt.lang.common-lib :as k] [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-notify/notify-ceremony, :added "4.0"}
(fact
 "creates the ceremony in order to get the port and method type"
 ^{:hidden true}
 (notify/notify-ceremony (assoc (l/rt :js) :type :basic))
 =>
 [(:id (l/rt :js))
  (:socket-port (l/default-notify))
  :js
  :socket
  "127.0.0.1"
  {}])

^{:refer xt.lang.common-notify/wait-on, :added "4.0"}
(fact
 "sets up a code context and waits for oneshot notification"
 ^{:hidden true}
 (notify/wait-on :js (repl/notify 1))
 =>
 1)

^{:refer xt.lang.common-notify/captured,
  :added "4.0",
  :setup [(notify/captured:clear-all)]}
(fact
 "gets captured results"
 ^{:hidden true}
 (do
  (notify/wait-on :js (repl/capture {:from "js"}) (repl/notify 1))
  (notify/captured :js))
 =>
 [{"from" "js"}])
