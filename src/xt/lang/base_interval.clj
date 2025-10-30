(ns xt.lang.base-interval
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:export [MODULE]})

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

(def.xt MODULE (!:module))
