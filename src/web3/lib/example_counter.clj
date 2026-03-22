(ns web3.lib.example-counter
  (:require [std.lang :as l]
            [std.lib.env :as env]))

(l/script :solidity
  {:require [[rt.solidity :as s]]})

(defenum.sol CounterLogType
  [:Inc :Dec])

(defevent.sol CounterLog
  [:address user]
  [:% -/CounterLogType eventType]
  [:uint8   counter])

(def.sol ^{:- [:uint :public]}
  g:Counter0)

(def.sol ^{:- [:uint :public]}
  g:Counter1)

(defn.sol ^{:- [:public :view]
            :static/returns [:uint]}
  m:get-counter0
  "gets the 0 counter"
  {:added "4.0"}
  []
  (return -/g:Counter0))

(defn.sol ^{:- [:public :view]
            :static/returns [:uint]}
  m:get-counter1
  "gets the 1 counter"
  {:added "4.0"}
  []
  (return -/g:Counter1))

(defn.sol ^{:- [:public]
            :static/returns [:uint]}
  m:inc-counter0
  "increments counter 0"
  {:added "4.0"}
  []
  (:= -/g:Counter0 (+ -/g:Counter0 1))
  (emit (-/CounterLog s/msg-sender
                 (. -/CounterLogType Inc)
                 1))
  (return -/g:Counter0))

(defn.sol ^{:- [:public]
            :static/returns [:uint]}
  m:inc-counter1
  "increments counter 1"
  {:added "4.0"}
  []
  (:= -/g:Counter1 (+ -/g:Counter1 1))
  (emit (-/CounterLog s/msg-sender
                 (. -/CounterLogType Inc)
                 2))
  (return -/g:Counter1))


(defn.sol ^{:- [:public]
            :static/returns [:uint]}
  m:dec-counter0
  "decrements counter 0"
  {:added "4.0"}
  []
  (:= -/g:Counter0 (- -/g:Counter0 1))
  (emit (-/CounterLog s/msg-sender
                 (. -/CounterLogType Dec)
                 1))
  (return -/g:Counter0))

(defn.sol ^{:- [:public]
            :static/returns [:uint]}
  m:dec-counter1
  "decrements counter 1"
  {:added "4.0"}
  []
  (:= -/g:Counter1 (- -/g:Counter1 1))
  (emit (-/CounterLog s/msg-sender
                 (. -/CounterLogType Dec)
                 2))
  (return -/g:Counter1))


(defn.sol ^{:- [:public]}
  m:inc-both
  "increments both"
  {:added "4.0"}
  []
  (:= -/g:Counter0 (+ -/g:Counter0 1))
  (:= -/g:Counter1 (+ -/g:Counter1 1))
  (emit (-/CounterLog s/msg-sender
                 (. -/CounterLogType Inc)
                 3)))

(defn.sol ^{:- [:public]}
  m:add-both
  "performs add operation"
  {:added "4.0"}
  [:uint n]
  (:= -/g:Counter0 (+ -/g:Counter0 n))
  (:= -/g:Counter1 (+ -/g:Counter1 n)))

(def +default-contract+
  {:ns   (env/ns-sym)
   :name "Counter"
   :args []})
