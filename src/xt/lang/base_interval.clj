(ns xt.lang.base-interval
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {})

(defspec.xt start-interval
  [:fn [[:fn [] :xt/any] :xt/num] :xt/any])

(defspec.xt stop-interval
  [:fn [:xt/any] :xt/any])

(defn.xt start-interval
  "starts an interval"
  {:added "4.0"}
  ([thunk ms]
   (return
    (x:start-interval thunk ms))))

(defn.xt stop-interval
  "stops the interval from happening"
  {:added "4.0"}
  ([instance]
   (return
    (x:stop-interval instance))))
