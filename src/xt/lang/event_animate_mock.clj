^{:no-test true}
(ns xt.lang.event-animate-mock
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt new-observed
  [v]
  (return {"::" "observed"
           :value v
           :listeners []}))

(defn.xt is-observed
  [x]
  (return (and (xt/x:is-object? x)
               (== "observed"
                   (xt/x:get-key x "::")))))

(defn.xt add-listener
  [obs f]
  (var #{listeners} obs)
  (xt/x:arr-push listeners f))

(defn.xt notify-listeners
  [obs]
  (var #{listeners value} obs)
  (xt/for:array [listener listeners]
    (listener value)))

(defn.xt get-value
  [obs]
  (var #{value} obs)
  (return value))

(defn.xt set-value
  [obs v]
  (var #{listeners} obs)
  (xt/x:set-key obs "value" v)
  (-/notify-listeners obs))

(defn.xt set-props
  [elem props]
  (xt/x:set-key elem "props" props))

(defn.xt mock-transition
  "creates a transition from params"
  {:added "4.0"}
  ([indicator tparams transition tf]
   (var [prev curr] transition)
    (var callback-fn
         (fn [callback]
           (-/set-value indicator (tf curr))
           (when callback
             (callback nil))))
    (return callback-fn)))

(def.xt MOCK
  {:create-val        -/new-observed
   :add-listener      -/add-listener
   :get-value         -/get-value
   :set-value         -/set-value
   :set-props         -/set-props
   :is-animated       -/is-observed
   :create-transition -/mock-transition
   :stop-transition   (fn [])})
