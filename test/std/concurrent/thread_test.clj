(ns std.concurrent.thread-test
  (:use [code.test :exclude [run]])
  (:require [std.concurrent.thread :refer :all]
            [std.lib :as h]))

^{:refer std.concurrent.thread/thread:current :added "3.0"}
(fact "returns the current thread"
  ^:hidden

  (thread:current)
  => Thread)

^{:refer std.concurrent.thread/thread:id :added "3.0"}
(fact "returns the id of a thread"
  ^:hidden

  (thread:id)
  => number?)

^{:refer std.concurrent.thread/thread:interrupt :added "3.0"}
(fact "interrupts a thread"
  ^:hidden

  (doto (thread {:handler (fn []
                            (h/suppress (Thread/sleep 100)))
                 :start true})
    (thread:interrupt))
  => Thread)

^{:refer std.concurrent.thread/thread:sleep :added "3.0"}
(fact "sleeps for n milliseconds"
  ^:hidden

  (thread:sleep 10)
  => nil)

^{:refer std.concurrent.thread/thread:spin :added "3.0"}
(fact "waits using onSpin"
  ^:hidden

  (thread:spin)
  => nil)

^{:refer std.concurrent.thread/thread:wait-on :added "3.0"}
(fact "waits for a lock to notify"
  ^:hidden

  (let [lock (Object.)]
    (future (thread:sleep 500)
            (thread:notify lock))
    (thread:wait-on lock))
  => nil)

^{:refer std.concurrent.thread/thread:notify :added "3.0"}
(fact "notifies threads waiting on lock")

^{:refer std.concurrent.thread/thread:notify-all :added "3.0"}
(fact "notifies all threads waiting on lock")

^{:refer std.concurrent.thread/thread:has-lock? :added "3.0"}
(fact "checks if thread has the lock"
  ^:hidden

  (let [lock (Object.)]
    (locking lock
      (thread:has-lock? lock)))
  => true)

^{:refer std.concurrent.thread/thread:yield :added "3.0"}
(fact "calls yield on current thread"
  ^:hidden

  (thread:yield)
  => nil)

^{:refer std.concurrent.thread/stacktrace :added "3.0"}
(fact "returns thread stacktrace"

  (vec (stacktrace))
  => )

^{:refer std.concurrent.thread/all-stacktraces :added "3.0"}
(fact "returns all available stacktraces"
  ^:hidden

  (all-stacktraces))

^{:refer std.concurrent.thread/thread:all :added "3.0"}
(fact "lists all threads"
  ^:hidden

  (thread:all))

^{:refer std.concurrent.thread/thread:all-ids :added "3.0"}
(fact "lists all thread ids"
  ^:hidden

  (thread:all-ids))

^{:refer std.concurrent.thread/thread:dump :added "3.0"}
(comment "dumps out current thread information"
  ^:hidden

  (thread:dump))

^{:refer std.concurrent.thread/thread:active-count :added "3.0"}
(fact "returns active threads"
  ^:hidden

  (thread:active-count)
  => number?)

^{:refer std.concurrent.thread/thread:alive? :added "3.0"}
(fact "checks if thread is alive"
  ^:hidden

  (thread:alive? (thread:current))
  => true)

^{:refer std.concurrent.thread/thread:daemon? :added "3.0"}
(fact "checks if thread is a daemon"
  ^:hidden

  (thread:daemon? (thread:current))
  => boolean?)

^{:refer std.concurrent.thread/thread:interrupted? :added "3.0"}
(fact "checks if thread has been interrupted"
  ^:hidden

  (thread:interrupted? (thread:current))
  => false)

^{:refer std.concurrent.thread/thread:has-access? :added "3.0"}
(fact "checks if thread allows access to current"
  ^:hidden

  (thread:has-access? (thread:current))
  => true)

^{:refer std.concurrent.thread/thread:start :added "3.0"}
(fact "starts a thread"
  ^:hidden

  (-> (thread {:handler (fn [])})
      (thread:start)))

^{:refer std.concurrent.thread/thread:run :added "3.0"}
(fact "runs the thread function locally"
  ^:hidden

  (-> (thread {:handler (fn [])})
      (thread:run)))

^{:refer std.concurrent.thread/thread:join :added "3.0"}
(fact "calls join on a thread"
  ^:hidden

  (thread:join (thread {:handler (fn [])
                        :start true}))
  => nil)

^{:refer std.concurrent.thread/thread:uncaught :added "3.0"}
(fact "gets and sets the uncaught exception handler")

^{:refer std.concurrent.thread/thread:global-uncaught :added "3.0"}
(fact "gets and sets the global uncaught exception handler")

^{:refer std.concurrent.thread/thread:classloader :added "3.0"}
(fact "gets and sets the context classloader")

^{:refer std.concurrent.thread/thread :added "3.0"}
(fact "creates a new thread")
