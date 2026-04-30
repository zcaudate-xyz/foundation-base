(ns xt.event.base-log-latest-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.base-log-latest :as log-latest]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.base-log-latest :as log-latest]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.base-log-latest :as log-latest]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.base-log-latest/queue-latest :added "4.1"}
(fact "deduplicates latest timestamps and clears cache"

  (!.js
   (var log (log-latest/new-log-latest {}))
   [(log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 2)
    (log-latest/queue-latest log "a" 2)
    (log-latest/queue-latest log "b" 3)
    (log-latest/clear-cache log 0)
    (log-latest/clear-cache log (+ (xt/x:now-ms)
                                   100000))])
  => [true false true false true [] ["a" "b"]]

  (!.lua
   (var log (log-latest/new-log-latest {}))
   [(log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 2)
    (log-latest/queue-latest log "a" 2)
    (log-latest/queue-latest log "b" 3)
    (log-latest/clear-cache log 0)
    (log-latest/clear-cache log (+ (xt/x:now-ms)
                                   100000))])
  => [true false true false true [] ["a" "b"]]

  (!.py
   (var log (log-latest/new-log-latest {}))
   [(log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 1)
    (log-latest/queue-latest log "a" 2)
    (log-latest/queue-latest log "a" 2)
    (log-latest/queue-latest log "b" 3)
    (log-latest/clear-cache log 0)
    (log-latest/clear-cache log (+ (xt/x:now-ms)
                                   100000))])
  => [true false true false true [] ["a" "b"]])

^{:refer xt.event.base-log-latest/new-log-latest :added "4.1"}
(fact "creates a new latest-log container"

  (!.js
   (var log (log-latest/new-log-latest {:interval 10}))
   [(. log ["::"])
    (. log ["interval"])
    (. log ["callback"])
    (. log ["cache"])])
  => ["event.log-latest"
      10
      nil
      {}]

  (!.lua
   (var log (log-latest/new-log-latest {:interval 10}))
   [(. log ["::"])
    (. log ["interval"])
    (. log ["callback"])
    (. log ["cache"])])
  => ["event.log-latest"
      10
      nil
      {}]

  (!.py
   (var log (log-latest/new-log-latest {:interval 10}))
   [(. log ["::"])
    (. log ["interval"])
    (. log ["callback"])
    (. log ["cache"])])
  => ["event.log-latest"
      10
      nil
      {}])

^{:refer xt.event.base-log-latest/clear-cache :added "4.1"}
(fact "evicts stale latest-log cache entries"

  (!.js
   (var log (log-latest/new-log-latest
             {:interval 10
              :cache {"a" {"t" 0 "latest" 1}
                      "b" {"t" 15 "latest" 2}}}))
   [(log-latest/clear-cache log 20)
    (xt/x:obj-keys (. log ["cache"]))
    (. log ["last"])])
  => [["a"]
      ["b"]
      20]

  (!.lua
   (var log (log-latest/new-log-latest
             {:interval 10
              :cache {"a" {"t" 0 "latest" 1}
                      "b" {"t" 15 "latest" 2}}}))
   [(log-latest/clear-cache log 20)
    (xt/x:obj-keys (. log ["cache"]))
    (. log ["last"])])
  => [["a"]
      ["b"]
      20]

  (!.py
   (var log (log-latest/new-log-latest
             {:interval 10
              :cache {"a" {"t" 0 "latest" 1}
                      "b" {"t" 15 "latest" 2}}}))
   [(log-latest/clear-cache log 20)
    (xt/x:obj-keys (. log ["cache"]))
    (. log ["last"])])
  => [["a"]
      ["b"]
      20])

(comment
  (s/snapto '[xt.event.base-log-latest])
  
  (s/seedgen-benchadd '[xt.event.base-log-latest] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.base-log-latest]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-log-latest]  {:lang [:lua :python] :write true}))