(ns
 xtbench.lua.lang.event-log-test
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
   [xt.lang.common-data :as xtd]
   [xt.lang.common-spec :as xt]
   [xt.lang.event-log :as log]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-log/new-log, :added "4.0"}
(fact
 "creates a new log"
 ^{:hidden true}
 (!.lua (log/new-log {}))
 =>
 map?)

^{:refer xt.lang.event-log/queue-entry, :added "4.0"}
(fact
 "queues a log entry"
 ^{:hidden true}
 (!.lua
  (var l (log/new-log {}))
  (xt/for:index
   [i [0 9]]
   (log/queue-entry
    l
    {:id (xt/x:cat "id-" (xt/x:to-string i))}
    log/id-fn
    k/identity
    1))
  (log/clear-cache l 100000)
  l)
 =>
 {"maximum" 100,
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

^{:refer xt.lang.event-log/list-listeners, :adopt true, :added "4.0"}
(fact
 "lists all listeners"
 ^{:hidden true}
 (!.lua
  (xtd/arr-sort
   (log/list-listeners
    (log/new-log
     {:listeners {:test1 (fn [id data t]), :test2 (fn [id data t])}}))
   k/identity
   xt/x:str-lt))
 =>
 ["test1" "test2"])

^{:refer xt.lang.event-log/add-listener, :added "4.0"}
(fact
 "adds a listener to the log"
 ^{:hidden true}
 (!.lua
  (xtd/tree-get-data
   (log/add-listener (log/new-log {}) "test1" (fn [id data t]) nil)))
 =>
 {"callback" "<function>",
  "meta" {"listener/id" "test1", "listener/type" "log"}})

^{:refer xt.lang.event-log/remove-listener, :adopt true, :added "4.0"}
(fact
 "removes a listener"
 (!.lua
  (var l (log/new-log {}))
  (log/add-listener l "test1" (fn [id data t meta]) nil)
  (xtd/tree-get-data (log/remove-listener l "test1")))
 =>
 {"callback" "<function>",
  "meta" {"listener/id" "test1", "listener/type" "log"}})
