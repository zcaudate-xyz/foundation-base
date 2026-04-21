(ns std.concurrent.thread-test
  (:require [std.concurrent.thread :refer :all]
            [std.lib.foundation :as f])
  (:use [code.test :exclude [run]]))

^{:refer std.concurrent.thread/thread:current :added "3.0"}
(fact "returns the current thread"

  (thread:current)
  => Thread)

^{:refer std.concurrent.thread/thread:id :added "3.0"}
(fact "returns the id of a thread"

  (thread:id)
  => number?)

^{:refer std.concurrent.thread/thread:interrupt :added "3.0"}
(fact "interrupts a thread"

  (doto (thread {:handler (fn []
                            (f/suppress (Thread/sleep 100)))
                 :start true})
    (thread:interrupt))
  => Thread)

^{:refer std.concurrent.thread/thread:sleep :added "3.0"}
(fact "sleeps for n milliseconds"

  (thread:sleep 10)
  => nil)

^{:refer std.concurrent.thread/thread:spin :added "3.0"}
(fact "waits using onSpin"

  (thread:spin)
  => nil)

^{:refer std.concurrent.thread/thread:wait-on :added "3.0"}
(fact "waits for a lock to notify"

  (let [lock (Object.)]
    (future (thread:sleep 500)
            (thread:notify lock))
    (thread:wait-on lock))
  => nil)

^{:refer std.concurrent.thread/thread:notify :added "3.0"}
(fact "notifies threads waiting on lock"
  (let [lock (Object.)]
    (thread:notify lock))
  => nil)

^{:refer std.concurrent.thread/thread:notify-all :added "3.0"}
(fact "notifies all threads waiting on lock"
  (let [lock (Object.)]
    (thread:notify-all lock))
  => nil)

^{:refer std.concurrent.thread/thread:has-lock? :added "3.0"}
(fact "checks if thread has the lock"

  (let [lock (Object.)]
    (locking lock
      (thread:has-lock? lock)))
  => true)

^{:refer std.concurrent.thread/thread:yield :added "3.0"}
(fact "calls yield on current thread"

  (thread:yield)
  => nil)

^{:refer std.concurrent.thread/stacktrace :added "3.0"}
(fact "returns thread stacktrace"

  (vec (stacktrace))
  => vector?)

^{:refer std.concurrent.thread/all-stacktraces :added "3.0"}
(fact "returns all available stacktraces"

  (all-stacktraces)
  => #(instance? java.util.Map %))

^{:refer std.concurrent.thread/thread:all :added "3.0"}
(fact "lists all threads"

  (thread:all)
  => seq?)

^{:refer std.concurrent.thread/thread:all-ids :added "3.0"}
(fact "lists all thread ids"

  (thread:all-ids)
  => set?)

^{:refer std.concurrent.thread/thread:dump :added "3.0"}
(fact "dumps out current thread information"

  (do (thread:dump) true)
  => true)

^{:refer std.concurrent.thread/thread:active-count :added "3.0"}
(fact "returns active threads"

  (thread:active-count)
  => number?)

^{:refer std.concurrent.thread/thread:alive? :added "3.0"}
(fact "checks if thread is alive"

  (thread:alive? (thread:current))
  => true)

^{:refer std.concurrent.thread/thread:daemon? :added "3.0"}
(fact "checks if thread is a daemon"

  (thread:daemon? (thread:current))
  => boolean?)

^{:refer std.concurrent.thread/thread:interrupted? :added "3.0"}
(fact "checks if thread has been interrupted"

  (thread:interrupted? (thread:current))
  => false)

^{:refer std.concurrent.thread/thread:has-access? :added "3.0"}
(fact "checks if thread allows access to current"

  (thread:has-access? (thread:current))
  => true)

^{:refer std.concurrent.thread/thread:start :added "3.0"}
(fact "starts a thread"

  (let [started (promise)
        t (thread {:handler (fn []
                              (deliver started true)
                              (thread:sleep 50))})]
    (thread:start t)
    [(deref started 100 false)
     (do (thread:join t) true)])
  => [true true])

^{:refer std.concurrent.thread/thread:run :added "3.0"}
(fact "runs the thread function locally"

  (-> (thread {:handler (fn [])})
      (thread:run))
  => nil)

^{:refer std.concurrent.thread/thread:join :added "3.0"}
(fact "calls join on a thread"

  (thread:join (thread {:handler (fn [])
                        :start true}))
  => nil)

^{:refer std.concurrent.thread/thread:uncaught :added "3.0"}
(fact "gets and sets the uncaught exception handler"
  (let [handler (reify Thread$UncaughtExceptionHandler
                  (uncaughtException [_ _ _] nil))
        thread  (thread {:handler (fn [])})]
    (thread:uncaught thread handler)
    (= handler (thread:uncaught thread)))
  => true)

^{:refer std.concurrent.thread/thread:global-uncaught :added "3.0"}
(fact "gets and sets the global uncaught exception handler"
  (let [original (thread:global-uncaught)
        handler  (reify Thread$UncaughtExceptionHandler
                   (uncaughtException [_ _ _] nil))]
    (try
      (thread:global-uncaught handler)
      (= handler (thread:global-uncaught))
      (finally
        (thread:global-uncaught original))))
  => true)

^{:refer std.concurrent.thread/thread:classloader :added "3.0"}
(fact "gets and sets the context classloader"
  (let [thread   (thread:current)
        original (thread:classloader thread)
        loader   (.getContextClassLoader (Thread/currentThread))]
    (try
      (thread:classloader thread loader)
      (= loader (thread:classloader thread))
      (finally
        (thread:classloader thread original))))
  => true)

^{:refer std.concurrent.thread/thread :added "3.0"}
(fact "creates a new thread"
  (let [thread (thread {:handler (fn [])
                        :name "hello-thread"
                        :daemon true})]
    [(.getName thread)
     (thread:daemon? thread)])
  => ["hello-thread" true])
