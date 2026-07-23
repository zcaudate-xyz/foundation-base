(ns ruby.lang.concurrent-promise
  (:refer-clojure :exclude [promise])
  (:require [hara.lang :as l]))

(l/script :ruby {})

(defn.rb ensure-loaded []
  (require "concurrent-ruby")
  (return true))

(defn.rb promise-native? [value]
  (-/ensure-loaded)
  (return (:- "value.is_a?(Concurrent::Promises::Future)")))

(defn.rb promise [thunk]
  (-/ensure-loaded)
  (var output (:- "Concurrent::Promises.future(&thunk).run"))
  (return output))

(defn.rb async-run [thunk]
  (return (-/promise thunk)))

(defn.rb promise-new [executor]
  (-/ensure-loaded)
  (var future (:- "Concurrent::Promises.resolvable_future"))
  (var resolve
       (:- "lambda do |value|
  if value.is_a?(Concurrent::Promises::Future)
    value.chain do |fulfilled, result, reason|
      fulfilled ? future.fulfill(result, false) : future.reject(reason, false)
    end
  else
    future.fulfill(value, false)
  end
end"))
  (var reject (:- "lambda { |reason| future.reject(reason, false) }"))
  (try
    (. executor (call resolve reject))
    (catch error
      (:- "future.reject(error, false)")))
  (return future))

(defn.rb promise-all [promises]
  (-/ensure-loaded)
  (var output
       (:- "Concurrent::Promises.zip(*promises.map { |value| value.is_a?(Concurrent::Promises::Future) ? value : Concurrent::Promises.fulfilled_future(value) }).then { |*values| values }"))
  (return output))

(defn.rb promise-then [promise thunk]
  (-/ensure-loaded)
  (var output (:- "promise.then(&thunk).run"))
  (return output))

(defn.rb promise-catch [promise thunk]
  (-/ensure-loaded)
  (var output (:- "promise.rescue(&thunk).run"))
  (return output))

(defn.rb promise-finally [promise thunk]
  (-/ensure-loaded)
  (var output
       (:- "promise.chain do |fulfilled, value, reason|
  Concurrent::Promises.future(&thunk).run.then do
    fulfilled ? Concurrent::Promises.fulfilled_future(value) : Concurrent::Promises.rejected_future(reason)
  end.run
end.run"))
  (return output))

(defn.rb with-delay [ms thunk]
  (-/ensure-loaded)
  (var output
       (:- "Concurrent::Promises.schedule(ms / 1000.0, &thunk).run"))
  (return output))
