(ns std.lib.future-test
  (:require [std.lib.future :as f :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [future future? catch]))

^{:refer std.lib.future/completed :added "3.0"}
(fact "creates a completed stage" ^:hidden

  @(completed 1)
  => 1)

^{:refer std.lib.future/failed :added "3.0"}
(fact "creates a failed stage" ^:hidden

  @(failed (ex-info "" {}))
  => (throws))

^{:refer std.lib.future/incomplete :added "3.0"}
(fact "creates an incomplete stage (like promise)"

  (incomplete)
  => (comp not future:complete?))

^{:refer std.lib.future/future? :added "3.0"}
(fact "checks if object is `CompleteableFuture`"

  (f/future? (f/future 1))
  => true)

^{:refer std.lib.future/future:fn :added "3.0"}
(fact "creates a future with :init/future props" ^:hidden

  (.get (f/future:fn (fn [] 1)))
  => (contains {:fn fn?}))

^{:refer std.lib.future/future:call :added "3.0"}
(fact "can create a future from a function or :init/future"

  @(f/future:call (fn [] 1))
  => 1

  @(-> (f/future:fn (fn [] 1))
       (f/future:call))
  => 1)

^{:refer std.lib.future/future:timeout :added "3.0"}
(fact "adds a timeout to the completed future"

  @(-> (f/future:call (fn [] (Thread/sleep 100)))
       (f/future:timeout 10 :ok))
  => :ok ^:hidden

  @(-> (f/future:call (fn [] (Thread/sleep 100)))
       (f/future:timeout 10))
  => (throws))

^{:refer std.lib.future/future:wait :added "3.0"}
(fact "waits for future to finish or "

  (-> (f/future:call (fn [] (Thread/sleep 100)))
      (f/future:timeout 10 :ok)
      (f/future:wait)
      (f/future:now))
  => :ok)

^{:refer std.lib.future/future:run :added "3.0"}
(fact "runs a function with additional settings"

  (f/future:run (fn [] (Thread/sleep 100))
              {:timeout 10
               :default :ok
               :delay 100
               :pool :async})
  => any?)

^{:refer std.lib.future/future:now :added "3.0"}
(fact "gets the value of a future at the current moment" ^:hidden

  (-> (f/future:run (fn [] (Thread/sleep 100)))
      (f/future:now :invalid))
  => :invalid)

^{:refer std.lib.future/future:value :added "3.0"}
(fact "gets the value of a future" ^:hidden

  (-> (f/future:run (fn [] 1))
      (f/future:value))
  => 1)

^{:refer std.lib.future/future:exception :added "3.0"}
(fact "accesses the exception in the future" ^:hidden

  (-> (f/future:run (fn [] (throw (ex-info "ERROR" {}))))
      (f/future:exception))
  => Throwable)

^{:refer std.lib.future/future:cancel :added "3.0"}
(fact "cancels the execution of the future" ^:hidden

  @(-> (f/future:run (fn [] (Thread/sleep 100)))
       (f/future:cancel))
  => (throws))

^{:refer std.lib.future/future:done :added "3.0"}
(fact "helper macro for status functions"

  (f/future:done (completed 1) true)
  => true)

^{:refer std.lib.future/future:cancelled? :added "3.0"}
(fact "checks if future has been cancelled" ^:hidden

  (-> (f/future:run (fn [] (Thread/sleep 100)))
      (f/future:cancel)
      (f/future:cancelled?))
  => true)

^{:refer std.lib.future/future:exception? :added "3.0"}
(fact "checks if future raised an exception" ^:hidden

  (-> (f/future:run (fn [] (throw (ex-info "Error" {}))))
      (f/future:wait)
      (f/future:exception?))
  => true

  (-> (f/future:run (fn [] (Thread/sleep 1000)))
      (f/future:exception?))
  => (throws))

^{:refer std.lib.future/future:timeout? :added "3.0"}
(fact "checks if future errored due to timeout" ^:hidden

  (-> (f/future:run (fn [] (Thread/sleep 100)))
      (f/future:timeout 10)
      (f/future:wait)
      (f/future:timeout?))
  => true)

^{:refer std.lib.future/future:success? :added "3.0"}
(fact "checks that future is successful" ^:hidden

  (-> (f/future:run (fn [] (Thread/sleep 100)))
      (f/future:wait)
      (f/future:success?))
  => true

  (-> (f/future:run (fn [] (Thread/sleep 100)))
      (f/future:success?))
  => (throws))

^{:refer std.lib.future/future:incomplete? :added "3.0"}
(fact "check that future is incomplete"

  (-> (incomplete)
      (f/future:incomplete?))
  => true)

^{:refer std.lib.future/future:complete? :added "3.0"}
(fact "checks that future has successfully completed" ^:hidden

  (-> (f/future:run (fn [] (throw (ex-info "Error" {}))))
      (f/future:wait)
      (f/future:success?))
  => false

  (f/future:complete? (completed 1))
  => true)

^{:refer std.lib.future/future:force :added "3.0"}
(fact "forces a value or exception as completed future" ^:hidden

  (-> (f/future:run (fn [] (Thread/sleep 1000)))
      (f/future:force 10)
      (f/future:value))
  => 10

  (-> (f/future:run (fn [] (Thread/sleep 1000)))
      (f/future:force 10)
      (f/future:complete?))
  => true

  (-> (f/future:run (fn [] (Thread/sleep 1000)))
      (f/future:force :exception (ex-info "Error" {}))
      (f/future:exception?))
  => true)

^{:refer std.lib.future/future:obtrude :added "4.0"}
(fact "like force but uses obtrude and obtrudeException"
  (-> (f/future:run (fn [] (Thread/sleep 1000)))
      (f/future:obtrude 10)
      (f/future:value))
  => 10)

^{:refer std.lib.future/future:dependents :added "3.0"}
(fact "returns number of steps waiting on current result" ^:hidden

  (-> (doto (f/future:run (fn [] (Thread/sleep 1000)))
        (on:complete (fn [e] e))
        (on:complete (fn [e] e)))
      (f/future:dependents))
  => 2)

^{:refer std.lib.future/future:lift :added "3.0"}
(fact "creates a future from a value" ^:hidden

  (f/future? (f/future:lift (Object.)))
  => true)

^{:refer std.lib.future/on:complete :added "3.0"}
(fact "process both the value and exception" ^:hidden

  @(-> (f/future 1)
       (on:complete (fn [val _] (throw (ex-info "Error" {:val (inc val)}))))
       (on:complete (fn [_ err] (inc (:val (ex-data err)))))
       (on:complete (fn [val _] (inc val))))
  => 4)

^{:refer std.lib.future/on:timeout :added "3.0"}
(fact "processes a function on timeout" ^:hidden

  (-> (f/future (Thread/sleep 100))
      (f/future:timeout 10)
      (on:timeout (fn [_] :timeout))
      (f/future:wait)
      (f/future:value))
  => :timeout

  @(-> (f/future {:timeout 10} (Thread/sleep 100))
       (f/future:cancel)
       (on:timeout (fn [_] :timeout)))
  => (throws))

^{:refer std.lib.future/on:cancel :added "3.0"}
(fact "processes a function on cancel" ^:hidden

  @(-> (f/future {:timeout 10} (Thread/sleep 100))
       (f/future:cancel)
       (on:cancel (fn [_] :cancel)))
  => :cancel

  @(-> (f/future {:timeout 10} (Thread/sleep 100))
       (f/future:wait)
       (on:cancel (fn [_] :cancel)))
  => (throws))

^{:refer std.lib.future/on:exception :added "3.0"}
(fact "process a function on exception" ^:hidden

  @(-> (f/future {:timeout 10} (Thread/sleep 100))
       (f/future:cancel)
       (on:exception (fn [_] :cancel)))
  => :cancel

  @(-> (f/future {:timeout 10} (Thread/sleep 100))
       (f/future:wait)
       (on:exception (fn [_] :timeout)))
  => :timeout

  @(-> (f/future (throw (ex-info "Error" {})))
       (f/future:wait)
       (on:exception (fn [_] :exception)))
  => :exception)

^{:refer std.lib.future/on:success :added "3.0"}
(fact "processes another step given successful operation" ^:hidden

  @(-> (f/future 1)
       (on:success inc)
       (on:success inc)
       (on:success inc))
  => 4)

^{:refer std.lib.future/on:all :added "3.0"}
(fact "calls a function when all futures are complete" ^:hidden

  @(on:all [(f/future 1)
            (f/future 2)
            (f/future 3)]
           (fn [a b c] (+ a b c)))
  => 6)

^{:refer std.lib.future/on:any :added "3.0"}
(fact "calls a function when any future is completed" ^:hidden

  @(on:any [(f/future (Thread/sleep 500) 1)
            (f/future (Thread/sleep 500) 2)
            (f/future 3)]
           (fn [a] a))
  => any)

^{:refer std.lib.future/future :added "3.0"}
(fact "constructs a completable future"

  @(f/future (Thread/sleep 100)
           1)
  => 1)

^{:refer std.lib.future/then :added "3.0" :style/indent 1}
(fact "shortcut for :on/success and :on/complete" ^:hidden

  @(-> (f/future (+ 1 2 3))
       (then [a] (+ a 1 2 3)))
  => 12

  @(-> (f/future (throw (ex-info "Error" {:data 1})))
       (then [a] (+ a 1 2 3))
       (f/catch [ex] (ex-data ex)))
  => {:data 1}

  @(-> (f/future (+ 1 2 3))
       (then [_ err] err))
  => nil)

^{:refer std.lib.future/catch :added "3.0"  :style/indent 1}
(fact "shortcut for :on/exception" ^:hidden

  @(-> (f/future (+ 2 3))
       (f/catch [e] e))
  => 5

  @(-> (f/future (throw (ex-info "" {:a 1})))
       (f/catch [e] (ex-data e)))
  => {:a 1})

^{:refer std.lib.future/fulfil :added "3.0"}
(fact "fulfils the return with a function"

  (fulfil (incomplete) (fn [] (+ 1 2 3)))
  => future?

  (fulfil (incomplete) (fn [] (throw (ex-info "Hello" {}))))
  => future?)

^{:refer std.lib.future/future:result :added "3.0"}
(fact "gets the result of the future"

  (f/future:result (completed 1))
  => {:status :success, :data 1, :exception nil}

  (f/future:result (failed (ex-info "" {})))
  => (contains {:status :error
                :data nil
                :exception Throwable}))

^{:refer std.lib.future/future:status :added "3.0"}
(fact "retrieves a status of either `:success`, `:error` or `waiting`"
  (f/future:status (completed 1))
  => :success)

^{:refer std.lib.future/future:chain :added "3.0"}
(fact "chains a set of functions"

  @(f/future:chain (completed 1) [inc inc inc])
  => 4

  @(f/future:chain (failed (ex-info "ERROR" {})) [inc inc inc])
  => (throws))
