(ns xt.lang.spec-promise
  (:require [lua.core.common-promise]
            [python.core.common-promise]
            [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt x:async-run [:fn [[:xt/fn]] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:async-run
  "executes a thunk in the host async model and adopts its result"
  {:added "4.1"}
  ([thunk] (list (quote x:async-run) thunk)))

(defspec.xt x:async-bind [:fn [:xt/promise :xt/any :xt/any] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:async-bind
  "binds success and error continuations onto a host promise"
  {:added "4.1"}
  ([promise success error]
   (list (quote x:async-bind) promise success error)))

(defspec.xt x:promise [:fn [[:xt/fn]] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:promise
  "wraps thunk execution in the native host promise type"
  {:added "4.1"}
  ([thunk] (list (quote x:promise) thunk)))

(defspec.xt x:promise-run [:fn [:xt/any] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:promise-run
  "normalises a value into a host promise"
  {:added "4.1"}
  [value]
  (list 'xt.lang.spec-promise/x:promise
        (list 'fn []
              (list 'return value))))

(defspec.xt x:promise-all [:fn [[:xt/array :xt/any]] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:promise-all
  "waits for all values in an array of host promises"
  {:added "4.1"}
  ([promises] (list (quote x:promise-all) promises)))

(defspec.xt x:promise-then [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:promise-then
  "chains a success callback onto a host promise"
  {:added "4.1"}
  ([promise thunk] (list (quote x:promise-then) promise thunk)))

(defspec.xt x:promise-catch [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:promise-catch
  "chains an error callback onto a host promise"
  {:added "4.1"}
  ([promise thunk] (list (quote x:promise-catch) promise thunk)))

(defspec.xt x:promise-finally [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:promise-finally
  "chains a cleanup callback onto a host promise"
  {:added "4.1"}
  ([promise thunk] (list (quote x:promise-finally) promise thunk)))

(defspec.xt x:promise-native? [:fn [:xt/any] :xt/bool])

(defmacro.xt ^{:standalone true}
  x:promise-native?
  "checks whether a value is already a native host promise"
  {:added "4.1"}
  ([value] (list (quote x:promise-native?) value)))

(defspec.xt x:with-delay [:fn [[:xt/fn] :xt/int] :xt/any])

(defmacro.xt ^{:standalone true}
  x:with-delay
  "wraps thunk execution in the native host with-delay type"
  {:added "4.1"}
  ([ms thunk] (list (quote x:with-delay) ms thunk)))
