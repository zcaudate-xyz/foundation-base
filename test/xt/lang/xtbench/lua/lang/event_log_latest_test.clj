(ns
 xtbench.lua.lang.event-log-latest-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-spec :as xt]
   [xt.lang.event-log-latest :as log-latest]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-log-latest/new-log-latest, :added "4.0"}
(fact
 "creates a new log-latest"
 ^{:hidden true}
 (!.lua (log-latest/new-log-latest {}))
 =>
 map?)

^{:refer xt.lang.event-log-latest/clear-cache, :added "4.0"}
(fact
 "clears the cache given a time point"
 ^{:hidden true}
 (!.lua
  (var log (log-latest/new-log-latest {}))
  (log-latest/queue-latest log "a" 1)
  (log-latest/queue-latest log "b" 2)
  [(log-latest/clear-cache log 0)
   (log-latest/clear-cache log (+ (xt/x:now-ms) 100000))])
 =>
 [{} ["a" "b"]])

^{:refer xt.lang.event-log-latest/queue-latest, :added "4.0"}
(fact
 "queues the latest time to log"
 ^{:hidden true}
 (!.lua
  (var log (log-latest/new-log-latest {}))
  [(log-latest/queue-latest log "a" 1)
   (log-latest/queue-latest log "a" 1)
   (log-latest/queue-latest log "a" 2)
   (log-latest/queue-latest log "a" 2)])
 =>
 [true false true false])
