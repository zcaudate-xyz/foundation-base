(ns std.lib.future-start
  (:require [std.lib.future :as f]))

(defprotocol IStartable
  (-start [startable]))

(defn startable
  "Creates a startable future.
   This is a factory that, when started, will produce a new CompletableFuture
   each time."
  [thunk]
  (reify
    Object
    (toString [_] (str "#startable[" thunk "]"))

    clojure.lang.IDeref
    (deref [this]
      (let [future (-start this)]
        @future))

    clojure.lang.IFn
    (invoke [this]
      (-start this))

    IStartable
    (-start [_]
      (f/future (thunk)))))

(extend-protocol IStartable
  clojure.lang.Fn
  (-start [thunk]
    (f/future (thunk))))
