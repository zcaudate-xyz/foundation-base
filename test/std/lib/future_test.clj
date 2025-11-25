(ns std.lib.future-test
  (:use code.test)
  (:require [std.lib.future :as f :refer :all])
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

  (future? (future 1))
  => true)

^{:refer std.lib.future/future:fn :added "3.0"}
(fact "creates a future with :init/future props" ^:hidden

  (.get (future:fn (fn [] 1)))
  => (contains {:fn fn?}))

^{:refer std.lib.future/future:call :added "3.0"}
(fact "can create a future from a function or :init/future"

  @(future:call (fn [] 1))
  => 1

  @(-> (future:fn (fn [] 1))
       (future:call))
  => 1)

^{:refer std.lib.future/future:timeout :added "3.0"}
(fact "adds a timeout to the completed future"

  @(-> (future:call (fn [] (Thread/sleep 100)))
       (future:timeout 10 :ok))
  => :ok ^:hidden

  @(-> (future:call (fn [] (Thread/sleep 100)))
       (future:timeout 10))
  => (throws))

^{:refer std.lib.future/future:wait :added "3.0"}
(fact "waits for future to finish or "

  (-> (future:call (fn [] (Thread/sleep 100)))
      (future:timeout 10 :ok)
      (future:wait)
      (future:now))
  => :ok)

^{:refer std.lib.future/future:run :added "3.0"}
(fact "runs a function with additional settings"

  (future:run (fn [] (Thread/sleep 100))
              {:timeout 10
               :default :ok
               :delay 100
               :pool :async})
  => any?)

^{:refer std.lib.future/future:now :added "3.0"}
(fact "gets the value of a future at the current moment" ^:hidden

  (-> (future:run (fn [] (Thread/sleep 100)))
      (future:now :invalid))
  => :invalid)

^{:refer std.lib.future/future:value :added "3.0"}
(fact "gets the value of a future" ^:hidden

  (-> (future:run (fn [] 1))
      (future:value))
  => 1)

^{:refer std.lib.future/future:exception :added "3.0"}
(fact "accesses the exception in the future" ^:hidden

  (-> (future:run (fn [] (throw (ex-info "ERROR" {}))))
      (future:exception))
  => Throwable)

^{:refer std.lib.future/future:cancel :added "3.0"}
(fact "cancels the execution of the future" ^:hidden

  @(-> (future:run (fn [] (Thread/sleep 100)))
       (future:cancel))
  => (throws))

^{:refer std.lib.future/future:done :added "3.0"}
(fact "helper macro for status functions"
  (future:done (completed 1) true)
  => true)

^{:refer std.lib.future/future:cancelled? :added "3.0"}
(fact "checks if future has been cancelled" ^:hidden

  (-> (future:run (fn [] (Thread/sleep 100)))
      (future:cancel)
      (future:cancelled?))
  => true)

^{:refer std.lib.future/future:exception? :added "3.0"}
(fact "checks if future raised an exception" ^:hidden

  (-> (future:run (fn [] (throw (ex-info "Error" {}))))
      (future:wait)
      (future:exception?))
  => true

  (-> (future:run (fn [] (Thread/sleep 1000)))
      (future:exception?))
  => (throws))

^{:refer std.lib.future/future:timeout? :added "3.0"}
(fact "checks if future errored due to timeout" ^:hidden

  (-> (future:run (fn [] (Thread/sleep 100)))
      (future:timeout 10)
      (future:wait)
      (future:timeout?))
  => true)

^{:refer std.lib.future/future:success? :added "3.0"}
(fact "checks that future is successful" ^:hidden

  (-> (future:run (fn [] (Thread/sleep 100)))
      (future:wait)
      (future:success?))
  => true

  (-> (future:run (fn [] (Thread/sleep 100)))
      (future:success?))
  => (throws))

^{:refer std.lib.future/future:incomplete? :added "3.0"}
(fact "check that future is incomplete"

  (-> (incomplete)
      (future:incomplete?))
  => true)

^{:refer std.lib.future/future:complete? :added "3.0"}
(fact "checks that future has successfully completed" ^:hidden

  (-> (future:run (fn [] (throw (ex-info "Error" {}))))
      (future:wait)
      (future:success?))
  => false

  (future:complete? (completed 1))
  => true)

^{:refer std.lib.future/future:force :added "3.0"}
(fact "forces a value or exception as completed future" ^:hidden

  (-> (future:run (fn [] (Thread/sleep 1000)))
      (future:force 10)
      (future:value))
  => 10

  (-> (future:run (fn [] (Thread/sleep 1000)))
      (future:force 10)
      (future:complete?))
  => true

  (-> (future:run (fn [] (Thread/sleep 1000)))
      (future:force :exception (ex-info "Error" {}))
      (future:exception?))
  => true)

