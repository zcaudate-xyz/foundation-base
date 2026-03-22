(ns xt.lang.event-log-latest-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.event-log-latest :as log-latest]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.event-log-latest :as log-latest]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-log-latest/new-log-latest :added "4.0"}
(fact "creates a new log-latest"
  ^:hidden

  (!.js
   (log-latest/new-log-latest {}))
  => map?
  
  (!.lua
   (log-latest/new-log-latest {}))
  => map?)

^{:refer xt.lang.event-log-latest/clear-cache :added "4.0"}
(fact "clears the cache given a time point"
  ^:hidden
  
  (!.js
   (var log (log-latest/new-log-latest {}))
   (log-latest/queue-latest log "a" 1)
   (log-latest/queue-latest log "b" 2)
   [(log-latest/clear-cache log 0)
    (log-latest/clear-cache log (+ (k/now-ms)
                                   100000))])
  => [[] ["a" "b"]])

^{:refer xt.lang.event-log-latest/queue-latest :added "4.0"}
(fact "queues the latest time to log"
  ^:hidden

  (!.js
   (var log (log-latest/new-log-latest {}))
   [(log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 2)
    (log-latest/queue-latest log "a" 2)])
  => [true false true false]

  (!.lua
   (var log (log-latest/new-log-latest {}))
   [(log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 2)
    (log-latest/queue-latest log "a" 2)])
  => [true false true false])
