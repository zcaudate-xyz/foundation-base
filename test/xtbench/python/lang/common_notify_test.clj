(ns
 xtbench.python.lang.common-notify-test
 (:require
  [net.http :as http]
  [std.json :as json]
  [std.lang :as l]
  [std.lang.interface.type-notify :as interface]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require [[xt.lang.common-lib :as k] [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-notify/wait-on, :added "4.0"}
(fact
 "sets up a code context and waits for oneshot notification"
 ^{:hidden true}
 (notify/wait-on :python (repl/notify 1))
 =>
 1)

^{:refer xt.lang.common-notify/captured,
  :added "4.0",
  :setup [(notify/captured:clear-all)]}
(fact
 "gets captured results"
 ^{:hidden true}
 (do
  (notify/wait-on
   :python
   (repl/capture {:from "python"})
   (repl/notify 1))
  (notify/captured :python))
 =>
 [{"from" "python"}])
