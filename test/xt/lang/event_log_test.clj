(ns xt.lang.event-log-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.json :as json]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.event-log :as log]
             [xt.lang.base-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.event-log :as log]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-log/new-log :added "4.0"}
(fact "creates a new log"
  ^:hidden

  (!.js
   (log/new-log {}))
  => map?
  
  (!.lua
   (log/new-log {}))
  => map?)

^{:refer xt.lang.event-log/get-count :added "4.0"}
(fact "gets the current count")

^{:refer xt.lang.event-log/get-last :added "4.0"}
(fact "gets the last log entry")

^{:refer xt.lang.event-log/get-head :added "4.0"}
(fact "gets `n` elements from beginning")

^{:refer xt.lang.event-log/get-filtered :added "4.0"}
(fact "filters entries using predicate")

^{:refer xt.lang.event-log/get-tail :added "4.0"}
(fact "gets `n` elements from tail")

^{:refer xt.lang.event-log/get-slice :added "4.0"}
(fact "gets a slice of the log entries")

^{:refer xt.lang.event-log/clear :added "4.0"}
(fact "clears all processed entries")

^{:refer xt.lang.event-log/clear-cache :added "4.0"}
(fact "clears log cache")

^{:refer xt.lang.event-log/queue-entry :added "4.0"}
(fact "queues a log entry"
  ^:hidden
  
  (!.js
   (var l (log/new-log {}))
   
   (k/for:index [i [0 10]]
     (log/queue-entry l {:id (k/cat "id-" i)}
                      k/id-fn
                      k/identity
                      1))
   (log/clear-cache l 100000)
   l)
  => {"callback" nil,
      "maximum" 100,
      "cache" {},
      "processed"
      [{"id" "id-0"}
       {"id" "id-1"}
       {"id" "id-2"}
       {"id" "id-3"}
       {"id" "id-4"}
       {"id" "id-5"}
       {"id" "id-6"}
       {"id" "id-7"}
       {"id" "id-8"}
       {"id" "id-9"}],
      "::" "event.log",
      "interval" 30000,
      "last" 100000,
      "listeners" {}}
  

  (!.lua
   (var l (log/new-log {}))
   
   (k/for:index [i [0 9]]
     (log/queue-entry l {:id (k/cat "id-" i)}
                      k/id-fn
                      k/identity
                      1))
   (log/clear-cache l 100000)
   l)
  => {"maximum" 100,
      "cache" {},
      "processed"
      [{"id" "id-0"}
       {"id" "id-1"}
       {"id" "id-2"}
       {"id" "id-3"}
       {"id" "id-4"}
       {"id" "id-5"}
       {"id" "id-6"}
       {"id" "id-7"}
       {"id" "id-8"}
       {"id" "id-9"}],
      "::" "event.log",
      "interval" 30000,
      "last" 100000,
      "listeners" {}})

^{:refer xt.lang.event-log/list-listeners :adopt true :added "4.0"}
(fact "lists all listeners"
  ^:hidden
  
  (set (!.js
        (log/list-listeners
         (log/new-log {:listeners {:test1 (fn [id data t])
                                   :test2 (fn [id data t])}}))))
  => #{"test1" "test2"}

  (set (!.lua
        (log/list-listeners
         (log/new-log {:listeners {:test1 (fn [id data t])
                                   :test2 (fn [id data t])}}))))
  => #{"test1" "test2"})

^{:refer xt.lang.event-log/add-listener :added "4.0"}
(fact "adds a listener to the log"
  ^:hidden

  (!.js
   (log/add-listener
    (log/new-log {})
    "test1" (fn [id data t])))
  => {"pred" nil, "meta" {"listener/id" "test1", "listener/type" "log"}}

  (!.lua
   (k/get-data
    (log/add-listener
     (log/new-log {})
     "test1" (fn [id data t]))))
  => {"callback" "<function>",
      "meta" {"listener/id" "test1", "listener/type" "log"}})

^{:refer xt.lang.event-log/remove-listener :adopt true :added "4.0"}
(fact "removes a listener"
  ^:hidden
  
  (!.js
   (var l (log/new-log {}))
   (log/add-listener
    l
    "test1" (fn [id data t meta]))
   (log/remove-listener
    l
    "test1"))
  => {"pred" nil, "meta" {"listener/id" "test1", "listener/type" "log"}}


  (!.js
   (var l (log/new-log {}))
   (log/add-listener
    l
    "test1" (fn [id data t meta]))
   (k/get-data
    (log/remove-listener
     l
     "test1")))
  => {"callback" "<function>",
      "pred" nil,
      "meta" {"listener/id" "test1", "listener/type" "log"}})



