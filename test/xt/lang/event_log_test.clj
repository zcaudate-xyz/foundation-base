(ns xt.lang.event-log-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :xtalk
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.event-log :as log]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.event-log :as log]
             [xt.lang.common-repl :as repl]]})

(defn.xt walk
  [obj pre-fn post-fn]
  (:= obj (pre-fn obj))
  (cond (xt/x:nil? obj)
        (return (post-fn obj))

        (xt/x:is-object? obj)
        (do (var out := {})
            (xt/for:object [[k v] obj]
              (xt/x:set-key out k (-/walk v pre-fn post-fn)))
            (return (post-fn out)))

        (xt/x:is-array? obj)
        (do (var out := [])
            (xt/for:array [e obj]
              (xt/x:arr-push out (-/walk e pre-fn post-fn)))
            (return (post-fn out)))

        :else
        (return (post-fn obj))))

(defn.xt get-data
  [obj]
  (var data-fn
       (fn [obj]
         (if (or (xt/x:is-string? obj)
                 (xt/x:is-number? obj)
                 (xt/x:is-boolean? obj)
                 (xt/x:is-object? obj)
                 (xt/x:is-array? obj)
                 (xt/x:nil? obj))
           (return obj)
           (return (xt/x:cat "<" (k/type-native obj) ">")))))
  (return (-/walk obj k/identity data-fn)))

(defn.xt id-fn
  [x]
  (return (xt/x:get-key x "id")))

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
   
   (xt/for:index [i [0 10]]
     (log/queue-entry l {:id (xt/x:cat "id-" i)}
                      -/id-fn
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
   
   (xt/for:index [i [0 9]]
     (log/queue-entry l {:id (xt/x:cat "id-" i)}
                      -/id-fn
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
   (-/get-data
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
   (-/get-data
    (log/remove-listener
     l
     "test1")))
  => {"callback" "<function>",
      "pred" nil,
      "meta" {"listener/id" "test1", "listener/type" "log"}})