^{:refer std.lib.future/future:obtrude :added "4.0"}
(fact "like force but uses obtrude and obtrudeException"
  (-> (future:run (fn [] (Thread/sleep 1000)))
      (future:obtrude 10)
      (future:value))
  => 10)

^{:refer std.lib.future/future:dependents :added "3.0"}
(fact "returns number of steps waiting on current result" ^:hidden

  (-> (doto (future:run (fn [] (Thread/sleep 1000)))
        (on:complete (fn [e] e))
        (on:complete (fn [e] e)))
      (future:dependents))
  => 2)

^{:refer std.lib.future/future:lift :added "3.0"}
(fact "creates a future from a value" ^:hidden

  (future? (future:lift (Object.)))
  => true)

^{:refer std.lib.future/on:complete :added "3.0"}
(fact "process both the value and exception" ^:hidden

  @(-> (future 1)
       (on:complete (fn [val _] (throw (ex-info "Error" {:val (inc val)}))))
       (on:complete (fn [_ err] (inc (:val (ex-data err)))))
       (on:complete (fn [val _] (inc val))))
  => 4)

^{:refer std.lib.future/on:timeout :added "3.0"}
(fact "processes a function on timeout" ^:hidden

  (-> (future (Thread/sleep 100))
      (future:timeout 10)
      (on:timeout (fn [_] :timeout))
      (future:wait)
      (future:value))
  => :timeout

  @(-> (future {:timeout 10} (Thread/sleep 100))
       (future:cancel)
       (on:timeout (fn [_] :timeout)))
  => (throws))

^{:refer std.lib.future/on:cancel :added "3.0"}
(fact "processes a function on cancel" ^:hidden

  @(-> (future {:timeout 10} (Thread/sleep 100))
       (future:cancel)
       (on:cancel (fn [_] :cancel)))
  => :cancel

  @(-> (future {:timeout 10} (Thread/sleep 100))
       (future:wait)
       (on:cancel (fn [_] :cancel)))
  => (throws))

^{:refer std.lib.future/on:exception :added "3.0"}
(fact "process a function on exception" ^:hidden

  @(-> (future {:timeout 10} (Thread/sleep 100))
       (future:cancel)
       (on:exception (fn [_] :cancel)))
  => :cancel

  @(-> (future {:timeout 10} (Thread/sleep 100))
       (future:wait)
       (on:exception (fn [_] :timeout)))
  => :timeout

  @(-> (future (throw (ex-info "Error" {})))
       (future:wait)
       (on:exception (fn [_] :exception)))
  => :exception)

^{:refer std.lib.future/on:success :added "3.0"}
(fact "processes another step given successful operation" ^:hidden

  @(-> (future 1)
       (on:success inc)
       (on:success inc)
       (on:success inc))
  => 4)

^{:refer std.lib.future/on:all :added "3.0"}
(fact "calls a function when all futures are complete" ^:hidden

  @(on:all [(future 1)
            (future 2)
            (future 3)]
           (fn [a b c] (+ a b c)))
  => 6)

^{:refer std.lib.future/on:any :added "3.0"}
(fact "calls a function when any future is completed" ^:hidden

  @(on:any [(future (Thread/sleep 500) 1)
            (future (Thread/sleep 500) 2)
            (future 3)]
           (fn [a] a))
  => any)

^{:refer std.lib.future/future :added "3.0"}
(fact "constructs a completable future"

  @(future (Thread/sleep 100)
           1)
  => 1)

^{:refer std.lib.future/then :added "3.0" :style/indent 1}
(fact "shortcut for :on/success and :on/complete" ^:hidden

  @(-> (future (+ 1 2 3))
       (then [a] (+ a 1 2 3)))
  => 12

  @(-> (future (throw (ex-info "Error" {:data 1})))
       (then [a] (+ a 1 2 3))
       (f/catch [ex] (ex-data ex)))
  => {:data 1}

  @(-> (future (+ 1 2 3))
       (then [_ err] err))
  => nil)

^{:refer std.lib.future/catch :added "3.0"  :style/indent 1}
(fact "shortcut for :on/exception" ^:hidden

  @(-> (future (+ 2 3))
       (f/catch [e] e))
  => 5

  @(-> (future (throw (ex-info "" {:a 1})))
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

  (future:result (completed 1))
  => {:status :success, :data 1, :exception nil}

  (future:result (failed (ex-info "" {})))
  => (contains {:status :error
                :data nil
                :exception Throwable}))

^{:refer std.lib.future/future:status :added "3.0"}
(fact "retrieves a status of either `:success`, `:error` or `waiting`"
  (future:status (completed 1))
  => :success)

^{:refer std.lib.future/future:chain :added "3.0"}
(fact "chains a set of functions"

  @(future:chain (completed 1) [inc inc inc])
  => 4

  @(future:chain (failed (ex-info "ERROR" {})) [inc inc inc])
  => (throws))
