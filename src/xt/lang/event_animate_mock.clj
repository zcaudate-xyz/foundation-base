^{:no-test true}
(ns xt.lang.event-animate-mock
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt new-observed
  [v]
  (return {"::" "observed"
           :value v
           :listeners []}))

(defn.xt is-observed
  [x]
  (return (and (k/obj? x)
               (== "observed"
                   (k/get-key x "::")))))

(defn.xt add-listener
  [obs f]
  (var #{listeners} obs)
  (x:arr-push listeners f))

(defn.xt notify-listeners
  [obs]
  (var #{listeners value} obs)
  (k/for:array [listener listeners]
    (listener value)))

(defn.xt get-value
  [obs]
  (var #{value} obs)
  (return value))

(defn.xt set-value
  [obs v]
  (var #{listeners} obs)
  (k/set-key obs "value" v)
  (-/notify-listeners obs))

(defn.xt mock-transition
  "creates a transition from params"
  {:added "4.0"}
  ([indicator tparams transition tf]
   (var [prev curr] transition)
   (return (fn [callback]
             (-/set-value indicator (tf curr))
             (when callback
               (callback))))))

(def.xt MOCK
  {:create-val        -/new-observed
   :add-listener      -/add-listener
   :get-value         -/get-value
   :set-value         -/set-value
   :set-props         (fn [elem props] (k/set-key elem "props" props))
   :is-animated       -/is-observed
   :create-transition -/mock-transition
   :stop-transition   (fn [])})

