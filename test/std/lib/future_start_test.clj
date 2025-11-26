(ns std.lib.future-start-test
  (:use code.test)
  (:require [std.lib.future-start :refer :all]
            [std.lib.future :as f]))

(fact "creates a startable future"
  ^:hidden

  (def t (startable (fn [_] 1)))

  (startable? t)
  => true

  (f/future? (t))
  => true

  @(t)
  => 1)

(fact "startable is not executed until invoked"
  ^:hidden

  (def started (atom false))
  (def t (startable (fn [_] (reset! started true) 1)))

  @started => false

  @(t)

  @started => true)

(fact "task function receives the startable instance"
  ^:hidden

  (def t (startable (fn [{:keys [instance]}]
                      (and (startable? instance)
                           (= instance t)))))
  @(t) => true)

(fact "invoking startable returns a completable future"
  ^:hidden

  (def t (startable (fn [_] 1)))
  (f/future? (t)) => true
  @(t) => 1)

(fact "multiple invocations return the same result"
  ^:hidden

  (def counter (atom 0))
  (def t (startable (fn [_] (swap! counter inc))))

  @(t) => 1
  @(t) => 1
  @counter => 1)

(fact "dereferencing a startable"
  ^:hidden

  (def t (startable (fn [_] (Thread/sleep 100) 1)))

  @(t)
  => 1

  @t
  => 1)

(fact "handling failures"
  ^:hidden

  (def t (startable (fn [_] (throw (ex-info "Failed" {})))))

  (try @(t) (catch Exception e (:cause (Throwable->map e))))
  => "Failed"

  (try @t (catch Exception e (:cause (Throwable->map e))))
  => "Failed")
